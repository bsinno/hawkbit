/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

public class EntityModifiedListener<T extends ProxyIdentifiableEntity> implements EventListener {
    private final UIEventBus eventBus;
    private final Runnable refreshGridCallback;
    private final SelectionSupport<T> selectionSupport;
    private final Class<T> entityType;
    private final Class<? extends ProxyIdentifiableEntity> parentEntityType;
    private final Supplier<Optional<Long>> parentEntityIdProvider;

    public EntityModifiedListener(final UIEventBus eventBus, final Runnable refreshGridCallback,
            final SelectionSupport<T> selectionSupport, final Class<T> entityType) {
        this(eventBus, refreshGridCallback, selectionSupport, entityType, null, null);
    }

    public EntityModifiedListener(final UIEventBus eventBus, final Runnable refreshGridCallback,
            final SelectionSupport<T> selectionSupport, final Class<T> entityType,
            final Class<? extends ProxyIdentifiableEntity> parentEntityType,
            final Supplier<Optional<Long>> parentEntityIdProvider) {
        this.eventBus = eventBus;
        this.refreshGridCallback = refreshGridCallback;
        this.selectionSupport = selectionSupport;
        this.entityType = entityType;
        this.parentEntityType = parentEntityType;
        this.parentEntityIdProvider = parentEntityIdProvider;

        eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onEntityModifiedEvent(final EntityModifiedEventPayload eventPayload) {
        if (!suitableEntityType(eventPayload.getEntityType())
                || !suitableParentEntity(eventPayload.getParentType(), eventPayload.getParentId())) {
            return;
        }

        final EntityModifiedEventType eventType = eventPayload.getEntityModifiedEventType();
        final Collection<Long> entityIds = eventPayload.getEntityIds();

        refreshGridCallback.run();

        switch (eventType) {
        case ENTITY_ADDED:
            UI.getCurrent().access(() -> onEntityAdded(entityIds));
            break;
        case ENTITY_UPDATED:
            UI.getCurrent().access(() -> onEntityUpdated(entityIds));
            break;
        case ENTITY_REMOVED:
            UI.getCurrent().access(() -> onEntityRemoved(entityIds));
            break;
        }
    }

    private boolean suitableEntityType(final Class<? extends ProxyIdentifiableEntity> type) {
        return entityType != null && entityType.equals(type);
    }

    private boolean suitableParentEntity(final Class<? extends ProxyIdentifiableEntity> parentType,
            final Long parentId) {
        return suitableParentEntityType(parentType) && suitableParentEntityId(parentId);
    }

    private boolean suitableParentEntityType(final Class<? extends ProxyIdentifiableEntity> parentType) {
        // parent type is optional
        return parentEntityType == null || parentEntityType.equals(parentType);
    }

    private boolean suitableParentEntityId(final Long parentId) {
        // parent id is optional
        return parentEntityIdProvider == null
                || parentEntityIdProvider.get().map(id -> id.equals(parentId)).orElse(false);
    }

    private void onEntityAdded(final Collection<Long> entityIds) {
        if (selectionSupport == null || selectionSupport.isNoSelectionModel()) {
            return;
        }

        if (entityIds.size() == 1) {
            // we always select newly added item
            selectionSupport.selectEntityById(entityIds.iterator().next());
        }
    }

    private void onEntityUpdated(final Collection<Long> entityIds) {
        if (selectionSupport == null || selectionSupport.isNoSelectionModel()) {
            return;
        }

        selectionSupport.getSelectedEntityId().ifPresent(selectedEntityId -> {
            if (!entityIds.contains(selectedEntityId)) {
                return;
            }

            // we load the up-to-date version of selected entity from
            // database and reselect it, so that master-aware components
            // could update itself
            selectionSupport.sendSelectionChangedEvent(SelectionChangedEventType.ENTITY_SELECTED, selectedEntityId);
        });
    }

    private void onEntityRemoved(final Collection<Long> entityIds) {
        if (selectionSupport == null || selectionSupport.isNoSelectionModel()) {
            return;
        }

        selectionSupport.getSelectedEntityId().ifPresent(selectedEntityId -> {
            if (!entityIds.contains(selectedEntityId)) {
                return;
            }

            // we need to update the master-aware components, that the
            // master entity was deselected after deletion
            selectionSupport.sendSelectionChangedEvent(SelectionChangedEventType.ENTITY_DESELECTED,
                    selectionSupport.getSelectedEntity().orElse(null));
        });
    }

    @Override
    public void unsubscribe() {
        eventBus.unsubscribe(this);
    }
}
