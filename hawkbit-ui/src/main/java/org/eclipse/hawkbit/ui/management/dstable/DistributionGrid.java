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

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.builder.GridComponentBuilder;
import org.eclipse.hawkbit.ui.common.data.filters.DsManagementFilterParams;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetManagementStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload.PinningChangedEventType;
import org.eclipse.hawkbit.ui.common.grid.AbstractDsGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.common.grid.support.PinSupport;
import org.eclipse.hawkbit.ui.common.grid.support.PinSupport.PinBehaviourType;
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
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;

/**
 * Distribution set grid which is shown on the Deployment View.
 */
public class DistributionGrid extends AbstractDsGrid<DsManagementFilterParams> {
    private static final long serialVersionUID = 1L;

    private static final String DS_PIN_BUTTON_ID = "dsPinnButton";

    private final TargetGridLayoutUiState targetGridLayoutUiState;
    private final DistributionGridLayoutUiState distributionGridLayoutUiState;
    private final TagFilterLayoutUiState distributionTagLayoutUiState;

    private final transient DeploymentManagement deploymentManagement;

    private final transient PinSupport<ProxyDistributionSet, String> pinSupport;

    public DistributionGrid(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final TargetManagement targetManagement, final DistributionSetManagement distributionSetManagement,
            final DeploymentManagement deploymentManagement, final UiProperties uiProperties,
            final DistributionGridLayoutUiState distributionGridLayoutUiState,
            final TargetGridLayoutUiState targetGridLayoutUiState,
            final TagFilterLayoutUiState distributionTagLayoutUiState) {
        super(i18n, eventBus, permissionChecker, notification, distributionSetManagement, distributionGridLayoutUiState,
                EventView.DEPLOYMENT);

        this.targetGridLayoutUiState = targetGridLayoutUiState;
        this.distributionGridLayoutUiState = distributionGridLayoutUiState;
        this.distributionTagLayoutUiState = distributionTagLayoutUiState;
        this.deploymentManagement = deploymentManagement;

        this.pinSupport = new PinSupport<>(this::publishPinningChangedEvent, this::refreshItem,
                this::getAssignedToTargetDsIds, this::getInstalledToTargetDsIds);

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

        setDragAndDropSupportSupport(
                new DragAndDropSupport<>(this, i18n, notification, sourceTargetAssignmentStrategies, eventBus));
        if (!distributionGridLayoutUiState.isMaximized()) {
            getDragAndDropSupportSupport().addDragAndDrop();
        }

        setFilterSupport(new FilterSupport<>(
                new DistributionSetManagementStateDataProvider(distributionSetManagement, dsToProxyDistributionMapper),
                getSelectionSupport()::deselectAll));
        initFilterMappings();
        getFilterSupport().setFilter(new DsManagementFilterParams());

        initTargetPinningStyleGenerator();
        init();
    }

    private void initFilterMappings() {
        getFilterSupport().addMapping(FilterType.SEARCH, DsManagementFilterParams::setSearchText,
                distributionGridLayoutUiState.getSearchFilter());
        getFilterSupport().addMapping(FilterType.NO_TAG, DsManagementFilterParams::setNoTagClicked,
                distributionTagLayoutUiState.isNoTagClicked());
        getFilterSupport().addMapping(FilterType.TAG, DsManagementFilterParams::setDistributionSetTags,
                distributionTagLayoutUiState.getClickedTagIdsWithName().values());
    }

    private void publishPinningChangedEvent(final PinBehaviourType pinType, final ProxyDistributionSet pinnedItem) {
        if (isPinFilterActive()) {
            getFilterSupport().updateFilter(DsManagementFilterParams::setPinnedTargetControllerId, null);
        }

        eventBus.publish(EventTopics.PINNING_CHANGED, this,
                new PinningChangedEventPayload<Long>(
                        pinType == PinBehaviourType.PINNED ? PinningChangedEventType.ENTITY_PINNED
                                : PinningChangedEventType.ENTITY_UNPINNED,
                        ProxyDistributionSet.class, pinnedItem.getId()));

        distributionGridLayoutUiState.setPinnedDsId(pinType == PinBehaviourType.PINNED ? pinnedItem.getId() : null);
    }

    private boolean isPinFilterActive() {
        return getFilter().map(DsManagementFilterParams::getPinnedTargetControllerId).isPresent();
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
            if (isPinFilterActive() && pinSupport.assignedOrInstalledNotEmpty()) {
                return pinSupport.getAssignedOrInstalledRowStyle(ds.getId());
            }

            return null;
        });
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.DIST_TABLE_ID;
    }

    public void updatePinnedTarget(final String pinnedControllerId) {
        if (pinSupport.clearPinning()) {
            distributionGridLayoutUiState.setPinnedDsId(null);
        }

        if (StringUtils.isEmpty(pinnedControllerId) && !isPinFilterActive()) {
            return;
        }

        pinSupport.repopulateAssignedAndInstalled(pinnedControllerId);
        getFilterSupport().updateFilter(DsManagementFilterParams::setPinnedTargetControllerId, pinnedControllerId);
    }

    @Override
    public void addColumns() {
        addNameColumn().setMinimumWidth(100d).setExpandRatio(1);

        addVersionColumn().setMinimumWidth(100d);

        addPinColumn().setMinimumWidth(50d);

        addDeleteColumn().setMinimumWidth(80d);

        getDefaultHeaderRow().join(DS_PIN_BUTTON_ID, DS_DELETE_BUTTON_ID).setText(i18n.getMessage("header.action"));
    }

    private Column<ProxyDistributionSet, Button> addPinColumn() {
        return addComponentColumn(ds -> GridComponentBuilder.buildActionButton(i18n,
                event -> pinSupport.changeItemPinning(ds), VaadinIcons.PIN,
                UIMessageIdProvider.TOOLTIP_DISTRIBUTION_SET_PIN, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.DIST_PIN_ICON + "." + ds.getId(), true)).setId(DS_PIN_BUTTON_ID)
                        .setStyleGenerator(pinSupport::getPinningStyle);
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
            getFilter().ifPresent(filter -> filter.setPinnedTargetControllerId(pinnedControllerId));
        }

        if (hasFilterSupport()) {
            getFilterSupport().restoreFilter();
        }

        if (hasSelectionSupport()) {
            getSelectionSupport().restoreSelection();
        }
    }

    public PinSupport<ProxyDistributionSet, String> getPinSupport() {
        return pinSupport;
    }
}
