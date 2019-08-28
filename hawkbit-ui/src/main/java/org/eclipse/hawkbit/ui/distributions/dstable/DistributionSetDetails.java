/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent.SoftwareModuleEventType;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractDistributionSetDetails;
import org.eclipse.hawkbit.ui.common.detailslayout.SoftwareModuleDetailsGrid;
import org.eclipse.hawkbit.ui.common.detailslayout.TargetFilterQueryDetailsGrid;
import org.eclipse.hawkbit.ui.distributions.event.SaveActionWindowEvent;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.eclipse.hawkbit.ui.management.dstable.DistributionAddUpdateWindowLayout;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

/**
 * Distribution set details layout.
 */
public class DistributionSetDetails extends AbstractDistributionSetDetails {

    private static final long serialVersionUID = 1L;

    private final ManageDistUIState manageDistUIState;

    private final TargetFilterQueryDetailsGrid tfqDetailsGrid;

    DistributionSetDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final ManageDistUIState manageDistUIState,
            final ManagementUIState managementUIState,
            final DistributionAddUpdateWindowLayout distributionAddUpdateWindowLayout,
            final DistributionSetManagement distributionSetManagement, final UINotification uiNotification,
            final DistributionSetTagManagement distributionSetTagManagement,
            final DsMetadataPopupLayout dsMetadataPopupLayout, final TenantConfigurationManagement configManagement,
            final SystemSecurityContext systemSecurityContext) {
        super(i18n, eventBus, permissionChecker, managementUIState, distributionAddUpdateWindowLayout,
                distributionSetManagement, dsMetadataPopupLayout, uiNotification, distributionSetTagManagement,
                createSoftwareModuleDetailsGrid(i18n, permissionChecker, distributionSetManagement, eventBus,
                        manageDistUIState, uiNotification),
                configManagement, systemSecurityContext);
        this.manageDistUIState = manageDistUIState;

        tfqDetailsGrid = new TargetFilterQueryDetailsGrid(i18n);

        addAdditionalTab();
        restoreState();
    }

    private void addAdditionalTab() {
        getDetailsTab().addTab(tfqDetailsGrid, getI18n().getMessage("caption.auto.assignment.ds"), null);
    }

    private static final SoftwareModuleDetailsGrid createSoftwareModuleDetailsGrid(final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final DistributionSetManagement distributionSetManagement,
            final UIEventBus eventBus, final ManageDistUIState manageDistUIState, final UINotification uiNotification) {
        return new SoftwareModuleDetailsGrid(i18n, true, permissionChecker, distributionSetManagement, eventBus,
                manageDistUIState, uiNotification);
    }

    @Override
    protected void populateDetailsWidget() {
        populateDetails();
        populateModule();
        populateTags(getDistributionTagToken());
        populateMetadataDetails();
        populateTargetFilterQueries();
    }

    protected void populateTargetFilterQueries() {
        tfqDetailsGrid.populateTableByDistributionSet(getSelectedBaseEntity());
    }

    @Override
    protected boolean onLoadIsTableMaximized() {
        return manageDistUIState.isDsTableMaximized();
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SoftwareModuleEvent event) {
        if (event.getSoftwareModuleEventType() == SoftwareModuleEventType.ASSIGN_SOFTWARE_MODULE) {
            // TODO: check if it works
            getDistributionSetManagement().getWithDetails(getSelectedBaseEntityId()).ifPresent(set -> {
                setSelectedBaseEntity(new DistributionSetToProxyDistributionMapper().map(set));
                UI.getCurrent().access(this::populateModule);
            });
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SaveActionWindowEvent saveActionWindowEvent) {
        if ((saveActionWindowEvent == SaveActionWindowEvent.SAVED_ASSIGNMENTS
                || saveActionWindowEvent == SaveActionWindowEvent.DISCARD_ALL_ASSIGNMENTS)
                && getSelectedBaseEntity() != null) {
            getDistributionSetManagement().getWithDetails(getSelectedBaseEntityId()).ifPresent(set -> {
                setSelectedBaseEntity(new DistributionSetToProxyDistributionMapper().map(set));
                UI.getCurrent().access(this::populateModule);
            });
        }
    }
}
