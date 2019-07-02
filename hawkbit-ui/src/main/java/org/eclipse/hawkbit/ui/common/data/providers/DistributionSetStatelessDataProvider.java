/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.providers;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.DistributionSetFilter;
import org.eclipse.hawkbit.repository.model.DistributionSetFilter.DistributionSetFilterBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Data provider for {@link DistributionSet}, which dynamically loads a batch of
 * {@link DistributionSet} entities from backend and maps them to corresponding
 * {@link ProxyDistributionSet} entities.
 */
public class DistributionSetStatelessDataProvider
        extends ProxyDataProvider<ProxyDistributionSet, DistributionSet, String> {

    private static final long serialVersionUID = 1L;

    private final transient DistributionSetManagement distributionSetManagement;

    public DistributionSetStatelessDataProvider(final DistributionSetManagement distributionSetManagement,
            final DistributionSetToProxyDistributionMapper entityMapper) {
        super(entityMapper, new Sort(Direction.ASC, "name", "version"));
        this.distributionSetManagement = distributionSetManagement;
    }

    @Override
    protected Optional<Slice<DistributionSet>> loadBackendEntities(final PageRequest pageRequest, final String filter) {
        return Optional.ofNullable(
                distributionSetManagement.findByDistributionSetFilter(pageRequest, getDistributionSetFilter(filter)));
    }

    private DistributionSetFilter getDistributionSetFilter(final String filter) {
        final DistributionSetFilterBuilder distributionSetFilterBuilder = new DistributionSetFilterBuilder()
                .setIsDeleted(false).setIsComplete(true);
        return StringUtils.isEmpty(filter) ? distributionSetFilterBuilder.build()
                : distributionSetFilterBuilder.setFilterString(filter).build();
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        return distributionSetManagement.findByDistributionSetFilter(pageRequest, getDistributionSetFilter(filter))
                .getTotalElements();
    }
}
