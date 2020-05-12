/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.actionhistory;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.exception.CancelActionNotAllowedException;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.Action.Status;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.ConfirmationDialog;
import org.eclipse.hawkbit.ui.common.builder.GridComponentBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.ActionToProxyActionMapper;
import org.eclipse.hawkbit.ui.common.data.providers.ActionDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction.IsActiveDecoration;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterEntitySupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * This grid presents the action history for a selected target.
 */
public class ActionHistoryGrid extends AbstractGrid<ProxyAction, String> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(ActionHistoryGrid.class);

    private static final String ACTION_ID = "id";
    private static final String DS_NAME_VERSION_ID = "dsNameVersion";
    private static final String ROLLOUT_NAME_ID = "rolloutName";
    private static final String MAINTENANCE_WINDOW_ID = "maintenanceWindow";
    private static final String LAST_MODIFIED_AT_ID = "lastModifiedAt";
    private static final String STATUS_ID = "status";
    private static final String ACTIVE_STATUS_ID = "isActiveDecoration";
    private static final String TYPE_ID = "type";
    private static final String TIME_FORCED_ID = "timeForced";

    private static final String CANCEL_BUTTON_ID = "cancel-action";
    private static final String FORCE_BUTTON_ID = "force-action";
    private static final String FORCE_QUIT_BUTTON_ID = "force-quit-action";

    private final UINotification notification;
    private final transient DeploymentManagement deploymentManagement;
    private final transient ActionToProxyActionMapper actionToProxyActionMapper;

    private final Map<Status, ProxyFontIcon> statusIconMap = new EnumMap<>(Status.class);
    private final Map<IsActiveDecoration, ProxyFontIcon> activeStatusIconMap = new EnumMap<>(IsActiveDecoration.class);
    private final Map<ActionType, ProxyFontIcon> actionTypeIconMap = new EnumMap<>(ActionType.class);

    private final transient MasterEntitySupport<ProxyTarget> masterEntitySupport;

    ActionHistoryGrid(final VaadinMessageSource i18n, final DeploymentManagement deploymentManagement,
            final UIEventBus eventBus, final UINotification notification, final SpPermissionChecker permissionChecker,
            final ActionHistoryGridLayoutUiState actionHistoryGridLayoutUiState) {
        super(i18n, eventBus, permissionChecker);

        this.notification = notification;
        this.deploymentManagement = deploymentManagement;
        this.actionToProxyActionMapper = new ActionToProxyActionMapper();

        // currently we do not restore action history selection
        setSelectionSupport(new SelectionSupport<ProxyAction>(this, eventBus, EventLayout.ACTION_HISTORY_LIST,
                EventView.DEPLOYMENT, this::mapIdToProxyEntity, null, null));
        if (actionHistoryGridLayoutUiState.isMaximized()) {
            getSelectionSupport().enableSingleSelection();
        } else {
            getSelectionSupport().disableSelection();
        }

        setFilterSupport(new FilterSupport<>(new ActionDataProvider(deploymentManagement, actionToProxyActionMapper)));
        initFilterMappings();

        this.masterEntitySupport = new MasterEntitySupport<>(getFilterSupport(), ProxyTarget::getControllerId);

        initStatusIconMap();
        initActiveStatusIconMap();
        initActionTypeIconMap();

        init();
    }

    public Optional<ProxyAction> mapIdToProxyEntity(final long entityId) {
        return deploymentManagement.findAction(entityId).map(actionToProxyActionMapper::map);
    }

    private void initFilterMappings() {
        getFilterSupport().<String> addMapping(FilterType.MASTER,
                (filter, masterFilter) -> getFilterSupport().setFilter(masterFilter));
    }

    private void initStatusIconMap() {
        statusIconMap.put(Status.FINISHED, new ProxyFontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getStatusDescription(Status.FINISHED)));
        statusIconMap.put(Status.SCHEDULED, new ProxyFontIcon(VaadinIcons.HOURGLASS_EMPTY,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(Status.SCHEDULED)));
        statusIconMap.put(Status.RUNNING, new ProxyFontIcon(VaadinIcons.ADJUST,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(Status.RUNNING)));
        statusIconMap.put(Status.RETRIEVED, new ProxyFontIcon(VaadinIcons.CHECK_CIRCLE_O,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(Status.RETRIEVED)));
        statusIconMap.put(Status.WARNING, new ProxyFontIcon(VaadinIcons.EXCLAMATION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_ORANGE, getStatusDescription(Status.WARNING)));
        statusIconMap.put(Status.DOWNLOAD, new ProxyFontIcon(VaadinIcons.CLOUD_DOWNLOAD,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(Status.DOWNLOAD)));
        statusIconMap.put(Status.DOWNLOADED, new ProxyFontIcon(VaadinIcons.CLOUD_DOWNLOAD,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getStatusDescription(Status.DOWNLOADED)));
        statusIconMap.put(Status.CANCELING, new ProxyFontIcon(VaadinIcons.CLOSE_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getStatusDescription(Status.CANCELING)));
        statusIconMap.put(Status.CANCELED, new ProxyFontIcon(VaadinIcons.CLOSE_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getStatusDescription(Status.CANCELED)));
        statusIconMap.put(Status.ERROR, new ProxyFontIcon(VaadinIcons.EXCLAMATION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_RED, getStatusDescription(Status.ERROR)));
    }

    private String getStatusDescription(final Status actionStatus) {
        return i18n
                .getMessage(UIMessageIdProvider.TOOLTIP_ACTION_STATUS_PREFIX + actionStatus.toString().toLowerCase());
    }

    private void initActiveStatusIconMap() {
        activeStatusIconMap.put(IsActiveDecoration.ACTIVE, new ProxyFontIcon(null,
                SPUIStyleDefinitions.STATUS_ICON_ACTIVE, getActiveStatusDescription(IsActiveDecoration.ACTIVE)));
        activeStatusIconMap.put(IsActiveDecoration.SCHEDULED, new ProxyFontIcon(VaadinIcons.HOURGLASS_EMPTY,
                SPUIStyleDefinitions.STATUS_ICON_PENDING, getActiveStatusDescription(IsActiveDecoration.SCHEDULED)));
        activeStatusIconMap.put(IsActiveDecoration.IN_ACTIVE, new ProxyFontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_NEUTRAL, getActiveStatusDescription(IsActiveDecoration.IN_ACTIVE)));
        activeStatusIconMap.put(IsActiveDecoration.IN_ACTIVE_ERROR, new ProxyFontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_RED, getActiveStatusDescription(IsActiveDecoration.IN_ACTIVE_ERROR)));
    }

    private String getActiveStatusDescription(final IsActiveDecoration activeActionStatus) {
        return i18n
                .getMessage(UIMessageIdProvider.TOOLTIP_ACTIVE_ACTION_STATUS_PREFIX + activeActionStatus.getMsgName());
    }

    private void initActionTypeIconMap() {
        actionTypeIconMap.put(ActionType.FORCED, new ProxyFontIcon(VaadinIcons.BOLT,
                SPUIStyleDefinitions.STATUS_ICON_FORCED, i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_FORCED)));
        actionTypeIconMap.put(ActionType.TIMEFORCED, new ProxyFontIcon(VaadinIcons.BOLT,
                SPUIStyleDefinitions.STATUS_ICON_FORCED, i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_FORCED)));
        actionTypeIconMap.put(ActionType.SOFT, new ProxyFontIcon(VaadinIcons.STEP_FORWARD,
                SPUIStyleDefinitions.STATUS_ICON_SOFT, i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_SOFT)));
        actionTypeIconMap.put(ActionType.DOWNLOAD_ONLY,
                new ProxyFontIcon(VaadinIcons.DOWNLOAD, SPUIStyleDefinitions.STATUS_ICON_DOWNLOAD_ONLY,
                        i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_DOWNLOAD_ONLY)));
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.ACTION_HISTORY_GRID_ID;
    }

    /**
     * Creates the grid content for maximized-state.
     */
    @Override
    public void createMaximizedContent() {
        createMaximizedContent(SelectionMode.SINGLE);
    }

    /**
     * Creates the grid content for normal (minimized) state.
     */
    @Override
    public void createMinimizedContent() {
        createMinimizedContent(SelectionMode.NONE);
    }

    @Override
    public void addColumns() {
        addActiveStatusColumn().setMinimumWidth(30d).setMaximumWidth(50d).setHidable(true);

        addDsColumn().setMinimumWidth(80d).setMaximumWidth(110d).setHidable(true);

        addDateAndTimeColumn().setMinimumWidth(80d).setMaximumWidth(110d).setHidable(true);

        addStatusColumn().setMinimumWidth(30d).setMaximumWidth(53d).setHidable(true);

        addMaintenanceWindowColumn().setMinimumWidth(150d).setMaximumWidth(150d).setHidable(true).setHidden(true);

        addTypeColumn().setWidth(25d);
        addTimeforcedColumn().setWidth(25d);
        getDefaultHeaderRow().join(TYPE_ID, TIME_FORCED_ID).setText(i18n.getMessage("label.action.type"));

        addCancelColumn().setWidth(25d);
        addForceColumn().setWidth(25d);
        addForceQuitColumn().setWidth(25d);
        getDefaultHeaderRow().join(CANCEL_BUTTON_ID, FORCE_BUTTON_ID, FORCE_QUIT_BUTTON_ID)
                .setText(i18n.getMessage("header.action"));
    }

    private Column<ProxyAction, Label> addActiveStatusColumn() {
        return addComponentColumn(this::buildActiveStatusIcon).setId(ACTIVE_STATUS_ID)
                .setCaption(i18n.getMessage("label.active")).setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);
    }

    private Label buildActiveStatusIcon(final ProxyAction action) {
        final ProxyFontIcon activeStatusFontIcon = Optional
                .ofNullable(activeStatusIconMap.get(action.getIsActiveDecoration()))
                .orElse(new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String activeStatusId = new StringBuilder(UIComponentIdProvider.ACTION_HISTORY_TABLE_ACTIVESTATE_LABEL_ID)
                .append(".").append(action.getId()).toString();

        return SPUIComponentProvider.getLabelIcon(activeStatusFontIcon, activeStatusId);
    }

    private Column<ProxyAction, String> addDsColumn() {
        return addColumn(ProxyAction::getDsNameVersion).setId(DS_NAME_VERSION_ID)
                .setCaption(i18n.getMessage("distribution.details.header"));
    }

    private Column<ProxyAction, String> addDateAndTimeColumn() {
        return addColumn(action -> SPDateTimeUtil.getFormattedDate(action.getLastModifiedAt(),
                SPUIDefinitions.LAST_QUERY_DATE_FORMAT_SHORT)).setId(LAST_MODIFIED_AT_ID)
                        .setCaption(i18n.getMessage("header.rolloutgroup.target.date"))
                        .setDescriptionGenerator(action -> SPDateTimeUtil.getFormattedDate(action.getLastModifiedAt()));
    }

    private Column<ProxyAction, Label> addStatusColumn() {
        return addComponentColumn(this::buildStatusIcon).setId(STATUS_ID).setCaption(i18n.getMessage("header.status"))
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);
    }

    private Label buildStatusIcon(final ProxyAction action) {
        final ProxyFontIcon statusFontIcon = Optional.ofNullable(statusIconMap.get(action.getStatus()))
                .orElse(new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String statusId = new StringBuilder(UIComponentIdProvider.ACTION_HISTORY_TABLE_STATUS_LABEL_ID)
                .append(".").append(action.getId()).toString();

        return SPUIComponentProvider.getLabelIcon(statusFontIcon, statusId);
    }

    private Column<ProxyAction, String> addMaintenanceWindowColumn() {
        return addColumn(ProxyAction::getMaintenanceWindow).setId(MAINTENANCE_WINDOW_ID)
                .setCaption(i18n.getMessage("header.maintenancewindow")).setDescriptionGenerator(action -> action
                        .getMaintenanceWindowStartTime().map(this::getFormattedNextMaintenanceWindow).orElse(null));
    }

    private String getFormattedNextMaintenanceWindow(final ZonedDateTime nextAt) {
        final long nextAtMilli = nextAt.toInstant().toEpochMilli();
        return i18n.getMessage(UIMessageIdProvider.TOOLTIP_NEXT_MAINTENANCE_WINDOW,
                SPDateTimeUtil.getFormattedDate(nextAtMilli, SPUIDefinitions.LAST_QUERY_DATE_FORMAT_SHORT));
    }

    private Column<ProxyAction, Label> addTypeColumn() {
        return addComponentColumn(this::buildTypeIcon).setId(TYPE_ID)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);
    }

    private Label buildTypeIcon(final ProxyAction action) {
        final ProxyFontIcon actionTypeFontIcon = Optional.ofNullable(actionTypeIconMap.get(action.getActionType()))
                .orElse(new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String actionTypeId = new StringBuilder(UIComponentIdProvider.ACTION_HISTORY_TABLE_TYPE_LABEL_ID)
                .append(".").append(action.getId()).toString();

        return SPUIComponentProvider.getLabelIcon(actionTypeFontIcon, actionTypeId);
    }

    private Column<ProxyAction, Label> addTimeforcedColumn() {
        return addComponentColumn(this::buildTimeforcedIcon).setId(TIME_FORCED_ID)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);
    }

    private Label buildTimeforcedIcon(final ProxyAction action) {
        if (ActionType.TIMEFORCED != action.getActionType()) {
            return null;
        }

        final long currentTimeMillis = System.currentTimeMillis();
        String style;
        String description;
        if (action.isHitAutoForceTime(currentTimeMillis)) {
            style = SPUIStyleDefinitions.STATUS_ICON_GREEN;
            final String duration = SPDateTimeUtil.getDurationFormattedString(action.getForcedTime(), currentTimeMillis,
                    i18n);
            description = i18n.getMessage(UIMessageIdProvider.TOOLTIP_TIMEFORCED_FORCED_SINCE, duration);
        } else {
            style = SPUIStyleDefinitions.STATUS_ICON_PENDING;
            final String duration = SPDateTimeUtil.getDurationFormattedString(currentTimeMillis, action.getForcedTime(),
                    i18n);
            description = i18n.getMessage(UIMessageIdProvider.TOOLTIP_TIMEFORCED_FORCED_IN, duration);
        }

        final ProxyFontIcon timeforcedFontIcon = new ProxyFontIcon(VaadinIcons.TIMER, style, description);

        final String actionTypeId = new StringBuilder(UIComponentIdProvider.ACTION_HISTORY_TABLE_TIMEFORCED_LABEL_ID)
                .append(".").append(action.getId()).toString();

        return SPUIComponentProvider.getLabelIcon(timeforcedFontIcon, actionTypeId);
    }

    private Column<ProxyAction, Button> addCancelColumn() {
        return addComponentColumn(
                action -> GridComponentBuilder.buildActionButton(i18n,
                        clickEvent -> confirmAndCancelAction(action.getId()), VaadinIcons.CLOSE_SMALL,
                        "message.cancel.action", SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                        UIComponentIdProvider.ACTION_HISTORY_TABLE_CANCEL_ID + "." + action.getId(), action.isActive()
                                && !action.isCancelingOrCanceled() && permissionChecker.hasUpdateTargetPermission()))
                                        .setId(CANCEL_BUTTON_ID);
    }

    /**
     * Show confirmation window and if ok then only, cancel the action.
     *
     * @param actionId
     *            as Id if the action needs to be cancelled.
     */
    private void confirmAndCancelAction(final Long actionId) {
        final ConfirmationDialog confirmDialog = new ConfirmationDialog(
                i18n.getMessage("caption.cancel.action.confirmbox"), i18n.getMessage("message.cancel.action.confirm"),
                i18n.getMessage(UIMessageIdProvider.BUTTON_OK), i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL),
                ok -> {
                    if (ok) {
                        cancelActiveAction(actionId);
                    }
                }, UIComponentIdProvider.CONFIRMATION_POPUP_ID);
        UI.getCurrent().addWindow(confirmDialog.getWindow());
        confirmDialog.getWindow().bringToFront();
    }

    private void cancelActiveAction(final Long actionId) {
        try {
            deploymentManagement.cancelAction(actionId);

            notification.displaySuccess(i18n.getMessage("message.cancel.action.success"));
            publishEntityModifiedEvent(actionId);
        } catch (final CancelActionNotAllowedException e) {
            LOG.trace("Cancel action not allowed exception: {}", e.getMessage());
            notification.displayValidationError(i18n.getMessage("message.cancel.action.failed"));
        }
    }

    private void publishEntityModifiedEvent(final Long actionId) {
        if (masterEntitySupport.getMasterId() != null) {
            eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                    new EntityModifiedEventPayload(EntityModifiedEventType.ENTITY_UPDATED, ProxyTarget.class,
                            masterEntitySupport.getMasterId(), ProxyAction.class, actionId));
        }
    }

    private Column<ProxyAction, Button> addForceColumn() {
        return addComponentColumn(action -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> confirmAndForceAction(action.getId()), VaadinIcons.BOLT, "message.force.action",
                SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.ACTION_HISTORY_TABLE_FORCE_ID + "." + action.getId(),
                action.isActive() && !action.isForce() && !action.isCancelingOrCanceled()
                        && permissionChecker.hasUpdateTargetPermission())).setId(FORCE_BUTTON_ID);
    }

    /**
     * Show confirmation window and if ok then only, force the action.
     *
     * @param actionId
     *            as Id if the action needs to be forced.
     */
    private void confirmAndForceAction(final Long actionId) {
        final ConfirmationDialog confirmDialog = new ConfirmationDialog(
                i18n.getMessage("caption.force.action.confirmbox"), i18n.getMessage("message.force.action.confirm"),
                i18n.getMessage(UIMessageIdProvider.BUTTON_OK), i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL),
                ok -> {
                    if (ok) {
                        forceActiveAction(actionId);
                    }
                }, UIComponentIdProvider.CONFIRMATION_POPUP_ID);
        UI.getCurrent().addWindow(confirmDialog.getWindow());

        confirmDialog.getWindow().bringToFront();
    }

    private void forceActiveAction(final Long actionId) {
        try {
            deploymentManagement.forceTargetAction(actionId);

            notification.displaySuccess(i18n.getMessage("message.force.action.success"));
            publishEntityModifiedEvent(actionId);
        } catch (final EntityNotFoundException e) {
            LOG.trace("Action was not found during force command: {}", e.getMessage());
            notification.displayValidationError(i18n.getMessage("message.force.action.failed"));
        }
    }

    private Column<ProxyAction, Button> addForceQuitColumn() {
        return addComponentColumn(action -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> confirmAndForceQuitAction(action.getId()), VaadinIcons.CLOSE_SMALL,
                "message.forcequit.action", SPUIStyleDefinitions.STATUS_ICON_RED,
                UIComponentIdProvider.ACTION_HISTORY_TABLE_FORCE_QUIT_ID + "." + action.getId(),
                action.isActive() && action.isCancelingOrCanceled() && permissionChecker.hasUpdateTargetPermission()))
                        .setId(FORCE_QUIT_BUTTON_ID);
    }

    /**
     * Show confirmation window and if ok then only, force quit action.
     *
     * @param actionId
     *            as Id if the action needs to be forced.
     */
    private void confirmAndForceQuitAction(final Long actionId) {
        final ConfirmationDialog confirmDialog = new ConfirmationDialog(
                i18n.getMessage("caption.forcequit.action.confirmbox"),
                i18n.getMessage("message.forcequit.action.confirm"), i18n.getMessage(UIMessageIdProvider.BUTTON_OK),
                i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL), ok -> {
                    if (ok) {
                        forceQuitActiveAction(actionId);
                    }
                }, VaadinIcons.WARNING, UIComponentIdProvider.CONFIRMATION_POPUP_ID, null);
        UI.getCurrent().addWindow(confirmDialog.getWindow());

        confirmDialog.getWindow().bringToFront();
    }

    private void forceQuitActiveAction(final Long actionId) {
        try {
            deploymentManagement.forceQuitAction(actionId);

            notification.displaySuccess(i18n.getMessage("message.forcequit.action.success"));
            publishEntityModifiedEvent(actionId);
        } catch (final CancelActionNotAllowedException e) {
            LOG.trace("Force Cancel action not allowed exception: {}", e.getMessage());
            notification.displayValidationError(i18n.getMessage("message.forcequit.action.failed"));
        }
    }

    @Override
    protected void addMaxColumns() {
        addActiveStatusColumn().setMinimumWidth(30d).setExpandRatio(1).setHidable(true);

        addActionIdColumn().setMinimumWidth(50d).setExpandRatio(1).setHidable(true);

        addDsColumn().setMinimumWidth(80d).setExpandRatio(1).setHidable(true);

        addDateAndTimeColumn().setMinimumWidth(80d).setExpandRatio(1).setHidable(true);

        addStatusColumn().setMinimumWidth(30d).setExpandRatio(1).setHidable(true);

        addMaintenanceWindowColumn().setMinimumWidth(150d).setExpandRatio(1).setHidable(true).setHidden(true);

        addRolloutNameColumn().setMinimumWidth(50d).setExpandRatio(1).setHidable(true);

        addTypeColumn().setWidth(25d);
        addTimeforcedColumn().setWidth(25d);
        getDefaultHeaderRow().join(TYPE_ID, TIME_FORCED_ID).setText(i18n.getMessage("label.action.type"));

        addCancelColumn().setWidth(25d);
        addForceColumn().setWidth(25d);
        addForceQuitColumn().setWidth(25d);
        getDefaultHeaderRow().join(CANCEL_BUTTON_ID, FORCE_BUTTON_ID, FORCE_QUIT_BUTTON_ID)
                .setText(i18n.getMessage("header.action"));
    }

    private Column<ProxyAction, Long> addActionIdColumn() {
        return addColumn(ProxyAction::getId).setId(ACTION_ID).setCaption(i18n.getMessage("label.action.id"))
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);
    }

    private Column<ProxyAction, String> addRolloutNameColumn() {
        return addColumn(ProxyAction::getRolloutName).setId(ROLLOUT_NAME_ID)
                .setCaption(i18n.getMessage("caption.rollout.name"));
    }

    public MasterEntitySupport<ProxyTarget> getMasterEntitySupport() {
        return masterEntitySupport;
    }
}
