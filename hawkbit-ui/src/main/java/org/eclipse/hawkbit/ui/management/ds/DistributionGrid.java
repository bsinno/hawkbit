/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.ds;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.RemoteEntityEvent;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.filters.DsManagementFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetManagementStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.entity.TargetIdName;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.PinSupport;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.AssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.DsTagsToDistributionSetAssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.TargetTagsToDistributionSetAssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.TargetsToDistributionSetAssignmentSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.dd.criteria.ManagementViewClientCriterion;
import org.eclipse.hawkbit.ui.management.event.DistributionTableEvent;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.event.PinUnpinEvent;
import org.eclipse.hawkbit.ui.management.event.RefreshDistributionTableByFilterEvent;
import org.eclipse.hawkbit.ui.management.miscs.DeploymentAssignmentWindowController;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.push.DistributionSetUpdatedEventContainer;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.eclipse.hawkbit.ui.view.filter.OnlyEventsFromDeploymentViewFilter;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.UI;

/**
 * Distribution set grid which is shown on the Deployment View.
 */
public class DistributionGrid extends AbstractGrid<ProxyDistributionSet, DsManagementFilterParams> {
    private static final long serialVersionUID = 1L;

    private static final String DS_NAME_ID = "dsName";
    private static final String DS_VERSION_ID = "dsVersion";
    private static final String DS_CREATED_BY_ID = "dsCreatedBy";
    private static final String DS_CREATED_DATE_ID = "dsCreatedDate";
    private static final String DS_MODIFIED_BY_ID = "dsModifiedBy";
    private static final String DS_MODIFIED_DATE_ID = "dsModifiedDate";
    private static final String DS_DESC_ID = "dsDescription";
    private static final String DS_PIN_BUTTON_ID = "dsPinnButton";
    private static final String DS_DELETE_BUTTON_ID = "dsDeleteButton";

    private final ManagementUIState managementUIState;
    private final transient DistributionSetManagement distributionSetManagement;
    private final transient DeploymentManagement deploymentManagement;

    private final ConfigurableFilterDataProvider<ProxyDistributionSet, Void, DsManagementFilterParams> dsDataProvider;
    private final DistributionSetToProxyDistributionMapper distributionSetToProxyDistributionMapper;
    private final PinSupport<ProxyDistributionSet> pinSupport;
    private final DeleteSupport<ProxyDistributionSet> distributionDeleteSupport;
    private final DragAndDropSupport<ProxyDistributionSet> dragAndDropSupport;

    DistributionGrid(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final ManagementUIState managementUIState,
            final ManagementViewClientCriterion managementViewClientCriterion, final TargetManagement targetManagement,
            final DistributionSetManagement distributionSetManagement, final DeploymentManagement deploymentManagement,
            final TargetTagManagement targetTagManagement, final UiProperties uiProperties) {
        super(i18n, eventBus, permissionChecker);

        this.managementUIState = managementUIState;
        this.distributionSetManagement = distributionSetManagement;
        this.deploymentManagement = deploymentManagement;

        this.distributionSetToProxyDistributionMapper = new DistributionSetToProxyDistributionMapper();
        this.dsDataProvider = new DistributionSetManagementStateDataProvider(distributionSetManagement,
                distributionSetToProxyDistributionMapper).withConfigurableFilter();

        setResizeSupport(new DistributionResizeSupport());

        setSelectionSupport(new SelectionSupport<ProxyDistributionSet>(this));
        if (managementUIState.isDsTableMaximized()) {
            getSelectionSupport().disableSelection();
        } else {
            getSelectionSupport().enableMultiSelection();
        }

        this.pinSupport = new PinSupport<>(eventBus, PinUnpinEvent.PIN_DISTRIBUTION, PinUnpinEvent.UNPIN_DISTRIBUTION,
                () -> setStyleGenerator(item -> null), this::getPinnedDsIdFromUiState, this::setPinnedDsIdInUiState);

        this.distributionDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("distribution.details.header"),
                permissionChecker, notification, this::dsIdsDeletionCallback);

        final Map<String, AssignmentSupport<?, ProxyDistributionSet>> sourceTargetAssignmentStrategies = new HashMap<>();

