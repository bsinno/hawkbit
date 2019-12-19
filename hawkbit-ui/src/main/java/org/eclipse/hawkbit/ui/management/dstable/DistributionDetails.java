/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstable;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractDistributionSetDetails;
import org.eclipse.hawkbit.ui.common.detailslayout.SoftwareModuleDetailsGrid;
import org.eclipse.hawkbit.ui.distributions.dstable.DsMetaDataWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Distribution set details layout.
 */
public class DistributionDetails extends AbstractDistributionSetDetails {
    private static final long serialVersionUID = 1L;

    private final transient SoftwareModuleManagement smManagement;
    private final transient DistributionSetTypeManagement dsTypeManagement;

    DistributionDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final UINotification uiNotification,
            final DistributionSetManagement distributionSetManagement, final SoftwareModuleManagement smManagement,
            final DistributionSetTypeManagement dsTypeManagement,
            final DistributionSetTagManagement distributionSetTagManagement,
            final TenantConfigurationManagement tenantConfigurationManagement,
            final SystemSecurityContext systemSecurityContext, final DsMetaDataWindowBuilder dsMetaDataWindowBuilder) {
        super(i18n, eventBus, permissionChecker, distributionSetManagement, uiNotification,
                distributionSetTagManagement, tenantConfigurationManagement, systemSecurityContext,
                dsMetaDataWindowBuilder);

        this.smManagement = smManagement;
        this.dsTypeManagement = dsTypeManagement;

        buildDetails();
    }

    @Override
    protected SoftwareModuleDetailsGrid getSoftwareModuleDetailsGrid() {
        return new SoftwareModuleDetailsGrid(i18n, eventBus, uiNotification, permissionChecker,
                distributionSetManagement, smManagement, dsTypeManagement, false);
    }
}
