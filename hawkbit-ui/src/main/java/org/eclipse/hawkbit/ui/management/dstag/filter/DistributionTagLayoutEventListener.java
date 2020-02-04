/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag.filter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.ActionsVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.ActionsVisibilityEventPayload.ActionsVisibilityType;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.View;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class DistributionTagLayoutEventListener {
    private final DistributionTagLayout distributionTagLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    DistributionTagLayoutEventListener(final DistributionTagLayout distributionTagLayout, final UIEventBus eventBus) {
        this.distributionTagLayout = distributionTagLayout;
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
            eventBus.subscribe(this, CommandTopics.CHANGE_ACTIONS_VISIBILITY);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onActionsVisibilityEvent(final ActionsVisibilityEventPayload eventPayload) {
            if (eventPayload.getView() != View.DEPLOYMENT
                    || eventPayload.getLayout() != distributionTagLayout.getLayout()) {
                return;
            }

            final ActionsVisibilityType actionsVisibilityType = eventPayload.getActionsVisibilityType();

            if (actionsVisibilityType == ActionsVisibilityType.HIDE_ALL) {
                distributionTagLayout.hideFilterButtonsActionIcons();
            } else if (actionsVisibilityType == ActionsVisibilityType.SHOW_EDIT) {
                distributionTagLayout.showFilterButtonsEditIcon();
            } else if (actionsVisibilityType == ActionsVisibilityType.SHOW_DELETE) {
                distributionTagLayout.showFilterButtonsDeleteIcon();
            }
        }
    }

    private class EntityModifiedListener {

        public EntityModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsTagEvent(final EntityModifiedEventPayload eventPayload) {
            if (!ProxyDistributionSet.class.equals(eventPayload.getParentType())
                    || !ProxyTag.class.equals(eventPayload.getEntityType())) {
                return;
            }

            distributionTagLayout.refreshFilterButtons();
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
