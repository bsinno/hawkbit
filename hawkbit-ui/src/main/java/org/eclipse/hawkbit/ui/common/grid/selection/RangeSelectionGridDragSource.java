/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.selection;

import org.eclipse.hawkbit.ui.common.grid.selection.client.RangeSelectionState;

import com.vaadin.ui.Grid;
import com.vaadin.ui.components.grid.GridDragSource;

/**
 * Extends {@link GridDragSource} to offer the number of currently selected
 * items to the client side
 *
 * @param <T>
 *            item type
 */
public class RangeSelectionGridDragSource<T> extends GridDragSource<T> {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     * 
     * @param target
     *            Grid to be extended.
     */
    public RangeSelectionGridDragSource(final Grid<T> target) {
        super(target);
        target.getSelectionModel()
                .addSelectionListener(event -> getState().setSelectionCount(event.getAllSelectedItems().size()));
    }

    @Override
    protected RangeSelectionState getState() {
        return (RangeSelectionState) super.getState();
    }

}
