/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.Collections;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.details.ArtifactDetailsGrid;
import org.eclipse.hawkbit.ui.artifacts.details.ArtifactDetailsGridLayout;
import org.eclipse.hawkbit.ui.artifacts.event.ArtifactDetailsEvent;
import org.eclipse.hawkbit.ui.artifacts.smtable.SmMetaDataWindowBuilder;
import org.eclipse.hawkbit.ui.artifacts.smtable.SmWindowBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class SoftwareModuleDetailsHeader extends DetailsHeader<ProxySoftwareModule> {
    private static final long serialVersionUID = 1L;

    private final SmWindowBuilder smWindowBuilder;
    private final transient SmMetaDataWindowBuilder smMetaDataWindowBuilder;

    private final ArtifactDetailsGridLayout artifactDetailsLayout;

    private final transient ArtifactDetailsHeaderSupport artifactDetailsHeaderSupport;

    public SoftwareModuleDetailsHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final UINotification uiNotification, final SmWindowBuilder smWindowBuilder,
            final SmMetaDataWindowBuilder smMetaDataWindowBuilder) {
        this(i18n, permChecker, eventBus, uiNotification, smWindowBuilder, smMetaDataWindowBuilder, null);
    }

    public SoftwareModuleDetailsHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final UINotification uiNotification, final SmWindowBuilder smWindowBuilder,
            final SmMetaDataWindowBuilder smMetaDataWindowBuilder,
            final ArtifactDetailsGridLayout artifactDetailsLayout) {
        super(i18n, permChecker, eventBus, uiNotification);

        this.smWindowBuilder = smWindowBuilder;
        this.smMetaDataWindowBuilder = smMetaDataWindowBuilder;
        this.artifactDetailsLayout = artifactDetailsLayout;

        if (artifactDetailsLayout != null) {
            this.artifactDetailsHeaderSupport = new ArtifactDetailsHeaderSupport(i18n,
                    UIComponentIdProvider.SW_MODULE_ARTIFACT_DETAILS_BUTTON, this::showArtifactDetailsWindow);
            addHeaderSupports(Collections.singletonList(artifactDetailsHeaderSupport));
        } else {
            this.artifactDetailsHeaderSupport = null;
        }

        restoreHeaderState();
        buildHeader();
    }

    @Override
    public void masterEntityChanged(final ProxySoftwareModule entity) {
        super.masterEntityChanged(entity);

        if (artifactDetailsHeaderSupport != null) {
            if (entity == null) {
                artifactDetailsHeaderSupport.disableMetaDataIcon();
            } else {
                artifactDetailsHeaderSupport.enableMetaDataIcon();
            }
        }
    }

    @Override
    protected String getEntityName(final ProxySoftwareModule entity) {
        return entity.getNameAndVersion();
    }

    @Override
    protected boolean hasEditPermission() {
        return permChecker.hasUpdateRepositoryPermission();
    }

    @Override
    protected String getEntityType() {
        return i18n.getMessage("upload.swModuleTable.header");
    }

    @Override
    protected String getDetailsHeaderCaptionId() {
        return UIComponentIdProvider.SOFTWARE_MODULE_DETAILS_HEADER_LABEL_ID;
    }

    @Override
    protected String getEditIconId() {
        return UIComponentIdProvider.UPLOAD_SW_MODULE_EDIT_BUTTON;
    }

    @Override
    protected void onEdit() {
        if (selectedEntity == null) {
            return;
        }

        final Window updateWindow = smWindowBuilder.getWindowForUpdateSm(selectedEntity);

        updateWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.software.module")));
        UI.getCurrent().addWindow(updateWindow);
        updateWindow.setVisible(Boolean.TRUE);
    }

    @Override
    protected String getMetaDataIconId() {
        return UIComponentIdProvider.UPLOAD_SW_MODULE_METADATA_BUTTON;
    }

    @Override
    protected void showMetaData() {
        if (selectedEntity == null) {
            return;
        }

        final Window metaDataWindow = smMetaDataWindowBuilder.getWindowForShowSmMetaData(selectedEntity.getId());

        metaDataWindow.setCaption(i18n.getMessage("caption.metadata.popup") + selectedEntity.getNameAndVersion());
        UI.getCurrent().addWindow(metaDataWindow);
        metaDataWindow.setVisible(Boolean.TRUE);
    }

    // TODO: check if it could be substituted with artifactDetailsLayout
    // injected directly into window; consider using CommonWindow here
    private void showArtifactDetailsWindow() {
        final Window artifactDtlsWindow = new Window();
        artifactDtlsWindow
                .setCaption(HawkbitCommonUtil.getArtifactoryDetailsLabelId(selectedEntity.getNameAndVersion(), i18n));
        artifactDtlsWindow.setCaptionAsHtml(true);
        artifactDtlsWindow.setClosable(true);
        artifactDtlsWindow.setResizable(true);

        artifactDtlsWindow.setWindowMode(WindowMode.NORMAL);
        artifactDtlsWindow.setModal(true);
        artifactDtlsWindow.addStyleName(SPUIStyleDefinitions.CONFIRMATION_WINDOW_CAPTION);

        // TODO: check if neccessary
        // artifactDetailsLayout.setFullWindowMode(false);

        artifactDetailsLayout.populateArtifactDetails(selectedEntity);
        final ArtifactDetailsGrid artifactDetailsGrid = artifactDetailsLayout.getArtifactDetailsGrid();
        artifactDetailsGrid.setWidth(700, Unit.PIXELS);
        artifactDetailsGrid.setHeight(500, Unit.PIXELS);

        artifactDtlsWindow.setContent(artifactDetailsGrid);

        artifactDtlsWindow.addWindowModeChangeListener(event -> {
            if (event.getWindowMode() == WindowMode.MAXIMIZED) {
                artifactDtlsWindow.setSizeFull();

                // TODO: check if neccessary
                // artifactDetailsLayout.setFullWindowMode(true);

                // TODO: check if it works
                eventBus.publish(this, new ArtifactDetailsEvent(BaseEntityEventType.MAXIMIZED));

                artifactDetailsGrid.setWidth(100, Unit.PERCENTAGE);
                artifactDetailsGrid.setHeight(100, Unit.PERCENTAGE);
                artifactDtlsWindow.setContent(artifactDetailsGrid);
            } else {
                artifactDtlsWindow.setSizeUndefined();
                artifactDtlsWindow.setContent(artifactDetailsGrid);
            }
        });
        UI.getCurrent().addWindow(artifactDtlsWindow);
    }
}
