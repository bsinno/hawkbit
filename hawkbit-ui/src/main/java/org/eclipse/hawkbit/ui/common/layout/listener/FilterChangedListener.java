/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventViewAware;
import org.eclipse.hawkbit.ui.common.event.FilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class FilterChangedListener extends ViewAwareListener {
    private final Class<? extends ProxyIdentifiableEntity> entityType;
    private final FilterSupport<?, ?> filterSupport;

    public FilterChangedListener(final UIEventBus eventBus, final Class<? extends ProxyIdentifiableEntity> entityType,
            final EventViewAware viewAware, final FilterSupport<?, ?> filterSupport) {
        super(eventBus, EventTopics.FILTER_CHANGED, viewAware);

        this.entityType = entityType;
        this.filterSupport = filterSupport;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onFilterEvent(final FilterChangedEventPayload<?> eventPayload) {
        if (!suitableEntityType(eventPayload.getEntityType()) || !getViewAware().suitableView(eventPayload)) {
            return;
        }

        final FilterType filterType = eventPayload.getFilterType();

        if (filterSupport.isFilterTypeSupported(filterType)) {
            filterSupport.updateFilter(filterType, eventPayload.getFilterValue());
        }
    }

    private boolean suitableEntityType(final Class<? extends ProxyIdentifiableEntity> type) {
        return entityType != null && entityType.equals(type);
    }
}
