/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.AddHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
import org.eclipse.hawkbit.ui.filtermanagement.event.CustomFilterUIEvent;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;

/**
 * Layout for Custom Filter view
 */
public class TargetFilterHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final FilterManagementUIState filterManagementUIState;

    private final transient SearchHeaderSupport searchHeaderSupport;
    private final transient AddHeaderSupport addHeaderSupport;

    /**
     * Constructor for TargetFilterHeader
     * 
     * @param eventBus
     *            UIEventBus
     * @param filterManagementUIState
     *            FilterManagementUIState
     * @param permissionChecker
     *            SpPermissionChecker
     * @param i18n
     *            VaadinMessageSource
     */
    public TargetFilterHeader(final UIEventBus eventBus, final FilterManagementUIState filterManagementUIState,
            final SpPermissionChecker permissionChecker, final VaadinMessageSource i18n) {
        super(i18n, permissionChecker, eventBus);

        this.filterManagementUIState = filterManagementUIState;

        this.searchHeaderSupport = new SearchHeaderSupport(i18n, UIComponentIdProvider.TARGET_FILTER_SEARCH_TEXT,
                UIComponentIdProvider.TARGET_FILTER_TBL_SEARCH_RESET_ID, this::getSearchTextFromUiState, this::searchBy,
                this::resetSearchText);
        // TODO: consider moving permission check to header support or parent
        // header
        if (permChecker.hasCreateTargetPermission()) {
            this.addHeaderSupport = new AddHeaderSupport(i18n, UIComponentIdProvider.TARGET_FILTER_ADD_ICON_ID,
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
        return new LabelBuilder().name(i18n.getMessage(UIMessageIdProvider.CAPTION_FILTER_CUSTOM)).buildCaptionLabel();
    }

    private String getSearchTextFromUiState() {
        return filterManagementUIState.getCustomFilterSearchText().orElse(null);
    }

    private void searchBy(final String newSearchText) {
        filterManagementUIState.setCustomFilterSearchText(newSearchText);
        eventBus.publish(this, CustomFilterUIEvent.FILTER_BY_CUST_FILTER_TEXT);
    }

    // TODO: check if needed or can be done by searchBy
    private void resetSearchText() {
        filterManagementUIState.setCustomFilterSearchText(null);
        eventBus.publish(this, CustomFilterUIEvent.FILTER_BY_CUST_FILTER_TEXT_REMOVE);
    }

    private void addNewItem() {
        filterManagementUIState.setTfQuery(null);
        filterManagementUIState.setFilterQueryValue(null);
        filterManagementUIState.setCreateFilterBtnClicked(true);

        eventBus.publish(this, CustomFilterUIEvent.CREATE_NEW_FILTER_CLICK);
    }
}
