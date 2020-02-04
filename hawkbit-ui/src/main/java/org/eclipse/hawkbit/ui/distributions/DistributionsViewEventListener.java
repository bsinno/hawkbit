/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.LayoutResizedEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload.TypeFilterChangedEventType;
import org.eclipse.hawkbit.ui.distributions.disttype.filter.DSTypeFilterButtons;
import org.eclipse.hawkbit.ui.distributions.disttype.filter.DSTypeFilterHeader;
import org.eclipse.hawkbit.ui.distributions.dstable.DistributionSetGrid;
import org.eclipse.hawkbit.ui.distributions.dstable.DistributionSetGridHeader;
import org.eclipse.hawkbit.ui.distributions.smtable.SwModuleGridHeader;
import org.eclipse.hawkbit.ui.distributions.smtype.filter.DistSMTypeFilterButtons;
import org.eclipse.hawkbit.ui.distributions.smtype.filter.DistSMTypeFilterHeader;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class DistributionsViewEventListener {
    private final DistributionsView distributionsView;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    DistributionsViewEventListener(final DistributionsView distributionsView, final UIEventBus eventBus) {
        this.distributionsView = distributionsView;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new SelectionChangedListener());
        eventListeners.add(new LayoutVisibilityChangedListener());
        eventListeners.add(new LayoutResizedListener());
        eventListeners.add(new TypeFilterChangedListener());
        eventListeners.add(new EntityModifiedListener());
    }

    private class SelectionChangedListener {

        public SelectionChangedListener() {
            eventBus.subscribe(this, EventTopics.SELECTION_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = DistributionSetGrid.class)
        private void onDsEvent(final SelectionChangedEventPayload<ProxyDistributionSet> eventPayload) {
            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                distributionsView.onDsSelected(eventPayload.getEntity());
            } else {
                distributionsView.onDsSelected(null);
            }
        }
    }

    private class LayoutVisibilityChangedListener {

        public LayoutVisibilityChangedListener() {
            eventBus.subscribe(this, EventTopics.LAYOUT_VISIBILITY_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = { DSTypeFilterHeader.class,
                DistributionSetGridHeader.class })
        private void onDsTypeEvent(final LayoutVisibilityChangedEventPayload eventPayload) {
            if (eventPayload == LayoutVisibilityChangedEventPayload.LAYOUT_SHOWN) {
                distributionsView.showDsTypeLayout();
            } else {
                distributionsView.hideDsTypeLayout();
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = { DistSMTypeFilterHeader.class,
                SwModuleGridHeader.class })
        private void onSmTypeEvent(final LayoutVisibilityChangedEventPayload eventPayload) {
            if (eventPayload == LayoutVisibilityChangedEventPayload.LAYOUT_SHOWN) {
                distributionsView.showSmTypeLayout();
            } else {
                distributionsView.hideSmTypeLayout();
            }
        }
    }

    private class LayoutResizedListener {

        public LayoutResizedListener() {
            eventBus.subscribe(this, EventTopics.LAYOUT_RESIZED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = DistributionSetGridHeader.class)
        private void onDsEvent(final LayoutResizedEventPayload eventPayload) {
            if (eventPayload == LayoutResizedEventPayload.LAYOUT_MAXIMIZED) {
                distributionsView.maximizeDsGridLayout();
            } else {
                distributionsView.minimizeDsGridLayout();
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = SwModuleGridHeader.class)
        private void onSmEvent(final LayoutResizedEventPayload eventPayload) {
            if (eventPayload == LayoutResizedEventPayload.LAYOUT_MAXIMIZED) {
                distributionsView.maximizeSmGridLayout();
            } else {
                distributionsView.minimizeSmGridLayout();
            }
        }
    }

    private class TypeFilterChangedListener {

        public TypeFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.TYPE_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = DSTypeFilterButtons.class)
        private void onDsEvent(final TypeFilterChangedEventPayload<DistributionSetType> typeFilter) {
            if (typeFilter.getTypeFilterChangedEventType() == TypeFilterChangedEventType.TYPE_CLICKED) {
                distributionsView.filterDsGridByType(typeFilter.getType());
            } else {
                distributionsView.filterDsGridByType(null);
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = DistSMTypeFilterButtons.class)
        private void onSmEvent(final TypeFilterChangedEventPayload<SoftwareModuleType> typeFilter) {
            if (typeFilter.getTypeFilterChangedEventType() == TypeFilterChangedEventType.TYPE_CLICKED) {
                distributionsView.filterSmGridByType(typeFilter.getType());
            } else {
                distributionsView.filterSmGridByType(null);
            }
        }
    }

    private class EntityModifiedListener {

        public EntityModifiedListener() {
            eventBus.subscribe(this, EventTopics.ENTITY_MODIFIED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onDsEvent(final EntityModifiedEventPayload eventPayload) {
            if (!ProxyDistributionSet.class.equals(eventPayload.getEntityType())) {
                return;
            }

            if (eventPayload.getEntityModifiedEventType() == EntityModifiedEventType.ENTITY_UPDATED) {
                distributionsView.onDsUpdated(eventPayload.getEntityIds());
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
