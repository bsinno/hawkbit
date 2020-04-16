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
import java.util.function.LongFunction;
import java.util.function.Predicate;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;

public class EntityModifiedSelectionAwareSupport<T extends ProxyIdentifiableEntity>
        implements EntityModifiedAwareSupport {
    private final SelectionSupport<T> selectionSupport;
    private final LongFunction<Optional<T>> getFromBackendCallback;
    private final Predicate<T> isInvalidEntityCallback;

    public EntityModifiedSelectionAwareSupport(final SelectionSupport<T> selectionSupport,
            final LongFunction<Optional<T>> getFromBackendCallback, final Predicate<T> isInvalidEntityCallback) {
        this.selectionSupport = selectionSupport;
        this.getFromBackendCallback = getFromBackendCallback;
        this.isInvalidEntityCallback = isInvalidEntityCallback;
    }

    public static <E extends ProxyIdentifiableEntity> EntityModifiedSelectionAwareSupport<E> of(
            final SelectionSupport<E> selectionSupport, final LongFunction<Optional<E>> getFromBackendCallback) {
        return of(selectionSupport, getFromBackendCallback, null);
    }

    public static <E extends ProxyIdentifiableEntity> EntityModifiedSelectionAwareSupport<E> of(
            final SelectionSupport<E> selectionSupport, final LongFunction<Optional<E>> getFromBackendCallback,
            final Predicate<E> isInvalidEntityCallback) {
        return new EntityModifiedSelectionAwareSupport<>(selectionSupport, getFromBackendCallback,
                isInvalidEntityCallback);
    }

    @Override
    public void onEntitiesAdded(final Collection<Long> entityIds) {
        if (selectionSupport == null || selectionSupport.isNoSelectionModel()) {
            return;
        }

        if (entityIds.size() == 1) {
            // we always select newly added item
            selectionSupport.selectEntityById(entityIds.iterator().next());
        }
    }

    @Override
    public void onEntitiesUpdated(final Collection<Long> entityIds) {
        if (getFromBackendCallback == null) {
            return;
        }

        getModifiedEntityId(entityIds).ifPresent(selectedEntityId ->
        // we load the up-to-date version of selected entity from
        // database and reselect it, so that master-aware components
        // could update itself
        getFromBackendCallback.apply(selectedEntityId).ifPresent(updatedItem -> selectionSupport
                .sendSelectionChangedEvent(getSelectionEventType(updatedItem), updatedItem)));
    }

    private Optional<Long> getModifiedEntityId(final Collection<Long> modifiedEntityIds) {
        if (selectionSupport == null) {
            return Optional.empty();
        }

        return selectionSupport.getSelectedEntityId().filter(modifiedEntityIds::contains);
    }

    private SelectionChangedEventType getSelectionEventType(final T updatedItem) {
        return isInvalidEntityCallback != null && isInvalidEntityCallback.test(updatedItem)
                ? SelectionChangedEventType.ENTITY_DESELECTED
                : SelectionChangedEventType.ENTITY_SELECTED;
    }

    @Override
    public void onEntitiesDeleted(final Collection<Long> entityIds) {
        getModifiedEntityId(entityIds).ifPresent(selectedEntityId ->
        // we need to update the master-aware components, that the
        // master entity was deselected after deletion
        selectionSupport.sendSelectionChangedEvent(SelectionChangedEventType.ENTITY_DESELECTED,
                selectionSupport.getSelectedEntity().orElse(null)));
    }
}
