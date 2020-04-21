/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class ViewAware {
    private final View view;

    public ViewAware(final View view) {
        this.view = view;
    }

    public ViewAware(final ViewAware viewAware) {
        this.view = viewAware.getView();
    }

    public boolean suitableView(final View view) {
        return this.view != null && view != null && this.view == view;
    }

    public boolean suitableView(final ViewAware viewAware) {
        return suitableView(viewAware.getView());
    }

    public View getView() {
        return view;
    }
}
