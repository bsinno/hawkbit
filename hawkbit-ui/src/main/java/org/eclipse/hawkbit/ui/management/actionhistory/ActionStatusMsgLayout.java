/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.actionhistory;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyActionStatus;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMessage;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.grid.DefaultGridHeader;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupportIdentifiable;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Layout responsible for messages-grid and the corresponding header.
 */
public class ActionStatusMsgLayout extends AbstractGridComponentLayout<ProxyMessage> {
    private static final long serialVersionUID = 1L;

    private final DefaultGridHeader actionStatusMsgHeader;
    private final ActionStatusMsgGrid actionStatusMsgGrid;

    private final MasterDetailsSupport<ProxyActionStatus, Long> masterDetailsSupport;

    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     * @param managementUIState
     * @param deploymentManagement
     */
    public ActionStatusMsgLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final ManagementUIState managementUIState, final DeploymentManagement deploymentManagement) {
        super(i18n, eventBus);

        this.actionStatusMsgHeader = new DefaultGridHeader(managementUIState,
                getI18n().getMessage(UIMessageIdProvider.CAPTION_ACTION_MESSAGES), getI18n()).init();
        this.actionStatusMsgGrid = new ActionStatusMsgGrid(getI18n(), getEventBus(), deploymentManagement);

        this.masterDetailsSupport = new MasterDetailsSupportIdentifiable<>(actionStatusMsgGrid);

        init();
    }

    @Override
    protected boolean doSubscribeToEventBus() {
        return false;
    }

    @Override
    public DefaultGridHeader getGridHeader() {
        return actionStatusMsgHeader;
    }

    @Override
    public ActionStatusMsgGrid getGrid() {
        return actionStatusMsgGrid;
    }

    public MasterDetailsSupport<ProxyActionStatus, Long> getMasterDetailsSupport() {
        return masterDetailsSupport;
    }
}
