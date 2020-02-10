/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

import java.util.Collection;
import java.util.function.BiConsumer;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.springframework.util.CollectionUtils;

/**
 * Support for pinning the items in grid.
 * 
 * @param <T>
 *            The item-type used by the grid
 */
public class PinSupport<T extends ProxyIdentifiableEntity> {
    private final BiConsumer<PinBehaviourType, T> pinningChangedCallback;

    private T pinnedItem;

    public PinSupport(final BiConsumer<PinBehaviourType, T> pinningChangedCallback) {
        this.pinningChangedCallback = pinningChangedCallback;
        this.pinnedItem = null;
    }

    public void changeItemPinning(final T item) {
        if (isPinned(item.getId())) {
            pinnedItem = null;
            pinningChangedCallback.accept(PinBehaviourType.UNPINNED, item);
        } else {
            pinnedItem = item;
            pinningChangedCallback.accept(PinBehaviourType.PINNED, item);
        }
    }

    private boolean isPinned(final Long itemId) {
        return pinnedItem != null && pinnedItem.getId().equals(itemId);
    }

    public String getPinningStyle(final T item) {
        if (isPinned(item.getId())) {
            return null;
        } else {
            return SPUIStyleDefinitions.UN_PINNED_STYLE;
        }
    }

    public void unPinItemIfDeleted(final Collection<Long> deletedItemIds) {
        if (pinnedItem != null && !CollectionUtils.isEmpty(deletedItemIds)
                && deletedItemIds.contains(pinnedItem.getId())) {
            pinningChangedCallback.accept(PinBehaviourType.UNPINNED, pinnedItem);
            pinnedItem = null;
        }
    }

    public boolean clearPinning() {
        if (pinnedItem != null) {
            pinnedItem = null;
            return true;
        }

        return false;
    }

    public enum PinBehaviourType {
        PINNED, UNPINNED;
    }
}
