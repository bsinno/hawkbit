/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.common.table.BaseUIEntityEvent;
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
    private final Class<? extends BaseUIEntityEvent<T>> selectedEventType;

    // for grids without selection or master-details support
    public SelectionSupport(final Grid<T> grid) {
        this(grid, null, null);
    }

    public SelectionSupport(final Grid<T> grid, final UIEventBus eventBus,
            final Class<? extends BaseUIEntityEvent<T>> selectedEventType) {
        this.grid = grid;
        this.eventBus = eventBus;
        this.selectedEventType = selectedEventType;
    }

    public final void enableMultiSelection() {
        grid.setSelectionMode(SelectionMode.MULTI);

        grid.asMultiSelect().addMultiSelectionListener(event -> {
            final Set<T> selectedItems = event.getAllSelectedItems();

            sendSelectedEvent(selectedItems.size() == 1 ? selectedItems.iterator().next() : null);
        });
    }

    private void sendSelectedEvent(final T selectedItemToSend) {
        if (eventBus == null || selectedEventType == null) {
            return;
        }

        // TODO: check if we should use this or grid as the sender
        try {
            if (selectedItemToSend == null) {
                eventBus.publish(this, selectedEventType.getConstructor(BaseEntityEventType.class)
                        .newInstance(BaseEntityEventType.SELECTED_ENTITY));
            } else {
                eventBus.publish(this,
                        selectedEventType.getConstructor(BaseEntityEventType.class, selectedItemToSend.getClass())
                                .newInstance(BaseEntityEventType.SELECTED_ENTITY, selectedItemToSend));
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            // TODO: refactor
            throw new RuntimeException(e);
        }
    }

    public final void enableSingleSelection() {
        grid.setSelectionMode(SelectionMode.SINGLE);

        grid.asSingleSelect()
                .addSingleSelectionListener(event -> sendSelectedEvent(event.getSelectedItem().orElse(null)));
    }

    public final void disableSelection() {
        grid.setSelectionMode(SelectionMode.NONE);
    }

    /**
     * Selects the first row if available and enabled.
     */
    public void selectFirstRow() {
        if (isNoSelectionModel()) {
            return;
        }

        final int size = grid.getDataCommunicator().getDataProviderSize();
        if (size > 0) {
            final T firstItem = grid.getDataCommunicator().fetchItemsWithRange(0, 1).get(0);
            grid.getDataProvider().refreshItem(firstItem);
            grid.select(firstItem);
        } else {
            grid.deselectAll();
        }
    }

    public void selectAll() {
        if (!isMultiSelectionModel()) {
            return;
        }

        grid.asMultiSelect().selectAll();
    }

    public boolean isMultiSelectionModel() {
        return grid.getSelectionModel() instanceof MultiSelectionModel;
    }

    public boolean isSingleSelectionModel() {
        return grid.getSelectionModel() instanceof SingleSelectionModel;
    }

    public boolean isNoSelectionModel() {
        return grid.getSelectionModel() instanceof NoSelectionModel;
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
}
