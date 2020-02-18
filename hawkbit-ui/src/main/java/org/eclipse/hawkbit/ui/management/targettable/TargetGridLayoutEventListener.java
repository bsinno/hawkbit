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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.BulkUploadEventPayload;
import org.eclipse.hawkbit.ui.common.event.CustomFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.CustomFilterChangedEventPayload.CustomFilterChangedEventType;
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
import org.eclipse.hawkbit.ui.common.event.TargetFilterTabChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.View;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.server.VaadinSession;
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
        eventListeners.add(new PinnedDsChangedListener());
        eventListeners.add(new BulkUploadChangedListener());
        eventListeners.add(new EntityModifiedListener());
    }

    private class SelectionChangedListener {

        public SelectionChangedListener() {
            eventBus.subscribe(this, EventTopics.SELECTION_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetEvent(final SelectionChangedEventPayload<ProxyTarget> eventPayload) {
            if (eventPayload.getView() != View.DEPLOYMENT || eventPayload.getLayout() != Layout.TARGET_LIST) {
                return;
            }

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

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onSearchFilterChanged(final SearchFilterEventPayload eventPayload) {
            if (eventPayload.getView() != View.DEPLOYMENT || eventPayload.getLayout() != targetGridLayout.getLayout()) {
                return;
            }

            targetGridLayout.filterGridBySearch(eventPayload.getFilter());
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

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetTagEvent(final TagFilterChangedEventPayload eventPayload) {
            if (eventPayload.getView() != View.DEPLOYMENT || eventPayload.getLayout() != Layout.TARGET_TAG_FILTER) {
                return;
            }

            targetGridLayout.filterGridByTags(eventPayload.getTagNames());
        }
    }

    private class NoTagFilterChangedListener {

        public NoTagFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.NO_TAG_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetNoTagEvent(final NoTagFilterChangedEventPayload eventPayload) {
            if (eventPayload.getView() != View.DEPLOYMENT || eventPayload.getLayout() != Layout.TARGET_TAG_FILTER) {
                return;
            }

            targetGridLayout.filterGridByNoTag(eventPayload.getIsNoTagActive());
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

    private class PinnedDsChangedListener {

        public PinnedDsChangedListener() {
            eventBus.subscribe(this, EventTopics.PINNING_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetPinEvent(final PinningChangedEventPayload<Long> eventPayload) {
            if (!ProxyDistributionSet.class.equals(eventPayload.getEntityType())) {
                return;
            }

            if (eventPayload.getPinningChangedEventType() == PinningChangedEventType.ENTITY_PINNED) {
                targetGridLayout.filterGridByPinnedDs(eventPayload.getEntityId());
            } else {
                targetGridLayout.filterGridByPinnedDs(null);
            }
        }
    }

    private class BulkUploadChangedListener {

        public BulkUploadChangedListener() {
            eventBus.subscribe(this, EventTopics.BULK_UPLOAD_CHANGED);
        }

        // session scope is used here because the bulk upload handler is running
        // as the background job, started by the ui Executor and survives the UI
        // restart
        @EventBusListenerMethod(scope = EventScope.SESSION)
        private void onBulkUploadEvent(final BulkUploadEventPayload eventPayload) {
            VaadinSession.getCurrent().access(() -> targetGridLayout.onBulkUploadChanged(eventPayload));
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

            final EntityModifiedEventType eventType = eventPayload.getEntityModifiedEventType();
            final Collection<Long> entityIds = eventPayload.getEntityIds();

            targetGridLayout.refreshGrid();

            if (eventType == EntityModifiedEventType.ENTITY_ADDED && entityIds.size() == 1) {
                UI.getCurrent().access(() -> targetGridLayout.selectEntityById(entityIds.iterator().next()));
            } else if (eventPayload.getEntityModifiedEventType() == EntityModifiedEventType.ENTITY_UPDATED) {
                // TODO: we need to access the UI here because of getting the
                // Timezone from getWebBrowser in SpDateTimeUtil, check if it is
                // right or improve
                UI.getCurrent().access(() -> targetGridLayout.onTargetUpdated(entityIds));
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
