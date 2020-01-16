/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rollout;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetFilterQueryToProxyTargetFilterMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.rollout.event.RolloutsLayoutEventListener;
import org.eclipse.hawkbit.ui.rollout.state.RolloutLayoutUIState;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowBuilder;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Rollout list view.
 */
public class RolloutGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final RolloutGridHeader rolloutListHeader;
    private final RolloutGrid rolloutListGrid;
    private final transient RolloutsLayoutEventListener eventListener;

    public RolloutGridLayout(final SpPermissionChecker permissionChecker, final RolloutLayoutUIState uiState,
            final UIEventBus eventBus, final RolloutManagement rolloutManagement,
            final TargetManagement targetManagement, final UINotification uiNotification,
            final UiProperties uiProperties, final EntityFactory entityFactory, final VaadinMessageSource i18n,
            final TargetFilterQueryManagement targetFilterQueryManagement,
            final RolloutGroupManagement rolloutGroupManagement, final QuotaManagement quotaManagement,
            final TenantConfigurationManagement tenantConfigManagement,
            final DistributionSetManagement distributionSetManagement) {
        final DistributionSetStatelessDataProvider distributionSetDataProvider = new DistributionSetStatelessDataProvider(
                distributionSetManagement, new DistributionSetToProxyDistributionMapper());
        // TODO update DataProvider filter with search text
        final TargetFilterQueryDataProvider targetFilterQueryDataProvider = new TargetFilterQueryDataProvider(
                targetFilterQueryManagement, new TargetFilterQueryToProxyTargetFilterMapper());

        final RolloutWindowDependencies rolloutWindowDependecies = new RolloutWindowDependencies(rolloutManagement,
                targetManagement, uiNotification, entityFactory, i18n, uiProperties, eventBus,
                targetFilterQueryManagement, rolloutGroupManagement, quotaManagement, distributionSetDataProvider,
                targetFilterQueryDataProvider);
        final RolloutWindowBuilder rolloutWindowBuilder = new RolloutWindowBuilder(rolloutWindowDependecies);

        this.rolloutListHeader = new RolloutGridHeader(permissionChecker, uiState, eventBus, i18n,
                rolloutWindowBuilder);
        this.rolloutListGrid = new RolloutGrid(i18n, eventBus, rolloutManagement, uiNotification, uiState,
                permissionChecker, tenantConfigManagement, rolloutWindowBuilder);
        this.eventListener = new RolloutsLayoutEventListener(this, eventBus);

        buildLayout(rolloutListHeader, rolloutListGrid);
    }

    /**
     * refresh the grid showing all rollouts
     */
    public void refreshGrid() {
        rolloutListGrid.getDataProvider().refreshAll();
    }

    /**
     * unsubscribe all listener
     */
    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
