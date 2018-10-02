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
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.Rollout.RolloutStatus;
import org.eclipse.hawkbit.repository.model.TotalTargetCountStatus;
import org.eclipse.hawkbit.repository.model.TotalTargetCountStatus.Status;
import org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow;
import org.eclipse.hawkbit.ui.common.ConfirmationDialog;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.customrenderers.client.renderers.RolloutRendererData;
import org.eclipse.hawkbit.ui.customrenderers.renderers.RolloutRenderer;
import org.eclipse.hawkbit.ui.push.RolloutChangeEventContainer;
import org.eclipse.hawkbit.ui.push.RolloutDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.event.RolloutChangedEvent;
import org.eclipse.hawkbit.ui.rollout.DistributionBarHelper;
import org.eclipse.hawkbit.ui.rollout.StatusFontIcon;
import org.eclipse.hawkbit.ui.rollout.event.RolloutEvent;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUILabelDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.components.grid.HeaderCell;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;

/**
 * Rollout list grid component.
 */
public class RolloutListGrid extends AbstractGrid<ProxyRollout> {

    private static final long serialVersionUID = 1L;

    private static final String ROLLOUT_RENDERER_DATA = "rolloutRendererData";

    private static final String VIRT_PROP_APPROVE = "approve";
    private static final String VIRT_PROP_RUN = "run";
    private static final String VIRT_PROP_PAUSE = "pause";
    private static final String VIRT_PROP_UPDATE = "update";
    private static final String VIRT_PROP_COPY = "copy";
    private static final String VIRT_PROP_DELETE = "delete";

    private final transient RolloutManagement rolloutManagement;

    private final transient RolloutGroupManagement rolloutGroupManagement;

    private final transient TenantConfigurationManagement tenantConfigManagement;

    private final AddUpdateRolloutWindowLayout addUpdateRolloutWindow;

    private final UINotification uiNotification;

    private final RolloutUIState rolloutUIState;

    private final RolloutDataProvider rolloutDataProvider;

    private static final List<RolloutStatus> DELETE_COPY_BUTTON_ENABLED = Arrays.asList(RolloutStatus.CREATING,
            RolloutStatus.ERROR_CREATING, RolloutStatus.ERROR_STARTING, RolloutStatus.PAUSED, RolloutStatus.READY,
            RolloutStatus.RUNNING, RolloutStatus.STARTING, RolloutStatus.STOPPED, RolloutStatus.FINISHED,
            RolloutStatus.WAITING_FOR_APPROVAL, RolloutStatus.APPROVAL_DENIED);

    private static final List<RolloutStatus> UPDATE_BUTTON_ENABLED = Arrays.asList(RolloutStatus.CREATING,
            RolloutStatus.ERROR_CREATING, RolloutStatus.ERROR_STARTING, RolloutStatus.PAUSED, RolloutStatus.READY,
            RolloutStatus.RUNNING, RolloutStatus.STARTING, RolloutStatus.STOPPED);

    private static final List<RolloutStatus> PAUSE_BUTTON_ENABLED = Arrays.asList(RolloutStatus.RUNNING);

    private static final List<RolloutStatus> RUN_BUTTON_ENABLED = Arrays.asList(RolloutStatus.READY,
            RolloutStatus.PAUSED);

    private static final List<RolloutStatus> APPROVE_BUTTON_ENABLED = Collections
            .singletonList(RolloutStatus.WAITING_FOR_APPROVAL);

    private static final Map<RolloutStatus, StatusFontIcon> statusIconMap = new EnumMap<>(RolloutStatus.class);

    private static final List<String> HIDDEN_COLUMNS = Arrays.asList(SPUILabelDefinitions.VAR_CREATED_DATE,
            SPUILabelDefinitions.VAR_CREATED_USER, SPUILabelDefinitions.VAR_MODIFIED_DATE,
            SPUILabelDefinitions.VAR_MODIFIED_BY, SPUILabelDefinitions.VAR_APPROVAL_DECIDED_BY,
            SPUILabelDefinitions.VAR_APPROVAL_REMARK, SPUILabelDefinitions.VAR_DESC);

