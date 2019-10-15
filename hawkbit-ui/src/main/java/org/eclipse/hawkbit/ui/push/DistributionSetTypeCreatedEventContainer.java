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

import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetTypeCreatedEvent;

/**
 * EventHolder for {@link DistributionSetTypeCreatedEvent}s.
 *
 */
public class DistributionSetTypeCreatedEventContainer implements EventContainer<DistributionSetTypeCreatedEvent> {
    private static final String I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE = "distribution.set.type.created.event.container.notifcation.message";
    private final List<DistributionSetTypeCreatedEvent> events;

    DistributionSetTypeCreatedEventContainer(final List<DistributionSetTypeCreatedEvent> events) {
        this.events = events;
    }

    @Override
    public List<DistributionSetTypeCreatedEvent> getEvents() {
        return events;
    }

    @Override
    public String getUnreadNotificationMessageKey() {
        return I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE;
    }

}
