/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener;

import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.LayoutViewAware;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload.TypeFilterChangedEventType;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class TypeFilterListener<T> extends LayoutViewAwareListener {
    private final Consumer<T> typeFilterCallback;

    public TypeFilterListener(final UIEventBus eventBus, final LayoutViewAware layoutViewAware,
            final Consumer<T> typeFilterCallback) {
        super(eventBus, EventTopics.TYPE_FILTER_CHANGED, layoutViewAware);

        this.typeFilterCallback = typeFilterCallback;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onTypeFilter(final TypeFilterChangedEventPayload<T> eventPayload) {
        if (!getLayoutViewAware().suitableViewLayout(eventPayload)) {
            return;
        }

        if (eventPayload.getTypeFilterChangedEventType() == TypeFilterChangedEventType.TYPE_CLICKED) {
            typeFilterCallback.accept(eventPayload.getType());
        } else {
            typeFilterCallback.accept(null);
        }
    }
}