    static {
        statusIconMap.put(RolloutStatus.FINISHED,
                new StatusFontIcon(VaadinIcons.CHECK_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_GREEN));
        statusIconMap.put(RolloutStatus.PAUSED,
                new StatusFontIcon(VaadinIcons.PAUSE, SPUIStyleDefinitions.STATUS_ICON_BLUE));
        statusIconMap.put(RolloutStatus.RUNNING, new StatusFontIcon(null, SPUIStyleDefinitions.STATUS_SPINNER_YELLOW));
        statusIconMap.put(RolloutStatus.WAITING_FOR_APPROVAL,
                new StatusFontIcon(VaadinIcons.HOURGLASS, SPUIStyleDefinitions.STATUS_ICON_ORANGE));
        statusIconMap.put(RolloutStatus.APPROVAL_DENIED,
                new StatusFontIcon(VaadinIcons.CLOSE_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_RED));
        statusIconMap.put(RolloutStatus.READY,
                new StatusFontIcon(VaadinIcons.BULLSEYE, SPUIStyleDefinitions.STATUS_ICON_LIGHT_BLUE));
        statusIconMap.put(RolloutStatus.STOPPED,
                new StatusFontIcon(VaadinIcons.STOP, SPUIStyleDefinitions.STATUS_ICON_RED));
        statusIconMap.put(RolloutStatus.CREATING, new StatusFontIcon(null, SPUIStyleDefinitions.STATUS_SPINNER_GREY));
        statusIconMap.put(RolloutStatus.STARTING, new StatusFontIcon(null, SPUIStyleDefinitions.STATUS_SPINNER_BLUE));
        statusIconMap.put(RolloutStatus.ERROR_CREATING,
                new StatusFontIcon(VaadinIcons.EXCLAMATION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_RED));
        statusIconMap.put(RolloutStatus.ERROR_STARTING,
                new StatusFontIcon(VaadinIcons.EXCLAMATION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_RED));
        statusIconMap.put(RolloutStatus.DELETING, new StatusFontIcon(null, SPUIStyleDefinitions.STATUS_SPINNER_RED));
    }

    RolloutListGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final RolloutManagement rolloutManagement, final UINotification uiNotification,
            final RolloutUIState rolloutUIState, final SpPermissionChecker permissionChecker,
            final TargetManagement targetManagement, final EntityFactory entityFactory, final UiProperties uiProperties,
            final TargetFilterQueryManagement targetFilterQueryManagement,
            final RolloutGroupManagement rolloutGroupManagement, final QuotaManagement quotaManagement,
            final TenantConfigurationManagement tenantConfigManagement, final RolloutDataProvider rolloutDataProvider) {
        super(i18n, eventBus, permissionChecker);
        this.rolloutManagement = rolloutManagement;
        this.rolloutGroupManagement = rolloutGroupManagement;
        this.tenantConfigManagement = tenantConfigManagement;
        this.addUpdateRolloutWindow = new AddUpdateRolloutWindowLayout(rolloutManagement, targetManagement,
                uiNotification, uiProperties, entityFactory, i18n, eventBus, targetFilterQueryManagement,
                rolloutGroupManagement, quotaManagement);
        this.uiNotification = uiNotification;
        this.rolloutUIState = rolloutUIState;
        this.rolloutDataProvider = rolloutDataProvider;

        setBeanType(ProxyRollout.class);

        init();
        hideColumnsDueToInsufficientPermissions();
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

        if (!rollout.isPresent()) {
            return;
        }

        updateItem(rollout.get());
    }

    private void updateItem(final Rollout rollout) {

        final ProxyRollout proxyRollout = (ProxyRollout) getDataProvider()
                .getId(ProxyRolloutService.createProxy(rollout));
        proxyRollout.setStatus(rollout.getStatus());
        proxyRollout.setTotalTargetCountStatus(rollout.getTotalTargetCountStatus());
        final Long groupCount = Long.valueOf(proxyRollout.getNumberOfGroups());
        final int groupsCreated = rollout.getRolloutGroupsCreated();
        proxyRollout.setRolloutRendererData(new RolloutRendererData(rollout.getName(), rollout.getStatus().toString()));

        if (groupsCreated != 0) {
            proxyRollout.setNumberOfGroups(groupsCreated);
            return;
        }

        final Long size = rolloutGroupManagement.countTargetsOfRolloutsGroup(rollout.getId());
        if (!size.equals(groupCount)) {
            proxyRollout.setNumberOfGroups(size.intValue());
        }
    }

    @Override
    protected void setDataProvider() {
        // use a Data Provider to fill the Grid with Data. You can also use
        // grid.setItems(list), but this is only for a small amount of data. If
        // the data amount is too large this is not recommended.
//        setDataProvider((sortOrders, offset, limit) -> {
//            final Map<String, Boolean> sortOrder = sortOrders.stream().collect(Collectors
//                    .toMap(sort -> sort.getSorted(), sort -> SortDirection.ASCENDING.equals(sort.getDirection())));
//            return proxyRolloutService.findAll(offset, limit, sortOrder).stream();
//        }, proxyRolloutService::size);
        setDataProvider(rolloutDataProvider);
    }

    @Override
    protected void addColumnRenderes() {
        // getColumn(SPUILabelDefinitions.VAR_NUMBER_OF_GROUPS).setRenderer(new
        // TotalTargetGroupsConverter());
        //
        // getColumn(SPUILabelDefinitions.VAR_TOTAL_TARGETS_COUNT_STATUS)
        // .setRenderer(new TotalTargetCountStatusConverter());
        //
        // getColumn(SPUILabelDefinitions.VAR_STATUS).setRenderer(new
        // RolloutStatusConverter());
        //
        // final RolloutRenderer customObjectRenderer = new
        // RolloutRenderer(RolloutRendererData.class);
        // customObjectRenderer.addClickListener(this::onClickOfRolloutName);
        // getColumn(ROLLOUT_RENDERER_DATA).setRenderer(customObjectRenderer);
    }

    @Override
    protected void addColumns() {
        final RolloutRenderer customObjectRenderer = new RolloutRenderer(RolloutRendererData.class);
        customObjectRenderer.addClickListener(this::onClickOfRolloutName);

        addColumn(ProxyRollout::getRolloutRendererData, customObjectRenderer).setId(ROLLOUT_RENDERER_DATA);
        addColumn(ProxyRollout::getDistributionSetNameVersion).setId(SPUILabelDefinitions.VAR_DIST_NAME_VERSION);
        addColumn(ProxyRollout::getStatus)/*
                                           * .setRenderer(new
                                           * HtmlLabelRenderer<RolloutStatus>())
                                           */
                .setId(SPUILabelDefinitions.VAR_STATUS);
        addColumn(ProxyRollout::getTotalTargetCountStatus).setId(SPUILabelDefinitions.VAR_TOTAL_TARGETS_COUNT_STATUS);
        addColumn(ProxyRollout::getNumberOfGroups).setId(SPUILabelDefinitions.VAR_NUMBER_OF_GROUPS);
        addColumn(ProxyRollout::getTotalTargetsCount).setId(SPUILabelDefinitions.VAR_TOTAL_TARGETS);
        addGeneratedColumns();
        addColumn(ProxyRollout::getCreatedDate).setId(SPUILabelDefinitions.VAR_CREATED_DATE);
        addColumn(ProxyRollout::getCreatedDate).setId(SPUILabelDefinitions.VAR_CREATED_USER);
        addColumn(ProxyRollout::getModifiedDate).setId(SPUILabelDefinitions.VAR_MODIFIED_DATE);
        addColumn(ProxyRollout::getLastModifiedBy).setId(SPUILabelDefinitions.VAR_MODIFIED_BY);
        addColumn(ProxyRollout::getApprovalDecidedBy).setId(SPUILabelDefinitions.VAR_APPROVAL_DECIDED_BY);
        addColumn(ProxyRollout::getApprovalRemark).setId(SPUILabelDefinitions.VAR_APPROVAL_REMARK);
        addColumn(ProxyRollout::getDescription).setId(SPUILabelDefinitions.VAR_DESC);
    }

    @Override
    protected void addGeneratedColumns() {
        // you can use components in Grid cells. You don't need renderers.
        addComponentColumn(rollout -> {
            final Button run = new Button();
            run.addClickListener(
                    clickEvent -> startOrResumeRollout(rollout.getId(), rollout.getName(), rollout.getStatus()));
            run.setIcon(VaadinIcons.PLAY);
            run.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_ROLLOUT_RUN));
            run.setEnabled(hasToBeEnabled(rollout.getStatus(), RUN_BUTTON_ENABLED));
            run.setId(UIComponentIdProvider.ROLLOUT_RUN_BUTTON_ID);
            return run;
        }).setId(VIRT_PROP_RUN);
        addComponentColumn(rollout -> {
            final Button approve = new Button();
            approve.addClickListener(clickEvent -> approveRollout(rollout.getId()));
            // in VaadinIcons there is a handshake icon. I would use that for
            // approving a rollout instead of the hammer
            approve.setIcon(VaadinIcons.HANDSHAKE);
            approve.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_ROLLOUT_APPROVE));
            approve.setEnabled(hasToBeEnabled(rollout.getStatus(), APPROVE_BUTTON_ENABLED));
            approve.setId(UIComponentIdProvider.ROLLOUT_APPROVAL_BUTTON_ID);
            return approve;
        }).setId(VIRT_PROP_APPROVE);
        addComponentColumn(rollout -> {
            final Button pause = new Button();
            pause.addClickListener(clickEvent -> pauseRollout(rollout.getId(), rollout.getName(), rollout.getStatus()));
            pause.setIcon(VaadinIcons.PAUSE);
            pause.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_ROLLOUT_PAUSE));
            pause.setEnabled(hasToBeEnabled(rollout.getStatus(), PAUSE_BUTTON_ENABLED));
            pause.setId(UIComponentIdProvider.ROLLOUT_PAUSE_BUTTON_ID);
            return pause;
        }).setId(VIRT_PROP_PAUSE);
        addComponentColumn(rollout -> {
            final Button update = new Button();
            update.addClickListener(clickEvent -> updateRollout(rollout.getId()));
            update.setIcon(VaadinIcons.EDIT);
            update.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_ROLLOUT_UPDATE));
            update.setEnabled(hasToBeEnabled(rollout.getStatus(), UPDATE_BUTTON_ENABLED));
            update.setId(UIComponentIdProvider.ROLLOUT_UPDATE_BUTTON_ID);
            return update;
        }).setId(VIRT_PROP_UPDATE);
        addComponentColumn(rollout -> {
            final Button copy = new Button();
            copy.addClickListener(clickEvent -> copyRollout(rollout.getId()));
            copy.setIcon(VaadinIcons.COPY);
            copy.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_ROLLOUT_COPY));
            copy.setEnabled(hasToBeEnabled(rollout.getStatus(), DELETE_COPY_BUTTON_ENABLED));
            copy.setId(UIComponentIdProvider.ROLLOUT_COPY_BUTTON_ID);
            return copy;
        }).setId(VIRT_PROP_COPY);
        addComponentColumn(rollout -> {
            final Button delete = new Button();
            delete.addClickListener(clickEvent -> deleteRollout(rollout.getId(), rollout.getName()));
            delete.setIcon(VaadinIcons.TRASH);
            delete.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_DELETE));
            delete.setEnabled(hasToBeEnabled(rollout.getStatus(), DELETE_COPY_BUTTON_ENABLED));
            delete.setId(UIComponentIdProvider.ROLLOUT_DELETE_BUTTON_ID);
            return delete;
        }).setId(VIRT_PROP_DELETE);
        joinColumns().setText(i18n.getMessage("header.action"));
    }

    @Override
    protected void setColumnHeaderNames() {
        getColumn(ROLLOUT_RENDERER_DATA).setCaption(i18n.getMessage("header.name"));
        getColumn(SPUILabelDefinitions.VAR_DIST_NAME_VERSION).setCaption(i18n.getMessage("header.distributionset"));
        getColumn(SPUILabelDefinitions.VAR_STATUS).setCaption(i18n.getMessage("header.status"));
        getColumn(SPUILabelDefinitions.VAR_TOTAL_TARGETS_COUNT_STATUS)
                .setCaption(i18n.getMessage("header.detail.status"));
        getColumn(SPUILabelDefinitions.VAR_NUMBER_OF_GROUPS).setCaption(i18n.getMessage("header.numberofgroups"));
        getColumn(SPUILabelDefinitions.VAR_TOTAL_TARGETS).setCaption(i18n.getMessage("header.total.targets"));
        getColumn(VIRT_PROP_RUN).setCaption(i18n.getMessage("header.action.run"));
        getColumn(VIRT_PROP_APPROVE).setCaption(i18n.getMessage("header.action.approve"));
        getColumn(VIRT_PROP_PAUSE).setCaption(i18n.getMessage("header.action.pause"));
        getColumn(VIRT_PROP_UPDATE).setCaption(i18n.getMessage("header.action.update"));
        getColumn(VIRT_PROP_COPY).setCaption(i18n.getMessage("header.action.copy"));
        getColumn(VIRT_PROP_DELETE).setCaption(i18n.getMessage("header.action.delete"));
        getColumn(SPUILabelDefinitions.VAR_CREATED_DATE).setCaption(i18n.getMessage("header.createdDate"));
        getColumn(SPUILabelDefinitions.VAR_CREATED_USER).setCaption(i18n.getMessage("header.createdBy"));
        getColumn(SPUILabelDefinitions.VAR_MODIFIED_DATE).setCaption(i18n.getMessage("header.modifiedDate"));
        getColumn(SPUILabelDefinitions.VAR_MODIFIED_BY).setCaption(i18n.getMessage("header.modifiedBy"));
        getColumn(SPUILabelDefinitions.VAR_APPROVAL_DECIDED_BY).setCaption(i18n.getMessage("header.approvalDecidedBy"));
        getColumn(SPUILabelDefinitions.VAR_APPROVAL_REMARK).setCaption(i18n.getMessage("header.approvalRemark"));
        getColumn(SPUILabelDefinitions.VAR_DESC).setCaption(i18n.getMessage("header.description"));
    }

    @Override
    protected void setColumnExpandRatio() {

        getColumn(ROLLOUT_RENDERER_DATA).setMinimumWidth(40);
        getColumn(ROLLOUT_RENDERER_DATA).setMaximumWidth(300);

        getColumn(SPUILabelDefinitions.VAR_DIST_NAME_VERSION).setMinimumWidth(40);
        getColumn(SPUILabelDefinitions.VAR_DIST_NAME_VERSION).setMaximumWidth(300);

        getColumn(SPUILabelDefinitions.VAR_STATUS).setMinimumWidth(40);
        getColumn(SPUILabelDefinitions.VAR_STATUS).setMaximumWidth(60);

        getColumn(SPUILabelDefinitions.VAR_TOTAL_TARGETS).setMinimumWidth(40);
        getColumn(SPUILabelDefinitions.VAR_TOTAL_TARGETS).setMaximumWidth(60);

        getColumn(SPUILabelDefinitions.VAR_NUMBER_OF_GROUPS).setMinimumWidth(40);
        getColumn(SPUILabelDefinitions.VAR_NUMBER_OF_GROUPS).setMaximumWidth(60);

        getColumn(VIRT_PROP_RUN).setMinimumWidth(25);
        getColumn(VIRT_PROP_RUN).setMaximumWidth(25);

        getColumn(VIRT_PROP_APPROVE).setMinimumWidth(25);
        getColumn(VIRT_PROP_APPROVE).setMaximumWidth(25);

        getColumn(VIRT_PROP_PAUSE).setMinimumWidth(25);
        getColumn(VIRT_PROP_PAUSE).setMaximumWidth(25);

        getColumn(VIRT_PROP_UPDATE).setMinimumWidth(25);
        getColumn(VIRT_PROP_UPDATE).setMaximumWidth(25);

        getColumn(VIRT_PROP_COPY).setMinimumWidth(25);
        getColumn(VIRT_PROP_COPY).setMaximumWidth(25);

        getColumn(VIRT_PROP_DELETE).setMinimumWidth(25);
        getColumn(VIRT_PROP_DELETE).setMaximumWidth(40);

        getColumn(SPUILabelDefinitions.VAR_TOTAL_TARGETS_COUNT_STATUS).setMinimumWidth(280);
    }

    private HeaderCell joinColumns() {

        return getDefaultHeaderRow().join(VIRT_PROP_RUN, VIRT_PROP_APPROVE, VIRT_PROP_PAUSE, VIRT_PROP_UPDATE,
                VIRT_PROP_COPY, VIRT_PROP_DELETE);
    }

    @Override
    protected String getGridId() {
        return UIComponentIdProvider.ROLLOUT_LIST_GRID_ID;
    }
    //
    // @Override
    // protected void setColumns() {
    // final List<String> columnsToShowInOrder =
    // Arrays.asList(ROLLOUT_RENDERER_DATA,
    // SPUILabelDefinitions.VAR_DIST_NAME_VERSION,
    // SPUILabelDefinitions.VAR_STATUS,
    // SPUILabelDefinitions.VAR_TOTAL_TARGETS_COUNT_STATUS,
    // SPUILabelDefinitions.VAR_NUMBER_OF_GROUPS,
    // SPUILabelDefinitions.VAR_TOTAL_TARGETS, VIRT_PROP_APPROVE, VIRT_PROP_RUN,
    // VIRT_PROP_PAUSE,
    // VIRT_PROP_UPDATE, VIRT_PROP_COPY, VIRT_PROP_DELETE,
    // SPUILabelDefinitions.VAR_CREATED_DATE,
    // SPUILabelDefinitions.VAR_CREATED_USER,
    // SPUILabelDefinitions.VAR_MODIFIED_DATE,
    // SPUILabelDefinitions.VAR_MODIFIED_BY,
    // SPUILabelDefinitions.VAR_APPROVAL_DECIDED_BY,
    // SPUILabelDefinitions.VAR_APPROVAL_REMARK, SPUILabelDefinitions.VAR_DESC);
    //
    // setColumns(columnsToShowInOrder.toArray(new
    // String[columnsToShowInOrder.size()]));
    //
    // }

    /**
     * Converter to convert {@link TotalTargetCountStatus} to formatted string
     * with status and count details.
     *
     */
    // TODO MR don't need that inner class, use Lambda
    class TotalTargetCountStatusConverter implements Converter<String, TotalTargetCountStatus> {

        private static final long serialVersionUID = 1L;

        @Override
        public Result<TotalTargetCountStatus> convertToModel(final String value, final ValueContext context) {
            return null;
        }

        @Override
        public String convertToPresentation(final TotalTargetCountStatus value, final ValueContext context) {
            return DistributionBarHelper.getDistributionBarAsHTMLString(value.getStatusTotalCountMap());
        }
    }

    /**
     * Converter to convert 0 to empty, if total target groups is zero.
     *
     */
    // TODO MR don't need that inner class, use Lambda
    class TotalTargetGroupsConverter implements Converter<String, Integer> {

        private static final long serialVersionUID = 1L;

        @Override
        public Result<Integer> convertToModel(final String value, final ValueContext context) {
            return null;
        }

        @Override
        public String convertToPresentation(final Integer value, final ValueContext context) {
            if (value == 0) {
                return "";
            }
            return value.toString();
        }

    }

    @Override
    protected void setHiddenColumns() {
        for (final String propertyId : HIDDEN_COLUMNS) {
            getColumn(propertyId).setHidden(true);
        }

        getColumn(VIRT_PROP_RUN).setHidable(false);
        getColumn(VIRT_PROP_APPROVE).setHidable(false);
        getColumn(VIRT_PROP_PAUSE).setHidable(false);
        getColumn(VIRT_PROP_DELETE).setHidable(false);
        getColumn(VIRT_PROP_UPDATE).setHidable(false);
        getColumn(VIRT_PROP_COPY).setHidable(false);
    }

    /**
     *
     * Converter to convert {@link RolloutStatus} to string.
     *
     */
    // TODO MR don't need that inner class, use Lambda
    class RolloutStatusConverter implements Converter<String, RolloutStatus> {

        private static final long serialVersionUID = 1L;

        private String convertRolloutStatusToString(final RolloutStatus value) {
            StatusFontIcon statusFontIcon = statusIconMap.get(value);
            if (statusFontIcon == null) {
                statusFontIcon = new StatusFontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE);
            }
            final String codePoint = HawkbitCommonUtil.getCodePoint(statusFontIcon);
            return HawkbitCommonUtil.getStatusLabelDetailsInString(codePoint, statusFontIcon.getStyle(),
                    UIComponentIdProvider.ROLLOUT_STATUS_LABEL_ID);
        }

        @Override
        public Result<RolloutStatus> convertToModel(final String value, final ValueContext context) {
            return null;
        }

        @Override
        public String convertToPresentation(final RolloutStatus value, final ValueContext context) {
            return convertRolloutStatusToString(value);
        }
    }


    // /**
    // * Generator class responsible to retrieve a Rollout from the grid data in
    // * order to generate a virtual property.
    // */
    // class GenericPropertyValueGenerator extends
    // PropertyValueGenerator<RolloutStatus> {
    // private static final long serialVersionUID = 1L;
    //
    // @Override
    // public RolloutStatus getValue(final Item item, final Object itemId, final
    // Object propertyId) {
    // return (RolloutStatus)
    // item.getItemProperty(SPUILabelDefinitions.VAR_STATUS).getValue();
    // }
    //
    // @Override
    // public Class<RolloutStatus> getType() {
    // return RolloutStatus.class;
    // }
    // }

    private void onClickOfRolloutName(final RendererClickEvent event) {
        rolloutUIState.setRolloutId(((ProxyRollout) event.getItem()).getId());
        rolloutUIState.setRolloutName(((ProxyRollout) event.getItem()).getName());
        rolloutUIState.setRolloutDistributionSet(((ProxyRollout) event.getItem()).getDistributionSetNameVersion());
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
        if (RolloutStatus.READY.equals(rolloutStatus)) {
            rolloutManagement.start(rolloutId);
            uiNotification.displaySuccess(i18n.getMessage("message.rollout.started", rolloutName));
            return;
        }

        if (RolloutStatus.PAUSED.equals(rolloutStatus)) {
            rolloutManagement.resumeRollout(rolloutId);
            uiNotification.displaySuccess(i18n.getMessage("message.rollout.resumed", rolloutName));
            return;
        }
    }

    private void approveRollout(final Long rolloutId) {
        final CommonDialogWindow addTargetWindow = addUpdateRolloutWindow.getWindow(rolloutId, false);
        addTargetWindow.setCaption(i18n.getMessage("caption.approve.rollout"));
        UI.getCurrent().addWindow(addTargetWindow);
        addTargetWindow.setVisible(Boolean.TRUE);
    }

    private void updateRollout(final Long rolloutId) {
        final CommonDialogWindow addTargetWindow = addUpdateRolloutWindow.getWindow(rolloutId, false);
        addTargetWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.rollout")));
        UI.getCurrent().addWindow(addTargetWindow);
        addTargetWindow.setVisible(Boolean.TRUE);
    }

    private void copyRollout(final Long rolloutId) {
        final CommonDialogWindow addTargetWindow = addUpdateRolloutWindow.getWindow(rolloutId, true);
        addTargetWindow.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.rollout")));
        UI.getCurrent().addWindow(addTargetWindow);
        addTargetWindow.setVisible(Boolean.TRUE);
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

    // @Override
    // protected CellDescriptionGenerator getDescriptionGenerator() {
    // return this::getDescription;
    // }

    // private String getDescription(final CellReference cell) {
    //
    // String description = null;
    //
    // // apparently there is no getId() on the Column object. You need to use
    // // the header caption for comparision. Check if this is even necessary.
    // // The class CellDescriptionGenerator is deprecated.
    // if
    // (SPUILabelDefinitions.VAR_STATUS.equals(cell.getColumn().getHeaderCaption()))
    // {
    // description =
    // cell.getProperty().getValue().toString().toLowerCase().replace("_", " ");
    // } else if (getActionLabeltext().equals(cell.getPropertyId())) {
    // description = getActionLabeltext().toLowerCase();
    // } else if (ROLLOUT_RENDERER_DATA.equals(cell.getPropertyId())) {
    // description = ((RolloutRendererData)
    // cell.getProperty().getValue()).getName();
    // } else if
    // (SPUILabelDefinitions.VAR_TOTAL_TARGETS_COUNT_STATUS.equals(cell.getPropertyId()))
    // {
    // description = getTooltip(((TotalTargetCountStatus)
    // cell.getValue()).getStatusTotalCountMap());
    // }
    //
    // return description;
    // }

    private static boolean hasToBeEnabled(final RolloutStatus currentRolloutStatus,
            final List<RolloutStatus> expectedRolloutStatus) {
        return expectedRolloutStatus.contains(currentRolloutStatus);
    }

    private final void hideColumnsDueToInsufficientPermissions() {

        final List<String> modifiableColumnsList = getColumns().stream().map(Column::getId)
                .collect(Collectors.toList());

        if (!permissionChecker.hasRolloutUpdatePermission()) {
            modifiableColumnsList.remove(VIRT_PROP_UPDATE);
        }
        if (!permissionChecker.hasRolloutCreatePermission()) {
            modifiableColumnsList.remove(VIRT_PROP_COPY);
        }
        if (!permissionChecker.hasRolloutApprovalPermission() || !tenantConfigManagement
                .getConfigurationValue(TenantConfigurationKey.ROLLOUT_APPROVAL_ENABLED, Boolean.class).getValue()) {
            modifiableColumnsList.remove(VIRT_PROP_APPROVE);
        }
        if (!permissionChecker.hasRolloutDeletePermission()) {
            modifiableColumnsList.remove(VIRT_PROP_DELETE);
        }
        if (!permissionChecker.hasRolloutHandlePermission()) {
            modifiableColumnsList.remove(VIRT_PROP_PAUSE);
            modifiableColumnsList.remove(VIRT_PROP_RUN);
        }

        setColumns(modifiableColumnsList.toArray(new String[modifiableColumnsList.size()]));
    }

    @Override
    public void refreshContainer() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void addContainerProperties() {
        // TODO Auto-generated method stub

    }


}
