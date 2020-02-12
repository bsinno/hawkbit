/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;

public class PinningChangedEventPayload<T> {

    private final PinningChangedEventType pinningChangedEventType;
    private final Class<? extends ProxyIdentifiableEntity> entityType;
    private final T entityId;

    public PinningChangedEventPayload(final PinningChangedEventType pinningChangedEventType,
            final Class<? extends ProxyIdentifiableEntity> entityType, final T entityId) {
        this.pinningChangedEventType = pinningChangedEventType;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public PinningChangedEventType getPinningChangedEventType() {
        return pinningChangedEventType;
    }

    public Class<? extends ProxyIdentifiableEntity> getEntityType() {
        return entityType;
    }

    public T getEntityId() {
        return entityId;
    }

    public enum PinningChangedEventType {
        ENTITY_PINNED, ENTITY_UNPINNED;
    }
}
