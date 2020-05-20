/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.providers;

import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.hawkbit.repository.OffsetBasedPageRequest;
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.repository.model.MetaData;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyActionStatus;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.vaadin.data.provider.AbstractBackEndDataProvider;
import com.vaadin.data.provider.Query;

/**
 * Abstract data provider for {@link MetaData}, which dynamically loads a batch
 * of {@link MetaData} entities from backend and maps them to corresponding
 * {@link ProxyMetaData} entities.
 */
public abstract class AbstractMetaDataDataProvider<T extends MetaData, F>
        extends AbstractBackEndDataProvider<ProxyMetaData, F> {
    private static final long serialVersionUID = 1L;

    private final Sort defaultSortOrder = new Sort(Direction.DESC, "key");

    @Override
    protected Stream<ProxyMetaData> fetchFromBackEnd(final Query<ProxyMetaData, F> query) {
        return loadBackendEntities(convertToPageRequest(query, defaultSortOrder), query.getFilter().orElse(null))
                .stream().map(this::createProxyMetaData);
    }

    protected abstract Page<T> loadBackendEntities(final PageRequest pageRequest,
            final F currentlySelectedMasterEntityId);

    // TODO: remove duplication with ProxyDataProvider
    private PageRequest convertToPageRequest(final Query<ProxyMetaData, F> query, final Sort sort) {
        return new OffsetBasedPageRequest(query.getOffset(), query.getLimit(), sort);
    }

    /**
     * Creates a list of {@link ProxyActionStatus} for presentation layer from
     * page of {@link ActionStatus}.
     *
     * @param actionBeans
     *            page of {@link ActionStatus}
     * @return list of {@link ProxyActionStatus}
     */
    protected ProxyMetaData createProxyMetaData(final T metadata) {
        final ProxyMetaData proxyMetaData = new ProxyMetaData();

        proxyMetaData.setEntityId(metadata.getEntityId());
        proxyMetaData.setKey(metadata.getKey());
        proxyMetaData.setValue(metadata.getValue());

        return proxyMetaData;
    }

    @Override
    protected int sizeInBackEnd(final Query<ProxyMetaData, F> query) {
        final long size = sizeInBackEnd(convertToPageRequest(query, defaultSortOrder), query.getFilter().orElse(null));

        if (size > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) size;
    }

    protected abstract long sizeInBackEnd(final PageRequest pageRequest, final F currentlySelectedMasterEntityId);

    @Override
    public Object getId(final ProxyMetaData item) {
        Objects.requireNonNull(item, "Cannot provide an id for a null item.");
        return item.getKey();
    }

}
