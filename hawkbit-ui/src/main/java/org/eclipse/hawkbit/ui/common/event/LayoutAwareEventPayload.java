/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public abstract class LayoutAwareEventPayload extends ViewAwareEventPayload {
    private final Layout layout;

    protected LayoutAwareEventPayload(final Layout layout, final View view) {
        super(view);

        this.layout = layout;
    }

    public Layout getLayout() {
        return layout;
    }
}
