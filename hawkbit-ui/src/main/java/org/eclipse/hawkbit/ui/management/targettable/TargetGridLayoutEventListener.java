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
import java.util.Collection;
import java.util.List;

import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.CustomFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.CustomFilterChangedEventPayload.CustomFilterChangedEventType;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.TargetFilterTabChangedEventPayload;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterButtons;
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
        eventListeners.add(new FilterModeChangedListener());
        eventListeners.add(new TagFilterChangedListener());
        eventListeners.add(new NoTagFilterChangedListener());
        eventListeners.add(new StatusFilterChangedListener());
        eventListeners.add(new OverdueFilterChangedListener());
        eventListeners.add(new CustomFilterChangedListener());
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

    private class FilterModeChangedListener {

        public FilterModeChangedListener() {
            eventBus.subscribe(this, EventTopics.TARGET_FILTER_TAB_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetEvent(final TargetFilterTabChangedEventPayload eventPayload) {
            targetGridLayout.onTargetFilterTabChanged(TargetFilterTabChangedEventPayload.CUSTOM == eventPayload);
        }
    }

    private class TagFilterChangedListener {

        public TagFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.TAG_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetTagFilterButtons.class)
        private void onTargetTagEvent(final Collection<String> eventPayload) {
            targetGridLayout.filterGridByTags(eventPayload);
        }
    }

    private class NoTagFilterChangedListener {

        public NoTagFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.NO_TAG_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetTagFilterButtons.class)
        private void onTargetNoTagEvent(final Boolean eventPayload) {
            targetGridLayout.filterGridByNoTag(eventPayload);
        }
    }

    private class StatusFilterChangedListener {

        public StatusFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.STATUS_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetEvent(final List<TargetUpdateStatus> eventPayload) {
            targetGridLayout.filterGridByStatus(eventPayload);
        }
    }

    private class OverdueFilterChangedListener {

        public OverdueFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.OVERDUE_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetEvent(final Boolean eventPayload) {
            targetGridLayout.filterGridByOverdue(eventPayload);
        }
    }

    private class CustomFilterChangedListener {

        public CustomFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.CUSTOM_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetEvent(final CustomFilterChangedEventPayload eventPayload) {
            if (eventPayload.getCustomFilterChangedEventType() == CustomFilterChangedEventType.CLICKED) {
                targetGridLayout.filterGridByCustomFilter(eventPayload.getCustomFilterId());
            } else {
                targetGridLayout.filterGridByCustomFilter(null);
            }
        }
    }

    private class EntityModifiedListener {

        public EntityModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetEvent(final EntityModifiedEventPayload eventPayload) {
            if (!ProxyTarget.class.equals(eventPayload.getEntityType())) {
                return;
            }

            targetGridLayout.refreshGrid();
            if (eventPayload.getEntityModifiedEventType() == EntityModifiedEventType.ENTITY_UPDATED) {
                // TODO: we need to access the UI here because of getting the
                // Timezone from getWebBrowser in SpDateTimeUtil, check if it is
                // right or improve
                UI.getCurrent().access(() -> targetGridLayout.onTargetUpdated(eventPayload.getEntityIds()));
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetTagEvent(final EntityModifiedEventPayload eventPayload) {
            if (!ProxyTarget.class.equals(eventPayload.getParentType())
                    || !ProxyTag.class.equals(eventPayload.getEntityType())) {
                return;
            }

            targetGridLayout.onTargetTagsModified(eventPayload.getEntityIds(),
                    eventPayload.getEntityModifiedEventType());
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
