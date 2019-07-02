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
import org.eclipse.hawkbit.ui.common.data.mappers.TargetFilterQueryToProxyTargetFilterMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.StringUtils;

/**
 * Data provider for {@link TargetFilterQuery}, which dynamically loads a batch
 * of {@link TargetFilterQuery} entities from backend and maps them to
 * corresponding {@link ProxyTargetFilterQuery} entities.
 */
public class TargetFilterQueryDataProvider extends ProxyDataProvider<ProxyTargetFilterQuery, TargetFilterQuery, String> {

    private static final long serialVersionUID = 1L;

    private final transient TargetFilterQueryManagement targetFilterQueryManagement;
    private final RolloutUIState rolloutUIState;

    public TargetFilterQueryDataProvider(final TargetFilterQueryManagement targetFilterQueryManagement,
            final RolloutUIState rolloutUIState, final TargetFilterQueryToProxyTargetFilterMapper entityMapper) {
        super(entityMapper, new Sort(Direction.ASC, "name"));

        this.targetFilterQueryManagement = targetFilterQueryManagement;
        this.rolloutUIState = rolloutUIState;
    }

    @Override
    protected Optional<Slice<TargetFilterQuery>> loadBackendEntities(final PageRequest pageRequest,
            final String filter) {
        return Optional.of(getSearchTextFromUiState()
                .map(searchText -> targetFilterQueryManagement.findByName(pageRequest, searchText))
                .orElseGet(() -> targetFilterQueryManagement.findAll(pageRequest)));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        return getSearchTextFromUiState()
                .map(searchText -> targetFilterQueryManagement.findByName(pageRequest, searchText).getTotalElements())
                .orElseGet(() -> targetFilterQueryManagement.findAll(pageRequest).getTotalElements());
    }

    private Optional<String> getSearchTextFromUiState() {
        return rolloutUIState.getSearchText().filter(searchText -> !StringUtils.isEmpty(searchText))
                .map(value -> String.format("%%%s%%", value));
    }

}
