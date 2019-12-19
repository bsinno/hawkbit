/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstable;

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
import org.eclipse.hawkbit.ui.common.detailslayout.DistributionSetDetailsHeader;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.distributions.dstable.DsMetaDataWindowBuilder;
import org.eclipse.hawkbit.ui.distributions.dstable.DsWindowBuilder;
import org.eclipse.hawkbit.ui.management.ds.DistributionGrid;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
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

    public DistributionGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final EntityFactory entityFactory,
            final UINotification notification, final ManagementUIState managementUIState,
            final TargetManagement targetManagement, final DistributionSetManagement distributionSetManagement,
            final SoftwareModuleManagement smManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement,
            final DistributionSetTagManagement distributionSetTagManagement, final SystemManagement systemManagement,
            final DeploymentManagement deploymentManagement, final TenantConfigurationManagement configManagement,
            final SystemSecurityContext systemSecurityContext, final UiProperties uiProperties) {
        this.distributionGridHeader = new DistributionGridHeader(i18n, permissionChecker, eventBus, managementUIState);
        this.distributionGrid = new DistributionGrid(eventBus, i18n, permissionChecker, notification, managementUIState,
                targetManagement, distributionSetManagement, deploymentManagement, uiProperties);

        final DsWindowBuilder dsWindowBuilder = new DsWindowBuilder(i18n, entityFactory, eventBus, notification,
                systemManagement, systemSecurityContext, configManagement, distributionSetManagement,
                distributionSetTypeManagement);
        final DsMetaDataWindowBuilder dsMetaDataWindowBuilder = new DsMetaDataWindowBuilder(i18n, entityFactory,
                eventBus, notification, permissionChecker, distributionSetManagement);

        this.distributionSetDetailsHeader = new DistributionSetDetailsHeader(i18n, permissionChecker, eventBus,
                notification, dsWindowBuilder, dsMetaDataWindowBuilder);
        this.distributionDetails = new DistributionDetails(i18n, eventBus, permissionChecker, notification,
                distributionSetManagement, smManagement, distributionSetTypeManagement, distributionSetTagManagement,
                configManagement, systemSecurityContext, dsMetaDataWindowBuilder);

        buildLayout(distributionGridHeader, distributionGrid, distributionSetDetailsHeader, distributionDetails);
    }

    public DistributionGrid getDistributionGrid() {
        return distributionGrid;
    }

}
