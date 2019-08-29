/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.Collections;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.Grid;
import com.vaadin.ui.themes.ValoTheme;

/**
 * DistributionSet TargetFilterQuery table
 *
 */
public class TargetFilterQueryDetailsGrid extends Grid<ProxyTargetFilterQuery> {

    private static final long serialVersionUID = 1L;

    private static final String TFQ_NAME_ID = "tfqName";
    private static final String TFQ_QUERY_ID = "tfqQuery";

    private final VaadinMessageSource i18n;

    public TargetFilterQueryDetailsGrid(final VaadinMessageSource i18n) {
        this.i18n = i18n;

        init();
    }

    /**
     * Populate distribution set metadata.
     *
     * @param distributionSet
     *            the selected distribution set
     */
    public void populateGrid(final ProxyDistributionSet distributionSet) {
        if (distributionSet == null) {
            setItems(Collections.emptyList());
            return;
        }

        final List<ProxyTargetFilterQuery> filters = distributionSet.getAutoAssignFilters();
        setItems(filters);
    }

    private void init() {
        addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        addStyleName(ValoTheme.TABLE_NO_STRIPES);
        addStyleName(SPUIStyleDefinitions.SW_MODULE_TABLE);
        addStyleName("details-layout");

        setSelectionMode(SelectionMode.NONE);
        setSizeFull();
        // same as height of other tabs in details tabsheet
        setHeight(116, Unit.PIXELS);

        addColumns();
    }

    private void addColumns() {
        addColumn(ProxyTargetFilterQuery::getName).setId(TFQ_NAME_ID)
                .setCaption(i18n.getMessage("header.target.filter.name")).setExpandRatio(2);
        addColumn(ProxyTargetFilterQuery::getQuery).setId(TFQ_QUERY_ID)
                .setCaption(i18n.getMessage("header.target.filter.query")).setExpandRatio(3);
    }
}
