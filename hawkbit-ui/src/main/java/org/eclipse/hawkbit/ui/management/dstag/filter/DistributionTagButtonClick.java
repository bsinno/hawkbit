/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag.filter;

import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterMultiButtonClick;

/**
 * Abstract class for button click behavior of the distribution set's tag
 * buttons. Filters the distribution sets according to the active tags.
 */
// TODO: remove duplication with TargetTagFilterButtonClick
public class DistributionTagButtonClick extends AbstractFilterMultiButtonClick<ProxyTag> {
    private static final long serialVersionUID = 1L;

    private final transient Consumer<Map<Long, String>> filterChangedCallback;
    private final transient Consumer<ClickBehaviourType> noTagChangedCallback;

    DistributionTagButtonClick(final Consumer<Map<Long, String>> filterChangedCallback,
            final Consumer<ClickBehaviourType> noTagChangedCallback) {
        this.filterChangedCallback = filterChangedCallback;
        this.noTagChangedCallback = noTagChangedCallback;
    }

    @Override
    protected void filterUnClicked(final ProxyTag clickedFilter) {
        if (clickedFilter.isNoTag()) {
            noTagChangedCallback.accept(ClickBehaviourType.UNCLICKED);
        } else {
            filterChangedCallback.accept(previouslyClickedFilterIdsWithName);
        }
    }

    @Override
    protected void filterClicked(final ProxyTag clickedFilter) {
        if (clickedFilter.isNoTag()) {
            noTagChangedCallback.accept(ClickBehaviourType.CLICKED);
        } else {
            filterChangedCallback.accept(previouslyClickedFilterIdsWithName);
        }
    }

    void clearPreviouslyClickedFilters() {
        previouslyClickedFilterIdsWithName.clear();
    }

    int getPreviouslyClickedFiltersSize() {
        return previouslyClickedFilterIdsWithName.size();
    }
}
