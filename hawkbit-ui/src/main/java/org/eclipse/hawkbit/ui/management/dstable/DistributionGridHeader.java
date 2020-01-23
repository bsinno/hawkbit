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
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.LayoutResizedEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityChangedEventPayload;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.FilterButtonsHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
import org.eclipse.hawkbit.ui.management.dstag.filter.DistributionTagLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;

/**
 * Distribution table header.
 *
 */
public class DistributionGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final DistributionGridLayoutUiState distributionGridLayoutUiState;
    private final DistributionTagLayoutUiState distributionTagLayoutUiState;

    private final transient SearchHeaderSupport searchHeaderSupport;
    private final transient FilterButtonsHeaderSupport filterButtonsHeaderSupport;
    private final transient ResizeHeaderSupport resizeHeaderSupport;

    DistributionGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final DistributionGridLayoutUiState distributionGridLayoutUiState,
            final DistributionTagLayoutUiState distributionTagLayoutUiState) {
        super(i18n, permChecker, eventBus);

        this.distributionGridLayoutUiState = distributionGridLayoutUiState;
        this.distributionTagLayoutUiState = distributionTagLayoutUiState;

        this.searchHeaderSupport = new SearchHeaderSupport(i18n, UIComponentIdProvider.DIST_SEARCH_TEXTFIELD,
                UIComponentIdProvider.DIST_SEARCH_ICON, this::getSearchTextFromUiState, this::searchBy,
                this::resetSearchText);
        this.filterButtonsHeaderSupport = new FilterButtonsHeaderSupport(i18n, UIComponentIdProvider.SHOW_DIST_TAG_ICON,
                this::showFilterButtonsLayout, this::onLoadIsShowFilterButtonDisplayed);
        this.resizeHeaderSupport = new ResizeHeaderSupport(i18n, UIComponentIdProvider.DS_MAX_MIN_TABLE_ICON,
                this::maximizeTable, this::minimizeTable, this::onLoadIsTableMaximized);
        addHeaderSupports(Arrays.asList(searchHeaderSupport, filterButtonsHeaderSupport, resizeHeaderSupport));

        restoreState();
        buildHeader();
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("header.dist.table")).buildCaptionLabel();
    }

    private String getSearchTextFromUiState() {
        return distributionGridLayoutUiState.getSearchFilter();
    }

    private void searchBy(final String newSearchText) {
        eventBus.publish(EventTopics.SEARCH_FILTER_CHANGED, this, newSearchText);

        distributionGridLayoutUiState.setSearchFilter(newSearchText);
    }

    // TODO: check if needed or can be done by searchBy
    private void resetSearchText() {
        eventBus.publish(EventTopics.SEARCH_FILTER_CHANGED, this, "");

        distributionGridLayoutUiState.setSearchFilter(null);
    }

    private void showFilterButtonsLayout() {
        eventBus.publish(EventTopics.LAYOUT_VISIBILITY_CHANGED, this, LayoutVisibilityChangedEventPayload.LAYOUT_SHOWN);

        distributionTagLayoutUiState.setHidden(false);
    }

    private Boolean onLoadIsShowFilterButtonDisplayed() {
        return !distributionTagLayoutUiState.isHidden();
    }

    private Boolean onLoadIsTableMaximized() {
        return distributionGridLayoutUiState.isMaximized();
    }

    private void maximizeTable() {
        eventBus.publish(EventTopics.LAYOUT_RESIZED, this, LayoutResizedEventPayload.LAYOUT_MAXIMIZED);

        distributionGridLayoutUiState.setMaximized(true);
    }

    private void minimizeTable() {
        eventBus.publish(EventTopics.LAYOUT_RESIZED, this, LayoutResizedEventPayload.LAYOUT_MINIMIZED);

        distributionGridLayoutUiState.setMaximized(false);
    }

    public void showDsTagIcon() {
        filterButtonsHeaderSupport.showFilterButtonsIcon();
    }

    public void hideDsTagIcon() {
        filterButtonsHeaderSupport.hideFilterButtonsIcon();
    }
}
