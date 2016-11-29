/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtype;

import java.io.Serializable;

import org.eclipse.hawkbit.repository.SoftwareManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.artifacts.event.SMFilterEvent;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterSingleButtonClick;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Button;

/**
 * Single button click behaviour of filter buttons layout.
 */
public class DistSMTypeFilterButtonClick extends AbstractFilterSingleButtonClick implements Serializable {

    private static final long serialVersionUID = -4166632002904286983L;

    private final transient EventBus.UIEventBus eventBus;

    private final ManageDistUIState manageDistUIState;

    private final transient SoftwareManagement softwareManagement;

    DistSMTypeFilterButtonClick(final UIEventBus eventBus, final ManageDistUIState manageDistUIState,
            final SoftwareManagement softwareManagement) {
        this.eventBus = eventBus;
        this.manageDistUIState = manageDistUIState;
        this.softwareManagement = softwareManagement;
    }

    @Override
    protected void filterUnClicked(final Button clickedButton) {
        manageDistUIState.getSoftwareModuleFilters().setSoftwareModuleType(null);
        eventBus.publish(this, SMFilterEvent.FILTER_BY_TYPE);
    }

    @Override
    protected void filterClicked(final Button clickedButton) {
        final SoftwareModuleType smType = softwareManagement
                .findSoftwareModuleTypeByName(clickedButton.getData().toString());
        manageDistUIState.getSoftwareModuleFilters().setSoftwareModuleType(smType);
        eventBus.publish(this, SMFilterEvent.FILTER_BY_TYPE);
    }

}
