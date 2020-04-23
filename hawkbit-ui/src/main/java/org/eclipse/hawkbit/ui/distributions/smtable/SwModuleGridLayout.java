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
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtable.SmMetaDataWindowBuilder;
import org.eclipse.hawkbit.ui.artifacts.smtable.SmWindowBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.detailslayout.SoftwareModuleDetailsHeader;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventLayoutViewAware;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.EventViewAware;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.FilterChangedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.SelectionChangedListener;
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

    private final transient FilterChangedListener<ProxySoftwareModule> smFilterListener;
    private final transient SelectionChangedListener<ProxyDistributionSet> masterDsChangedListener;
    private final transient SelectionChangedListener<ProxySoftwareModule> masterSmChangedListener;
    private final transient EntityModifiedListener<ProxySoftwareModule> smModifiedListener;

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

        this.smFilterListener = new FilterChangedListener<>(eventBus, ProxySoftwareModule.class,
                new EventViewAware(EventView.DISTRIBUTIONS), swModuleGrid.getFilterSupport());
        this.masterDsChangedListener = new SelectionChangedListener<>(eventBus,
                new EventLayoutViewAware(EventLayout.DS_LIST, EventView.DISTRIBUTIONS), getMasterDswareComponents());
        this.masterSmChangedListener = new SelectionChangedListener<>(eventBus,
                new EventLayoutViewAware(EventLayout.SM_LIST, EventView.DISTRIBUTIONS), getMasterSmAwareComponents());
        this.smModifiedListener = new EntityModifiedListener.Builder<>(eventBus, ProxySoftwareModule.class)
                .entityModifiedAwareSupports(getSmModifiedAwareSupports()).build();

        buildLayout(swModuleGridHeader, swModuleGrid, softwareModuleDetailsHeader, swModuleDetails);
    }

    private List<MasterEntityAwareComponent<ProxyDistributionSet>> getMasterDswareComponents() {
        return Collections.singletonList(swModuleGrid);
    }

    private List<MasterEntityAwareComponent<ProxySoftwareModule>> getMasterSmAwareComponents() {
        return Arrays.asList(softwareModuleDetailsHeader, swModuleDetails);
    }

    private List<EntityModifiedAwareSupport> getSmModifiedAwareSupports() {
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
        smFilterListener.unsubscribe();
        masterDsChangedListener.unsubscribe();
        masterSmChangedListener.unsubscribe();
        smModifiedListener.unsubscribe();
    }
}
