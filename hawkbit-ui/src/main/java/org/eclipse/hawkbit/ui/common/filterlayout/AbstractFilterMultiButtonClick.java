/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.filterlayout;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;

/**
 * Abstract class for button click behavior. It is possible to click multiple
 * buttons.
 * 
 * @param <T>
 *            The type of the Filter Button
 */
public abstract class AbstractFilterMultiButtonClick<T extends ProxyIdentifiableEntity>
        extends AbstractFilterButtonClickBehaviour<T> {

    private static final long serialVersionUID = 1L;
    protected final transient Set<Long> previouslyClickedFilterIds = new HashSet<>();

    @Override
    public void processFilterClick(final T clickedFilter) {
        final Long clickedFilterId = clickedFilter.getId();

        if (isFilterPreviouslyClicked(clickedFilter)) {
            previouslyClickedFilterIds.remove(clickedFilterId);
            filterUnClicked(clickedFilter);
        } else {
            previouslyClickedFilterIds.add(clickedFilterId);
            filterClicked(clickedFilter);
        }
    }

    @Override
    public boolean isFilterPreviouslyClicked(final T clickedFilter) {
        return !previouslyClickedFilterIds.isEmpty() && previouslyClickedFilterIds.contains(clickedFilter.getId());
    }
}
