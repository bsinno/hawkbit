/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.details;

import java.util.Optional;

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent.SoftwareModuleEventType;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupportIdentifiable;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

/**
 * Display the details of the artifacts for a selected software module.
 */
public class ArtifactDetailsGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final ArtifactUploadState artifactUploadState;

    private final ArtifactDetailsGridHeader artifactDetailsHeader;
    private final ArtifactDetailsGrid artifactDetailsGrid;

    private final MasterDetailsSupport<ProxySoftwareModule, Long> masterDetailsSupport;

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
            final ArtifactUploadState artifactUploadState, final UINotification notification,
            final ArtifactManagement artifactManagement, final SpPermissionChecker permChecker) {
        super(i18n, eventBus);

        this.artifactUploadState = artifactUploadState;

        this.artifactDetailsHeader = new ArtifactDetailsGridHeader(i18n, artifactUploadState, eventBus);
        this.artifactDetailsGrid = new ArtifactDetailsGrid(eventBus, i18n, permChecker, notification,
                artifactManagement);

        this.masterDetailsSupport = new MasterDetailsSupportIdentifiable<>(artifactDetailsGrid);

        buildLayout(artifactDetailsHeader, artifactDetailsGrid);
    }

    // TODO: check if it can be removed with registerDetails in
    // UploadArtifactView
    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SoftwareModuleEvent swModuleUIEvent) {
        final Optional<Long> swModuleId = artifactUploadState.getSelectedBaseSwModuleId();

        if (BaseEntityEventType.SELECTED_ENTITY == swModuleUIEvent.getEventType()
                || SoftwareModuleEventType.ARTIFACTS_CHANGED == swModuleUIEvent.getSoftwareModuleEventType()) {
            UI.getCurrent().access(() -> populateArtifactDetails(swModuleUIEvent.getEntity()));
        } else if (BaseEntityEventType.REMOVE_ENTITY == swModuleUIEvent.getEventType() && swModuleId.isPresent()
                && swModuleUIEvent.getEntityIds().contains(swModuleId.get())) {
            UI.getCurrent().access(() -> populateArtifactDetails(null));
        }
    }

    /**
     * Populate artifact details header and grid for the software module.
     *
     * @param swModule
     *            the Software Module
     */
    public void populateArtifactDetails(final ProxySoftwareModule swModule) {
        if (swModule != null) {
            artifactDetailsHeader.updateArtifactDetailsHeader(swModule.getNameAndVersion());
            masterDetailsSupport.masterItemChangedCallback(swModule);
        } else {
            artifactDetailsHeader.updateArtifactDetailsHeader(" ");
            masterDetailsSupport.masterItemChangedCallback(null);
        }
    }

    public MasterDetailsSupport<ProxySoftwareModule, Long> getMasterDetailsSupport() {
        return masterDetailsSupport;
    }

    public ArtifactDetailsGrid getArtifactDetailsGrid() {
        return artifactDetailsGrid;
    }
}
