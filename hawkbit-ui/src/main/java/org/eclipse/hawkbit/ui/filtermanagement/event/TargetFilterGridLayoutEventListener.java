/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement.event;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SearchFilterEventPayload;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.filtermanagement.TargetFilterGridLayout;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class TargetFilterGridLayoutEventListener {
    private final TargetFilterGridLayout targetFilterGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    public TargetFilterGridLayoutEventListener(final TargetFilterGridLayout targetFilterGridLayout,
            final UIEventBus eventBus) {
        this.targetFilterGridLayout = targetFilterGridLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new SearchFilterChangedListener());
        eventListeners.add(new TargetFilterModifiedListener());
    }

    private class SearchFilterChangedListener {
        public SearchFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.SEARCH_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onSearchFilterChanged(final SearchFilterEventPayload eventPayload) {
            if (eventPayload.getView() != View.TARGET_FILTER
                    || eventPayload.getLayout() != targetFilterGridLayout.getLayout()) {
                return;
            }

            targetFilterGridLayout.filterGridByName(eventPayload.getFilter());
        }
    }

    private class TargetFilterModifiedListener {
        public TargetFilterModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetFilterModified(final EntityModifiedEventPayload eventPayload) {
            if (!ProxyTargetFilterQuery.class.equals(eventPayload.getEntityType())) {
                return;
            }

            targetFilterGridLayout.refreshGrid();
        }
    }

    public void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
