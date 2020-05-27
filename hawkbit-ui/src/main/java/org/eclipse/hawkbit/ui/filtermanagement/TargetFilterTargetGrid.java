/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import java.util.Map;

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.ui.common.builder.IconBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.filtermanagement.state.TargetFilterDetailsLayoutUiState;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Label;

/**
 * Shows the targets as a result of the executed filter query.
 */
public class TargetFilterTargetGrid extends AbstractGrid<ProxyTarget, String> {
    private static final long serialVersionUID = 1L;

    private static final String TARGET_NAME_ID = "targetName";
    private static final String TARGET_CREATED_BY_ID = "targetCreatedBy";
    private static final String TARGET_CREATED_DATE_ID = "targetCreatedDate";
    private static final String TARGET_MODIFIED_BY_ID = "targetModifiedBy";
    private static final String TARGET_MODIFIED_DATE_ID = "targetModifiedDate";
    private static final String TARGET_DESCRIPTION_ID = "targetDescription";
    private static final String TARGET_STATUS_ID = "targetStatus";

    private final Map<TargetUpdateStatus, ProxyFontIcon> targetStatusIconMap;

    private final TargetFilterDetailsLayoutUiState uiState;

    TargetFilterTargetGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final TargetManagement targetManagement, final TargetFilterDetailsLayoutUiState uiState) {
        super(i18n, eventBus);

        this.uiState = uiState;

        setFilterSupport(new FilterSupport<>(
                new TargetFilterStateDataProvider(targetManagement, new TargetToProxyTargetMapper(i18n))));
        initFilterMappings();

        targetStatusIconMap = IconBuilder.generateTargetStatusIcons(i18n);
        init();
    }

    private void initFilterMappings() {
        getFilterSupport().<String> addMapping(FilterType.QUERY, (filter, queryText) -> setQueryFilter(queryText),
                uiState.getFilterQueryValueInput());
    }

    private void setQueryFilter(final String queryText) {
        getFilterSupport().setFilter(queryText);
    }

    @Override
    public void init() {
        super.init();

        addStyleName("grid-row-border");
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.CUSTOM_FILTER_TARGET_TABLE_ID;
    }

    @Override
    public void addColumns() {
        addColumn(ProxyTarget::getName).setId(TARGET_NAME_ID).setCaption(i18n.getMessage("header.name"))
                .setExpandRatio(2);

        addColumn(ProxyTarget::getCreatedBy).setId(TARGET_CREATED_BY_ID).setCaption(i18n.getMessage("header.createdBy"))
                .setExpandRatio(1);

        addColumn(ProxyTarget::getCreatedDate).setId(TARGET_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate")).setExpandRatio(1);

        addColumn(ProxyTarget::getLastModifiedBy).setId(TARGET_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setExpandRatio(1).setHidable(true).setHidden(true);

        addColumn(ProxyTarget::getModifiedDate).setId(TARGET_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setExpandRatio(1).setHidable(true).setHidden(true);

        addColumn(ProxyTarget::getDescription).setId(TARGET_DESCRIPTION_ID)
                .setCaption(i18n.getMessage("header.description")).setExpandRatio(1);

        addComponentColumn(this::buildTargetStatusIcon).setId(TARGET_STATUS_ID)
                .setCaption(i18n.getMessage("header.status")).setMinimumWidth(50D).setMaximumWidth(50D)
                .setHidable(false).setHidden(false).setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);
    }

    private Label buildTargetStatusIcon(final ProxyTarget target) {
        return IconBuilder.buildStatusIconLabel(i18n, targetStatusIconMap, ProxyTarget::getUpdateStatus,
                TARGET_STATUS_ID, target);
    }
}
