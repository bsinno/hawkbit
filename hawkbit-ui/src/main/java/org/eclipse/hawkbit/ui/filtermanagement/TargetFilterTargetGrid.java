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
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.filtermanagement.state.TargetFilterDetailsLayoutUiState;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

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

    private final ConfigurableFilterDataProvider<ProxyTarget, Void, String> targetDataProvider;

    private final TargetFilterDetailsLayoutUiState uiState;

    TargetFilterTargetGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final TargetManagement targetManagement, final TargetFilterDetailsLayoutUiState uiState) {
        super(i18n, eventBus);

        this.uiState = uiState;
        targetDataProvider = new TargetFilterStateDataProvider(targetManagement, new TargetToProxyTargetMapper(i18n))
                .withConfigurableFilter();

        // TODO: check if relevant or should be defined in AbstractGrid
        // setStyleName("sp-table");
        // setSizeFull();
        // setHeight(100.0F, Unit.PERCENTAGE);
        // addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        // addStyleName(ValoTheme.TABLE_SMALL);

        initTargetStatusIconMap();

        init();
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.CUSTOM_FILTER_TARGET_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyTarget, Void, String> getFilterDataProvider() {
        return targetDataProvider;
    }

    // TODO: check if icons are correct
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

    public void updateTargetFilterQueryFilter(final String targetFilterQuery) {
        getFilterDataProvider().setFilter(targetFilterQuery);
        final long totalTargetCount = -1000; // TODO get real value
        uiState.setFilterQueryValueOfLatestSerach(targetFilterQuery);
        eventBus.publish(EventTopics.UI_ELEMENT_CHANGED, this, totalTargetCount);
    }

    @Override
    public void addColumns() {
        addColumn(ProxyTarget::getName).setId(TARGET_NAME_ID).setCaption(i18n.getMessage("header.name"))
                .setExpandRatio(2);

        addColumn(ProxyTarget::getCreatedBy).setId(TARGET_CREATED_BY_ID)
                .setCaption(i18n.getMessage("header.createdBy"));

        addColumn(ProxyTarget::getCreatedDate).setId(TARGET_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate"));

        addColumn(ProxyTarget::getLastModifiedBy).setId(TARGET_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setHidable(true).setHidden(true);

        addColumn(ProxyTarget::getModifiedDate).setId(TARGET_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setHidable(true).setHidden(true);

        addColumn(ProxyTarget::getDescription).setId(TARGET_DESCRIPTION_ID)
                .setCaption(i18n.getMessage("header.description"));

        addComponentColumn(this::buildTargetStatusIcon).setId(TARGET_STATUS_ID)
                .setCaption(i18n.getMessage("header.status")).setMinimumWidth(50d).setMaximumWidth(50d)
                .setHidable(false).setHidden(false).setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);
    }

    private Label buildTargetStatusIcon(final ProxyTarget target) {
        final ProxyFontIcon targetStatusFontIcon = Optional.ofNullable(targetStatusIconMap.get(target.getUpdateStatus()))
                .orElse(new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String targetStatusId = new StringBuilder(TARGET_STATUS_ID).append(".").append(target.getId()).toString();

        return buildLabelIcon(targetStatusFontIcon, targetStatusId);
    }

    public void restoreState() {
        final String latestFilter = uiState.getFilterQueryValueOfLatestSerach();
        if (!latestFilter.isEmpty()) {
            updateTargetFilterQueryFilter(latestFilter);
        }
    }
}
