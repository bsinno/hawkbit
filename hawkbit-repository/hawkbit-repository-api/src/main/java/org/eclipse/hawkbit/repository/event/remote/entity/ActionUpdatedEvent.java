/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.event.remote.entity;

import org.eclipse.hawkbit.repository.model.Action;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines the remote event of updated a {@link Action}.
 */
public class ActionUpdatedEvent extends RemoteEntityEvent<Action> {
    private static final long serialVersionUID = 181780358321768629L;

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
    protected ActionUpdatedEvent(@JsonProperty("tenant") final String tenant,
            @JsonProperty("entityId") final Long entityId,
            @JsonProperty("entityClass") final Class<? extends Action> entityClass,
            @JsonProperty("originService") final String applicationId) {
        super(tenant, entityId, entityClass, applicationId);
    }

    /**
     * Constructor
     * 
     * @param action
     *            the updated action
     * @param applicationId
     *            the origin application id
     */
    public ActionUpdatedEvent(final Action action, final String applicationId) {
        super(action, applicationId);
    }

}