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

import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.ui.common.data.mappers.RolloutToProxyRolloutMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Data provider for {@link Rollout}, which dynamically loads a batch of
 * {@link Rollout} entities from backend and maps them to corresponding
 * {@link ProxyRollout} entities.
 */
public class RolloutDataProvider extends ProxyDataProvider<ProxyRollout, Rollout, String> {

    private static final long serialVersionUID = 1L;

    private final transient RolloutManagement rolloutManagement;

    /**
     * Constructor
     * 
     * @param rolloutManagement
     *            to get the entities
     * @param entityMapper
     *            entityMapper
     */
    public RolloutDataProvider(final RolloutManagement rolloutManagement,
            final RolloutToProxyRolloutMapper entityMapper) {
        super(entityMapper, new Sort(Direction.DESC, "lastModifiedAt"));

        this.rolloutManagement = rolloutManagement;
    }

    @Override
    protected Optional<Slice<Rollout>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<String> filter) {
        return Optional.of(filter
                .map(searchText -> rolloutManagement.findByFiltersWithDetailedStatus(pageRequest, searchText, false))
                .orElseGet(() -> rolloutManagement.findAllWithDetailedStatus(pageRequest, false)));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<String> filter) {
        return filter.map(rolloutManagement::countByFilters).orElseGet(rolloutManagement::count);

    }

}
