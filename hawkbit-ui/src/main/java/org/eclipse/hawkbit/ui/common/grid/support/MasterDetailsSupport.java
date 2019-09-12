/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;

/**
 * Support for master-details relationship between master-data and the grid.
 * This means that grid content (=details) is updated as soon as master-data
 * changes.
 * 
 * @param <T>
 *            The masterItem-type
 * @param <F>
 *            The filter-type used by the grid
 */
public abstract class MasterDetailsSupport<T, F> {
    private final AbstractGrid<?, F> grid;

    public MasterDetailsSupport(final AbstractGrid<?, F> grid) {
        this.grid = grid;
    }

    /**
     * Set or update the filter in order to refresh the underlying data provider
     * when selected master-data is changed (as all presented grid-data is
     * related to this master-data).
     *
     * @param masterItem
     *            updated master item
     */
    public void masterItemChangedCallback(final T masterItem) {
        grid.clearSortOrder();

        // TODO: refactor (check if it is correct)
        if (masterItem != null) {
            final F filter = mapMasterItemToDetailsFilter(masterItem);
            grid.getFilterDataProvider().setFilter(filter);

            adaptSelection(filter);
        } else {
            grid.getFilterDataProvider().setFilter(null);
        }
    }

    // TODO: check if it really belongs here, or rather to abstract grid
    private void adaptSelection(final F filter) {
        if (!grid.hasSelectionSupport()) {
            return;
        }

        if (filter == null) {
            grid.getSelectionSupport().clearSelection();
            return;
        }

        grid.getSelectionSupport().selectFirstRow();
    }

    /**
     * Maps the master-data type to the grids filter-type.
     *
     * @param masterItem
     *            updated master item
     * 
     * @return grids filter object
     */
    protected abstract F mapMasterItemToDetailsFilter(final T masterItem);
}
