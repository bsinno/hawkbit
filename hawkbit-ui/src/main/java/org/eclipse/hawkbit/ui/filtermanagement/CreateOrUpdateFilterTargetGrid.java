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
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.filtermanagement.event.CustomFilterUIEvent;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.rollout.FontIcon;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * Shows the targets as a result of the executed filter query.
 */
public class CreateOrUpdateFilterTargetGrid extends AbstractGrid<ProxyTarget, String> {

    private static final long serialVersionUID = 1L;

    private static final String TARGET_NAME_ID = "targetName";
    private static final String TARGET_CREATED_BY_ID = "targetCreatedBy";
    private static final String TARGET_CREATED_DATE_ID = "targetCreatedDate";
    private static final String TARGET_MODIFIED_BY_ID = "targetModifiedBy";
    private static final String TARGET_MODIFIED_DATE_ID = "targetModifiedDate";
    private static final String TARGET_DESCRIPTION_ID = "targetDescription";
    private static final String TARGET_STATUS_ID = "targetStatus";

    private final FilterManagementUIState filterManagementUIState;

    private final Map<TargetUpdateStatus, FontIcon> targetStatusIconMap = new EnumMap<>(TargetUpdateStatus.class);

    private final ConfigurableFilterDataProvider<ProxyTarget, Void, String> targetDataProvider;

    CreateOrUpdateFilterTargetGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final TargetManagement targetManagement, final FilterManagementUIState filterManagementUIState) {
        super(i18n, eventBus);

        this.filterManagementUIState = filterManagementUIState;

        targetDataProvider = new TargetFilterStateDataProvider(targetManagement, filterManagementUIState,
                new TargetToProxyTargetMapper(i18n)).withConfigurableFilter();

        // TODO: check if relevant or should be defined in AbstractGrid
        // setStyleName("sp-table");
        // setSizeFull();
        // setHeight(100.0F, Unit.PERCENTAGE);
        // addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        // addStyleName(ValoTheme.TABLE_SMALL);

        restoreOnLoad();

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

    // TODO: check if we should also refresh dataprovider filter
    private void restoreOnLoad() {
        if (filterManagementUIState.isCreateFilterViewDisplayed()) {
            filterManagementUIState.setFilterQueryValue(null);
        } else {
            filterManagementUIState.getTfQuery()
                    .ifPresent(value -> filterManagementUIState.setFilterQueryValue(value.getQuery()));
        }
    }

    // TODO: check if icons are correct
    // TODO: reuse code with TargetGrid
    private void initTargetStatusIconMap() {
        targetStatusIconMap.put(TargetUpdateStatus.ERROR, new FontIcon(VaadinIcons.EXCLAMATION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_RED, getTargetStatusDescription(TargetUpdateStatus.ERROR)));
        targetStatusIconMap.put(TargetUpdateStatus.UNKNOWN, new FontIcon(VaadinIcons.QUESTION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_BLUE, getTargetStatusDescription(TargetUpdateStatus.UNKNOWN)));
        targetStatusIconMap.put(TargetUpdateStatus.IN_SYNC, new FontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getTargetStatusDescription(TargetUpdateStatus.IN_SYNC)));
        targetStatusIconMap.put(TargetUpdateStatus.PENDING, new FontIcon(VaadinIcons.DOT_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_YELLOW, getTargetStatusDescription(TargetUpdateStatus.PENDING)));
        targetStatusIconMap.put(TargetUpdateStatus.REGISTERED,
                new FontIcon(VaadinIcons.DOT_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_LIGHT_BLUE,
                        getTargetStatusDescription(TargetUpdateStatus.REGISTERED)));
    }

    // TODO: reuse code with TargetGrid
    private String getTargetStatusDescription(final TargetUpdateStatus targetStatus) {
        return i18n
                .getMessage(UIMessageIdProvider.TOOLTIP_TARGET_STATUS_PREFIX + targetStatus.toString().toLowerCase());
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final CustomFilterUIEvent custFUIEvent) {
        if (custFUIEvent == CustomFilterUIEvent.TARGET_DETAILS_VIEW
                || custFUIEvent == CustomFilterUIEvent.CREATE_NEW_FILTER_CLICK) {
            UI.getCurrent().access(this::refreshContainer);
        } else if (custFUIEvent == CustomFilterUIEvent.FILTER_TARGET_BY_QUERY) {
            UI.getCurrent().access(() -> {
                refreshFilter();
                eventBus.publish(this, CustomFilterUIEvent.UPDATE_TARGET_FILTER_SEARCH_ICON);
            });
        }
    }

    private void refreshFilter() {
        final String filterQuery = filterManagementUIState.getFilterQueryValue();

        if (!StringUtils.isEmpty(filterQuery)) {
            getFilterDataProvider().setFilter(filterQuery);
        } else {
            // TODO: check if it is needed
            getFilterDataProvider().setFilter(null);
        }
    }

    @Override
    public void addColumns() {
        addColumn(ProxyTarget::getName).setId(TARGET_NAME_ID).setCaption(i18n.getMessage("header.name"))
                .setExpandRatio(2);

        addColumn(ProxyTarget::getName).setId(TARGET_CREATED_BY_ID).setCaption(i18n.getMessage("header.createdBy"));

        addColumn(ProxyTarget::getName).setId(TARGET_CREATED_DATE_ID).setCaption(i18n.getMessage("header.createdDate"));

        addColumn(ProxyTarget::getName).setId(TARGET_MODIFIED_BY_ID).setCaption(i18n.getMessage("header.modifiedBy"))
                .setHidable(true).setHidden(true);

        addColumn(ProxyTarget::getName).setId(TARGET_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setHidable(true).setHidden(true);

        addColumn(ProxyTarget::getName).setId(TARGET_DESCRIPTION_ID).setCaption(i18n.getMessage("header.description"));

        addComponentColumn(this::buildTargetStatusIcon).setId(TARGET_STATUS_ID)
                .setCaption(i18n.getMessage("header.status")).setMinimumWidth(50d).setMaximumWidth(50d)
                .setHidable(false).setHidden(false).setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);
    }

    private Label buildTargetStatusIcon(final ProxyTarget target) {
        final FontIcon targetStatusFontIcon = Optional.ofNullable(targetStatusIconMap.get(target.getUpdateStatus()))
                .orElse(new FontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String targetStatusId = new StringBuilder(TARGET_STATUS_ID).append(".").append(target.getId()).toString();

        return buildLabelIcon(targetStatusFontIcon, targetStatusId);
    }
}
