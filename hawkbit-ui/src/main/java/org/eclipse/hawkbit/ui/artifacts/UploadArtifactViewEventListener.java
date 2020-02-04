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
import java.util.EnumSet;
import java.util.List;

import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload.ResizeType;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.View;
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
        eventListeners.add(new LayoutVisibilityListener());
        eventListeners.add(new LayoutResizeListener());
    }

    private class LayoutVisibilityListener {

        public LayoutVisibilityListener() {
            eventBus.subscribe(this, CommandTopics.CHANGE_LAYOUT_VISIBILITY);
        }

        final EnumSet<Layout> availableLayouts = EnumSet.of(Layout.SM_TYPE_FILTER);

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onLayoutVisibilityEvent(final LayoutVisibilityEventPayload eventPayload) {
            if (eventPayload.getView() != View.UPLOAD || !availableLayouts.contains(eventPayload.getLayout())) {
                return;
            }

            final VisibilityType visibilityType = eventPayload.getVisibilityType();

            if (visibilityType == VisibilityType.SHOW) {
                uploadArtifactView.showSmTypeLayout();
            } else {
                uploadArtifactView.hideSmTypeLayout();
            }
        }
    }

    private class LayoutResizeListener {

        public LayoutResizeListener() {
            eventBus.subscribe(this, CommandTopics.RESIZE_LAYOUT);
        }

        final EnumSet<Layout> availableLayouts = EnumSet.of(Layout.SM_LIST, Layout.ARTIFACT_LIST);

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onLayoutResizeEvent(final LayoutResizeEventPayload eventPayload) {
            if (eventPayload.getView() != View.UPLOAD || !availableLayouts.contains(eventPayload.getLayout())) {
                return;
            }

            final Layout changedLayout = eventPayload.getLayout();
            final ResizeType visibilityType = eventPayload.getResizeType();

            if (changedLayout == Layout.SM_LIST) {
                if (visibilityType == ResizeType.MAXIMIZE) {
                    uploadArtifactView.maximizeSmGridLayout();
                } else {
                    uploadArtifactView.minimizeSmGridLayout();
                }
            }

            if (changedLayout == Layout.ARTIFACT_LIST) {
                if (visibilityType == ResizeType.MAXIMIZE) {
                    uploadArtifactView.maximizeArtifactGridLayout();
                } else {
                    uploadArtifactView.minimizeArtifactGridLayout();
                }
            }
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
