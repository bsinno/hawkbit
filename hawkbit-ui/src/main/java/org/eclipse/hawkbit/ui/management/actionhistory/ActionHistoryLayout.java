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
import org.eclipse.hawkbit.ui.common.grid.DefaultGridHeader;
import org.eclipse.hawkbit.ui.common.grid.DefaultGridHeader.AbstractHeaderMaximizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
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
    private final String actionHistoryCaption;

    private final ActionHistoryHeader actionHistoryHeader;
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
        this.actionHistoryCaption = getActionHistoryCaption(null);

        this.actionHistoryHeader = new ActionHistoryHeader(managementUIState).init();
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

    private String getActionHistoryCaption(final String targetName) {
        final String caption;
        if (StringUtils.hasText(targetName)) {
            caption = getI18n().getMessage(UIMessageIdProvider.CAPTION_ACTION_HISTORY_FOR,
                    HawkbitCommonUtil.getBoldHTMLText(targetName));
        } else {
            caption = getI18n().getMessage(UIMessageIdProvider.CAPTION_ACTION_HISTORY);
        }

        return HawkbitCommonUtil.getCaptionText(caption);
    }

    @Override
    public ActionHistoryHeader getGridHeader() {
        return actionHistoryHeader;
    }

    @Override
    public ActionHistoryGrid getGrid() {
        return actionHistoryGrid;
    }

    @Override
    public void registerDetails(final MasterDetailsSupport<ProxyAction, ?> detailsSupport) {
        getGrid().addSelectionListener(event -> {
            final ProxyAction selectedAction = event.getFirstSelectedItem().orElse(null);
            if (managementUIState.isActionHistoryMaximized()) {
                detailsSupport.masterItemChangedCallback(selectedAction);
            }
        });
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
        if (null != target) {
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

    /**
     * Header for ActionHistory with maximize-support.
     */
    class ActionHistoryHeader extends DefaultGridHeader {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param managementUIState
         */
        ActionHistoryHeader(final ManagementUIState managementUIState) {
            super(managementUIState, actionHistoryCaption, getI18n());
            this.setHeaderMaximizeSupport(
                    new ActionHistoryHeaderMaxSupport(this, SPUIDefinitions.EXPAND_ACTION_HISTORY));
        }

        /**
         * Initializes the header.
         */
        @Override
        public ActionHistoryHeader init() {
            super.init();
            restorePreviousState();
            return this;
        }

        /**
         * Updates header with target name.
         *
         * @param targetName
         *            name of the target
         */
        public void updateActionHistoryHeader(final String targetName) {
            updateTitle(getActionHistoryCaption(targetName));
        }

        /**
         * Restores the previous min-max state.
         */
        private void restorePreviousState() {
            if (hasHeaderMaximizeSupport() && managementUIState.isActionHistoryMaximized()) {
                getHeaderMaximizeSupport().showMinIcon();
            }
        }
    }

    /**
     * Min-max support for header.
     */
    class ActionHistoryHeaderMaxSupport extends AbstractHeaderMaximizeSupport {

        private final DefaultGridHeader abstractGridHeader;

        /**
         * Constructor.
         *
         * @param abstractGridHeader
         * @param maximizeButtonId
         */
        protected ActionHistoryHeaderMaxSupport(final DefaultGridHeader abstractGridHeader,
                final String maximizeButtonId) {
            abstractGridHeader.super(maximizeButtonId);
            this.abstractGridHeader = abstractGridHeader;
        }

        @Override
        protected void maximize() {
            // TODO: check if it is needed
            // details.populateMasterDataAndRecreateContainer(masterForDetails);
            getEventBus().publish(this, ManagementUIEvent.MAX_ACTION_HISTORY);
        }

        @Override
        protected void minimize() {
            getEventBus().publish(this, ManagementUIEvent.MIN_ACTION_HISTORY);
        }

        /**
         * Gets the grid header the maximize support is for.
         *
         * @return grid header
         */
        protected DefaultGridHeader getGridHeader() {
            return abstractGridHeader;
        }
    }
}
