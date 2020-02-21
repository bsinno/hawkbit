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
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.ActionToProxyActionMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Layout responsible for action-history-grid and the corresponding header.
 */
public class ActionHistoryGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final transient DeploymentManagement deploymentManagement;
    private final transient ActionToProxyActionMapper actionMapper;

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
            final UIEventBus eventBus, final UINotification notification, final SpPermissionChecker permChecker,
            final ActionHistoryGridLayoutUiState actionHistoryGridLayoutUiState) {
        this.deploymentManagement = deploymentManagement;
        this.actionMapper = new ActionToProxyActionMapper();

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

    public void onActionUpdated(final Long targetId, final Collection<Long> entityIds) {
        final Long masterEntityId = actionHistoryGrid.getMasterEntityId();

        if (masterEntityId != null && masterEntityId.equals(targetId)) {
            actionHistoryGrid.refreshContainer();

            if (actionHistoryGrid.getSelectedItems().size() == 1) {
                final Long selectedEntityId = actionHistoryGrid.getSelectedItems().iterator().next().getId();

                entityIds.stream().filter(entityId -> entityId.equals(selectedEntityId)).findAny()
                        .ifPresent(updatedEntityId -> mapIdToProxyEntity(updatedEntityId).ifPresent(
                                updatedEntity -> actionHistoryGrid.getSelectionSupport().sendSelectionChangedEvent(
                                        SelectionChangedEventType.ENTITY_SELECTED, updatedEntity)));
            }
        }
    }

    // TODO: extract to parent abstract #mapIdToProxyEntity?
    private Optional<ProxyAction> mapIdToProxyEntity(final Long entityId) {
        return deploymentManagement.findAction(entityId).map(actionMapper::map);
    }

    public void maximize() {
        actionHistoryGrid.createMaximizedContent();
        actionHistoryGrid.getSelectionSupport().selectFirstRow();
    }

    public void minimize() {
        actionHistoryGrid.createMinimizedContent();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
