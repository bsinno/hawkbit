/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.mappers;

import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;

/**
 * Maps {@link RolloutGroup} entities, fetched from backend, to the
 * {@link ProxyRolloutGroup} entities.
 */
public class RolloutGroupToProxyRolloutGroupMapper
        extends AbstractNamedEntityToProxyNamedEntityMapper<ProxyRolloutGroup, RolloutGroup> {

    @Override
    public ProxyRolloutGroup map(final RolloutGroup rolloutGroup) {
        final ProxyRolloutGroup proxyRolloutGroup = new ProxyRolloutGroup();

        mapNamedEntityAttributes(rolloutGroup, proxyRolloutGroup);

        proxyRolloutGroup.setStatus(rolloutGroup.getStatus());
        proxyRolloutGroup.setErrorAction(rolloutGroup.getErrorAction());
        proxyRolloutGroup.setErrorActionExp(rolloutGroup.getErrorActionExp());
        proxyRolloutGroup.setErrorCondition(rolloutGroup.getErrorCondition());
        proxyRolloutGroup.setErrorConditionExp(rolloutGroup.getErrorConditionExp());
        proxyRolloutGroup.setSuccessCondition(rolloutGroup.getSuccessCondition());
        proxyRolloutGroup.setSuccessConditionExp(rolloutGroup.getSuccessConditionExp());
        proxyRolloutGroup.setFinishedPercentage(HawkbitCommonUtil.formattingFinishedPercentage(rolloutGroup,
                rolloutGroup.getTotalTargetCountStatus().getFinishedPercent()));
        proxyRolloutGroup.setTotalTargetsCount(String.valueOf(rolloutGroup.getTotalTargets()));
        proxyRolloutGroup.setTotalTargetCountStatus(rolloutGroup.getTotalTargetCountStatus());

        return proxyRolloutGroup;
    }

}
