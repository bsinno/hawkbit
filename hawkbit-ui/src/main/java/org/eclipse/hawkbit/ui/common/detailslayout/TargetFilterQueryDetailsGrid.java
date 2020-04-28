/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetFilterQueryToProxyTargetFilterMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDetailsDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.themes.ValoTheme;

/**
 * DistributionSet TargetFilterQuery table
 *
 */
public class TargetFilterQueryDetailsGrid extends AbstractGrid<ProxyTargetFilterQuery, Long>
        implements MasterEntityAwareComponent<Long> {
    private static final long serialVersionUID = 1L;

    private static final String TFQ_NAME_ID = "tfqName";
    private static final String TFQ_QUERY_ID = "tfqQuery";

    private final ConfigurableFilterDataProvider<ProxyTargetFilterQuery, Void, Long> targetFilterQueryDataProvider;

    public TargetFilterQueryDetailsGrid(final VaadinMessageSource i18n,
            final TargetFilterQueryManagement targetFilterQueryManagement) {
        super(i18n, null);

        this.targetFilterQueryDataProvider = new TargetFilterQueryDetailsDataProvider(targetFilterQueryManagement,
                new TargetFilterQueryToProxyTargetFilterMapper()).withConfigurableFilter();

        init();
        setVisible(false);
    }

    @Override
    protected void init() {
        super.init();

        setHeightMode(HeightMode.UNDEFINED);

        addStyleName(ValoTheme.TABLE_NO_STRIPES);
        addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        // addStyleName(SPUIStyleDefinitions.SW_MODULE_TABLE);
    }

    @Override
    public void addColumns() {
        addColumn(ProxyTargetFilterQuery::getName).setId(TFQ_NAME_ID)
                .setCaption(i18n.getMessage("header.target.filter.name")).setExpandRatio(2);
        addColumn(ProxyTargetFilterQuery::getQuery).setId(TFQ_QUERY_ID)
                .setCaption(i18n.getMessage("header.target.filter.query")).setExpandRatio(3);
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyTargetFilterQuery, Void, Long> getFilterDataProvider() {
        return targetFilterQueryDataProvider;
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.TARGET_FILTER_TABLE_ID;
    }

    @Override
    public void masterEntityChanged(final Long masterEntityId) {
        getFilterDataProvider().setFilter(masterEntityId);
        setVisible(masterEntityId != null);
    }
}
