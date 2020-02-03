/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;

public class TargetTagFilterLayoutUiState implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean hidden;
    private final Map<Long, String> clickedTargetTagIdsWithName = new HashMap<>();
    private boolean isNoTagClicked;
    private Long clickedTargetFilterQueryId;
    private final List<TargetUpdateStatus> clickedTargetUpdateStatusFilters = new ArrayList<>();
    private boolean isOverdueFilterClicked;
    private boolean isCustomFilterTabSelected;

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public Map<Long, String> getClickedTargetTagIdsWithName() {
        return clickedTargetTagIdsWithName;
    }

    public void setClickedTargetTagIdsWithName(final Map<Long, String> clickedTargetTagIdsWithName) {
        this.clickedTargetTagIdsWithName.clear();
        this.clickedTargetTagIdsWithName.putAll(clickedTargetTagIdsWithName);
    }

    public boolean isNoTagClicked() {
        return isNoTagClicked;
    }

    public void setNoTagClicked(final boolean isNoTagClicked) {
        this.isNoTagClicked = isNoTagClicked;
    }

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
