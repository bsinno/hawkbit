/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

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
public class SelectionSupport<T> {
    final Grid<T> grid;

    public SelectionSupport(final Grid<T> grid) {
        this.grid = grid;
    }

    public final void enableMultiSelection() {
        grid.setSelectionMode(SelectionMode.MULTI);
    }

    public final void enableSingleSelection() {
        grid.setSelectionMode(SelectionMode.SINGLE);
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
