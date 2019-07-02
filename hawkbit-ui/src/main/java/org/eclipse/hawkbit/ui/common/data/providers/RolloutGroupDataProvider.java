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

import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.ui.common.data.mappers.RolloutGroupToProxyRolloutGroupMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

/**
 * Data provider for {@link RolloutGroup}, which dynamically loads a batch of
 * {@link RolloutGroup} entities from backend and maps them to corresponding
 * {@link ProxyRolloutGroup} entities.
 */
public class RolloutGroupDataProvider extends ProxyDataProvider<ProxyRolloutGroup, RolloutGroup, String> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(RolloutGroupDataProvider.class);

    private final transient RolloutGroupManagement rolloutGroupManagement;
    private final RolloutUIState rolloutUIState;

    /**
     * Parametric Constructor.
     *
     * @param rolloutGroupManagement
     *            rollout group management
     * @param rolloutUIState
     *            ui state
     */
    public RolloutGroupDataProvider(final RolloutGroupManagement rolloutGroupManagement,
            final RolloutUIState rolloutUIState, final RolloutGroupToProxyRolloutGroupMapper entityMapper) {
        super(entityMapper);

        this.rolloutGroupManagement = rolloutGroupManagement;
        this.rolloutUIState = rolloutUIState;
    }

    @Override
    protected Optional<Slice<RolloutGroup>> loadBackendEntities(final PageRequest pageRequest, final String filter) {
        return rolloutUIState.getRolloutId()
                .map(rolloutId -> rolloutGroupManagement.findByRolloutWithDetailedStatus(pageRequest, rolloutId));

    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        final Optional<Long> rolloutId = rolloutUIState.getRolloutId();
        return rolloutId.map(id -> {
            try {
                return rolloutGroupManagement.countByRollout(id);
            } catch (final EntityNotFoundException e) {
                LOG.warn("Rollout does not exists. Redirecting to Rollouts overview", e);
                rolloutUIState.setShowRolloutGroups(false);
                rolloutUIState.setShowRollOuts(true);

                return 0L;
            }
        }).orElse(0L);
    }
}
