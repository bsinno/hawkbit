/**
 * Copyright (c) 2020 Bosch.IO GmbH, Germany. All rights reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rolloutgroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.push.RolloutGroupChangedEventContainer;
import org.eclipse.hawkbit.ui.push.event.RolloutGroupChangedEvent;
import org.eclipse.hawkbit.ui.common.event.ShowDetailsEventPayload;
import org.eclipse.hawkbit.ui.common.event.View;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

/**
 * Listener for events directed to the {@link RolloutGroupGridLayout} view
 *
 */
public class RolloutGroupGridLayoutEventListener {
    private final RolloutGroupGridLayout rolloutGroupGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    /**
     * Constructor
     * 
     * @param rolloutGroupGridLayout
     *            The element that is called when receiving an event
     * @param eventBus
     *            The bus to listen on
     */
    public RolloutGroupGridLayoutEventListener(final RolloutGroupGridLayout rolloutGroupGridLayout,
            final UIEventBus eventBus) {
        this.rolloutGroupGridLayout = rolloutGroupGridLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new ShowRolloutGroupsLayoutListener());
        eventListeners.add(new RolloutGroupChanhedListener());
    }

    private class ShowRolloutGroupsLayoutListener {
        public ShowRolloutGroupsLayoutListener() {
            eventBus.subscribe(this, CommandTopics.SHOW_ENTITY_DETAILS_LAYOUT);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onShowDetailsEvent(final ShowDetailsEventPayload eventPayload) {
            if (eventPayload.getView() != View.ROLLOUT || eventPayload.getEntityType() != ProxyRollout.class) {
                return;
            }

            rolloutGroupGridLayout.showGroupsForRollout(eventPayload.getEntityId(), eventPayload.getEntityName());

            eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this, new LayoutVisibilityEventPayload(
                    VisibilityType.SHOW, rolloutGroupGridLayout.getLayout(), View.ROLLOUT));
        }
    }
    
    private class RolloutGroupChanhedListener {
        public RolloutGroupChanhedListener() {
            eventBus.subscribe(this, EventTopics.REMOTE_EVENT_RECEIVED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onRolloutChanged(final RolloutGroupChangedEventContainer container) {
            Long currentRolloutId = rolloutGroupGridLayout.getCurrentParentRolloutId();
            if (currentRolloutId != null) {
                List<Long> idsToUpdate = container.getEvents().stream()
                        .filter(event -> currentRolloutId.equals(event.getRolloutId()))
                        .map(RolloutGroupChangedEvent::getRolloutGroupId).collect(Collectors.toList());
                rolloutGroupGridLayout.refreshGridItems(idsToUpdate);
            }
        }
    }

    /**
     * unsubscribe all listeners
     */
    public void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }

}
