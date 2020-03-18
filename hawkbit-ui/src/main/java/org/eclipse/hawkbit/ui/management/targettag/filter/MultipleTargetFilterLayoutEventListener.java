/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag.filter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.event.ActionsVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.ActionsVisibilityEventPayload.ActionsVisibilityType;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class MultipleTargetFilterLayoutEventListener {
    private final MultipleTargetFilter multipleTargetFilter;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    MultipleTargetFilterLayoutEventListener(final MultipleTargetFilter multipleTargetFilter,
            final UIEventBus eventBus) {
        this.multipleTargetFilter = multipleTargetFilter;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new FilterButtonsActionsChangedListener());
    }

    private class FilterButtonsActionsChangedListener {

        public FilterButtonsActionsChangedListener() {
            eventBus.subscribe(this, CommandTopics.CHANGE_ACTIONS_VISIBILITY);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onActionsVisibilityEvent(final ActionsVisibilityEventPayload eventPayload) {
            if (eventPayload.getView() != multipleTargetFilter.getView()
                    || eventPayload.getLayout() != multipleTargetFilter.getLayout()) {
                return;
            }

            final ActionsVisibilityType actionsVisibilityType = eventPayload.getActionsVisibilityType();

            if (actionsVisibilityType == ActionsVisibilityType.HIDE_ALL) {
                multipleTargetFilter.hideFilterButtonsActionIcons();
            } else if (actionsVisibilityType == ActionsVisibilityType.SHOW_EDIT) {
                multipleTargetFilter.showFilterButtonsEditIcon();
            } else if (actionsVisibilityType == ActionsVisibilityType.SHOW_DELETE) {
                multipleTargetFilter.showFilterButtonsDeleteIcon();
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
