/** 
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.layout.listener;

import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.event.EventLayoutViewAware;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.GridSizeExceedanceEventPayload;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

/**
 * Event listener for grid size exceedance
 *
 */
public class GridSizeExceedanceListener extends LayoutViewAwareListener {
    private final Consumer<Boolean> onChange;

    /**
     * Constructor for SelectionChangedListener
     *
     * @param eventBus
     *            UIEventBus
     * @param layoutViewAware
     *            EventLayoutViewAware
     * @param onChange
     *            called on change
     */
    public GridSizeExceedanceListener(final UIEventBus eventBus, final EventLayoutViewAware layoutViewAware,
            final Consumer<Boolean> onChange) {
        super(eventBus, EventTopics.GRID_SIZE_EXCEEDANCE_CHANGED, layoutViewAware);
        this.onChange = onChange;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onGridSizeExceedanceEvent(final GridSizeExceedanceEventPayload eventPayload) {
        if (!getLayoutViewAware().suitableViewLayout(eventPayload)) {
            return;
        }
        onChange.accept(eventPayload.isSizeLimitExceeded());
    }

}
