/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstable;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.detailslayout.DistributionSetDetailsHeader;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.distributions.dstable.DsMetaDataWindowBuilder;
import org.eclipse.hawkbit.ui.distributions.dstable.DsWindowBuilder;
import org.eclipse.hawkbit.ui.management.dstag.filter.DistributionTagLayoutUiState;
import org.eclipse.hawkbit.ui.management.targettable.TargetGridLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Distribution Set table layout which is shown on the Distribution View
 */
public class DistributionGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final transient DistributionSetManagement distributionSetManagement;
    private final transient DistributionSetToProxyDistributionMapper dsToProxyDistributionMapper;

    private final DistributionGridHeader distributionGridHeader;
    private final DistributionGrid distributionGrid;
    private final DistributionSetDetailsHeader distributionSetDetailsHeader;
    private final DistributionDetails distributionDetails;

    private final DistributionGridLayoutUiState distributionGridLayoutUiState;

    private final transient DistributionGridLayoutEventListener eventListener;

    public DistributionGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final EntityFactory entityFactory,
            final UINotification notification, final TargetManagement targetManagement,
            final DistributionSetManagement distributionSetManagement, final SoftwareModuleManagement smManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement,
            final DistributionSetTagManagement distributionSetTagManagement, final SystemManagement systemManagement,
            final DeploymentManagement deploymentManagement, final TenantConfigurationManagement configManagement,
            final SystemSecurityContext systemSecurityContext, final UiProperties uiProperties,
            final DistributionGridLayoutUiState distributionGridLayoutUiState,
            final DistributionTagLayoutUiState distributionTagLayoutUiState,
            final TargetGridLayoutUiState targetGridLayoutUiState) {
        this.distributionSetManagement = distributionSetManagement;
        this.dsToProxyDistributionMapper = new DistributionSetToProxyDistributionMapper();
        this.distributionGridLayoutUiState = distributionGridLayoutUiState;

        final DsWindowBuilder dsWindowBuilder = new DsWindowBuilder(i18n, entityFactory, eventBus, notification,
                systemManagement, systemSecurityContext, configManagement, distributionSetManagement,
                distributionSetTypeManagement);
        final DsMetaDataWindowBuilder dsMetaDataWindowBuilder = new DsMetaDataWindowBuilder(i18n, entityFactory,
                eventBus, notification, permissionChecker, distributionSetManagement);

        this.distributionGridHeader = new DistributionGridHeader(i18n, permissionChecker, eventBus,
                distributionGridLayoutUiState, distributionTagLayoutUiState);
        this.distributionGrid = new DistributionGrid(eventBus, i18n, permissionChecker, notification, targetManagement,
                distributionSetManagement, deploymentManagement, uiProperties, distributionGridLayoutUiState,
                targetGridLayoutUiState, distributionTagLayoutUiState);

        this.distributionSetDetailsHeader = new DistributionSetDetailsHeader(i18n, permissionChecker, eventBus,
                notification, dsWindowBuilder, dsMetaDataWindowBuilder);
        this.distributionDetails = new DistributionDetails(i18n, eventBus, permissionChecker, notification,
                distributionSetManagement, smManagement, distributionSetTypeManagement, distributionSetTagManagement,
                configManagement, systemSecurityContext, dsMetaDataWindowBuilder);

        this.eventListener = new DistributionGridLayoutEventListener(this, eventBus);

        buildLayout(distributionGridHeader, distributionGrid, distributionSetDetailsHeader, distributionDetails);
    }

    public void restoreState() {
        distributionGridHeader.restoreState();
        distributionGrid.restoreState();

        restoreGridSelection();
    }

    private void restoreGridSelection() {
        if (!distributionGrid.hasSelectionSupport()) {
            return;
        }

        final Long lastSelectedEntityId = distributionGridLayoutUiState.getSelectedDsId();

        if (lastSelectedEntityId != null) {
            selectEntityById(lastSelectedEntityId);
        } else {
            distributionGrid.getSelectionSupport().selectFirstRow();
        }
    }

    // TODO: extract to parent abstract #selectEntityById?
    public void selectEntityById(final Long entityId) {
        if (!distributionGrid.hasSelectionSupport()) {
            return;
        }

        if (!distributionGrid.getSelectedItems().isEmpty()) {
            distributionGrid.deselectAll();
        }

        mapIdToProxyEntity(entityId).ifPresent(distributionGrid::select);
    }

    // TODO: extract to parent abstract #mapIdToProxyEntity?
    private Optional<ProxyDistributionSet> mapIdToProxyEntity(final Long entityId) {
        return distributionSetManagement.get(entityId).map(dsToProxyDistributionMapper::map);
    }

    // TODO: extract to parent #onMasterEntityChanged?
    public void onDsChanged(final ProxyDistributionSet ds) {
        if (isIncomplete(ds)) {
            resetIncompleteDs(ds);
        } else {
            distributionSetDetailsHeader.masterEntityChanged(ds);
            distributionDetails.masterEntityChanged(ds);
        }
    }

    private boolean isIncomplete(final ProxyDistributionSet ds) {
        return ds != null && !ds.getIsComplete();
    }

    private void resetIncompleteDs(final ProxyDistributionSet ds) {
        final Long dsId = ds.getId();

        if (dsId.equals(distributionGridLayoutUiState.getSelectedDsId())) {
            distributionGrid.deselect(ds);
        }

        if (dsId.equals(distributionGridLayoutUiState.getPinnedDsId())) {
            distributionGrid.unpinnItemById(ds.getId());
        }
    }

    // TODO: extract to parent #onMasterEntityUpdated?
    public void onDsUpdated(final Collection<Long> entityIds) {
        final Long selectedEntityId = distributionGrid.hasSelectionSupport()
                && distributionGrid.getSelectedItems().size() == 1
                        ? distributionGrid.getSelectedItems().iterator().next().getId()
                        : null;
        final Long pinnedDsId = distributionGridLayoutUiState.getPinnedDsId();

        if (selectedEntityId != null || pinnedDsId != null) {
            entityIds.stream().filter(entityId -> entityId.equals(selectedEntityId) || entityId.equals(pinnedDsId))
                    .findAny()
                    .ifPresent(updatedEntityId -> mapIdToProxyEntity(updatedEntityId).ifPresent(
                            updatedEntity -> distributionGrid.getSelectionSupport().sendSelectionChangedEvent(
                                    SelectionChangedEventType.ENTITY_SELECTED, updatedEntity)));
        }
    }

    public void onDsTagsModified(final Collection<Long> entityIds, final EntityModifiedEventType entityModifiedType) {
        distributionDetails.onDsTagsModified(entityIds, entityModifiedType);
    }

    public void showDsTagHeaderIcon() {
        distributionGridHeader.showDsTagIcon();
    }

    public void hideDsTagHeaderIcon() {
        distributionGridHeader.hideDsTagIcon();
    }

    public void filterGridBySearch(final String searchFilter) {
        distributionGrid.updateSearchFilter(searchFilter);
        distributionGrid.deselectAll();
    }

    public void filterGridByTags(final Collection<String> tagFilterNames) {
        distributionGrid.updateTagFilter(tagFilterNames);
        distributionGrid.deselectAll();
    }

    public void filterGridByNoTag(final boolean isNoTagClicked) {
        distributionGrid.updateNoTagFilter(isNoTagClicked);
        distributionGrid.deselectAll();
    }

    public void filterGridByPinnedTarget(final String controllerId) {
        distributionGrid.updatePinnedTargetFilter(controllerId);
        distributionGrid.deselectAll();
    }

    public void maximize() {
        distributionGrid.createMaximizedContent();
        hideDetailsLayout();
    }

    public void minimize() {
        distributionGrid.createMinimizedContent();
        showDetailsLayout();
    }

    public void refreshGrid() {
        distributionGrid.refreshContainer();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }

    public Layout getLayout() {
        return Layout.DS_LIST;
    }
}
