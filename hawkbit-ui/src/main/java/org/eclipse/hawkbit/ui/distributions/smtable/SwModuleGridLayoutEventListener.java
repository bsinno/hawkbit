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
import java.util.Collection;
import java.util.List;

import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.SearchFilterEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload.TypeFilterChangedEventType;
import org.eclipse.hawkbit.ui.common.event.View;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

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
        eventListeners.add(new SelectionChangedListener());
        eventListeners.add(new SearchFilterChangedListener());
        eventListeners.add(new TypeFilterChangedListener());
        eventListeners.add(new EntityModifiedListener());
    }

    private class SelectionChangedListener {

        public SelectionChangedListener() {
            eventBus.subscribe(this, EventTopics.SELECTION_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onSmEvent(final SelectionChangedEventPayload<ProxySoftwareModule> eventPayload) {
            if (eventPayload.getView() != View.DISTRIBUTIONS || eventPayload.getLayout() != Layout.SM_LIST) {
                return;
            }

            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                swModuleGridLayout.onSmChanged(eventPayload.getEntity());
            } else {
                swModuleGridLayout.onSmChanged(null);
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsEvent(final SelectionChangedEventPayload<ProxyDistributionSet> eventPayload) {
            if (eventPayload.getView() != View.DISTRIBUTIONS || eventPayload.getLayout() != Layout.DS_LIST) {
                return;
            }

            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                swModuleGridLayout.onDsSelected(eventPayload.getEntity());
            } else {
                swModuleGridLayout.onDsSelected(null);
            }
        }
    }

    private class SearchFilterChangedListener {

        public SearchFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.SEARCH_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onSearchFilterChanged(final SearchFilterEventPayload eventPayload) {
            if (eventPayload.getView() != View.DISTRIBUTIONS
                    || eventPayload.getLayout() != swModuleGridLayout.getLayout()) {
                return;
            }

            swModuleGridLayout.filterGridBySearch(eventPayload.getFilter());
        }
    }

    private class TypeFilterChangedListener {

        public TypeFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.TYPE_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTypeChangedEvent(final TypeFilterChangedEventPayload<SoftwareModuleType> eventPayload) {
            if (eventPayload.getView() != View.DISTRIBUTIONS || eventPayload.getLayout() != Layout.SM_TYPE_FILTER) {
                return;
            }

            if (eventPayload.getTypeFilterChangedEventType() == TypeFilterChangedEventType.TYPE_CLICKED) {
                swModuleGridLayout.filterGridByType(eventPayload.getType());
            } else {
                swModuleGridLayout.filterGridByType(null);
            }
        }
    }

    private class EntityModifiedListener {

        public EntityModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onSmEvent(final EntityModifiedEventPayload eventPayload) {
            if (!ProxySoftwareModule.class.equals(eventPayload.getEntityType())) {
                return;
            }

            final EntityModifiedEventType eventType = eventPayload.getEntityModifiedEventType();
            final Collection<Long> entityIds = eventPayload.getEntityIds();

            swModuleGridLayout.refreshGrid();

            if (eventType == EntityModifiedEventType.ENTITY_ADDED && entityIds.size() == 1) {
                UI.getCurrent().access(() -> swModuleGridLayout.selectEntityById(entityIds.iterator().next()));
            } else if (eventType == EntityModifiedEventType.ENTITY_UPDATED) {
                // TODO: we need to access the UI here because of getting the
                // Timezone from getWebBrowser in SpDateTimeUtil, check if it is
                // right or improve
                UI.getCurrent().access(() -> swModuleGridLayout.onSmUpdated(entityIds));
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsEvent(final EntityModifiedEventPayload eventPayload) {
            if (!ProxyDistributionSet.class.equals(eventPayload.getEntityType())) {
                return;
            }

            if (eventPayload.getEntityModifiedEventType() == EntityModifiedEventType.ENTITY_UPDATED) {
                swModuleGridLayout.onDsUpdated(eventPayload.getEntityIds());
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
