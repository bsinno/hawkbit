/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractEntityGridHeader;
import org.eclipse.hawkbit.ui.common.state.GridLayoutUiState;
import org.eclipse.hawkbit.ui.common.state.HidableLayoutUiState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Header of Software module table.
 */
public class SoftwareModuleGridHeader extends AbstractEntityGridHeader {
    private static final long serialVersionUID = 1L;

    public SoftwareModuleGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final HidableLayoutUiState smTypeFilterLayoutUiState,
            final GridLayoutUiState smGridLayoutUiState, final SmWindowBuilder smWindowBuilder, final EventView view) {
        super(i18n, permChecker, eventBus, smTypeFilterLayoutUiState, smGridLayoutUiState, EventLayout.SM_TYPE_FILTER,
                view);

        addAddHeaderSupport(smWindowBuilder);
    }

    @Override
    protected String getCaptionMsg() {
        // TODO use constant
        return "upload.swModuleTable.header";
    }

    @Override
    protected String getSearchFieldId() {
        return UIComponentIdProvider.SW_MODULE_SEARCH_TEXT_FIELD;
    }

    @Override
    protected String getSearchResetIconId() {
        return UIComponentIdProvider.SW_MODULE_SEARCH_RESET_ICON;
    }

    @Override
    protected Class<? extends ProxyIdentifiableEntity> getEntityType() {
        return ProxySoftwareModule.class;
    }

    @Override
    protected String getFilterButtonsIconId() {
        return UIComponentIdProvider.SHOW_SM_TYPE_ICON;
    }

    @Override
    protected String getMaxMinIconId() {
        return UIComponentIdProvider.SW_MAX_MIN_TABLE_ICON;
    }

    @Override
    protected EventLayout getLayout() {
        return EventLayout.SM_LIST;
    }

    @Override
    protected boolean hasCreatePermission() {
        return permChecker.hasCreateRepositoryPermission();
    }

    @Override
    protected String getAddIconId() {
        return UIComponentIdProvider.SW_MODULE_ADD_BUTTON;
    }

    @Override
    protected String getAddWindowCaptionMsg() {
        // TODO use constant
        return "caption.software.module";
    }
}
