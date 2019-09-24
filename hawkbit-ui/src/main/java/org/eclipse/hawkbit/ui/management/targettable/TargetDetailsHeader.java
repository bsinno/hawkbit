/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.Optional;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.Target;
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

    private final transient EntityFactory entityFactory;
    private final transient TargetManagement targetManagement;

    private final TargetAddUpdateWindowLayout targetAddUpdateWindowLayout;

    public TargetDetailsHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final UINotification uiNotification, final EntityFactory entityFactory,
            final TargetManagement targetManagement, final TargetAddUpdateWindowLayout targetAddUpdateWindowLayout) {
        super(i18n, permChecker, eventBus, uiNotification);

        this.entityFactory = entityFactory;
        this.targetManagement = targetManagement;
        this.targetAddUpdateWindowLayout = targetAddUpdateWindowLayout;

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

        final Window targetWindow = targetAddUpdateWindowLayout.getWindow(selectedEntity.getControllerId());
        if (targetWindow == null) {
            return;
        }
        targetWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.target")));
        UI.getCurrent().addWindow(targetWindow);
        targetWindow.setVisible(Boolean.TRUE);
    }

    @Override
    protected String getMetaDataIconId() {
        return UIComponentIdProvider.TARGET_METADATA_BUTTON;
    }

    @Override
    protected void showMetaData() {
        final Optional<Target> target = targetManagement.get(selectedEntity.getId());
        if (!target.isPresent()) {
            uiNotification.displayWarning(i18n.getMessage("targets.not.exists"));
            return;
        }

        final TargetMetadataPopupLayout targetMetadataPopupLayout = new TargetMetadataPopupLayout(i18n, uiNotification,
                eventBus, targetManagement, entityFactory, permChecker);
        UI.getCurrent().addWindow(targetMetadataPopupLayout.getWindow(target.get(), null));
    }
}
