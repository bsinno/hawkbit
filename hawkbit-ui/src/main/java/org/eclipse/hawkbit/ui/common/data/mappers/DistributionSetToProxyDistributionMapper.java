/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.mappers;

import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.springframework.util.CollectionUtils;

/**
 * Maps {@link DistributionSet} entities, fetched from backend, to the
 * {@link ProxyDistributionSet} entities.
 */
public class DistributionSetToProxyDistributionMapper
        extends AbstractNamedEntityToProxyNamedEntityMapper<ProxyDistributionSet, DistributionSet> {

    @Override
    public ProxyDistributionSet map(final DistributionSet distributionSet) {
        final ProxyDistributionSet proxyDistribution = new ProxyDistributionSet();

        mapNamedEntityAttributes(distributionSet, proxyDistribution);

        proxyDistribution.setDistId(distributionSet.getId());
        proxyDistribution.setVersion(distributionSet.getVersion());
        proxyDistribution.setNameVersion(
                HawkbitCommonUtil.getFormattedNameVersion(distributionSet.getName(), distributionSet.getVersion()));
        proxyDistribution.setIsComplete(distributionSet.isComplete());
        proxyDistribution.setType(distributionSet.getType());
        proxyDistribution.setRequiredMigrationStep(distributionSet.isRequiredMigrationStep());

        // TODO: check if really needed
        if (!CollectionUtils.isEmpty(distributionSet.getModules())) {
            proxyDistribution.setModules(distributionSet.getModules().stream()
                    .map(sm -> new SoftwareModuleToProxyMapper().map(sm)).collect(Collectors.toSet()));
        }

        // TODO: check if really needed
        if (!CollectionUtils.isEmpty(distributionSet.getAutoAssignFilters())) {
            proxyDistribution.setAutoAssignFilters(distributionSet.getAutoAssignFilters().stream()
                    .map(tf -> new TargetFilterQueryToProxyTargetFilterMapper().map(tf)).collect(Collectors.toList()));
        }

        return proxyDistribution;
    }

}
