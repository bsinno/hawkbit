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
import java.util.List;

import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.LayoutResizedEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityChangedEventPayload;
import org.eclipse.hawkbit.ui.management.actionhistory.ActionHistoryGridHeader;
import org.eclipse.hawkbit.ui.management.dstable.DistributionGridHeader;
import org.eclipse.hawkbit.ui.management.dstag.filter.DistributionTagFilterHeader;
import org.eclipse.hawkbit.ui.management.targettable.TargetGridHeader;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterHeader;
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
        eventListeners.add(new LayoutVisibilityChangedListener());
        eventListeners.add(new LayoutResizedListener());
    }

    private class LayoutVisibilityChangedListener {

        public LayoutVisibilityChangedListener() {
            eventBus.subscribe(this, EventTopics.LAYOUT_VISIBILITY_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = { TargetTagFilterHeader.class, TargetGridHeader.class })
        private void onTargetTagEvent(final LayoutVisibilityChangedEventPayload eventPayload) {
            if (eventPayload == LayoutVisibilityChangedEventPayload.LAYOUT_SHOWN) {
                deploymentView.showTargetTagLayout();
            } else {
                deploymentView.hideTargetTagLayout();
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = { DistributionTagFilterHeader.class,
                DistributionGridHeader.class })
        private void onDsTagEvent(final LayoutVisibilityChangedEventPayload eventPayload) {
            if (eventPayload == LayoutVisibilityChangedEventPayload.LAYOUT_SHOWN) {
                deploymentView.showDsTagLayout();
            } else {
                deploymentView.hideDsTagLayout();
            }
        }
    }

    private class LayoutResizedListener {

        public LayoutResizedListener() {
            eventBus.subscribe(this, EventTopics.LAYOUT_RESIZED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetGridHeader.class)
        private void onTargetEvent(final LayoutResizedEventPayload eventPayload) {
            if (eventPayload == LayoutResizedEventPayload.LAYOUT_MAXIMIZED) {
                deploymentView.maximizeTargetGridLayout();
            } else {
                deploymentView.minimizeTargetGridLayout();
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = DistributionGridHeader.class)
        private void onDsEvent(final LayoutResizedEventPayload eventPayload) {
            if (eventPayload == LayoutResizedEventPayload.LAYOUT_MAXIMIZED) {
                deploymentView.maximizeDsGridLayout();
            } else {
                deploymentView.minimizeDsGridLayout();
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = ActionHistoryGridHeader.class)
        private void onActionHistoryEvent(final LayoutResizedEventPayload eventPayload) {
            if (eventPayload == LayoutResizedEventPayload.LAYOUT_MAXIMIZED) {
                deploymentView.maximizeActionHistoryGridLayout();
            } else {
                deploymentView.minimizeActionHistoryGridLayout();
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
