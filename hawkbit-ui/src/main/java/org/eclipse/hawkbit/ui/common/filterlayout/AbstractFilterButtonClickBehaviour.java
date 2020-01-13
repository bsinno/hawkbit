/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.filterlayout;

import java.io.Serializable;

/**
 * Abstract button click behaviour of filter buttons layout.
 * 
 * @param <T>
 *            The type of the Filter Button
 */
public abstract class AbstractFilterButtonClickBehaviour<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * @param clickedFilter
     */
    public abstract void processFilterClick(final T clickedFilter);

    /**
     * @param clickedFilter
     */
    public abstract boolean isFilterPreviouslyClicked(final T clickedFilter);

    /**
     * @param clickedFilter
     */
    protected abstract void filterUnClicked(final T clickedFilter);

    /**
     * @param clickedFilter
     */
    protected abstract void filterClicked(final T clickedFilter);

    public enum ClickBehaviourType {
        CLICKED, UNCLICKED;
    }
}
