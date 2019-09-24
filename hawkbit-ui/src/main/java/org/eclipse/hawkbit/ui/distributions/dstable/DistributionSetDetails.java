/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
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

/**
 * Distribution set details layout.
 */
public class DistributionSetDetails extends AbstractDistributionSetDetails {
    private static final long serialVersionUID = 1L;

    private final ManageDistUIState manageDistUIState;

    private final TargetFilterQueryDetailsGrid tfqDetailsGrid;

    DistributionSetDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final ManageDistUIState manageDistUIState,
            final ManagementUIState managementUIState, final DistributionSetManagement distributionSetManagement,
            final UINotification uiNotification, final DistributionSetTagManagement distributionSetTagManagement,
            final TenantConfigurationManagement configManagement, final SystemSecurityContext systemSecurityContext,
            final DistributionAddUpdateWindowLayout distributionAddUpdateWindowLayout,
            final EntityFactory entityFactory) {
        super(i18n, eventBus, permissionChecker, managementUIState, distributionAddUpdateWindowLayout,
                distributionSetManagement, uiNotification, distributionSetTagManagement, configManagement,
                systemSecurityContext, entityFactory);

        this.manageDistUIState = manageDistUIState;

        tfqDetailsGrid = new TargetFilterQueryDetailsGrid(i18n);

        addDetailsComponents(Collections
                .singletonList(new SimpleEntry<>(i18n.getMessage("caption.auto.assignment.ds"), tfqDetailsGrid)));
        buildDetails();
        restoreState();
    }

    @Override
    protected SoftwareModuleDetailsGrid getSoftwareModuleDetailsGrid() {
        return new SoftwareModuleDetailsGrid(i18n, true, permChecker, distributionSetManagement, eventBus,
                manageDistUIState, uiNotification);
    }

    // TODO: implement
    // protected void populateTargetFilterQueries() {
    // tfqDetailsGrid.populateGrid(getSelectedBaseEntity());
    // }

    private void restoreState() {
        if (manageDistUIState.isDsTableMaximized()) {
            setVisible(false);
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SoftwareModuleEvent event) {
        if (event.getSoftwareModuleEventType() == SoftwareModuleEventType.ASSIGN_SOFTWARE_MODULE) {
            // TODO: check if it works
            distributionSetManagement.getWithDetails(binder.getBean().getId()).ifPresent(set -> {
                binder.setBean(new DistributionSetToProxyDistributionMapper().map(set));
                // TODO: check if this is needed
                // UI.getCurrent().access(this::populateSmDetails);
            });
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SaveActionWindowEvent saveActionWindowEvent) {
        if ((saveActionWindowEvent == SaveActionWindowEvent.SAVED_ASSIGNMENTS
                || saveActionWindowEvent == SaveActionWindowEvent.DISCARD_ALL_ASSIGNMENTS)
                && binder.getBean() != null) {
            // TODO: check if it works
            distributionSetManagement.getWithDetails(binder.getBean().getId()).ifPresent(set -> {
                binder.setBean(new DistributionSetToProxyDistributionMapper().map(set));
                // TODO: check if this is needed
                // UI.getCurrent().access(this::populateSmDetails);
            });
        }
    }
}
