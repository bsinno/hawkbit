/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtype.filter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.event.ActionsVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.ActionsVisibilityEventPayload.ActionsVisibilityType;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class DistSMTypeFilterLayoutEventListener {
    private final DistSMTypeFilterLayout distSMTypeFilterLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    DistSMTypeFilterLayoutEventListener(final DistSMTypeFilterLayout distSMTypeFilterLayout,
            final UIEventBus eventBus) {
        this.distSMTypeFilterLayout = distSMTypeFilterLayout;
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
            if (eventPayload.getView() != EventView.DISTRIBUTIONS
                    || eventPayload.getLayout() != distSMTypeFilterLayout.getLayout()) {
                return;
            }

            final ActionsVisibilityType actionsVisibilityType = eventPayload.getActionsVisibilityType();

            if (actionsVisibilityType == ActionsVisibilityType.HIDE_ALL) {
                distSMTypeFilterLayout.hideFilterButtonsActionIcons();
            } else if (actionsVisibilityType == ActionsVisibilityType.SHOW_EDIT) {
                distSMTypeFilterLayout.showFilterButtonsEditIcon();
            } else if (actionsVisibilityType == ActionsVisibilityType.SHOW_DELETE) {
                distSMTypeFilterLayout.showFilterButtonsDeleteIcon();
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
