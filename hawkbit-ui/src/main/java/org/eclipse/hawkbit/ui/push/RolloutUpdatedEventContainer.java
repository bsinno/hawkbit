/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.push;

import java.util.List;

import org.eclipse.hawkbit.repository.event.remote.entity.RolloutUpdatedEvent;
import org.eclipse.hawkbit.ui.push.event.RolloutChangedEvent;

/**
 * EventHolder for {@link RolloutChangedEvent}s.
 *
 */
public class RolloutUpdatedEventContainer implements EventContainer<RolloutUpdatedEvent> {
    private static final String I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE = "rollout.updated.event.container.notifcation.message";

    private final List<RolloutUpdatedEvent> events;

    RolloutUpdatedEventContainer(final List<RolloutUpdatedEvent> events) {
        this.events = events;
    }

    @Override
    public List<RolloutUpdatedEvent> getEvents() {
        return events;
    }

    @Override
    public String getUnreadNotificationMessageKey() {
        return I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE;
    }
}
