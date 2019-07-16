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

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.ui.common.data.mappers.ActionStatusToProxyActionStatusMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyActionStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Data provider for {@link ActionStatus}, which dynamically loads a batch of
 * {@link ActionStatus} entities from backend and maps them to corresponding
 * {@link ProxyActionStatus} entities. The filter is used for master-details
 * relationship with {@link Action}, using its id.
 */
public class ActionStatusDataProvider extends ProxyDataProvider<ProxyActionStatus, ActionStatus, Long> {

    private static final long serialVersionUID = 1L;

    private final transient DeploymentManagement deploymentManagement;

    public ActionStatusDataProvider(final DeploymentManagement deploymentManagement,
            final ActionStatusToProxyActionStatusMapper entityMapper) {
        super(entityMapper, new Sort(Direction.DESC, "id"));

        this.deploymentManagement = deploymentManagement;
    }

    @Override
    protected Optional<Slice<ActionStatus>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<Long> filter) {
        return filter.map(currentlySelectedActionId -> deploymentManagement.findActionStatusByAction(pageRequest,
                currentlySelectedActionId));

    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<Long> filter) {
        return filter
                .map(currentlySelectedActionId -> deploymentManagement
                        .findActionStatusByAction(pageRequest, currentlySelectedActionId).getTotalElements())
                .orElse(0L);
    }
}
