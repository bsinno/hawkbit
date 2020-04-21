/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.details;

import java.util.Arrays;
import java.util.List;

import javax.servlet.MultipartConfigElement;

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.ArtifactUploadState;
import org.eclipse.hawkbit.ui.artifacts.upload.FileUploadProgress;
import org.eclipse.hawkbit.ui.artifacts.upload.UploadDropAreaLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.LayoutViewAware;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.common.layout.listener.SelectionChangedListener;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Display the details of the artifacts for a selected software module.
 */
public class ArtifactDetailsGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final ArtifactDetailsGridHeader artifactDetailsHeader;
    private final ArtifactDetailsGrid artifactDetailsGrid;
    private final UploadDropAreaLayout uploadDropAreaLayout;

    private final transient ArtifactDetailsGridLayoutEventListener eventListener;

    private final transient SelectionChangedListener<ProxySoftwareModule> selectionChangedListener;

    /**
     * Constructor for ArtifactDetailsLayout
     * 
     * @param i18n
     *            VaadinMessageSource
     * @param eventBus
     *            UIEventBus
     * @param artifactDetailsGridLayoutUiState
     *            ArtifactDetailsGridLayoutUiState
     * @param notification
     *            UINotification
     * @param artifactManagement
     *            ArtifactManagement
     * @param permChecker
     *            SpPermissionChecker
     */
    public ArtifactDetailsGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permChecker, final UINotification notification,
            final ArtifactUploadState artifactUploadState,
            final ArtifactDetailsGridLayoutUiState artifactDetailsGridLayoutUiState,
            final ArtifactManagement artifactManagement, final SoftwareModuleManagement softwareManagement,
            final MultipartConfigElement multipartConfigElement) {
        this.artifactDetailsHeader = new ArtifactDetailsGridHeader(i18n, eventBus, artifactDetailsGridLayoutUiState);
        this.artifactDetailsGrid = new ArtifactDetailsGrid(eventBus, i18n, permChecker, notification,
                artifactManagement);

        if (permChecker.hasCreateRepositoryPermission()) {
            this.uploadDropAreaLayout = new UploadDropAreaLayout(i18n, eventBus, notification, artifactUploadState,
                    multipartConfigElement, softwareManagement, artifactManagement);

            buildLayout(artifactDetailsHeader, artifactDetailsGrid, uploadDropAreaLayout);
        } else {
            this.uploadDropAreaLayout = null;

            buildLayout(artifactDetailsHeader, artifactDetailsGrid);
        }

        this.eventListener = new ArtifactDetailsGridLayoutEventListener(this, eventBus);

        final LayoutViewAware masterLayoutView = new LayoutViewAware(Layout.SM_LIST, View.UPLOAD);
        this.selectionChangedListener = new SelectionChangedListener<>(eventBus, masterLayoutView,
                getMasterEntityAwareComponents());
    }

    private List<MasterEntityAwareComponent<ProxySoftwareModule>> getMasterEntityAwareComponents() {
        return Arrays.asList(artifactDetailsHeader, artifactDetailsGrid, uploadDropAreaLayout);
    }

    public void onUploadChanged(final FileUploadProgress fileUploadProgress) {
        if (uploadDropAreaLayout != null) {
            uploadDropAreaLayout.onUploadChanged(fileUploadProgress);
        }
    }

    public void maximize() {
        artifactDetailsGrid.createMaximizedContent();
        hideDetailsLayout();
    }

    public void minimize() {
        artifactDetailsGrid.createMinimizedContent();
        showDetailsLayout();
    }

    public void restoreState() {
        artifactDetailsHeader.restoreState();

        if (uploadDropAreaLayout != null) {
            uploadDropAreaLayout.restoreState();
        }
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();

        selectionChangedListener.unsubscribe();
    }
}
