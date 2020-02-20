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
import java.util.Optional;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetToProxyTargetMapper;
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

    private final transient TargetManagement targetManagement;
    private final transient TargetToProxyTargetMapper targetMapper;

    private final ActionHistoryGridHeader actionHistoryHeader;
    private final ActionHistoryGrid actionHistoryGrid;

    private final transient ActionHistoryGridLayoutEventListener eventListener;

    /**
     * Constructor.
     *
     * @param i18n
     * @param deploymentManagement
     * @param eventBus
     * @param notification
     * @param permChecker
     */
    public ActionHistoryGridLayout(final VaadinMessageSource i18n, final DeploymentManagement deploymentManagement,
            final TargetManagement targetManagement, final UIEventBus eventBus, final UINotification notification,
            final SpPermissionChecker permChecker,
            final ActionHistoryGridLayoutUiState actionHistoryGridLayoutUiState) {
        this.targetManagement = targetManagement;
        this.targetMapper = new TargetToProxyTargetMapper(i18n);

        this.actionHistoryHeader = new ActionHistoryGridHeader(i18n, eventBus, actionHistoryGridLayoutUiState);
        this.actionHistoryGrid = new ActionHistoryGrid(i18n, deploymentManagement, eventBus, notification, permChecker,
                actionHistoryGridLayoutUiState);

        this.eventListener = new ActionHistoryGridLayoutEventListener(this, eventBus);

        buildLayout(actionHistoryHeader, actionHistoryGrid);
    }

    public void restoreState() {
        actionHistoryHeader.restoreState();
    }

    public void onTargetChanged(final ProxyTarget target) {
        actionHistoryHeader.updateActionHistoryHeader(target != null ? target.getName() : "");
        actionHistoryGrid.updateMasterEntityFilter(target);
    }

    public void onTargetUpdated(final Collection<Long> updatedTargetIds) {
        final Long masterEntityId = actionHistoryGrid.getMasterEntityId();

        if (masterEntityId != null && updatedTargetIds.contains(masterEntityId)) {
            mapTargetIdToProxyEntity(masterEntityId).ifPresent(this::onTargetChanged);
        }
    }

    // TODO: should we really make a database call here?
    private Optional<ProxyTarget> mapTargetIdToProxyEntity(final Long entityId) {
        return targetManagement.get(entityId).map(targetMapper::map);
    }

    public void maximize() {
        actionHistoryGrid.createMaximizedContent();
    }

    public void minimize() {
        actionHistoryGrid.createMinimizedContent();
    }

    public void refreshGrid() {
        actionHistoryGrid.refreshContainer();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
