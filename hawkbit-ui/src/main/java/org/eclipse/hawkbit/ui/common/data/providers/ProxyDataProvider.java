/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.providers;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.hawkbit.repository.OffsetBasedPageRequest;
import org.eclipse.hawkbit.ui.common.data.mappers.IdentifiableEntityToProxyIdentifiableEntityMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.hateoas.Identifiable;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;

/**
 * Base class for loading a batch of {@link Identifiable} entities from backend
 * mapping them to {@link ProxyIdentifiableEntity} entities.
 */
public abstract class ProxyDataProvider<T extends ProxyIdentifiableEntity, U extends Identifiable<Long>, F>
        extends AbstractBackEndDataProvider<T, F> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(ProxyDataProvider.class);

    private final Sort defaultSortOrder;

    private final transient IdentifiableEntityToProxyIdentifiableEntityMapper<T, U> entityMapper;

    public ProxyDataProvider(final IdentifiableEntityToProxyIdentifiableEntityMapper<T, U> mapper) {
        this(mapper, new Sort(Direction.ASC, "id"));
    }

    public ProxyDataProvider(final IdentifiableEntityToProxyIdentifiableEntityMapper<T, U> mapper,
            final Sort defaultSortOrder) {
        this.entityMapper = mapper;
        this.defaultSortOrder = defaultSortOrder;
    }

    @Override
    protected Stream<T> fetchFromBackEnd(final Query<T, F> query) {
        return getProxyEntities(loadBackendEntities(convertToPageRequest(query, defaultSortOrder), query.getFilter()))
                .stream();
    }

    private List<T> getProxyEntities(final Optional<Slice<U>> backendEntities) {
        return backendEntities
                .map(entities -> entities.getContent().stream().map(entityMapper::map).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private PageRequest convertToPageRequest(final Query<T, F> query, final Sort sort) {
        return new OffsetBasedPageRequest(query.getOffset(), query.getLimit(), sort);
    }

    protected abstract Optional<Slice<U>> loadBackendEntities(final PageRequest pageRequest, Optional<F> filter);

    @Override
    protected int sizeInBackEnd(final Query<T, F> query) {
        final long size = sizeInBackEnd(convertToPageRequest(query, defaultSortOrder), query.getFilter());

        try {
            return Math.toIntExact(size);
        } catch (final ArithmeticException e) {
            LOG.trace("Error converting size in backend from UI Dataprovider: {}", e.getMessage());

            return Integer.MAX_VALUE;
        }
    }

    protected abstract long sizeInBackEnd(final PageRequest pageRequest, Optional<F> filter);

    @Override
    public Object getId(final T item) {
        Objects.requireNonNull(item, "Cannot provide an id for a null item.");
        return item.getId();
    }
}
