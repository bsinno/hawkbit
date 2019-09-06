/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstable;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.FilterButtonsHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.management.event.DistributionTableEvent;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.event.RefreshDistributionTableByFilterEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.Component;

/**
 * Distribution table header.
 *
 */
public class DistributionGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final ManagementUIState managementUIState;

    private final transient SearchHeaderSupport searchHeaderSupport;
    private final transient FilterButtonsHeaderSupport filterButtonsHeaderSupport;
    private final transient ResizeHeaderSupport resizeHeaderSupport;

    DistributionGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final ManagementUIState managementUIState) {
        super(i18n, permChecker, eventBus);

        this.managementUIState = managementUIState;

        this.searchHeaderSupport = new SearchHeaderSupport(i18n, UIComponentIdProvider.DIST_SEARCH_TEXTFIELD,
                UIComponentIdProvider.DIST_SEARCH_ICON, this::getSearchTextFromUiState, this::searchBy,
                this::resetSearchText);
        this.filterButtonsHeaderSupport = new FilterButtonsHeaderSupport(i18n, UIComponentIdProvider.SHOW_DIST_TAG_ICON,
                this::showFilterButtonsLayout, this::onLoadIsShowFilterButtonDisplayed);
        this.resizeHeaderSupport = new ResizeHeaderSupport(i18n, UIComponentIdProvider.DS_MAX_MIN_TABLE_ICON,
                this::maximizeTable, this::minimizeTable, this::onLoadIsTableMaximized);
        addHeaderSupports(Arrays.asList(searchHeaderSupport, filterButtonsHeaderSupport, resizeHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("header.dist.table")).buildCaptionLabel();
    }

    private String getSearchTextFromUiState() {
        return managementUIState.getDistributionTableFilters().getSearchText().orElse(null);
    }

    private void searchBy(final String newSearchText) {
        managementUIState.getDistributionTableFilters().setSearchText(newSearchText);
        eventBus.publish(this, new RefreshDistributionTableByFilterEvent());
    }

    // TODO: check if needed or can be done by searchBy
    private void resetSearchText() {
        if (managementUIState.getDistributionTableFilters().getSearchText().isPresent()) {
            managementUIState.getDistributionTableFilters().setSearchText(null);
            eventBus.publish(this, new RefreshDistributionTableByFilterEvent());
        }
    }

    private void showFilterButtonsLayout() {
        managementUIState.setDistTagFilterClosed(false);
        eventBus.publish(this, ManagementUIEvent.SHOW_DISTRIBUTION_TAG_LAYOUT);
    }

    private Boolean onLoadIsShowFilterButtonDisplayed() {
        return managementUIState.isDistTagFilterClosed();
    }

    private void maximizeTable() {
        managementUIState.setDsTableMaximized(Boolean.TRUE);
        eventBus.publish(this, new DistributionTableEvent(BaseEntityEventType.MAXIMIZED));
    }

    private void minimizeTable() {
        managementUIState.setDsTableMaximized(Boolean.FALSE);
        eventBus.publish(this, new DistributionTableEvent(BaseEntityEventType.MINIMIZED));
    }

    private Boolean onLoadIsTableMaximized() {
        return managementUIState.isDsTableMaximized();
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent event) {
        if (event == ManagementUIEvent.HIDE_DISTRIBUTION_TAG_LAYOUT) {
            filterButtonsHeaderSupport.showFilterButtonsIcon();
        } else if (event == ManagementUIEvent.SHOW_DISTRIBUTION_TAG_LAYOUT) {
            filterButtonsHeaderSupport.hideFilterButtonsIcon();
        }
    }
}
