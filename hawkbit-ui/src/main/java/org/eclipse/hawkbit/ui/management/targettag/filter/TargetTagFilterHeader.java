/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag.filter;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractFilterHeader;
import org.eclipse.hawkbit.ui.management.ManagementUIState;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.targettag.TargetTagWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Window;

/**
 * Target Tag filter by Tag Header.
 */
public class TargetTagFilterHeader extends AbstractFilterHeader {
    private static final long serialVersionUID = 1L;

    private final ManagementUIState managementUIState;

    private final transient TargetTagWindowBuilder targetTagWindowBuilder;

    public TargetTagFilterHeader(final VaadinMessageSource i18n, final ManagementUIState managementUIState,
            final SpPermissionChecker permChecker, final UIEventBus eventBus,
            final TargetTagWindowBuilder targetTagWindowBuilder) {
        super(i18n, permChecker, eventBus);

        this.managementUIState = managementUIState;
        this.targetTagWindowBuilder = targetTagWindowBuilder;

        buildHeader();
        restoreHeaderState();
    }

    @Override
    protected String getHeaderCaptionMsgKey() {
        return "header.target.filter.tag";
    }

    @Override
    protected String getCrudMenuBarId() {
        return UIComponentIdProvider.TARGET_MENU_BAR_ID;
    }

    @Override
    protected Window getWindowForAdd() {
        return targetTagWindowBuilder.getWindowForAddTargetTag();
    }

    @Override
    protected String getAddEntityWindowCaptionMsgKey() {
        return "caption.tag";
    }

    @Override
    protected String getCloseIconId() {
        return UIComponentIdProvider.HIDE_TARGET_TAGS;
    }

    @Override
    protected void updateHiddenUiState() {
        managementUIState.setTargetTagFilterClosed(true);
    }

    // TODO: change to relevant payload
    public void filterTabChanged(final ManagementUIEvent event) {
        if (event == ManagementUIEvent.RESET_TARGET_FILTER_QUERY) {
            enableCrudMenu();
        } else if (event == ManagementUIEvent.RESET_SIMPLE_FILTERS) {
            disableCrudMenu();
        }
    }
}
