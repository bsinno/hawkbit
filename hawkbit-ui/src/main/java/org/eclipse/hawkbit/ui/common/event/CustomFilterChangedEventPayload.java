/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class CustomFilterChangedEventPayload {

    private final CustomFilterChangedEventType customFilterChangedEventType;
    private final Long customFilterId;

    public CustomFilterChangedEventPayload(final CustomFilterChangedEventType customFilterChangedEventType,
            final Long customFilterId) {
        this.customFilterChangedEventType = customFilterChangedEventType;
        this.customFilterId = customFilterId;
    }

    public CustomFilterChangedEventType getCustomFilterChangedEventType() {
        return customFilterChangedEventType;
    }

    public Long getCustomFilterId() {
        return customFilterId;
    }

    public enum CustomFilterChangedEventType {
        CLICKED, UNCLICKED;
    }
}
