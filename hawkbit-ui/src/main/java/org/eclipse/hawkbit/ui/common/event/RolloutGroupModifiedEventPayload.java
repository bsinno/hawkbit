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

public class RolloutGroupModifiedEventPayload extends EntityModifiedEventPayload {

    private final Long rolloutId;

    public RolloutGroupModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType) {
        super(entityModifiedEventType);

        this.rolloutId = null;
    }

    public RolloutGroupModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType, final Long entityId,
            final Long rolloutId) {
        super(entityModifiedEventType, entityId);

        this.rolloutId = rolloutId;
    }

    public RolloutGroupModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType,
            final Collection<Long> entityIds, final Long rolloutId) {
        super(entityModifiedEventType, entityIds);

        this.rolloutId = rolloutId;
    }

    public Long getRolloutId() {
        return rolloutId;
    }
}
