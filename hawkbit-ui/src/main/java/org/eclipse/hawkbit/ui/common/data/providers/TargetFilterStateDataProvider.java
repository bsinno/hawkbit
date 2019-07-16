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

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.util.StringUtils;

/**
 * Data provider for {@link Target}, which dynamically loads a batch of
 * {@link Target} entities from backend and maps them to corresponding
 * {@link ProxyTarget} entities.
 */
public class TargetFilterStateDataProvider extends ProxyDataProvider<ProxyTarget, Target, Void> {

    private static final long serialVersionUID = 1L;

    private final transient TargetManagement targetManagement;
    private final FilterManagementUIState filterManagementUIState;

    public TargetFilterStateDataProvider(final TargetManagement targetManagement,
            final FilterManagementUIState filterManagementUIState, final TargetToProxyTargetMapper entityMapper) {
        super(entityMapper);

        this.targetManagement = targetManagement;
        this.filterManagementUIState = filterManagementUIState;
    }

    // TODO: use filter instead of uiState
    @Override
    protected Optional<Slice<Target>> loadBackendEntities(final PageRequest pageRequest, final Optional<Void> filter) {
        final String filterQuery = getFilterQueryFromUiState();

        if (!StringUtils.isEmpty(filterQuery)) {
            return Optional.of(targetManagement.findByRsql(pageRequest, filterQuery));
        } else {
            return Optional.of(targetManagement.findAll(pageRequest));
        }
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<Void> filter) {
        final String filterQuery = getFilterQueryFromUiState();
        long size = 0;

        if (!StringUtils.isEmpty(filterQuery)) {
            size = targetManagement.countByRsql(filterQuery);
        } else {
            size = targetManagement.count();
        }

        filterManagementUIState.setTargetsCountAll(size);
        if (size > SPUIDefinitions.MAX_TABLE_ENTRIES) {
            filterManagementUIState.setTargetsTruncated(size - SPUIDefinitions.MAX_TABLE_ENTRIES);
            size = SPUIDefinitions.MAX_TABLE_ENTRIES;
        } else {
            filterManagementUIState.setTargetsTruncated(null);
        }

        return size;
    }

    private String getFilterQueryFromUiState() {
        return filterManagementUIState.getFilterQueryValue();
    }
}
