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
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.table.AbstractTableLayout;
import org.eclipse.hawkbit.ui.dd.criteria.DistributionsViewClientCriterion;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.eclipse.hawkbit.ui.management.dstable.DistributionAddUpdateWindowLayout;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * DistributionSet table layout.
 */
public class DistributionSetTableLayout extends AbstractTableLayout<ProxyDistributionSet> {

    private static final long serialVersionUID = 1L;

    private final DistributionSetGrid distributionSetGrid;

    public DistributionSetTableLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final ManagementUIState managementUIState,
            final ManageDistUIState manageDistUIState, final SoftwareModuleManagement softwareManagement,
            final DistributionSetManagement distributionSetManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement, final TargetManagement targetManagement,
            final EntityFactory entityFactory, final UINotification uiNotification,
            final DistributionSetTagManagement distributionSetTagManagement,
            final DistributionsViewClientCriterion distributionsViewClientCriterion,
            final SystemManagement systemManagement, final TenantConfigurationManagement configManagement,
            final SystemSecurityContext systemSecurityContext) {

        this.distributionSetGrid = new DistributionSetGrid(eventBus, i18n, permissionChecker, uiNotification,
                manageDistUIState, targetManagement, distributionSetManagement);

        final DistributionAddUpdateWindowLayout distributionAddUpdateWindowLayout = new DistributionAddUpdateWindowLayout(
                i18n, uiNotification, eventBus, distributionSetManagement, distributionSetTypeManagement,
                systemManagement, entityFactory, configManagement, systemSecurityContext);

        final DsMetadataPopupLayout popupLayout = new DsMetadataPopupLayout(i18n, uiNotification, eventBus,
                distributionSetManagement, entityFactory, permissionChecker);

        super.init(i18n,
                new DistributionSetGridHeader(
                        i18n, permissionChecker, eventBus, manageDistUIState, distributionAddUpdateWindowLayout),
                distributionSetGrid,
                new DistributionSetDetails(i18n, eventBus, permissionChecker, manageDistUIState, managementUIState,
                        distributionAddUpdateWindowLayout, distributionSetManagement, uiNotification,
                        distributionSetTagManagement, popupLayout, configManagement, systemSecurityContext));
    }

    public DistributionSetGrid getDistributionSetGrid() {
        return distributionSetGrid;
    }

}
