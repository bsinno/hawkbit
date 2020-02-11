/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rolloutgroup;

import java.util.Collection;

import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.rollout.state.RolloutGroupLayoutUIState;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Groups List View.
 */
public class RolloutGroupGridLayout extends AbstractGridComponentLayout {

    private static final long serialVersionUID = 1L;

    private final RolloutGroupGridHeader rolloutGroupsListHeader;
    private final RolloutGroupGrid rolloutGroupListGrid;
    private final RolloutGroupLayoutUIState uiState;

    private final transient RolloutGroupGridLayoutEventListener eventListener;

    /**
     * Constructor for RolloutGroupsListView
     * 
     * @param i18n
     *            I18N
     * @param eventBus
     *            UIEventBus
     * @param rolloutGroupManagement
     *            RolloutGroupManagement
     * @param uiState
     *            RolloutGroupLayoutUIState
     * @param permissionChecker
     *            SpPermissionChecker
     */
    public RolloutGroupGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final RolloutGroupManagement rolloutGroupManagement, final RolloutGroupLayoutUIState uiState,
            final SpPermissionChecker permissionChecker) {
        this.rolloutGroupsListHeader = new RolloutGroupGridHeader(eventBus, uiState, i18n);
        this.rolloutGroupListGrid = new RolloutGroupGrid(i18n, eventBus, permissionChecker, rolloutGroupManagement,
                uiState);
        this.uiState = uiState;

        this.eventListener = new RolloutGroupGridLayoutEventListener(this, eventBus);

        buildLayout(rolloutGroupsListHeader, rolloutGroupListGrid);
    }

    public void showGroupsForRollout(final Long parentEntityId, final String parentEntityName) {
        uiState.setSelectedRolloutId(parentEntityId);
        uiState.setSelectedRolloutName(parentEntityName);
        rolloutGroupsListHeader.setRolloutName(parentEntityName);
        rolloutGroupListGrid.updateMasterEntityFilter(parentEntityId);
    }

    public void restoreState() {
        showGroupsForRollout(uiState.getSelectedRolloutId(), uiState.getSelectedRolloutName());
    }

    public Long getCurrentParentRolloutId() {
        return uiState.getSelectedRolloutId();
    }

    public void refreshGridItems(final Collection<Long> ids) {
        rolloutGroupListGrid.updateGridItems(ids);
    }

    public Layout getLayout() {
        return Layout.ROLLOUT_GROUP_LIST;
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
