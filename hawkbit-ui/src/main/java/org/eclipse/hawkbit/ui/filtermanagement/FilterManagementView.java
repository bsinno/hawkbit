/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import java.util.concurrent.Executor;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.rsql.RsqlValidationOracle;
import org.eclipse.hawkbit.ui.AbstractHawkbitUI;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState.FilterView;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
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
            final TargetManagement targetManagement, final DistributionSetManagement distributionSetManagement,
            @Qualifier("uiExecutor") final Executor executor) {
        this.filterManagementUIState = filterManagementUIState;
        this.eventListener = new FilterManagementViewEventListener(this, eventBus);

        this.targetFilterGridLayout = new TargetFilterGridLayout(i18n, eventBus, permissionChecker, notification,
                entityFactory, targetFilterQueryManagement, targetManagement, distributionSetManagement,
                filterManagementUIState);

        this.targetFilterDetailsLayout = new TargetFilterDetailsLayout(i18n, eventBus, notification, uiProperties,
                entityFactory, rsqlValidationOracle, executor, targetManagement, targetFilterQueryManagement,
                filterManagementUIState.getDetailsLayoutUiState());
    }

    public void onFilterQueryOpen(final ProxyTargetFilterQuery targetFilterQuery) {
        showFilterEditLayout(targetFilterQuery);
    }

    public void onFilterQueryCreate() {
        showFilterCreateLayout();
    }

    public void onDetailsClose() {
        showFilterGridLayout();
    }

    @PostConstruct
    void init() {
        buildLayout();
        restoreState();
    }

    @PreDestroy
    void destroy() {
        eventListener.unsubscribeListeners();
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

    private void restoreState() {
        if (FilterView.FILTERS.equals(filterManagementUIState.getCurrentView())) {
            showFilterGridLayout();
        } else if (FilterView.DETAILS.equals(filterManagementUIState.getCurrentView())) {
            showFilterDetailsLayout();
        }
        targetFilterDetailsLayout.restoreState();
        targetFilterGridLayout.restoreState();
    }

    private void showFilterCreateLayout() {
        targetFilterDetailsLayout.showAddFilterLayout();
        showFilterDetailsLayout();
    }

    private void showFilterEditLayout(final ProxyTargetFilterQuery targetFilterQuery) {
        targetFilterDetailsLayout.showEditFilterLayout(targetFilterQuery);
        showFilterDetailsLayout();
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

    @Override
    public void enter(final ViewChangeEvent event) {
        // This view is constructed in the init() method()
    }

}
