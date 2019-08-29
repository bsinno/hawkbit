/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.upload;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.artifacts.upload.FileUploadProgress.FileUploadStatus;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyUploadProgress;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyUploadProgress.ProgressSatus;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.window.WindowMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Window that shows the progress of all uploads.
 */
public class UploadProgressInfoWindow extends Window {

    private static final long serialVersionUID = 1L;

    private final ArtifactUploadState artifactUploadState;

    private final VaadinMessageSource i18n;

    private final UploadProgressGrid uploadProgressGrid;

    private final List<ProxyUploadProgress> uploads;

    private final VerticalLayout mainLayout;

    private Label windowCaption;

    private Button closeButton;

    UploadProgressInfoWindow(final UIEventBus eventBus, final ArtifactUploadState artifactUploadState,
            final VaadinMessageSource i18n) {
        this.artifactUploadState = artifactUploadState;
        this.i18n = i18n;

        setPopupProperties();
        createStatusPopupHeaderComponents();

        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(Boolean.TRUE);
        mainLayout.setSizeUndefined();
        setPopupSizeInMinMode();

        uploads = new ArrayList<>();
        uploadProgressGrid = new UploadProgressGrid(i18n);
        uploadProgressGrid.setItems(uploads);

        mainLayout.addComponents(getCaptionLayout(), uploadProgressGrid);
        mainLayout.setExpandRatio(uploadProgressGrid, 1.0F);
        setContent(mainLayout);
        eventBus.subscribe(this);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final FileUploadProgress fileUploadProgress) {
        switch (fileUploadProgress.getFileUploadStatus()) {
        case UPLOAD_STARTED:
            UI.getCurrent().access(() -> onUploadStarted(fileUploadProgress));
            break;
        case UPLOAD_IN_PROGRESS:
        case UPLOAD_FAILED:
        case UPLOAD_SUCCESSFUL:
            UI.getCurrent().access(() -> updateUploadProgressInfoRowObject(fileUploadProgress));
            break;
        case UPLOAD_FINISHED:
            UI.getCurrent().access(this::onUploadFinished);
            break;
        default:
            break;
        }
    }

    private void updateUploadProgressInfoRowObject(final FileUploadProgress fileUploadProgress) {
        final FileUploadId fileUploadId = fileUploadProgress.getFileUploadId();
        final ProxyUploadProgress gridUploadItem = uploads.stream()
                .filter(upload -> upload.getFileUploadId().equals(fileUploadId)).findAny()
                .orElse(new ProxyUploadProgress());

        gridUploadItem.setStatus(getStatusRepresentaion(fileUploadProgress.getFileUploadStatus()));
        gridUploadItem.setReason(getFailureReason(fileUploadId));

        final long bytesRead = fileUploadProgress.getBytesRead();
        final long fileSize = fileUploadProgress.getContentLength();
        if (bytesRead > 0 && fileSize > 0) {
            gridUploadItem.setProgress((double) bytesRead / (double) fileSize);
        }

        if (gridUploadItem.getFileUploadId() == null) {
            gridUploadItem.setFileUploadId(fileUploadId);
            uploads.add(gridUploadItem);
            // TODO: do we need to call refreshAll on the Grid here?
        }
        // TODO: do we need to call refreshItem on the Grid here?
    }

    private ProgressSatus getStatusRepresentaion(final FileUploadStatus uploadStatus) {
        if (uploadStatus == FileUploadStatus.UPLOAD_FAILED) {
            return ProgressSatus.FAILED;
        } else if (uploadStatus == FileUploadStatus.UPLOAD_SUCCESSFUL) {
            return ProgressSatus.FINISHED;
        } else {
            return ProgressSatus.INPROGRESS;
        }
    }

