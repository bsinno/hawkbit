/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

import java.util.Optional;
import java.util.Set;

import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;

import com.vaadin.ui.Button;

/**
 * Support for pinning the items in grid.
 * 
 * @param <T>
 *            The item-type used by the grid
 * @param <F>
 *            The item-type identifier used by the UI state
 */
public abstract class PinSupport<T, F> {
    private static final String PINNED_STYLE = "itemPinned";
    private static final String STATUS_PIN_TOGGLE = "statusPinToggle";

    public abstract void styleRowOnPinning();

    public abstract void restoreRowStyle();

    // TODO: consider changing assignedDistItemId to assignedDistItemIds list
    // (in multi-assignment scenario)
    protected String getRowStyleForPinning(final Long assignedDistributionSetId, final Long installedDistributionSetId,
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
        pinBtn.addStyleName(STATUS_PIN_TOGGLE);

        return pinBtn;
    }

    private boolean isPinned(final T item) {
        return getPinnedItemIdFromUiState().map(id -> id.equals(getPinnedItemIdFromItem(item))).orElse(false);
    }

    protected abstract Optional<F> getPinnedItemIdFromUiState();

    protected abstract F getPinnedItemIdFromItem(final T item);

    private void pinItem(final Button pinBtn) {
        publishPinItem();
        restoreRowStyle();
        pinBtn.addStyleName(PINNED_STYLE);
    }

    protected abstract void publishPinItem();

    public void pinItemListener(final T item, final Button clickedButton) {
        if (isPinned(item)) {
            unPinItem(clickedButton);
            setPinnedItemIdInUiState(null);
        } else {
            pinItem(clickedButton);
            setPinnedItemIdInUiState(getPinnedItemIdFromItem(item));
        }
    }

    public abstract void setPinnedItemIdInUiState(final F pinnedItemId);

    private void unPinItem(final Button pinBtn) {
        publishUnPinItem();
        pinBtn.removeStyleName(PINNED_STYLE);
    }

    protected abstract void publishUnPinItem();

    public void unPinDeletedItems(final Long pinnedItemId, final Set<Long> deletedItemIds) {
        if (deletedItemIds.contains(pinnedItemId)) {
            setPinnedItemIdInUiState(null);
            publishUnPinItem();
        }
    }
}
