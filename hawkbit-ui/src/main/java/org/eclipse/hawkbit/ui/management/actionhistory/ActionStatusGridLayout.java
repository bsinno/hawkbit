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
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Layout responsible for action-states-grid and the corresponding header.
 */
public class ActionStatusGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final ActionStatusGridHeader actionStatusGridHeader;
    private final ActionStatusGrid actionStatusGrid;

    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     * @param managementUIState
     */
    public ActionStatusGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final DeploymentManagement deploymentManagement,
            final ActionStatusGridLayoutUiState actionStatusGridLayoutUiState) {
        this.actionStatusGridHeader = new ActionStatusGridHeader(i18n);
        this.actionStatusGrid = new ActionStatusGrid(i18n, eventBus, deploymentManagement,
                actionStatusGridLayoutUiState);

        buildLayout(actionStatusGridHeader, actionStatusGrid);
    }
}
