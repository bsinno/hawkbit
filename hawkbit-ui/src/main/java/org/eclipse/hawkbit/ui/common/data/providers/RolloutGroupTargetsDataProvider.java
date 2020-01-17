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
import org.eclipse.hawkbit.repository.model.TargetWithActionStatus;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetWithActionStatusToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

/**
 * Data provider for {@link TargetWithActionStatus}, which dynamically loads a
 * batch of {@link TargetWithActionStatus} entities from backend and maps them
 * to corresponding {@link ProxyTarget} entities.
 */
public class RolloutGroupTargetsDataProvider extends ProxyDataProvider<ProxyTarget, TargetWithActionStatus, Long> {
    private static final long serialVersionUID = 1L;

    private final transient RolloutGroupManagement rolloutGroupManagement;

    public RolloutGroupTargetsDataProvider(final RolloutGroupManagement rolloutGroupManagement,
            final TargetWithActionStatusToProxyTargetMapper entityMapper) {
        super(entityMapper);

        this.rolloutGroupManagement = rolloutGroupManagement;
    }

    @Override
    protected Optional<Slice<TargetWithActionStatus>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<Long> filter) {
        return filter.map(rolloutGroupId -> rolloutGroupManagement
                .findAllTargetsOfRolloutGroupWithActionStatus(pageRequest, rolloutGroupId));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<Long> filter) {
        return filter.map(rolloutGroupManagement::countTargetsOfRolloutsGroup).orElse(0L);
    }
}
