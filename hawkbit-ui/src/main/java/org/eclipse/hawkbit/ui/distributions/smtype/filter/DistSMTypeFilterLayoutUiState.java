/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtype.filter;

import java.io.Serializable;

import org.eclipse.hawkbit.repository.model.SoftwareModuleType;

public class DistSMTypeFilterLayoutUiState implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean hidden;
    private SoftwareModuleType clickedSmType;

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public SoftwareModuleType getClickedSmType() {
        return clickedSmType;
    }

    public void setClickedSmType(final SoftwareModuleType clickedSmType) {
        this.clickedSmType = clickedSmType;
    }
}
