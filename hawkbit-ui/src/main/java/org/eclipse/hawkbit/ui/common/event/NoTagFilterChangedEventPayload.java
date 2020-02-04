/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

public class NoTagFilterChangedEventPayload {

    private final Boolean isNoTagActive;
    private final Layout layout;
    private final View view;

    public NoTagFilterChangedEventPayload(final Boolean isNoTagActive, final Layout layout, final View view) {
        this.isNoTagActive = isNoTagActive;
        this.layout = layout;
        this.view = view;
    }

    public Layout getLayout() {
        return layout;
    }

    public View getView() {
        return view;
    }

    public Boolean getIsNoTagActive() {
        return isNoTagActive;
    }
}
