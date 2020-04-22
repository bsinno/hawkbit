/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import java.util.Arrays;
import java.util.List;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.ArtifactUploadState;
import org.eclipse.hawkbit.ui.artifacts.smtype.filter.SMTypeFilterLayoutUiState;
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
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Software module table layout. (Upload Management)
 */
public class SoftwareModuleGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final SoftwareModuleGridHeader softwareModuleGridHeader;
    private final SoftwareModuleGrid softwareModuleGrid;
    private final SoftwareModuleDetailsHeader softwareModuleDetailsHeader;
    private final SoftwareModuleDetails softwareModuleDetails;

    private final transient SearchFilterListener searchFilterListener;
    private final transient TypeFilterListener<SoftwareModuleType> typeFilterListener;
    private final transient SelectionChangedListener<ProxySoftwareModule> masterEntityChangedListener;
    private final transient EntityModifiedListener<ProxySoftwareModule> entityModifiedListener;

    public SoftwareModuleGridLayout(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UINotification uiNotification, final UIEventBus eventBus,
            final SoftwareModuleManagement softwareModuleManagement,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement, final EntityFactory entityFactory,
            final ArtifactUploadState artifactUploadState, final SMTypeFilterLayoutUiState smTypeFilterLayoutUiState,
            final SoftwareModuleGridLayoutUiState smGridLayoutUiState) {
        super();

        final SmWindowBuilder smWindowBuilder = new SmWindowBuilder(i18n, entityFactory, eventBus, uiNotification,
                softwareModuleManagement, softwareModuleTypeManagement);
        final SmMetaDataWindowBuilder smMetaDataWindowBuilder = new SmMetaDataWindowBuilder(i18n, entityFactory,
                eventBus, uiNotification, permChecker, softwareModuleManagement);

        this.softwareModuleGridHeader = new SoftwareModuleGridHeader(i18n, permChecker, eventBus,
                smTypeFilterLayoutUiState, smGridLayoutUiState, smWindowBuilder);
        this.softwareModuleGrid = new SoftwareModuleGrid(eventBus, i18n, permChecker, uiNotification,
                artifactUploadState, smTypeFilterLayoutUiState, smGridLayoutUiState, softwareModuleManagement);

        this.softwareModuleDetailsHeader = new SoftwareModuleDetailsHeader(i18n, permChecker, eventBus, uiNotification,
                smWindowBuilder, smMetaDataWindowBuilder);
        this.softwareModuleDetails = new SoftwareModuleDetails(i18n, eventBus, softwareModuleManagement,
                smMetaDataWindowBuilder);

        final EventLayoutViewAware layoutView = new EventLayoutViewAware(EventLayout.SM_LIST, EventView.UPLOAD);
        final EventLayoutViewAware typeLayoutView = new EventLayoutViewAware(EventLayout.SM_TYPE_FILTER, EventView.UPLOAD);

        this.searchFilterListener = new SearchFilterListener(eventBus, layoutView, this::filterGridBySearch);
        this.typeFilterListener = new TypeFilterListener<>(eventBus, typeLayoutView, this::filterGridByType);
        this.masterEntityChangedListener = new SelectionChangedListener<>(eventBus, layoutView,
                getMasterEntityAwareComponents());
        this.entityModifiedListener = new EntityModifiedListener.Builder<>(eventBus, ProxySoftwareModule.class)
                .entityModifiedAwareSupports(getEntityModifiedAwareSupports()).build();

        buildLayout(softwareModuleGridHeader, softwareModuleGrid, softwareModuleDetailsHeader, softwareModuleDetails);
    }

    private List<MasterEntityAwareComponent<ProxySoftwareModule>> getMasterEntityAwareComponents() {
        return Arrays.asList(softwareModuleDetailsHeader, softwareModuleDetails);
    }

    private List<EntityModifiedAwareSupport> getEntityModifiedAwareSupports() {
        return Arrays.asList(EntityModifiedGridRefreshAwareSupport.of(softwareModuleGrid::refreshContainer),
                EntityModifiedSelectionAwareSupport.of(softwareModuleGrid.getSelectionSupport(),
                        softwareModuleGrid::mapIdToProxyEntity));
    }

    public void showSmTypeHeaderIcon() {
        softwareModuleGridHeader.showSmTypeIcon();
    }

    public void hideSmTypeHeaderIcon() {
        softwareModuleGridHeader.hideSmTypeIcon();
    }

    public void filterGridBySearch(final String searchFilter) {
        softwareModuleGrid.updateSearchFilter(searchFilter);
        softwareModuleGrid.deselectAll();
    }

    public void filterGridByType(final SoftwareModuleType typeFilter) {
        softwareModuleGrid.updateTypeFilter(typeFilter);
        softwareModuleGrid.deselectAll();
    }

    public void maximize() {
        softwareModuleGrid.createMaximizedContent();
        hideDetailsLayout();
    }

    public void minimize() {
        softwareModuleGrid.createMinimizedContent();
        showDetailsLayout();
    }

    public void restoreState() {
        softwareModuleGridHeader.restoreState();
        softwareModuleGrid.restoreState();
    }

    public void unsubscribeListener() {
        searchFilterListener.unsubscribe();
        typeFilterListener.unsubscribe();
        masterEntityChangedListener.unsubscribe();
        entityModifiedListener.unsubscribe();
    }
}
