/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.detailslayout.DetailsHeader;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class TargetDetailsHeader extends DetailsHeader<ProxyTarget> {
    private static final long serialVersionUID = 1L;

    private final transient TargetWindowBuilder targetWindowBuilder;
    private final transient TargetMetaDataWindowBuilder targetMetaDataWindowBuilder;

    public TargetDetailsHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final UINotification uiNotification,
            final TargetWindowBuilder targetWindowBuilder,
            final TargetMetaDataWindowBuilder targetMetaDataWindowBuilder) {
        super(i18n, permChecker, eventBus, uiNotification);

        this.targetWindowBuilder = targetWindowBuilder;
        this.targetMetaDataWindowBuilder = targetMetaDataWindowBuilder;

        restoreHeaderState();
        buildHeader();
    }

    @Override
    protected boolean hasEditPermission() {
        return permChecker.hasUpdateTargetPermission();
    }

    @Override
    protected String getEntityType() {
        return i18n.getMessage("target.details.header");
    }

    @Override
    protected String getDetailsHeaderCaptionId() {
        return UIComponentIdProvider.TARGET_DETAILS_HEADER_LABEL_ID;
    }

    @Override
    protected String getEditIconId() {
        return UIComponentIdProvider.TARGET_EDIT_ICON;
    }

    @Override
    protected void onEdit() {
        if (selectedEntity == null) {
            return;
        }

        final Window updateWindow = targetWindowBuilder.getWindowForUpdateTarget(selectedEntity);

        updateWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.target")));
        UI.getCurrent().addWindow(updateWindow);
        updateWindow.setVisible(Boolean.TRUE);
    }

    @Override
    protected String getMetaDataIconId() {
        return UIComponentIdProvider.TARGET_METADATA_BUTTON;
    }

    @Override
    protected void showMetaData() {
        if (selectedEntity == null) {
            return;
        }

        final Window metaDataWindow = targetMetaDataWindowBuilder
                .getWindowForShowTargetMetaData(selectedEntity.getControllerId());

        metaDataWindow.setCaption(i18n.getMessage("caption.metadata.popup") + selectedEntity.getName());
        UI.getCurrent().addWindow(metaDataWindow);
        metaDataWindow.setVisible(Boolean.TRUE);
    }
}
