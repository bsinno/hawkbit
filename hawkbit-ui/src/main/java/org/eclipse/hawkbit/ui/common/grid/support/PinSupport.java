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
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.management.event.PinUnpinEvent;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Button;

/**
 * Support for pinning the items in grid.
 * 
 * @param <T>
 *            The item-type used by the grid
 */
public class PinSupport<T extends ProxyIdentifiableEntity> {
    private static final String UN_PINNED_STYLE = "rotate-90-deg";
    private static final String STATUS_PIN_TOGGLE = "statusPinToggle";

    private final Runnable restoreRowStyleCallback;
    private final Supplier<Long> getPinnedItemIdFromUiStateCallback;
    private final Consumer<Long> setPinnedItemIdInUiStateCallback;
    private final UIEventBus eventBus;
    private final PinUnpinEvent pinEvent;
    private final PinUnpinEvent unPinEvent;

    public PinSupport(final UIEventBus eventBus, final PinUnpinEvent pinEvent, final PinUnpinEvent unPinEvent,
            final Runnable restoreRowStyleCallback, final Supplier<Long> getPinnedItemIdFromUiStateCallback,
            final Consumer<Long> setPinnedItemIdInUiStateCallback) {
        this.eventBus = eventBus;
        this.pinEvent = pinEvent;
        this.unPinEvent = unPinEvent;
        this.restoreRowStyleCallback = restoreRowStyleCallback;
        this.getPinnedItemIdFromUiStateCallback = getPinnedItemIdFromUiStateCallback;
        this.setPinnedItemIdInUiStateCallback = setPinnedItemIdInUiStateCallback;
    }

    // TODO: consider changing assignedDistItemId to assignedDistItemIds list
    // (in multi-assignment scenario)
    public String getRowStyleForPinning(final Long assignedDistributionSetId, final Long installedDistributionSetId,
            final Long pinnedDistributionSetId) {
        if (pinnedDistributionSetId == null) {
            return null;
        } else if (pinnedDistributionSetId.equals(installedDistributionSetId)) {
            return SPUIDefinitions.HIGHLIGHT_GREEN;
        } else if (pinnedDistributionSetId.equals(assignedDistributionSetId)) {
            return SPUIDefinitions.HIGHLIGHT_ORANGE;
        }

        return null;
    }

    public Button buildPinActionButton(final Button pinBtn, final T item) {
        if (isPinned(item)) {
            pinItem(pinBtn);
        }
        pinBtn.addStyleName(UN_PINNED_STYLE);
        pinBtn.addStyleName(STATUS_PIN_TOGGLE);

        return pinBtn;
    }

    private boolean isPinned(final T item) {
        return item.getId().equals(getPinnedItemIdFromUiStateCallback.get());
    }

    private void pinItem(final Button pinBtn) {
        publishPinItem();
        restoreRowStyleCallback.run();
        pinBtn.removeStyleName(UN_PINNED_STYLE);
    }

    private void publishPinItem() {
        // TODO: check if the sender is correct or should we use grid
        // component here
        eventBus.publish(this, pinEvent);
    }

    private void publishUnPinItem() {
        // TODO: check if the sender is correct or should we use grid
        // component here
        eventBus.publish(this, unPinEvent);
    }

    public void pinItemListener(final T item, final Button clickedButton) {
        if (isPinned(item)) {
            unPinItem(clickedButton);
            setPinnedItemIdInUiStateCallback.accept(null);
        } else {
            pinItem(clickedButton);
            setPinnedItemIdInUiStateCallback.accept(item.getId());
        }
    }

    private void unPinItem(final Button pinBtn) {
        publishUnPinItem();
        pinBtn.addStyleName(UN_PINNED_STYLE);
    }

    public void unPinItemAfterDeletion(final Long pinnedItemId, final Collection<Long> deletedItemIds) {
        if (deletedItemIds.contains(pinnedItemId)) {
            setPinnedItemIdInUiStateCallback.accept(null);
            publishUnPinItem();
        }
    }
}
