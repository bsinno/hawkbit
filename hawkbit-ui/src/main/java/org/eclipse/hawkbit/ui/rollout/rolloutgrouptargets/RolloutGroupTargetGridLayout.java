/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rolloutgrouptargets;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.common.layout.listener.EntityModifiedListener;
import org.eclipse.hawkbit.ui.common.layout.listener.MasterEntityChangedListener;
import org.eclipse.hawkbit.ui.filtermanagement.TargetFilterCountMessageLabel;
import org.eclipse.hawkbit.ui.rollout.RolloutManagementUIState;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Rollout Group Targets List View.
 */
public class RolloutGroupTargetGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final RolloutGroupTargetGridHeader rolloutGroupTargetsListHeader;
    private final RolloutGroupTargetGrid rolloutGroupTargetsListGrid;
    private final transient TargetFilterCountMessageLabel rolloutGroupTargetCountMessageLabel;

    private final transient MasterEntityChangedListener<ProxyRolloutGroup> masterEntityChangedListener;
    private final transient EntityModifiedListener<ProxyRolloutGroup> entityModifiedListener;

    public RolloutGroupTargetGridLayout(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final RolloutGroupManagement rolloutGroupManagement,
            final RolloutManagementUIState rolloutManagementUIState) {
        this.rolloutGroupTargetsListHeader = new RolloutGroupTargetGridHeader(eventBus, i18n, rolloutManagementUIState);
        this.rolloutGroupTargetsListGrid = new RolloutGroupTargetGrid(i18n, eventBus, rolloutGroupManagement,
                rolloutManagementUIState);
        this.rolloutGroupTargetCountMessageLabel = new TargetFilterCountMessageLabel(i18n);

        initGridDataUpdatedListener();

        this.masterEntityChangedListener = new MasterEntityChangedListener<>(eventBus, getMasterEntityAwareComponents(),
                getView(), Layout.ROLLOUT_GROUP_LIST);

        this.entityModifiedListener = new EntityModifiedListener.Builder<>(eventBus,
                rolloutGroupTargetsListGrid::refreshContainer, ProxyRolloutGroup.class)
                        .parentEntityType(ProxyRollout.class).build();

        buildLayout(rolloutGroupTargetsListHeader, rolloutGroupTargetsListGrid, rolloutGroupTargetCountMessageLabel);
    }

    private void initGridDataUpdatedListener() {
        rolloutGroupTargetsListGrid.getFilterDataProvider()
                .addDataProviderListener(event -> rolloutGroupTargetCountMessageLabel
                        .updateTotalFilteredTargetsCount(rolloutGroupTargetsListGrid.getDataSize()));
    }

    private List<MasterEntityAwareComponent<ProxyRolloutGroup>> getMasterEntityAwareComponents() {
        return Arrays.asList(rolloutGroupTargetsListHeader, rolloutGroupTargetsListGrid);
    }

    private Optional<Long> getMasterEntityId() {
        return Optional.ofNullable(rolloutGroupTargetsListGrid.getMasterEntityId());
    }

    public void restoreState() {
        rolloutGroupTargetsListHeader.restoreState();
        // TODO:
        // rolloutGroupTargetsListGrid.restoreState();
    }

    public Layout getLayout() {
        return Layout.ROLLOUT_GROUP_TARGET_LIST;
    }

    public View getView() {
        return View.ROLLOUT;
    }

    /**
     * unsubscribe all listener
     */
    public void unsubscribeListener() {
        masterEntityChangedListener.unsubscribe();
        entityModifiedListener.unsubscribe();
    }
}
