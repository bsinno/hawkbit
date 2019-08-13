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
import org.eclipse.hawkbit.ui.common.data.mappers.RolloutToProxyRolloutMapper;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetFilterQueryToProxyTargetFilterMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.providers.RolloutDataProvider;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Rollout list view.
 */
public class RolloutListView extends AbstractGridComponentLayout<ProxyRollout> {
    private static final long serialVersionUID = 1L;

    private final RolloutListHeader rolloutListHeader;
    private final RolloutListGrid rolloutListGrid;

    public RolloutListView(final SpPermissionChecker permissionChecker, final RolloutUIState rolloutUIState,
            final UIEventBus eventBus, final RolloutManagement rolloutManagement,
            final TargetManagement targetManagement, final UINotification uiNotification,
            final UiProperties uiProperties, final EntityFactory entityFactory, final VaadinMessageSource i18n,
            final TargetFilterQueryManagement targetFilterQueryManagement,
            final RolloutGroupManagement rolloutGroupManagement, final QuotaManagement quotaManagement,
            final TenantConfigurationManagement tenantConfigManagement,
            final DistributionSetManagement distributionSetManagement) {
        super(i18n, eventBus);

        final RolloutDataProvider rolloutDataProvider = new RolloutDataProvider(rolloutManagement, rolloutUIState,
                new RolloutToProxyRolloutMapper());
        final DistributionSetStatelessDataProvider distributionSetDataProvider = new DistributionSetStatelessDataProvider(
                distributionSetManagement, new DistributionSetToProxyDistributionMapper());
        // TODO update DataProvider filter with search text
        final TargetFilterQueryDataProvider targetFilterQueryDataProvider = new TargetFilterQueryDataProvider(
                targetFilterQueryManagement, new TargetFilterQueryToProxyTargetFilterMapper());

        this.rolloutListHeader = new RolloutListHeader(permissionChecker, rolloutUIState, getEventBus(),
                rolloutManagement, targetManagement, uiNotification, uiProperties, entityFactory, getI18n(),
                targetFilterQueryManagement, rolloutGroupManagement, quotaManagement, distributionSetDataProvider,
                targetFilterQueryDataProvider);
        this.rolloutListGrid = new RolloutListGrid(getI18n(), getEventBus(), rolloutManagement, uiNotification,
                rolloutUIState, permissionChecker, targetManagement, entityFactory, uiProperties,
                targetFilterQueryManagement, rolloutGroupManagement, quotaManagement, tenantConfigManagement,
                rolloutDataProvider, distributionSetDataProvider, targetFilterQueryDataProvider);

        init();
    }

    @Override
    protected boolean doSubscribeToEventBus() {
        return false;
    }

    @Override
    public RolloutListHeader getGridHeader() {
        return rolloutListHeader;
    }

    @Override
    public RolloutListGrid getGrid() {
        return rolloutListGrid;
    }
}
