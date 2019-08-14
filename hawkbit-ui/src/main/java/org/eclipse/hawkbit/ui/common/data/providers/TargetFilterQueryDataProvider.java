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

import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.data.filters.SearchTextFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetFilterQueryToProxyTargetFilterMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Data provider for {@link TargetFilterQuery}, which dynamically loads a batch
 * of {@link TargetFilterQuery} entities from backend and maps them to
 * corresponding {@link ProxyTargetFilterQuery} entities.
 */
public class TargetFilterQueryDataProvider
        extends ProxyDataProvider<ProxyTargetFilterQuery, TargetFilterQuery, SearchTextFilterParams> {

    private static final long serialVersionUID = 1L;

    private final transient TargetFilterQueryManagement targetFilterQueryManagement;

    public TargetFilterQueryDataProvider(final TargetFilterQueryManagement targetFilterQueryManagement,
            final TargetFilterQueryToProxyTargetFilterMapper entityMapper) {
        super(entityMapper, new Sort(Direction.ASC, "name"));

        this.targetFilterQueryManagement = targetFilterQueryManagement;
    }

    @Override
    protected Optional<Slice<TargetFilterQuery>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<SearchTextFilterParams> filter) {
        if (!filter.isPresent()) {
            return Optional.of(targetFilterQueryManagement.findAll(pageRequest));
        }

        return filter
                .map(filterParams -> targetFilterQueryManagement.findByName(pageRequest, filterParams.getSearchText()));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<SearchTextFilterParams> filter) {

        return filter.map(filterParams -> targetFilterQueryManagement
                .findByName(pageRequest, filterParams.getSearchText()).getTotalElements())
                .orElseGet(targetFilterQueryManagement::count);
    }
}
