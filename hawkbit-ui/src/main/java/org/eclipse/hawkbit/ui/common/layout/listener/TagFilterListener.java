/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventLayoutViewAware;
import org.eclipse.hawkbit.ui.common.event.NoTagFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.TagFilterChangedEventPayload;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class TagFilterListener extends LayoutViewAwareListener {
    private final Consumer<Collection<String>> tagFilterCallback;
    private final Consumer<Boolean> noTagFilterCallback;

    public TagFilterListener(final UIEventBus eventBus, final EventLayoutViewAware layoutViewAware,
            final Consumer<Collection<String>> tagFilterCallback, final Consumer<Boolean> noTagFilterCallback) {
        super(eventBus, Arrays.asList(EventTopics.TAG_FILTER_CHANGED, EventTopics.NO_TAG_FILTER_CHANGED),
                layoutViewAware);

        this.tagFilterCallback = tagFilterCallback;
        this.noTagFilterCallback = noTagFilterCallback;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onTagFilter(final TagFilterChangedEventPayload eventPayload) {
        if (getLayoutViewAware().suitableViewLayout(eventPayload)) {
            tagFilterCallback.accept(eventPayload.getTagNames());
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onNoTagFilter(final NoTagFilterChangedEventPayload eventPayload) {
        if (getLayoutViewAware().suitableViewLayout(eventPayload)) {
            noTagFilterCallback.accept(eventPayload.getIsNoTagActive());
        }
    }
}
