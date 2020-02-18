/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.NoTagFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload.PinningChangedEventType;
import org.eclipse.hawkbit.ui.common.event.SearchFilterEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.TagFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.View;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

public class DistributionGridLayoutEventListener {
    private final DistributionGridLayout distributionGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    DistributionGridLayoutEventListener(final DistributionGridLayout distributionGridLayout,
            final UIEventBus eventBus) {
        this.distributionGridLayout = distributionGridLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new SelectionChangedListener());
        eventListeners.add(new SearchFilterChangedListener());
        eventListeners.add(new TagFilterChangedListener());
        eventListeners.add(new NoTagFilterChangedListener());
        eventListeners.add(new PinnedTargetChangedListener());
        eventListeners.add(new EntityModifiedListener());
    }

    private class SelectionChangedListener {

        public SelectionChangedListener() {
            eventBus.subscribe(this, EventTopics.SELECTION_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsEvent(final SelectionChangedEventPayload<ProxyDistributionSet> eventPayload) {
            if (eventPayload.getView() != View.DEPLOYMENT || eventPayload.getLayout() != Layout.DS_LIST) {
                return;
            }

            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                distributionGridLayout.onDsChanged(eventPayload.getEntity());
            } else {
                distributionGridLayout.onDsChanged(null);
            }
        }
    }

    private class SearchFilterChangedListener {

        public SearchFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.SEARCH_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onSearchFilterChanged(final SearchFilterEventPayload eventPayload) {
            if (eventPayload.getView() != View.DEPLOYMENT
                    || eventPayload.getLayout() != distributionGridLayout.getLayout()) {
                return;
            }

            distributionGridLayout.filterGridBySearch(eventPayload.getFilter());
        }
    }

    private class TagFilterChangedListener {

        public TagFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.TAG_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsTagEvent(final TagFilterChangedEventPayload eventPayload) {
            if (eventPayload.getView() != View.DEPLOYMENT || eventPayload.getLayout() != Layout.DS_TAG_FILTER) {
                return;
            }

            distributionGridLayout.filterGridByTags(eventPayload.getTagNames());
        }
    }

    private class NoTagFilterChangedListener {

        public NoTagFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.NO_TAG_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsNoTagEvent(final NoTagFilterChangedEventPayload eventPayload) {
            if (eventPayload.getView() != View.DEPLOYMENT || eventPayload.getLayout() != Layout.DS_TAG_FILTER) {
                return;
            }

            distributionGridLayout.filterGridByNoTag(eventPayload.getIsNoTagActive());
        }
    }

    private class PinnedTargetChangedListener {

        public PinnedTargetChangedListener() {
            eventBus.subscribe(this, EventTopics.PINNING_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetPinEvent(final PinningChangedEventPayload<String> eventPayload) {
            if (!ProxyTarget.class.equals(eventPayload.getEntityType())) {
                return;
            }

            if (eventPayload.getPinningChangedEventType() == PinningChangedEventType.ENTITY_PINNED) {
                distributionGridLayout.filterGridByPinnedTarget(eventPayload.getEntityId());
            } else {
                distributionGridLayout.filterGridByPinnedTarget(null);
            }
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

            final EntityModifiedEventType eventType = eventPayload.getEntityModifiedEventType();
            final Collection<Long> entityIds = eventPayload.getEntityIds();

            distributionGridLayout.refreshGrid();

            if (eventType == EntityModifiedEventType.ENTITY_ADDED && entityIds.size() == 1) {
                UI.getCurrent().access(() -> distributionGridLayout.selectEntityById(entityIds.iterator().next()));
            } else if (eventPayload.getEntityModifiedEventType() == EntityModifiedEventType.ENTITY_UPDATED) {
                // TODO: we need to access the UI here because of getting the
                // Timezone from getWebBrowser in SpDateTimeUtil, check if it is
                // right or improve
                UI.getCurrent().access(() -> distributionGridLayout.onDsUpdated(entityIds));
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsTagEvent(final EntityModifiedEventPayload eventPayload) {
            if (!ProxyDistributionSet.class.equals(eventPayload.getParentType())
                    || !ProxyTag.class.equals(eventPayload.getEntityType())) {
                return;
            }

            distributionGridLayout.onDsTagsModified(eventPayload.getEntityIds(),
                    eventPayload.getEntityModifiedEventType());
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
