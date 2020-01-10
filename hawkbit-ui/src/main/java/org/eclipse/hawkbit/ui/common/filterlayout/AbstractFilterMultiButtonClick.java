/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.filterlayout;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyNamedEntity;

/**
 * Abstract class for button click behavior. It is possible to click multiple
 * buttons.
 * 
 * @param <T>
 *            The type of the Filter Button
 */
public abstract class AbstractFilterMultiButtonClick<T extends ProxyNamedEntity>
        extends AbstractFilterButtonClickBehaviour<T> {
    private static final long serialVersionUID = 1L;

    protected final transient Map<Long, String> previouslyClickedFilterIdsWithName = new HashMap<>();

    @Override
    public void processFilterClick(final T clickedFilter) {
        final Long clickedFilterId = clickedFilter.getId();

        if (isFilterPreviouslyClicked(clickedFilter)) {
            previouslyClickedFilterIdsWithName.remove(clickedFilterId);
            filterUnClicked(clickedFilter);
        } else {
            previouslyClickedFilterIdsWithName.put(clickedFilterId, clickedFilter.getName());
            filterClicked(clickedFilter);
        }
    }

    @Override
    public boolean isFilterPreviouslyClicked(final T clickedFilter) {
        return !previouslyClickedFilterIdsWithName.isEmpty()
                && previouslyClickedFilterIdsWithName.containsKey(clickedFilter.getId());
    }
}
