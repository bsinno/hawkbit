/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.mappers;

import org.eclipse.hawkbit.repository.model.AssignedSoftwareModule;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.ui.common.UserDetailsFormatter;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAssignedSoftwareModule;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;

/**
 * Maps {@link AssignedSoftwareModule} entities, fetched from backend, to the
 * {@link ProxyAssignedSoftwareModule} entities.
 */
public class AssignedSoftwareModuleToProxyMapper implements
        IdentifiableEntityToProxyIdentifiableEntityMapper<ProxyAssignedSoftwareModule, AssignedSoftwareModule> {

    @Override
    public ProxyAssignedSoftwareModule map(final AssignedSoftwareModule assignedSwModule) {
        final ProxyAssignedSoftwareModule proxyAssignedSwModule = new ProxyAssignedSoftwareModule();

        final SoftwareModule softwareModule = assignedSwModule.getSoftwareModule();
        proxyAssignedSwModule.setId(softwareModule.getId());
        proxyAssignedSwModule.setSwId(softwareModule.getId());
        final String swNameVersion = HawkbitCommonUtil.concatStrings(":", softwareModule.getName(),
                softwareModule.getVersion());
        proxyAssignedSwModule.setNameAndVersion(swNameVersion);
        proxyAssignedSwModule.setCreatedDate(SPDateTimeUtil.getFormattedDate(softwareModule.getCreatedAt()));
        proxyAssignedSwModule.setModifiedDate(SPDateTimeUtil.getFormattedDate(softwareModule.getLastModifiedAt()));
        proxyAssignedSwModule.setName(softwareModule.getName());
        proxyAssignedSwModule.setVersion(softwareModule.getVersion());
        proxyAssignedSwModule.setVendor(softwareModule.getVendor());
        proxyAssignedSwModule.setDescription(softwareModule.getDescription());
        proxyAssignedSwModule.setCreatedBy(UserDetailsFormatter.loadAndFormatCreatedBy(softwareModule));
        proxyAssignedSwModule.setLastModifiedBy(UserDetailsFormatter.loadAndFormatLastModifiedBy(softwareModule));

        proxyAssignedSwModule.setColour(softwareModule.getType().getColour());
        proxyAssignedSwModule.setTypeId(softwareModule.getType().getId());
        proxyAssignedSwModule.setAssigned(assignedSwModule.isAssigned());

        return proxyAssignedSwModule;
    }
}
