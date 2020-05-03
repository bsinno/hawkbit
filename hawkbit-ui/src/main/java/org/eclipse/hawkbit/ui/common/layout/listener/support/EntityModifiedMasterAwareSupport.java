/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener.support;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.function.Supplier;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;

// TODO: check with team if needed at all 
// (can be used to define EntityModifiedListeners of master enttities in master aware layouts) 
// as opposed to just listening on change coming from EntityModifiedSelectionAwareSupport
public class EntityModifiedMasterAwareSupport<T extends ProxyIdentifiableEntity> implements EntityModifiedAwareSupport {
    private final LongFunction<Optional<T>> getFromBackendCallback;
    private final Supplier<Optional<Long>> masterIdCallback;
    private final List<MasterEntityAwareComponent<T>> masterEntityAwareComponents;

    public EntityModifiedMasterAwareSupport(final LongFunction<Optional<T>> getFromBackendCallback,
            final Supplier<Optional<Long>> masterIdCallback,
            final List<MasterEntityAwareComponent<T>> masterEntityAwareComponents) {
        this.getFromBackendCallback = getFromBackendCallback;
        this.masterIdCallback = masterIdCallback;
        this.masterEntityAwareComponents = masterEntityAwareComponents;
    }

    public static <E extends ProxyIdentifiableEntity> EntityModifiedMasterAwareSupport<E> of(
            final LongFunction<Optional<E>> getFromBackendCallback, final Supplier<Optional<Long>> masterIdCallback,
            final List<MasterEntityAwareComponent<E>> masterEntityAwareComponents) {
        return new EntityModifiedMasterAwareSupport<>(getFromBackendCallback, masterIdCallback,
                masterEntityAwareComponents);
    }

    @Override
    public void onEntitiesUpdated(final Collection<Long> entityIds) {
        if (getFromBackendCallback == null) {
            return;
        }

        getModifiedEntityId(entityIds).ifPresent(updatedMasterEntityId -> getFromBackendCallback
                .apply(updatedMasterEntityId).ifPresent(this::updateMasterAwareComponents));
    }

    private Optional<Long> getModifiedEntityId(final Collection<Long> modifiedEntityIds) {
        if (masterIdCallback == null) {
            return Optional.empty();
        }

        return masterIdCallback.get().filter(modifiedEntityIds::contains);
    }

    private void updateMasterAwareComponents(final T updatedMasterEntity) {
        masterEntityAwareComponents.forEach(component -> component.masterEntityChanged(updatedMasterEntity));
    }

    @Override
    public void onEntitiesDeleted(final Collection<Long> entityIds) {
        getModifiedEntityId(entityIds).ifPresent(deletedMasterEntityId -> updateMasterAwareComponents(null));
    }
}
