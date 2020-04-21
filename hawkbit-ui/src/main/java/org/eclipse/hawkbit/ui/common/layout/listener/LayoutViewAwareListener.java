/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener;

import java.util.Collection;

import org.eclipse.hawkbit.ui.common.event.LayoutViewAware;
import org.vaadin.spring.events.EventBus.UIEventBus;

public abstract class LayoutViewAwareListener extends EventListener {
    private final LayoutViewAware layoutViewAware;

    public LayoutViewAwareListener(final UIEventBus eventBus, final String topic,
            final LayoutViewAware layoutViewAware) {
        super(eventBus, topic);

        this.layoutViewAware = layoutViewAware;
    }

    public LayoutViewAwareListener(final UIEventBus eventBus, final Collection<String> topics,
            final LayoutViewAware layoutViewAware) {
        super(eventBus, topics);

        this.layoutViewAware = layoutViewAware;
    }

    public LayoutViewAware getLayoutViewAware() {
        return layoutViewAware;
    }
}
