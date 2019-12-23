/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.artifacts.details.ArtifactDetailsGridHeader;
import org.eclipse.hawkbit.ui.artifacts.smtable.SoftwareModuleGrid;
import org.eclipse.hawkbit.ui.artifacts.smtable.SoftwareModuleGridHeader;
import org.eclipse.hawkbit.ui.artifacts.smtype.filter.SMTypeFilterButtons;
import org.eclipse.hawkbit.ui.artifacts.smtype.filter.SMTypeFilterHeader;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.LayoutResizedEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.TypeFilterChangedEventPayload.TypeFilterChangedEventType;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class UploadArtifactViewEventListener {
    private final UploadArtifactView uploadArtifactView;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    UploadArtifactViewEventListener(final UploadArtifactView uploadArtifactView, final UIEventBus eventBus) {
        this.uploadArtifactView = uploadArtifactView;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new SelectionChangedListener());
        eventListeners.add(new LayoutVisibilityChangedListener());
        eventListeners.add(new LayoutResizedListener());
        eventListeners.add(new TypeFilterChangedListener());
    }

    private class SelectionChangedListener {

        public SelectionChangedListener() {
            eventBus.subscribe(this, EventTopics.SELECTION_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = SoftwareModuleGrid.class)
        private void onSmEvent(final SelectionChangedEventPayload<ProxySoftwareModule> eventPayload) {
            if (eventPayload.getSelectionChangedEventType() == SelectionChangedEventType.ENTITY_SELECTED) {
                uploadArtifactView.onSmSelected(eventPayload.getEntity());
            } else {
                uploadArtifactView.onSmSelected(null);
            }
        }
    }

    private class LayoutVisibilityChangedListener {

        public LayoutVisibilityChangedListener() {
            eventBus.subscribe(this, EventTopics.LAYOUT_VISIBILITY_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = { SMTypeFilterHeader.class,
                SoftwareModuleGridHeader.class })
        private void onSmTypeEvent(final LayoutVisibilityChangedEventPayload eventPayload) {
            if (eventPayload == LayoutVisibilityChangedEventPayload.LAYOUT_SHOWN) {
                uploadArtifactView.showSmTypeLayout();
            } else {
                uploadArtifactView.hideSmTypeLayout();
            }
        }
    }

    private class LayoutResizedListener {

        public LayoutResizedListener() {
            eventBus.subscribe(this, EventTopics.LAYOUT_RESIZED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = SoftwareModuleGridHeader.class)
        private void onSmEvent(final LayoutResizedEventPayload eventPayload) {
            if (eventPayload == LayoutResizedEventPayload.LAYOUT_MAXIMIZED) {
                uploadArtifactView.maximizeSmGridLayout();
            } else {
                uploadArtifactView.minimizeSmGridLayout();
            }
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = ArtifactDetailsGridHeader.class)
        private void onArtifactEvent(final LayoutResizedEventPayload eventPayload) {
            if (eventPayload == LayoutResizedEventPayload.LAYOUT_MAXIMIZED) {
                uploadArtifactView.maximizeArtifactGridLayout();
            } else {
                uploadArtifactView.minimizeArtifactGridLayout();
            }
        }
    }

    private class TypeFilterChangedListener {

        public TypeFilterChangedListener() {
            eventBus.subscribe(this, EventTopics.TYPE_FILTER_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI, source = SMTypeFilterButtons.class)
        private void onSmEvent(final TypeFilterChangedEventPayload<SoftwareModuleType> typeFilter) {
            if (typeFilter.getTypeFilterChangedEventType() == TypeFilterChangedEventType.TYPE_CLICKED) {
                uploadArtifactView.filterSmGridByType(typeFilter.getType());
            } else {
                uploadArtifactView.filterSmGridByType(null);
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
