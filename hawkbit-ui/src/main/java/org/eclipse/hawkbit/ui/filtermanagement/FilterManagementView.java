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
import org.eclipse.hawkbit.ui.filtermanagement.event.CustomFilterUIEvent;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

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
    private final transient UIEventBus eventBus;

    private final TargetFilterGridLayout targetFilterGridLayout;
    private final TargetFilterDetailsLayout targetFilterDetailsLayout;

    @Autowired
    FilterManagementView(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final FilterManagementUIState filterManagementUIState, final RsqlValidationOracle rsqlValidationOracle,
            final TargetFilterQueryManagement targetFilterQueryManagement, final SpPermissionChecker permissionChecker,
            final UINotification notification, final UiProperties uiProperties, final EntityFactory entityFactory,
            final TargetManagement targetManagement, final DistributionSetManagement distributionSetManagement,
            @Qualifier("uiExecutor") final Executor executor) {
        this.filterManagementUIState = filterManagementUIState;
        this.eventBus = eventBus;

        this.targetFilterGridLayout = new TargetFilterGridLayout(i18n, eventBus, permissionChecker, notification,
                entityFactory, targetFilterQueryManagement, targetManagement, distributionSetManagement,
                filterManagementUIState);

        this.targetFilterDetailsLayout = new TargetFilterDetailsLayout(i18n, eventBus, notification, uiProperties,
                entityFactory, rsqlValidationOracle, executor, targetManagement, targetFilterQueryManagement,
                filterManagementUIState);
    }

    @PostConstruct
    void init() {
        setMargin(false);
        setSpacing(false);
        setSizeFull();

        buildLayout();
        eventBus.subscribe(this);
    }

    @PreDestroy
    void destroy() {
        eventBus.unsubscribe(this);
    }

    private void buildLayout() {
        if (filterManagementUIState.isCreateFilterViewDisplayed()) {
            showFilterCreateDetailsLayout();
        } else if (filterManagementUIState.isEditViewDisplayed()) {
            filterManagementUIState.getTfQuery().ifPresent(this::showFilterEditDetailsLayout);
        } else {
            showFilterGridLayout();
        }
    }

    private void showFilterCreateDetailsLayout() {
        targetFilterDetailsLayout.showAddFilterLayout();

        showFilterDetailsLayout();
    }

    private void showFilterDetailsLayout() {
        removeAllComponents();

        addComponent(targetFilterDetailsLayout);
        setComponentAlignment(targetFilterDetailsLayout, Alignment.TOP_CENTER);
        setExpandRatio(targetFilterDetailsLayout, 1.0F);
    }

    private void showFilterEditDetailsLayout(final ProxyTargetFilterQuery tfQuery) {
        targetFilterDetailsLayout.showEditFilterLayout(tfQuery);

        showFilterDetailsLayout();
    }

    private void showFilterGridLayout() {
        removeAllComponents();

        addComponent(targetFilterGridLayout);
        setComponentAlignment(targetFilterGridLayout, Alignment.TOP_CENTER);
        setExpandRatio(targetFilterGridLayout, 1.0F);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final CustomFilterUIEvent custFilterUIEvent) {
        if (custFilterUIEvent == CustomFilterUIEvent.TARGET_FILTER_DETAIL_VIEW) {
            // use payload from event instead of ui state
            filterManagementUIState.getTfQuery().ifPresent(this::showFilterEditDetailsLayout);
        } else if (custFilterUIEvent == CustomFilterUIEvent.CREATE_NEW_FILTER_CLICK) {
            showFilterCreateDetailsLayout();
        } else if (custFilterUIEvent == CustomFilterUIEvent.SHOW_FILTER_MANAGEMENT) {
            showFilterGridLayout();
        }
    }

    @Override
    public void enter(final ViewChangeEvent event) {
        // This view is constructed in the init() method()
    }

}
