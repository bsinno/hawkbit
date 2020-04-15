/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout;

import java.io.Serializable;
import java.util.Optional;

import org.eclipse.hawkbit.ui.common.event.Layout;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;

/**
 * Stores rollout management view UI state according to user interactions.
 *
 */
@VaadinSessionScope
@SpringComponent
public class RolloutManagementUIState implements Serializable {
    private static final long serialVersionUID = 1L;

    private Layout currentLayout;
    private String searchText;

    private Long selectedRolloutId;
    private String selectedRolloutName;

    private Long selectedRolloutGroupId;
    private String selectedRolloutGroupName;

    public Optional<Layout> getCurrentLayout() {
        return Optional.ofNullable(currentLayout);
    }

    public void setCurrentLayout(final Layout currentLayout) {
        this.currentLayout = currentLayout;
    }

    public Optional<String> getSearchText() {
        return Optional.ofNullable(searchText);
    }

    public void setSearchText(final String searchText) {
        this.searchText = searchText;
    }

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
