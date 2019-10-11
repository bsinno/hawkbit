/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.filters;

import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetDistributionsStateDataProvider;

/**
 * Filter params for {@link DistributionSetDistributionsStateDataProvider}.
 */
public class DsDistributionsFilterParams {
    private String searchText;
    private DistributionSetType clickedDistSetType;

    public DsDistributionsFilterParams() {
        this(null, null);
    }

    /**
     * Constructor.
     * 
     * @param searchText
     * @param clickedDistSetType
     */
    public DsDistributionsFilterParams(final String searchText, final DistributionSetType clickedDistSetType) {
        this.setSearchText(searchText);
        this.setClickedDistSetType(clickedDistSetType);
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(final String searchText) {
        this.searchText = searchText;
    }

    public DistributionSetType getClickedDistSetType() {
        return clickedDistSetType;
    }

    public void setClickedDistSetType(final DistributionSetType clickedDistSetType) {
        this.clickedDistSetType = clickedDistSetType;
    }
}
