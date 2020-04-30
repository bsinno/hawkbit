/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag.filter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.ui.common.state.TagFilterLayoutUiState;

public class TargetTagFilterLayoutUiState extends TagFilterLayoutUiState {
    private static final long serialVersionUID = 1L;

    private Long clickedTargetFilterQueryId;
    private final List<TargetUpdateStatus> clickedTargetUpdateStatusFilters = new ArrayList<>();
    private boolean isOverdueFilterClicked;
    private boolean isCustomFilterTabSelected;

    public Long getClickedTargetFilterQueryId() {
        return clickedTargetFilterQueryId;
    }

    public void setClickedTargetFilterQueryId(final Long clickedTargetFilterQueryId) {
        this.clickedTargetFilterQueryId = clickedTargetFilterQueryId;
    }

    public List<TargetUpdateStatus> getClickedTargetUpdateStatusFilters() {
        return clickedTargetUpdateStatusFilters;
    }

    public void setClickedTargetUpdateStatusFilters(final List<TargetUpdateStatus> clickedTargetUpdateStatusFilters) {
        this.clickedTargetUpdateStatusFilters.clear();
        this.clickedTargetUpdateStatusFilters.addAll(clickedTargetUpdateStatusFilters);
    }

    public boolean isOverdueFilterClicked() {
        return isOverdueFilterClicked;
    }

    public void setOverdueFilterClicked(final boolean isOverdueFilterClicked) {
        this.isOverdueFilterClicked = isOverdueFilterClicked;
    }

    public boolean isCustomFilterTabSelected() {
        return isCustomFilterTabSelected;
    }

    public void setCustomFilterTabSelected(final boolean isCustomFilterTabSelected) {
        this.isCustomFilterTabSelected = isCustomFilterTabSelected;
    }
}
