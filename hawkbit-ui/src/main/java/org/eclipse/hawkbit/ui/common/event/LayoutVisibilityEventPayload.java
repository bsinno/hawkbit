/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class LayoutVisibilityEventPayload {
    private final VisibilityType visibilityType;
    private final Layout layout;
    private final View view;

    public LayoutVisibilityEventPayload(final VisibilityType visibilityType, final Layout layout, final View view) {
        this.visibilityType = visibilityType;
        this.layout = layout;
        this.view = view;
    }

    public VisibilityType getVisibilityType() {
        return visibilityType;
    }

    public Layout getLayout() {
        return layout;
    }

    public View getView() {
        return view;
    }

    public enum VisibilityType {
        SHOW, HIDE;
    }
}
