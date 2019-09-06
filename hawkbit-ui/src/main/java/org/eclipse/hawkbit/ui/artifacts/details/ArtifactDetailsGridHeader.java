/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.details;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.artifacts.event.ArtifactDetailsEvent;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Header for ArtifactDetails with maximize-support.
 */
// TODO: remove duplication with ActionHistoryGridHeader
public class ArtifactDetailsGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final ArtifactUploadState artifactUploadState;

    private final Label headerCaptionTitle;
    private final Label headerCaptionDetails;

    private final transient ResizeHeaderSupport resizeHeaderSupport;

    public ArtifactDetailsGridHeader(final VaadinMessageSource i18n, final ArtifactUploadState artifactUploadState) {
        super(i18n, null, null);

        this.artifactUploadState = artifactUploadState;

        this.headerCaptionTitle = buildHeaderCaptionTitle();
        this.headerCaptionDetails = buildHeaderCaptionDetails();

        this.resizeHeaderSupport = new ResizeHeaderSupport(i18n, SPUIDefinitions.EXPAND_ARTIFACT_DETAILS,
                this::maximizeTable, this::minimizeTable, this::onLoadIsTableMaximized);
        addHeaderSupports(Arrays.asList(resizeHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    private Label buildHeaderCaptionTitle() {
        return new LabelBuilder().name(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_DETAILS))
                .buildCaptionLabel();
    }

    private Label buildHeaderCaptionDetails() {
        return new Label("", ContentMode.HTML);
    }

    @Override
    protected boolean doSubscribeToEventBus() {
        return false;
    }

    @Override
    protected Component getHeaderCaption() {
        final HorizontalLayout headerCaptionLayout = new HorizontalLayout();
        headerCaptionLayout.setMargin(false);
        headerCaptionLayout.setSpacing(false);

        headerCaptionLayout.addComponent(headerCaptionTitle);
        headerCaptionLayout.addComponent(headerCaptionDetails);

        return headerCaptionLayout;
    }

    private void maximizeTable() {
        // TODO: check if it is needed
        // details.populateMasterDataAndRecreateContainer(masterForDetails);
        artifactUploadState.setArtifactDetailsMaximized(Boolean.TRUE);
        eventBus.publish(this, new ArtifactDetailsEvent(BaseEntityEventType.MAXIMIZED));
    }

    private void minimizeTable() {
        artifactUploadState.setArtifactDetailsMaximized(Boolean.FALSE);
        eventBus.publish(this, new ArtifactDetailsEvent(BaseEntityEventType.MINIMIZED));
    }

    private Boolean onLoadIsTableMaximized() {
        return artifactUploadState.isArtifactDetailsMaximized();
    }

    /**
     * Updates header with swModuleNameVersion name.
     *
     * @param swModuleNameVersion
     *            name and version of the software module
     */
    public void updateArtifactDetailsHeader(final String swModuleNameVersion) {
        if (StringUtils.hasText(swModuleNameVersion)) {
            headerCaptionTitle.setValue(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_DETAILS_OF));
            headerCaptionDetails.setValue(HawkbitCommonUtil.getBoldHTMLText(swModuleNameVersion));
        } else {
            headerCaptionTitle.setValue(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_DETAILS));
            headerCaptionDetails.setValue("");
        }
    }
}
