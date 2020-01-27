/**
 * Copyright (c) 2020 Bosch.IO GmbH, Germany. All rights reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.push;

import java.util.List;

import org.eclipse.hawkbit.repository.event.remote.entity.RolloutCreatedEvent;

/**
 * EventHolder for {@link RolloutCreatedEvent}s.
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
