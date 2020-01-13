/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstable;

import java.io.Serializable;

public class DistributionGridLayoutUiState implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean maximized;
    private String searchFilter;
    private Long selectedDsId;

    public boolean isMaximized() {
        return maximized;
    }

    public void setMaximized(final boolean maximized) {
        this.maximized = maximized;
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(final String searchFilter) {
        this.searchFilter = searchFilter;
    }

    public Long getSelectedDsId() {
        return selectedDsId;
    }

    public void setSelectedDsId(final Long selectedDsId) {
        this.selectedDsId = selectedDsId;
    }
}
