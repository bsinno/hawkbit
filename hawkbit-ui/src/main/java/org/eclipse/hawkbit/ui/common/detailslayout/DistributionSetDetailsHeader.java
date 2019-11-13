/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.distributions.dstable.DsMetaDataWindowBuilder;
import org.eclipse.hawkbit.ui.distributions.dstable.DsWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class DistributionSetDetailsHeader extends DetailsHeader<ProxyDistributionSet> {
    private static final long serialVersionUID = 1L;

    private final transient DsWindowBuilder dsWindowBuilder;
    private final transient DsMetaDataWindowBuilder dsMetaDataWindowBuilder;

    public DistributionSetDetailsHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final UINotification uiNotification, final DsWindowBuilder dsWindowBuilder,
            final DsMetaDataWindowBuilder dsMetaDataWindowBuilder) {
        super(i18n, permChecker, eventBus, uiNotification);

        this.dsWindowBuilder = dsWindowBuilder;
        this.dsMetaDataWindowBuilder = dsMetaDataWindowBuilder;

        restoreHeaderState();
        buildHeader();
    }

    @Override
    protected String getEntityName(final ProxyDistributionSet entity) {
        return entity.getNameVersion();
    }

    @Override
    protected boolean hasEditPermission() {
        return permChecker.hasUpdateRepositoryPermission();
    }

    @Override
    protected String getEntityType() {
        return i18n.getMessage("distribution.details.header");
    }

    @Override
    protected String getDetailsHeaderCaptionId() {
        return UIComponentIdProvider.DISTRIBUTION_DETAILS_HEADER_LABEL_ID;
    }

    @Override
    protected String getEditIconId() {
        return UIComponentIdProvider.DS_EDIT_BUTTON;
    }

    @Override
    protected void onEdit() {
        if (selectedEntity == null) {
            return;
        }

        final Window updateWindow = dsWindowBuilder.getWindowForUpdateDs(selectedEntity);

        updateWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.distribution")));
        UI.getCurrent().addWindow(updateWindow);
        updateWindow.setVisible(Boolean.TRUE);
    }

    @Override
    protected String getMetaDataIconId() {
        return UIComponentIdProvider.DS_METADATA_BUTTON;
    }

    @Override
    protected void showMetaData() {
        if (selectedEntity == null) {
            return;
        }

        final Window metaDataWindow = dsMetaDataWindowBuilder.getWindowForShowDsMetaData(selectedEntity.getId());

        metaDataWindow.setCaption(i18n.getMessage("caption.metadata.popup") + selectedEntity.getNameVersion());
        UI.getCurrent().addWindow(metaDataWindow);
        metaDataWindow.setVisible(Boolean.TRUE);
    }

}