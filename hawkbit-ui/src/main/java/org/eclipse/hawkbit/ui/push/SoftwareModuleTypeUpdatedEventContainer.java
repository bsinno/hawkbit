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

import org.eclipse.hawkbit.repository.event.remote.entity.SoftwareModuleTypeUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.SoftwareModuleUpdatedEvent;

/**
 * EventHolder for {@link SoftwareModuleUpdatedEvent}s.
 *
 */
public class SoftwareModuleTypeUpdatedEventContainer implements EventContainer<SoftwareModuleTypeUpdatedEvent> {

    private final List<SoftwareModuleTypeUpdatedEvent> events;

    SoftwareModuleTypeUpdatedEventContainer(final List<SoftwareModuleTypeUpdatedEvent> events) {
        this.events = events;
    }

    @Override
    public List<SoftwareModuleTypeUpdatedEvent> getEvents() {
        return events;
    }

}
