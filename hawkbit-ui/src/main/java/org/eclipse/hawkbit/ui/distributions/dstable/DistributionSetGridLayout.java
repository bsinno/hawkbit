/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.detailslayout.DistributionSetDetailsHeader;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.distributions.disttype.filter.DSTypeFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * DistributionSet table layout.
 */
public class DistributionSetGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final transient DistributionSetManagement distributionSetManagement;
    private final transient DistributionSetToProxyDistributionMapper dsToProxyDistributionMapper;

    private final DistributionSetGridHeader distributionSetGridHeader;
    private final DistributionSetGrid distributionSetGrid;
    private final DistributionSetDetailsHeader distributionSetDetailsHeader;
    private final DistributionSetDetails distributionSetDetails;

    private final DistributionSetGridLayoutUiState distributionSetGridLayoutUiState;

    private final transient DistributionSetGridLayoutEventListener eventListener;

    public DistributionSetGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final DistributionSetManagement distributionSetManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement, final TargetManagement targetManagement,
            final TargetFilterQueryManagement targetFilterQueryManagement, final EntityFactory entityFactory,
            final UINotification uiNotification, final DistributionSetTagManagement distributionSetTagManagement,
            final SystemManagement systemManagement, final TenantConfigurationManagement configManagement,
            final SystemSecurityContext systemSecurityContext,
            final DSTypeFilterLayoutUiState dSTypeFilterLayoutUiState,
            final DistributionSetGridLayoutUiState distributionSetGridLayoutUiState) {
        this.distributionSetManagement = distributionSetManagement;
        this.dsToProxyDistributionMapper = new DistributionSetToProxyDistributionMapper();
        this.distributionSetGridLayoutUiState = distributionSetGridLayoutUiState;

        final DsWindowBuilder dsWindowBuilder = new DsWindowBuilder(i18n, entityFactory, eventBus, uiNotification,
                systemManagement, systemSecurityContext, configManagement, distributionSetManagement,
                distributionSetTypeManagement);
        final DsMetaDataWindowBuilder dsMetaDataWindowBuilder = new DsMetaDataWindowBuilder(i18n, entityFactory,
                eventBus, uiNotification, permissionChecker, distributionSetManagement);

        this.distributionSetGridHeader = new DistributionSetGridHeader(i18n, permissionChecker, eventBus,
                dsWindowBuilder, dSTypeFilterLayoutUiState, distributionSetGridLayoutUiState);
        this.distributionSetGrid = new DistributionSetGrid(eventBus, i18n, permissionChecker, uiNotification,
                targetManagement, distributionSetManagement, distributionSetTypeManagement,
                distributionSetGridLayoutUiState, dsToProxyDistributionMapper);

        this.distributionSetDetailsHeader = new DistributionSetDetailsHeader(i18n, permissionChecker, eventBus,
                uiNotification, dsWindowBuilder, dsMetaDataWindowBuilder);
        this.distributionSetDetails = new DistributionSetDetails(i18n, eventBus, permissionChecker,
                distributionSetManagement, uiNotification, distributionSetTagManagement, targetFilterQueryManagement,
                configManagement, systemSecurityContext, distributionSetGridLayoutUiState, dsMetaDataWindowBuilder);

        this.eventListener = new DistributionSetGridLayoutEventListener(this, eventBus);

        buildLayout(distributionSetGridHeader, distributionSetGrid, distributionSetDetailsHeader,
                distributionSetDetails);
    }

    public void restoreState() {
        final Long lastSelectedEntityId = distributionSetGridLayoutUiState.getSelectedDsId();

        if (lastSelectedEntityId != null) {
            mapIdToProxyEntity(lastSelectedEntityId).ifPresent(distributionSetGrid::select);
        } else {
            distributionSetGrid.getSelectionSupport().selectFirstRow();
        }
    }

    // TODO: extract to parent abstract #mapIdToProxyEntity?
    private Optional<ProxyDistributionSet> mapIdToProxyEntity(final Long entityId) {
        return distributionSetManagement.get(entityId).map(dsToProxyDistributionMapper::map);
    }

    // TODO: extract to parent #onMasterEntityChanged?
    public void onDsChanged(final ProxyDistributionSet ds) {
        distributionSetDetailsHeader.masterEntityChanged(ds);
        distributionSetDetails.masterEntityChanged(ds);
    }

    // TODO: extract to parent #onMasterEntityUpdated?
    public void onDsUpdated(final Collection<Long> entityIds) {
        entityIds.stream().filter(entityId -> entityId.equals(distributionSetGridLayoutUiState.getSelectedDsId()))
                .findAny()
                .ifPresent(updatedEntityId -> mapIdToProxyEntity(updatedEntityId).ifPresent(this::onDsChanged));
    }

    public void showDsTypeHeaderIcon() {
        distributionSetGridHeader.showDsTypeIcon();
    }

    public void hideDsTypeHeaderIcon() {
        distributionSetGridHeader.hideDsTypeIcon();
    }

    public void filterGridBySearch(final String searchFilter) {
        distributionSetGrid.updateSearchFilter(searchFilter);
        distributionSetGrid.deselectAll();
    }

    public void filterGridByType(final DistributionSetType typeFilter) {
        distributionSetGrid.updateTypeFilter(typeFilter);
        distributionSetGrid.deselectAll();
    }

    public void maximize() {
        distributionSetGrid.createMaximizedContent();
        hideDetailsLayout();
    }

    public void minimize() {
        distributionSetGrid.createMinimizedContent();
        showDetailsLayout();
    }

    public void refreshGrid() {
        distributionSetGrid.refreshContainer();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
