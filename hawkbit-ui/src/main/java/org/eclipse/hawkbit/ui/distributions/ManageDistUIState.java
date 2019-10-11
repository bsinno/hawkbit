/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions;

import java.io.Serializable;

import org.eclipse.hawkbit.ui.distributions.disttype.filter.DSTypeFilterLayoutUiState;
import org.eclipse.hawkbit.ui.distributions.dstable.DistributionSetGridLayoutUiState;
import org.eclipse.hawkbit.ui.distributions.smtable.SwModuleGridLayoutUiState;
import org.eclipse.hawkbit.ui.distributions.smtype.filter.DistSMTypeFilterLayoutUiState;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;

/**
 * Manage Distributions user state.
 */
@SpringComponent
@VaadinSessionScope
public class ManageDistUIState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final DSTypeFilterLayoutUiState dSTypeFilterLayoutUiState;
    private final DistributionSetGridLayoutUiState distributionSetGridLayoutUiState;
    private final SwModuleGridLayoutUiState swModuleGridLayoutUiState;
    private final DistSMTypeFilterLayoutUiState distSMTypeFilterLayoutUiState;

    ManageDistUIState() {
        this.dSTypeFilterLayoutUiState = new DSTypeFilterLayoutUiState();
        this.distributionSetGridLayoutUiState = new DistributionSetGridLayoutUiState();
        this.swModuleGridLayoutUiState = new SwModuleGridLayoutUiState();
        this.distSMTypeFilterLayoutUiState = new DistSMTypeFilterLayoutUiState();
    }

    public DSTypeFilterLayoutUiState getDSTypeFilterLayoutUiState() {
        return dSTypeFilterLayoutUiState;
    }

    public DistributionSetGridLayoutUiState getDistributionSetGridLayoutUiState() {
        return distributionSetGridLayoutUiState;
    }

    public SwModuleGridLayoutUiState getSwModuleGridLayoutUiState() {
        return swModuleGridLayoutUiState;
    }

    public DistSMTypeFilterLayoutUiState getDistSMTypeFilterLayoutUiState() {
        return distSMTypeFilterLayoutUiState;
    }
}
