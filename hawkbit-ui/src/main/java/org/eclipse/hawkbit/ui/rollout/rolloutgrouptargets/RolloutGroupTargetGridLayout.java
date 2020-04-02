/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rolloutgrouptargets;

import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.filtermanagement.TargetFilterCountMessageLabel;
import org.eclipse.hawkbit.ui.rollout.state.RolloutGroupTargetLayoutUIState;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Rollout Group Targets List View.
 */
public class RolloutGroupTargetGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final RolloutGroupTargetGridHeader rolloutGroupTargetsListHeader;
    private final RolloutGroupTargetGrid rolloutGroupTargetsListGrid;
    private final RolloutGroupTargetLayoutUIState uiState;
    private final transient TargetFilterCountMessageLabel rolloutGroupTargetCountMessageLabel;

    private final transient RolloutGroupTargetGridLayoutEventListener eventListener;

    public RolloutGroupTargetGridLayout(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final RolloutGroupManagement rolloutGroupManagement, final RolloutGroupTargetLayoutUIState uiState) {
        this.rolloutGroupTargetsListHeader = new RolloutGroupTargetGridHeader(eventBus, i18n, uiState);
        this.rolloutGroupTargetsListGrid = new RolloutGroupTargetGrid(i18n, eventBus, rolloutGroupManagement, uiState);
        this.rolloutGroupTargetCountMessageLabel = new TargetFilterCountMessageLabel(i18n);
        this.uiState = uiState;

        initGridDataUpdatedListener();

        this.eventListener = new RolloutGroupTargetGridLayoutEventListener(this, eventBus);

        buildLayout(rolloutGroupTargetsListHeader, rolloutGroupTargetsListGrid, rolloutGroupTargetCountMessageLabel);
    }

    private void initGridDataUpdatedListener() {
        rolloutGroupTargetsListGrid.getFilterDataProvider()
                .addDataProviderListener(event -> rolloutGroupTargetCountMessageLabel
                        .updateTotalFilteredTargetsCount(rolloutGroupTargetsListGrid.getDataSize()));
    }

    public void showTargetsForGroup(final Long groupId, final String groupName, final String rolloutName) {
        uiState.setSelectedRolloutGroupId(groupId);
        uiState.setParentRolloutName(rolloutName);
        uiState.setSelectedRolloutGroupName(groupName);
        rolloutGroupTargetsListHeader.setRolloutName(rolloutName);
        rolloutGroupTargetsListHeader.setRolloutGroupName(groupName);
        rolloutGroupTargetsListGrid.updateMasterEntityFilter(groupId);
    }

    public Long getCurrentRolloutGroupId() {
        return uiState.getSelectedRolloutGroupId();
    }

    public void restoreState() {
        showTargetsForGroup(uiState.getSelectedRolloutGroupId(), uiState.getSelectedRolloutGroupName(),
                uiState.getParentRolloutName());
    }

    public void refreshGridItems() {
        rolloutGroupTargetsListGrid.getDataProvider().refreshAll();
    }

    public Layout getLayout() {
        return Layout.ROLLOUT_GROUP_TARGET_LIST;
    }

    /**
     * unsubscribe all listener
     */
    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
