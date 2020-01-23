/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag.filter;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractFilterHeader;
import org.eclipse.hawkbit.ui.management.dstag.DsTagWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Window;

/**
 * Distribution Set Tag filter buttons header.
 */
public class DistributionTagFilterHeader extends AbstractFilterHeader {
    private static final long serialVersionUID = 1L;

    private final DistributionTagLayoutUiState distributionTagLayoutUiState;

    private final transient DsTagWindowBuilder dsTagWindowBuilder;

    public DistributionTagFilterHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final DsTagWindowBuilder dsTagWindowBuilder,
            final DistributionTagLayoutUiState distributionTagLayoutUiState) {
        super(i18n, permChecker, eventBus);

        this.distributionTagLayoutUiState = distributionTagLayoutUiState;
        this.dsTagWindowBuilder = dsTagWindowBuilder;

        buildHeader();
        restoreState();
    }

    @Override
    protected String getHeaderCaptionMsgKey() {
        return "header.filter.tag";
    }

    @Override
    protected String getCrudMenuBarId() {
        return UIComponentIdProvider.DIST_TAG_MENU_BAR_ID;
    }

    @Override
    protected Window getWindowForAdd() {
        return dsTagWindowBuilder.getWindowForAddDsTag();
    }

    @Override
    protected String getAddEntityWindowCaptionMsgKey() {
        return "caption.tag";
    }

    @Override
    protected String getCloseIconId() {
        return UIComponentIdProvider.HIDE_DS_TAGS;
    }

    @Override
    protected void updateHiddenUiState() {
        distributionTagLayoutUiState.setHidden(true);
    }
}
