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

public class TargetFilterDetailsLayoutUiState implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Mode {
        CREATE, EDIT
    }

    private Mode currentMode;
    private Long selectedFilterId;
    private String nameInput;
    private String filterQueryValueInput;
    private String filterQueryValueOfLatestSearch;

    public Mode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(final Mode currentMode) {
        this.currentMode = currentMode;
    }

    public String getFilterQueryValueInput() {
        return filterQueryValueInput == null ? "" : filterQueryValueInput;
    }

    public void setFilterQueryValueInput(final String filterQueryValueInput) {
        this.filterQueryValueInput = filterQueryValueInput;
    }

    public String getNameInput() {
        return nameInput == null ? "" : nameInput;
    }

    public void setNameInput(final String nameInput) {
        this.nameInput = nameInput;
    }

    public String getFilterQueryValueOfLatestSearch() {
        return filterQueryValueOfLatestSearch == null ? "" : filterQueryValueOfLatestSearch;
    }

    public void setFilterQueryValueOfLatestSearch(final String filterQueryValueOfLatestSearch) {
        this.filterQueryValueOfLatestSearch = filterQueryValueOfLatestSearch;
    }

    public Long getSelectedFilterId() {
        return selectedFilterId;
    }

    public void setSelectedFilterId(Long selectedFilterId) {
        this.selectedFilterId = selectedFilterId;
    }

}
