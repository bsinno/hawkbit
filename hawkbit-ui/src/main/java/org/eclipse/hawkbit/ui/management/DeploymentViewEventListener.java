/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload.ResizeType;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class DeploymentViewEventListener {
    private final DeploymentView deploymentView;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    DeploymentViewEventListener(final DeploymentView deploymentView, final UIEventBus eventBus) {
        this.deploymentView = deploymentView;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new LayoutVisibilityListener());
        eventListeners.add(new LayoutResizeListener());
    }

    private class LayoutVisibilityListener {

        public LayoutVisibilityListener() {
            eventBus.subscribe(this, CommandTopics.CHANGE_LAYOUT_VISIBILITY);
        }

        final EnumSet<EventLayout> availableLayouts = EnumSet.of(EventLayout.TARGET_TAG_FILTER, EventLayout.DS_TAG_FILTER);

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onLayoutVisibilityEvent(final LayoutVisibilityEventPayload eventPayload) {
            if (eventPayload.getView() != EventView.DEPLOYMENT || !availableLayouts.contains(eventPayload.getLayout())) {
                return;
            }

            final EventLayout changedLayout = eventPayload.getLayout();
            final VisibilityType visibilityType = eventPayload.getVisibilityType();

            if (changedLayout == EventLayout.TARGET_TAG_FILTER) {
                if (visibilityType == VisibilityType.SHOW) {
                    deploymentView.showTargetTagLayout();
                } else {
                    deploymentView.hideTargetTagLayout();
                }
            }

            if (changedLayout == EventLayout.DS_TAG_FILTER) {
                if (visibilityType == VisibilityType.SHOW) {
                    deploymentView.showDsTagLayout();
                } else {
                    deploymentView.hideDsTagLayout();
                }
            }
        }
    }

    private class LayoutResizeListener {

        public LayoutResizeListener() {
            eventBus.subscribe(this, CommandTopics.RESIZE_LAYOUT);
        }

        final EnumSet<EventLayout> availableLayouts = EnumSet.of(EventLayout.TARGET_LIST, EventLayout.DS_LIST,
                EventLayout.ACTION_HISTORY_LIST);

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onLayoutResizeEvent(final LayoutResizeEventPayload eventPayload) {
            if (eventPayload.getView() != EventView.DEPLOYMENT || !availableLayouts.contains(eventPayload.getLayout())) {
                return;
            }

            final EventLayout changedLayout = eventPayload.getLayout();
            final ResizeType visibilityType = eventPayload.getResizeType();

            if (changedLayout == EventLayout.TARGET_LIST) {
                if (visibilityType == ResizeType.MAXIMIZE) {
                    deploymentView.maximizeTargetGridLayout();
                } else {
                    deploymentView.minimizeTargetGridLayout();
                }
            }

            if (changedLayout == EventLayout.DS_LIST) {
                if (visibilityType == ResizeType.MAXIMIZE) {
                    deploymentView.maximizeDsGridLayout();
                } else {
                    deploymentView.minimizeDsGridLayout();
                }
            }

            if (changedLayout == EventLayout.ACTION_HISTORY_LIST) {
                if (visibilityType == ResizeType.MAXIMIZE) {
                    deploymentView.maximizeActionHistoryGridLayout();
                } else {
                    deploymentView.minimizeActionHistoryGridLayout();
                }
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
