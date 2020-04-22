/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class EventViewAware {
    private final EventView view;

    public EventViewAware(final EventView view) {
        this.view = view;
    }

    public EventViewAware(final EventViewAware viewAware) {
        this.view = viewAware.getView();
    }

    public boolean suitableView(final EventView view) {
        return this.view != null && view != null && this.view == view;
    }

    public boolean suitableView(final EventViewAware viewAware) {
        return suitableView(viewAware.getView());
    }

    public EventView getView() {
        return view;
    }
}
