/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterStateDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.filtermanagement.state.TargetFilterDetailsLayoutUiState;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.cronutils.utils.StringUtils;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
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

    private final Map<TargetUpdateStatus, ProxyFontIcon> targetStatusIconMap = new EnumMap<>(TargetUpdateStatus.class);

    private final TargetFilterDetailsLayoutUiState uiState;

    private final transient FilterSupport<ProxyTarget, String> filterSupport;

    TargetFilterTargetGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final TargetManagement targetManagement, final TargetFilterDetailsLayoutUiState uiState) {
        super(i18n, eventBus);

        this.uiState = uiState;

        this.filterSupport = new FilterSupport<>(
                new TargetFilterStateDataProvider(targetManagement, new TargetToProxyTargetMapper(i18n)));

        initFilterMappings();
        initTargetStatusIconMap();
        init();
    }

    private void initFilterMappings() {
        filterSupport.<String> addMapping(FilterType.QUERY, (filter, queryText) -> setQueryFilter(queryText));
    }

    private void setQueryFilter(final String queryText) {
        filterSupport.setFilter(queryText);
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
    public ConfigurableFilterDataProvider<ProxyTarget, Void, String> getFilterDataProvider() {
        return filterSupport.getFilterDataProvider();
    }

    // TODO: reuse code with TargetGrid
    private void initTargetStatusIconMap() {
        targetStatusIconMap.put(TargetUpdateStatus.ERROR, new ProxyFontIcon(VaadinIcons.EXCLAMATION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_RED, getTargetStatusDescription(TargetUpdateStatus.ERROR)));
        targetStatusIconMap.put(TargetUpdateStatus.UNKNOWN, new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_BLUE, getTargetStatusDescription(TargetUpdateStatus.UNKNOWN)));
        targetStatusIconMap.put(TargetUpdateStatus.IN_SYNC, new ProxyFontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getTargetStatusDescription(TargetUpdateStatus.IN_SYNC)));
        targetStatusIconMap.put(TargetUpdateStatus.PENDING, new ProxyFontIcon(VaadinIcons.DOT_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_YELLOW, getTargetStatusDescription(TargetUpdateStatus.PENDING)));
        targetStatusIconMap.put(TargetUpdateStatus.REGISTERED,
                new ProxyFontIcon(VaadinIcons.DOT_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_LIGHT_BLUE,
                        getTargetStatusDescription(TargetUpdateStatus.REGISTERED)));
    }

    // TODO: reuse code with TargetGrid
    private String getTargetStatusDescription(final TargetUpdateStatus targetStatus) {
        return i18n
                .getMessage(UIMessageIdProvider.TOOLTIP_TARGET_STATUS_PREFIX + targetStatus.toString().toLowerCase());
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
        final ProxyFontIcon targetStatusFontIcon = Optional
                .ofNullable(targetStatusIconMap.get(target.getUpdateStatus()))
                .orElse(new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String targetStatusId = new StringBuilder(TARGET_STATUS_ID).append(".").append(target.getId()).toString();

        return SPUIComponentProvider.getLabelIcon(targetStatusFontIcon, targetStatusId);
    }

    public void restoreState() {
        if (!StringUtils.isEmpty(uiState.getFilterQueryValueInput())) {
            filterSupport.setFilter(uiState.getFilterQueryValueInput());
            filterSupport.refreshFilter();
        }
    }

    public FilterSupport<ProxyTarget, String> getFilterSupport() {
        return filterSupport;
    }
}
