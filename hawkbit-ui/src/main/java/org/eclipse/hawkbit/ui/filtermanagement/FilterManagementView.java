/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.repository.rsql.RsqlValidationOracle;
import org.eclipse.hawkbit.ui.AbstractHawkbitUI;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.filtermanagement.event.FilterManagementViewEventListener;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState.FilterView;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.navigator.View;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.VerticalLayout;

/**
 * View for custom target filter management.
 */
@UIScope
@SpringView(name = FilterManagementView.VIEW_NAME, ui = AbstractHawkbitUI.class)
public class FilterManagementView extends VerticalLayout implements View {
    private static final long serialVersionUID = 1L;

    public static final String VIEW_NAME = "targetFilters";

    private final FilterManagementUIState filterManagementUIState;

    private final TargetFilterGridLayout targetFilterGridLayout;
    private final TargetFilterDetailsLayout targetFilterDetailsLayout;

    private final transient FilterManagementViewEventListener eventListener;

    @Autowired
    FilterManagementView(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final FilterManagementUIState filterManagementUIState, final RsqlValidationOracle rsqlValidationOracle,
            final TargetFilterQueryManagement targetFilterQueryManagement, final SpPermissionChecker permissionChecker,
            final UINotification notification, final UiProperties uiProperties, final EntityFactory entityFactory,
            final TargetManagement targetManagement, final DistributionSetManagement distributionSetManagement) {
        this.filterManagementUIState = filterManagementUIState;

        this.targetFilterGridLayout = new TargetFilterGridLayout(i18n, eventBus, permissionChecker, notification,
                entityFactory, targetFilterQueryManagement, targetManagement, distributionSetManagement,
                filterManagementUIState);

        this.targetFilterDetailsLayout = new TargetFilterDetailsLayout(i18n, eventBus, notification, uiProperties,
                entityFactory, rsqlValidationOracle, targetManagement, targetFilterQueryManagement,
                filterManagementUIState.getDetailsLayoutUiState());

        this.eventListener = new FilterManagementViewEventListener(this, eventBus);
    }

    /**
     * Change UI content to modify a {@link TargetFilterQuery}
     * 
     * @param targetFilterQuery
     *            the filter to modify
     * 
     */
    public void showFilterQueryEdit(final ProxyTargetFilterQuery targetFilterQuery) {
        targetFilterDetailsLayout.showEditFilterUi(targetFilterQuery);
        showFilterDetailsLayout();
    }

    /**
     * Change UI content to create a {@link TargetFilterQuery}
     */
    public void showFilterQueryCreate() {
        targetFilterDetailsLayout.showAddFilterUi();
        showFilterDetailsLayout();
    }

    /**
     * Change UI content to show all {@link TargetFilterQuery}
     */
    public void showFilterQueryOverview() {
        showFilterGridLayout();
    }

    @PostConstruct
    void init() {
        buildLayout();
        restoreState();
    }

    private void buildLayout() {
        setMargin(false);
        setSpacing(false);
        setSizeFull();

        addComponent(targetFilterGridLayout);
        setComponentAlignment(targetFilterGridLayout, Alignment.TOP_CENTER);
        setExpandRatio(targetFilterGridLayout, 1.0F);

        targetFilterDetailsLayout.setVisible(false);
        addComponent(targetFilterDetailsLayout);
        setComponentAlignment(targetFilterDetailsLayout, Alignment.TOP_CENTER);
        setExpandRatio(targetFilterDetailsLayout, 1.0F);
    }

    private void showFilterDetailsLayout() {
        filterManagementUIState.setCurrentView(FilterView.DETAILS);
        targetFilterGridLayout.setVisible(false);
        targetFilterDetailsLayout.setVisible(true);
    }

    private void showFilterGridLayout() {
        filterManagementUIState.setCurrentView(FilterView.FILTERS);
        targetFilterDetailsLayout.setVisible(false);
        targetFilterGridLayout.setVisible(true);
    }

    private void restoreState() {
        if (FilterView.FILTERS == filterManagementUIState.getCurrentView()) {
            showFilterGridLayout();
        } else if (FilterView.DETAILS == filterManagementUIState.getCurrentView()) {
            showFilterDetailsLayout();
        }
        targetFilterDetailsLayout.restoreState();
        targetFilterGridLayout.restoreState();
    }

    @PreDestroy
    void destroy() {
        targetFilterGridLayout.unsubscribeListener();
        targetFilterDetailsLayout.unsubscribeListener();
        eventListener.unsubscribeListeners();
    }
}
