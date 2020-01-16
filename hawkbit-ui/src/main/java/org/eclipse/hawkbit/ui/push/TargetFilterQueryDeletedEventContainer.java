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

import org.eclipse.hawkbit.repository.event.remote.TargetFilterQueryDeletedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetFilterQueryUpdatedEvent;

/**
 * EventHolder for {@link TargetFilterQueryUpdatedEvent}s.
 *
 */
public class TargetFilterQueryDeletedEventContainer implements EventContainer<TargetFilterQueryDeletedEvent> {
    private static final String I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE = "targetfilterquery.deleted.event.container.notifcation.message";
    private final List<TargetFilterQueryDeletedEvent> events;

    TargetFilterQueryDeletedEventContainer(final List<TargetFilterQueryDeletedEvent> events) {
        this.events = events;
    }

    @Override
    public List<TargetFilterQueryDeletedEvent> getEvents() {
        return events;
    }

    @Override
    public String getUnreadNotificationMessageKey() {
        return I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE;
    }
}
