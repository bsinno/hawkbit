/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.details;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.artifacts.upload.FileUploadProgress;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class ArtifactDetailsGridLayoutEventListener {
    private final ArtifactDetailsGridLayout artifactDetailsGridLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    ArtifactDetailsGridLayoutEventListener(final ArtifactDetailsGridLayout artifactDetailsGridLayout,
            final UIEventBus eventBus) {
        this.artifactDetailsGridLayout = artifactDetailsGridLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new FileUploadChangedListener());
    }

    private class FileUploadChangedListener {

        public FileUploadChangedListener() {
            eventBus.subscribe(this, EventTopics.FILE_UPLOAD_CHANGED);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onArtifactEvent(final FileUploadProgress fileUploadProgress) {
            artifactDetailsGridLayout.onUploadChanged(fileUploadProgress);
        }
    }

    void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
