/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

import java.util.Set;
import java.util.function.BiConsumer;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.components.grid.MultiSelectionModel;
import com.vaadin.ui.components.grid.NoSelectionModel;
import com.vaadin.ui.components.grid.SingleSelectionModel;

/**
 * Support for single selection on the grid.
 * 
 * @param <T>
 *            The item-type used by the grid
 */
public class SelectionSupport<T extends ProxyIdentifiableEntity> {
    private final Grid<T> grid;
    private final UIEventBus eventBus;
    private final Object selectedEventSender;
    private final BiConsumer<SelectionChangedEventType, T> updateLastSelectedUiStateCallback;

    // for grids without selection or master-details support
    public SelectionSupport(final Grid<T> grid) {
        this(grid, null, null, null);
    }

    public SelectionSupport(final Grid<T> grid, final UIEventBus eventBus, final Object selectedEventSender,
            final BiConsumer<SelectionChangedEventType, T> updateLastSelectedUiStateCallback) {
        this.grid = grid;
        this.eventBus = eventBus;
        this.selectedEventSender = selectedEventSender;
        this.updateLastSelectedUiStateCallback = updateLastSelectedUiStateCallback;
    }

    public final void disableSelection() {
        grid.setSelectionMode(SelectionMode.NONE);
    }

    public final void enableSingleSelection() {
        grid.setSelectionMode(SelectionMode.SINGLE);

        grid.asSingleSelect().addSingleSelectionListener(event -> {
            final SelectionChangedEventType type = event.getSelectedItem().isPresent()
                    ? SelectionChangedEventType.ENTITY_SELECTED
                    : SelectionChangedEventType.ENTITY_DESELECTED;
            final T itemToSend = event.getSelectedItem().orElse(event.getOldValue());

            sendEvent(type, itemToSend);
        });
    }

    private void sendEvent(final SelectionChangedEventType type, final T itemToSend) {
        if (eventBus == null || itemToSend == null) {
            return;
        }

        eventBus.publish(EventTopics.SELECTION_CHANGED, grid, new SelectionChangedEventPayload<>(type, itemToSend));
        updateLastSelectedUiStateCallback.accept(type, itemToSend);
    }

    public final void enableMultiSelection() {
        grid.setSelectionMode(SelectionMode.MULTI);

        grid.asMultiSelect().addMultiSelectionListener(event -> {
            final Set<T> selectedItems = event.getAllSelectedItems();
            final SelectionChangedEventType type = selectedItems.size() == 1 ? SelectionChangedEventType.ENTITY_SELECTED
                    : SelectionChangedEventType.ENTITY_DESELECTED;
            final T itemToSend = selectedItems.size() == 1 ? selectedItems.iterator().next()
                    : event.getOldSelection().iterator().next();

            sendEvent(type, itemToSend);
        });
    }

    public boolean isNoSelectionModel() {
        return grid.getSelectionModel() instanceof NoSelectionModel;
    }

    public boolean isSingleSelectionModel() {
        return grid.getSelectionModel() instanceof SingleSelectionModel;
    }

    public boolean isMultiSelectionModel() {
        return grid.getSelectionModel() instanceof MultiSelectionModel;
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        if (isNoSelectionModel()) {
            return;
        }

        grid.deselectAll();
    }

    /**
     * Selects the first row if available and enabled.
     */
    public boolean selectFirstRow() {
        if (isNoSelectionModel()) {
            return false;
        }

        final int size = grid.getDataCommunicator().getDataProviderSize();
        if (size > 0) {
            final T firstItem = grid.getDataCommunicator().fetchItemsWithRange(0, 1).get(0);

            if (firstItem != null) {
                grid.select(firstItem);

                return true;
            }
        }

        grid.deselectAll();

        return false;
    }

    public void selectAll() {
        if (!isMultiSelectionModel()) {
            return;
        }

        grid.asMultiSelect().selectAll();
    }
}
