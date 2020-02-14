/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.BulkUploadEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.management.CountMessageLabel;
import org.eclipse.hawkbit.ui.management.dstable.DistributionGridLayoutUiState;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Target table layout.
 */
public class TargetGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final transient TargetManagement targetManagement;
    private final transient TargetToProxyTargetMapper targetMapper;

    private final TargetGridHeader targetGridHeader;
    private final TargetGrid targetGrid;
    private final TargetDetailsHeader targetDetailsHeader;
    private final TargetDetails targetDetails;
    private final transient CountMessageLabel countMessageLabel;

    private final TargetGridLayoutUiState targetGridLayoutUiState;

    private final transient TargetGridLayoutEventListener eventListener;

    public TargetGridLayout(final UIEventBus eventBus, final TargetManagement targetManagement,
            final EntityFactory entityFactory, final VaadinMessageSource i18n, final UINotification uiNotification,
            final DeploymentManagement deploymentManagement, final UiProperties uiProperties,
            final SpPermissionChecker permissionChecker, final TargetTagManagement targetTagManagement,
            final DistributionSetManagement distributionSetManagement, final Executor uiExecutor,
            final TenantConfigurationManagement configManagement, final SystemSecurityContext systemSecurityContext,
            final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState,
            final TargetGridLayoutUiState targetGridLayoutUiState,
            final TargetBulkUploadUiState targetBulkUploadUiState,
            final DistributionGridLayoutUiState distributionGridLayoutUiState) {
        this.targetManagement = targetManagement;
        this.targetMapper = new TargetToProxyTargetMapper(i18n);
        this.targetGridLayoutUiState = targetGridLayoutUiState;

        final TargetWindowBuilder targetWindowBuilder = new TargetWindowBuilder(i18n, entityFactory, eventBus,
                uiNotification, targetManagement);
        final TargetMetaDataWindowBuilder targetMetaDataWindowBuilder = new TargetMetaDataWindowBuilder(i18n,
                entityFactory, eventBus, uiNotification, permissionChecker, targetManagement);

        this.targetGridHeader = new TargetGridHeader(i18n, permissionChecker, eventBus, uiNotification,
                targetManagement, deploymentManagement, uiProperties, entityFactory, targetTagManagement,
                distributionSetManagement, uiExecutor, targetWindowBuilder, targetTagFilterLayoutUiState,
                targetGridLayoutUiState, targetBulkUploadUiState);
        this.targetGrid = new TargetGrid(eventBus, i18n, uiNotification, targetManagement, permissionChecker,
                deploymentManagement, configManagement, systemSecurityContext, uiProperties, targetGridLayoutUiState,
                distributionGridLayoutUiState, targetTagFilterLayoutUiState);

        this.targetDetailsHeader = new TargetDetailsHeader(i18n, permissionChecker, eventBus, uiNotification,
                targetWindowBuilder, targetMetaDataWindowBuilder);
        this.targetDetails = new TargetDetails(i18n, eventBus, permissionChecker, uiNotification, targetTagManagement,
                targetManagement, deploymentManagement, targetMetaDataWindowBuilder);

        this.countMessageLabel = new CountMessageLabel(targetManagement, i18n);

        initGridDataUpdatedListener();

        this.eventListener = new TargetGridLayoutEventListener(this, eventBus);

        buildLayout(targetGridHeader, targetGrid, targetDetailsHeader, targetDetails);
    }

    private void initGridDataUpdatedListener() {
        // TODO: refactor based on FilterParams
        targetGrid.getFilterDataProvider().addDataProviderListener(event -> countMessageLabel
                .displayTargetCountStatus(targetGrid.getDataSize(), targetGrid.getTargetFilter()));
    }

    public void restoreState() {
        targetGridHeader.restoreState();
        targetGrid.restoreState();

        restoreGridSelection();
    }

    private void restoreGridSelection() {
        final Long lastSelectedEntityId = targetGridLayoutUiState.getSelectedTargetId();

        if (lastSelectedEntityId != null) {
            mapIdToProxyEntity(lastSelectedEntityId).ifPresent(targetGrid::select);
        } else {
            targetGrid.getSelectionSupport().selectFirstRow();
        }
    }

    // TODO: extract to parent abstract #mapIdToProxyEntity?
    private Optional<ProxyTarget> mapIdToProxyEntity(final Long entityId) {
        return targetManagement.get(entityId).map(targetMapper::map);
    }

    // TODO: extract to parent #onMasterEntityChanged?
    public void onTargetChanged(final ProxyTarget target) {
        targetDetailsHeader.masterEntityChanged(target);
        targetDetails.masterEntityChanged(target);
    }

    // TODO: extract to parent #onMasterEntityUpdated?
    public void onTargetUpdated(final Collection<Long> entityIds) {
        entityIds.stream().filter(entityId -> entityId.equals(targetGridLayoutUiState.getSelectedTargetId())).findAny()
                .ifPresent(updatedEntityId -> mapIdToProxyEntity(updatedEntityId).ifPresent(this::onTargetChanged));
    }

    public void onTargetTagsModified(final Collection<Long> entityIds,
            final EntityModifiedEventType entityModifiedType) {
        targetDetails.onTargetTagsModified(entityIds, entityModifiedType);
    }

    public void showTargetTagHeaderIcon() {
        targetGridHeader.showTargetTagIcon();
    }

    public void hideTargetTagHeaderIcon() {
        targetGridHeader.hideTargetTagIcon();
    }

    public void onTargetFilterTabChanged(final boolean isCustomFilterTabSelected) {
        if (isCustomFilterTabSelected) {
            targetGridHeader.onSimpleFilterReset();
        } else {
            targetGridHeader.enableSearchIcon();
        }

        targetGrid.onTargetFilterTabChanged(isCustomFilterTabSelected);
    }

    public void filterGridBySearch(final String searchFilter) {
        targetGrid.updateSearchFilter(searchFilter);
        targetGrid.deselectAll();
    }

    public void filterGridByTags(final Collection<String> tagFilterNames) {
        targetGrid.updateTagFilter(tagFilterNames);
        targetGrid.deselectAll();
    }

    public void filterGridByNoTag(final boolean isActive) {
        targetGrid.updateNoTagFilter(isActive);
        targetGrid.deselectAll();
    }

    public void filterGridByStatus(final List<TargetUpdateStatus> statusFilters) {
        targetGrid.updateStatusFilter(statusFilters);
        targetGrid.deselectAll();
    }

    public void filterGridByOverdue(final boolean isOverdue) {
        targetGrid.updateOverdueFilter(isOverdue);
        targetGrid.deselectAll();
    }

    public void filterGridByCustomFilter(final Long customFilterId) {
        targetGrid.updateCustomFilter(customFilterId);
        targetGrid.deselectAll();
    }

    public void filterGridByDs(final Long dsId) {
        targetGrid.updateDsFilter(dsId);
        targetGrid.deselectAll();
    }

    public void filterGridByPinnedDs(final Long pinnedDsId) {
        targetGrid.updatePinnedDsFilter(pinnedDsId);
        targetGrid.deselectAll();
    }

    public void onBulkUploadChanged(final BulkUploadEventPayload eventPayload) {
        targetGridHeader.onBulkUploadChanged(eventPayload);
    }

    public void maximize() {
        targetGrid.createMaximizedContent();
        hideDetailsLayout();
    }

    public void minimize() {
        targetGrid.createMinimizedContent();
        showDetailsLayout();
    }

    public CountMessageLabel getCountMessageLabel() {
        return countMessageLabel;
    }

    public void refreshGrid() {
        targetGrid.refreshContainer();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }

    public Layout getLayout() {
        return Layout.TARGET_LIST;
    }
}
