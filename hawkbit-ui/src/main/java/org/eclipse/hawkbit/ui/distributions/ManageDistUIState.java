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

import org.eclipse.hawkbit.ui.common.state.GridLayoutUiState;
import org.eclipse.hawkbit.ui.common.state.TypeFilterLayoutUiState;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;

/**
 * Manage Distributions user state.
 */
@SpringComponent
@VaadinSessionScope
public class ManageDistUIState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final TypeFilterLayoutUiState dsTypeFilterLayoutUiState;
    private final GridLayoutUiState distributionSetGridLayoutUiState;
    private final GridLayoutUiState swModuleGridLayoutUiState;
    private final TypeFilterLayoutUiState smTypeFilterLayoutUiState;

    ManageDistUIState() {
        this.dsTypeFilterLayoutUiState = new TypeFilterLayoutUiState();
        this.distributionSetGridLayoutUiState = new GridLayoutUiState();
        this.swModuleGridLayoutUiState = new GridLayoutUiState();
        this.smTypeFilterLayoutUiState = new TypeFilterLayoutUiState();
    }

    public TypeFilterLayoutUiState getDsTypeFilterLayoutUiState() {
        return dsTypeFilterLayoutUiState;
    }

    public GridLayoutUiState getDistributionSetGridLayoutUiState() {
        return distributionSetGridLayoutUiState;
    }

    public GridLayoutUiState getSwModuleGridLayoutUiState() {
        return swModuleGridLayoutUiState;
    }

    public TypeFilterLayoutUiState getSmTypeFilterLayoutUiState() {
        return smTypeFilterLayoutUiState;
    }
}
