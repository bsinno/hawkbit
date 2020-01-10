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

import org.eclipse.hawkbit.repository.event.remote.entity.TargetFilterQueryCreatedEvent;

/**
 * EventHolder for {@link TargetFilterQueryCreatedEvent}s.
 *
 */
public class TargetFilterQueryCreatedEventContainer implements EventContainer<TargetFilterQueryCreatedEvent> {
    private static final String I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE = "targetfilterquery.created.event.container.notifcation.message";
    private final List<TargetFilterQueryCreatedEvent> events;

    TargetFilterQueryCreatedEventContainer(final List<TargetFilterQueryCreatedEvent> events) {
        this.events = events;
    }

    @Override
    public List<TargetFilterQueryCreatedEvent> getEvents() {
        return events;
    }

    @Override
    public String getUnreadNotificationMessageKey() {
        return I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE;
    }
}
