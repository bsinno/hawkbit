/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener.support;

import java.util.Collection;
import java.util.Optional;
import java.util.function.LongFunction;
import java.util.function.Predicate;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.grid.support.PinSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;

public class EntityModifiedPinAwareSupport<T extends ProxyIdentifiableEntity> implements EntityModifiedAwareSupport {
    private final PinSupport<T, ?> pinSupport;
    private final LongFunction<Optional<T>> getFromBackendCallback;
    private final Predicate<T> shouldUnpinnCallback;

    public EntityModifiedPinAwareSupport(final PinSupport<T, ?> pinSupport,
            final LongFunction<Optional<T>> getFromBackendCallback, final Predicate<T> shouldUnpinnCallback) {
        this.pinSupport = pinSupport;
        this.getFromBackendCallback = getFromBackendCallback;
        this.shouldUnpinnCallback = shouldUnpinnCallback;
    }

    public static <E extends ProxyIdentifiableEntity> EntityModifiedPinAwareSupport<E> of(
            final PinSupport<E, ?> pinSupport) {
        return of(pinSupport, null, null);
    }

    public static <E extends ProxyIdentifiableEntity> EntityModifiedPinAwareSupport<E> of(
            final PinSupport<E, ?> pinSupport, final LongFunction<Optional<E>> getFromBackendCallback,
            final Predicate<E> shouldUnpinnCallback) {
        return new EntityModifiedPinAwareSupport<>(pinSupport, getFromBackendCallback, shouldUnpinnCallback);
    }

    @Override
    public void onEntitiesUpdated(final Collection<Long> entityIds) {
        if (pinSupport == null || getFromBackendCallback == null || shouldUnpinnCallback == null) {
            return;
        }

        pinSupport.getPinnedItemId().ifPresent(pinnedItemId -> {
            if (!entityIds.contains(pinnedItemId)) {
                return;
            }

            getFromBackendCallback.apply(pinnedItemId).ifPresent(updatedItem -> {
                if (shouldUnpinnCallback.test(updatedItem)) {
                    pinSupport.changeItemPinning(updatedItem);
                }
            });
        });
    }

    @Override
    public void onEntitiesDeleted(final Collection<Long> entityIds) {
        if (pinSupport == null) {
            return;
        }

        pinSupport.unPinItemIfInIds(entityIds);
    }
}