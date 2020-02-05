/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class SelectionChangedEventPayload<T> {

    private final SelectionChangedEventType selectionChangedEventType;
    private final T entity;
    private final Layout layout;
    private final View view;

    public SelectionChangedEventPayload(final SelectionChangedEventType selectionChangedEventType, final T entity,
            final Layout layout, final View view) {
        this.selectionChangedEventType = selectionChangedEventType;
        this.entity = entity;
        this.layout = layout;
        this.view = view;
    }

    public SelectionChangedEventType getSelectionChangedEventType() {
        return selectionChangedEventType;
    }

    public T getEntity() {
        return entity;
    }

    public Layout getLayout() {
        return layout;
    }

    public View getView() {
        return view;
    }

    public enum SelectionChangedEventType {
        ENTITY_SELECTED, ENTITY_DESELECTED;
    }
}
