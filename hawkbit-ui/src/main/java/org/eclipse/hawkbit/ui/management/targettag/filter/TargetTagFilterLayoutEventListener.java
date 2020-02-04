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

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.FilterButtonsActionsChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.TargetFilterTabChangedEventPayload;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class TargetTagFilterLayoutEventListener {
    private final TargetTagFilterLayout targetTagFilterLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    TargetTagFilterLayoutEventListener(final TargetTagFilterLayout targetTagFilterLayout, final UIEventBus eventBus) {
        this.targetTagFilterLayout = targetTagFilterLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new FilterButtonsActionsChangedListener());
        eventListeners.add(new TabChangedListener());
        eventListeners.add(new EntityModifiedListener());
    }

    private class FilterButtonsActionsChangedListener {

        public FilterButtonsActionsChangedListener() {
            eventBus.subscribe(this, EventTopics.FILTER_BUTTONS_ACTIONS_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetTagFilterHeader.class)
        private void onTargetTagEvent(final FilterButtonsActionsChangedEventPayload eventPayload) {
            if (eventPayload == FilterButtonsActionsChangedEventPayload.HIDE_ALL) {
                targetTagFilterLayout.hideFilterButtonsActionIcons();
            } else if (eventPayload == FilterButtonsActionsChangedEventPayload.SHOW_EDIT) {
                targetTagFilterLayout.showFilterButtonsEditIcon();
            } else if (eventPayload == FilterButtonsActionsChangedEventPayload.SHOW_DELETE) {
                targetTagFilterLayout.showFilterButtonsDeleteIcon();
            }
        }
    }

    private class TabChangedListener {

        public TabChangedListener() {
            eventBus.subscribe(this, EventTopics.TARGET_FILTER_TAB_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTagChangedEvent(final TargetFilterTabChangedEventPayload eventPayload) {
            targetTagFilterLayout.onTargetFilterTabChanged(TargetFilterTabChangedEventPayload.CUSTOM == eventPayload);
        }
    }

    private class EntityModifiedListener {

        public EntityModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetTagEvent(final EntityModifiedEventPayload eventPayload) {
            if (!ProxyTarget.class.equals(eventPayload.getParentType())
                    || !ProxyTag.class.equals(eventPayload.getEntityType())) {
                return;
            }

            targetTagFilterLayout.refreshFilterButtons();
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetFilterEvent(final EntityModifiedEventPayload eventPayload) {
            if (!ProxyTargetFilterQuery.class.equals(eventPayload.getEntityType())) {
                return;
            }

            targetTagFilterLayout.refreshFilterButtons();
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
