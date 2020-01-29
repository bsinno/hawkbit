/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.actionhistory;

import java.util.Collection;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyActionStatus;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Layout responsible for action-history-grid and the corresponding header.
 */
public class ActionHistoryGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final ActionHistoryGridHeader actionHistoryHeader;
    private final ActionHistoryGrid actionHistoryGrid;

    private final ActionHistoryGridLayoutUiState actionHistoryGridLayoutUiState;

    private final transient ActionHistoryGridLayoutEventListener eventListener;

    /**
     * Constructor.
     *
     * @param i18n
     * @param deploymentManagement
     * @param eventBus
     * @param notification
     * @param managementUIState
     * @param permChecker
     */
    public ActionHistoryGridLayout(final VaadinMessageSource i18n, final DeploymentManagement deploymentManagement,
            final UIEventBus eventBus, final UINotification notification, final SpPermissionChecker permChecker,
            final ActionHistoryGridLayoutUiState actionHistoryGridLayoutUiState) {
        this.actionHistoryGridLayoutUiState = actionHistoryGridLayoutUiState;

        this.actionHistoryHeader = new ActionHistoryGridHeader(i18n, eventBus, actionHistoryGridLayoutUiState);
        this.actionHistoryGrid = new ActionHistoryGrid(i18n, deploymentManagement, eventBus, notification, permChecker,
                actionHistoryGridLayoutUiState);

        this.eventListener = new ActionHistoryGridLayoutEventListener(this, eventBus);

        buildLayout(actionHistoryHeader, actionHistoryGrid);
    }

    public void restoreState() {
        // TODO
    }

    public void onTargetSelected(final ProxyTarget target) {
        actionHistoryHeader.updateActionHistoryHeader(target != null ? target.getName() : "");
        actionHistoryGrid.updateMasterEntityFilter(target);
    }

    public void onTargetUpdated(final Collection<Long> updatedTargetIds) {
        if (actionHistoryGrid.getSelectedMasterTarget() != null
                && updatedTargetIds.contains(actionHistoryGrid.getSelectedMasterTarget().getId())) {
            // TODO
        }
    }

    public void onActionChanged(final ProxyAction entity) {
        // TODO Auto-generated method stub

    }

    public Object onActionUpdated(final Collection<Long> entityIds) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onActionStatusChanged(final ProxyActionStatus entity) {
        // TODO Auto-generated method stub

    }

    public void maximize() {
        actionHistoryGrid.createMaximizedContent();
        // TODO
        // hideDetailsLayout();
    }

    public void minimize() {
        actionHistoryGrid.createMinimizedContent();
        // TODO
        // showDetailsLayout();
    }

    public void refreshGrid() {
        actionHistoryGrid.refreshContainer();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