        final DeploymentAssignmentWindowController assignmentController = new DeploymentAssignmentWindowController(i18n,
                uiProperties, managementUIState, eventBus, notification, deploymentManagement);
        final TargetsToDistributionSetAssignmentSupport targetsToDsAssignment = new TargetsToDistributionSetAssignmentSupport(
                notification, i18n, assignmentController);
        final TargetTagsToDistributionSetAssignmentSupport targetTagsToDsAssignment = new TargetTagsToDistributionSetAssignmentSupport(
                notification, i18n, targetManagement, targetsToDsAssignment);
        final DsTagsToDistributionSetAssignmentSupport dsTagsToDsAssignment = new DsTagsToDistributionSetAssignmentSupport(
                notification, i18n, distributionSetManagement, managementUIState, eventBus);

        sourceTargetAssignmentStrategies.put(UIComponentIdProvider.TARGET_TABLE_ID, targetsToDsAssignment);
        sourceTargetAssignmentStrategies.put(UIComponentIdProvider.TARGET_TAG_TABLE_ID, targetTagsToDsAssignment);
        sourceTargetAssignmentStrategies.put(UIComponentIdProvider.DISTRIBUTION_TAG_TABLE_ID, dsTagsToDsAssignment);

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, notification, permissionChecker,
                sourceTargetAssignmentStrategies);
        this.dragAndDropSupport.addDragAndDrop();

        init();
    }

    private Optional<Long> getPinnedDsIdFromUiState() {
        return managementUIState.getTargetTableFilters().getPinnedDistId();
    }

    private void setPinnedDsIdInUiState(final ProxyDistributionSet ds) {
        managementUIState.getTargetTableFilters().setPinnedDistId(ds != null ? ds.getId() : null);
    }

    private void dsIdsDeletionCallback(final Collection<Long> dsToBeDeletedIds) {
        distributionSetManagement.delete(dsToBeDeletedIds);

        // TODO: should we really pass the dsToBeDeletedIds? We call
        // dataprovider refreshAll anyway after receiving the event
        eventBus.publish(this, new DistributionTableEvent(BaseEntityEventType.REMOVE_ENTITY, dsToBeDeletedIds));

        getPinnedDsIdFromUiState()
                .ifPresent(pinnedDsId -> pinSupport.unPinItemAfterDeletion(pinnedDsId, dsToBeDeletedIds));
        managementUIState.getSelectedDsIdName().clear();
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.DIST_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyDistributionSet, Void, DsManagementFilterParams> getFilterDataProvider() {
        return dsDataProvider;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onDistributionSetUpdateEvents(final DistributionSetUpdatedEventContainer eventContainer) {
        deselectIncompleteDs(eventContainer.getEvents().stream());

        if (!eventContainer.getEvents().isEmpty()) {
            // TODO: Consider updating only corresponding distribution sets with
            // dataProvider.refreshItem() based on distribution set ids instead
            // of full refresh (evaluate getDataCommunicator().getKeyMapper())
            refreshContainer();
        }

        // TODO: do we really need it?
        publishDsSelectedEntityForRefresh(eventContainer.getEvents().stream());
    }

    private void deselectIncompleteDs(final Stream<DistributionSetUpdatedEvent> dsEntityUpdateEventStream) {
        if (dsEntityUpdateEventStream.filter(event -> !event.isComplete()).map(DistributionSetUpdatedEvent::getEntityId)
                .anyMatch(this::isLastSelectedDs)) {
            // TODO: consider renaming it to setLastSelectedDsIdName
            managementUIState.setLastSelectedEntityId(null);
        }
    }

    private void publishDsSelectedEntityForRefresh(
            final Stream<? extends RemoteEntityEvent<DistributionSet>> dsEntityEventStream) {
        dsEntityEventStream.filter(event -> isLastSelectedDs(event.getEntityId())).filter(Objects::nonNull).findAny()
                .ifPresent(event -> eventBus.publish(this,
                        new DistributionTableEvent(BaseEntityEventType.SELECTED_ENTITY, event.getEntity())));
    }

    private boolean isLastSelectedDs(final Long dsId) {
        return managementUIState.getLastSelectedDsIdName().map(lastSelectedDsId -> lastSelectedDsId.equals(dsId))
                .orElse(false);
    }

    /**
     * DistributionTableFilterEvent.
     *
     * @param filterEvent
     *            as instance of {@link RefreshDistributionTableByFilterEvent}
     */
    @EventBusListenerMethod(scope = EventScope.UI, filter = OnlyEventsFromDeploymentViewFilter.class)
    void onEvent(final RefreshDistributionTableByFilterEvent filterEvent) {
        UI.getCurrent().access(this::refreshFilter);
    }

    private void refreshFilter() {
        final DsManagementFilterParams filterParams = new DsManagementFilterParams(getSearchTextFromUiState(),
                isNoTagClickedFromUiState(), getDistributionTagsFromUiState(), getPinnedTargetFromUiState());

        getFilterDataProvider().setFilter(filterParams);
    }

    private String getSearchTextFromUiState() {
        return managementUIState.getDistributionTableFilters().getSearchText()
                .filter(searchText -> !StringUtils.isEmpty(searchText)).map(value -> String.format("%%%s%%", value))
                .orElse(null);
    }

    private Boolean isNoTagClickedFromUiState() {
        return managementUIState.getDistributionTableFilters().isNoTagSelected();
    }

    private List<String> getDistributionTagsFromUiState() {
        return managementUIState.getDistributionTableFilters().getDistSetTags();
    }

    private TargetIdName getPinnedTargetFromUiState() {
        return managementUIState.getDistributionTableFilters().getPinnedTarget().orElse(null);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final DistributionTableEvent event) {
        if (BaseEntityEventType.MINIMIZED == event.getEventType()) {
            UI.getCurrent().access(this::createMinimizedContent);
        } else if (BaseEntityEventType.MAXIMIZED == event.getEventType()) {
            UI.getCurrent().access(this::createMaximizedContent);
        } else if (BaseEntityEventType.ADD_ENTITY == event.getEventType()
                || BaseEntityEventType.REMOVE_ENTITY == event.getEventType()) {
            UI.getCurrent().access(this::refreshContainer);
        }

        if (BaseEntityEventType.UPDATED_ENTITY != event.getEventType()) {
            return;
        }
        UI.getCurrent().access(() -> updateDistributionSet(event.getEntity()));
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

    /**
     * To update distribution set details in the grid.
     *
     * @param updatedDs
     *            as reference
     */
    public void updateDistributionSet(final DistributionSet updatedDs) {
        if (updatedDs != null) {
            getDataProvider().refreshItem(distributionSetToProxyDistributionMapper.map(updatedDs));
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final PinUnpinEvent pinUnpinEvent) {
        UI.getCurrent().access(() -> {
            if (pinUnpinEvent == PinUnpinEvent.PIN_TARGET) {
                /* if target is pinned, unpin distribution set if pinned */
                setPinnedDsIdInUiState(null);
                refreshFilter();
                // TODO: check if refreshFilter() shuld be called after
                styleDsRowOnPinning();
            } else if (pinUnpinEvent == PinUnpinEvent.UNPIN_TARGET) {
                refreshFilter();
                setStyleGenerator(item -> null);
            }
        });
    }

    private void styleDsRowOnPinning() {
        // TODO: check if it would be better to store the ProxyTarget in Ui
        // state or extend TargetIdName with installedDs and assignedDs in order
        // not to perform database calls
        final TargetIdName pinnedTargetIdName = getPinnedTargetFromUiState();

        if (pinnedTargetIdName == null || pinnedTargetIdName.getControllerId() == null) {
            return;
        }

        final String pinnedTargetControllerId = pinnedTargetIdName.getControllerId();
        final Long installedDistId = deploymentManagement.getInstalledDistributionSet(pinnedTargetControllerId)
                .map(DistributionSet::getId).orElse(null);
        final Long assignedDistId = deploymentManagement.getAssignedDistributionSet(pinnedTargetControllerId)
                .map(DistributionSet::getId).orElse(null);

        setStyleGenerator(item -> pinSupport.getRowStyleForPinning(assignedDistId, installedDistId, item.getId()));
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent managementUIEvent) {
        UI.getCurrent().access(() -> {
            if (managementUIEvent == ManagementUIEvent.UNASSIGN_DISTRIBUTION_TAG
                    || managementUIEvent == ManagementUIEvent.ASSIGN_DISTRIBUTION_TAG) {
                refreshFilter();
            }
        });
    }

    @Override
    public void addColumns() {
        // TODO: check width
        addColumn(ProxyDistributionSet::getName).setId(DS_NAME_ID).setCaption(i18n.getMessage("header.name"))
                .setMinimumWidth(100d).setMaximumWidth(150d).setHidable(false).setHidden(false);

        addColumn(ProxyDistributionSet::getVersion).setId(DS_VERSION_ID).setCaption(i18n.getMessage("header.version"))
                .setMinimumWidth(50d).setMaximumWidth(100d).setHidable(false).setHidden(false);

        addActionColumns();

        addColumn(ProxyDistributionSet::getCreatedBy).setId(DS_CREATED_BY_ID)
                .setCaption(i18n.getMessage("header.createdBy")).setHidable(true).setHidden(true);

        addColumn(ProxyDistributionSet::getCreatedDate).setId(DS_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate")).setHidable(true).setHidden(true);

        addColumn(ProxyDistributionSet::getLastModifiedBy).setId(DS_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setHidable(true).setHidden(true);

        addColumn(ProxyDistributionSet::getModifiedDate).setId(DS_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setHidable(true).setHidden(true);

        addColumn(ProxyDistributionSet::getDescription).setId(DS_DESC_ID)
                .setCaption(i18n.getMessage("header.description")).setHidable(true).setHidden(true);
    }

    private void addActionColumns() {
        addComponentColumn(ds -> {
            final Button pinBtn = buildActionButton(event -> pinSupport.pinItemListener(ds, event.getButton()),
                    VaadinIcons.PIN, UIMessageIdProvider.TOOLTIP_DISTRIBUTION_SET_PIN,
                    SPUIStyleDefinitions.STATUS_ICON_NEUTRAL, UIComponentIdProvider.DIST_PIN_ICON + "." + ds.getId(),
                    true);

            return pinSupport.buildPinActionButton(pinBtn, ds);
        }).setId(DS_PIN_BUTTON_ID).setMinimumWidth(50d).setMaximumWidth(50d).setHidable(false).setHidden(false);

        addComponentColumn(
                ds -> buildActionButton(clickEvent -> distributionDeleteSupport.openConfirmationWindowDeleteAction(ds),
                        VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                        UIComponentIdProvider.DIST_DELET_ICON + "." + ds.getId(),
                        distributionDeleteSupport.hasDeletePermission())).setId(DS_DELETE_BUTTON_ID)
                                .setMinimumWidth(50d).setMaximumWidth(50d).setHidable(false).setHidden(false);

        getDefaultHeaderRow().join(DS_PIN_BUTTON_ID, DS_DELETE_BUTTON_ID).setText(i18n.getMessage("header.action"));
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

    private Set<Long> getItemIdsToSelectFromUiState() {
        return managementUIState.getSelectedDsIdName().isEmpty() ? null : managementUIState.getSelectedDsIdName();
    }

    /**
     * Adds support to resize the target grid.
     */
    class DistributionResizeSupport implements ResizeSupport {

        private final String[] maxColumnOrder = new String[] { DS_NAME_ID, DS_CREATED_BY_ID, DS_CREATED_DATE_ID,
                DS_MODIFIED_BY_ID, DS_MODIFIED_DATE_ID, DS_DESC_ID, DS_VERSION_ID, DS_DELETE_BUTTON_ID };

        private final String[] minColumnOrder = new String[] { DS_NAME_ID, DS_VERSION_ID, DS_PIN_BUTTON_ID,
                DS_DELETE_BUTTON_ID };

        @Override
        public void setMaximizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(maxColumnOrder);
        }

        @Override
        public void setMaximizedHiddenColumns() {
            getColumn(DS_PIN_BUTTON_ID).setHidden(true);

            getColumn(DS_CREATED_BY_ID).setHidden(false);
            getColumn(DS_CREATED_DATE_ID).setHidden(false);
            getColumn(DS_MODIFIED_BY_ID).setHidden(false);
            getColumn(DS_MODIFIED_DATE_ID).setHidden(false);
            getColumn(DS_DESC_ID).setHidden(false);
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
            getColumn(DS_PIN_BUTTON_ID).setHidden(false);

            getColumn(DS_CREATED_BY_ID).setHidden(true);
            getColumn(DS_CREATED_DATE_ID).setHidden(true);
            getColumn(DS_MODIFIED_BY_ID).setHidden(true);
            getColumn(DS_MODIFIED_DATE_ID).setHidden(true);
            getColumn(DS_DESC_ID).setHidden(true);
        }

        @Override
        public void setMinimizedColumnExpandRatio() {
        }
    }
}
