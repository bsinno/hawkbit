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
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class LayoutVisibilityListener extends ViewAwareListener {
    private final Map<EventLayout, Consumer<Boolean>> layoutVisibilityHandlers;

    public LayoutVisibilityListener(final UIEventBus eventBus, final EventViewAware viewAware,
            final Map<EventLayout, Consumer<Boolean>> layoutVisibilityHandlers) {
        super(eventBus, CommandTopics.CHANGE_LAYOUT_VISIBILITY, viewAware);

        this.layoutVisibilityHandlers = layoutVisibilityHandlers;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onSelectionChangedEvent(final LayoutVisibilityEventPayload eventPayload) {
        if (!getViewAware().suitableView(eventPayload)
                || !layoutVisibilityHandlers.keySet().contains(eventPayload.getLayout())) {
            return;
        }

        layoutVisibilityHandlers.get(eventPayload.getLayout())
                .accept(VisibilityType.SHOW == eventPayload.getVisibilityType());
    }
}