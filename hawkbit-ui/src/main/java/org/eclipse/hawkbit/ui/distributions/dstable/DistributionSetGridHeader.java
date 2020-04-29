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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.FilterChangedEventPayload;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload.ResizeType;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.AddHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.FilterButtonsHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
import org.eclipse.hawkbit.ui.common.state.TypeFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Distribution table header.
 */
// TODO: remove duplication with DistributionGridHeader
public class DistributionSetGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final TypeFilterLayoutUiState dSTypeFilterLayoutUiState;
    private final DistributionSetGridLayoutUiState distributionSetGridLayoutUiState;

    private final transient DsWindowBuilder dsWindowBuilder;

    private final transient SearchHeaderSupport searchHeaderSupport;
    private final transient FilterButtonsHeaderSupport filterButtonsHeaderSupport;
    private final transient AddHeaderSupport addHeaderSupport;
    private final transient ResizeHeaderSupport resizeHeaderSupport;

    DistributionSetGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final DsWindowBuilder dsWindowBuilder,
            final TypeFilterLayoutUiState dSTypeFilterLayoutUiState,
            final DistributionSetGridLayoutUiState distributionSetGridLayoutUiState) {
        super(i18n, permChecker, eventBus);

        this.dSTypeFilterLayoutUiState = dSTypeFilterLayoutUiState;
        this.distributionSetGridLayoutUiState = distributionSetGridLayoutUiState;

        this.dsWindowBuilder = dsWindowBuilder;

        this.searchHeaderSupport = new SearchHeaderSupport(i18n, UIComponentIdProvider.DIST_SEARCH_TEXTFIELD,
                UIComponentIdProvider.DIST_SEARCH_ICON, this::getSearchTextFromUiState, this::searchBy);
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

        buildHeader();
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("header.dist.table")).buildCaptionLabel();
    }

    private String getSearchTextFromUiState() {
        return distributionSetGridLayoutUiState.getSearchFilter();
    }

    private void searchBy(final String newSearchText) {
        eventBus.publish(EventTopics.FILTER_CHANGED, this, new FilterChangedEventPayload<>(ProxyDistributionSet.class,
                FilterType.SEARCH, newSearchText, EventView.DISTRIBUTIONS));

        distributionSetGridLayoutUiState.setSearchFilter(newSearchText);
    }

    private void showFilterButtonsLayout() {
        eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this, new LayoutVisibilityEventPayload(
                VisibilityType.SHOW, EventLayout.DS_TYPE_FILTER, EventView.DISTRIBUTIONS));

        dSTypeFilterLayoutUiState.setHidden(false);
    }

    private Boolean onLoadIsShowFilterButtonDisplayed() {
        return dSTypeFilterLayoutUiState.isHidden();
    }

    private void addNewItem() {
        final Window addWindow = dsWindowBuilder.getWindowForAddDs();

        addWindow.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.distribution")));
        UI.getCurrent().addWindow(addWindow);
        addWindow.setVisible(Boolean.TRUE);
    }

    private Boolean onLoadIsTableMaximized() {
        return distributionSetGridLayoutUiState.isMaximized();
    }

    private void maximizeTable() {
        eventBus.publish(CommandTopics.RESIZE_LAYOUT, this,
                new LayoutResizeEventPayload(ResizeType.MAXIMIZE, EventLayout.DS_LIST, EventView.DISTRIBUTIONS));

        if (addHeaderSupport != null) {
            addHeaderSupport.hideAddIcon();
        }

        distributionSetGridLayoutUiState.setMaximized(true);
    }

    private void minimizeTable() {
        eventBus.publish(CommandTopics.RESIZE_LAYOUT, this,
                new LayoutResizeEventPayload(ResizeType.MINIMIZE, EventLayout.DS_LIST, EventView.DISTRIBUTIONS));

        if (addHeaderSupport != null) {
            addHeaderSupport.showAddIcon();
        }

        distributionSetGridLayoutUiState.setMaximized(false);
    }

    public void showDsTypeIcon() {
        filterButtonsHeaderSupport.showFilterButtonsIcon();
    }

    public void hideDsTypeIcon() {
        filterButtonsHeaderSupport.hideFilterButtonsIcon();
    }
}
