/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.details;

import javax.servlet.MultipartConfigElement;

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.artifacts.upload.UploadDropAreaLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
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

        this.uploadDropAreaLayout = new UploadDropAreaLayout(i18n, eventBus, notification, artifactUploadState,
                multipartConfigElement, softwareManagement, artifactManagement);

        this.eventListener = new ArtifactDetailsGridLayoutEventListener(this, eventBus);

        if (permChecker.hasCreateRepositoryPermission()) {
            buildLayout(artifactDetailsHeader, artifactDetailsGrid, uploadDropAreaLayout);
        } else {
            buildLayout(artifactDetailsHeader, artifactDetailsGrid);
        }
    }

    public ArtifactDetailsGrid getArtifactDetailsGrid() {
        return artifactDetailsGrid;
    }

    public void onSmSelected(final ProxySoftwareModule selectedSm) {
        artifactDetailsHeader.updateArtifactDetailsHeader(selectedSm != null ? selectedSm.getNameAndVersion() : "");
        artifactDetailsGrid.updateMasterEntityFilter(selectedSm != null ? selectedSm.getId() : null);
        uploadDropAreaLayout.updateMasterEntityFilter(selectedSm != null ? selectedSm.getId() : null);
    }

    public void maximize() {
        artifactDetailsGrid.createMaximizedContent();
        hideDetailsLayout();
    }

    public void minimize() {
        artifactDetailsGrid.createMinimizedContent();
        showDetailsLayout();
    }

    public void refreshGrid() {
        artifactDetailsGrid.refreshContainer();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
