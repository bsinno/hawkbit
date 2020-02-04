/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtype.filter;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.smtype.SmTypeWindowBuilder;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractFilterHeader;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Window;

/**
 * Software module type filter buttons header.
 */
public class SMTypeFilterHeader extends AbstractFilterHeader {
    private static final long serialVersionUID = 1L;

    private final SMTypeFilterLayoutUiState smTypeFilterLayoutUiState;

    private final transient SmTypeWindowBuilder smTypeWindowBuilder;

    public SMTypeFilterHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final SMTypeFilterLayoutUiState smTypeFilterLayoutUiState,
            final SmTypeWindowBuilder smTypeWindowBuilder) {
        super(i18n, permChecker, eventBus);

        this.smTypeFilterLayoutUiState = smTypeFilterLayoutUiState;
        this.smTypeWindowBuilder = smTypeWindowBuilder;

        buildHeader();
    }

    @Override
    protected String getHeaderCaptionMsgKey() {
        return UIMessageIdProvider.CAPTION_FILTER_BY_TYPE;
    }

    @Override
    protected String getCrudMenuBarId() {
        return UIComponentIdProvider.SOFT_MODULE_TYPE_MENU_BAR_ID;
    }

    @Override
    protected Window getWindowForAdd() {
        return smTypeWindowBuilder.getWindowForAddSmType();
    }

    @Override
    protected String getAddEntityWindowCaptionMsgKey() {
        return "caption.type";
    }

    @Override
    protected String getCloseIconId() {
        return UIComponentIdProvider.HIDE_SM_TYPES;
    }

    @Override
    protected void updateHiddenUiState() {
        smTypeFilterLayoutUiState.setHidden(true);
    }

    @Override
    protected Layout getLayout() {
        return Layout.SM_TYPE_FILTER;
    }

    @Override
    protected View getView() {
        return View.UPLOAD;
    }
}
