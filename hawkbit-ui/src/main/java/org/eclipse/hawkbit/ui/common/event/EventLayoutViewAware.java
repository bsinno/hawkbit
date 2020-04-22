/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class EventLayoutViewAware extends EventViewAware {
    private final EventLayout layout;

    public EventLayoutViewAware(final EventLayout layout, final EventView view) {
        super(view);

        this.layout = layout;
    }

    public EventLayoutViewAware(final EventLayoutViewAware layoutViewAware) {
        super(layoutViewAware);

        this.layout = layoutViewAware.getLayout();
    }

    public boolean suitableLayout(final EventLayout layout) {
        return this.layout != null && layout != null && this.layout == layout;
    }

    public boolean suitableLayout(final EventLayoutViewAware layoutAware) {
        return suitableLayout(layoutAware.getLayout());
    }

    public boolean suitableViewLayout(final EventLayout layout, final EventView view) {
        return suitableView(view) && suitableLayout(layout);
    }

    public boolean suitableViewLayout(final EventLayoutViewAware layoutAware) {
        return suitableViewLayout(layoutAware.getLayout(), layoutAware.getView());
    }

    public EventLayout getLayout() {
        return layout;
    }
}
