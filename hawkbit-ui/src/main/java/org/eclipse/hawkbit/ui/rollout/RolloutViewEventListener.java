/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.View;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

/**
 * Listener for events directed to the rollout view
 *
 */
public class RolloutViewEventListener {
    private final RolloutView rolloutView;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    /**
     * Constructor
     * 
     * @param rolloutView
     *            The element that is called when receiving an event
     * @param eventBus
     *            The bus to listen on
     */
    public RolloutViewEventListener(final RolloutView rolloutView, final UIEventBus eventBus) {
        this.rolloutView = rolloutView;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new LayoutVisibilityListener());
    }

    private class LayoutVisibilityListener {
        public LayoutVisibilityListener() {
            eventBus.subscribe(this, CommandTopics.CHANGE_LAYOUT_VISIBILITY);
        }

        final EnumSet<Layout> availableLayouts = EnumSet.of(Layout.ROLLOUT_LIST, Layout.ROLLOUT_GROUP_LIST,
                Layout.ROLLOUT_GROUP_TARGET_LIST);

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onLayoutVisibilityEvent(final LayoutVisibilityEventPayload eventPayload) {
            if (eventPayload.getView() != View.ROLLOUT || !availableLayouts.contains(eventPayload.getLayout())) {
                return;
            }

            final Layout changedLayout = eventPayload.getLayout();
            final VisibilityType visibilityType = eventPayload.getVisibilityType();

            if ((changedLayout == Layout.ROLLOUT_LIST && visibilityType == VisibilityType.SHOW)
                    || (changedLayout == Layout.ROLLOUT_GROUP_LIST && visibilityType == VisibilityType.HIDE)) {
                rolloutView.showRolloutListView();
            } else if ((changedLayout == Layout.ROLLOUT_GROUP_LIST && visibilityType == VisibilityType.SHOW)
                    || (changedLayout == Layout.ROLLOUT_GROUP_TARGET_LIST && visibilityType == VisibilityType.HIDE)) {
                rolloutView.showRolloutGroupListView();
            } else if (changedLayout == Layout.ROLLOUT_GROUP_TARGET_LIST && visibilityType == VisibilityType.SHOW) {
                rolloutView.showRolloutGroupTargetsListView();
            }
        }
    }

    /**
     * unsubscribe all listeners
     */
    public void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }

}
