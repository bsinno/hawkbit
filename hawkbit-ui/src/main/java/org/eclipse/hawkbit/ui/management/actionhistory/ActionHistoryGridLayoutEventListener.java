/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.actionhistory;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyActionStatus;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.ActionModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.TargetModifiedEventPayload;
import org.eclipse.hawkbit.ui.management.targettable.TargetGrid;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

public class ActionHistoryGridLayoutEventListener {
    private final ActionHistoryGridLayout actionHistoryGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    ActionHistoryGridLayoutEventListener(final ActionHistoryGridLayout actionHistoryGridLayout,
            final UIEventBus eventBus) {
        this.actionHistoryGridLayout = actionHistoryGridLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new SelectionChangedListener());
        eventListeners.add(new EntityModifiedListener());
    }

    private class SelectionChangedListener {

        public SelectionChangedListener() {
            eventBus.subscribe(this, EventTopics.SELECTION_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetGrid.class)
        private void onTargetEvent(final SelectionChangedEventPayload<ProxyTarget> eventPayload) {
            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                actionHistoryGridLayout.onTargetSelected(eventPayload.getEntity());
            } else {
                actionHistoryGridLayout.onTargetSelected(null);
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = ActionHistoryGrid.class)
        private void onActionEvent(final SelectionChangedEventPayload<ProxyAction> eventPayload) {
            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                actionHistoryGridLayout.onActionChanged(eventPayload.getEntity());
            } else {
                actionHistoryGridLayout.onActionChanged(null);
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = ActionStatusGridLayout.class)
        private void onActionStatusEvent(final SelectionChangedEventPayload<ProxyActionStatus> eventPayload) {
            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                actionHistoryGridLayout.onActionStatusChanged(eventPayload.getEntity());
            } else {
                actionHistoryGridLayout.onActionStatusChanged(null);
            }
        }
    }

    private class EntityModifiedListener {

        public EntityModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetEvent(final TargetModifiedEventPayload eventPayload) {
            if (eventPayload.getEntityModifiedEventType() == EntityModifiedEventType.ENTITY_UPDATED) {
                actionHistoryGridLayout.onTargetUpdated(eventPayload.getEntityIds());
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onActionEvent(final ActionModifiedEventPayload eventPayload) {
            actionHistoryGridLayout.refreshGrid();
            if (eventPayload.getEntityModifiedEventType() == EntityModifiedEventType.ENTITY_UPDATED) {
                // TODO: we need to access the UI here because of getting the
                // Timezone from getWebBrowser in SpDateTimeUtil, check if it is
                // right or improve
                UI.getCurrent().access(() -> actionHistoryGridLayout.onActionUpdated(eventPayload.getEntityIds()));
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
