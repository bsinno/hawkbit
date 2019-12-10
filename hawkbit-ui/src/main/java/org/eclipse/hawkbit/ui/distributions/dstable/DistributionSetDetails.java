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
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractDistributionSetDetails;
import org.eclipse.hawkbit.ui.common.detailslayout.SoftwareModuleDetailsGrid;
import org.eclipse.hawkbit.ui.common.detailslayout.TargetFilterQueryDetailsGrid;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Distribution set details layout.
 */
public class DistributionSetDetails extends AbstractDistributionSetDetails {
    private static final long serialVersionUID = 1L;

    private final transient UIEventBus eventBus;
    private final SpPermissionChecker permissionChecker;

    private final DistributionSetGridLayoutUiState distributionSetGridLayoutUiState;

    private final TargetFilterQueryDetailsGrid tfqDetailsGrid;

    DistributionSetDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final DistributionSetManagement distributionSetManagement,
            final UINotification uiNotification, final DistributionSetTagManagement distributionSetTagManagement,
            final TargetFilterQueryManagement targetFilterQueryManagement,
            final TenantConfigurationManagement configManagement, final SystemSecurityContext systemSecurityContext,
            final DistributionSetGridLayoutUiState distributionSetGridLayoutUiState,
            final DsMetaDataWindowBuilder dsMetaDataWindowBuilder) {
        super(i18n, eventBus, permissionChecker, distributionSetManagement, uiNotification,
                distributionSetTagManagement, configManagement, systemSecurityContext, dsMetaDataWindowBuilder);

        this.eventBus = eventBus;
        this.permissionChecker = permissionChecker;

        this.distributionSetGridLayoutUiState = distributionSetGridLayoutUiState;

        tfqDetailsGrid = new TargetFilterQueryDetailsGrid(i18n, targetFilterQueryManagement);

        addDetailsComponents(Collections
                .singletonList(new SimpleEntry<>(i18n.getMessage("caption.auto.assignment.ds"), tfqDetailsGrid)));

        buildDetails();
    }

    @Override
    protected SoftwareModuleDetailsGrid getSoftwareModuleDetailsGrid() {
        return new SoftwareModuleDetailsGrid(i18n, true, permissionChecker, distributionSetManagement, eventBus,
                uiNotification);
    }
}
