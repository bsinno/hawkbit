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
import java.util.Optional;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;

public class TargetFilterDetailsLayoutUiState implements Serializable {
    public enum Mode {
        CREATE, EDIT
    }

    private Mode currentMode;
    private ProxyTargetFilterQuery targetFilterQueryforEdit;
    private String nameInput;
    private String filterQueryValueInput;
    private String filterQueryValueOfLatestSerach;

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

    public Optional<ProxyTargetFilterQuery> getTargetFilterQueryforEdit() {
        return Optional.ofNullable(targetFilterQueryforEdit);
    }

    public void setTargetFilterQueryforEdit(final ProxyTargetFilterQuery targetFilterQueryforEdit) {
        this.targetFilterQueryforEdit = targetFilterQueryforEdit;
    }

    public String getNameInput() {
        return nameInput == null ? "" : nameInput;
    }

    public void setNameInput(final String nameInput) {
        this.nameInput = nameInput;
    }

    public String getFilterQueryValueOfLatestSerach() {
        return filterQueryValueOfLatestSerach == null ? "" : filterQueryValueOfLatestSerach;
    }

    public void setFilterQueryValueOfLatestSerach(final String filterQueryValueOfLatestSerach) {
        this.filterQueryValueOfLatestSerach = filterQueryValueOfLatestSerach;
    }

}
