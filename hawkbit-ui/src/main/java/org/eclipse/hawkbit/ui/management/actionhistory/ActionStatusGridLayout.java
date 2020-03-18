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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAction;
import org.eclipse.hawkbit.ui.common.event.SelectionChangedEventPayload.SelectionChangedEventType;
import org.eclipse.hawkbit.ui.common.layout.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Layout responsible for action-states-grid and the corresponding header.
 */
public class ActionStatusGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final ActionStatusGridHeader actionStatusGridHeader;
    private final ActionStatusGrid actionStatusGrid;

    private final transient ActionStatusGridLayoutEventListener eventListener;

    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     */
    public ActionStatusGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final DeploymentManagement deploymentManagement) {
        this.actionStatusGridHeader = new ActionStatusGridHeader(i18n);
        this.actionStatusGrid = new ActionStatusGrid(i18n, eventBus, deploymentManagement);

        this.eventListener = new ActionStatusGridLayoutEventListener(this, eventBus);

        buildLayout(actionStatusGridHeader, actionStatusGrid);
    }

    public void onActionChanged(final ProxyAction action) {
        actionStatusGrid.updateMasterEntityFilter(action != null ? action.getId() : null);

        if (actionStatusGrid.getSelectedItems().size() == 1) {
            // we do not need to fetch the updated action status from backend
            // here, because we only need to refresh messages based on id
            actionStatusGrid.getSelectionSupport().sendSelectionChangedEvent(SelectionChangedEventType.ENTITY_SELECTED,
                    actionStatusGrid.getSelectedItems().iterator().next());
        }
    }

    public void maximize() {
        actionStatusGrid.createMaximizedContent();
        actionStatusGrid.getSelectionSupport().selectFirstRow();
    }

    public void minimize() {
        actionStatusGrid.createMinimizedContent();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
