/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.concurrent.Executor;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.builder.WindowBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyBulkUploadWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.Binder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Bulk target upload layout.
 */
public class TargetBulkUpdateWindowLayout extends CustomComponent {
    private static final long serialVersionUID = 1L;

    private final VaadinMessageSource i18n;

    private final TargetBulkUploadUiState targetBulkUploadUiState;

    private final ComboBox<ProxyDistributionSet> dsCombo;
    private final BulkUploadTagsComponent tagsComponent;
    private final TextArea descTextArea;
    private final ProgressBar progressBar;
    private final Label targetsCountLabel;
    private final Upload uploadButton;
    private final Link linkToSystemConfigHelp;

    private final Label windowCaption;
    private final Button closeButton;
    private Window bulkUploadWindow;

    private final Binder<ProxyBulkUploadWindow> binder;

    TargetBulkUpdateWindowLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker checker, final UINotification uinotification,
            final TargetManagement targetManagement, final DeploymentManagement deploymentManagement,
            final TargetTagManagement tagManagement, final DistributionSetManagement distributionSetManagement,
            final EntityFactory entityFactory, final UiProperties uiproperties, final Executor uiExecutor,
            final TargetBulkUploadUiState targetBulkUploadUiState) {
        this.i18n = i18n;
        this.targetBulkUploadUiState = targetBulkUploadUiState;
        this.binder = new Binder<>();

        final BulkUploadWindowLayoutComponentBuilder componentBuilder = new BulkUploadWindowLayoutComponentBuilder(i18n,
                distributionSetManagement);

        this.dsCombo = componentBuilder.createDistributionSetCombo(binder);
        this.tagsComponent = componentBuilder.createTargetTagsField(binder, new TargetBulkTokenTags(i18n, eventBus,
                checker, uinotification, tagManagement, targetBulkUploadUiState));
        this.descTextArea = componentBuilder.createDescriptionField(binder);
        this.progressBar = createProgressBar();
        this.targetsCountLabel = getStatusCountLabel();

        final BulkUploadHandler bulkUploadHandler = new BulkUploadHandler(eventBus, targetManagement, tagManagement,
                distributionSetManagement, deploymentManagement, i18n, uinotification, entityFactory, UI.getCurrent(),
                uiExecutor, targetBulkUploadUiState, binder::getBean);
        this.uploadButton = createUploadButton(bulkUploadHandler);

        this.linkToSystemConfigHelp = SPUIComponentProvider.getHelpLink(i18n,
                uiproperties.getLinks().getDocumentation().getDeploymentView());

        this.windowCaption = new Label(i18n.getMessage("caption.bulk.upload.targets"));
        this.closeButton = getCloseButton();

        buildLayout();
        // TODO: adapt
        binder.setBean(new ProxyBulkUploadWindow());
    }

    private Upload createUploadButton(final BulkUploadHandler uploadHandler) {
        final Upload upload = new Upload();

        upload.setButtonCaption(i18n.getMessage("caption.bulk.upload"));
        upload.setReceiver(uploadHandler);
        upload.addSucceededListener(uploadHandler);
        upload.addFailedListener(uploadHandler);
        upload.addStartedListener(uploadHandler);

        return upload;
    }

    private static ProgressBar createProgressBar() {
        final ProgressBar progressBarIndicator = new ProgressBar(0F);
        progressBarIndicator.addStyleName("bulk-upload-label");
        progressBarIndicator.setSizeFull();

        return progressBarIndicator;
    }

    private static Label getStatusCountLabel() {
        final Label countLabel = new Label();

        countLabel.addStyleName("bulk-upload-label");
        countLabel.setVisible(false);
        countLabel.setCaptionAsHtml(true);
        countLabel.setId(UIComponentIdProvider.BULK_UPLOAD_COUNT);
        return countLabel;
    }

    private Button getCloseButton() {
        final Button closeBtn = SPUIComponentProvider.getButton(UIComponentIdProvider.BULK_UPLOAD_CLOSE_BUTTON_ID, "",
                "", "", true, VaadinIcons.CLOSE, SPUIButtonStyleNoBorder.class);
        closeBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        closeBtn.addClickListener(event -> closePopup());
        return closeBtn;
    }

    private void closePopup() {
        // TODO: adapt to either minimize or close
        // managementUIState.setBulkUploadWindowMinimised(true);
        // eventBus.publish(this, BulkUploadPopupEvent.MINIMIZED);
        // eventBus.publish(this, BulkUploadPopupEvent.CLOSED);
        clearPreviousSessionData();
        reset();
        bulkUploadWindow.close();
    }

    private void clearPreviousSessionData() {
        targetBulkUploadUiState.setDescription(null);
        targetBulkUploadUiState.setDsId(null);
        targetBulkUploadUiState.setTagIdsWithNameToAssign(null);

        targetBulkUploadUiState.setFailedUploadCount(0);
        targetBulkUploadUiState.setSucessfulUploadCount(0);
        targetBulkUploadUiState.getAssignedTagNames().clear();
        targetBulkUploadUiState.getTargetsCreated().clear();
    }

    private void buildLayout() {
        final HorizontalLayout captionLayout = new HorizontalLayout();
        captionLayout.setMargin(false);
        captionLayout.setSpacing(false);
        captionLayout.setSizeFull();
        captionLayout.addStyleName("v-window-header");

        captionLayout.addComponents(windowCaption, closeButton);
        captionLayout.setExpandRatio(windowCaption, 1.0F);

        final VerticalLayout tagsLayout = new VerticalLayout();
        tagsLayout.setId(UIComponentIdProvider.BULK_UPLOAD_TAG);
        tagsLayout.setCaption(i18n.getMessage("caption.tags.tab"));
        tagsLayout.setSpacing(false);
        tagsLayout.setMargin(false);
        tagsLayout.setSizeFull();
        tagsLayout.setHeight("100px");
        tagsLayout.addStyleName("bulk-target-tags-layout");

        tagsLayout.addComponent(tagsComponent);

        final HorizontalLayout uploaderLayout = new HorizontalLayout();
        uploaderLayout.setMargin(false);
        uploaderLayout.setSpacing(false);
        uploaderLayout.setSizeFull();

        uploaderLayout.addComponent(uploadButton);
        uploaderLayout.addComponent(linkToSystemConfigHelp);
        uploaderLayout.setComponentAlignment(linkToSystemConfigHelp, Alignment.BOTTOM_RIGHT);
        uploaderLayout.setExpandRatio(uploadButton, 1.0F);

        final FormLayout inputsLayout = new FormLayout();
        inputsLayout.setMargin(false);
        inputsLayout.setSpacing(true);
        inputsLayout.setWidth("300px");

        inputsLayout.addComponents(dsCombo, tagsLayout, descTextArea, progressBar, targetsCountLabel, uploaderLayout);

        final VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(false);
        mainLayout.setSpacing(true);

        mainLayout.addComponents(captionLayout, inputsLayout);

        setCompositionRoot(mainLayout);
    }

    public void onStartOfUpload() {
        final ProxyBulkUploadWindow bulkUploadInputs = binder.getBean();

        targetBulkUploadUiState.setDsId(bulkUploadInputs.getDistributionSetId());
        targetBulkUploadUiState.setDescription(bulkUploadInputs.getDescription());
        targetBulkUploadUiState.setTagIdsWithNameToAssign(bulkUploadInputs.getTagIdsWithNameToAssign());

        targetBulkUploadUiState.setProgressBarCurrentValue(0F);
        targetBulkUploadUiState.setFailedUploadCount(0);
        targetBulkUploadUiState.setSucessfulUploadCount(0);
    }

    public void setProgressBarValue(final Float value) {
        progressBar.setValue(value);
        progressBar.setVisible(true);
    }

    /**
     * Reset the values in popup.
     */
    public void reset() {
        binder.setBean(new ProxyBulkUploadWindow());

        progressBar.setValue(0F);
        progressBar.setVisible(false);
        targetBulkUploadUiState.setProgressBarCurrentValue(0F);
        targetsCountLabel.setVisible(false);
    }

    /**
     * Restore the target bulk upload layout field values.
     */
    public void restoreComponentsValue() {
        progressBar.setValue(targetBulkUploadUiState.getProgressBarCurrentValue());

        final ProxyBulkUploadWindow bulkUploadInputsToRestore = new ProxyBulkUploadWindow();
        bulkUploadInputsToRestore.setDistributionSetId(targetBulkUploadUiState.getDsId());
        bulkUploadInputsToRestore.setDescription(targetBulkUploadUiState.getDescription());
        bulkUploadInputsToRestore.setTagIdsWithNameToAssign(targetBulkUploadUiState.getTagIdsWithNameToAssign());

        if (targetBulkUploadUiState.getProgressBarCurrentValue() >= 1) {
            targetsCountLabel.setVisible(true);
            targetsCountLabel.setCaption(getFormattedCountLabelValue(targetBulkUploadUiState.getSucessfulUploadCount(),
                    targetBulkUploadUiState.getFailedUploadCount()));
        }
    }

    private String getFormattedCountLabelValue(final int successfulUploadCount, final int failedUploadCount) {
        final StringBuilder countLabelBuilder = new StringBuilder();
        countLabelBuilder.append(
                i18n.getMessage(UIMessageIdProvider.MESSAGE_TARGET_BULKUPLOAD_RESULT_SUCCESS, successfulUploadCount));
        countLabelBuilder.append("<br/><font color=RED>");
        countLabelBuilder
                .append(i18n.getMessage(UIMessageIdProvider.MESSAGE_TARGET_BULKUPLOAD_RESULT_FAIL, failedUploadCount));
        countLabelBuilder.append("</font>");
        return countLabelBuilder.toString();
    }

    /**
     * Actions once bulk upload is completed.
     */
    public void onUploadCompletion() {
        final String targetCountLabel = getFormattedCountLabelValue(targetBulkUploadUiState.getSucessfulUploadCount(),
                targetBulkUploadUiState.getFailedUploadCount());
        targetsCountLabel.setVisible(true);
        targetsCountLabel.setCaption(targetCountLabel);
    }

    /**
     * create and return window
     * 
     * @return Window window
     */
    public Window getWindow() {
        // TODO: check if neede
        // managementUIState.setBulkUploadWindowMinimised(false);

        bulkUploadWindow = new WindowBuilder(SPUIDefinitions.CREATE_UPDATE_WINDOW).caption("").content(this)
                .buildWindow();
        bulkUploadWindow.addStyleName("bulk-upload-window");

        uploadButton.setEnabled(targetBulkUploadUiState.getProgressBarCurrentValue() <= 0);

        return bulkUploadWindow;
    }

    public void disableInputs() {
        dsCombo.setEnabled(false);
        tagsComponent.setEnabled(false);
        descTextArea.setEnabled(false);
        uploadButton.setEnabled(false);
    }
}
