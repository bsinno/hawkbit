/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.mappers;

import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;

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

        proxyDistribution.setVersion(distributionSet.getVersion());
        proxyDistribution.setNameVersion(
                HawkbitCommonUtil.getFormattedNameVersion(distributionSet.getName(), distributionSet.getVersion()));
        proxyDistribution.setIsComplete(distributionSet.isComplete());
        proxyDistribution.setRequiredMigrationStep(distributionSet.isRequiredMigrationStep());
        proxyDistribution.setTypeId(distributionSet.getType().getId());
        // TODO: consider removing or refactor
        proxyDistribution.setProxyType(new TypeToProxyTypeMapper<DistributionSetType>().map(distributionSet.getType()));

        return proxyDistribution;
    }

}
