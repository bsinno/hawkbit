/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.event.remote.entity.RemoteEntityEvent;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.filters.TargetManagementFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetManagementStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.entity.DistributionSetIdName;
import org.eclipse.hawkbit.ui.common.entity.TargetIdName;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.TargetModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.PinSupport;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.AssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.DistributionSetsToTargetAssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.TargetTagsToTargetAssignmentSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.management.DeploymentView;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.event.PinUnpinEvent;
import org.eclipse.hawkbit.ui.management.event.TargetAddUpdateWindowEvent;
import org.eclipse.hawkbit.ui.management.event.TargetFilterEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent.TargetComponentEvent;
import org.eclipse.hawkbit.ui.management.miscs.DeploymentAssignmentWindowController;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.push.TargetUpdatedEventContainer;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * Concrete implementation of Target grid which is displayed on the Deployment
 * View.
 */
public class TargetGrid extends AbstractGrid<ProxyTarget, TargetManagementFilterParams> {
    private static final long serialVersionUID = 1L;

    private static final String TARGET_STATUS_ID = "targetStatus";
    private static final String TARGET_NAME_ID = "targetName";
    private static final String TARGET_POLLING_STATUS_ID = "targetPolling";
    private static final String TARGET_CREATED_BY_ID = "targetCreatedBy";
    private static final String TARGET_CREATED_DATE_ID = "targetCreatedDate";
    private static final String TARGET_MODIFIED_BY_ID = "targetModifiedBy";
    private static final String TARGET_MODIFIED_DATE_ID = "targetModifiedDate";
    private static final String TARGET_DESC_ID = "targetDescription";
    private static final String TARGET_PIN_BUTTON_ID = "targetPinButton";
    private static final String TARGET_DELETE_BUTTON_ID = "targetDeleteButton";

    private final ManagementUIState managementUIState;
    private final transient TargetManagement targetManagement;
    private final transient DeploymentManagement deploymentManagement;

    private final Map<TargetUpdateStatus, ProxyFontIcon> targetStatusIconMap = new EnumMap<>(TargetUpdateStatus.class);

    private final ConfigurableFilterDataProvider<ProxyTarget, Void, TargetManagementFilterParams> targetDataProvider;
    private final TargetToProxyTargetMapper targetToProxyTargetMapper;
    private final PinSupport<ProxyTarget> pinSupport;
    private final DeleteSupport<ProxyTarget> targetDeleteSupport;
    private final DragAndDropSupport<ProxyTarget> dragAndDropSupport;

    public TargetGrid(final UIEventBus eventBus, final VaadinMessageSource i18n, final UINotification notification,
            final TargetManagement targetManagement, final ManagementUIState managementUIState,
            final SpPermissionChecker permChecker, final DeploymentManagement deploymentManagement,
            final TenantConfigurationManagement configManagement, final SystemSecurityContext systemSecurityContext,
            final UiProperties uiProperties) {
        super(i18n, eventBus, permChecker);

        this.managementUIState = managementUIState;
        this.targetManagement = targetManagement;
        this.deploymentManagement = deploymentManagement;

        this.targetToProxyTargetMapper = new TargetToProxyTargetMapper(i18n);
        this.targetDataProvider = new TargetManagementStateDataProvider(targetManagement, managementUIState,
                targetToProxyTargetMapper).withConfigurableFilter();

        setResizeSupport(new TargetResizeSupport());

        setSelectionSupport(new SelectionSupport<ProxyTarget>(this, eventBus, DeploymentView.VIEW_NAME,
                this::updateLastSelectedTargetUiState));
        if (managementUIState.isTargetTableMaximized()) {
            getSelectionSupport().disableSelection();
        } else {
            getSelectionSupport().enableMultiSelection();
        }

        this.pinSupport = new PinSupport<>(eventBus, PinUnpinEvent.PIN_TARGET, PinUnpinEvent.UNPIN_TARGET,
                () -> setStyleGenerator(item -> null), this::getPinnedTargetIdFromUiState,
                this::setPinnedTargetIdInUiState);

        this.targetDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("target.details.header"),
                permChecker, notification, this::targetsDeletionCallback);

