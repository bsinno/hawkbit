/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.builder.RolloutCreate;
import org.eclipse.hawkbit.repository.builder.RolloutGroupCreate;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.RepositoryModelConstants;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupErrorAction;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupErrorCondition;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupSuccessAction;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupSuccessCondition;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditionBuilder;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditions;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.rollout.event.RolloutEvent;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowLayoutComponentBuilder;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AbstractRolloutWindowLayout;
import org.eclipse.hawkbit.ui.rollout.window.layouts.AddRolloutWindowLayout;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Controller for populating and saving data in Add Rollout Window.
 */
public class AddRolloutWindowController implements RolloutWindowController {

    private final RolloutManagement rolloutManagement;
    private final UINotification uiNotification;
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    protected final VaadinMessageSource i18n;

    protected final AddRolloutWindowLayout layout;

    protected ProxyRolloutWindow proxyRolloutWindow;

    public AddRolloutWindowController(final RolloutWindowDependencies dependencies,
            final AddRolloutWindowLayout layout) {
        this.rolloutManagement = dependencies.getRolloutManagement();
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
        // We ignore the method parameter, because we are interested in the
        // empty object, that we can populate with defaults
        proxyRolloutWindow = new ProxyRolloutWindow();

        proxyRolloutWindow.setActionType(ActionType.FORCED);
        proxyRolloutWindow.setForcedTime(LocalDateTime.now().plusWeeks(2)
                .atZone(SPDateTimeUtil.getTimeZoneId(SPDateTimeUtil.getBrowserTimeZone())).toInstant().toEpochMilli());
        final RolloutGroupConditions defaultRolloutGroupConditions = RolloutWindowLayoutComponentBuilder
                .getDefaultRolloutGroupConditions();
        proxyRolloutWindow.setTriggerThresholdPercentage(defaultRolloutGroupConditions.getSuccessConditionExp());
        proxyRolloutWindow.setErrorThresholdPercentage(defaultRolloutGroupConditions.getErrorConditionExp());

        layout.getProxyRolloutBinder().setBean(proxyRolloutWindow);
        layout.addAdvancedGroupRowAndValidate();
        layout.resetGroupsLegendLayout();
    }

    @Override
    public SaveDialogCloseListener getSaveDialogCloseListener() {
        return new SaveDialogCloseListener() {
            @Override
            public void saveOrUpdate() {
                saveRollout();
            }

            @Override
            public boolean canWindowSaveOrUpdate() {
                return duplicateCheck();
            }
        };
    }

    private void saveRollout() {
        final RolloutGroupConditions conditions = new RolloutGroupConditionBuilder()
                .successAction(RolloutGroupSuccessAction.NEXTGROUP, null)
                .successCondition(RolloutGroupSuccessCondition.THRESHOLD,
                        proxyRolloutWindow.getTriggerThresholdPercentage())
                .errorCondition(RolloutGroupErrorCondition.THRESHOLD, proxyRolloutWindow.getErrorThresholdPercentage())
                .errorAction(RolloutGroupErrorAction.PAUSE, null).build();

        final RolloutCreate rolloutCreate = entityFactory.rollout().create().name(proxyRolloutWindow.getName())
                .description(proxyRolloutWindow.getDescription()).set(proxyRolloutWindow.getDistributionSetId())
                .targetFilterQuery(proxyRolloutWindow.getTargetFilterQuery())
                .actionType(proxyRolloutWindow.getActionType())
                .forcedTime(
                        proxyRolloutWindow.getActionType() == ActionType.TIMEFORCED ? proxyRolloutWindow.getForcedTime()
                                : RepositoryModelConstants.NO_FORCE_TIME)
                .startAt(proxyRolloutWindow.getStartAt());

        Rollout rolloutToCreate;
        if (layout.isNumberOfGroups()) {
            rolloutToCreate = rolloutManagement.create(rolloutCreate, proxyRolloutWindow.getNumberOfGroups(),
                    conditions);
        } else if (layout.isGroupsDefinition()) {
            final List<RolloutGroupCreate> groups = layout.getAdvancedRolloutGroups();
            rolloutToCreate = rolloutManagement.create(rolloutCreate, groups, conditions);
        } else {
            throw new IllegalStateException("Either of the Tabs must be selected");
        }

        uiNotification.displaySuccess(i18n.getMessage("message.save.success", rolloutToCreate.getName()));
        eventBus.publish(this, RolloutEvent.CREATE_ROLLOUT);
    }

    private boolean duplicateCheck() {
        if (!StringUtils.hasText(proxyRolloutWindow.getName())) {
            uiNotification.displayValidationError(i18n.getMessage("message.rollout.name.empty"));
            return false;
        }
        if (rolloutManagement.getByName(getTrimmedRolloutName()).isPresent()) {
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
