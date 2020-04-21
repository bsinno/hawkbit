/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener;

import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.PinningChangedEventPayload.PinningChangedEventType;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class PinningChangedListener<T> extends EventListener {
    private final Class<? extends ProxyIdentifiableEntity> entityType;
    private final Consumer<T> pinningCallback;

    public PinningChangedListener(final UIEventBus eventBus, final Class<? extends ProxyIdentifiableEntity> entityType,
            final Consumer<T> pinningCallback) {
        super(eventBus, EventTopics.PINNING_CHANGED);

        this.entityType = entityType;
        this.pinningCallback = pinningCallback;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onPinEvent(final PinningChangedEventPayload<T> eventPayload) {
        if (!suitableEntityType(eventPayload.getEntityType())) {
            return;
        }

        if (eventPayload.getPinningChangedEventType() == PinningChangedEventType.ENTITY_PINNED) {
            pinningCallback.accept(eventPayload.getEntityId());
        } else {
            pinningCallback.accept(null);
        }
    }

    private boolean suitableEntityType(final Class<? extends ProxyIdentifiableEntity> type) {
        return entityType != null && entityType.equals(type);
    }
}
