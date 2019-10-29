/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import java.util.concurrent.Executor;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.rsql.RsqlValidationOracle;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.filtermanagement.footer.TargetFilterCountMessageLabel;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * DistributionSet table layout.
 */
public class TargetFilterDetailsLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final TargetFilterDetailsGridHeader targetFilterDetailsGridHeader;
    private final TargetFilterTargetGrid targetFilterTargetGrid;
    private final TargetFilterCountMessageLabel targetFilterCountMessageLabel;

    // private final TargetFilterDetailsLayoutEventListener eventListener;

    public TargetFilterDetailsLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final UINotification uiNotification, final UiProperties uiProperties, final EntityFactory entityFactory,
            final RsqlValidationOracle rsqlValidationOracle, final Executor executor,
            final TargetManagement targetManagement, final TargetFilterQueryManagement targetFilterManagement,
            final FilterManagementUIState filterManagementUIState) {
        super(i18n, eventBus);

        final TargetFilterAddUpdateLayout targetFilterAddUpdateLayout = new TargetFilterAddUpdateLayout(i18n,
                uiProperties, filterManagementUIState, eventBus, rsqlValidationOracle, executor);
        final AddTargetFilterController addTargetFilterController = new AddTargetFilterController(i18n, entityFactory,
                eventBus, uiNotification, targetFilterManagement, targetFilterAddUpdateLayout);
        final UpdateTargetFilterController updateTargetFilterController = new UpdateTargetFilterController(i18n,
                entityFactory, eventBus, uiNotification, targetFilterManagement, targetFilterAddUpdateLayout);

        this.targetFilterDetailsGridHeader = new TargetFilterDetailsGridHeader(i18n, eventBus,
                targetFilterAddUpdateLayout, addTargetFilterController, updateTargetFilterController,
                filterManagementUIState);

        this.targetFilterTargetGrid = new TargetFilterTargetGrid(i18n, eventBus, targetManagement,
                filterManagementUIState);

        this.targetFilterCountMessageLabel = new TargetFilterCountMessageLabel(filterManagementUIState, i18n, eventBus);

        buildLayout(targetFilterDetailsGridHeader, targetFilterTargetGrid, targetFilterCountMessageLabel);
    }

    public void showAddFilterLayout() {
        targetFilterDetailsGridHeader.showAddFilterLayout();
    }

    public void showEditFilterLayout(final ProxyTargetFilterQuery proxyEntity) {
        targetFilterDetailsGridHeader.showEditFilterLayout(proxyEntity);
    }

    // public void unsubscribeListener() {
    // eventListener.unsubscribeListeners();
    // }
}
