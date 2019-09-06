/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.table.AbstractTableLayout;
import org.eclipse.hawkbit.ui.dd.criteria.UploadViewClientCriterion;
import org.eclipse.hawkbit.ui.distributions.smtable.SwMetadataPopupLayout;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Software module table layout. (Upload Management)
 */
public class SoftwareModuleTableLayout extends AbstractTableLayout<ProxySoftwareModule> {

    private static final long serialVersionUID = 1L;

    private final SoftwareModuleGrid softwareModuleGrid;

    public SoftwareModuleTableLayout(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final ArtifactUploadState artifactUploadState, final UINotification uiNotification,
            final UIEventBus eventBus, final SoftwareModuleManagement softwareModuleManagement,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement, final EntityFactory entityFactory,
            final UploadViewClientCriterion uploadViewClientCriterion) {

        final SwMetadataPopupLayout swMetadataPopupLayout = new SwMetadataPopupLayout(i18n, uiNotification, eventBus,
                softwareModuleManagement, entityFactory, permChecker);
        this.softwareModuleGrid = new SoftwareModuleGrid(eventBus, i18n, permChecker, uiNotification,
                artifactUploadState, softwareModuleManagement);

        final SoftwareModuleAddUpdateWindow softwareModuleAddUpdateWindow = new SoftwareModuleAddUpdateWindow(i18n,
                uiNotification, eventBus, softwareModuleManagement, softwareModuleTypeManagement, entityFactory);

        super.init(i18n,
                new SoftwareModuleGridHeader(i18n, permChecker, eventBus, artifactUploadState,
                        softwareModuleAddUpdateWindow),
                softwareModuleGrid,
                new SoftwareModuleDetails(i18n, eventBus, permChecker, softwareModuleAddUpdateWindow,
                        artifactUploadState, softwareModuleManagement, swMetadataPopupLayout));
    }

    public SoftwareModuleGrid getSoftwareModuleGrid() {
        return softwareModuleGrid;
    }

}
