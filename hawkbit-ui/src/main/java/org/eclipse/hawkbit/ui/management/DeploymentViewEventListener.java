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
import java.util.Collection;
import java.util.List;

import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.CustomFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.CustomFilterChangedEventPayload.CustomFilterChangedEventType;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.LayoutResizedEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.TargetModifiedEventPayload;
import org.eclipse.hawkbit.ui.distributions.dstable.DistributionSetGridHeader;
import org.eclipse.hawkbit.ui.management.actionhistory.ActionHistoryGridHeader;
import org.eclipse.hawkbit.ui.management.dstable.DistributionGridHeader;
import org.eclipse.hawkbit.ui.management.dstag.filter.DistributionTagButtons;
import org.eclipse.hawkbit.ui.management.dstag.filter.DistributionTagFilterHeader;
import org.eclipse.hawkbit.ui.management.targettable.TargetGrid;
import org.eclipse.hawkbit.ui.management.targettable.TargetGridHeader;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterButtons;
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
        eventListeners.add(new SelectionChangedListener());
        eventListeners.add(new LayoutVisibilityChangedListener());
        eventListeners.add(new LayoutResizedListener());
        eventListeners.add(new TagFilterChangedListener());
        eventListeners.add(new NoTagFilterChangedListener());
        eventListeners.add(new StatusFilterChangedListener());
        eventListeners.add(new OverdueFilterChangedListener());
        eventListeners.add(new CustomFilterChangedListener());
        eventListeners.add(new EntityModifiedListener());
    }

    private class SelectionChangedListener {

        public SelectionChangedListener() {
            eventBus.subscribe(this, EventTopics.SELECTION_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetGrid.class)
        private void onTargetEvent(final SelectionChangedEventPayload<ProxyTarget> eventPayload) {
            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                deploymentView.onTargetSelected(eventPayload.getEntity());
            } else {
                deploymentView.onTargetSelected(null);
            }
        }
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

        @EventBusListenerMethod(scope = EventScope.UI, source = DistributionSetGridHeader.class)
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

    private class TagFilterChangedListener {

        public TagFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.TAG_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetTagFilterButtons.class)
        private void onTargetTagEvent(final Collection<String> eventPayload) {
            deploymentView.filterTargetGridByTags(eventPayload);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = DistributionTagButtons.class)
        private void onDsTagEvent(final Collection<String> eventPayload) {
            deploymentView.filterDsGridByTags(eventPayload);
        }
    }

    private class NoTagFilterChangedListener {

        public NoTagFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.NO_TAG_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = TargetTagFilterButtons.class)
        private void onTargetNoTagEvent(final Boolean eventPayload) {
            deploymentView.filterTargetGridByNoTag(eventPayload);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = DistributionTagButtons.class)
        private void onDsNoTagEvent(final Boolean eventPayload) {
            deploymentView.filterDsGridByNoTag(eventPayload);
        }
    }

    private class StatusFilterChangedListener {

        public StatusFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.STATUS_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetEvent(final List<TargetUpdateStatus> eventPayload) {
            deploymentView.filterTargetGridByStatus(eventPayload);
        }
    }

    private class OverdueFilterChangedListener {

        public OverdueFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.OVERDUE_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetEvent(final Boolean eventPayload) {
            deploymentView.filterTargetGridByOverdue(eventPayload);
        }
    }

    private class CustomFilterChangedListener {

        public CustomFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.CUSTOM_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetEvent(final CustomFilterChangedEventPayload eventPayload) {
            if (eventPayload.getCustomFilterChangedEventType() == CustomFilterChangedEventType.CLICKED) {
                deploymentView.filterTargetGridByCustomFilter(eventPayload.getCustomFilterId());
            } else {
                deploymentView.filterTargetGridByCustomFilter(null);
            }
        }
    }

    private class EntityModifiedListener {

        public EntityModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onTargetEvent(final TargetModifiedEventPayload eventPayload) {
            if (eventPayload.getEntityModifiedEventType() == EntityModifiedEventType.ENTITY_UPDATED) {
                deploymentView.onTargetUpdated(eventPayload.getEntityIds());
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
