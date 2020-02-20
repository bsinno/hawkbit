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

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyActionStatus;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.View;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class ActionStatusMsgGridLayoutEventListener {
    private final ActionStatusMsgGridLayout actionStatusMsgGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    ActionStatusMsgGridLayoutEventListener(final ActionStatusMsgGridLayout actionStatusMsgGridLayout,
            final UIEventBus eventBus) {
        this.actionStatusMsgGridLayout = actionStatusMsgGridLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new SelectionChangedListener());
    }

    private class SelectionChangedListener {

        public SelectionChangedListener() {
            eventBus.subscribe(this, EventTopics.SELECTION_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onActionStatusEvent(final SelectionChangedEventPayload<ProxyActionStatus> eventPayload) {
            if (eventPayload.getView() != View.DEPLOYMENT
                    || eventPayload.getLayout() != Layout.ACTION_HISTORY_STATUS_LIST) {
                return;
            }

            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                actionStatusMsgGridLayout.onActionStatusChanged(eventPayload.getEntity());
            } else {
                actionStatusMsgGridLayout.onActionStatusChanged(null);
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
