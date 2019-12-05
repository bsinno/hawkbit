/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype.filter;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractFilterHeader;
import org.eclipse.hawkbit.ui.distributions.disttype.DsTypeWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Window;

/**
 * Distribution Set Type filter buttons header.
 */
public class DSTypeFilterHeader extends AbstractFilterHeader {
    private static final long serialVersionUID = 1L;

    private final DSTypeFilterLayoutUiState dSTypeFilterLayoutUiState;

    private final transient DsTypeWindowBuilder dsTypeWindowBuilder;

    /**
     * Constructor
     * 
     * @param i18n
     *            VaadinMessageSource
     * @param permChecker
     *            SpPermissionChecker
     * @param eventBus
     *            UIEventBus
     * @param manageDistUIState
     *            ManageDistUIState
     * @param entityFactory
     *            EntityFactory
     * @param uiNotification
     *            UINotification
     * @param softwareModuleTypeManagement
     *            SoftwareModuleTypeManagement
     * @param distributionSetTypeManagement
     *            DistributionSetTypeManagement
     * @param dSTypeFilterButtons
     *            DSTypeFilterButtons
     */
    DSTypeFilterHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker, final UIEventBus eventBus,
            final DSTypeFilterLayoutUiState dSTypeFilterLayoutUiState, final DsTypeWindowBuilder dsTypeWindowBuilder) {
        super(i18n, permChecker, eventBus);

        this.dSTypeFilterLayoutUiState = dSTypeFilterLayoutUiState;
        this.dsTypeWindowBuilder = dsTypeWindowBuilder;

        buildHeader();
        restoreHeaderState();
    }

    @Override
    protected String getHeaderCaptionMsgKey() {
        return UIMessageIdProvider.CAPTION_FILTER_BY_TYPE;
    }

    @Override
    protected String getCrudMenuBarId() {
        return UIComponentIdProvider.DIST_TYPE_MENU_BAR_ID;
    }

    @Override
    protected Window getWindowForAdd() {
        return dsTypeWindowBuilder.getWindowForAddDsType();
    }

    @Override
    protected String getAddEntityWindowCaptionMsgKey() {
        return "caption.type";
    }

    @Override
    protected String getCloseIconId() {
        return UIComponentIdProvider.HIDE_DS_TYPES;
    }

    @Override
    protected void updateHiddenUiState() {
        dSTypeFilterLayoutUiState.setHidden(true);
    }
}
