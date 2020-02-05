/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

import java.util.Collection;

public class TagFilterChangedEventPayload {

    private final Collection<String> tagNames;
    private final Layout layout;
    private final View view;

    public TagFilterChangedEventPayload(final Collection<String> tagNames, final Layout layout, final View view) {
        this.tagNames = tagNames;
        this.layout = layout;
        this.view = view;
    }

    public Collection<String> getTagNames() {
        return tagNames;
    }

    public Layout getLayout() {
        return layout;
    }

    public View getView() {
        return view;
    }
}
