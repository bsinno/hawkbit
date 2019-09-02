/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag.filter;

import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetFilterQueryToProxyTargetFilterMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Target filter query{#link {@link TargetFilterQuery} buttons layout.
 */
public class TargetFilterQueryButtons extends Grid<ProxyTargetFilterQuery> {
    private static final long serialVersionUID = 1L;
    protected static final String FILTER_BUTTON_COLUMN_ID = "filterButton";

    private final ManagementUIState managementUIState;
    private final transient EventBus.UIEventBus eventBus;
    private final transient TargetFilterQueryDataProvider tfqDataProvider;
    private final CustomTargetTagFilterButtonClick customTargetTagFilterButtonClick;

    TargetFilterQueryButtons(final ManagementUIState managementUIState, final UIEventBus eventBus,
            final TargetFilterQueryManagement targetFilterQueryManagement) {
        this.managementUIState = managementUIState;
        this.eventBus = eventBus;
        this.customTargetTagFilterButtonClick = new CustomTargetTagFilterButtonClick(eventBus, managementUIState,
                targetFilterQueryManagement);

        this.tfqDataProvider = new TargetFilterQueryDataProvider(targetFilterQueryManagement,
                new TargetFilterQueryToProxyTargetFilterMapper());

        init();
    }

    /**
     * initializing table.
     * 
     * @param filterButtonClickBehaviour
     */
    private void init() {
        setId(UIComponentIdProvider.CUSTOM_TARGET_TAG_TABLE_ID);

        addStyleName(ValoTheme.TABLE_NO_STRIPES);
        addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        addStyleName(ValoTheme.TABLE_BORDERLESS);
        addStyleName(ValoTheme.TABLE_COMPACT);
        setStyleName("type-button-layout");
        setSizeFull();

        setSelectionMode(SelectionMode.NONE);

        setDataProvider(tfqDataProvider);

        addColumns();

        eventBus.subscribe(this);
    }

    private void addColumns() {
        addComponentColumn(this::buildTfqButton).setId(FILTER_BUTTON_COLUMN_ID);
    }

    private Button buildTfqButton(final ProxyTargetFilterQuery filterQuery) {
        final Button tfqButton = new Button(filterQuery.getName());

        tfqButton.setDescription(filterQuery.getName());
        tfqButton.addStyleName("generatedColumnPadding");
        tfqButton.addStyleName("button-no-border");
        tfqButton.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        tfqButton.addStyleName("button-tag-no-border");
        tfqButton.addStyleName("custom-filter-button");
        tfqButton.setId("customFilter." + filterQuery.getId());

        tfqButton.addClickListener(
                event -> customTargetTagFilterButtonClick.processFilterButtonClick(event.getButton(), filterQuery));

        if (isClickedByDefault(filterQuery.getId())) {
            customTargetTagFilterButtonClick.setDefaultButtonClicked(tfqButton);
        }

        return tfqButton;
    }

    private boolean isClickedByDefault(final Long id) {
        return managementUIState.getTargetTableFilters().getTargetFilterQuery().map(q -> q.equals(id)).orElse(false);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent event) {
        if (event == ManagementUIEvent.RESET_TARGET_FILTER_QUERY) {
            customTargetTagFilterButtonClick.clearAppliedTargetFilterQuery();
        }
    }
}
