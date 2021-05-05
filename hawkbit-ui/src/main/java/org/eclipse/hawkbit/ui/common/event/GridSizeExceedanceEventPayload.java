/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.event;

/**
 * Payload event for grid size limit
 * 
 */
public class GridSizeExceedanceEventPayload extends EventLayoutViewAware {
    private final boolean isSizeLimitExceeded;

    /**
     * Constructor
     * 
     * @param layout
     *            EventLayout
     * @param view
     *            EventView
     * @param isSizeLimitExceeded
     *            is size limit passed or not
     */
    public GridSizeExceedanceEventPayload(final EventLayout layout, final EventView view, final boolean isSizeLimitExceeded) {
        super(layout, view);
        this.isSizeLimitExceeded = isSizeLimitExceeded;
    }

    public boolean isSizeLimitExceeded() {
        return isSizeLimitExceeded;
    }

}
