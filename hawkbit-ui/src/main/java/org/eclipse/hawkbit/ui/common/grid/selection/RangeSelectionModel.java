/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.selection;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Stream;

import org.eclipse.hawkbit.ui.common.grid.selection.client.RangeSelectionServerRpc;

import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.Query;
import com.vaadin.event.ShortcutAction;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.communication.ServerRpc;
import com.vaadin.ui.components.grid.MultiSelectionModelImpl;

/**
 * 
 * Extends {@link MultiSelectionModelImpl} to allow selecting items by clicking
 * instead of using the checkboxes. Also allows using CTRL and SHIFT to select
 * multiple items or a range of items.
 *
 * @param <T>
 *            Item type
 */
public class RangeSelectionModel<T> extends MultiSelectionModelImpl<T> {
    private static final long serialVersionUID = 1L;

    /**
     * {@link ServerRpc} implementation to enable client side code to execute a
     * range selection
     */
    public class RangeSelectionServerRpcImp implements RangeSelectionServerRpc {
        private static final long serialVersionUID = 1L;

        @Override
        public void selectRange(final int startIndex, final int endIndex, final boolean overwrite) {
            if (overwrite) {
                onDeselectAll(true);
            }
            onSelectRange(startIndex, endIndex);
        }

        @Override
        public void selectAll() {
            onSelectAll(true);
        }
    }

    /**
     * Consumes CTRL+A presses of the user. This prevents the selection of
     * everything shown in the browser. Since the listening does not seem to be
     * bound to the grid, the selection logic is moved to the client side.
     *
     */
    private class SelectAllListener extends ShortcutListener {
        private static final long serialVersionUID = 1L;
        private static final int keyCode = ShortcutAction.KeyCode.A;

        /**
         * Constructor
         */
        public SelectAllListener() {
            super("Select all", keyCode, new int[] { ModifierKey.CTRL });
        }

        @Override
        public void handleAction(final Object sender, final Object target) {
            // Do nothing
        }
    }

    @Override
    protected void init() {
        super.init();
        registerRpc(new RangeSelectionServerRpcImp());
        getGrid().addShortcutListener(new SelectAllListener());
    }

    protected void onSelectRange(final int startIndex, final int endIndex) {
        final int offset = Math.min(startIndex, endIndex);
        final int limit = Math.abs(startIndex - endIndex) + 1;

        final DataProvider<T, ?> dataSource = getGrid().getDataProvider();
        final Stream<T> stream = dataSource.fetch(new Query<>(offset, limit, Collections.emptyList(), null, null));
        final LinkedHashSet<T> allItems = new LinkedHashSet<>();
        stream.forEach(allItems::add);
        updateSelection(allItems, Collections.emptySet(), true);
    }
}
