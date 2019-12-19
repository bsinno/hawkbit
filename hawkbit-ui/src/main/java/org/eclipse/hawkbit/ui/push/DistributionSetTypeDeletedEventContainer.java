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

import org.eclipse.hawkbit.repository.event.remote.DistributionSetTypeDeletedEvent;

/**
 * EventHolder for {@link DistributionSetTypeDeletedEvent}s.
 *
 */
public class DistributionSetTypeDeletedEventContainer implements EventContainer<DistributionSetTypeDeletedEvent> {
    private static final String I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE = "distribution.set.type.deleted.event.container.notifcation.message";
    private final List<DistributionSetTypeDeletedEvent> events;

    DistributionSetTypeDeletedEventContainer(final List<DistributionSetTypeDeletedEvent> events) {
        this.events = events;
    }

    @Override
    public List<DistributionSetTypeDeletedEvent> getEvents() {
        return events;
    }

    @Override
    public String getUnreadNotificationMessageKey() {
        return I18N_UNREAD_NOTIFICATION_UNREAD_MESSAGE;
    }

}
