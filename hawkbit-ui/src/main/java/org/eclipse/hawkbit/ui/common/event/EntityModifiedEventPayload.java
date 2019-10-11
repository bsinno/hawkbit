/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

import java.util.Collection;
import java.util.Collections;

//TODO: make it abstract?
public class EntityModifiedEventPayload {

    private final EntityModifiedEventType entityModifiedEventType;
    private final Collection<Long> entityIds;

    public EntityModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType) {
        this(entityModifiedEventType, Collections.emptyList());
    }

    public EntityModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType, final Long entityId) {
        this(entityModifiedEventType, Collections.singletonList(entityId));
    }

    public EntityModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType,
            final Collection<Long> entityIds) {
        this.entityModifiedEventType = entityModifiedEventType;
        this.entityIds = entityIds;
    }

    public EntityModifiedEventType getEntityModifiedEventType() {
        return entityModifiedEventType;
    }

    public Collection<Long> getEntityIds() {
        return entityIds;
    }

    public enum EntityModifiedEventType {
        ENTITY_ADDED, ENTITY_UPDATED, ENTITY_REMOVED;
    }
}
