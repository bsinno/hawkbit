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
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Header for ArtifactDetails with maximize-support.
 */
// TODO: remove duplication with ActionHistoryGridHeader
public class ArtifactDetailsGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final ArtifactUploadState artifactUploadState;

    private final Label headerCaption;

    private final transient ResizeHeaderSupport resizeHeaderSupport;

    public ArtifactDetailsGridHeader(final VaadinMessageSource i18n, final ArtifactUploadState artifactUploadState,
            final UIEventBus eventBus) {
        super(i18n, null, eventBus);

        this.artifactUploadState = artifactUploadState;

        this.headerCaption = buildHeaderCaption();

        this.resizeHeaderSupport = new ResizeHeaderSupport(i18n, SPUIDefinitions.EXPAND_ARTIFACT_DETAILS,
                this::maximizeTable, this::minimizeTable, this::onLoadIsTableMaximized);
        addHeaderSupports(Arrays.asList(resizeHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    private Label buildHeaderCaption() {
        final Label caption = new Label(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_DETAILS),
                ContentMode.HTML);

        caption.addStyleName(ValoTheme.LABEL_SMALL);
        caption.addStyleName(ValoTheme.LABEL_BOLD);
        caption.addStyleName("header-caption");

        return caption;
    }

    @Override
    protected boolean doSubscribeToEventBus() {
        return false;
    }

    @Override
    protected Component getHeaderCaption() {
        return headerCaption;
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
            headerCaption.setValue(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_DETAILS_OF) + " "
                    + HawkbitCommonUtil.getBoldHTMLText(swModuleNameVersion));
        } else {
            headerCaption.setValue(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_DETAILS));
        }
    }
}