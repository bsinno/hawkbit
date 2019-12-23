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
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractDistributionSetDetails;
import org.eclipse.hawkbit.ui.common.detailslayout.TargetFilterQueryDetailsGrid;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Distribution set details layout.
 */
public class DistributionSetDetails extends AbstractDistributionSetDetails {
    private static final long serialVersionUID = 1L;

    DistributionSetDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final UINotification uiNotification,
            final DistributionSetManagement dsManagement, final SoftwareModuleManagement smManagement,
            final DistributionSetTypeManagement dsTypeManagement, final DistributionSetTagManagement dsTagManagement,
            final TargetFilterQueryManagement targetFilterQueryManagement,
            final TenantConfigurationManagement configManagement, final SystemSecurityContext systemSecurityContext,
            final DsMetaDataWindowBuilder dsMetaDataWindowBuilder) {
        super(i18n, eventBus, permissionChecker, uiNotification, dsManagement, smManagement, dsTypeManagement,
                dsTagManagement, configManagement, systemSecurityContext, dsMetaDataWindowBuilder);

        final TargetFilterQueryDetailsGrid tfqDetailsGrid = new TargetFilterQueryDetailsGrid(i18n,
                targetFilterQueryManagement);

        addDetailsComponents(Collections
                .singletonList(new SimpleEntry<>(i18n.getMessage("caption.auto.assignment.ds"), tfqDetailsGrid)));

        buildDetails();
    }

    @Override
    protected boolean isUnassignSmAllowed() {
        return true;
    }
}
