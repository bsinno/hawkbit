/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class LayoutVisibilityEventPayload extends EventLayoutViewAware {
    private final VisibilityType visibilityType;

    public LayoutVisibilityEventPayload(final VisibilityType visibilityType, final EventLayout layout, final EventView view) {
        super(layout, view);

        this.visibilityType = visibilityType;
    }

    public VisibilityType getVisibilityType() {
        return visibilityType;
    }

    public enum VisibilityType {
        SHOW, HIDE;
    }
}
