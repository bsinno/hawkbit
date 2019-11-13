/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractSoftwareModuleDetails;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Software module details.
 */
public class SoftwareModuleDetails extends AbstractSoftwareModuleDetails {
    private static final long serialVersionUID = 1L;

    private final SoftwareModuleGridLayoutUiState smGridLayoutUiState;

    SoftwareModuleDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final SoftwareModuleManagement softwareManagement,
            final SoftwareModuleGridLayoutUiState smGridLayoutUiState,
            final SmMetaDataWindowBuilder smMetaDataWindowBuilder) {
        super(i18n, eventBus, permissionChecker, softwareManagement, smMetaDataWindowBuilder);

        this.smGridLayoutUiState = smGridLayoutUiState;

        buildDetails();
        restoreState();
    }

    private void restoreState() {
        if (smGridLayoutUiState.isMaximized()) {
            setVisible(false);
        }
    }
}
