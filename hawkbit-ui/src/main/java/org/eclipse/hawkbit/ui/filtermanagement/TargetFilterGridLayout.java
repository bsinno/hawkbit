/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * DistributionSet table layout.
 */
public class TargetFilterGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final TargetFilterGridHeader targetFilterGridHeader;
    private final TargetFilterGrid targetFilterGrid;

    // private final TargetFilterGridLayoutEventListener eventListener;

    public TargetFilterGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final EntityFactory entityFactory, final TargetFilterQueryManagement targetFilterQueryManagement,
            final TargetManagement targetManagement, final DistributionSetManagement distributionSetManagement,
            final FilterManagementUIState filterManagementUIState) {
        this.targetFilterGridHeader = new TargetFilterGridHeader(eventBus, filterManagementUIState, permissionChecker,
                i18n);

        final AutoAssignmentWindowBuilder autoAssignmentWindowBuilder = new AutoAssignmentWindowBuilder(i18n, eventBus,
                notification, entityFactory, targetManagement, targetFilterQueryManagement, distributionSetManagement);

        this.targetFilterGrid = new TargetFilterGrid(i18n, notification, eventBus, filterManagementUIState,
                targetFilterQueryManagement, permissionChecker, autoAssignmentWindowBuilder);

        buildLayout(targetFilterGridHeader, targetFilterGrid);
    }

    // public void unsubscribeListener() {
    // eventListener.unsubscribeListeners();
    // }
}
