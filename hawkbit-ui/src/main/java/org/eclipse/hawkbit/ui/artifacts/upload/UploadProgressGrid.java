/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.upload;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyUploadProgress;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyUploadProgress.ProgressSatus;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.renderers.ProgressBarRenderer;

/**
 * Grid for Upload Progress pop up info window.
 */
public class UploadProgressGrid extends Grid<ProxyUploadProgress> {
    private static final long serialVersionUID = 1L;

    private final VaadinMessageSource i18n;

    private static final String UPLOAD_PROGRESS_STATUS_ID = "uploadProgressStatus";
    private static final String UPLOAD_PROGRESS_BAR_ID = "uploadProgressBar";
    private static final String UPLOAD_PROGRESS_FILENAME_ID = "uploadProgressFileName";
    private static final String UPLOAD_PROGRESS_SM_ID = "uploadProgressSm";
    private static final String UPLOAD_PROGRESS_REASON_ID = "uploadProgressReason";

    private final Map<ProgressSatus, ProxyFontIcon> progressStatusIconMap = new EnumMap<>(ProgressSatus.class);

    public UploadProgressGrid(final VaadinMessageSource i18n) {
        this.i18n = i18n;

        initProgressStatusIconMap();
        init();
    }

    private void initProgressStatusIconMap() {
        progressStatusIconMap.put(ProgressSatus.INPROGRESS, new ProxyFontIcon(VaadinIcons.ADJUST,
                SPUIStyleDefinitions.STATUS_ICON_YELLOW, getProgressStatusDescription(ProgressSatus.INPROGRESS)));
        progressStatusIconMap.put(ProgressSatus.FINISHED, new ProxyFontIcon(VaadinIcons.CHECK_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_GREEN, getProgressStatusDescription(ProgressSatus.FINISHED)));
        progressStatusIconMap.put(ProgressSatus.FAILED, new ProxyFontIcon(VaadinIcons.EXCLAMATION_CIRCLE,
                SPUIStyleDefinitions.STATUS_ICON_RED, getProgressStatusDescription(ProgressSatus.FAILED)));
    }

    private String getProgressStatusDescription(final ProgressSatus progressStatus) {
        return i18n
                .getMessage(UIMessageIdProvider.TOOLTIP_UPLOAD_STATUS_PREFIX + progressStatus.toString().toLowerCase());
    }

    private void init() {
        setId(UIComponentIdProvider.UPLOAD_STATUS_POPUP_GRID);
        addStyleName(SPUIStyleDefinitions.UPLOAD_STATUS_GRID);
        setSelectionMode(SelectionMode.NONE);
        setSizeFull();

        addColumns();
    }

    private void addColumns() {
        addComponentColumn(this::buildProgressStatusIcon).setId(UPLOAD_PROGRESS_STATUS_ID)
                .setCaption(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_UPLOAD_STATUS)).setMinimumWidth(60d)
                .setStyleGenerator(item -> "v-align-center");

        addColumn(ProxyUploadProgress::getProgress, new ProgressBarRenderer()).setId(UPLOAD_PROGRESS_BAR_ID)
                .setCaption(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_UPLOAD_PROGRESS))
                .setMinimumWidth(150d);

        addColumn(uploadProgress -> uploadProgress.getFileUploadId().getFilename()).setId(UPLOAD_PROGRESS_FILENAME_ID)
                .setCaption(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_FILENAME)).setMinimumWidth(200d);

        addColumn(uploadProgress -> HawkbitCommonUtil.getFormattedNameVersion(
                uploadProgress.getFileUploadId().getSoftwareModuleName(),
                uploadProgress.getFileUploadId().getSoftwareModuleVersion())).setId(UPLOAD_PROGRESS_SM_ID)
                        .setCaption(i18n.getMessage(UIMessageIdProvider.CAPTION_SOFTWARE_MODULE)).setMinimumWidth(200d);

        addColumn(ProxyUploadProgress::getReason).setId(UPLOAD_PROGRESS_REASON_ID)
                .setCaption(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_UPLOAD_REASON)).setMinimumWidth(290d);

        setFrozenColumnCount(5);
    }

    private Label buildProgressStatusIcon(final ProxyUploadProgress uploadProgress) {
        final ProxyFontIcon progressStatusFontIcon = Optional
                .ofNullable(progressStatusIconMap.get(uploadProgress.getStatus()))
                .orElse(new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String progressStatusId = new StringBuilder(UIComponentIdProvider.UPLOAD_STATUS_LABEL_ID).append(".")
                .append(uploadProgress.getId()).toString();

        return SPUIComponentProvider.getLabelIcon(progressStatusFontIcon, progressStatusId);
    }
}
