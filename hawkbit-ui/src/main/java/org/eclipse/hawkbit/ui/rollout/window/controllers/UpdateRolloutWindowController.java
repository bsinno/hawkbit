/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.builder.RolloutUpdate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.EntityReadOnlyException;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.RepositoryModelConstants;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.Rollout.ApprovalDecision;
import org.eclipse.hawkbit.repository.model.Rollout.RolloutStatus;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
import org.eclipse.hawkbit.ui.common.EntityWindowLayout;
import org.eclipse.hawkbit.ui.common.data.mappers.RolloutGroupToAdvancedDefinitionMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAdvancedRolloutGroup;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow.GroupDefinitionMode;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.rollout.event.RolloutEvent;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AutoStartOptionGroupLayout.AutoStartOption;
import org.eclipse.hawkbit.ui.rollout.window.layouts.UpdateRolloutWindowLayout;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Controller for populating and editing/saving data in Update Rollout Window.
 */
public class UpdateRolloutWindowController extends AbstractEntityWindowController<ProxyRollout, ProxyRolloutWindow> {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateRolloutWindowController.class);

    private final VaadinMessageSource i18n;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;

    private final TargetFilterQueryManagement targetFilterQueryManagement;
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

        this.targetFilterQueryManagement = dependencies.getTargetFilterQueryManagement();
        this.rolloutManagement = dependencies.getRolloutManagement();
        this.rolloutGroupManagement = dependencies.getRolloutGroupManagement();
        this.quotaManagement = dependencies.getQuotaManagement();

        this.layout = layout;
    }

    @Override
    public EntityWindowLayout<ProxyRolloutWindow> getLayout() {
        return layout;
    }

    @Override
    protected ProxyRolloutWindow buildEntityFromProxy(final ProxyRollout proxyEntity) {
        final ProxyRolloutWindow proxyRolloutWindow = new ProxyRolloutWindow(proxyEntity);

        if (proxyRolloutWindow.getForcedTime() == null
                || RepositoryModelConstants.NO_FORCE_TIME.equals(proxyRolloutWindow.getForcedTime())) {
            proxyRolloutWindow.setForcedTime(SPDateTimeUtil.twoWeeksFromNowEpochMilli());
        }

        proxyRolloutWindow.setAutoStartOption(proxyRolloutWindow.getOptionByStartAt());
        if (AutoStartOption.SCHEDULED != proxyRolloutWindow.getAutoStartOption()) {
            proxyRolloutWindow.setStartAt(SPDateTimeUtil.halfAnHourFromNowEpochMilli());
        }

        proxyRolloutWindow.setGroupDefinitionMode(GroupDefinitionMode.ADVANCED);
        setRolloutGroups(proxyRolloutWindow);

        if (!StringUtils.isEmpty(proxyEntity.getApprovalDecidedBy())) {
            proxyRolloutWindow.setApprovalDecision(
                    RolloutStatus.APPROVAL_DENIED == proxyEntity.getStatus() ? ApprovalDecision.DENIED
                            : ApprovalDecision.APPROVED);
            proxyRolloutWindow.setApprovalRemark(proxyEntity.getApprovalRemark());
        }

        nameBeforeEdit = proxyRolloutWindow.getName();

        return proxyRolloutWindow;
    }

    // TODO: try to remove duplication with CopyRolloutWindowController
    private void setRolloutGroups(final ProxyRolloutWindow proxyRolloutWindow) {
        final List<RolloutGroup> rolloutGroups = rolloutGroupManagement
                .findByRollout(PageRequest.of(0, quotaManagement.getMaxRolloutGroupsPerRollout()),
                        proxyRolloutWindow.getId())
                .getContent();

        final RolloutGroupToAdvancedDefinitionMapper mapper = new RolloutGroupToAdvancedDefinitionMapper(
                targetFilterQueryManagement);
        final List<ProxyAdvancedRolloutGroup> advancedRolloutGroupDefinitions = rolloutGroups.stream().map(mapper::map)
                .collect(Collectors.toList());

        proxyRolloutWindow.setAdvancedRolloutGroupDefinitions(advancedRolloutGroupDefinitions);
    }

    @Override
    protected void adaptLayout(final ProxyRollout proxyEntity) {
        if (Rollout.RolloutStatus.READY == proxyEntity.getStatus()
                || Rollout.RolloutStatus.WAITING_FOR_APPROVAL == proxyEntity.getStatus()) {
            layout.adaptForPendingStatus();
        } else {
            layout.adaptForStartedStatus();
        }

        layout.resetValidation();
    }

    @Override
    protected void persistEntity(final ProxyRolloutWindow entity) {
        final RolloutUpdate rolloutUpdate = entityFactory.rollout().update(entity.getId()).name(entity.getName())
                .description(entity.getDescription()).set(entity.getDistributionSetId())
                .actionType(entity.getActionType())
                .forcedTime(entity.getActionType() == ActionType.TIMEFORCED ? entity.getForcedTime()
                        : RepositoryModelConstants.NO_FORCE_TIME)
                .startAt(entity.getStartAtByOption());

        Rollout updatedRollout;
        try {
            updatedRollout = rolloutManagement.update(rolloutUpdate);
        } catch (final EntityNotFoundException | EntityReadOnlyException e) {
            LOG.trace("Update of rollout failed in UI: {}", e.getMessage());
            // TODO: add 18n
            uiNotification.displayWarning(
                    "Rollout with name " + entity.getName() + " was deleted or you are not allowed to update it");
            eventBus.publish(this, RolloutEvent.SHOW_ROLLOUTS);
            return;
        }

        if (Rollout.RolloutStatus.WAITING_FOR_APPROVAL == updatedRollout.getStatus()) {
            rolloutManagement.approveOrDeny(updatedRollout.getId(), entity.getApprovalDecision(),
                    entity.getApprovalRemark());
        }

        uiNotification.displaySuccess(i18n.getMessage("message.update.success", updatedRollout.getName()));
        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_UPDATED, ProxyRollout.class, updatedRollout.getId()));
    }

    @Override
    protected boolean isEntityValid(final ProxyRolloutWindow entity) {
        if (entity == null) {
            uiNotification
                    .displayValidationError(i18n.getMessage("message.save.fail", i18n.getMessage("caption.rollout")));
            return false;
        }

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

        if (Rollout.RolloutStatus.WAITING_FOR_APPROVAL == entity.getStatus() && entity.getApprovalDecision() == null) {
            // TODO: add 18n
            uiNotification.displayValidationError("You should approve or reject the Rollout");
            return false;
        }

        return true;
    }
}
