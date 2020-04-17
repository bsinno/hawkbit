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
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.SearchFilterEventPayload;
import org.eclipse.hawkbit.ui.common.event.View;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class SearchFilterListener implements EventListener {
    private final UIEventBus eventBus;
    private final Consumer<String> searchFilterCallback;
    private final View view;
    private final Layout layout;

    public SearchFilterListener(final UIEventBus eventBus, final Consumer<String> searchFilterCallback, final View view,
            final Layout layout) {
        this.eventBus = eventBus;
        this.searchFilterCallback = searchFilterCallback;
        this.view = view;
        this.layout = layout;

        eventBus.subscribe(this, EventTopics.SEARCH_FILTER_CHANGED);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onSearchFilter(final SearchFilterEventPayload eventPayload) {
        if (eventPayload.getView() != view || eventPayload.getLayout() != layout) {
            return;
        }

        searchFilterCallback.accept(eventPayload.getFilter());
    }

    @Override
    public void unsubscribe() {
        eventBus.unsubscribe(this);
    }
}
