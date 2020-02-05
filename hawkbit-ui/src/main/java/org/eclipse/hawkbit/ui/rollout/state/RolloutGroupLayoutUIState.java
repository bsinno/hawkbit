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

/**
 * Stores rollout group layout UI state according to user interactions.
 *
 */
public class RolloutGroupLayoutUIState implements Serializable {
    private Long selectedRolloutId;
    private String selectedRolloutName;

    public Long getSelectedRolloutId() {
        return selectedRolloutId;
    }

    public void setSelectedRolloutId(final Long selectedRolloutId) {
        this.selectedRolloutId = selectedRolloutId;
    }

    public String getSelectedRolloutName() {
        return selectedRolloutName;
    }

    public void setSelectedRolloutName(final String selectedRolloutName) {
        this.selectedRolloutName = selectedRolloutName;
    }

}
