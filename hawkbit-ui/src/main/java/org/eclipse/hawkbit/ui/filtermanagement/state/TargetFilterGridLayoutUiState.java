/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement.state;

import java.io.Serializable;

public class TargetFilterGridLayoutUiState implements Serializable {
    private static final long serialVersionUID = 1L;

    private String searchFilterInput;
    private String latestSearchFilterApplied;

    public String getSearchFilterInput() {
        return searchFilterInput;
    }

    public void setSearchFilterInput(final String searchFilterInput) {
        this.searchFilterInput = searchFilterInput;
    }

    public String getLatestSearchFilterApplied() {
        return latestSearchFilterApplied;
    }

    public void setLatestSearchFilterApplied(final String latestSearchFilterApplied) {
        this.latestSearchFilterApplied = latestSearchFilterApplied;
    }

}
