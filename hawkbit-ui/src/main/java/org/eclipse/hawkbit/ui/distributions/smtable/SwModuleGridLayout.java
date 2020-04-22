/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtable.SmMetaDataWindowBuilder;
import org.eclipse.hawkbit.ui.artifacts.smtable.SmWindowBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.detailslayout.SoftwareModuleDetailsHeader;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventLayoutViewAware;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.SearchFilterListener;
import org.eclipse.hawkbit.ui.common.layout.listener.SelectionChangedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.TypeFilterListener;
import org.eclipse.hawkbit.ui.common.layout.listener.support.EntityModifiedGridRefreshAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.support.EntityModifiedSelectionAwareSupport;
import org.eclipse.hawkbit.ui.distributions.smtype.filter.DistSMTypeFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Implementation of software module Layout on the Distribution View
 */
public class SwModuleGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final SwModuleGridHeader swModuleGridHeader;
    private final SwModuleGrid swModuleGrid;
    // TODO: change to SwModuleDetailsHeader
    private final SoftwareModuleDetailsHeader softwareModuleDetailsHeader;
    private final SwModuleDetails swModuleDetails;

    private final transient SearchFilterListener searchFilterListener;
    private final transient TypeFilterListener<SoftwareModuleType> typeFilterListener;
    private final transient SelectionChangedListener<ProxyDistributionSet> masterDsEntityChangedListener;
    private final transient SelectionChangedListener<ProxySoftwareModule> masterSmEntityChangedListener;
    private final transient EntityModifiedListener<ProxySoftwareModule> entityModifiedListener;

    public SwModuleGridLayout(final VaadinMessageSource i18n, final UINotification uiNotification,
            final UIEventBus eventBus, final SoftwareModuleManagement softwareModuleManagement,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement, final EntityFactory entityFactory,
            final SpPermissionChecker permChecker, final ArtifactManagement artifactManagement,
            final DistSMTypeFilterLayoutUiState distSMTypeFilterLayoutUiState,
            final SwModuleGridLayoutUiState swModuleGridLayoutUiState) {
        final SmWindowBuilder smWindowBuilder = new SmWindowBuilder(i18n, entityFactory, eventBus, uiNotification,
                softwareModuleManagement, softwareModuleTypeManagement);
        final SmMetaDataWindowBuilder smMetaDataWindowBuilder = new SmMetaDataWindowBuilder(i18n, entityFactory,
                eventBus, uiNotification, permChecker, softwareModuleManagement);

        this.swModuleGridHeader = new SwModuleGridHeader(i18n, permChecker, eventBus, smWindowBuilder,
                distSMTypeFilterLayoutUiState, swModuleGridLayoutUiState);
        this.swModuleGrid = new SwModuleGrid(eventBus, i18n, permChecker, uiNotification, softwareModuleManagement,
                distSMTypeFilterLayoutUiState, swModuleGridLayoutUiState);

        this.softwareModuleDetailsHeader = new SoftwareModuleDetailsHeader(i18n, permChecker, eventBus, uiNotification,
                smWindowBuilder, smMetaDataWindowBuilder, artifactManagement);
        this.swModuleDetails = new SwModuleDetails(i18n, eventBus, softwareModuleManagement, smMetaDataWindowBuilder);

        final EventLayoutViewAware layoutView = new EventLayoutViewAware(EventLayout.SM_LIST, EventView.DISTRIBUTIONS);
        final EventLayoutViewAware typeLayoutView = new EventLayoutViewAware(EventLayout.SM_TYPE_FILTER, EventView.DISTRIBUTIONS);
        final EventLayoutViewAware masterLayoutView = new EventLayoutViewAware(EventLayout.DS_LIST, EventView.DISTRIBUTIONS);

        this.searchFilterListener = new SearchFilterListener(eventBus, layoutView, this::filterGridBySearch);
        this.typeFilterListener = new TypeFilterListener<>(eventBus, typeLayoutView, this::filterGridByType);
        this.masterDsEntityChangedListener = new SelectionChangedListener<>(eventBus, masterLayoutView,
                Collections.singletonList(swModuleGrid));
        this.masterSmEntityChangedListener = new SelectionChangedListener<>(eventBus, layoutView,
                getMasterEntityAwareComponents());
        this.entityModifiedListener = new EntityModifiedListener.Builder<>(eventBus, ProxySoftwareModule.class)
                .entityModifiedAwareSupports(getEntityModifiedAwareSupports()).build();

        buildLayout(swModuleGridHeader, swModuleGrid, softwareModuleDetailsHeader, swModuleDetails);
    }

    private List<MasterEntityAwareComponent<ProxySoftwareModule>> getMasterEntityAwareComponents() {
        return Arrays.asList(softwareModuleDetailsHeader, swModuleDetails);
    }

    private List<EntityModifiedAwareSupport> getEntityModifiedAwareSupports() {
        return Arrays.asList(EntityModifiedGridRefreshAwareSupport.of(swModuleGrid::refreshContainer),
                EntityModifiedSelectionAwareSupport.of(swModuleGrid.getSelectionSupport(),
                        swModuleGrid::mapIdToProxyEntity));
    }

    public void showSmTypeHeaderIcon() {
        swModuleGridHeader.showSmTypeIcon();
    }

    public void hideSmTypeHeaderIcon() {
        swModuleGridHeader.hideSmTypeIcon();
    }

    public void filterGridBySearch(final String searchFilter) {
        swModuleGrid.updateSearchFilter(searchFilter);
        swModuleGrid.deselectAll();
    }

    public void filterGridByType(final SoftwareModuleType typeFilter) {
        swModuleGrid.updateTypeFilter(typeFilter);
        swModuleGrid.deselectAll();
    }

    public void maximize() {
        swModuleGrid.createMaximizedContent();
        hideDetailsLayout();
    }

    public void minimize() {
        swModuleGrid.createMinimizedContent();
        showDetailsLayout();
    }

    public void restoreState() {
        swModuleGridHeader.restoreState();
        swModuleGrid.restoreState();
    }

    public void unsubscribeListener() {
        searchFilterListener.unsubscribe();
        typeFilterListener.unsubscribe();
        masterDsEntityChangedListener.unsubscribe();
        masterSmEntityChangedListener.unsubscribe();
        entityModifiedListener.unsubscribe();
    }
}
