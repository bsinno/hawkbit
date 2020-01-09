/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.TargetModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.TargetTagModifiedEventPayload;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

public class TargetGridLayoutEventListener {
    private final TargetGridLayout targetGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    TargetGridLayoutEventListener(final TargetGridLayout targetGridLayout, final UIEventBus eventBus) {
        this.targetGridLayout = targetGridLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new SelectionChangedListener());
        eventListeners.add(new SearchFilterChangedListener());
        eventListeners.add(new EntityModifiedListener());
    }

    private class SelectionChangedListener {

        public SelectionChangedListener() {
            eventBus.subscribe(this, EventTopics.SELECTION_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetGrid.class)
        private void onTargetEvent(final SelectionChangedEventPayload<ProxyTarget> eventPayload) {
            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                targetGridLayout.onTargetChanged(eventPayload.getEntity());
            } else {
                targetGridLayout.onTargetChanged(null);
            }
        }
    }

    private class SearchFilterChangedListener {

        public SearchFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.SEARCH_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetGridHeader.class)
        private void onTargetEvent(final String searchFilter) {
            targetGridLayout.filterGridBySearch(searchFilter);
        }
    }

    private class EntityModifiedListener {

        public EntityModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetEvent(final TargetModifiedEventPayload eventPayload) {
            targetGridLayout.refreshGrid();
            if (eventPayload.getEntityModifiedEventType() == EntityModifiedEventType.ENTITY_UPDATED) {
                // TODO: we need to access the UI here because of getting the
                // Timezone from getWebBrowser in SpDateTimeUtil, check if it is
                // right or improve
                UI.getCurrent().access(() -> targetGridLayout.onTargetUpdated(eventPayload.getEntityIds()));
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetTagEvent(final TargetTagModifiedEventPayload eventPayload) {
            targetGridLayout.onTargetTagsModified(eventPayload.getEntityIds(),
                    eventPayload.getEntityModifiedEventType());
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
