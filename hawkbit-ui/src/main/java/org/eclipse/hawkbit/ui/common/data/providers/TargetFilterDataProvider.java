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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilter;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

/**
 * Data provider for {@link TargetFilterQuery}, which dynamically loads a batch
 * of {@link TargetFilterQuery} entities from backend and maps them to
 * corresponding {@link ProxyTargetFilter} entities.
 */
public class TargetFilterDataProvider extends ProxyDataProvider<ProxyTargetFilter, TargetFilterQuery, String> {

    private static final long serialVersionUID = 1L;

    private transient TargetFilterQueryManagement targetFilterQueryManagement;

    public TargetFilterDataProvider(final TargetFilterQueryManagement targetFilterQueryManagement,
            final RolloutUIState rolloutUIState, final TargetFilterQueryToProxyTargetFilterMapper entityMapper) {
        super(rolloutUIState, entityMapper);
        this.targetFilterQueryManagement = targetFilterQueryManagement;
    }

    @Override
    protected Optional<Slice<TargetFilterQuery>> loadBeans(final PageRequest pageRequest) {
        return Optional.of(getSearchTextFromUiState()
                .map(searchText -> targetFilterQueryManagement.findByName(pageRequest, searchText))
                .orElseGet(() -> targetFilterQueryManagement.findAll(pageRequest)));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest) {
        return getSearchTextFromUiState()
                .map(searchText -> targetFilterQueryManagement.findByName(pageRequest, searchText).getTotalElements())
                .orElseGet(() -> targetFilterQueryManagement.findAll(pageRequest).getTotalElements());
    }

}
