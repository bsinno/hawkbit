/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class LayoutViewAware extends ViewAware {
    private final Layout layout;

    public LayoutViewAware(final Layout layout, final View view) {
        super(view);

        this.layout = layout;
    }

    public LayoutViewAware(final LayoutViewAware layoutViewAware) {
        super(layoutViewAware);

        this.layout = layoutViewAware.getLayout();
    }

    public boolean suitableLayout(final Layout layout) {
        return this.layout != null && layout != null && this.layout == layout;
    }

    public boolean suitableLayout(final LayoutViewAware layoutAware) {
        return suitableLayout(layoutAware.getLayout());
    }

    public boolean suitableViewLayout(final Layout layout, final View view) {
        return suitableView(view) && suitableLayout(layout);
    }

    public boolean suitableViewLayout(final LayoutViewAware layoutAware) {
        return suitableViewLayout(layoutAware.getLayout(), layoutAware.getView());
    }

    public Layout getLayout() {
        return layout;
    }
}
