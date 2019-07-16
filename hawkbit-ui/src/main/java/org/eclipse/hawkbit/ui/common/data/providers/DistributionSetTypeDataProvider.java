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

import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeTagMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Data provider for {@link DistributionSetType}, which dynamically loads a
 * batch of {@link DistributionSetType} entities from backend and maps them to
 * corresponding {@link ProxyType} entities.
 */
public class DistributionSetTypeDataProvider extends ProxyDataProvider<ProxyType, DistributionSetType, Void> {

    private static final long serialVersionUID = 1L;

    private final transient DistributionSetTypeManagement distributionSetTypeManagement;

    public DistributionSetTypeDataProvider(final DistributionSetTypeManagement distributionSetTypeManagement,
            final TypeToProxyTypeTagMapper<DistributionSetType> mapper) {
        super(mapper, new Sort(Direction.ASC, "name"));
        this.distributionSetTypeManagement = distributionSetTypeManagement;
    }

    @Override
    protected Optional<Slice<DistributionSetType>> loadBackendEntities(final PageRequest pageRequest,
            final Optional<Void> filter) {
        return Optional.of(distributionSetTypeManagement.findAll(pageRequest));
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final Optional<Void> filter) {
        return distributionSetTypeManagement.count();
    }

}
