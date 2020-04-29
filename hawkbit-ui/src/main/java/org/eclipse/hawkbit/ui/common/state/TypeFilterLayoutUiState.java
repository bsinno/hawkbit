/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.state;

import java.io.Serializable;

public class TypeFilterLayoutUiState implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean hidden;
    private Long clickedTypeId;

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public Long getClickedTypeId() {
        return clickedTypeId;
    }

    public void setClickedTypeId(final Long clickedTypeId) {
        this.clickedTypeId = clickedTypeId;
    }
}
