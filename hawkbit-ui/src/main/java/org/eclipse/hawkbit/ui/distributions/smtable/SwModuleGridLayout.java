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
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.MasterEntityChangedListener;
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

    private final transient SwModuleGridLayoutEventListener eventListener;

    private final transient MasterEntityChangedListener<ProxyDistributionSet> masterDsEntitySupport;
    private final transient MasterEntityChangedListener<ProxySoftwareModule> masterEntitySupport;
    private final transient EntityModifiedListener<ProxySoftwareModule> entityModifiedSupport;

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

        this.eventListener = new SwModuleGridLayoutEventListener(this, eventBus);

        this.masterDsEntitySupport = new MasterEntityChangedListener<>(eventBus, Collections.singletonList(swModuleGrid),
                getView(), Layout.DS_LIST);
        this.masterEntitySupport = new MasterEntityChangedListener<>(eventBus, getMasterEntityAwareComponents(), getView(),
                getLayout());
        this.entityModifiedSupport = new EntityModifiedListener<>(eventBus, swModuleGrid::refreshContainer,
                swModuleGrid.getSelectionSupport(), ProxySoftwareModule.class);

        buildLayout(swModuleGridHeader, swModuleGrid, softwareModuleDetailsHeader, swModuleDetails);
    }

    private List<MasterEntityAwareComponent<ProxySoftwareModule>> getMasterEntityAwareComponents() {
        return Arrays.asList(softwareModuleDetailsHeader, swModuleDetails);
    }

    public void restoreState() {
        swModuleGridHeader.restoreState();
        swModuleGrid.restoreState();
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

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();

        masterDsEntitySupport.unsubscribe();
        masterEntitySupport.unsubscribe();
        entityModifiedSupport.unsubscribe();
    }

    public Layout getLayout() {
        return Layout.SM_LIST;
    }

    public View getView() {
        return View.DISTRIBUTIONS;
    }
}
