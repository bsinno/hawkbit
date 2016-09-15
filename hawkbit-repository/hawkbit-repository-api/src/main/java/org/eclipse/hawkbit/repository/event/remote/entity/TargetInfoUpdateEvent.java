/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.event.remote.entity;

import org.eclipse.hawkbit.repository.event.remote.json.GenericEventEntity;
import org.eclipse.hawkbit.repository.model.TargetInfo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event for update the targets info.
 */
public class TargetInfoUpdateEvent extends BaseEntityEvent<TargetInfo, Long> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for json serialization.
     * 
     * @param entitySource
     *            the entity source within the json entity information
     * @param tenant
     *            the tenant
     * @param applicationId
     *            the origin application id
     */
    @JsonCreator
    protected TargetInfoUpdateEvent(@JsonProperty("entitySource") final GenericEventEntity<Long> entitySource,
            @JsonProperty("tenant") final String tenant, @JsonProperty("originService") final String applicationId) {
        super(entitySource, tenant, applicationId);
    }

    /**
     * Constructor.
     * 
     * @param targetInfo
     *            the target info
     * @param applicationId
     *            the origin application id
     */
    public TargetInfoUpdateEvent(final TargetInfo targetInfo, final String applicationId) {
        super(targetInfo.getTarget().getTenant(), targetInfo.getTarget().getId(), targetInfo, applicationId);
    }

}