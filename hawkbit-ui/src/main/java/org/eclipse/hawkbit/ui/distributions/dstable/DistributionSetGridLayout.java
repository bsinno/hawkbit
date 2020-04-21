/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.detailslayout.DistributionSetDetailsHeader;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.LayoutViewAware;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.SearchFilterListener;
import org.eclipse.hawkbit.ui.common.layout.listener.SelectionChangedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.TypeFilterListener;
import org.eclipse.hawkbit.ui.common.layout.listener.support.EntityModifiedGridRefreshAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.support.EntityModifiedSelectionAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.support.EntityModifiedTagTokenAwareSupport;
import org.eclipse.hawkbit.ui.distributions.disttype.filter.DSTypeFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * DistributionSet table layout.
 */
public class DistributionSetGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final DistributionSetGridHeader distributionSetGridHeader;
    private final DistributionSetGrid distributionSetGrid;
    private final DistributionSetDetailsHeader distributionSetDetailsHeader;
    private final DistributionSetDetails distributionSetDetails;

    private final transient SearchFilterListener searchFilterListener;
    private final transient TypeFilterListener<DistributionSetType> typeFilterListener;
    private final transient SelectionChangedListener<ProxyDistributionSet> masterEntityChangedListener;
    private final transient EntityModifiedListener<ProxyDistributionSet> entityModifiedListener;
    private final transient EntityModifiedListener<ProxyTag> tagModifiedListener;

    public DistributionSetGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final UINotification uiNotification,
            final EntityFactory entityFactory, final TargetManagement targetManagement,
            final TargetFilterQueryManagement targetFilterQueryManagement,
            final DistributionSetManagement distributionSetManagement, final SoftwareModuleManagement smManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement,
            final DistributionSetTagManagement distributionSetTagManagement,
            final SoftwareModuleTypeManagement smTypeManagement, final SystemManagement systemManagement,
            final TenantConfigurationManagement configManagement, final SystemSecurityContext systemSecurityContext,
            final DSTypeFilterLayoutUiState dSTypeFilterLayoutUiState,
            final DistributionSetGridLayoutUiState distributionSetGridLayoutUiState) {
        final DsWindowBuilder dsWindowBuilder = new DsWindowBuilder(i18n, entityFactory, eventBus, uiNotification,
                systemManagement, systemSecurityContext, configManagement, distributionSetManagement,
                distributionSetTypeManagement);
        final DsMetaDataWindowBuilder dsMetaDataWindowBuilder = new DsMetaDataWindowBuilder(i18n, entityFactory,
                eventBus, uiNotification, permissionChecker, distributionSetManagement);

        this.distributionSetGridHeader = new DistributionSetGridHeader(i18n, permissionChecker, eventBus,
                dsWindowBuilder, dSTypeFilterLayoutUiState, distributionSetGridLayoutUiState);
        this.distributionSetGrid = new DistributionSetGrid(eventBus, i18n, permissionChecker, uiNotification,
                targetManagement, distributionSetManagement, smManagement, distributionSetTypeManagement,
                smTypeManagement, dSTypeFilterLayoutUiState, distributionSetGridLayoutUiState);

        this.distributionSetDetailsHeader = new DistributionSetDetailsHeader(i18n, permissionChecker, eventBus,
                uiNotification, dsWindowBuilder, dsMetaDataWindowBuilder);
        this.distributionSetDetails = new DistributionSetDetails(i18n, eventBus, permissionChecker, uiNotification,
                distributionSetManagement, smManagement, distributionSetTypeManagement, distributionSetTagManagement,
                targetFilterQueryManagement, configManagement, systemSecurityContext, dsMetaDataWindowBuilder);

        final LayoutViewAware layoutView = new LayoutViewAware(Layout.DS_LIST, View.DISTRIBUTIONS);
        final LayoutViewAware typeLayoutView = new LayoutViewAware(Layout.DS_TYPE_FILTER, View.DISTRIBUTIONS);

        this.searchFilterListener = new SearchFilterListener(eventBus, layoutView, this::filterGridBySearch);
        this.typeFilterListener = new TypeFilterListener<>(eventBus, typeLayoutView, this::filterGridByType);
        this.masterEntityChangedListener = new SelectionChangedListener<>(eventBus, layoutView,
                getMasterEntityAwareComponents());
        this.entityModifiedListener = new EntityModifiedListener.Builder<>(eventBus, ProxyDistributionSet.class)
                .entityModifiedAwareSupports(getEntityModifiedAwareSupports()).build();
        this.tagModifiedListener = new EntityModifiedListener.Builder<>(eventBus, ProxyTag.class)
                .entityModifiedAwareSupports(getTagModifiedAwareSupports()).parentEntityType(ProxyDistributionSet.class)
                .build();

        buildLayout(distributionSetGridHeader, distributionSetGrid, distributionSetDetailsHeader,
                distributionSetDetails);
    }

    private List<MasterEntityAwareComponent<ProxyDistributionSet>> getMasterEntityAwareComponents() {
        return Arrays.asList(distributionSetDetailsHeader, distributionSetDetails);
    }

    private List<EntityModifiedAwareSupport> getEntityModifiedAwareSupports() {
        return Arrays.asList(EntityModifiedGridRefreshAwareSupport.of(distributionSetGrid::refreshContainer),
                EntityModifiedSelectionAwareSupport.of(distributionSetGrid.getSelectionSupport(),
                        distributionSetGrid::mapIdToProxyEntity));
    }

    private List<EntityModifiedAwareSupport> getTagModifiedAwareSupports() {
        return Collections
                .singletonList(EntityModifiedTagTokenAwareSupport.of(distributionSetDetails.getDistributionTagToken()));
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

    public void restoreState() {
        distributionSetGridHeader.restoreState();
        distributionSetGrid.restoreState();
    }

    public void unsubscribeListener() {
        searchFilterListener.unsubscribe();
        typeFilterListener.unsubscribe();
        masterEntityChangedListener.unsubscribe();
        entityModifiedListener.unsubscribe();
        tagModifiedListener.unsubscribe();
    }
}
