/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.details;

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
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

    // TODO: should we introduce listener for Artifact Changed (Artifact
    // Modified) ?
    /**
     * Constructor for ArtifactDetailsLayout
     * 
     * @param i18n
     *            VaadinMessageSource
     * @param eventBus
     *            UIEventBus
     * @param artifactUploadState
     *            ArtifactUploadState
     * @param notification
     *            UINotification
     * @param artifactManagement
     *            ArtifactManagement
     */
    public ArtifactDetailsGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final ArtifactDetailsGridLayoutUiState artifactDetailsGridLayoutUiState, final UINotification notification,
            final ArtifactManagement artifactManagement, final SpPermissionChecker permChecker) {
        super(i18n, eventBus);

        this.artifactDetailsHeader = new ArtifactDetailsGridHeader(i18n, eventBus, artifactDetailsGridLayoutUiState);
        this.artifactDetailsGrid = new ArtifactDetailsGrid(eventBus, i18n, permChecker, notification,
                artifactManagement);

        buildLayout(artifactDetailsHeader, artifactDetailsGrid);
    }

    public ArtifactDetailsGrid getArtifactDetailsGrid() {
        return artifactDetailsGrid;
    }

    public void onSmSelected(final ProxySoftwareModule selectedSm) {
        artifactDetailsHeader.updateArtifactDetailsHeader(selectedSm != null ? selectedSm.getNameAndVersion() : "");
        artifactDetailsGrid.updateMasterEntityFilter(selectedSm != null ? selectedSm.getId() : null);
    }

    public void maximize() {
        artifactDetailsGrid.createMaximizedContent();
    }

    public void minimize() {
        artifactDetailsGrid.createMinimizedContent();
    }

    public void refreshGrid() {
        artifactDetailsGrid.refreshContainer();
    }
}
