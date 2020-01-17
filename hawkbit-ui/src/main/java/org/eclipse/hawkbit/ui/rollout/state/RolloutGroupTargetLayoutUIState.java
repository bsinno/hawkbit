/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.state;

import java.io.Serializable;

public class RolloutGroupTargetLayoutUIState implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long selectedRolloutGroupId;
    private String selectedRolloutGroupName;

    public Long getSelectedRolloutGroupId() {
        return selectedRolloutGroupId;
    }

    public void setSelectedRolloutGroupId(final Long selectedRolloutGroupId) {
        this.selectedRolloutGroupId = selectedRolloutGroupId;
    }

    public String getSelectedRolloutGroupName() {
        return selectedRolloutGroupName;
    }

    public void setSelectedRolloutGroupName(final String selectedRolloutGroupName) {
        this.selectedRolloutGroupName = selectedRolloutGroupName;
    }

}
