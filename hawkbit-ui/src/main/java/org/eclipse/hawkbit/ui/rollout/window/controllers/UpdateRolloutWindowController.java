/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.controllers;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.builder.RolloutUpdate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.EntityReadOnlyException;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.RepositoryModelConstants;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.rollout.event.RolloutEvent;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AbstractRolloutWindowLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.UpdateRolloutWindowLayout;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus;

/**
 * Controller for populating and editing/saving data in Update Rollout Window.
 */
public class UpdateRolloutWindowController implements RolloutWindowController {

    private final RolloutManagement rolloutManagement;
    private final RolloutGroupManagement rolloutGroupManagement;
    private final QuotaManagement quotaManagement;
    private final UINotification uiNotification;
    private final EntityFactory entityFactory;
    private final VaadinMessageSource i18n;
    private final EventBus.UIEventBus eventBus;

    private final UpdateRolloutWindowLayout layout;

    private ProxyRolloutWindow proxyRolloutWindow;
    private String rolloutNameBeforeEdit;

    public UpdateRolloutWindowController(final RolloutWindowDependencies dependencies,
            final UpdateRolloutWindowLayout layout) {
        this.rolloutManagement = dependencies.getRolloutManagement();
        this.rolloutGroupManagement = dependencies.getRolloutGroupManagement();
        this.quotaManagement = dependencies.getQuotaManagement();
        this.uiNotification = dependencies.getUiNotification();
        this.entityFactory = dependencies.getEntityFactory();
        this.i18n = dependencies.getI18n();
        this.eventBus = dependencies.getEventBus();
        this.layout = layout;
    }

    @Override
    public AbstractRolloutWindowLayout getLayout() {
        return layout;
    }

    @Override
    public void populateWithData(final ProxyRollout proxyRollout) {
        proxyRolloutWindow = new ProxyRolloutWindow(proxyRollout);

        rolloutNameBeforeEdit = proxyRolloutWindow.getName();

        if (proxyRolloutWindow.getStatus() != Rollout.RolloutStatus.READY) {
            layout.disableRequiredFieldsOnEdit();
        }

        layout.getProxyRolloutBinder().setBean(proxyRolloutWindow);
        layout.populateTotalTargetsLegend();
        layout.updateGroupsChart(
                rolloutGroupManagement.findByRollout(PageRequest.of(0, quotaManagement.getMaxRolloutGroupsPerRollout()),
                        proxyRolloutWindow.getId()).getContent(),
                proxyRolloutWindow.getTotalTargets());
    }

    @Override
    public SaveDialogCloseListener getSaveDialogCloseListener() {
        return new SaveDialogCloseListener() {
            @Override
            public void saveOrUpdate() {
                editRollout();
            }

            @Override
            public boolean canWindowSaveOrUpdate() {
                return duplicateCheckForEdit();
            }
        };
    }

    private void editRollout() {
        if (proxyRolloutWindow == null) {
            return;
        }

        final RolloutUpdate rolloutUpdate = entityFactory.rollout().update(proxyRolloutWindow.getId())
                .name(proxyRolloutWindow.getName()).description(proxyRolloutWindow.getDescription())
                .set(proxyRolloutWindow.getDistributionSetId()).actionType(proxyRolloutWindow.getActionType())
                .forcedTime(
                        proxyRolloutWindow.getActionType() == ActionType.TIMEFORCED ? proxyRolloutWindow.getForcedTime()
                                : RepositoryModelConstants.NO_FORCE_TIME)
                .startAt(proxyRolloutWindow.getStartAt());

        Rollout updatedRollout;
        try {
            updatedRollout = rolloutManagement.update(rolloutUpdate);
        } catch (final EntityNotFoundException | EntityReadOnlyException e) {
            uiNotification.displayWarning(
                    "Rollout with name " + proxyRolloutWindow.getName() + " was deleted. Update is not possible");
            eventBus.publish(this, RolloutEvent.SHOW_ROLLOUTS);
            return;
        }

        if (proxyRolloutWindow.getStatus().equals(Rollout.RolloutStatus.WAITING_FOR_APPROVAL)) {
            rolloutManagement.approveOrDeny(proxyRolloutWindow.getId(), proxyRolloutWindow.getApprovalDecision(),
                    proxyRolloutWindow.getApprovalRemark());
        }

        uiNotification.displaySuccess(i18n.getMessage("message.update.success", updatedRollout.getName()));
        eventBus.publish(this, RolloutEvent.UPDATE_ROLLOUT);
    }

    private boolean duplicateCheckForEdit() {
        if (!StringUtils.hasText(proxyRolloutWindow.getName())) {
            uiNotification.displayValidationError(i18n.getMessage("message.rollout.name.empty"));
            return false;
        }
        if (!rolloutNameBeforeEdit.equals(getTrimmedRolloutName())
                && rolloutManagement.getByName(getTrimmedRolloutName()).isPresent()) {
            uiNotification.displayValidationError(
                    i18n.getMessage("message.rollout.duplicate.check", getTrimmedRolloutName()));
            return false;
        }
        return true;
    }

    private String getTrimmedRolloutName() {
        return StringUtils.trimWhitespace(proxyRolloutWindow.getName());
    }
}
