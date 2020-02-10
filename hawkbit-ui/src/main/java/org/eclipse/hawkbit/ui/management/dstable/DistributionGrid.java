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
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.filters.DsManagementFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetManagementStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload.PinningChangedEventType;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.PinSupport;
import org.eclipse.hawkbit.ui.common.grid.support.PinSupport.PinBehaviourType;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.AssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.DsTagsToDistributionSetAssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.TargetTagsToDistributionSetAssignmentSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.TargetsToDistributionSetAssignmentSupport;
import org.eclipse.hawkbit.ui.management.dstag.filter.DistributionTagLayoutUiState;
import org.eclipse.hawkbit.ui.management.miscs.DeploymentAssignmentWindowController;
import org.eclipse.hawkbit.ui.management.targettable.TargetGridLayoutUiState;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
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
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;

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
    private final DistributionTagLayoutUiState distributionTagLayoutUiState;
    private final transient DistributionSetManagement distributionSetManagement;
    private final transient DeploymentManagement deploymentManagement;

    private final ConfigurableFilterDataProvider<ProxyDistributionSet, Void, DsManagementFilterParams> dsDataProvider;
    private final transient DistributionSetToProxyDistributionMapper distributionSetToProxyDistributionMapper;
    private final DsManagementFilterParams dsFilter;

    private final transient PinSupport<ProxyDistributionSet> pinSupport;
    private final transient DeleteSupport<ProxyDistributionSet> distributionDeleteSupport;
    private final transient DragAndDropSupport<ProxyDistributionSet> dragAndDropSupport;

    public DistributionGrid(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final TargetManagement targetManagement, final DistributionSetManagement distributionSetManagement,
            final DeploymentManagement deploymentManagement, final UiProperties uiProperties,
            final DistributionGridLayoutUiState distributionGridLayoutUiState,
            final TargetGridLayoutUiState targetGridLayoutUiState,
            final DistributionTagLayoutUiState distributionTagLayoutUiState) {
        super(i18n, eventBus, permissionChecker);

        this.targetGridLayoutUiState = targetGridLayoutUiState;
        this.distributionGridLayoutUiState = distributionGridLayoutUiState;
        this.distributionTagLayoutUiState = distributionTagLayoutUiState;
        this.distributionSetManagement = distributionSetManagement;
        this.deploymentManagement = deploymentManagement;

        this.distributionSetToProxyDistributionMapper = new DistributionSetToProxyDistributionMapper();
        this.dsDataProvider = new DistributionSetManagementStateDataProvider(distributionSetManagement,
                distributionSetToProxyDistributionMapper).withConfigurableFilter();
        this.dsFilter = new DsManagementFilterParams();

        setResizeSupport(new DistributionResizeSupport());

        setSelectionSupport(new SelectionSupport<ProxyDistributionSet>(this, eventBus, Layout.DS_LIST, View.DEPLOYMENT,
                this::updateLastSelectedDsUiState));
        if (distributionGridLayoutUiState.isMaximized()) {
            getSelectionSupport().disableSelection();
        } else {
            getSelectionSupport().enableMultiSelection();
        }

        this.pinSupport = new PinSupport<>(this::publishPinningChangedEvent);

        this.distributionDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("distribution.details.header"),
                ProxyDistributionSet::getNameVersion, permissionChecker, notification, this::deleteDistributionSets,
                UIComponentIdProvider.DS_DELETE_CONFIRMATION_DIALOG);

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

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, notification, sourceTargetAssignmentStrategies);
        this.dragAndDropSupport.addDragAndDrop();

        initTargetPinningStyleGenerator();
        init();
    }

    @Override
    protected void init() {
        super.init();

        addStyleName("grid-row-border");
    }

    private void updateLastSelectedDsUiState(final SelectionChangedEventType type,
            final ProxyDistributionSet selectedDs) {
        if (type == SelectionChangedEventType.ENTITY_DESELECTED) {
            distributionGridLayoutUiState.setSelectedDsId(null);
        } else {
            distributionGridLayoutUiState.setSelectedDsId(selectedDs.getId());
        }
    }

    private void deleteDistributionSets(final Collection<ProxyDistributionSet> setsToBeDeleted) {
        final Collection<Long> dsToBeDeletedIds = setsToBeDeleted.stream().map(ProxyIdentifiableEntity::getId)
                .collect(Collectors.toList());
        distributionSetManagement.delete(dsToBeDeletedIds);

        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_REMOVED, ProxyDistributionSet.class, dsToBeDeletedIds));

        pinSupport.unPinItemIfDeleted(dsToBeDeletedIds);
    }

    private void publishPinningChangedEvent(final PinBehaviourType pinType, final ProxyDistributionSet pinnedItem) {
        if (!StringUtils.isEmpty(dsFilter.getPinnedTargetControllerId())) {
            dsFilter.setPinnedTargetControllerId(null, Collections.emptyList(), null);
            getFilterDataProvider().setFilter(dsFilter);
        } else {
            // TODO: somehow move it to abstract class/TypeFilterButtonClick
            // needed to trigger style generator
            getDataCommunicator().reset();
        }

        eventBus.publish(EventTopics.PINNING_CHANGED, this,
                new PinningChangedEventPayload<Long>(
                        pinType == PinBehaviourType.PINNED ? PinningChangedEventType.ENTITY_PINNED
                                : PinningChangedEventType.ENTITY_UNPINNED,
                        ProxyDistributionSet.class, pinnedItem.getId()));

        distributionGridLayoutUiState.setPinnedDsId(pinType == PinBehaviourType.PINNED ? pinnedItem.getId() : null);
    }

    private void initTargetPinningStyleGenerator() {
        setStyleGenerator(ds -> {
            if (dsFilter.getPinnedTargetControllerId() != null) {
                return getAssignedOrInstalledStyle(dsFilter.getAssignedToTargetDsIds(),
                        dsFilter.getInstalledToTargetDsId(), ds.getId());
            }

            return null;
        });
    }

    private String getAssignedOrInstalledStyle(final Collection<Long> assignedToPinnedTargetFilterDsIds,
            final Long installedToPinnedTargetFilterDsId, final Long dsId) {
        if (installedToPinnedTargetFilterDsId != null && installedToPinnedTargetFilterDsId.equals(dsId)) {
            return SPUIDefinitions.HIGHLIGHT_GREEN;
        }

        if (!CollectionUtils.isEmpty(assignedToPinnedTargetFilterDsIds)
                && assignedToPinnedTargetFilterDsIds.contains(dsId)) {
            return SPUIDefinitions.HIGHLIGHT_ORANGE;
        }

        return null;
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.DIST_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyDistributionSet, Void, DsManagementFilterParams> getFilterDataProvider() {
        return dsDataProvider;
    }

    public void updateSearchFilter(final String searchFilter) {
        dsFilter.setSearchText(!StringUtils.isEmpty(searchFilter) ? String.format("%%%s%%", searchFilter) : null);
        getFilterDataProvider().setFilter(dsFilter);
    }

    public void updateTagFilter(final Collection<String> tagFilterNames) {
        dsFilter.setDistributionSetTags(tagFilterNames);
        getFilterDataProvider().setFilter(dsFilter);
    }

    public void updateNoTagFilter(final boolean isNoTagClicked) {
        dsFilter.setNoTagClicked(isNoTagClicked);
        getFilterDataProvider().setFilter(dsFilter);
    }

    public void updatePinnedTargetFilter(final String pinnedControllerId) {
        if (pinSupport.clearPinning()) {
            // in order to update pinning column style
            getDataCommunicator().reset();
            distributionGridLayoutUiState.setPinnedDsId(null);
        }

        if (StringUtils.isEmpty(pinnedControllerId) && dsFilter.getPinnedTargetControllerId() == null) {
            return;
        }

        if (!StringUtils.isEmpty(pinnedControllerId)) {
            final Collection<Long> assignedDsIds = getAssignedToTargetDsIds(pinnedControllerId);
            final Long installedDsId = getInstalledToTargetDsId(pinnedControllerId);

            if (!CollectionUtils.isEmpty(assignedDsIds) || installedDsId != null) {
                dsFilter.setPinnedTargetControllerId(pinnedControllerId, assignedDsIds, installedDsId);
                getFilterDataProvider().setFilter(dsFilter);

                return;
            }
        }

        dsFilter.setPinnedTargetControllerId(null, Collections.emptyList(), null);
        getFilterDataProvider().setFilter(dsFilter);
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

    private Long getInstalledToTargetDsId(final String controllerId) {
        return deploymentManagement.getInstalledDistributionSet(controllerId).map(DistributionSet::getId).orElse(null);
    }

    /**
     * Creates the grid content for maximized-state.
     */
    public void createMaximizedContent() {
        getSelectionSupport().disableSelection();
        getResizeSupport().createMaximizedContent();
        recalculateColumnWidths();
    }

    /**
     * Creates the grid content for normal (minimized) state.
     */
    public void createMinimizedContent() {
        getSelectionSupport().enableMultiSelection();
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
        addComponentColumn(ds -> buildActionButton(event -> pinSupport.changeItemPinning(ds), VaadinIcons.PIN,
                UIMessageIdProvider.TOOLTIP_DISTRIBUTION_SET_PIN, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.DIST_PIN_ICON + "." + ds.getId(), true)).setId(DS_PIN_BUTTON_ID)
                        .setMinimumWidth(50d).setStyleGenerator(pinSupport::getPinningStyle);

        addComponentColumn(
                ds -> buildActionButton(clickEvent -> distributionDeleteSupport.openConfirmationWindowDeleteAction(ds),
                        VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                        UIComponentIdProvider.DIST_DELET_ICON + "." + ds.getId(),
                        distributionDeleteSupport.hasDeletePermission())).setId(DS_DELETE_BUTTON_ID)
                                .setCaption(i18n.getMessage("header.action.delete")).setMinimumWidth(50d);

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

    public void restoreState() {
        final String pinnedControllerId = targetGridLayoutUiState.getPinnedControllerId();
        // TODO: remove duplication with updatePinnedTargetFilter method
        if (!StringUtils.isEmpty(pinnedControllerId)) {
            final Collection<Long> assignedDsIds = getAssignedToTargetDsIds(pinnedControllerId);
            final Long installedDsId = getInstalledToTargetDsId(pinnedControllerId);

            if (!CollectionUtils.isEmpty(assignedDsIds) || installedDsId != null) {
                dsFilter.setPinnedTargetControllerId(pinnedControllerId, assignedDsIds, installedDsId);
            }
        }

        final String searchFilter = distributionGridLayoutUiState.getSearchFilter();
        dsFilter.setSearchText(!StringUtils.isEmpty(searchFilter) ? String.format("%%%s%%", searchFilter) : null);

        dsFilter.setNoTagClicked(distributionTagLayoutUiState.isNoTagClicked());

        final Collection<String> tagFilterNames = distributionTagLayoutUiState.getClickedTargetTagIdsWithName()
                .values();
        if (!CollectionUtils.isEmpty(tagFilterNames)) {
            dsFilter.setDistributionSetTags(tagFilterNames);
        }

        getFilterDataProvider().setFilter(dsFilter);
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
