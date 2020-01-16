package org.eclipse.hawkbit.ui.rollout.event;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.rollout.rollout.RolloutGridLayout;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

/**
 * Listener for events directed to the {@link RolloutGridLayout} view
 *
 */
public class RolloutsLayoutEventListener {
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
    public RolloutsLayoutEventListener(final RolloutGridLayout rolloutGridLayout, final UIEventBus eventBus) {
        this.rolloutGridLayout = rolloutGridLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new RolloutModifiedListener());
    }

    private class RolloutModifiedListener {
        public RolloutModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onRolloutModified(final RolloutModifiedListener payload) {
            rolloutGridLayout.refreshGrid();
        }
    }

    // TODO searchEvent

    /**
     * unsubscribe all listeners
     */
    public void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }

}
