package org.eclipse.hawkbit.ui.rollout.rollout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.RolloutModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SearchFilterEventPayload;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.push.RolloutChangedEventContainer;
import org.eclipse.hawkbit.ui.push.RolloutCreatedEventContainer;
import org.eclipse.hawkbit.ui.push.RolloutDeletedEventContainer;
import org.eclipse.hawkbit.ui.push.event.RolloutChangedEvent;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

/**
 * Listener for events directed to the {@link RolloutGridLayout} view
 *
 */
public class RolloutGridLayoutEventListener {
    private final RolloutGridLayout rolloutGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    /**
     * Constructor
     * 
     * @param rolloutGridLayout
     *            The element that is called when receiving an event
     * @param eventBus
     *            The bus to listen on
     */
    public RolloutGridLayoutEventListener(final RolloutGridLayout rolloutGridLayout, final UIEventBus eventBus) {
        this.rolloutGridLayout = rolloutGridLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new SearchFilterListener());
        eventListeners.add(new RolloutModifiedListener());
        eventListeners.add(new RolloutChanhedOnBackendListener());
    }

    private class SearchFilterListener {
        public SearchFilterListener() {
            eventBus.subscribe(this, EventTopics.SEARCH_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onSearchFilter(final SearchFilterEventPayload payload) {
            if (payload.getView() != View.ROLLOUT || payload.getLayout() != rolloutGridLayout.getLayout()) {
                return;
            }

            rolloutGridLayout.filterGridByName(payload.getFilter());
        }
    }

    private class RolloutModifiedListener {
        public RolloutModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onRolloutCreated(final RolloutModifiedEventPayload payload) {
            final EntityModifiedEventType modificationType = payload.getEntityModifiedEventType();
            if (modificationType == EntityModifiedEventType.ENTITY_ADDED
                    || modificationType == EntityModifiedEventType.ENTITY_REMOVED) {
                rolloutGridLayout.refreshGrid();
            } else if (modificationType == EntityModifiedEventType.ENTITY_UPDATED) {
                rolloutGridLayout.refreshGridItems(payload.getEntityIds());
            }
        }
    }

    private class RolloutChanhedOnBackendListener {
        public RolloutChanhedOnBackendListener() {
            eventBus.subscribe(this, EventTopics.REMOTE_EVENT_RECEIVED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onRolloutChanged(final RolloutChangedEventContainer payload) {
            final List<Long> ids = payload.getEvents().stream().map(RolloutChangedEvent::getRolloutId)
                    .collect(Collectors.toList());
            rolloutGridLayout.refreshGridItems(ids);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onRolloutCreated(final RolloutCreatedEventContainer payload) {
            rolloutGridLayout.refreshGrid();
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onRolloutDeleted(final RolloutDeletedEventContainer payload) {
            rolloutGridLayout.refreshGrid();
        }
    }

    /**
     * unsubscribe all listeners
     */
    public void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }

}
