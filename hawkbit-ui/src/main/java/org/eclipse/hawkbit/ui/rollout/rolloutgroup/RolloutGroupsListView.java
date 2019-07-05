/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rolloutgroup;

import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.RolloutGroupToProxyRolloutGroupMapper;
import org.eclipse.hawkbit.ui.common.data.providers.RolloutGroupDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Grid;

/**
 * Groups List View.
 */
public class RolloutGroupsListView extends AbstractGridComponentLayout<ProxyRolloutGroup> {

    private static final long serialVersionUID = 1L;

    private final SpPermissionChecker permissionChecker;
    private final RolloutUIState rolloutUIState;
    private final transient RolloutGroupManagement rolloutGroupManagement;

    private final RolloutGroupDataProvider rolloutGroupDataProvider;

    /**
     * Constructor for RolloutGroupsListView
     * 
     * @param i18n
     *            I18N
     * @param eventBus
     *            UIEventBus
     * @param rolloutGroupManagement
     *            RolloutGroupManagement
     * @param rolloutUIState
     *            RolloutUIState
     * @param permissionChecker
     *            SpPermissionChecker
     */
    public RolloutGroupsListView(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final RolloutGroupManagement rolloutGroupManagement, final RolloutUIState rolloutUIState,
            final SpPermissionChecker permissionChecker) {
        super(i18n, eventBus);
        this.permissionChecker = permissionChecker;
        this.rolloutUIState = rolloutUIState;
        this.rolloutGroupManagement = rolloutGroupManagement;
        this.rolloutGroupDataProvider = new RolloutGroupDataProvider(rolloutGroupManagement, rolloutUIState,
                new RolloutGroupToProxyRolloutGroupMapper());
        init();
    }

    @Override
    protected boolean doSubscribeToEventBus() {
        return false;
    }

    @Override
    public RolloutGroupsListHeader createGridHeader() {
        return new RolloutGroupsListHeader(getEventBus(), rolloutUIState, getI18n());
    }

    @Override
    public Grid<ProxyRolloutGroup> createGrid() {
        return new RolloutGroupListGrid(getI18n(), getEventBus(), rolloutGroupManagement, rolloutUIState,
                permissionChecker, rolloutGroupDataProvider);
    }

}
