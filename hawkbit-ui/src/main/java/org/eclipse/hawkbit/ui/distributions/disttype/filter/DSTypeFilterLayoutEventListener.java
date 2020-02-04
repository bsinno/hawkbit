/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype.filter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.FilterButtonsActionsChangedEventPayload;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class DSTypeFilterLayoutEventListener {
    private final DSTypeFilterLayout dSTypeFilterLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    DSTypeFilterLayoutEventListener(final DSTypeFilterLayout dSTypeFilterLayout, final UIEventBus eventBus) {
        this.dSTypeFilterLayout = dSTypeFilterLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new FilterButtonsActionsChangedListener());
        eventListeners.add(new EntityModifiedListener());
    }

    private class FilterButtonsActionsChangedListener {

        public FilterButtonsActionsChangedListener() {
            eventBus.subscribe(this, EventTopics.FILTER_BUTTONS_ACTIONS_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = DSTypeFilterHeader.class)
        private void onDsTypeEvent(final FilterButtonsActionsChangedEventPayload eventPayload) {
            if (eventPayload == FilterButtonsActionsChangedEventPayload.HIDE_ALL) {
                dSTypeFilterLayout.hideFilterButtonsActionIcons();
            } else if (eventPayload == FilterButtonsActionsChangedEventPayload.SHOW_EDIT) {
                dSTypeFilterLayout.showFilterButtonsEditIcon();
            } else if (eventPayload == FilterButtonsActionsChangedEventPayload.SHOW_DELETE) {
                dSTypeFilterLayout.showFilterButtonsDeleteIcon();
            }
        }
    }

    private class EntityModifiedListener {

        public EntityModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsTypeEvent(final EntityModifiedEventPayload eventPayload) {
            if (!ProxyDistributionSet.class.equals(eventPayload.getParentType())
                    || !ProxyType.class.equals(eventPayload.getEntityType())) {
                return;
            }

            dSTypeFilterLayout.refreshFilterButtons();
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
