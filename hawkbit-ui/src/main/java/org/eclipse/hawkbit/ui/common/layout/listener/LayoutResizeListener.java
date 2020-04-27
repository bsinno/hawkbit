/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener;

import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventViewAware;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload.ResizeType;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class LayoutResizeListener extends ViewAwareListener {
    private final Map<EventLayout, Consumer<Boolean>> layoutResizeHandlers;

    public LayoutResizeListener(final UIEventBus eventBus, final EventViewAware viewAware,
            final Map<EventLayout, Consumer<Boolean>> layoutResizeHandlers) {
        super(eventBus, CommandTopics.RESIZE_LAYOUT, viewAware);

        this.layoutResizeHandlers = layoutResizeHandlers;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onSelectionChangedEvent(final LayoutResizeEventPayload eventPayload) {
        if (!getViewAware().suitableView(eventPayload)
                || !layoutResizeHandlers.keySet().contains(eventPayload.getLayout())) {
            return;
        }

        layoutResizeHandlers.get(eventPayload.getLayout()).accept(ResizeType.MAXIMIZE == eventPayload.getResizeType());
    }
}
