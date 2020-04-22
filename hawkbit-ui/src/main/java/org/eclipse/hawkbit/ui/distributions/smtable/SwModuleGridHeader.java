/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtable;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtable.SmWindowBuilder;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload.ResizeType;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.SearchFilterEventPayload;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.AddHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.FilterButtonsHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
import org.eclipse.hawkbit.ui.distributions.smtype.filter.DistSMTypeFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Implementation of software module Header block using generic abstract details
 * style .
 */
// TODO: remove duplication with SoftwareModuleGridHeader
public class SwModuleGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final DistSMTypeFilterLayoutUiState distSMTypeFilterLayoutUiState;
    private final SwModuleGridLayoutUiState swModuleGridLayoutUiState;

    private final transient SmWindowBuilder smWindowBuilder;

    private final transient SearchHeaderSupport searchHeaderSupport;
    private final transient FilterButtonsHeaderSupport filterButtonsHeaderSupport;
    private final transient AddHeaderSupport addHeaderSupport;
    private final transient ResizeHeaderSupport resizeHeaderSupport;

    SwModuleGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker, final UIEventBus eventBus,
            final SmWindowBuilder smWindowBuilder, final DistSMTypeFilterLayoutUiState distSMTypeFilterLayoutUiState,
            final SwModuleGridLayoutUiState swModuleGridLayoutUiState) {
        super(i18n, permChecker, eventBus);

        this.distSMTypeFilterLayoutUiState = distSMTypeFilterLayoutUiState;
        this.swModuleGridLayoutUiState = swModuleGridLayoutUiState;

        this.smWindowBuilder = smWindowBuilder;

        this.searchHeaderSupport = new SearchHeaderSupport(i18n, UIComponentIdProvider.SW_MODULE_SEARCH_TEXT_FIELD,
                UIComponentIdProvider.SW_MODULE_SEARCH_RESET_ICON, this::getSearchTextFromUiState, this::searchBy);
        this.filterButtonsHeaderSupport = new FilterButtonsHeaderSupport(i18n, UIComponentIdProvider.SHOW_SM_TYPE_ICON,
                this::showFilterButtonsLayout, this::onLoadIsShowFilterButtonDisplayed);
        // TODO: consider moving permission check to header support or parent
        // header
        if (permChecker.hasCreateRepositoryPermission()) {
            this.addHeaderSupport = new AddHeaderSupport(i18n, UIComponentIdProvider.SW_MODULE_ADD_BUTTON,
                    this::addNewItem, this::onLoadIsTableMaximized);
        } else {
            this.addHeaderSupport = null;
        }
        this.resizeHeaderSupport = new ResizeHeaderSupport(i18n, UIComponentIdProvider.SW_MAX_MIN_TABLE_ICON,
                this::maximizeTable, this::minimizeTable, this::onLoadIsTableMaximized);
        addHeaderSupports(
                Arrays.asList(searchHeaderSupport, filterButtonsHeaderSupport, addHeaderSupport, resizeHeaderSupport));

        buildHeader();
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("upload.swModuleTable.header")).buildCaptionLabel();
    }

    private String getSearchTextFromUiState() {
        return swModuleGridLayoutUiState.getSearchFilter();
    }

    private void searchBy(final String newSearchText) {
        eventBus.publish(EventTopics.SEARCH_FILTER_CHANGED, this,
                new SearchFilterEventPayload(newSearchText, EventLayout.SM_LIST, EventView.DISTRIBUTIONS));

        swModuleGridLayoutUiState.setSearchFilter(newSearchText);
    }

    private void showFilterButtonsLayout() {
        eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this,
                new LayoutVisibilityEventPayload(VisibilityType.SHOW, EventLayout.SM_TYPE_FILTER, EventView.DISTRIBUTIONS));

        distSMTypeFilterLayoutUiState.setHidden(false);
    }

    private boolean onLoadIsShowFilterButtonDisplayed() {
        return distSMTypeFilterLayoutUiState.isHidden();
    }

    private void addNewItem() {
        final Window addWindow = smWindowBuilder.getWindowForAddSm();

        addWindow.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.software.module")));
        UI.getCurrent().addWindow(addWindow);
        addWindow.setVisible(Boolean.TRUE);
    }

    private Boolean onLoadIsTableMaximized() {
        return swModuleGridLayoutUiState.isMaximized();
    }

    private void maximizeTable() {
        eventBus.publish(CommandTopics.RESIZE_LAYOUT, this,
                new LayoutResizeEventPayload(ResizeType.MAXIMIZE, EventLayout.SM_LIST, EventView.DISTRIBUTIONS));

        if (addHeaderSupport != null) {
            addHeaderSupport.hideAddIcon();
        }

        swModuleGridLayoutUiState.setMaximized(true);
    }

    private void minimizeTable() {
        eventBus.publish(CommandTopics.RESIZE_LAYOUT, this,
                new LayoutResizeEventPayload(ResizeType.MINIMIZE, EventLayout.SM_LIST, EventView.DISTRIBUTIONS));

        if (addHeaderSupport != null) {
            addHeaderSupport.showAddIcon();
        }

        swModuleGridLayoutUiState.setMaximized(false);
    }

    public void showSmTypeIcon() {
        filterButtonsHeaderSupport.showFilterButtonsIcon();
    }

    public void hideSmTypeIcon() {
        filterButtonsHeaderSupport.hideFilterButtonsIcon();
    }
}
