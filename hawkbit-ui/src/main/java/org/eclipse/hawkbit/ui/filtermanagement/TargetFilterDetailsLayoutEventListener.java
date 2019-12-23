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

import org.eclipse.hawkbit.ui.common.event.ChangeUiElementPayload;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class TargetFilterDetailsLayoutEventListener {
    private final TargetFilterDetailsLayout targetFilterDetailsLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    TargetFilterDetailsLayoutEventListener(final TargetFilterDetailsLayout targetFilterDetailsLayout,
            final UIEventBus eventBus) {
        this.targetFilterDetailsLayout = targetFilterDetailsLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new GridUpdatedListener());
        eventListeners.add(new CloseDetailsListener());
        eventListeners.add(new UpdateGridListener());
    }

    private class GridUpdatedListener {
        public GridUpdatedListener() {
            eventBus.subscribe(this, EventTopics.UI_ELEMENT_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetFilterTargetGrid.class)
        private void onFilterQuery(final Long totalTargetCount) {
            targetFilterDetailsLayout.onGridUpdated(totalTargetCount);
        }
    }

    private class UpdateGridListener {
        public UpdateGridListener() {
            eventBus.subscribe(this, EventTopics.SEARCH_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetFilterAddUpdateLayout.class)
        private void onSearchFilterChanged(final String newFilter) {
            targetFilterDetailsLayout.onSearchFilterChanged(newFilter);
        }
    }

    private class CloseDetailsListener {
        public CloseDetailsListener() {
            eventBus.subscribe(this, EventTopics.CHANGE_UI_ELEMENT_STATE);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetFilterDetailsGridHeader.class)
        private void onFilterQuery(final ChangeUiElementPayload payload) {
            targetFilterDetailsLayout.onClose();
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
