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
    private String rolloutName;

    public String getRolloutName() {
        return rolloutName;
    }

    public void setRolloutName(final String rolloutName) {
        this.rolloutName = rolloutName;
    }

}
