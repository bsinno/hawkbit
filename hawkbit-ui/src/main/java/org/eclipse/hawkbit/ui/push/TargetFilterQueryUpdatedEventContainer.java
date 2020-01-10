/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.push;

import java.util.List;

import org.eclipse.hawkbit.repository.event.remote.entity.TargetFilterQueryUpdatedEvent;

/**
 * EventHolder for {@link TargetFilterQueryUpdatedEvent}s.
 *
 */
public class TargetFilterQueryUpdatedEventContainer implements EventContainer<TargetFilterQueryUpdatedEvent> {
    private static final String I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE = "targetfilterquery.updated.event.container.notifcation.message";
    private final List<TargetFilterQueryUpdatedEvent> events;

    TargetFilterQueryUpdatedEventContainer(final List<TargetFilterQueryUpdatedEvent> events) {
        this.events = events;
    }

    @Override
    public List<TargetFilterQueryUpdatedEvent> getEvents() {
        return events;
    }

    @Override
    public String getUnreadNotificationMessageKey() {
        return I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE;
    }
}
