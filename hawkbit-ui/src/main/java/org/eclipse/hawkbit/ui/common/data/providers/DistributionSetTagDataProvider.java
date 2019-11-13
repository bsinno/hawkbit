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

import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetTag;
import org.eclipse.hawkbit.ui.common.data.mappers.TagToProxyTagMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Data provider for {@link DistributionSetTag}, which dynamically loads a batch
 * of {@link DistributionSetTag} entities from backend and maps them to
 * corresponding {@link ProxyTag} entities.
 */
public class DistributionSetTagDataProvider extends ProxyDataProvider<ProxyTag, DistributionSetTag, Void> {

    private static final long serialVersionUID = 1L;

    private final transient DistributionSetTagManagement distributionSetTagManagement;

    public DistributionSetTagDataProvider(final DistributionSetTagManagement distributionSetTagManagement,
            final TagToProxyTagMapper<DistributionSetTag> mapper) {
        super(mapper, new Sort(Direction.ASC, "name"));
        this.distributionSetTagManagement = distributionSetTagManagement;
    }

    @Override
    protected Optional<Slice<DistributionSetTag>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<Void> filter) {
        return Optional.of(distributionSetTagManagement.findAll(pageRequest));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<Void> filter) {
        return distributionSetTagManagement.count();
    }

}