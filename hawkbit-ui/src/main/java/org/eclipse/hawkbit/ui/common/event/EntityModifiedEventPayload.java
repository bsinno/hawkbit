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

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.push.HawkbitEventProvider.EntityModifiedEventPayloadIdentifier;

public class EntityModifiedEventPayload {

    private final EntityModifiedEventType entityModifiedEventType;
    private final Class<? extends ProxyIdentifiableEntity> parentType;
    private final Long parentId;
    private final Class<? extends ProxyIdentifiableEntity> entityType;
    private final Collection<Long> entityIds;

    public EntityModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType,
            final Class<? extends ProxyIdentifiableEntity> entityType, final Long entityId) {
        this(entityModifiedEventType, entityType, Collections.singletonList(entityId));
    }

    public EntityModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType,
            final Class<? extends ProxyIdentifiableEntity> entityType, final Collection<Long> entityIds) {
        this(entityModifiedEventType, null, null, entityType, entityIds);
    }

    public EntityModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType,
            final Class<? extends ProxyIdentifiableEntity> parentType,
            final Class<? extends ProxyIdentifiableEntity> entityType, final Long entityId) {
        this(entityModifiedEventType, parentType, entityType, Collections.singletonList(entityId));
    }

    public EntityModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType,
            final Class<? extends ProxyIdentifiableEntity> parentType,
            final Class<? extends ProxyIdentifiableEntity> entityType, final Collection<Long> entityIds) {
        this(entityModifiedEventType, parentType, null, entityType, entityIds);
    }

    public EntityModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType,
            final Class<? extends ProxyIdentifiableEntity> parentType, final Long parentId,
            final Class<? extends ProxyIdentifiableEntity> entityType, final Collection<Long> entityIds) {
        this.entityModifiedEventType = entityModifiedEventType;
        this.parentType = parentType;
        this.parentId = parentId;
        this.entityType = entityType;
        this.entityIds = entityIds;
    }

    public EntityModifiedEventType getEntityModifiedEventType() {
        return entityModifiedEventType;
    }

    public Class<? extends ProxyIdentifiableEntity> getParentType() {
        return parentType;
    }

    public Long getParentId() {
        return parentId;
    }

    public Class<? extends ProxyIdentifiableEntity> getEntityType() {
        return entityType;
    }

    public Collection<Long> getEntityIds() {
        return entityIds;
    }

    public static EntityModifiedEventPayload of(final EntityModifiedEventPayloadIdentifier eventPayloadIdentifier,
            final Long parentId, final Collection<Long> entityIds) {
        return new EntityModifiedEventPayload(eventPayloadIdentifier.getModifiedEventType(),
                eventPayloadIdentifier.getParentType(), parentId, eventPayloadIdentifier.getEntityType(), entityIds);
    }

    public static EntityModifiedEventPayload of(final EntityModifiedEventPayloadIdentifier eventPayloadIdentifier,
            final Collection<Long> entityIds) {
        return of(eventPayloadIdentifier, null, entityIds);
    }

    public enum EntityModifiedEventType {
        ENTITY_ADDED, ENTITY_UPDATED, ENTITY_REMOVED;
    }
}
