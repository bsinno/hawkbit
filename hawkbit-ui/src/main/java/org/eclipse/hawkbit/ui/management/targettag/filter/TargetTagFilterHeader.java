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

    private final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState;

    private final transient TargetTagWindowBuilder targetTagWindowBuilder;

    public TargetTagFilterHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState,
            final TargetTagWindowBuilder targetTagWindowBuilder) {
        super(i18n, permChecker, eventBus);

        this.targetTagFilterLayoutUiState = targetTagFilterLayoutUiState;
        this.targetTagWindowBuilder = targetTagWindowBuilder;

        buildHeader();
        restoreHeaderState();
    }

    @Override
    protected String getHeaderCaptionMsgKey() {
        // TODO: constant
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
        // TODO: use constant
        return "caption.tag";
    }

    @Override
    protected String getCloseIconId() {
        return UIComponentIdProvider.HIDE_TARGET_TAGS;
    }

    @Override
    protected void updateHiddenUiState() {
        targetTagFilterLayoutUiState.setHidden(true);
    }
}
