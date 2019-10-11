/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.SmModifiedEventPayload;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class SwModuleGridLayoutEventListener {
    private final SwModuleGridLayout swModuleGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    SwModuleGridLayoutEventListener(final SwModuleGridLayout swModuleGridLayout, final UIEventBus eventBus) {
        this.swModuleGridLayout = swModuleGridLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        // TODO: should we listen for the event directly in
        // layouts/components instead of calling methods here?
        eventListeners.add(new SelectionChangedListener());
        eventListeners.add(new SearchFilterChangedListener());
        eventListeners.add(new EntityModifiedListener());
    }

    private class SelectionChangedListener {

        public SelectionChangedListener() {
            eventBus.subscribe(this, EventTopics.SELECTION_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = SwModuleGrid.class)
        private void onSmEvent(final SelectionChangedEventPayload<ProxySoftwareModule> eventPayload) {
            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                swModuleGridLayout.onSmSelected(eventPayload.getEntity());
            } else {
                swModuleGridLayout.onSmSelected(null);
            }
        }
    }

    private class SearchFilterChangedListener {

        public SearchFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.SEARCH_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = SwModuleGridHeader.class)
        private void onSmEvent(final String searchFilter) {
            swModuleGridLayout.filterGridBySearch(searchFilter);
        }
    }

    private class EntityModifiedListener {

        public EntityModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onSmEvent(final SmModifiedEventPayload eventPayload) {
            swModuleGridLayout.refreshGrid();
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
