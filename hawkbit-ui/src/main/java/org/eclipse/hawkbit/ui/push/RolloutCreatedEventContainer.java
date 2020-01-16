package org.eclipse.hawkbit.ui.push;

import java.util.List;

import org.eclipse.hawkbit.repository.event.remote.entity.RolloutCreatedEvent;
import org.eclipse.hawkbit.ui.push.event.RolloutChangedEvent;

/**
 * EventHolder for {@link RolloutChangedEvent}s.
 *
 */
public class RolloutCreatedEventContainer implements EventContainer<RolloutCreatedEvent> {
    private static final String I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE = "rollout.created.event.container.notifcation.message";

    private final List<RolloutCreatedEvent> events;

    RolloutCreatedEventContainer(final List<RolloutCreatedEvent> events) {
        this.events = events;
    }

    @Override
    public List<RolloutCreatedEvent> getEvents() {
        return events;
    }

    @Override
    public String getUnreadNotificationMessageKey() {
        return I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE;
    }
}
