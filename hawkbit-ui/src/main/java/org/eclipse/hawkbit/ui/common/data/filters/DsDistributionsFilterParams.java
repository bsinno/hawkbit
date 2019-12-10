/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.filters;

import java.io.Serializable;

import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetDistributionsStateDataProvider;

/**
 * Filter params for {@link DistributionSetDistributionsStateDataProvider}.
 */
public class DsDistributionsFilterParams implements Serializable {
    private static final long serialVersionUID = 1L;

    private String searchText;
    private Long dsTypeId;

    public DsDistributionsFilterParams() {
        this(null, null);
    }

    /**
     * Constructor.
     * 
     * @param searchText
     * @param dsTypeId
     */
    public DsDistributionsFilterParams(final String searchText, final Long dsTypeId) {
        this.searchText = searchText;
        this.dsTypeId = dsTypeId;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(final String searchText) {
        this.searchText = searchText;
    }

    public Long getDsTypeId() {
        return dsTypeId;
    }

    public void setDsTypeId(final Long dsTypeId) {
        this.dsTypeId = dsTypeId;
    }
}
