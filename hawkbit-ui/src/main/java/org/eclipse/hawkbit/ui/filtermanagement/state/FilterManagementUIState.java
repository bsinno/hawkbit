/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement.state;

import java.io.Serializable;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;

@VaadinSessionScope
@SpringComponent
public class FilterManagementUIState implements Serializable {

    private static final long serialVersionUID = 2477103280605559284L;

    public enum FilterView {
        FILTERS, DETAILS
    }

    private final TargetFilterGridLayoutUiState gridLayoutUiState;

    private final TargetFilterDetailsLayoutUiState detailsLayoutUiState;

    private FilterView currentView;

    FilterManagementUIState() {
        this.gridLayoutUiState = new TargetFilterGridLayoutUiState();
        this.detailsLayoutUiState = new TargetFilterDetailsLayoutUiState();
    }

    public FilterView getCurrentView() {
        return currentView;
    }

    public void setCurrentView(final FilterView currentView) {
        this.currentView = currentView;
    }

    public TargetFilterGridLayoutUiState getGridLayoutUiState() {
        return gridLayoutUiState;
    }

    public TargetFilterDetailsLayoutUiState getDetailsLayoutUiState() {
        return detailsLayoutUiState;
    }

}
