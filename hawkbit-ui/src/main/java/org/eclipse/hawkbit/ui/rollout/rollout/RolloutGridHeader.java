/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rollout;

import java.util.Arrays;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.AddHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
import org.eclipse.hawkbit.ui.rollout.event.RolloutEvent;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowBuilder;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Header layout of rollout list view.
 */
public class RolloutGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final RolloutUIState rolloutUIState;

    private final transient RolloutWindowBuilder rolloutWindowBuilder;

    private final transient SearchHeaderSupport searchHeaderSupport;
    private final transient AddHeaderSupport addHeaderSupport;

    RolloutGridHeader(final SpPermissionChecker permissionChecker, final RolloutUIState rolloutUIState,
            final UIEventBus eventBus, final RolloutManagement rolloutManagement,
            final TargetManagement targetManagement, final UINotification uiNotification,
            final UiProperties uiProperties, final EntityFactory entityFactory, final VaadinMessageSource i18n,
            final TargetFilterQueryManagement targetFilterQueryManagement,
            final RolloutGroupManagement rolloutGroupManagement, final QuotaManagement quotaManagement,
            final DistributionSetStatelessDataProvider distributionSetDataProvider,
            final TargetFilterQueryDataProvider targetFilterQueryDataProvider) {
        super(i18n, permissionChecker, eventBus);

        this.rolloutUIState = rolloutUIState;

        final RolloutWindowDependencies rolloutWindowDependecies = new RolloutWindowDependencies(rolloutManagement,
                targetManagement, uiNotification, entityFactory, i18n, uiProperties, eventBus,
                targetFilterQueryManagement, rolloutGroupManagement, quotaManagement, distributionSetDataProvider,
                targetFilterQueryDataProvider);
        this.rolloutWindowBuilder = new RolloutWindowBuilder(rolloutWindowDependecies);

        this.searchHeaderSupport = new SearchHeaderSupport(i18n, UIComponentIdProvider.ROLLOUT_LIST_SEARCH_BOX_ID,
                UIComponentIdProvider.ROLLOUT_LIST_SEARCH_RESET_ICON_ID, this::getSearchTextFromUiState, this::searchBy,
                this::resetSearchText);
        // TODO: consider moving permission check to header support or parent
        // header
        if (permChecker.hasRolloutCreatePermission()) {
            this.addHeaderSupport = new AddHeaderSupport(i18n, UIComponentIdProvider.ROLLOUT_ADD_ICON_ID,
                    this::addNewItem, () -> false);
        } else {
            this.addHeaderSupport = null;
        }
        addHeaderSupports(Arrays.asList(searchHeaderSupport, addHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("message.rollouts")).buildCaptionLabel();
    }

    private String getSearchTextFromUiState() {
        return rolloutUIState.getSearchText().orElse(null);
    }

    private void searchBy(final String newSearchText) {
        rolloutUIState.setSearchText(newSearchText);
        eventBus.publish(this, RolloutEvent.FILTER_BY_TEXT);
    }

    // TODO: check if needed or can be done by searchBy
    private void resetSearchText() {
        rolloutUIState.setSearchText(null);
        eventBus.publish(this, RolloutEvent.FILTER_BY_TEXT);
    }

    private void addNewItem() {
        final Window addWindow = rolloutWindowBuilder.getWindowForAddRollout();

        UI.getCurrent().addWindow(addWindow);
        addWindow.setVisible(Boolean.TRUE);

    }
}
