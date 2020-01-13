/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.ui.AbstractHawkbitUI;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.rollout.rollout.RolloutGridLayout;
import org.eclipse.hawkbit.ui.rollout.rolloutgroup.RolloutGroupGridLayout;
import org.eclipse.hawkbit.ui.rollout.rolloutgrouptargets.RolloutGroupTargetGridLayout;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.VerticalLayout;

/**
 * Rollout management view.
 */
@UIScope
@SpringView(name = RolloutView.VIEW_NAME, ui = AbstractHawkbitUI.class)
public class RolloutView extends VerticalLayout implements View {
    private static final long serialVersionUID = 1L;

    public static final String VIEW_NAME = "rollout";
    private final SpPermissionChecker permChecker;

    private final RolloutGridLayout rolloutsLayout;
    private final RolloutGroupGridLayout rolloutGroupsLayout;
    private final RolloutGroupTargetGridLayout rolloutGroupTargetsLayout;
    private final RolloutUIState uiState;

    private final transient RolloutManagement rolloutManagement;
    private final transient EventBus.UIEventBus eventBus;

    @Autowired
    RolloutView(final SpPermissionChecker permissionChecker, final RolloutUIState uiState, final UIEventBus eventBus,
            final RolloutManagement rolloutManagement, final RolloutGroupManagement rolloutGroupManagement,
            final TargetManagement targetManagement, final UINotification uiNotification,
            final UiProperties uiProperties, final EntityFactory entityFactory, final VaadinMessageSource i18n,
            final TargetFilterQueryManagement targetFilterQueryManagement, final QuotaManagement quotaManagement,
            final TenantConfigurationManagement tenantConfigManagement,
            final DistributionSetManagement distributionSetManagement) {
        this.permChecker = permissionChecker;
        this.rolloutManagement = rolloutManagement;

        this.rolloutsLayout = new RolloutGridLayout(permissionChecker, uiState, eventBus, rolloutManagement,
                targetManagement, uiNotification, uiProperties, entityFactory, i18n, targetFilterQueryManagement,
                rolloutGroupManagement, quotaManagement, tenantConfigManagement, distributionSetManagement);
        this.rolloutGroupsLayout = new RolloutGroupGridLayout(i18n, eventBus, rolloutGroupManagement, uiState,
                permissionChecker);
        this.rolloutGroupTargetsLayout = new RolloutGroupTargetGridLayout(eventBus, i18n, uiState,
                rolloutGroupManagement);
        this.uiState = uiState;
        this.eventBus = eventBus;
    }

    @PostConstruct
    void init() {
        buildLayout();
        restoreState();
        eventBus.subscribe(this);
    }

    @PreDestroy
    void destroy() {
        eventBus.unsubscribe(this);
    }

    private void buildLayout() {
        setSpacing(false);
        setMargin(false);
        setSizeFull();

        addComponent(rolloutsLayout);
        setComponentAlignment(rolloutsLayout, Alignment.TOP_CENTER);
        setExpandRatio(rolloutsLayout, 1.0F);

        rolloutGroupsLayout.setVisible(false);
        addComponent(rolloutGroupsLayout);
        setComponentAlignment(rolloutGroupsLayout, Alignment.TOP_CENTER);
        setExpandRatio(rolloutGroupsLayout, 1.0F);

        rolloutGroupTargetsLayout.setVisible(false);
        addComponent(rolloutGroupTargetsLayout);
        setComponentAlignment(rolloutGroupTargetsLayout, Alignment.TOP_CENTER);
        setExpandRatio(rolloutGroupTargetsLayout, 1.0F);
    }

    private void showRolloutGroupTargetsListView() {
        uiState.setCurrentView(RolloutUIState.RolloutView.ROLLOUT_GROUP_TARGETS);
        rolloutsLayout.setVisible(false);
        rolloutGroupsLayout.setVisible(false);
        rolloutGroupTargetsLayout.setVisible(true);
    }

    private void showRolloutGroupListView() {
        uiState.setCurrentView(RolloutUIState.RolloutView.ROLLOUT_GROUPS);
        rolloutsLayout.setVisible(false);
        rolloutGroupTargetsLayout.setVisible(false);
        rolloutGroupsLayout.setVisible(true);
    }

    private void showRolloutListView() {
        uiState.setCurrentView(RolloutUIState.RolloutView.ROLLOUTS);
        rolloutGroupsLayout.setVisible(false);
        rolloutGroupTargetsLayout.setVisible(false);
        rolloutsLayout.setVisible(true);
    }

    private void restoreState() {
        switch (uiState.getCurrentView()) {
        case ROLLOUTS:
            showRolloutListView();
            break;
        case ROLLOUT_GROUPS:
            showRolloutGroupListView();
            break;
        case ROLLOUT_GROUP_TARGETS:
            showRolloutGroupTargetsListView();
            break;
        }
        // TODO restoreStates
    }

    @Override
    public void enter(final ViewChangeEvent event) {
        // This view is constructed in the init() method()
    }

}
