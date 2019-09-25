/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtable;

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.details.ArtifactDetailsGridLayout;
import org.eclipse.hawkbit.ui.artifacts.smtable.SoftwareModuleAddUpdateWindow;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.common.detailslayout.SoftwareModuleDetailsHeader;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Implementation of software module Layout on the Distribution View
 */
public class SwModuleGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final SwModuleGridHeader swModuleGridHeader;
    private final SwModuleGrid swModuleGrid;
    private final SoftwareModuleDetailsHeader softwareModuleDetailsHeader;
    private final SwModuleDetails swModuleDetails;

    public SwModuleGridLayout(final VaadinMessageSource i18n, final UINotification uiNotification,
            final UIEventBus eventBus, final SoftwareModuleManagement softwareModuleManagement,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement, final EntityFactory entityFactory,
            final ManageDistUIState manageDistUIState, final SpPermissionChecker permChecker,
            final ArtifactUploadState artifactUploadState, final ArtifactManagement artifactManagement) {
        super(i18n, eventBus);

        final SoftwareModuleAddUpdateWindow softwareModuleAddUpdateWindow = new SoftwareModuleAddUpdateWindow(i18n,
                uiNotification, eventBus, softwareModuleManagement, softwareModuleTypeManagement, entityFactory);

        this.swModuleGridHeader = new SwModuleGridHeader(i18n, permChecker, eventBus, manageDistUIState,
                softwareModuleAddUpdateWindow);
        this.swModuleGrid = new SwModuleGrid(eventBus, i18n, permChecker, uiNotification, manageDistUIState,
                softwareModuleManagement);

        // TODO: change to load ArtifactDetailsGridLayout only after button
        // click
        final ArtifactDetailsGridLayout artifactDetailsLayout = new ArtifactDetailsGridLayout(i18n, eventBus,
                artifactUploadState, uiNotification, artifactManagement, permChecker);
        this.softwareModuleDetailsHeader = new SoftwareModuleDetailsHeader(i18n, permChecker, eventBus, uiNotification,
                entityFactory, softwareModuleManagement, softwareModuleAddUpdateWindow, artifactDetailsLayout);
        this.swModuleDetails = new SwModuleDetails(i18n, eventBus, permChecker, manageDistUIState,
                softwareModuleManagement, entityFactory, uiNotification);

        buildLayout(swModuleGridHeader, swModuleGrid, softwareModuleDetailsHeader, swModuleDetails);
    }

    public SwModuleGrid getSwModuleGrid() {
        return swModuleGrid;
    }
}
