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
import java.util.Collection;
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
import org.eclipse.hawkbit.ui.common.detailslayout.DistributionSetDetailsHeader;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedGridRefreshAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedSelectionAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.SelectionChangedListener;
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

    private final transient DistributionSetGridLayoutEventListener eventListener;

    private final transient SelectionChangedListener<ProxyDistributionSet> masterEntityChangedListener;
    private final transient EntityModifiedListener<ProxyDistributionSet> entityModifiedListener;

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

        this.eventListener = new DistributionSetGridLayoutEventListener(this, eventBus);

        this.masterEntityChangedListener = new SelectionChangedListener<>(eventBus, getMasterEntityAwareComponents(),
                getView(), getLayout());
        this.entityModifiedListener = new EntityModifiedListener.Builder<>(eventBus, ProxyDistributionSet.class)
                .entityModifiedAwareSupports(getEntityModifiedAwareSupports()).build();

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

    public void restoreState() {
        distributionSetGridHeader.restoreState();
        distributionSetGrid.restoreState();
    }

    public void onDsTagsModified(final Collection<Long> entityIds, final EntityModifiedEventType entityModifiedType) {
        distributionSetDetails.onDsTagsModified(entityIds, entityModifiedType);
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

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();

        masterEntityChangedListener.unsubscribe();
        entityModifiedListener.unsubscribe();
    }

    public Layout getLayout() {
        return Layout.DS_LIST;
    }

    public View getView() {
        return View.DISTRIBUTIONS;
    }
}
