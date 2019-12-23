/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.event.ChangeUiElementPayload;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class FilterManagementViewEventListener {
    private final FilterManagementView filterManagementView;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    FilterManagementViewEventListener(final FilterManagementView filterManagementView, final UIEventBus eventBus) {
        this.filterManagementView = filterManagementView;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        // TODO: should we listen for the event directly in
        // layouts/components instead of calling methods here?
        eventListeners.add(new OpenFilterQueryListener());
        eventListeners.add(new CloseDetailsListener());
        eventListeners.add(new CretateFilterQueryListener());
    }

    private class OpenFilterQueryListener {
        public OpenFilterQueryListener() {
            eventBus.subscribe(this, EventTopics.OPEN_ENTITY);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetFilterGrid.class)
        private void onFilterQueryOpen(final ProxyTargetFilterQuery filterQuery) {
            filterManagementView.onFilterQueryOpen(filterQuery);
        }
    }

    private class CloseDetailsListener {
        public CloseDetailsListener() {
            eventBus.subscribe(this, EventTopics.CHANGE_UI_ELEMENT_STATE);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetFilterDetailsLayout.class)
        private void onDetailsClose(final ChangeUiElementPayload payload) {
            filterManagementView.onDetailsClose();
        }
    }

    private class CretateFilterQueryListener {
        public CretateFilterQueryListener() {
            eventBus.subscribe(this, EventTopics.CREATE_ENTITY);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetFilterGridHeader.class)
        private void onFilterQueryCreate(final Class c) {
            filterManagementView.onFilterQueryCreate();
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
