/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtable;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtable.SmMetaDataWindowBuilder;
import org.eclipse.hawkbit.ui.artifacts.smtable.SmWindowBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.SoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.detailslayout.SoftwareModuleDetailsHeader;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.distributions.smtype.filter.DistSMTypeFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Implementation of software module Layout on the Distribution View
 */
public class SwModuleGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final transient SoftwareModuleManagement softwareModuleManagement;
    private final transient SoftwareModuleToProxyMapper softwareModuleToProxyMapper;

    private final SwModuleGridHeader swModuleGridHeader;
    private final SwModuleGrid swModuleGrid;
    // TODO: change to SwModuleDetailsHeader
    private final SoftwareModuleDetailsHeader softwareModuleDetailsHeader;
    private final SwModuleDetails swModuleDetails;

    private final SwModuleGridLayoutUiState swModuleGridLayoutUiState;

    private final transient SwModuleGridLayoutEventListener eventListener;

    public SwModuleGridLayout(final VaadinMessageSource i18n, final UINotification uiNotification,
            final UIEventBus eventBus, final SoftwareModuleManagement softwareModuleManagement,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement, final EntityFactory entityFactory,
            final SpPermissionChecker permChecker, final ArtifactManagement artifactManagement,
            final DistSMTypeFilterLayoutUiState distSMTypeFilterLayoutUiState,
            final SwModuleGridLayoutUiState swModuleGridLayoutUiState) {
        this.softwareModuleManagement = softwareModuleManagement;
        this.softwareModuleToProxyMapper = new SoftwareModuleToProxyMapper();
        this.swModuleGridLayoutUiState = swModuleGridLayoutUiState;

        final SmWindowBuilder smWindowBuilder = new SmWindowBuilder(i18n, entityFactory, eventBus, uiNotification,
                softwareModuleManagement, softwareModuleTypeManagement);
        final SmMetaDataWindowBuilder smMetaDataWindowBuilder = new SmMetaDataWindowBuilder(i18n, entityFactory,
                eventBus, uiNotification, permChecker, softwareModuleManagement);

        this.swModuleGridHeader = new SwModuleGridHeader(i18n, permChecker, eventBus, smWindowBuilder,
                distSMTypeFilterLayoutUiState, swModuleGridLayoutUiState);
        this.swModuleGrid = new SwModuleGrid(eventBus, i18n, permChecker, uiNotification, softwareModuleManagement,
                distSMTypeFilterLayoutUiState, swModuleGridLayoutUiState, softwareModuleToProxyMapper);

        this.softwareModuleDetailsHeader = new SoftwareModuleDetailsHeader(i18n, permChecker, eventBus, uiNotification,
                smWindowBuilder, smMetaDataWindowBuilder, artifactManagement);
        this.swModuleDetails = new SwModuleDetails(i18n, eventBus, softwareModuleManagement, smMetaDataWindowBuilder);

        this.eventListener = new SwModuleGridLayoutEventListener(this, eventBus);

        buildLayout(swModuleGridHeader, swModuleGrid, softwareModuleDetailsHeader, swModuleDetails);
    }

    public void restoreState() {
        swModuleGridHeader.restoreState();
        swModuleGrid.restoreState();

        restoreGridSelection();
    }

    private void restoreGridSelection() {
        if (!swModuleGrid.hasSelectionSupport()) {
            return;
        }

        final Long lastSelectedEntityId = swModuleGridLayoutUiState.getSelectedSmId();

        if (lastSelectedEntityId != null) {
            selectEntityById(lastSelectedEntityId);
        } else {
            swModuleGrid.getSelectionSupport().selectFirstRow();
        }
    }

    // TODO: extract to parent abstract #selectEntityById?
    public void selectEntityById(final Long entityId) {
        if (!swModuleGrid.hasSelectionSupport()) {
            return;
        }

        if (!swModuleGrid.getSelectedItems().isEmpty()) {
            swModuleGrid.deselectAll();
        }

        mapIdToProxyEntity(entityId).ifPresent(swModuleGrid::select);
    }

    // TODO: extract to parent abstract #mapIdToProxyEntity?
    private Optional<ProxySoftwareModule> mapIdToProxyEntity(final Long entityId) {
        return softwareModuleManagement.get(entityId).map(softwareModuleToProxyMapper::map);
    }

    // TODO: extract to parent #onMasterEntityChanged?
    public void onSmChanged(final ProxySoftwareModule sm) {
        softwareModuleDetailsHeader.masterEntityChanged(sm);
        swModuleDetails.masterEntityChanged(sm);
    }

    // TODO: extract to parent #onMasterEntityUpdated?
    public void onSmUpdated(final Collection<Long> entityIds) {
        if (!swModuleGrid.hasSelectionSupport()) {
            return;
        }

        if (swModuleGrid.getSelectedItems().size() == 1) {
            final Long selectedEntityId = swModuleGrid.getSelectedItems().iterator().next().getId();

            entityIds.stream().filter(entityId -> entityId.equals(selectedEntityId)).findAny()
                    .ifPresent(updatedEntityId -> mapIdToProxyEntity(updatedEntityId)
                            .ifPresent(updatedEntity -> swModuleGrid.getSelectionSupport().sendSelectionChangedEvent(
                                    SelectionChangedEventType.ENTITY_SELECTED, updatedEntity)));
        }
    }

    public void onDsChanged(final ProxyDistributionSet selectedDs) {
        swModuleGrid.updateMasterEntityFilter(selectedDs != null ? selectedDs.getId() : null);
        swModuleGrid.deselectAll();
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

    public void refreshGrid() {
        swModuleGrid.refreshContainer();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }

    public Layout getLayout() {
        return Layout.SM_LIST;
    }
}
