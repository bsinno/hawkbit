/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.detailslayout.DistributionSetDetailsHeader;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedPinAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedSelectionAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.MasterEntityChangedListener;
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

    private final DistributionGridHeader distributionGridHeader;
    private final DistributionGrid distributionGrid;
    private final DistributionSetDetailsHeader distributionSetDetailsHeader;
    private final DistributionDetails distributionDetails;

    private final transient DistributionGridLayoutEventListener eventListener;

    private final transient MasterEntityChangedListener<ProxyDistributionSet> masterEntityChangedListener;
    private final transient EntityModifiedListener<ProxyDistributionSet> entityModifiedListener;

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

        this.masterEntityChangedListener = new MasterEntityChangedListener<>(eventBus, getMasterEntityAwareComponents(),
                getView(), getLayout());

        this.entityModifiedListener = new EntityModifiedListener<>(eventBus, distributionGrid::refreshContainer,
                getEntityModifiedAwareSupports(), ProxyDistributionSet.class);

        buildLayout(distributionGridHeader, distributionGrid, distributionSetDetailsHeader, distributionDetails);
    }

    public void restoreState() {
        distributionGridHeader.restoreState();
        distributionGrid.restoreState();
    }

    private List<MasterEntityAwareComponent<ProxyDistributionSet>> getMasterEntityAwareComponents() {
        return Arrays.asList(distributionSetDetailsHeader, distributionDetails);
    }

    private List<EntityModifiedAwareSupport> getEntityModifiedAwareSupports() {
        return Arrays.asList(
                EntityModifiedSelectionAwareSupport.of(distributionGrid.getSelectionSupport(),
                        distributionGrid::mapIdToProxyEntity, this::isIncomplete),
                EntityModifiedPinAwareSupport.of(distributionGrid.getPinSupport(), distributionGrid::mapIdToProxyEntity,
                        this::isIncomplete));
    }

    private boolean isIncomplete(final ProxyDistributionSet ds) {
        return ds != null && !ds.getIsComplete();
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

        masterEntityChangedListener.unsubscribe();
        entityModifiedListener.unsubscribe();
    }

    public Layout getLayout() {
        return Layout.DS_LIST;
    }

    public View getView() {
        return View.DEPLOYMENT;
    }
}
