/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.event;

import java.util.Collection;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyArtifact;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.common.table.BaseUIEntityEvent;

/**
 * TenantAwareEvent to represent artifact add or delete.
 *
 */
public class ArtifactDetailsEvent extends BaseUIEntityEvent<ProxyArtifact> {
    /**
     * Creates artifact details event.
     * 
     * @param entityEventType
     *            the event type
     */
    public ArtifactDetailsEvent(final BaseEntityEventType entityEventType) {
        super(entityEventType, null);
    }

    /**
     * Creates artifact details event.
     * 
     * @param entityEventType
     *            the event type
     * @param artifact
     *            the artifact
     */
    public ArtifactDetailsEvent(final BaseEntityEventType entityEventType, final ProxyArtifact artifact) {
        super(entityEventType, artifact);
    }

    /**
     * Constructor
     * 
     * @param eventType
     *            the event type
     * @param entityIds
     *            the entity ids
     */
    public ArtifactDetailsEvent(final BaseEntityEventType eventType, final Collection<Long> entityIds) {
        super(eventType, entityIds, ProxyArtifact.class);
    }
}
