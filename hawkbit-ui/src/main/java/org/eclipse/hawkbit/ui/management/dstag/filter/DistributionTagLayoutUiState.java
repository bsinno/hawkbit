/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag.filter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DistributionTagLayoutUiState implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean hidden;
    private final Map<Long, String> clickedTargetTagIdsWithName = new HashMap<>();
    private boolean noTagClicked;

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isNoTagClicked() {
        return noTagClicked;
    }

    public void setNoTagClicked(final boolean noTagClicked) {
        this.noTagClicked = noTagClicked;
    }

    public Map<Long, String> getClickedTargetTagIdsWithName() {
        return clickedTargetTagIdsWithName;
    }

    public void setClickedTargetTagIdsWithName(final Map<Long, String> clickedTargetTagIdsWithName) {
        this.clickedTargetTagIdsWithName.clear();
        this.clickedTargetTagIdsWithName.putAll(clickedTargetTagIdsWithName);
    }
}
