/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class SelectionChangedEventPayload<T> extends EventLayoutViewAware {

    private final SelectionChangedEventType selectionChangedEventType;
    private final T entity;

    public SelectionChangedEventPayload(final SelectionChangedEventType selectionChangedEventType, final T entity,
            final EventLayout layout, final EventView view) {
        super(layout, view);

        this.selectionChangedEventType = selectionChangedEventType;
        this.entity = entity;
    }

    public SelectionChangedEventType getSelectionChangedEventType() {
        return selectionChangedEventType;
    }

    public T getEntity() {
        return entity;
    }

    public enum SelectionChangedEventType {
        ENTITY_SELECTED, ENTITY_DESELECTED;
    }
}
