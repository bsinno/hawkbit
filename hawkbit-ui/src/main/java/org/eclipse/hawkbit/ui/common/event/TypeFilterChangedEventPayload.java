/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class TypeFilterChangedEventPayload<T> extends LayoutAwareEventPayload {

    private final TypeFilterChangedEventType typeFilterChangedEventType;
    private final T type;

    public TypeFilterChangedEventPayload(final TypeFilterChangedEventType typeFilterChangedEventType, final T type,
            final Layout layout, final View view) {
        super(layout, view);

        this.typeFilterChangedEventType = typeFilterChangedEventType;
        this.type = type;
    }

    public TypeFilterChangedEventType getTypeFilterChangedEventType() {
        return typeFilterChangedEventType;
    }

    public T getType() {
        return type;
    }

    public enum TypeFilterChangedEventType {
        TYPE_CLICKED, TYPE_UNCLICKED;
    }
}
