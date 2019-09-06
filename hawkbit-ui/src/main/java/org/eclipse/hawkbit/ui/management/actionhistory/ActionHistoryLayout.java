/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.actionhistory;

import java.util.Optional;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

/**
 * Layout responsible for action-history-grid and the corresponding header.
 */
public class ActionHistoryLayout extends AbstractGridComponentLayout<ProxyAction> {
    private static final long serialVersionUID = 1L;

    private final ManagementUIState managementUIState;

    private final ActionHistoryGridHeader actionHistoryHeader;
    private final ActionHistoryGrid actionHistoryGrid;

    private final MasterDetailsSupport<ProxyTarget, String> masterDetailsSupport;

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
    public ActionHistoryLayout(final VaadinMessageSource i18n, final DeploymentManagement deploymentManagement,
            final UIEventBus eventBus, final UINotification notification, final ManagementUIState managementUIState,
            final SpPermissionChecker permChecker) {
        super(i18n, eventBus);

        this.managementUIState = managementUIState;

        this.actionHistoryHeader = new ActionHistoryGridHeader(i18n, managementUIState);
        this.actionHistoryGrid = new ActionHistoryGrid(getI18n(), deploymentManagement, getEventBus(), notification,
                managementUIState, permChecker);

        this.masterDetailsSupport = new MasterDetailsSupport<ProxyTarget, String>(actionHistoryGrid) {

            // TODO: check if Target controllerId could be changed to Id
            @Override
            protected String mapMasterItemToDetailsFilter(final ProxyTarget masterItem) {
                actionHistoryGrid.setSelectedMasterTarget(masterItem);
                return masterItem.getControllerId();
            }
        };

        init();
    }

    @Override
    public ActionHistoryGridHeader getGridHeader() {
        return actionHistoryHeader;
    }

    @Override
    public ActionHistoryGrid getGrid() {
        return actionHistoryGrid;
    }

    // TODO: check if it can be removed with registerDetails in Deployment View
    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final TargetTableEvent targetUIEvent) {
        final Optional<Long> targetId = managementUIState.getLastSelectedTargetId();

        if (BaseEntityEventType.SELECTED_ENTITY == targetUIEvent.getEventType()) {
            setData(getI18n().getMessage(UIMessageIdProvider.MESSAGE_DATA_AVAILABLE));
            UI.getCurrent().access(() -> populateActionHistoryDetails(targetUIEvent.getEntity()));
        } else if (BaseEntityEventType.REMOVE_ENTITY == targetUIEvent.getEventType() && targetId.isPresent()
                && targetUIEvent.getEntityIds().contains(targetId.get())) {
            setData(getI18n().getMessage(UIMessageIdProvider.MESSAGE_NO_DATA));
            UI.getCurrent().access(this::populateActionHistoryDetails);
        }
    }

    /**
     * Populate action header and table for the target.
     *
     * @param target
     *            the target
     */
    private void populateActionHistoryDetails(final ProxyTarget target) {
        if (target != null) {
            actionHistoryHeader.updateActionHistoryHeader(target.getName());
            masterDetailsSupport.masterItemChangedCallback(target);
        } else {
            actionHistoryHeader.updateActionHistoryHeader(" ");
        }
    }

    /**
     * Populate empty action header and empty table for empty selection.
     */
    private void populateActionHistoryDetails() {
        actionHistoryHeader.updateActionHistoryHeader(" ");
        masterDetailsSupport.masterItemChangedCallback(null);
    }

    public MasterDetailsSupport<ProxyTarget, String> getMasterDetailsSupport() {
        return masterDetailsSupport;
    }
}
