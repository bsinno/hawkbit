/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
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

public class SoftwareModuleGridLayoutEventListener {
    private final SoftwareModuleGridLayout softwareModuleGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    SoftwareModuleGridLayoutEventListener(final SoftwareModuleGridLayout softwareModuleGridLayout,
            final UIEventBus eventBus) {
        this.softwareModuleGridLayout = softwareModuleGridLayout;
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
            if (eventPayload.getView() != View.UPLOAD || eventPayload.getLayout() != Layout.SM_LIST) {
                return;
            }

            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                softwareModuleGridLayout.onSmChanged(eventPayload.getEntity());
            } else {
                softwareModuleGridLayout.onSmChanged(null);
            }
        }
    }

    private class SearchFilterChangedListener {

        public SearchFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.SEARCH_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onSearchFilterChanged(final SearchFilterEventPayload eventPayload) {
            if (eventPayload.getView() != View.UPLOAD
                    || eventPayload.getLayout() != softwareModuleGridLayout.getLayout()) {
                return;
            }

            softwareModuleGridLayout.filterGridBySearch(eventPayload.getFilter());
        }
    }

    private class TypeFilterChangedListener {

        public TypeFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.TYPE_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTypeChangedEvent(final TypeFilterChangedEventPayload<SoftwareModuleType> eventPayload) {
            if (eventPayload.getView() != View.UPLOAD || eventPayload.getLayout() != Layout.SM_TYPE_FILTER) {
                return;
            }

            if (eventPayload.getTypeFilterChangedEventType() == TypeFilterChangedEventType.TYPE_CLICKED) {
                softwareModuleGridLayout.filterGridByType(eventPayload.getType());
            } else {
                softwareModuleGridLayout.filterGridByType(null);
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

            softwareModuleGridLayout.refreshGrid();

            if (eventType == EntityModifiedEventType.ENTITY_ADDED && entityIds.size() == 1) {
                UI.getCurrent().access(() -> softwareModuleGridLayout.selectEntityById(entityIds.iterator().next()));
            } else if (eventType == EntityModifiedEventType.ENTITY_UPDATED) {
                // TODO: we need to access the UI here because of getting the
                // Timezone from getWebBrowser in SpDateTimeUtil, check if it is
                // right or improve
                UI.getCurrent().access(() -> softwareModuleGridLayout.onSmUpdated(entityIds));
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
