/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener;

import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.LayoutViewAware;
import org.eclipse.hawkbit.ui.common.event.SearchFilterEventPayload;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class SearchFilterListener extends LayoutViewAwareListener {
    private final Consumer<String> searchFilterCallback;

    public SearchFilterListener(final UIEventBus eventBus, final LayoutViewAware layoutViewAware,
            final Consumer<String> searchFilterCallback) {
        super(eventBus, EventTopics.SEARCH_FILTER_CHANGED, layoutViewAware);

        this.searchFilterCallback = searchFilterCallback;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onSearchFilter(final SearchFilterEventPayload eventPayload) {
        if (getLayoutViewAware().suitableViewLayout(eventPayload)) {
            searchFilterCallback.accept(eventPayload.getFilter());
        }
    }
}
