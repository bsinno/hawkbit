/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.builder.GridComponentBuilder;
import org.eclipse.hawkbit.ui.common.data.filters.DsManagementFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetManagementStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload.PinningChangedEventType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.common.grid.support.PinSupport;
import org.eclipse.hawkbit.ui.common.grid.support.PinSupport.PinBehaviourType;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.AssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.DsTagsToDistributionSetAssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.TargetTagsToDistributionSetAssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.TargetsToDistributionSetAssignmentSupport;
import org.eclipse.hawkbit.ui.common.state.TagFilterLayoutUiState;
import org.eclipse.hawkbit.ui.management.miscs.DeploymentAssignmentWindowController;
import org.eclipse.hawkbit.ui.management.targettable.TargetGridLayoutUiState;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;

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

    private final TargetGridLayoutUiState targetGridLayoutUiState;
    private final DistributionGridLayoutUiState distributionGridLayoutUiState;
    private final TagFilterLayoutUiState distributionTagLayoutUiState;
    private final transient DistributionSetManagement distributionSetManagement;
    private final transient DeploymentManagement deploymentManagement;
    private final transient DistributionSetToProxyDistributionMapper distributionSetToProxyDistributionMapper;

    private final transient PinSupport<ProxyDistributionSet, String> pinSupport;
    private final transient DeleteSupport<ProxyDistributionSet> distributionDeleteSupport;
    private final transient DragAndDropSupport<ProxyDistributionSet> dragAndDropSupport;
    private final transient FilterSupport<ProxyDistributionSet, DsManagementFilterParams> filterSupport;

    public DistributionGrid(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final TargetManagement targetManagement, final DistributionSetManagement distributionSetManagement,
            final DeploymentManagement deploymentManagement, final UiProperties uiProperties,
            final DistributionGridLayoutUiState distributionGridLayoutUiState,
            final TargetGridLayoutUiState targetGridLayoutUiState,
            final TagFilterLayoutUiState distributionTagLayoutUiState) {
        super(i18n, eventBus, permissionChecker);

        this.targetGridLayoutUiState = targetGridLayoutUiState;
        this.distributionGridLayoutUiState = distributionGridLayoutUiState;
        this.distributionTagLayoutUiState = distributionTagLayoutUiState;
        this.distributionSetManagement = distributionSetManagement;
        this.deploymentManagement = deploymentManagement;
        this.distributionSetToProxyDistributionMapper = new DistributionSetToProxyDistributionMapper();

        setResizeSupport(new DistributionResizeSupport());

        setSelectionSupport(new SelectionSupport<ProxyDistributionSet>(this, eventBus, EventLayout.DS_LIST,
                EventView.DEPLOYMENT, this::mapIdToProxyEntity, this::getSelectedEntityIdFromUiState,
                this::setSelectedEntityIdToUiState));
        if (distributionGridLayoutUiState.isMaximized()) {
            getSelectionSupport().disableSelection();
        } else {
            getSelectionSupport().enableMultiSelection();
        }

        this.pinSupport = new PinSupport<>(this::publishPinningChangedEvent, this::refreshItem,
                this::getAssignedToTargetDsIds, this::getInstalledToTargetDsIds);

        this.distributionDeleteSupport = new DeleteSupport<>(this, i18n, notification,
                i18n.getMessage("distribution.details.header"), ProxyDistributionSet::getNameVersion,
                this::deleteDistributionSets, UIComponentIdProvider.DS_DELETE_CONFIRMATION_DIALOG);

        final Map<String, AssignmentSupport<?, ProxyDistributionSet>> sourceTargetAssignmentStrategies = new HashMap<>();

        final DeploymentAssignmentWindowController assignmentController = new DeploymentAssignmentWindowController(i18n,
                uiProperties, eventBus, notification, deploymentManagement, targetGridLayoutUiState,
                distributionGridLayoutUiState);
        final TargetsToDistributionSetAssignmentSupport targetsToDsAssignment = new TargetsToDistributionSetAssignmentSupport(
                notification, i18n, permissionChecker, assignmentController);
        final TargetTagsToDistributionSetAssignmentSupport targetTagsToDsAssignment = new TargetTagsToDistributionSetAssignmentSupport(
                notification, i18n, targetManagement, targetsToDsAssignment);
        final DsTagsToDistributionSetAssignmentSupport dsTagsToDsAssignment = new DsTagsToDistributionSetAssignmentSupport(
                notification, i18n, distributionSetManagement, distributionTagLayoutUiState, eventBus);

        sourceTargetAssignmentStrategies.put(UIComponentIdProvider.TARGET_TABLE_ID, targetsToDsAssignment);
        sourceTargetAssignmentStrategies.put(UIComponentIdProvider.TARGET_TAG_TABLE_ID, targetTagsToDsAssignment);
        sourceTargetAssignmentStrategies.put(UIComponentIdProvider.DISTRIBUTION_TAG_TABLE_ID, dsTagsToDsAssignment);

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, notification, sourceTargetAssignmentStrategies,
                eventBus);
        if (!distributionGridLayoutUiState.isMaximized()) {
            this.dragAndDropSupport.addDragAndDrop();
        }

        this.filterSupport = new FilterSupport<>(
                new DistributionSetManagementStateDataProvider(distributionSetManagement,
                        distributionSetToProxyDistributionMapper),
                getSelectionSupport()::deselectAll);
        this.filterSupport.setFilter(new DsManagementFilterParams());

        initFilterMappings();
        initTargetPinningStyleGenerator();
        init();
    }

    private void initFilterMappings() {
        filterSupport.addMapping(FilterType.SEARCH, DsManagementFilterParams::setSearchText);
        filterSupport.addMapping(FilterType.NO_TAG, DsManagementFilterParams::setNoTagClicked);
        filterSupport.addMapping(FilterType.TAG, DsManagementFilterParams::setDistributionSetTags);
    }

    @Override
    protected void init() {
        super.init();

        addStyleName("grid-row-border");
    }

    public Optional<ProxyDistributionSet> mapIdToProxyEntity(final long entityId) {
        return distributionSetManagement.get(entityId).map(distributionSetToProxyDistributionMapper::map);
    }

    private Optional<Long> getSelectedEntityIdFromUiState() {
        return Optional.ofNullable(distributionGridLayoutUiState.getSelectedEntityId());
    }

    private void setSelectedEntityIdToUiState(final Optional<Long> entityId) {
        distributionGridLayoutUiState.setSelectedEntityId(entityId.orElse(null));
    }

    private boolean deleteDistributionSets(final Collection<ProxyDistributionSet> setsToBeDeleted) {
        final Collection<Long> dsToBeDeletedIds = setsToBeDeleted.stream().map(ProxyIdentifiableEntity::getId)
                .collect(Collectors.toList());
        distributionSetManagement.delete(dsToBeDeletedIds);

        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_REMOVED, ProxyDistributionSet.class, dsToBeDeletedIds));

        return true;
    }

    private void publishPinningChangedEvent(final PinBehaviourType pinType, final ProxyDistributionSet pinnedItem) {
        if (!StringUtils.isEmpty(getFilter().getPinnedTargetControllerId())) {
            getFilter().setPinnedTargetControllerId(null);
            filterSupport.refreshFilter();
        }

        eventBus.publish(EventTopics.PINNING_CHANGED, this,
                new PinningChangedEventPayload<Long>(
                        pinType == PinBehaviourType.PINNED ? PinningChangedEventType.ENTITY_PINNED
                                : PinningChangedEventType.ENTITY_UNPINNED,
                        ProxyDistributionSet.class, pinnedItem.getId()));

        distributionGridLayoutUiState.setPinnedDsId(pinType == PinBehaviourType.PINNED ? pinnedItem.getId() : null);
    }

    private Collection<Long> getAssignedToTargetDsIds(final String controllerId) {
        // currently we do not support getting all assigned distribution sets
        // even in multi-assignment mode
        final Long assignedDsId = deploymentManagement.getAssignedDistributionSet(controllerId)
                .map(DistributionSet::getId).orElse(null);

        if (assignedDsId != null) {
            return Collections.singletonList(assignedDsId);
        }

        return Collections.emptyList();
    }

    private Collection<Long> getInstalledToTargetDsIds(final String controllerId) {
        final Long installedDsId = deploymentManagement.getInstalledDistributionSet(controllerId)
                .map(DistributionSet::getId).orElse(null);

        return installedDsId != null ? Collections.singletonList(installedDsId) : Collections.emptyList();
    }

    private void initTargetPinningStyleGenerator() {
        setStyleGenerator(ds -> {
            if (getFilter().getPinnedTargetControllerId() != null && pinSupport.assignedOrInstalledNotEmpty()) {
                return pinSupport.getAssignedOrInstalledRowStyle(ds.getId());
            }

            return null;
        });
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.DIST_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyDistributionSet, Void, DsManagementFilterParams> getFilterDataProvider() {
        return filterSupport.getFilterDataProvider();
    }

    public void updatePinnedTarget(final String pinnedControllerId) {
        if (pinSupport.clearPinning()) {
            distributionGridLayoutUiState.setPinnedDsId(null);
        }

        if (StringUtils.isEmpty(pinnedControllerId) && getFilter().getPinnedTargetControllerId() == null) {
            return;
        }

        pinSupport.repopulateAssignedAndInstalled(pinnedControllerId);
        getFilter().setPinnedTargetControllerId(pinnedControllerId);

        filterSupport.refreshFilter();
        getSelectionSupport().deselectAll();
    }

    /**
     * Creates the grid content for maximized-state.
     */
    public void createMaximizedContent() {
        getSelectionSupport().disableSelection();
        dragAndDropSupport.removeDragAndDrop();
        getResizeSupport().createMaximizedContent();
        recalculateColumnWidths();
    }

    /**
     * Creates the grid content for normal (minimized) state.
     */
    public void createMinimizedContent() {
        getSelectionSupport().enableMultiSelection();
        dragAndDropSupport.addDragAndDrop();
        getResizeSupport().createMinimizedContent();
        recalculateColumnWidths();
    }

    @Override
    public void addColumns() {
        // TODO: check width
        addColumn(ProxyDistributionSet::getName).setId(DS_NAME_ID).setCaption(i18n.getMessage("header.name"))
                .setMinimumWidth(100d).setExpandRatio(1);

        addColumn(ProxyDistributionSet::getVersion).setId(DS_VERSION_ID).setCaption(i18n.getMessage("header.version"))
                .setMinimumWidth(50d);

        addActionColumns();

        addColumn(ProxyDistributionSet::getCreatedBy).setId(DS_CREATED_BY_ID)
                .setCaption(i18n.getMessage("header.createdBy")).setHidden(true);

        addColumn(ProxyDistributionSet::getCreatedDate).setId(DS_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate")).setHidden(true);

        addColumn(ProxyDistributionSet::getLastModifiedBy).setId(DS_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setHidden(true);

        addColumn(ProxyDistributionSet::getModifiedDate).setId(DS_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setHidden(true);

        addColumn(ProxyDistributionSet::getDescription).setId(DS_DESC_ID)
                .setCaption(i18n.getMessage("header.description")).setHidden(true);
    }

    private void addActionColumns() {
        addComponentColumn(ds -> GridComponentBuilder.buildActionButton(i18n, event -> pinSupport.changeItemPinning(ds),
                VaadinIcons.PIN, UIMessageIdProvider.TOOLTIP_DISTRIBUTION_SET_PIN,
                SPUIStyleDefinitions.STATUS_ICON_NEUTRAL, UIComponentIdProvider.DIST_PIN_ICON + "." + ds.getId(), true))
                        .setId(DS_PIN_BUTTON_ID).setMinimumWidth(50d).setStyleGenerator(pinSupport::getPinningStyle);

        addComponentColumn(ds -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> distributionDeleteSupport.openConfirmationWindowDeleteAction(ds), VaadinIcons.TRASH,
                UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.DIST_DELET_ICON + "." + ds.getId(),
                permissionChecker.hasDeleteRepositoryPermission())).setId(DS_DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.delete")).setMinimumWidth(50d);

        getDefaultHeaderRow().join(DS_PIN_BUTTON_ID, DS_DELETE_BUTTON_ID).setText(i18n.getMessage("header.action"));
    }

    public void restoreState() {
        final Long pinnedDsId = distributionGridLayoutUiState.getPinnedDsId();
        if (pinnedDsId != null) {
            final ProxyDistributionSet pinnedDs = new ProxyDistributionSet();
            pinnedDs.setId(pinnedDsId);
            pinSupport.restorePinning(pinnedDs);
        }

        final String pinnedControllerId = targetGridLayoutUiState.getPinnedControllerId();
        if (!StringUtils.isEmpty(pinnedControllerId)) {
            pinSupport.repopulateAssignedAndInstalled(pinnedControllerId);
            getFilter().setPinnedTargetControllerId(pinnedControllerId);
        }

        getFilter().setSearchText(distributionGridLayoutUiState.getSearchFilter());
        getFilter().setNoTagClicked(distributionTagLayoutUiState.isNoTagClicked());

        final Collection<String> tagFilterNames = distributionTagLayoutUiState.getClickedTagIdsWithName().values();
        if (!CollectionUtils.isEmpty(tagFilterNames)) {
            getFilter().setDistributionSetTags(tagFilterNames);
        }

        filterSupport.refreshFilter();
        getSelectionSupport().restoreSelection();
    }

    public DsManagementFilterParams getFilter() {
        return filterSupport.getFilter();
    }

    public FilterSupport<ProxyDistributionSet, DsManagementFilterParams> getFilterSupport() {
        return filterSupport;
    }

    public PinSupport<ProxyDistributionSet, String> getPinSupport() {
        return pinSupport;
    }

    /**
     * Adds support to resize the Distribution grid.
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

            getColumns().forEach(column -> column.setHidable(true));
        }

        @Override
        public void setMaximizedColumnExpandRatio() {
            getColumns().forEach(column -> column.setExpandRatio(0));

            getColumn(DS_NAME_ID).setExpandRatio(1);
            getColumn(DS_DESC_ID).setExpandRatio(1);
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

            getColumns().forEach(column -> column.setHidable(false));
        }

        @Override
        public void setMinimizedColumnExpandRatio() {
            getColumns().forEach(column -> column.setExpandRatio(0));

            getColumn(DS_NAME_ID).setExpandRatio(1);
        }
    }
}
