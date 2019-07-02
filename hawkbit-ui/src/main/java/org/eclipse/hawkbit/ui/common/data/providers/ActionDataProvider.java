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
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.common.data.mappers.ActionToProxyActionMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Data provider for {@link Action}, which dynamically loads a batch of
 * {@link Action} entities from backend and maps them to corresponding
 * {@link ProxyAction} entities.
 */
public class ActionDataProvider extends ProxyDataProvider<ProxyAction, Action, String> {

    private static final long serialVersionUID = 1L;

    private final transient DeploymentManagement deploymentManagement;
    private final Target selectedTarget;

    public ActionDataProvider(final DeploymentManagement deploymentManagement, final Target selectedTarget,
            final ActionToProxyActionMapper entityMapper) {
        super(entityMapper, new Sort(Direction.DESC, "id"));

        this.deploymentManagement = deploymentManagement;
        this.selectedTarget = selectedTarget;
    }

    @Override
    protected Optional<Slice<Action>> loadBackendEntities(final PageRequest pageRequest, final String filter) {
        return selectedTarget != null
                ? Optional.of(deploymentManagement.findActionsByTarget(selectedTarget.getControllerId(), pageRequest))
                : Optional.empty();
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final String filter) {
        return selectedTarget != null ? deploymentManagement.countActionsByTarget(selectedTarget.getControllerId())
                : 0L;
    }
}
