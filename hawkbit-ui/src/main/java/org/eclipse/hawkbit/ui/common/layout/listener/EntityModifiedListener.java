/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

public class EntityModifiedListener<T extends ProxyIdentifiableEntity> implements EventListener {
    private final UIEventBus eventBus;
    private final Runnable refreshGridCallback;
    private final List<EntityModifiedAwareSupport> entityModifiedAwareSupports;
    private final Class<T> entityType;
    private final Class<? extends ProxyIdentifiableEntity> parentEntityType;
    private final Supplier<Optional<Long>> parentEntityIdProvider;

    public EntityModifiedListener(final UIEventBus eventBus, final Runnable refreshGridCallback,
            final Class<T> entityType) {
        this(eventBus, refreshGridCallback, Collections.emptyList(), entityType);
    }

    public EntityModifiedListener(final UIEventBus eventBus, final Runnable refreshGridCallback,
            final Class<T> entityType, final Class<? extends ProxyIdentifiableEntity> parentEntityType) {
        this(eventBus, refreshGridCallback, Collections.emptyList(), entityType, parentEntityType, null);
    }

    public EntityModifiedListener(final UIEventBus eventBus, final Runnable refreshGridCallback,
            final List<EntityModifiedAwareSupport> entityModifiedAwareSupports, final Class<T> entityType) {
        this(eventBus, refreshGridCallback, entityModifiedAwareSupports, entityType, null, null);
    }

    public EntityModifiedListener(final UIEventBus eventBus, final Runnable refreshGridCallback,
            final List<EntityModifiedAwareSupport> entityModifiedAwareSupports, final Class<T> entityType,
            final Class<? extends ProxyIdentifiableEntity> parentEntityType) {
        this(eventBus, refreshGridCallback, entityModifiedAwareSupports, entityType, parentEntityType, null);
    }

    public EntityModifiedListener(final UIEventBus eventBus, final Runnable refreshGridCallback,
            final List<EntityModifiedAwareSupport> entityModifiedAwareSupports, final Class<T> entityType,
            final Class<? extends ProxyIdentifiableEntity> parentEntityType,
            final Supplier<Optional<Long>> parentEntityIdProvider) {
        this.eventBus = eventBus;
        this.refreshGridCallback = refreshGridCallback;
        this.entityModifiedAwareSupports = entityModifiedAwareSupports;
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

        if (CollectionUtils.isEmpty(entityModifiedAwareSupports)) {
            return;
        }

        switch (eventType) {
        case ENTITY_ADDED:
            handleEntitiesModified(support -> support.onEntitiesAdded(entityIds));
            break;
        case ENTITY_UPDATED:
            handleEntitiesModified(support -> support.onEntitiesUpdated(entityIds));
            break;
        case ENTITY_REMOVED:
            handleEntitiesModified(support -> support.onEntitiesDeleted(entityIds));
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

    private void handleEntitiesModified(final Consumer<EntityModifiedAwareSupport> handler) {
        UI.getCurrent().access(() -> entityModifiedAwareSupports.forEach(handler::accept));
    }

    @Override
    public void unsubscribe() {
        eventBus.unsubscribe(this);
    }

    public interface EntityModifiedAwareSupport {
        default void onEntitiesAdded(final Collection<Long> entityIds) {
            // do nothing by default
        }

        default void onEntitiesUpdated(final Collection<Long> entityIds) {
            // do nothing by default
        }

        default void onEntitiesDeleted(final Collection<Long> entityIds) {
            // do nothing by default
        }
    }
}
