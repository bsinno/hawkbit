/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.mappers;

import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

/**
 * Maps {@link Target} entities, fetched from backend, to the
 * {@link ProxyTarget} entities.
 */
public class TargetToProxyTargetMapper extends AbstractNamedEntityToProxyNamedEntityMapper<ProxyTarget, Target> {

    private final transient VaadinMessageSource i18n;

    public TargetToProxyTargetMapper(final VaadinMessageSource i18n) {
        this.i18n = i18n;
    }

    @Override
    public ProxyTarget map(final Target target) {
        final ProxyTarget proxyTarget = new ProxyTarget();

        mapNamedEntityAttributes(target, proxyTarget);

        proxyTarget.setControllerId(target.getControllerId());
        proxyTarget.setInstallationDate(target.getInstallationDate());
        proxyTarget.setAddress(target.getAddress());
        proxyTarget.setLastTargetQuery(target.getLastTargetQuery());
        proxyTarget.setUpdateStatus(target.getUpdateStatus());
        proxyTarget.setPollStatusToolTip(HawkbitCommonUtil.getPollStatusToolTip(target.getPollStatus(), i18n));
        proxyTarget.setSecurityToken(target.getSecurityToken());

        return proxyTarget;
    }
}