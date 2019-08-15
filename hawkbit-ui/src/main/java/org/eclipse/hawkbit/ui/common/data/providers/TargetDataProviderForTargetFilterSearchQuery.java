/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.providers;

import java.util.Optional;

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.common.data.filters.SearchTextFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Executes a TargetFilterQuery and loads the Targets as result.
 */
public class TargetDataProviderForTargetFilterSearchQuery
        extends ProxyDataProvider<ProxyTarget, Target, SearchTextFilterParams> {

    private static final long serialVersionUID = 1L;

    private transient TargetManagement targetManagement;
    private FilterManagementUIState filterManagementUIState;

    public TargetDataProviderForTargetFilterSearchQuery(final TargetManagement targetManagement,
            final FilterManagementUIState filterManagementUIState, final TargetToProxyTargetMapper entityMapper) {
        super(entityMapper, new Sort(Direction.ASC, "id"));

        this.targetManagement = targetManagement;
    }

    @Override
    protected Optional<Slice<Target>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<SearchTextFilterParams> filter) {
        return filter.map(filterParams -> targetManagement.findByRsql(pageRequest, filterParams.getSearchText()));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<SearchTextFilterParams> filter) {
        long size = 0;

        if (filter.isPresent()) {
            size = targetManagement.countByRsql(filter.get().getSearchText());
        }
        filterManagementUIState.setTargetsCountAll(size);

        if (size > SPUIDefinitions.MAX_TABLE_ENTRIES) {
            filterManagementUIState.setTargetsTruncated(size - SPUIDefinitions.MAX_TABLE_ENTRIES);
            size = SPUIDefinitions.MAX_TABLE_ENTRIES;
        } else {
            filterManagementUIState.setTargetsTruncated(null);
        }
        return (int) size;
    }
}
