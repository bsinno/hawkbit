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

public class DsModifiedEventPayload extends EntityModifiedEventPayload {

    public DsModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType) {
        super(entityModifiedEventType);
    }

    public DsModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType, final Long entityId) {
        super(entityModifiedEventType, entityId);
    }

    public DsModifiedEventPayload(final EntityModifiedEventType entityModifiedEventType,
            final Collection<Long> entityIds) {
        super(entityModifiedEventType, entityIds);
    }
}
