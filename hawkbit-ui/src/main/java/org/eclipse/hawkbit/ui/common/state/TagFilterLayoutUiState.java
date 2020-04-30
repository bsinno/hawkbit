/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.state;

import java.util.HashMap;
import java.util.Map;

public class TagFilterLayoutUiState extends HidableLayoutUiState {
    private static final long serialVersionUID = 1L;

    private boolean noTagClicked;
    private final Map<Long, String> clickedTagIdsWithName = new HashMap<>();

    public boolean isNoTagClicked() {
        return noTagClicked;
    }

    public void setNoTagClicked(final boolean noTagClicked) {
        this.noTagClicked = noTagClicked;
    }

    public Map<Long, String> getClickedTagIdsWithName() {
        return clickedTagIdsWithName;
    }

    public void setClickedTagIdsWithName(final Map<Long, String> clickedTagIdsWithName) {
        this.clickedTagIdsWithName.clear();
        this.clickedTagIdsWithName.putAll(clickedTagIdsWithName);
    }
}
