/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rollout;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.Rollout.RolloutStatus;
import org.eclipse.hawkbit.repository.model.TotalTargetCountStatus.Status;
import org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.ConfirmationDialog;
import org.eclipse.hawkbit.ui.common.data.mappers.RolloutToProxyRolloutMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.providers.RolloutDataProvider;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.push.RolloutChangeEventContainer;
import org.eclipse.hawkbit.ui.push.RolloutDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.event.RolloutChangedEvent;
import org.eclipse.hawkbit.ui.rollout.DistributionBarHelper;
import org.eclipse.hawkbit.ui.rollout.FontIcon;
import org.eclipse.hawkbit.ui.rollout.event.RolloutEvent;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowBuilder;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.cronutils.utils.StringUtils;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.HtmlRenderer;

/**
 * Rollout list grid component.
 */
public class RolloutGrid extends AbstractGrid<ProxyRollout, Void> {

    private static final long serialVersionUID = 1L;

    private static final String ROLLOUT_LINK_ID = "rollout";
    private static final String DIST_NAME_VERSION_ID = "distNameVersion";
    private static final String STATUS_ID = "status";
    private static final String TOTAL_TARGETS_COUNT_STATUS_ID = "totalTargetsCountStatus";
    private static final String NUMBER_OF_GROUPS_ID = "numberOfGroups";
    private static final String TOTAL_TARGETS_ID = "totalTargets";
    private static final String CREATED_DATE_ID = "createdDate";
    private static final String CREATED_USER_ID = "createdUser";
    private static final String MODIFIED_DATE_ID = "modifiedDate";
    private static final String MODIFIED_BY_ID = "modifiedBy";
    private static final String APPROVAL_DECIDED_BY_ID = "approvalDecidedBy";
    private static final String APPROVAL_REMARK_ID = "approvalRemark";
    private static final String DESC_ID = "description";
    private static final String ACTION_TYPE_ID = "actionType";

    private static final String APPROVE_BUTTON_ID = "approve";
    private static final String RUN_BUTTON_ID = "run";
    private static final String PAUSE_BUTTON_ID = "pause";
    private static final String UPDATE_BUTTON_ID = "update";
    private static final String COPY_BUTTON_ID = "copy";
    private static final String DELETE_BUTTON_ID = "delete";

    private final transient RolloutManagement rolloutManagement;

    private final transient RolloutGroupManagement rolloutGroupManagement;

    private final transient TenantConfigurationManagement tenantConfigManagement;

    private final transient RolloutWindowBuilder rolloutWindowBuilder;

    private final UINotification uiNotification;

    private final RolloutUIState rolloutUIState;

    private static final List<RolloutStatus> DELETE_COPY_BUTTON_ENABLED = Arrays.asList(RolloutStatus.CREATING,
            RolloutStatus.PAUSED, RolloutStatus.READY, RolloutStatus.RUNNING, RolloutStatus.STARTING,
            RolloutStatus.STOPPED, RolloutStatus.FINISHED, RolloutStatus.WAITING_FOR_APPROVAL,
            RolloutStatus.APPROVAL_DENIED);

    private static final List<RolloutStatus> UPDATE_BUTTON_ENABLED = Arrays.asList(RolloutStatus.CREATING,
            RolloutStatus.PAUSED, RolloutStatus.READY, RolloutStatus.RUNNING, RolloutStatus.STARTING,
            RolloutStatus.STOPPED);

    private static final List<RolloutStatus> PAUSE_BUTTON_ENABLED = Collections.singletonList(RolloutStatus.RUNNING);

    private static final List<RolloutStatus> RUN_BUTTON_ENABLED = Arrays.asList(RolloutStatus.READY,
            RolloutStatus.PAUSED);

    private static final List<RolloutStatus> APPROVE_BUTTON_ENABLED = Collections
            .singletonList(RolloutStatus.WAITING_FOR_APPROVAL);

    private final Map<RolloutStatus, FontIcon> statusIconMap = new EnumMap<>(RolloutStatus.class);
    private final Map<ActionType, FontIcon> actionTypeIconMap = new EnumMap<>(ActionType.class);

    private final ConfigurableFilterDataProvider<ProxyRollout, Void, Void> rolloutDataProvider;

    RolloutGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final RolloutManagement rolloutManagement, final UINotification uiNotification,
            final RolloutUIState rolloutUIState, final SpPermissionChecker permissionChecker,
            final TargetManagement targetManagement, final EntityFactory entityFactory, final UiProperties uiProperties,
            final TargetFilterQueryManagement targetFilterQueryManagement,
            final RolloutGroupManagement rolloutGroupManagement, final QuotaManagement quotaManagement,
            final TenantConfigurationManagement tenantConfigManagement, final RolloutDataProvider rolloutDataProvider,
            final DistributionSetStatelessDataProvider distributionSetDataProvider,
            final TargetFilterQueryDataProvider targetFilterQueryDataProvider) {
        super(i18n, eventBus, permissionChecker);
        this.rolloutManagement = rolloutManagement;
        this.rolloutGroupManagement = rolloutGroupManagement;
        this.tenantConfigManagement = tenantConfigManagement;
        this.uiNotification = uiNotification;
        this.rolloutUIState = rolloutUIState;
        this.rolloutDataProvider = rolloutDataProvider.withConfigurableFilter();

        final RolloutWindowDependencies rolloutWindowDependecies = new RolloutWindowDependencies(rolloutManagement,
                targetManagement, uiNotification, entityFactory, i18n, uiProperties, eventBus,
                targetFilterQueryManagement, rolloutGroupManagement, quotaManagement, distributionSetDataProvider,
                targetFilterQueryDataProvider);
        this.rolloutWindowBuilder = new RolloutWindowBuilder(rolloutWindowDependecies);

        initStatusIconMap();
        initActionTypeIconMap();

        init();
        hideColumnsDueToInsufficientPermissions();
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyRollout, Void, Void> getFilterDataProvider() {
        return rolloutDataProvider;
    }

    private void initStatusIconMap() {
        statusIconMap.put(RolloutStatus.FINISHED, new FontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getStatusDescription(RolloutStatus.FINISHED)));
        statusIconMap.put(RolloutStatus.PAUSED, new FontIcon(VaadinIcons.PAUSE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                getStatusDescription(RolloutStatus.PAUSED)));
        statusIconMap.put(RolloutStatus.RUNNING, new FontIcon(null, SPUIStyleDefinitions.STATUS_SPINNER_YELLOW,
                getStatusDescription(RolloutStatus.RUNNING)));
        statusIconMap.put(RolloutStatus.WAITING_FOR_APPROVAL, new FontIcon(VaadinIcons.HOURGLASS,
                SPUIStyleDefinitions.STATUS_ICON_ORANGE, getStatusDescription(RolloutStatus.WAITING_FOR_APPROVAL)));
        statusIconMap.put(RolloutStatus.APPROVAL_DENIED, new FontIcon(VaadinIcons.CLOSE_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_RED, getStatusDescription(RolloutStatus.APPROVAL_DENIED)));
        statusIconMap.put(RolloutStatus.READY, new FontIcon(VaadinIcons.BULLSEYE,
                SPUIStyleDefinitions.STATUS_ICON_LIGHT_BLUE, getStatusDescription(RolloutStatus.READY)));
        statusIconMap.put(RolloutStatus.STOPPED, new FontIcon(VaadinIcons.STOP, SPUIStyleDefinitions.STATUS_ICON_RED,
                getStatusDescription(RolloutStatus.STOPPED)));
        statusIconMap.put(RolloutStatus.CREATING, new FontIcon(null, SPUIStyleDefinitions.STATUS_SPINNER_GREY,
                getStatusDescription(RolloutStatus.CREATING)));
        statusIconMap.put(RolloutStatus.STARTING, new FontIcon(null, SPUIStyleDefinitions.STATUS_SPINNER_BLUE,
                getStatusDescription(RolloutStatus.STARTING)));
        statusIconMap.put(RolloutStatus.DELETING, new FontIcon(null, SPUIStyleDefinitions.STATUS_SPINNER_RED,
                getStatusDescription(RolloutStatus.DELETING)));
    }

    private String getStatusDescription(final RolloutStatus status) {
        return i18n.getMessage(UIMessageIdProvider.TOOLTIP_ROLLOUT_STATUS_PREFIX + status.toString().toLowerCase());
    }

    private void initActionTypeIconMap() {
        actionTypeIconMap.put(ActionType.FORCED, new FontIcon(VaadinIcons.BOLT, SPUIStyleDefinitions.STATUS_ICON_FORCED,
                i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_FORCED)));
        actionTypeIconMap.put(ActionType.TIMEFORCED,
                new FontIcon(VaadinIcons.TIMER, SPUIStyleDefinitions.STATUS_ICON_TIME_FORCED,
                        i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_TIME_FORCED)));
        actionTypeIconMap.put(ActionType.SOFT, new FontIcon(VaadinIcons.STEP_FORWARD,
                SPUIStyleDefinitions.STATUS_ICON_SOFT, i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_SOFT)));
        actionTypeIconMap.put(ActionType.DOWNLOAD_ONLY,
                new FontIcon(VaadinIcons.DOWNLOAD, SPUIStyleDefinitions.STATUS_ICON_DOWNLOAD_ONLY,
                        i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_DOWNLOAD_ONLY)));
    }

    private final void hideColumnsDueToInsufficientPermissions() {

        if (!permissionChecker.hasRolloutUpdatePermission()) {
            removeColumn(UPDATE_BUTTON_ID);
        }
        if (!permissionChecker.hasRolloutCreatePermission()) {
            removeColumn(COPY_BUTTON_ID);
        }
        if (!permissionChecker.hasRolloutApprovalPermission() || !tenantConfigManagement
                .getConfigurationValue(TenantConfigurationKey.ROLLOUT_APPROVAL_ENABLED, Boolean.class).getValue()) {
            removeColumn(APPROVE_BUTTON_ID);
        }
        if (!permissionChecker.hasRolloutDeletePermission()) {
            removeColumn(DELETE_BUTTON_ID);
        }
        if (!permissionChecker.hasRolloutHandlePermission()) {
            removeColumn(PAUSE_BUTTON_ID);
            removeColumn(RUN_BUTTON_ID);
        }
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.ROLLOUT_LIST_GRID_ID;
    }

    /**
     * Handles the RolloutEvent to refresh Grid.
     *
     */
    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final RolloutEvent event) {
        switch (event) {
        case FILTER_BY_TEXT:
        case CREATE_ROLLOUT:
        case UPDATE_ROLLOUT:
        case SHOW_ROLLOUTS:
            refreshContainer();
            break;
        default:
            return;
        }
    }

    /**
     * Handles the RolloutDeletedEvent to refresh the grid.
     *
     * @param eventContainer
     *            container which holds the rollout delete event
     */
    @EventBusListenerMethod(scope = EventScope.UI)
    public void onRolloutDeletedEvent(final RolloutDeletedEventContainer eventContainer) {
        refreshContainer();
    }

    /**
     * Handles the RolloutChangeEvent to refresh the item in the grid.
     *
     * @param eventContainer
     *            container which holds the rollout change event
     */
    @EventBusListenerMethod(scope = EventScope.UI)
    public void onRolloutChangeEvent(final RolloutChangeEventContainer eventContainer) {
        eventContainer.getEvents().forEach(this::handleEvent);
    }

    private void handleEvent(final RolloutChangedEvent rolloutChangeEvent) {
        if (!rolloutUIState.isShowRollOuts() || rolloutChangeEvent.getRolloutId() == null) {
            return;
        }
        final Optional<Rollout> rollout = rolloutManagement.getWithDetailedStatus(rolloutChangeEvent.getRolloutId());

        rollout.ifPresent(this::updateItem);
    }

    private void updateItem(final Rollout rollout) {
        final ProxyRollout proxyRollout = RolloutToProxyRolloutMapper.mapRollout(rollout);

        if (rollout.getRolloutGroupsCreated() == 0) {
            final Long groupsCount = rolloutGroupManagement.countByRollout(rollout.getId());
            proxyRollout.setNumberOfGroups(groupsCount.intValue());
        }

        getDataProvider().refreshItem(proxyRollout);
    }

    @Override
    public void addColumns() {
        addComponentColumn(this::buildRolloutLink).setId(ROLLOUT_LINK_ID).setCaption(i18n.getMessage("header.name"))
                .setMinimumWidth(40d).setMaximumWidth(300d).setHidable(true).setHidden(false);

        addColumn(ProxyRollout::getDistributionSetNameVersion).setId(DIST_NAME_VERSION_ID)
                .setCaption(i18n.getMessage("header.distributionset")).setMinimumWidth(40d).setMaximumWidth(300d)
                .setHidable(true).setHidden(false);

        addComponentColumn(this::buildStatusIcon).setId(STATUS_ID).setCaption(i18n.getMessage("header.status"))
                .setMinimumWidth(40d).setMaximumWidth(60d).setHidable(true).setHidden(false)
                .setStyleGenerator(item -> "v-align-center");

        addComponentColumn(this::buildTypeIcon).setId(ACTION_TYPE_ID).setCaption(i18n.getMessage("header.type"))
                .setMinimumWidth(45d).setMaximumWidth(45d).setHidable(true).setHidden(true)
                .setStyleGenerator(item -> "v-align-center");

        addColumn(rollout -> DistributionBarHelper.getDistributionBarAsHTMLString(rollout.getStatusTotalCountMap()),
                new HtmlRenderer()).setId(TOTAL_TARGETS_COUNT_STATUS_ID)
                        .setCaption(i18n.getMessage("header.detail.status")).setMinimumWidth(280d).setHidable(true)
                        .setHidden(false);

        addColumn(ProxyRollout::getNumberOfGroups).setId(NUMBER_OF_GROUPS_ID)
                .setCaption(i18n.getMessage("header.numberofgroups")).setMinimumWidth(40d).setMaximumWidth(60d)
                .setHidable(true).setHidden(false);

        addColumn(ProxyRollout::getTotalTargets).setId(TOTAL_TARGETS_ID)
                .setCaption(i18n.getMessage("header.total.targets")).setMinimumWidth(40d).setMaximumWidth(60d)
                .setHidable(true).setHidden(false);

        addActionColumns();

        addColumn(ProxyRollout::getCreatedDate).setId(CREATED_DATE_ID).setCaption(i18n.getMessage("header.createdDate"))
                .setHidable(true).setHidden(true);

        addColumn(ProxyRollout::getCreatedBy).setId(CREATED_USER_ID).setCaption(i18n.getMessage("header.createdBy"))
                .setHidable(true).setHidden(true);

        addColumn(ProxyRollout::getModifiedDate).setId(MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setHidable(true).setHidden(true);

        addColumn(ProxyRollout::getLastModifiedBy).setId(MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setHidable(true).setHidden(true);

        addColumn(ProxyRollout::getApprovalDecidedBy).setId(APPROVAL_DECIDED_BY_ID)
                .setCaption(i18n.getMessage("header.approvalDecidedBy")).setHidable(true).setHidden(true);

        addColumn(ProxyRollout::getApprovalDecidedBy).setId(APPROVAL_REMARK_ID)
                .setCaption(i18n.getMessage("header.approvalRemark")).setHidable(true).setHidden(true);

        addColumn(ProxyRollout::getDescription).setId(DESC_ID).setCaption(i18n.getMessage("header.description"))
                .setHidable(true).setHidden(true);
    }

    private Label buildStatusIcon(final ProxyRollout rollout) {
        final FontIcon statusFontIcon = Optional.ofNullable(statusIconMap.get(rollout.getStatus()))
                .orElse(new FontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String statusId = new StringBuilder(UIComponentIdProvider.ROLLOUT_STATUS_LABEL_ID).append(".")
                .append(rollout.getId()).toString();

        return buildLabelIcon(statusFontIcon, statusId);
    }

    private Label buildTypeIcon(final ProxyRollout rollout) {
        final FontIcon actionTypeFontIcon = Optional.ofNullable(actionTypeIconMap.get(rollout.getActionType()))
                .orElse(new FontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String actionTypeId = new StringBuilder(UIComponentIdProvider.ROLLOUT_ACTION_TYPE_LABEL_ID).append(".")
                .append(rollout.getId()).toString();

        return buildLabelIcon(actionTypeFontIcon, actionTypeId);
    }

    private void addActionColumns() {
        addComponentColumn(rollout -> buildActionButton(
                clickEvent -> startOrResumeRollout(rollout.getId(), rollout.getName(), rollout.getStatus()),
                VaadinIcons.PLAY, UIMessageIdProvider.TOOLTIP_ROLLOUT_RUN, rollout.getStatus(), RUN_BUTTON_ENABLED,
                UIComponentIdProvider.ROLLOUT_RUN_BUTTON_ID)).setId(RUN_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.run")).setHidable(false).setMinimumWidth(25d)
                        .setMaximumWidth(25d);

        addComponentColumn(rollout -> buildActionButton(clickEvent -> approveRollout(rollout), VaadinIcons.HANDSHAKE,
                UIMessageIdProvider.TOOLTIP_ROLLOUT_APPROVE, rollout.getStatus(), APPROVE_BUTTON_ENABLED,
                UIComponentIdProvider.ROLLOUT_APPROVAL_BUTTON_ID)).setId(APPROVE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.approve")).setHidable(false).setMinimumWidth(25d)
                        .setMaximumWidth(25d);

        addComponentColumn(rollout -> buildActionButton(
                clickEvent -> pauseRollout(rollout.getId(), rollout.getName(), rollout.getStatus()), VaadinIcons.PAUSE,
                UIMessageIdProvider.TOOLTIP_ROLLOUT_PAUSE, rollout.getStatus(), PAUSE_BUTTON_ENABLED,
                UIComponentIdProvider.ROLLOUT_PAUSE_BUTTON_ID)).setId(PAUSE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.pause")).setHidable(false).setMinimumWidth(25d)
                        .setMaximumWidth(25d);

        addComponentColumn(rollout -> buildActionButton(clickEvent -> updateRollout(rollout), VaadinIcons.EDIT,
                UIMessageIdProvider.TOOLTIP_ROLLOUT_UPDATE, rollout.getStatus(), UPDATE_BUTTON_ENABLED,
                UIComponentIdProvider.ROLLOUT_UPDATE_BUTTON_ID)).setId(UPDATE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.update")).setHidable(false).setMinimumWidth(25d)
                        .setMaximumWidth(25d);

        addComponentColumn(rollout -> buildActionButton(clickEvent -> copyRollout(rollout), VaadinIcons.COPY,
                UIMessageIdProvider.TOOLTIP_ROLLOUT_COPY, rollout.getStatus(), DELETE_COPY_BUTTON_ENABLED,
                UIComponentIdProvider.ROLLOUT_COPY_BUTTON_ID)).setId(COPY_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.copy")).setHidable(false).setMinimumWidth(25d)
                        .setMaximumWidth(25d);

        addComponentColumn(rollout -> buildActionButton(clickEvent -> deleteRollout(rollout.getId(), rollout.getName()),
                VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, rollout.getStatus(), DELETE_COPY_BUTTON_ENABLED,
                UIComponentIdProvider.ROLLOUT_DELETE_BUTTON_ID)).setId(DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.delete")).setHidable(false).setMinimumWidth(25d)
                        .setMaximumWidth(25d);

        getDefaultHeaderRow().join(RUN_BUTTON_ID, APPROVE_BUTTON_ID, PAUSE_BUTTON_ID, UPDATE_BUTTON_ID, COPY_BUTTON_ID,
                DELETE_BUTTON_ID).setText(i18n.getMessage("header.action"));
    }

    private Button buildActionButton(final ClickListener clickListener, final VaadinIcons icon,
            final String descriptionProperty, final RolloutStatus rolloutStatus,
            final List<RolloutStatus> rolloutEnabledStatuses, final String buttonId) {
        final Button actionButton = new Button();

        actionButton.addClickListener(clickListener);
        actionButton.setIcon(icon);
        actionButton.setDescription(i18n.getMessage(descriptionProperty));
        actionButton.setEnabled(hasToBeEnabled(rolloutStatus, rolloutEnabledStatuses));
        actionButton.setId(buttonId);
        actionButton.addStyleName("tiny");
        actionButton.addStyleName("borderless");
        actionButton.addStyleName("button-no-border");
        actionButton.addStyleName("action-type-padding");

        return actionButton;
    }

    private Button buildRolloutLink(final ProxyRollout rollout) {
        final Button rolloutLink = new Button();

        rolloutLink.addClickListener(clickEvent -> onClickOfRolloutName(rollout.getId(), rollout.getName(),
                rollout.getDistributionSetNameVersion()));
        rolloutLink.setId(new StringBuilder("rollout.link.").append(rollout.getId()).toString());
        rolloutLink.addStyleName("borderless");
        rolloutLink.addStyleName("small");
        rolloutLink.addStyleName("on-focus-no-border");
        rolloutLink.addStyleName("link");
        rolloutLink.setCaption(rollout.getName());
        // this is to allow the button to disappear, if the text is null
        rolloutLink.setVisible(!StringUtils.isEmpty(rollout.getName()));

        /*
         * checking Rollout Status for applying button style. If Rollout status
         * is not "CREATING", then the Rollout button is applying hyperlink
         * style
         */
        final boolean isStatusCreating = rollout.getStatus() != null
                && RolloutStatus.CREATING.equals(rollout.getStatus());
        if (isStatusCreating) {
            rolloutLink.addStyleName("boldhide");
            rolloutLink.setEnabled(false);
        } else {
            rolloutLink.setEnabled(true);
        }

        return rolloutLink;
    }

    private void onClickOfRolloutName(final Long rolloutId, final String rolloutName,
            final String distributionSetNameVersion) {
        rolloutUIState.setRolloutId(rolloutId);
        rolloutUIState.setRolloutName(rolloutName);
        rolloutUIState.setRolloutDistributionSet(distributionSetNameVersion);
        eventBus.publish(this, RolloutEvent.SHOW_ROLLOUT_GROUPS);
    }

    private void pauseRollout(final Long rolloutId, final String rolloutName, final RolloutStatus rolloutStatus) {
        if (!RolloutStatus.RUNNING.equals(rolloutStatus)) {
            return;
        }

        rolloutManagement.pauseRollout(rolloutId);
        uiNotification.displaySuccess(i18n.getMessage("message.rollout.paused", rolloutName));
    }

    private void startOrResumeRollout(final Long rolloutId, final String rolloutName,
            final RolloutStatus rolloutStatus) {
        switch (rolloutStatus) {
        case READY:
            rolloutManagement.start(rolloutId);
            uiNotification.displaySuccess(i18n.getMessage("message.rollout.started", rolloutName));
            break;
        case PAUSED:
            rolloutManagement.resumeRollout(rolloutId);
            uiNotification.displaySuccess(i18n.getMessage("message.rollout.resumed", rolloutName));
            break;
        default:
            break;
        }
    }

    private void approveRollout(final ProxyRollout rollout) {
        final Window approveWindow = rolloutWindowBuilder.getWindowForApproveRollout(rollout);

        approveWindow.setCaption(i18n.getMessage("caption.approve.rollout"));
        UI.getCurrent().addWindow(approveWindow);
        approveWindow.setVisible(Boolean.TRUE);
    }

    private void updateRollout(final ProxyRollout rollout) {
        final Window updateWindow = rolloutWindowBuilder.getWindowForUpdateRollout(rollout);

        updateWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.rollout")));
        UI.getCurrent().addWindow(updateWindow);
        updateWindow.setVisible(Boolean.TRUE);
    }

    private void copyRollout(final ProxyRollout rollout) {
        final Window copyWindow = rolloutWindowBuilder.getWindowForCopyRollout(rollout);

        copyWindow.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.rollout")));
        UI.getCurrent().addWindow(copyWindow);
        copyWindow.setVisible(Boolean.TRUE);
    }

    private void deleteRollout(final Long rolloutId, final String rolloutName) {
        final Optional<Rollout> rollout = rolloutManagement.getWithDetailedStatus(rolloutId);

        if (!rollout.isPresent()) {
            return;
        }

        final String formattedConfirmationQuestion = getConfirmationQuestion(rollout.get());
        final ConfirmationDialog confirmationDialog = new ConfirmationDialog(
                i18n.getMessage("caption.entity.delete.action.confirmbox"), formattedConfirmationQuestion,
                i18n.getMessage(UIMessageIdProvider.BUTTON_OK), i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL),
                ok -> {
                    if (!ok) {
                        return;
                    }
                    rolloutManagement.delete(rolloutId);
                    uiNotification.displaySuccess(i18n.getMessage("message.rollout.deleted", rolloutName));
                }, UIComponentIdProvider.ROLLOUT_DELETE_CONFIRMATION_DIALOG);
        UI.getCurrent().addWindow(confirmationDialog.getWindow());
        confirmationDialog.getWindow().bringToFront();
    }

    private String getConfirmationQuestion(final Rollout rollout) {

        final Map<Status, Long> statusTotalCount = rollout.getTotalTargetCountStatus().getStatusTotalCountMap();
        Long scheduledActions = statusTotalCount.get(Status.SCHEDULED);
        if (scheduledActions == null) {
            scheduledActions = 0L;
        }
        final Long runningActions = statusTotalCount.get(Status.RUNNING);
        String rolloutDetailsMessage = "";
        if ((scheduledActions > 0) || (runningActions > 0)) {
            rolloutDetailsMessage = i18n.getMessage("message.delete.rollout.details", runningActions, scheduledActions);
        }

        return i18n.getMessage("message.delete.rollout", rollout.getName(), rolloutDetailsMessage);
    }

    private static boolean hasToBeEnabled(final RolloutStatus currentRolloutStatus,
            final List<RolloutStatus> expectedRolloutStatus) {
        return expectedRolloutStatus.contains(currentRolloutStatus);
    }
}
