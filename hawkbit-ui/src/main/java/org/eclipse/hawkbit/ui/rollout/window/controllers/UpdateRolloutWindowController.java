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
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.RolloutModifiedEventPayload;
import org.eclipse.hawkbit.ui.rollout.event.RolloutEvent;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.layouts.UpdateRolloutWindowLayout;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Controller for populating and editing/saving data in Update Rollout Window.
 */
public class UpdateRolloutWindowController extends AbstractEntityWindowController<ProxyRollout, ProxyRolloutWindow> {
    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final RolloutManagement rolloutManagement;
    private final RolloutGroupManagement rolloutGroupManagement;
    private final QuotaManagement quotaManagement;

    private final UpdateRolloutWindowLayout layout;

    private String nameBeforeEdit;

    public UpdateRolloutWindowController(final RolloutWindowDependencies dependencies,
            final UpdateRolloutWindowLayout layout) {
        this.i18n = dependencies.getI18n();
        this.entityFactory = dependencies.getEntityFactory();
        this.eventBus = dependencies.getEventBus();
        this.uiNotification = dependencies.getUiNotification();

        this.rolloutManagement = dependencies.getRolloutManagement();
        this.rolloutGroupManagement = dependencies.getRolloutGroupManagement();
        this.quotaManagement = dependencies.getQuotaManagement();

        this.layout = layout;
    }

    @Override
    public AbstractEntityWindowLayout<ProxyRolloutWindow> getLayout() {
        return layout;
    }

    @Override
    protected ProxyRolloutWindow buildEntityFromProxy(final ProxyRollout proxyEntity) {
        final ProxyRolloutWindow proxyRolloutWindow = new ProxyRolloutWindow(proxyEntity);

        nameBeforeEdit = proxyRolloutWindow.getName();

        return proxyRolloutWindow;
    }

    @Override
    protected void adaptLayout() {
        if (layout.getEntity().getStatus() != Rollout.RolloutStatus.READY) {
            layout.disableRequiredFieldsOnEdit();
        }

        layout.populateTotalTargetsLegend();
        layout.updateGroupsChart(
                rolloutGroupManagement.findByRollout(PageRequest.of(0, quotaManagement.getMaxRolloutGroupsPerRollout()),
                        layout.getEntity().getId()).getContent(),
                layout.getEntity().getTotalTargets());
    }

    @Override
    protected void persistEntity(final ProxyRolloutWindow entity) {
        final RolloutUpdate rolloutUpdate = entityFactory.rollout().update(entity.getId()).name(entity.getName())
                .description(entity.getDescription()).set(entity.getDistributionSetId())
                .actionType(entity.getActionType())
                .forcedTime(entity.getActionType() == ActionType.TIMEFORCED ? entity.getForcedTime()
                        : RepositoryModelConstants.NO_FORCE_TIME)
                .startAt(entity.getStartAt());

        Rollout updatedRollout;
        try {
            updatedRollout = rolloutManagement.update(rolloutUpdate);
        } catch (final EntityNotFoundException | EntityReadOnlyException e) {
            // TODO: use i18n
            uiNotification.displayWarning(
                    "Rollout with name " + entity.getName() + " was deleted or you are not allowed to update it");
            eventBus.publish(this, RolloutEvent.SHOW_ROLLOUTS);
            return;
        }

        if (updatedRollout.getStatus().equals(Rollout.RolloutStatus.WAITING_FOR_APPROVAL)) {
            rolloutManagement.approveOrDeny(updatedRollout.getId(), entity.getApprovalDecision(),
                    entity.getApprovalRemark());
        }

        uiNotification.displaySuccess(i18n.getMessage("message.update.success", updatedRollout.getName()));
        // TODO: verify if sender is correct
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new RolloutModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, updatedRollout.getId()));
    }

    @Override
    protected boolean isEntityValid(final ProxyRolloutWindow entity) {
        if (!StringUtils.hasText(entity.getName())) {
            uiNotification.displayValidationError(i18n.getMessage("message.rollout.name.empty"));
            return false;
        }

        final String trimmedName = StringUtils.trimWhitespace(entity.getName());
        if (!nameBeforeEdit.equals(trimmedName) && rolloutManagement.getByName(trimmedName).isPresent()) {
            // TODO: is the notification right here?
            uiNotification.displayValidationError(i18n.getMessage("message.rollout.duplicate.check", trimmedName));
            return false;
        }

        return true;
    }
}