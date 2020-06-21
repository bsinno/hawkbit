/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.providers;

import java.util.stream.Stream;

import org.eclipse.hawkbit.ui.common.data.mappers.IdentifiableEntityToProxyIdentifiableEntityMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.hateoas.Identifiable;

/**
 * Base class for loading a batch of {@link Identifiable} entities from backend
 * mapping them to {@link ProxyIdentifiableEntity} entities.
 */
public abstract class ProxyDataProvider<T extends ProxyIdentifiableEntity, U extends Identifiable<Long>, F>
        extends GenericDataProvider<T, U, F> {
    private static final long serialVersionUID = 1L;

    private final transient IdentifiableEntityToProxyIdentifiableEntityMapper<T, U> entityMapper;

    /**
     * Constructor for ProxyDataProvider with mapper
     *
     * @param mapper
     *          IdentifiableEntityToProxyIdentifiableEntityMapper of generic type
     */
    public ProxyDataProvider(final IdentifiableEntityToProxyIdentifiableEntityMapper<T, U> mapper) {
        this(mapper, new Sort(Direction.ASC, "id"));
    }

    /**
     * Constructor for ProxyDataProvider with mapper and sorting order
     *
     * @param mapper
     *          IdentifiableEntityToProxyIdentifiableEntityMapper of generic type
     * @param defaultSortOrder
     *          Sort
     */
    public ProxyDataProvider(final IdentifiableEntityToProxyIdentifiableEntityMapper<T, U> mapper,
            final Sort defaultSortOrder) {
        super(defaultSortOrder);

        this.entityMapper = mapper;
    }

    @Override
    protected Stream<T> getProxyEntities(final Slice<U> backendEntities) {
        return backendEntities.stream().map(entityMapper::map);
    }
}
