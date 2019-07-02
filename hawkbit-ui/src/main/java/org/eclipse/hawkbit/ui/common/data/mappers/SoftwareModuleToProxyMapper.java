/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.mappers;

import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;

/**
 * Maps {@link SoftwareModule} entities, fetched from backend, to the
 * {@link ProxySoftwareModule} entities.
 */
public class SoftwareModuleToProxyMapper
        extends AbstractNamedEntityToProxyNamedEntityMapper<ProxySoftwareModule, SoftwareModule> {

    @Override
    public ProxySoftwareModule map(final SoftwareModule softwareModule) {
        final ProxySoftwareModule proxySoftwareModule = new ProxySoftwareModule();

        mapNamedEntityAttributes(softwareModule, proxySoftwareModule);

        proxySoftwareModule.setSwId(softwareModule.getId());
        final String swNameVersion = HawkbitCommonUtil.concatStrings(":", softwareModule.getName(),
                softwareModule.getVersion());
        proxySoftwareModule.setNameAndVersion(swNameVersion);
        proxySoftwareModule.setVendor(softwareModule.getVendor());

        return proxySoftwareModule;
    }
}
