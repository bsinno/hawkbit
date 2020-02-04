/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

public class DistributionSetGridLayoutEventListener {
    private final DistributionSetGridLayout distributionSetGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    DistributionSetGridLayoutEventListener(final DistributionSetGridLayout distributionSetGridLayout,
            final UIEventBus eventBus) {
        this.distributionSetGridLayout = distributionSetGridLayout;
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

        @EventBusListenerMethod(scope = EventScope.UI, source = DistributionSetGrid.class)
        private void onDsEvent(final SelectionChangedEventPayload<ProxyDistributionSet> eventPayload) {
            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                distributionSetGridLayout.onDsChanged(eventPayload.getEntity());
            } else {
                distributionSetGridLayout.onDsChanged(null);
            }
        }
    }

    private class SearchFilterChangedListener {

        public SearchFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.SEARCH_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = DistributionSetGridHeader.class)
        private void onDsEvent(final String searchFilter) {
            distributionSetGridLayout.filterGridBySearch(searchFilter);
        }
    }

    private class EntityModifiedListener {

        public EntityModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsEvent(final EntityModifiedEventPayload eventPayload) {
            if (!ProxyDistributionSet.class.equals(eventPayload.getEntityType())) {
                return;
            }

            distributionSetGridLayout.refreshGrid();
            if (eventPayload.getEntityModifiedEventType() == EntityModifiedEventType.ENTITY_UPDATED) {
                // TODO: we need to access the UI here because of getting the
                // Timezone from getWebBrowser in SpDateTimeUtil, check if it is
                // right or improve
                UI.getCurrent().access(() -> distributionSetGridLayout.onDsUpdated(eventPayload.getEntityIds()));
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsTagEvent(final EntityModifiedEventPayload eventPayload) {
            if (!ProxyDistributionSet.class.equals(eventPayload.getParentType())
                    || !ProxyTag.class.equals(eventPayload.getEntityType())) {
                return;
            }

            distributionSetGridLayout.onDsTagsModified(eventPayload.getEntityIds(),
                    eventPayload.getEntityModifiedEventType());
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
