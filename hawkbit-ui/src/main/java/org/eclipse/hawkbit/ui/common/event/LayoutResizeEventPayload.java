/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class LayoutResizeEventPayload {
    private final ResizeType resizeType;
    private final Layout layout;
    private final View view;

    public LayoutResizeEventPayload(final ResizeType resizeType, final Layout layout, final View view) {
        this.resizeType = resizeType;
        this.layout = layout;
        this.view = view;
    }

    public ResizeType getResizeType() {
        return resizeType;
    }

    public Layout getLayout() {
        return layout;
    }

    public View getView() {
        return view;
    }

    public enum ResizeType {
        MINIMIZE, MAXIMIZE;
    }
}
