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
import org.eclipse.hawkbit.repository.model.TargetWithActionStatus;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetWithActionStatusToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

/**
 * Data provider for {@link TargetWithActionStatus}, which dynamically loads a
 * batch of {@link TargetWithActionStatus} entities from backend and maps them
 * to corresponding {@link ProxyTarget} entities.
 */
public class RolloutGroupTargetsDataProvider extends ProxyDataProvider<ProxyTarget, TargetWithActionStatus, String> {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(RolloutGroupTargetsDataProvider.class);

    private final transient RolloutGroupManagement rolloutGroupManagement;
    private final RolloutUIState rolloutUIState;

    public RolloutGroupTargetsDataProvider(final RolloutGroupManagement rolloutGroupManagement,
            final RolloutUIState rolloutUIState, final TargetWithActionStatusToProxyTargetMapper entityMapper) {
        super(entityMapper);

        this.rolloutGroupManagement = rolloutGroupManagement;
        this.rolloutUIState = rolloutUIState;
    }

    @Override
    protected Optional<Slice<TargetWithActionStatus>> loadBackendEntities(final PageRequest pageRequest,
            final String filter) {
        return getRolloutGroupIdFromUiState().map(rolloutGroupId -> rolloutGroupManagement
                .findAllTargetsOfRolloutGroupWithActionStatus(pageRequest, rolloutGroupId));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        final Optional<Long> rolloutGroupId = getRolloutGroupIdFromUiState();
        final long size = rolloutGroupId.map(id -> {
            try {
                return rolloutGroupManagement.countTargetsOfRolloutsGroup(id);
            } catch (final EntityNotFoundException e) {
                LOG.warn("Rollout group does not exists. Redirecting to Rollouts Group overview", e);
                rolloutUIState.setShowRolloutGroupTargets(false);
                rolloutUIState.setShowRolloutGroups(true);

                return 0L;
            }
        }).orElse(0L);

        rolloutUIState.setRolloutGroupTargetsTotalCount(size);
        if (size > SPUIDefinitions.MAX_TABLE_ENTRIES) {
            rolloutUIState.setRolloutGroupTargetsTruncated(size - SPUIDefinitions.MAX_TABLE_ENTRIES);
            return SPUIDefinitions.MAX_TABLE_ENTRIES;
        }

        return size;

    }

    private Optional<Long> getRolloutGroupIdFromUiState() {
        return rolloutUIState.getRolloutGroup().map(RolloutGroup::getId);
    }
}
