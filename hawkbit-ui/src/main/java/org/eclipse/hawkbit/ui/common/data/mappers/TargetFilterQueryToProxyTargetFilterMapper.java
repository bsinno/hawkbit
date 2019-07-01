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
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.UserDetailsFormatter;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilter;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;

/**
 * Maps {@link TargetFilterQuery} entities, fetched from backend, to the
 * {@link ProxyTargetFilter} entities.
 */
public class TargetFilterQueryToProxyTargetFilterMapper
        implements IdentifiableEntityToProxyIdentifiableEntityMapper<ProxyTargetFilter, TargetFilterQuery> {

    @Override
    public ProxyTargetFilter map(final TargetFilterQuery targetFilterQuery) {
        final ProxyTargetFilter proxyTargetFilter = new ProxyTargetFilter();

        proxyTargetFilter.setName(targetFilterQuery.getName());
        proxyTargetFilter.setId(targetFilterQuery.getId());
        proxyTargetFilter.setCreatedDate(SPDateTimeUtil.getFormattedDate(targetFilterQuery.getCreatedAt()));
        proxyTargetFilter.setCreatedBy(UserDetailsFormatter.loadAndFormatCreatedBy(targetFilterQuery));
        proxyTargetFilter.setModifiedDate(SPDateTimeUtil.getFormattedDate(targetFilterQuery.getLastModifiedAt()));
        proxyTargetFilter.setLastModifiedBy(UserDetailsFormatter.loadAndFormatLastModifiedBy(targetFilterQuery));
        proxyTargetFilter.setQuery(targetFilterQuery.getQuery());

        final DistributionSet distributionSet = targetFilterQuery.getAutoAssignDistributionSet();
        if (distributionSet != null) {
            proxyTargetFilter
                    .setAutoAssignDistributionSet(new DistributionSetToProxyDistributionMapper().map(distributionSet));
        }

        return proxyTargetFilter;
    }

}
