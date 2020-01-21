/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.actionhistory;

import java.io.Serializable;

public class ActionStatusGridLayoutUiState implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long selectedActionStatusId;

    public Long getSelectedActionStatusId() {
        return selectedActionStatusId;
    }

    public void setSelectedActionStatusId(final Long selectedActionStatusId) {
        this.selectedActionStatusId = selectedActionStatusId;
    }
}
