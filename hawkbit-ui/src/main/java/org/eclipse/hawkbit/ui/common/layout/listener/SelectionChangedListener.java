/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener;

import java.util.List;
import java.util.Objects;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class SelectionChangedListener<T extends ProxyIdentifiableEntity> implements EventListener {
    private final UIEventBus eventBus;
    private final List<MasterEntityAwareComponent<T>> masterEntityAwareComponents;
    private final View masterEntityView;
    private final Layout masterEntityLayout;

    public SelectionChangedListener(final UIEventBus eventBus,
            final List<MasterEntityAwareComponent<T>> masterEntityAwareComponents, final View masterEntityView,
            final Layout masterEntityLayout) {
        this.eventBus = eventBus;
        this.masterEntityAwareComponents = masterEntityAwareComponents;
        this.masterEntityView = masterEntityView;
        this.masterEntityLayout = masterEntityLayout;

        eventBus.subscribe(this, EventTopics.SELECTION_CHANGED);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onSelectionChangedEvent(final SelectionChangedEventPayload<T> eventPayload) {
        if (eventPayload.getView() != masterEntityView || eventPayload.getLayout() != masterEntityLayout) {
            return;
        }

        if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
            onSelectionChanged(eventPayload.getEntity());
        } else {
            onSelectionChanged(null);
        }
    }

    private void onSelectionChanged(final T entity) {
        if (CollectionUtils.isEmpty(masterEntityAwareComponents)) {
            return;
        }

        masterEntityAwareComponents.stream().filter(Objects::nonNull)
                .forEach(component -> component.masterEntityChanged(entity));
    }

    @Override
    public void unsubscribe() {
        eventBus.unsubscribe(this);
    }
}
