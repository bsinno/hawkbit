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

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.management.miscs.DeploymentAssignmentWindowController;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

/**
 * Support for assigning targets to distribution set.
 * 
 */
public class TargetsToDistributionSetAssignmentSupport
        extends DeploymentAssignmentSupport<ProxyTarget, ProxyDistributionSet> {

    public TargetsToDistributionSetAssignmentSupport(final UINotification notification, final VaadinMessageSource i18n,
            final DeploymentAssignmentWindowController assignmentController) {
        super(notification, i18n, assignmentController);
    }

    @Override
    protected void performAssignment(final List<ProxyTarget> sourceItemsToAssign,
            final ProxyDistributionSet targetItem) {
        openConfirmationWindowForAssignments(sourceItemsToAssign, targetItem, () -> assignmentController
                .assignTargetsToDistributions(sourceItemsToAssign, Collections.singletonList(targetItem)));
    }

    @Override
    protected String sourceEntityType() {
        return i18n.getMessage("caption.target");
    }

    @Override
    protected String targetEntityType() {
        return i18n.getMessage("distribution.details.header");
    }
}
