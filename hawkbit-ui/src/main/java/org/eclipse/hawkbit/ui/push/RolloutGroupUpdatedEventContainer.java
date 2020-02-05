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

import org.eclipse.hawkbit.repository.event.remote.entity.RolloutGroupUpdatedEvent;
import org.eclipse.hawkbit.ui.push.event.RolloutGroupChangedEvent;

/**
 * EventHolder for {@link RolloutGroupChangedEvent}s.
 *
 */
public class RolloutGroupUpdatedEventContainer implements EventContainer<RolloutGroupUpdatedEvent> {
    private final List<RolloutGroupUpdatedEvent> events;

    RolloutGroupUpdatedEventContainer(final List<RolloutGroupUpdatedEvent> events) {
        this.events = events;
    }

    @Override
    public List<RolloutGroupUpdatedEvent> getEvents() {
        return events;
    }

}
