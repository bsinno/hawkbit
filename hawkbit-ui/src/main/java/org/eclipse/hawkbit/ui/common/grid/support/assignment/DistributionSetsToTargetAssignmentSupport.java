/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support.assignment;

import java.util.Collections;
import java.util.List;

import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.management.miscs.DeploymentAssignmentWindowController;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

/**
 * Support for assigning the distribution sets to target.
 * 
 */
public class DistributionSetsToTargetAssignmentSupport
        extends DeploymentAssignmentSupport<ProxyDistributionSet, ProxyTarget> {
    private final SystemSecurityContext systemSecurityContext;
    private final TenantConfigurationManagement configManagement;

    public DistributionSetsToTargetAssignmentSupport(final UINotification notification, final VaadinMessageSource i18n,
            final DeploymentAssignmentWindowController assignmentController,
            final SystemSecurityContext systemSecurityContext, final TenantConfigurationManagement configManagement) {
        super(notification, i18n, assignmentController);

        this.systemSecurityContext = systemSecurityContext;
        this.configManagement = configManagement;
    }

    @Override
    protected List<ProxyDistributionSet> getFilteredSourceItems(final List<ProxyDistributionSet> sourceItemsToAssign) {
        return isMultiAssignmentEnabled() ? sourceItemsToAssign : Collections.singletonList(sourceItemsToAssign.get(0));
    }

    private boolean isMultiAssignmentEnabled() {
        return systemSecurityContext.runAsSystem(() -> configManagement
                .getConfigurationValue(TenantConfigurationKey.MULTI_ASSIGNMENTS_ENABLED, Boolean.class).getValue());
    }

    @Override
    protected void performAssignment(final List<ProxyDistributionSet> sourceItemsToAssign,
            final ProxyTarget targetItem) {
        openConfirmationWindowForAssignments(sourceItemsToAssign, targetItem, () -> assignmentController
                .assignTargetsToDistributions(Collections.singletonList(targetItem), sourceItemsToAssign));
    }

    @Override
    protected String sourceEntityType() {
        return i18n.getMessage("distribution.details.header");
    }

    @Override
    protected String targetEntityType() {
        return i18n.getMessage("caption.target");
    }
}
