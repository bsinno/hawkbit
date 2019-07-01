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

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.DistributionSetFilter;
import org.eclipse.hawkbit.repository.model.DistributionSetFilter.DistributionSetFilterBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

/**
 * Data provider for {@link DistributionSet}, which dynamically loads a batch of
 * {@link DistributionSet} entities from backend and maps them to corresponding
 * {@link ProxyDistributionSet} entities.
 */
public class DistributionSetDataProvider extends ProxyDataProvider<ProxyDistributionSet, DistributionSet, String> {

    private static final long serialVersionUID = 1L;

    private transient DistributionSetManagement distributionSetManagement;

    public DistributionSetDataProvider(final DistributionSetManagement distributionSetManagement,
            final RolloutUIState rolloutUIState, final DistributionSetToProxyDistributionMapper entityMapper) {
        super(rolloutUIState, entityMapper);
        this.distributionSetManagement = distributionSetManagement;
    }

    @Override
    protected Optional<Slice<DistributionSet>> loadBeans(final PageRequest pageRequest) {
        final DistributionSetFilter distributionSetFilter = new DistributionSetFilterBuilder().setIsDeleted(false)
                .build();
        return Optional
                .ofNullable(distributionSetManagement.findByDistributionSetFilter(pageRequest, distributionSetFilter));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest) {
        final DistributionSetFilter distributionSetFilter = new DistributionSetFilterBuilder().setIsDeleted(false)
                .setIsComplete(true).build();
        return distributionSetManagement.findByDistributionSetFilter(pageRequest, distributionSetFilter)
                .getTotalElements();
    }

}
