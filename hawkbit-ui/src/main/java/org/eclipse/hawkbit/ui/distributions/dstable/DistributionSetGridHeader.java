/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.AddHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.FilterButtonsHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.distributions.event.DistributionsUIEvent;
import org.eclipse.hawkbit.ui.distributions.state.ManageDistUIState;
import org.eclipse.hawkbit.ui.management.dstable.DistributionAddUpdateWindowLayout;
import org.eclipse.hawkbit.ui.management.event.DistributionTableEvent;
import org.eclipse.hawkbit.ui.management.event.RefreshDistributionTableByFilterEvent;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Distribution table header.
 */
// TODO: remove duplication with DistributionGridHeader
public class DistributionSetGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final ManageDistUIState manageDistUIstate;

    private final DistributionAddUpdateWindowLayout addUpdateWindowLayout;

    private final transient SearchHeaderSupport searchHeaderSupport;
    private final transient FilterButtonsHeaderSupport filterButtonsHeaderSupport;
    private final transient AddHeaderSupport addHeaderSupport;
    private final transient ResizeHeaderSupport resizeHeaderSupport;

    DistributionSetGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final ManageDistUIState manageDistUIstate,
            final DistributionAddUpdateWindowLayout addUpdateWindowLayout) {
        super(i18n, permChecker, eventBus);

        this.manageDistUIstate = manageDistUIstate;

        this.addUpdateWindowLayout = addUpdateWindowLayout;

        this.searchHeaderSupport = new SearchHeaderSupport(i18n, UIComponentIdProvider.DIST_SEARCH_TEXTFIELD,
                UIComponentIdProvider.DIST_SEARCH_ICON, this::getSearchTextFromUiState, this::searchBy,
                this::resetSearchText);
        this.filterButtonsHeaderSupport = new FilterButtonsHeaderSupport(i18n, UIComponentIdProvider.SHOW_DIST_TAG_ICON,
                this::showFilterButtonsLayout, this::onLoadIsShowFilterButtonDisplayed);
        // TODO: consider moving permission check to header support or parent
        // header
        if (permChecker.hasCreateRepositoryPermission()) {
            this.addHeaderSupport = new AddHeaderSupport(i18n, UIComponentIdProvider.DIST_ADD_ICON, this::addNewItem,
                    this::onLoadIsTableMaximized);
        } else {
            this.addHeaderSupport = null;
        }
        this.resizeHeaderSupport = new ResizeHeaderSupport(i18n, UIComponentIdProvider.DS_MAX_MIN_TABLE_ICON,
                this::maximizeTable, this::minimizeTable, this::onLoadIsTableMaximized);
        addHeaderSupports(
                Arrays.asList(searchHeaderSupport, filterButtonsHeaderSupport, addHeaderSupport, resizeHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("header.dist.table")).buildCaptionLabel();
    }

    private String getSearchTextFromUiState() {
        return manageDistUIstate.getManageDistFilters().getSearchText().orElse(null);
    }

    private void searchBy(final String newSearchText) {
        manageDistUIstate.getManageDistFilters().setSearchText(newSearchText);
        eventBus.publish(this, new RefreshDistributionTableByFilterEvent());
    }

    // TODO: check if needed or can be done by searchBy
    private void resetSearchText() {
        if (manageDistUIstate.getManageDistFilters().getSearchText().isPresent()) {
            manageDistUIstate.getManageDistFilters().setSearchText(null);
            eventBus.publish(this, new RefreshDistributionTableByFilterEvent());
        }
    }

    private void showFilterButtonsLayout() {
        manageDistUIstate.setDistTypeFilterClosed(false);
        eventBus.publish(this, DistributionsUIEvent.SHOW_DIST_FILTER_BY_TYPE);
    }

    private Boolean onLoadIsShowFilterButtonDisplayed() {
        return manageDistUIstate.isDistTypeFilterClosed();
    }

    private void addNewItem() {
        final Window newDistWindow = addUpdateWindowLayout.getWindowForCreateDistributionSet();
        UI.getCurrent().addWindow(newDistWindow);
        newDistWindow.setVisible(Boolean.TRUE);
    }

    private Boolean onLoadIsTableMaximized() {
        return manageDistUIstate.isDsTableMaximized();
    }

    private void maximizeTable() {
        if (addHeaderSupport != null) {
            addHeaderSupport.hideAddIcon();
        }

        manageDistUIstate.setDsTableMaximized(Boolean.TRUE);
        eventBus.publish(this, new DistributionTableEvent(BaseEntityEventType.MAXIMIZED));
    }

    private void minimizeTable() {
        if (addHeaderSupport != null) {
            addHeaderSupport.showAddIcon();
        }

        manageDistUIstate.setDsTableMaximized(Boolean.FALSE);
        eventBus.publish(this, new DistributionTableEvent(BaseEntityEventType.MINIMIZED));
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final DistributionsUIEvent event) {
        if (event == DistributionsUIEvent.HIDE_DIST_FILTER_BY_TYPE) {
            filterButtonsHeaderSupport.showFilterButtonsIcon();
        }
    }
}