        final Map<String, AssignmentSupport<?, ProxyTarget>> sourceTargetAssignmentStrategies = new HashMap<>();

        final DeploymentAssignmentWindowController assignmentController = new DeploymentAssignmentWindowController(i18n,
                uiProperties, managementUIState, eventBus, notification, deploymentManagement);
        final DistributionSetsToTargetAssignmentSupport distributionsToTargetAssignment = new DistributionSetsToTargetAssignmentSupport(
                notification, i18n, systemSecurityContext, configManagement, permChecker, assignmentController);
        final TargetTagsToTargetAssignmentSupport targetTagsToTargetAssignment = new TargetTagsToTargetAssignmentSupport(
                notification, i18n, targetManagement, managementUIState, eventBus);

        sourceTargetAssignmentStrategies.put(UIComponentIdProvider.DIST_TABLE_ID, distributionsToTargetAssignment);
        sourceTargetAssignmentStrategies.put(UIComponentIdProvider.TARGET_TAG_TABLE_ID, targetTagsToTargetAssignment);

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, notification, sourceTargetAssignmentStrategies);
        this.dragAndDropSupport.addDragAndDrop();

        initTargetStatusIconMap();

        init();
    }

    private void updateLastSelectedTargetUiState(final SelectionChangedEventType type,
            final ProxyTarget selectedTarget) {
        if (type == SelectionChangedEventType.ENTITY_DESELECTED) {
            managementUIState.setLastSelectedTargetId(null);
        } else {
            managementUIState.setLastSelectedTargetId(selectedTarget.getId());
        }
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.TARGET_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyTarget, Void, TargetManagementFilterParams> getFilterDataProvider() {
        return targetDataProvider;
    }

    private Optional<Long> getPinnedTargetIdFromUiState() {
        return managementUIState.getDistributionTableFilters().getPinnedTarget().map(TargetIdName::getTargetId);
    }

    private void setPinnedTargetIdInUiState(final ProxyTarget target) {
        managementUIState.getDistributionTableFilters()
                .setPinnedTarget(target != null ? new TargetIdName(target.getId(), target.getControllerId()) : null);
    }

    private void targetsDeletionCallback(final Collection<ProxyTarget> targetsToBeDeleted) {
        final Collection<Long> targetToBeDeletedIds = targetsToBeDeleted.stream().map(ProxyIdentifiableEntity::getId)
                .collect(Collectors.toList());
        targetManagement.delete(targetToBeDeletedIds);

        eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                new TargetModifiedEventPayload(EntityModifiedEventType.ENTITY_REMOVED, targetToBeDeletedIds));

        getPinnedTargetIdFromUiState()
                .ifPresent(pinnedTargetId -> pinSupport.unPinItemAfterDeletion(pinnedTargetId, targetToBeDeletedIds));
        managementUIState.getSelectedTargetId().clear();
    }

    // TODO: check if icons are correct
    private void initTargetStatusIconMap() {
        targetStatusIconMap.put(TargetUpdateStatus.ERROR, new ProxyFontIcon(VaadinIcons.EXCLAMATION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_RED, getTargetStatusDescription(TargetUpdateStatus.ERROR)));
        targetStatusIconMap.put(TargetUpdateStatus.UNKNOWN, new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_BLUE, getTargetStatusDescription(TargetUpdateStatus.UNKNOWN)));
        targetStatusIconMap.put(TargetUpdateStatus.IN_SYNC, new ProxyFontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getTargetStatusDescription(TargetUpdateStatus.IN_SYNC)));
        targetStatusIconMap.put(TargetUpdateStatus.PENDING, new ProxyFontIcon(VaadinIcons.DOT_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_YELLOW, getTargetStatusDescription(TargetUpdateStatus.PENDING)));
        targetStatusIconMap.put(TargetUpdateStatus.REGISTERED,
                new ProxyFontIcon(VaadinIcons.DOT_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_LIGHT_BLUE,
                        getTargetStatusDescription(TargetUpdateStatus.REGISTERED)));
    }

    private String getTargetStatusDescription(final TargetUpdateStatus targetStatus) {
        return i18n
                .getMessage(UIMessageIdProvider.TOOLTIP_TARGET_STATUS_PREFIX + targetStatus.toString().toLowerCase());
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onTargetUpdatedEvents(final TargetUpdatedEventContainer eventContainer) {
        if (!eventContainer.getEvents().isEmpty()) {
            // TODO: Consider updating only corresponding targets with
            // dataProvider.refreshItem() based on
            // target ids instead of full refresh (evaluate
            // getDataCommunicator().getKeyMapper())
            refreshContainer();
        }

        // TODO: consider removing after registering corresponding target
        // selection listeners
        publishTargetSelectedEntityForRefresh(eventContainer.getEvents().stream());
    }

    private void publishTargetSelectedEntityForRefresh(
            final Stream<? extends RemoteEntityEvent<Target>> targetEntityEventStream) {
        targetEntityEventStream.filter(event -> isLastSelectedTarget(event.getEntityId())).filter(Objects::nonNull)
                .findAny()
                .ifPresent(event -> eventBus.publish(this, new TargetTableEvent(BaseEntityEventType.SELECTED_ENTITY,
                        targetToProxyTargetMapper.map(event.getEntity()))));
    }

    private boolean isLastSelectedTarget(final Long targetId) {
        return managementUIState.getLastSelectedTargetId()
                .map(lastSelectedTargetId -> lastSelectedTargetId.equals(targetId)).orElse(false);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final PinUnpinEvent pinUnpinEvent) {
        UI.getCurrent().access(() -> {
            if (pinUnpinEvent == PinUnpinEvent.PIN_DISTRIBUTION) {
                /* if distribution set is pinned, unpin target if pinned */
                setPinnedTargetIdInUiState(null);
                refreshFilter();
                // TODO: check if refreshFilter() should be called at the end
                setStyleGenerator(item -> pinSupport.getRowStyleForPinning(item.getAssignedDistributionSet().getId(),
                        item.getInstalledDistributionSet().getId(), getPinnedDistIdFromUiState()));
            } else if (pinUnpinEvent == PinUnpinEvent.UNPIN_DISTRIBUTION) {
                refreshFilter();
                setStyleGenerator(item -> null);
            }
        });
    }

    private void refreshFilter() {
        final TargetManagementFilterParams filterParams = new TargetManagementFilterParams(getPinnedDistIdFromUiState(),
                getSearchTextFromUiState(), getTargetUpdateStatusFromUiState(), getOverdueStateFromUiState(),
                getDistributionIdFromUiState(), isNoTagClickedFromUiState(), getTargetTagsFromUiState(),
                getTargetFilterQueryIdFromUiState());

        getFilterDataProvider().setFilter(filterParams);
    }

    private String getSearchTextFromUiState() {
        return managementUIState.getTargetTableFilters().getSearchText()
                .filter(searchText -> !StringUtils.isEmpty(searchText)).map(value -> String.format("%%%s%%", value))
                .orElse(null);
    }

    private Long getPinnedDistIdFromUiState() {
        return managementUIState.getTargetTableFilters().getPinnedDistId().orElse(null);
    }

    private Long getTargetFilterQueryIdFromUiState() {
        return managementUIState.getTargetTableFilters().getTargetFilterQuery().orElse(null);
    }

    private String[] getTargetTagsFromUiState() {
        return managementUIState.getTargetTableFilters().getClickedTargetTags().stream().toArray(String[]::new);
    }

    private Boolean isNoTagClickedFromUiState() {
        return managementUIState.getTargetTableFilters().isNoTagSelected();
    }

    private Long getDistributionIdFromUiState() {
        return managementUIState.getTargetTableFilters().getDistributionSet().map(DistributionSetIdName::getId)
                .orElse(null);
    }

    private Boolean getOverdueStateFromUiState() {
        return managementUIState.getTargetTableFilters().isOverdueFilterEnabled();
    }

    private Collection<TargetUpdateStatus> getTargetUpdateStatusFromUiState() {
        return managementUIState.getTargetTableFilters().getClickedStatusTargetTags();
    }

    private Set<Long> getItemIdsToSelectFromUiState() {
        return managementUIState.getSelectedTargetId().isEmpty() ? null : managementUIState.getSelectedTargetId();
    }

    private void setLastSelectedTargetId(final Long lastSelectedTargetId) {
        managementUIState.setLastSelectedTargetId(lastSelectedTargetId);
    }

    // TODO: do we still need it?
    private void setManagementUIStateValues(final Set<Long> selectedTargetIds, final Long lastSelectedTargetId) {
        managementUIState.setSelectedTargetId(selectedTargetIds);
        managementUIState.setLastSelectedTargetId(lastSelectedTargetId);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void addOrEditEvent(final TargetAddUpdateWindowEvent targetUIEvent) {
        if (BaseEntityEventType.UPDATED_ENTITY != targetUIEvent.getEventType()) {
            return;
        }
        UI.getCurrent().access(() -> updateTarget(targetUIEvent.getEntity()));
    }

    /**
     * To update target details in the grid.
     *
     * @param updatedTarget
     *            as reference
     */
    public void updateTarget(final ProxyTarget updatedTarget) {
        if (updatedTarget != null) {
            if (getPinnedDistIdFromUiState() == null) {
                updatedTarget.setInstalledDistributionSet(null);
                updatedTarget.setAssignedDistributionSet(null);
            } else {
                deploymentManagement.getAssignedDistributionSet(updatedTarget.getControllerId())
                        .ifPresent(updatedTarget::setAssignedDistributionSet);
                deploymentManagement.getInstalledDistributionSet(updatedTarget.getControllerId())
                        .ifPresent(updatedTarget::setInstalledDistributionSet);
            }

            getDataProvider().refreshItem(updatedTarget);
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final TargetFilterEvent filterEvent) {
        UI.getCurrent().access(() -> {
            refreshFilter();
            eventBus.publish(this, ManagementUIEvent.TARGET_TABLE_FILTER);
        });
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent managementUIEvent) {
        UI.getCurrent().access(() -> {
            if (tableIsFilteredByTagsAndTagWasUnassignedFromTarget(managementUIEvent)
                    || tableIsFilteredByNoTagAndTagWasAssignedToTarget(managementUIEvent)) {
                refreshFilter();
            }
        });
    }

    private boolean tableIsFilteredByTagsAndTagWasUnassignedFromTarget(final ManagementUIEvent managementUIEvent) {
        return managementUIEvent == ManagementUIEvent.UNASSIGN_TARGET_TAG && isFilteredByTags();
    }

    private boolean isFilteredByTags() {
        return !managementUIState.getTargetTableFilters().getClickedTargetTags().isEmpty();
    }

    private boolean tableIsFilteredByNoTagAndTagWasAssignedToTarget(final ManagementUIEvent managementUIEvent) {
        return managementUIEvent == ManagementUIEvent.ASSIGN_TARGET_TAG && isNoTagClickedFromUiState();
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final TargetTableEvent event) {
        // TODO: consider moving min/max events to ManagementUIEvent similar to
        // ActionHistoryGrid
        if (BaseEntityEventType.MINIMIZED == event.getEventType()) {
            UI.getCurrent().access(this::createMinimizedContent);
        } else if (BaseEntityEventType.MAXIMIZED == event.getEventType()) {
            UI.getCurrent().access(this::createMaximizedContent);
        } else if (BaseEntityEventType.ADD_ENTITY == event.getEventType()
                || BaseEntityEventType.REMOVE_ENTITY == event.getEventType()) {
            UI.getCurrent().access(this::refreshContainer);

            // TODO: check selection/deselection, refactor if neccessary
            if (BaseEntityEventType.ADD_ENTITY == event.getEventType()) {
                select(event.getEntity());
            }
        }
    }

    /**
     * Creates the grid content for maximized-state.
     */
    private void createMaximizedContent() {
        getSelectionSupport().disableSelection();
        getResizeSupport().createMaximizedContent();
        recalculateColumnWidths();
    }

    /**
     * Creates the grid content for normal (minimized) state.
     */
    private void createMinimizedContent() {
        getSelectionSupport().enableMultiSelection();
        getResizeSupport().createMinimizedContent();
        recalculateColumnWidths();
    }

    @Override
    public void addColumns() {
        // TODO: check width
        addColumn(ProxyTarget::getName).setId(TARGET_NAME_ID).setCaption(i18n.getMessage("header.name"))
                .setMinimumWidth(100d).setMaximumWidth(150d).setHidable(false).setHidden(false);

        addComponentColumn(this::buildTargetPollingStatusIcon).setId(TARGET_POLLING_STATUS_ID).setMinimumWidth(50d)
                .setMaximumWidth(50d).setHidable(false).setHidden(false)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);

        addComponentColumn(this::buildTargetStatusIcon).setId(TARGET_STATUS_ID).setMinimumWidth(50d)
                .setMaximumWidth(50d).setHidable(false).setHidden(false)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);

        getDefaultHeaderRow().join(TARGET_POLLING_STATUS_ID, TARGET_STATUS_ID)
                .setText(i18n.getMessage("header.status"));

        addActionColumns();

        addColumn(ProxyTarget::getCreatedBy).setId(TARGET_CREATED_BY_ID).setCaption(i18n.getMessage("header.createdBy"))
                .setHidable(true).setHidden(true);

        addColumn(ProxyTarget::getCreatedDate).setId(TARGET_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate")).setHidable(true).setHidden(true);

        addColumn(ProxyTarget::getLastModifiedBy).setId(TARGET_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setHidable(true).setHidden(true);

        addColumn(ProxyTarget::getModifiedDate).setId(TARGET_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setHidable(true).setHidden(true);

        addColumn(ProxyTarget::getDescription).setId(TARGET_DESC_ID).setCaption(i18n.getMessage("header.description"))
                .setHidable(true).setHidden(true);
    }

    private Label buildTargetStatusIcon(final ProxyTarget target) {
        final ProxyFontIcon targetStatusFontIcon = Optional.ofNullable(targetStatusIconMap.get(target.getUpdateStatus()))
                .orElse(new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String targetStatusId = new StringBuilder(UIComponentIdProvider.TARGET_TABLE_STATUS_LABEL_ID).append(".")
                .append(target.getId()).toString();

        return buildLabelIcon(targetStatusFontIcon, targetStatusId);
    }

    private Label buildTargetPollingStatusIcon(final ProxyTarget target) {
        final String pollStatusToolTip = target.getPollStatusToolTip();

        final ProxyFontIcon pollStatusFontIcon = StringUtils.hasText(pollStatusToolTip)
                ? new ProxyFontIcon(VaadinIcons.EXCLAMATION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                        pollStatusToolTip)
                : new ProxyFontIcon(VaadinIcons.CLOCK, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                        i18n.getMessage(UIMessageIdProvider.TOOLTIP_IN_TIME));

        final String pollStatusId = new StringBuilder(UIComponentIdProvider.TARGET_TABLE_POLLING_STATUS_LABEL_ID)
                .append(".").append(target.getId()).toString();

        return buildLabelIcon(pollStatusFontIcon, pollStatusId);
    }

    private void addActionColumns() {
        addComponentColumn(target -> {
            final Button pinBtn = buildActionButton(event -> pinSupport.pinItemListener(target, event.getButton()),
                    VaadinIcons.PIN, UIMessageIdProvider.TOOLTIP_TARGET_PIN, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                    UIComponentIdProvider.TARGET_PIN_ICON + "." + target.getId(), true);

            return pinSupport.buildPinActionButton(pinBtn, target);
        }).setId(TARGET_PIN_BUTTON_ID).setMinimumWidth(50d).setMaximumWidth(50d).setHidable(false).setHidden(false);

        addComponentColumn(target -> buildActionButton(
                clickEvent -> targetDeleteSupport.openConfirmationWindowDeleteAction(target, target.getName()),
                VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.TARGET_DELET_ICON + "." + target.getId(),
                targetDeleteSupport.hasDeletePermission())).setId(TARGET_DELETE_BUTTON_ID).setMinimumWidth(50d)
                        .setMaximumWidth(50d).setHidable(false).setHidden(false);

        getDefaultHeaderRow().join(TARGET_PIN_BUTTON_ID, TARGET_DELETE_BUTTON_ID)
                .setText(i18n.getMessage("header.action"));
    }

    private Button buildActionButton(final ClickListener clickListener, final VaadinIcons icon,
            final String descriptionProperty, final String style, final String buttonId, final boolean enabled) {
        final Button actionButton = new Button();

        actionButton.addClickListener(clickListener);
        actionButton.setIcon(icon);
        actionButton.setDescription(i18n.getMessage(descriptionProperty));
        actionButton.setEnabled(enabled);
        actionButton.setId(buttonId);
        actionButton.addStyleName("tiny");
        actionButton.addStyleName("borderless");
        actionButton.addStyleName("button-no-border");
        actionButton.addStyleName("action-type-padding");
        actionButton.addStyleName(style);

        return actionButton;
    }

    @Override
    public void refreshContainer() {
        super.refreshContainer();
        // TODO: check if we really need to send this event here (in order to
        // update count label another approach could be used)
        eventBus.publish(this, new TargetTableEvent(TargetComponentEvent.REFRESH_TARGETS));
    }

    /**
     * Adds support to resize the target grid.
     */
    class TargetResizeSupport implements ResizeSupport {

        private final String[] maxColumnOrder = new String[] { TARGET_NAME_ID, TARGET_CREATED_BY_ID,
                TARGET_CREATED_DATE_ID, TARGET_MODIFIED_BY_ID, TARGET_MODIFIED_DATE_ID, TARGET_DESC_ID,
                TARGET_DELETE_BUTTON_ID };

        private final String[] minColumnOrder = new String[] { TARGET_NAME_ID, TARGET_POLLING_STATUS_ID,
                TARGET_STATUS_ID, TARGET_PIN_BUTTON_ID, TARGET_DELETE_BUTTON_ID };

        @Override
        public void setMaximizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(maxColumnOrder);
        }

        @Override
        public void setMaximizedHiddenColumns() {
            getColumn(TARGET_POLLING_STATUS_ID).setHidden(true);
            getColumn(TARGET_STATUS_ID).setHidden(true);
            getColumn(TARGET_PIN_BUTTON_ID).setHidden(true);

            getColumn(TARGET_CREATED_BY_ID).setHidden(false);
            getColumn(TARGET_CREATED_DATE_ID).setHidden(false);
            getColumn(TARGET_MODIFIED_BY_ID).setHidden(false);
            getColumn(TARGET_MODIFIED_DATE_ID).setHidden(false);
            getColumn(TARGET_DESC_ID).setHidden(false);
        }

        @Override
        public void setMaximizedColumnExpandRatio() {
        }

        @Override
        public void setMinimizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(minColumnOrder);
        }

        @Override
        public void setMinimizedHiddenColumns() {
            getColumn(TARGET_POLLING_STATUS_ID).setHidden(false);
            getColumn(TARGET_STATUS_ID).setHidden(false);
            getColumn(TARGET_PIN_BUTTON_ID).setHidden(false);

            getColumn(TARGET_CREATED_BY_ID).setHidden(true);
            getColumn(TARGET_CREATED_DATE_ID).setHidden(true);
            getColumn(TARGET_MODIFIED_BY_ID).setHidden(true);
            getColumn(TARGET_MODIFIED_DATE_ID).setHidden(true);
            getColumn(TARGET_DESC_ID).setHidden(true);
        }

        @Override
        public void setMinimizedColumnExpandRatio() {
        }
    }
}