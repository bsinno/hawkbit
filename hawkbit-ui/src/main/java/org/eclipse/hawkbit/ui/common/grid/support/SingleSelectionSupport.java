/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

import java.util.Collections;

import com.vaadin.data.provider.Query;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.components.grid.SingleSelectionModel;

/**
 * Support for single selection on the grid.
 * 
 * @param <T>
 *            The item-type used by the grid
 */
public class SingleSelectionSupport<T> {
    final Grid<T> grid;

    public SingleSelectionSupport(final Grid<T> grid) {
        this.grid = grid;

        enable();
    }

    public final void enable() {
        grid.setSelectionMode(SelectionMode.SINGLE);
    }

    public final void disable() {
        grid.setSelectionMode(SelectionMode.NONE);
    }

    /**
     * Selects the first row if available and enabled.
     */
    public void selectFirstRow() {
        if (!isSingleSelectionModel()) {
            return;
        }

        final int size = grid.getDataProvider().size(new Query<>());
        if (size > 0) {
            final T firstItem = grid.getDataProvider().fetch(new Query<>(0, 1, Collections.emptyList(), null, null))
                    .findFirst().orElse(null);
            grid.getDataProvider().refreshItem(firstItem);
            grid.getSelectionModel().select(firstItem);
        } else {
            grid.getSelectionModel().select(null);
        }
    }

    private boolean isSingleSelectionModel() {
        return grid.getSelectionModel() instanceof SingleSelectionModel;
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        if (!isSingleSelectionModel()) {
            return;
        }
        grid.getSelectionModel().select(null);
    }
}
