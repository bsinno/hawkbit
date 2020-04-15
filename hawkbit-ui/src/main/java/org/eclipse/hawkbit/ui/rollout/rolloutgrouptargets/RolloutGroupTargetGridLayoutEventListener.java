/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rolloutgrouptargets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.ShowEntityDetailsEventPayload;
import org.eclipse.hawkbit.ui.common.event.View;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class RolloutGroupTargetGridLayoutEventListener {
    private final RolloutGroupTargetGridLayout rolloutGroupTargetGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    public RolloutGroupTargetGridLayoutEventListener(final RolloutGroupTargetGridLayout rolloutGroupTargetGridLayout,
            final UIEventBus eventBus) {
        this.rolloutGroupTargetGridLayout = rolloutGroupTargetGridLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new ShowRolloutGroupTargetsLayoutListener());
    }

    private class ShowRolloutGroupTargetsLayoutListener {
        public ShowRolloutGroupTargetsLayoutListener() {
            eventBus.subscribe(this, CommandTopics.SHOW_ENTITY_DETAILS_LAYOUT);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onShowDetailsEvent(final ShowEntityDetailsEventPayload eventPayload) {
            if (eventPayload.getView() != View.ROLLOUT || eventPayload.getEntityType() != ProxyRolloutGroup.class) {
                return;
            }

            rolloutGroupTargetGridLayout.showTargetsForGroup(eventPayload.getEntityId(), eventPayload.getEntityName(),
                    eventPayload.getParentEntityName());

            eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this, new LayoutVisibilityEventPayload(
                    VisibilityType.SHOW, rolloutGroupTargetGridLayout.getLayout(), View.ROLLOUT));
        }
    }

    public void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
