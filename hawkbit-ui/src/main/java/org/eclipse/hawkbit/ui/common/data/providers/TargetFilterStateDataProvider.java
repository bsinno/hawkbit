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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Data provider for {@link Target}, which dynamically loads a batch of
 * {@link Target} entities from backend and maps them to corresponding
 * {@link ProxyTarget} entities.
 */
public class TargetFilterStateDataProvider extends ProxyDataProvider<ProxyTarget, Target, String> {

    private static final long serialVersionUID = 1L;

    private final transient TargetManagement targetManagement;

    public TargetFilterStateDataProvider(final TargetManagement targetManagement,
            final TargetToProxyTargetMapper entityMapper) {
        super(entityMapper, new Sort(Direction.ASC, "name"));

        this.targetManagement = targetManagement;
    }

    @Override
    protected Optional<Slice<Target>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<String> filter) {
        return filter.map(searchText -> targetManagement.findByRsql(pageRequest, searchText));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<String> filter) {
        return filter.map(targetManagement::countByRsql).orElse(0L);
    }
}
