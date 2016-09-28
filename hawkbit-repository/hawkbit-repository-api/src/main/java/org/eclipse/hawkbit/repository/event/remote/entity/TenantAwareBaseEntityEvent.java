/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.event.remote.entity;

import org.eclipse.hawkbit.repository.event.EntityEvent;
import org.eclipse.hawkbit.repository.event.remote.EventEntityManagerHolder;
import org.eclipse.hawkbit.repository.model.TenantAwareBaseEntity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A base definition class for {@link EntityEvent} for some object.
 *
 * @param <E>
 *            the type of the entity
 */
public class TenantAwareBaseEntityEvent<E extends TenantAwareBaseEntity> extends BaseEntityIdEvent
        implements EntityEvent {

    private static final long serialVersionUID = 1L;

    @JsonProperty(required = true)
    private Class<? extends E> entityClass;

    @JsonIgnore
    private transient E entity;

    /**
     * Constructor for json serialization.
     * 
     * @param tenant
     *            the tenant
     * @param entityId
     *            the entity id
     * @param entityClass
     *            the entity entityClassName
     * @param applicationId
     *            the origin application id
     */
    @JsonCreator
    protected TenantAwareBaseEntityEvent(@JsonProperty("tenant") final String tenant,
            @JsonProperty("entityId") final Long entityId,
            @JsonProperty("entityClass") final Class<? extends E> entityClass,
            @JsonProperty("originService") final String applicationId) {
        super(entityId, tenant, applicationId);
        this.entityClass = entityClass;
    }

    /**
     * Constructor.
     * 
     * @param baseEntity
     *            the base entity
     * @param applicationId
     *            the origin application id
     */
    @SuppressWarnings("unchecked")
    protected TenantAwareBaseEntityEvent(final E baseEntity, final String applicationId) {
        this(baseEntity.getTenant(), baseEntity.getId(), (Class<? extends E>) baseEntity.getClass(), applicationId);
        this.entity = baseEntity;
    }

    @Override
    @JsonIgnore
    public E getEntity() {
        if (entity == null) {
            entity = EventEntityManagerHolder.getInstance().getEventEntityManager().findEntity(getTenant(),
                    getEntityId(), entityClass);
        }
        return entity;
    }

    @Override
    @JsonIgnore
    public <T> T getEntity(final Class<T> entityClass) {
        return entityClass.cast(entity);
    }

}