    /**
     * Returns the failure reason for the provided fileUploadId or an empty
     * string but never <code>null</code>.
     * 
     * @param fileUploadId
     * @return the failure reason or an empty String.
     */
    private String getFailureReason(final FileUploadId fileUploadId) {
        String failureReason = "";
        if (artifactUploadState.getFileUploadProgress(fileUploadId) != null) {
            failureReason = artifactUploadState.getFileUploadProgress(fileUploadId).getFailureReason();
        }
        if (StringUtils.isEmpty(failureReason)) {
            return "";
        }
        return failureReason;
    }

    private void onUploadStarted(final FileUploadProgress fileUploadProgress) {
        updateUploadProgressInfoRowObject(fileUploadProgress);

        if (isWindowNotAlreadyAttached()) {
            maximizeWindow();
        }

        // TODO: do we need this, if so, how to scroll?
        // grid.scrollTo(fileUploadProgress.getFileUploadId());
    }

    private boolean isWindowNotAlreadyAttached() {
        return !UI.getCurrent().getWindows().contains(this);
    }

    private void restoreState() {
        uploads.clear();
        for (final FileUploadProgress fileUploadProgress : artifactUploadState
                .getAllFileUploadProgressValuesFromOverallUploadProcessList()) {
            updateUploadProgressInfoRowObject(fileUploadProgress);
        }
    }

    private void setPopupProperties() {
        setId(UIComponentIdProvider.UPLOAD_STATUS_POPUP_ID);
        addStyleName(SPUIStyleDefinitions.UPLOAD_INFO);

        setResizable(false);
        setDraggable(true);
        setClosable(false);
        setModal(true);
    }

    private HorizontalLayout getCaptionLayout() {
        final HorizontalLayout captionLayout = new HorizontalLayout();
        captionLayout.setSizeFull();
        captionLayout.setHeight("36px");
        captionLayout.addComponents(windowCaption, closeButton);
        captionLayout.setExpandRatio(windowCaption, 1.0F);
        captionLayout.addStyleName("v-window-header");
        return captionLayout;
    }

    private void createStatusPopupHeaderComponents() {
        windowCaption = new Label(i18n.getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_UPLOAD_POPUP));
        closeButton = getCloseButton();
    }

    private void openWindow() {
        UI.getCurrent().addWindow(this);
        center();
    }

    protected void maximizeWindow() {
        openWindow();
        restoreState();
        artifactUploadState.setStatusPopupMinimized(false);
    }

    private void minimizeWindow() {
        artifactUploadState.setStatusPopupMinimized(true);
        closeWindow();

        if (artifactUploadState.areAllUploadsFinished()) {
            cleanupStates();
        }
    }

    /**
     * Called for every finished (succeeded or failed) upload.
     */
    private void onUploadFinished() {
        if (artifactUploadState.areAllUploadsFinished() && artifactUploadState.isStatusPopupMinimized()) {
            if (artifactUploadState.getFilesInFailedState().isEmpty()) {
                cleanupStates();
                closeWindow();
            } else {
                maximizeWindow();
            }
        }
    }

    private void cleanupStates() {
        uploads.clear();
        artifactUploadState.clearUploadTempData();
    }

    private void setPopupSizeInMinMode() {
        mainLayout.setWidth(900, Unit.PIXELS);
        mainLayout.setHeight(510, Unit.PIXELS);
    }

    private Button getCloseButton() {
        final Button closeBtn = SPUIComponentProvider.getButton(
                UIComponentIdProvider.UPLOAD_STATUS_POPUP_CLOSE_BUTTON_ID, "", "", "", true, FontAwesome.TIMES,
                SPUIButtonStyleNoBorder.class);
        closeBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        closeBtn.addClickListener(event -> onClose());
        return closeBtn;
    }

    private void onClose() {
        if (artifactUploadState.areAllUploadsFinished()) {
            cleanupStates();
            closeWindow();
        } else {
            minimizeWindow();
        }
    }

    private void closeWindow() {
        setWindowMode(WindowMode.NORMAL);
        // TODO: do we need this?
        // setColumnWidth();
        setPopupSizeInMinMode();
        this.close();
    }
}
