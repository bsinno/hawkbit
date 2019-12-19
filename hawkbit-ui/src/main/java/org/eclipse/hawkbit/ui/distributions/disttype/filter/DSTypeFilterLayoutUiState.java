/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype.filter;

import java.io.Serializable;

import org.eclipse.hawkbit.repository.model.DistributionSetType;

public class DSTypeFilterLayoutUiState implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean hidden;
    private DistributionSetType clickedDsType;
    private Long clickedDsTypeId;

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public DistributionSetType getClickedDsType() {
        return clickedDsType;
    }

    public void setClickedDsType(final DistributionSetType clickedDsType) {
        this.clickedDsType = clickedDsType;
    }

    public Long getClickedDsTypeId() {
        return clickedDsTypeId;
    }

    public void setClickedDsTypeId(final Long clickedDsTypeId) {
        this.clickedDsTypeId = clickedDsTypeId;
    }
}
