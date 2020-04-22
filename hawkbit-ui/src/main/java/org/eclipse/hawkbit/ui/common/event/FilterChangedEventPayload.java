/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;

public class FilterChangedEventPayload<F> extends EventViewAware {
    private final Class<? extends ProxyIdentifiableEntity> entityType;
    private final FilterType filterType;
    private final F filterValue;

    public FilterChangedEventPayload(final Class<? extends ProxyIdentifiableEntity> entityType, final FilterType filterType,
            final F filterValue, final EventView view) {
        super(view);

        this.entityType = entityType;
        this.filterType = filterType;
        this.filterValue = filterValue;
    }

    public Class<? extends ProxyIdentifiableEntity> getEntityType() {
        return entityType;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public F getFilterValue() {
        return filterValue;
    }
}
