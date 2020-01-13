/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.event;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.rollout.RolloutView;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Listener for events directed to the rollout view
 *
 */
public class RolloutViewEventListener {
    private final RolloutView rolloutView;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    public RolloutViewEventListener(final RolloutView rolloutView, final UIEventBus eventBus) {
        this.rolloutView = rolloutView;
        this.eventBus = eventBus;
        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        // TODO add eventListeners.add(new XX());
    }

}
