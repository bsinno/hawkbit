/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement.event;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.filtermanagement.FilterManagementView;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class FilterManagementViewEventListener {
    private final FilterManagementView filterManagementView;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    public FilterManagementViewEventListener(final FilterManagementView filterManagementView,
            final UIEventBus eventBus) {
        this.filterManagementView = filterManagementView;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new LayoutVisibilityListener());
    }

    private class LayoutVisibilityListener {
        public LayoutVisibilityListener() {
            eventBus.subscribe(this, CommandTopics.CHANGE_LAYOUT_VISIBILITY);
        }

        final EnumSet<EventLayout> availableLayouts = EnumSet.of(EventLayout.TARGET_FILTER_QUERY_LIST,
                EventLayout.TARGET_FILTER_QUERY_FORM);

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onLayoutVisibilityEvent(final LayoutVisibilityEventPayload eventPayload) {
            if (eventPayload.getView() != EventView.TARGET_FILTER || !availableLayouts.contains(eventPayload.getLayout())) {
                return;
            }

            final EventLayout changedLayout = eventPayload.getLayout();
            final VisibilityType visibilityType = eventPayload.getVisibilityType();

            if ((changedLayout == EventLayout.TARGET_FILTER_QUERY_LIST && visibilityType == VisibilityType.SHOW)
                    || (changedLayout == EventLayout.TARGET_FILTER_QUERY_FORM && visibilityType == VisibilityType.HIDE)) {
                filterManagementView.showFilterGridLayout();
            } else if ((eventPayload.getLayout() == EventLayout.TARGET_FILTER_QUERY_LIST
                    && eventPayload.getVisibilityType() == VisibilityType.HIDE)
                    || (eventPayload.getLayout() == EventLayout.TARGET_FILTER_QUERY_FORM
                            && eventPayload.getVisibilityType() == VisibilityType.SHOW)) {
                filterManagementView.showFilterDetailsLayout();
            }
        }
    }

    public void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
