/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.springframework.util.CollectionUtils;

/**
 * Support for pinning the items in grid.
 * 
 * @param <T>
 *            The item-type used by the grid
 * @param <F>
 *            The type of identifier assigned and installed ids are related to
 */
public class PinSupport<T extends ProxyIdentifiableEntity, F> {
    private final BiConsumer<PinBehaviourType, T> pinningChangedCallback;

    private T pinnedItem;

    // used to change the pinning style
    private final Consumer<T> refreshItemCallback;

    private final Collection<Long> assignedIds;
    private final Collection<Long> installedIds;

    private final Function<F, Collection<Long>> assignedIdsProvider;
    private final Function<F, Collection<Long>> installedIdsProvider;

    public PinSupport(final BiConsumer<PinBehaviourType, T> pinningChangedCallback,
            final Consumer<T> refreshItemCallback, final Function<F, Collection<Long>> assignedIdsProvider,
            final Function<F, Collection<Long>> installedIdsProvider) {
        this.pinningChangedCallback = pinningChangedCallback;
        this.refreshItemCallback = refreshItemCallback;
        this.assignedIdsProvider = assignedIdsProvider;
        this.installedIdsProvider = installedIdsProvider;

        this.assignedIds = new ArrayList<>();
        this.installedIds = new ArrayList<>();
        this.pinnedItem = null;
    }

    public void changeItemPinning(final T item) {
        if (isPinned(item.getId())) {
            pinnedItem = null;

            refreshItemCallback.accept(item);
            pinningChangedCallback.accept(PinBehaviourType.UNPINNED, item);
        } else {
            // used to reset styling of pinned items' grid
            clearAssignedAndInstalled();

            final T previouslyPinnedItem = pinnedItem;
            pinnedItem = item;

            if (previouslyPinnedItem != null) {
                refreshItemCallback.accept(previouslyPinnedItem);
            }
            refreshItemCallback.accept(item);
            pinningChangedCallback.accept(PinBehaviourType.PINNED, item);
        }
    }

    private boolean isPinned(final Long itemId) {
        return pinnedItem != null && pinnedItem.getId().equals(itemId);
    }

    public Optional<T> getPinnedItem() {
        return Optional.ofNullable(pinnedItem);
    }

    public Optional<Long> getPinnedItemId() {
        return getPinnedItem().map(ProxyIdentifiableEntity::getId);
    }

    public String getPinningStyle(final T item) {
        if (isPinned(item.getId())) {
            return null;
        } else {
            return SPUIStyleDefinitions.UN_PINNED_STYLE;
        }
    }

    public String getAssignedOrInstalledRowStyle(final Long itemId) {
        if (!CollectionUtils.isEmpty(installedIds) && installedIds.contains(itemId)) {
            return SPUIDefinitions.HIGHLIGHT_GREEN;
        }

        if (!CollectionUtils.isEmpty(assignedIds) && assignedIds.contains(itemId)) {
            return SPUIDefinitions.HIGHLIGHT_ORANGE;
        }

        return null;
    }

    public boolean assignedOrInstalledNotEmpty() {
        return !CollectionUtils.isEmpty(assignedIds) || !CollectionUtils.isEmpty(installedIds);
    }

    public void unPinItemIfInIds(final Collection<Long> itemIds) {
        if (pinnedItem != null && !CollectionUtils.isEmpty(itemIds) && itemIds.contains(pinnedItem.getId())) {
            pinningChangedCallback.accept(PinBehaviourType.UNPINNED, pinnedItem);
            pinnedItem = null;
        }
    }

    public boolean clearPinning() {
        if (pinnedItem != null) {
            final T previouslyPinnedItem = pinnedItem;
            pinnedItem = null;

            refreshItemCallback.accept(previouslyPinnedItem);

            return true;
        }

        return false;
    }

    public void restorePinning(final T itemToRestore) {
        pinnedItem = itemToRestore;
    }

    public void repopulateAssignedAndInstalled(final F assignedOrInstalledToId) {
        clearAssignedAndInstalled();

        if (assignedOrInstalledToId != null) {
            assignedIds.addAll(assignedIdsProvider.apply(assignedOrInstalledToId));
            installedIds.addAll(installedIdsProvider.apply(assignedOrInstalledToId));
        }
    }

    public void clearAssignedAndInstalled() {
        assignedIds.clear();
        installedIds.clear();
    }

    public enum PinBehaviourType {
        PINNED, UNPINNED;
    }
}
