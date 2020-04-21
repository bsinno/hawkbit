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

import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.ui.common.event.BulkUploadEventPayload;
import org.eclipse.hawkbit.ui.common.event.CustomFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.CustomFilterChangedEventPayload.CustomFilterChangedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.FilterByDsEventPayload;
import org.eclipse.hawkbit.ui.common.event.TargetFilterTabChangedEventPayload;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.server.VaadinSession;

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
        eventListeners.add(new FilterModeChangedListener());
        eventListeners.add(new StatusFilterChangedListener());
        eventListeners.add(new OverdueFilterChangedListener());
        eventListeners.add(new CustomFilterChangedListener());
        eventListeners.add(new FilterByDsListener());
        eventListeners.add(new BulkUploadChangedListener());
    }

    private class FilterModeChangedListener {

        public FilterModeChangedListener() {
            eventBus.subscribe(this, EventTopics.TARGET_FILTER_TAB_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetFilterTabChangedEvent(final TargetFilterTabChangedEventPayload eventPayload) {
            targetGridLayout.onTargetFilterTabChanged(TargetFilterTabChangedEventPayload.CUSTOM == eventPayload);
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

    private class FilterByDsListener {

        public FilterByDsListener() {
            eventBus.subscribe(this, EventTopics.FILTER_BY_DS_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsFilterChanged(final FilterByDsEventPayload eventPayload) {
            targetGridLayout.filterGridByDs(eventPayload.getDsId());
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
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
}
