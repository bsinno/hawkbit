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
import org.eclipse.hawkbit.ui.common.event.ChangeUiElementPayload;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.filtermanagement.footer.TargetFilterCountMessageLabel;
import org.eclipse.hawkbit.ui.filtermanagement.state.TargetFilterDetailsLayoutUiState;
import org.eclipse.hawkbit.ui.filtermanagement.state.TargetFilterDetailsLayoutUiState.Mode;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * DistributionSet table layout.
 */
public class TargetFilterDetailsLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final TargetFilterDetailsLayoutUiState uiState;

    private final TargetFilterDetailsGridHeader targetFilterDetailsGridHeader;
    private final TargetFilterTargetGrid targetFilterTargetGrid;
    private final transient TargetFilterCountMessageLabel targetFilterCountMessageLabel;

    private final UIEventBus eventBus;
    private final TargetFilterDetailsLayoutEventListener eventListener;
    private final TargetFilterAddUpdateLayout targetFilterAddUpdateLayout;

    public TargetFilterDetailsLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final UINotification uiNotification, final UiProperties uiProperties, final EntityFactory entityFactory,
            final RsqlValidationOracle rsqlValidationOracle, final Executor executor,
            final TargetManagement targetManagement, final TargetFilterQueryManagement targetFilterManagement,
            final TargetFilterDetailsLayoutUiState uiState) {
        this.uiState = uiState;
        this.eventBus = eventBus;
        this.eventListener = new TargetFilterDetailsLayoutEventListener(this, eventBus);

        this.targetFilterAddUpdateLayout = new TargetFilterAddUpdateLayout(i18n, uiProperties, uiState, eventBus,
                rsqlValidationOracle, executor);
        final AddTargetFilterController addTargetFilterController = new AddTargetFilterController(i18n, entityFactory,
                eventBus, uiNotification, targetFilterManagement, targetFilterAddUpdateLayout);
        final UpdateTargetFilterController updateTargetFilterController = new UpdateTargetFilterController(i18n,
                entityFactory, eventBus, uiNotification, targetFilterManagement, targetFilterAddUpdateLayout);

        this.targetFilterDetailsGridHeader = new TargetFilterDetailsGridHeader(i18n, eventBus,
                targetFilterAddUpdateLayout, addTargetFilterController, updateTargetFilterController);

        this.targetFilterTargetGrid = new TargetFilterTargetGrid(i18n, eventBus, targetManagement, uiState);

        this.targetFilterCountMessageLabel = new TargetFilterCountMessageLabel(i18n);

        buildLayout(targetFilterDetailsGridHeader, targetFilterTargetGrid, targetFilterCountMessageLabel);
    }

    public void showAddFilterLayout() {
        uiState.setCurrentMode(Mode.CREATE);
        targetFilterDetailsGridHeader.showAddFilterLayout();
        targetFilterTargetGrid.updateTargetFilterQueryFilter(null);
    }

    public void showEditFilterLayout(final ProxyTargetFilterQuery proxyEntity) {
        uiState.setCurrentMode(Mode.EDIT);
        uiState.setTargetFilterQueryforEdit(proxyEntity);
        targetFilterDetailsGridHeader.showEditFilterLayout(proxyEntity);
        targetFilterTargetGrid.updateTargetFilterQueryFilter(proxyEntity.getQuery());
    }

    public void onSearchFilterChanged(final String newFilter) {
        targetFilterTargetGrid.updateTargetFilterQueryFilter(newFilter);
    }

    public void onGridUpdated(final long totalTargetCount) {
        targetFilterCountMessageLabel.updateTotalFilteredTargetsCount(totalTargetCount);
    }

    public void onClose() {
        // TODO is it OK to just republisch the Event so it comes from this
        // class?
        eventBus.publish(EventTopics.CHANGE_UI_ELEMENT_STATE, this, ChangeUiElementPayload.CLOSE);
    }

    public void restoreState() {
        if (Mode.EDIT.equals(uiState.getCurrentMode())) {
            uiState.getTargetFilterQueryforEdit().ifPresent(this::showEditFilterLayout);
        } else if (Mode.CREATE.equals(uiState.getCurrentMode())) {
            showAddFilterLayout();
        }
        targetFilterAddUpdateLayout.restoreState();
        targetFilterTargetGrid.restoreState();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
