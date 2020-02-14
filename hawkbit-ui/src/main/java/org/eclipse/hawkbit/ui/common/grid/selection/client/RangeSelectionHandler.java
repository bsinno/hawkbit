/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.selection.client;

import com.vaadin.client.widget.grid.EventCellReference;
import com.vaadin.client.widget.grid.events.BodyClickHandler;
import com.vaadin.client.widget.grid.events.BodyKeyDownHandler;
import com.vaadin.client.widget.grid.events.GridClickEvent;
import com.vaadin.client.widget.grid.events.GridKeyDownEvent;
import com.vaadin.client.widget.grid.selection.SelectionModel;
import com.vaadin.client.widgets.Grid;
import com.vaadin.event.ShortcutAction.KeyCode;

import elemental.json.JsonObject;

/**
 * Client side handler that detects selection requests of the user on grid items
 * and forwards them to the server side.
 *
 */
public class RangeSelectionHandler implements BodyClickHandler, BodyKeyDownHandler {
    private final Grid<JsonObject> grid;
    private final RangeSelectionServerRpc rangeSelectionServerRpc;

    private int previousRowIndex;

    /**
     * Constructor
     * 
     * @param grid
     *            to listen on
     * @param rangeSelectionServerRpc
     *            RPC server to forward selection requests to
     */
    public RangeSelectionHandler(final Grid<JsonObject> grid, final RangeSelectionServerRpc rangeSelectionServerRpc) {
        this.grid = grid;
        this.rangeSelectionServerRpc = rangeSelectionServerRpc;
        grid.addBodyClickHandler(this);
        grid.addBodyKeyDownHandler(this);
    }

    @Override
    public void onClick(final GridClickEvent event) {
        final SelectionModel<JsonObject> selectionModel = grid.getSelectionModel();
        final EventCellReference<JsonObject> eventCell = grid.getEventCell();
        final int currentRowIndex = eventCell.getRowIndex();
        final JsonObject item = eventCell.getRow();

        if (event.isShiftKeyDown()) {
            rangeSelectionServerRpc.selectRange(previousRowIndex, currentRowIndex, !event.isControlKeyDown());
            return;
        }

        if (event.isControlKeyDown()) {
            if (selectionModel.isSelected(item)) {
                selectionModel.deselect(item);
            } else {
                selectionModel.select(item);
            }
        } else {
            selectionModel.deselectAll();
            selectionModel.select(item);
        }

        previousRowIndex = currentRowIndex;
    }

    @Override
    public void onKeyDown(final GridKeyDownEvent event) {
        if (event.isControlKeyDown() && event.getNativeKeyCode() == KeyCode.A) {
            rangeSelectionServerRpc.selectAll();
        }
    }

}
