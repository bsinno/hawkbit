/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.support;

/**
 * Via implementations of this support capability an expand-mode is provided
 * that maximizes/minimizes the grid size.
 */
public interface ResizeSupport {
    /**
     * Renews the content for maximized layout.
     */
    default void createMaximizedContent() {
        setMaximizedColumnOrder();
        setMaximizedHiddenColumns();
        setMaximizedColumnExpandRatio();
    }

    /**
     * Renews the content for minimized layout.
     */
    default void createMinimizedContent() {
        setMinimizedColumnOrder();
        setMinimizedHiddenColumns();
        setMinimizedColumnExpandRatio();
    }

    /**
     * Sets the column order for minimized-state.
     */
    void setMinimizedColumnOrder();

    /**
     * Sets the hidden columns for minimized-state.
     */
    void setMinimizedHiddenColumns();

    /**
     * Sets column expand ratio for minimized-state.
     */
    void setMinimizedColumnExpandRatio();

    /**
     * Sets the column order for maximized-state.
     */
    void setMaximizedColumnOrder();

    /**
     * Sets the hidden columns for maximized-state.
     */
    void setMaximizedHiddenColumns();

    /**
     * Sets column expand ratio for maximized-state.
     */
    void setMaximizedColumnExpandRatio();
}
