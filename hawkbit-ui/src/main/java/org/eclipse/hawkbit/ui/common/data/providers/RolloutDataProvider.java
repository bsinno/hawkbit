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
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.util.StringUtils;

/**
 * Data provider for {@link Rollout}, which dynamically loads a batch of
 * {@link Rollout} entities from backend and maps them to corresponding
 * {@link ProxyRollout} entities.
 */
public class RolloutDataProvider extends ProxyDataProvider<ProxyRollout, Rollout, String> {

    private static final long serialVersionUID = 1L;

    private final transient RolloutManagement rolloutManagement;
    private final RolloutUIState rolloutUIState;

    public RolloutDataProvider(final RolloutManagement rolloutManagement, final RolloutUIState rolloutUIState,
            final RolloutToProxyRolloutMapper entityMapper) {
        super(entityMapper);

        this.rolloutManagement = rolloutManagement;
        this.rolloutUIState = rolloutUIState;
    }

    @Override
    protected Optional<Slice<Rollout>> loadBackendEntities(final PageRequest pageRequest, final String filter) {
        return Optional.of(getSearchTextFromUiState()
                .map(searchText -> rolloutManagement.findByFiltersWithDetailedStatus(pageRequest, searchText, false))
                .orElseGet(() -> rolloutManagement.findAllWithDetailedStatus(pageRequest, false)));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        return getSearchTextFromUiState().map(rolloutManagement::countByFilters).orElseGet(rolloutManagement::count);

    }

    private Optional<String> getSearchTextFromUiState() {
        return rolloutUIState.getSearchText().filter(searchText -> !StringUtils.isEmpty(searchText))
                .map(value -> String.format("%%%s%%", value));
    }
}
