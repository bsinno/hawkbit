/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtype.filter;

import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.ui.artifacts.event.RefreshSoftwareModuleByFilterEvent;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterSingleButtonClick;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Single button click behavior of filter buttons layout for software module
 * table on the Upload view.
 */
public class SMTypeFilterButtonClick extends AbstractFilterSingleButtonClick<ProxyType> {

    private static final long serialVersionUID = 1L;

    private final transient EventBus.UIEventBus eventBus;

    private final ArtifactUploadState artifactUploadState;

    private final transient SoftwareModuleTypeManagement softwareModuleTypeManagement;

    SMTypeFilterButtonClick(final UIEventBus eventBus, final ArtifactUploadState artifactUploadState,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement) {
        this.eventBus = eventBus;
        this.artifactUploadState = artifactUploadState;
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
    }

    @Override
    protected void filterUnClicked(final ProxyType clickedFilter) {
        artifactUploadState.getSoftwareModuleFilters().setSoftwareModuleType(null);
        eventBus.publish(this, new RefreshSoftwareModuleByFilterEvent());
    }

    @Override
    protected void filterClicked(final ProxyType clickedFilter) {
        softwareModuleTypeManagement.getByName(clickedFilter.getName()).ifPresent(softwareModuleType -> {
            artifactUploadState.getSoftwareModuleFilters().setSoftwareModuleType(softwareModuleType);
            eventBus.publish(this, new RefreshSoftwareModuleByFilterEvent());
        });
    }

}
