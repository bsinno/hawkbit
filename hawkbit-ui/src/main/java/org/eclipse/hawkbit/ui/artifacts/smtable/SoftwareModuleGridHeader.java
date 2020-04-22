/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtype.filter.SMTypeFilterLayoutUiState;
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
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Header of Software module table.
 */
public class SoftwareModuleGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final SMTypeFilterLayoutUiState smTypeFilterLayoutUiState;
    private final SoftwareModuleGridLayoutUiState smGridLayoutUiState;

    private final transient SmWindowBuilder smWindowBuilder;

    private final transient SearchHeaderSupport searchHeaderSupport;
    private final transient FilterButtonsHeaderSupport filterButtonsHeaderSupport;
    private final transient AddHeaderSupport addHeaderSupport;
    private final transient ResizeHeaderSupport resizeHeaderSupport;

    SoftwareModuleGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final SMTypeFilterLayoutUiState smTypeFilterLayoutUiState,
            final SoftwareModuleGridLayoutUiState smGridLayoutUiState, final SmWindowBuilder smWindowBuilder) {
        super(i18n, permChecker, eventBus);

        this.smTypeFilterLayoutUiState = smTypeFilterLayoutUiState;
        this.smGridLayoutUiState = smGridLayoutUiState;

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
        return smGridLayoutUiState.getSearchFilter();
    }

    private void searchBy(final String newSearchText) {
        eventBus.publish(EventTopics.SEARCH_FILTER_CHANGED, this,
                new SearchFilterEventPayload(newSearchText, EventLayout.SM_LIST, EventView.UPLOAD));

        smGridLayoutUiState.setSearchFilter(newSearchText);
    }

    private void showFilterButtonsLayout() {
        eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this,
                new LayoutVisibilityEventPayload(VisibilityType.SHOW, EventLayout.SM_TYPE_FILTER, EventView.UPLOAD));

        smTypeFilterLayoutUiState.setHidden(false);
    }

    private Boolean onLoadIsShowFilterButtonDisplayed() {
        return smTypeFilterLayoutUiState.isHidden();
    }

    private void addNewItem() {
        final Window addWindow = smWindowBuilder.getWindowForAddSm();

        addWindow.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.software.module")));
        UI.getCurrent().addWindow(addWindow);
        addWindow.setVisible(Boolean.TRUE);
    }

    private Boolean onLoadIsTableMaximized() {
        return smGridLayoutUiState.isMaximized();
    }

    private void maximizeTable() {
        eventBus.publish(CommandTopics.RESIZE_LAYOUT, this,
                new LayoutResizeEventPayload(ResizeType.MAXIMIZE, EventLayout.SM_LIST, EventView.UPLOAD));

        if (addHeaderSupport != null) {
            addHeaderSupport.hideAddIcon();
        }

        smGridLayoutUiState.setMaximized(true);
    }

    private void minimizeTable() {
        eventBus.publish(CommandTopics.RESIZE_LAYOUT, this,
                new LayoutResizeEventPayload(ResizeType.MINIMIZE, EventLayout.SM_LIST, EventView.UPLOAD));

        if (addHeaderSupport != null) {
            addHeaderSupport.showAddIcon();
        }

        smGridLayoutUiState.setMaximized(false);
    }

    public void showSmTypeIcon() {
        filterButtonsHeaderSupport.showFilterButtonsIcon();
    }

    public void hideSmTypeIcon() {
        filterButtonsHeaderSupport.hideFilterButtonsIcon();
    }
}
