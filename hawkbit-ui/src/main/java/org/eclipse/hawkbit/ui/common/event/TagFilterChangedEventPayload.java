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

public class TagFilterChangedEventPayload extends LayoutAwareEventPayload {

    private final Collection<String> tagNames;

    public TagFilterChangedEventPayload(final Collection<String> tagNames, final Layout layout, final View view) {
        super(layout, view);

        this.tagNames = tagNames;
    }

    public Collection<String> getTagNames() {
        return tagNames;
    }
}
