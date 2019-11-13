/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtype.filter;

import java.util.function.BiConsumer;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload.TypeFilterChangedEventType;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterSingleButtonClick;

/**
 * Single button click behavior of filter buttons layout for software module
 * table on the Upload view.
 */
// TODO: remove duplication with DSTypeFilterButtonClick
public class SMTypeFilterButtonClick extends AbstractFilterSingleButtonClick<ProxyType> {
    private static final long serialVersionUID = 1L;

    private final BiConsumer<ProxyType, TypeFilterChangedEventType> filterChangedCallback;

    SMTypeFilterButtonClick(final BiConsumer<ProxyType, TypeFilterChangedEventType> filterChangedCallback) {
        this.filterChangedCallback = filterChangedCallback;
    }

    @Override
    protected void filterUnClicked(final ProxyType clickedFilter) {
        filterChangedCallback.accept(clickedFilter, TypeFilterChangedEventType.TYPE_UNCLICKED);
    }

    @Override
    protected void filterClicked(final ProxyType clickedFilter) {
        filterChangedCallback.accept(clickedFilter, TypeFilterChangedEventType.TYPE_CLICKED);
    }
}
