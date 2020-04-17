/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rollout;

import java.util.Arrays;
import java.util.List;

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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedGridRefreshAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener.EntityModifiedAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedSelectionAwareSupport;
import org.eclipse.hawkbit.ui.common.layout.listener.SearchFilterListener;
import org.eclipse.hawkbit.ui.rollout.RolloutManagementUIState;
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

    private final transient SearchFilterListener searchFilterListener;
    private final transient EntityModifiedListener<ProxyRollout> entityModifiedListener;

    public RolloutGridLayout(final SpPermissionChecker permissionChecker,
            final RolloutManagementUIState rolloutManagementUIState, final UIEventBus eventBus,
            final RolloutManagement rolloutManagement, final TargetManagement targetManagement,
            final UINotification uiNotification, final UiProperties uiProperties, final EntityFactory entityFactory,
            final VaadinMessageSource i18n, final TargetFilterQueryManagement targetFilterQueryManagement,
            final RolloutGroupManagement rolloutGroupManagement, final QuotaManagement quotaManagement,
            final TenantConfigurationManagement tenantConfigManagement,
            final DistributionSetManagement distributionSetManagement) {
        final RolloutWindowDependencies rolloutWindowDependecies = new RolloutWindowDependencies(rolloutManagement,
                targetManagement, uiNotification, entityFactory, i18n, uiProperties, eventBus,
                targetFilterQueryManagement, rolloutGroupManagement, quotaManagement, distributionSetManagement);

        final RolloutWindowBuilder rolloutWindowBuilder = new RolloutWindowBuilder(rolloutWindowDependecies);

        this.rolloutListHeader = new RolloutGridHeader(permissionChecker, rolloutManagementUIState, eventBus, i18n,
                rolloutWindowBuilder);
        this.rolloutListGrid = new RolloutGrid(i18n, eventBus, rolloutManagement, rolloutGroupManagement,
                uiNotification, rolloutManagementUIState, permissionChecker, tenantConfigManagement,
                rolloutWindowBuilder);

        this.searchFilterListener = new SearchFilterListener(eventBus, rolloutListGrid::updateSearchFilter,
                View.ROLLOUT, Layout.ROLLOUT_LIST);
        this.entityModifiedListener = new EntityModifiedListener.Builder<>(eventBus, ProxyRollout.class)
                .entityModifiedAwareSupports(getEntityModifiedAwareSupports()).build();

        buildLayout(rolloutListHeader, rolloutListGrid);
    }

    private List<EntityModifiedAwareSupport> getEntityModifiedAwareSupports() {
        return Arrays.asList(
                EntityModifiedGridRefreshAwareSupport.of(rolloutListGrid::refreshContainer,
                        rolloutListGrid::updateGridItems),
                EntityModifiedSelectionAwareSupport.of(rolloutListGrid.getSelectionSupport(),
                        rolloutListGrid::mapIdToProxyEntity, rolloutListGrid::onSelectedRolloutDeleted));
    }

    public void restoreState() {
        rolloutListHeader.restoreState();
        rolloutListGrid.restoreState();
    }

    /**
     * unsubscribe all listener
     */
    public void unsubscribeListener() {
        searchFilterListener.unsubscribe();
        entityModifiedListener.unsubscribe();
    }
}
