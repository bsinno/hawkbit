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
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.details.ArtifactDetailsGridLayout;
import org.eclipse.hawkbit.ui.artifacts.smtable.SoftwareModuleAddUpdateWindow;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractSoftwareModuleDetails;
import org.eclipse.hawkbit.ui.common.detailslayout.SoftwareModuleDetailsHeader;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Implementation of software module details block using generic abstract
 * details style .
 */
public class SwModuleDetails extends AbstractSoftwareModuleDetails {
    private static final long serialVersionUID = 1L;

    private final ManageDistUIState manageDistUIState;

    private final SoftwareModuleDetailsHeader softwareModuleDetailsHeader;

    SwModuleDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker,
            final SoftwareModuleAddUpdateWindow softwareModuleAddUpdateWindow,
            final ManageDistUIState manageDistUIState, final SoftwareModuleManagement softwareManagement,
            final EntityFactory entityFactory, final UINotification uiNotification,
            final ArtifactUploadState artifactUploadState, final ArtifactManagement artifactManagement) {
        super(i18n, eventBus, permissionChecker, softwareManagement, entityFactory, uiNotification);

        this.manageDistUIState = manageDistUIState;

        // TODO: change to load ArtifactDetailsGridLayout only after button
        // click
        final ArtifactDetailsGridLayout artifactDetailsLayout = new ArtifactDetailsGridLayout(i18n, eventBus,
                artifactUploadState, uiNotification, artifactManagement, permChecker);
        this.softwareModuleDetailsHeader = new SoftwareModuleDetailsHeader(i18n, permissionChecker, eventBus,
                uiNotification, entityFactory, softwareManagement, softwareModuleAddUpdateWindow,
                artifactDetailsLayout);

        buildDetails();
        restoreState();
    }

    private void restoreState() {
        if (manageDistUIState.isSwModuleTableMaximized()) {
            setVisible(false);
        }
    }

    @Override
    protected SoftwareModuleDetailsHeader getDetailsHeader() {
        return softwareModuleDetailsHeader;
    }
}
