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
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.ui.common.data.mappers.ActionStatusToProxyActionStatusMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyActionStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

/**
 * Data provider for {@link ActionStatus}, which dynamically loads a batch of
 * {@link ActionStatus} entities from backend and maps them to corresponding
 * {@link ProxyActionStatus} entities.
 */
public class ActionStatusDataProvider extends ProxyDataProvider<ProxyActionStatus, ActionStatus, String> {

    // TODO: override sortOrders: new Sort(Direction.DESC, "id");

    private static final long serialVersionUID = 1L;

    private final transient DeploymentManagement deploymentManagement;
    private final Long currentSelectedActionId;

    public ActionStatusDataProvider(final DeploymentManagement deploymentManagement, final Long currentSelectedActionId,
            final ActionStatusToProxyActionStatusMapper entityMapper) {
        super(entityMapper);

        this.deploymentManagement = deploymentManagement;
        this.currentSelectedActionId = currentSelectedActionId;
    }

    @Override
    protected Optional<Slice<ActionStatus>> loadBeans(final PageRequest pageRequest, final String filter) {
        return currentSelectedActionId != null
                ? Optional.of(deploymentManagement.findActionStatusByAction(pageRequest, currentSelectedActionId))
                : Optional.empty();
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        return currentSelectedActionId != null
                ? deploymentManagement.findActionStatusByAction(pageRequest, currentSelectedActionId).getTotalElements()
                : 0L;
    }
}
