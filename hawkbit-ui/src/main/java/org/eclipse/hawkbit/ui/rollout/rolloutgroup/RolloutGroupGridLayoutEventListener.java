package org.eclipse.hawkbit.ui.rollout.rolloutgroup;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
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
        // TODO: add entityModified listener for group change
    }

    private class ShowRolloutGroupsLayoutListener {
        public ShowRolloutGroupsLayoutListener() {
            eventBus.subscribe(this, CommandTopics.SHOW_ENTITY_DETAILS_LAYOUT);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onShowDetailsEvent(final ShowDetailsEventPayload eventPayload) {
            if (eventPayload.getView() != View.ROLLOUT || eventPayload.getEntityType() != ProxyRolloutGroup.class) {
                return;
            }

            rolloutGroupGridLayout.showGroupsForRollout(eventPayload.getParentEntityId(),
                    eventPayload.getParentEntityName());

            eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this, new LayoutVisibilityEventPayload(
                    VisibilityType.SHOW, rolloutGroupGridLayout.getLayout(), View.ROLLOUT));
        }
    }

    /**
     * unsubscribe all listeners
     */
    public void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }

}
