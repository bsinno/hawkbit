/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.bulkupload;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
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
import com.vaadin.shared.ui.ContentMode;
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
    private final UINotification uinotification;

    private final TargetBulkUploadUiState targetBulkUploadUiState;

    private final ComboBox<ProxyDistributionSet> dsCombo;
    private final transient TargetBulkTokenTags tagsComponent;
    private final TextArea descTextArea;
    private final ProgressBar progressBar;
    private final Label targetsCountLabel;
    private final Upload uploadButton;
    private final Link linkToSystemConfigHelp;

    private final Label windowCaption;
    private final Button closeButton;
    private Window bulkUploadWindow;

    private final Binder<ProxyBulkUploadWindow> binder;

    public TargetBulkUpdateWindowLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker checker, final UINotification uinotification,
            final TargetManagement targetManagement, final DeploymentManagement deploymentManagement,
            final TargetTagManagement tagManagement, final DistributionSetManagement distributionSetManagement,
            final EntityFactory entityFactory, final UiProperties uiproperties, final Executor uiExecutor,
            final TargetBulkUploadUiState targetBulkUploadUiState) {
        this.i18n = i18n;
        this.uinotification = uinotification;
        this.targetBulkUploadUiState = targetBulkUploadUiState;
        this.binder = new Binder<>();

        final BulkUploadWindowLayoutComponentBuilder componentBuilder = new BulkUploadWindowLayoutComponentBuilder(i18n,
                distributionSetManagement);

        this.dsCombo = componentBuilder.createDistributionSetCombo(binder);

        this.tagsComponent = new TargetBulkTokenTags(i18n, eventBus, checker, uinotification, tagManagement);

        this.descTextArea = componentBuilder.createDescriptionField(binder);
        this.progressBar = createProgressBar();
        this.targetsCountLabel = getStatusCountLabel();

        final BulkUploadHandler bulkUploadHandler = new BulkUploadHandler(i18n, eventBus, entityFactory, uiExecutor,
                targetManagement, tagManagement, distributionSetManagement, deploymentManagement,
                this::getBulkUploadInputsBean);
        this.uploadButton = createUploadButton(bulkUploadHandler);

        this.linkToSystemConfigHelp = SPUIComponentProvider.getHelpLink(i18n,
                uiproperties.getLinks().getDocumentation().getDeploymentView());

        this.windowCaption = new Label(i18n.getMessage("caption.bulk.upload.targets"));
        this.closeButton = getCloseButton();

        buildLayout();
        reset();
    }

    private ProgressBar createProgressBar() {
        final ProgressBar progressBarIndicator = new ProgressBar(0F);
        progressBarIndicator.setCaption(i18n.getMessage("artifact.upload.progress.caption"));
        progressBarIndicator.setSizeFull();
        progressBarIndicator.setVisible(false);

        return progressBarIndicator;
    }

    private static Label getStatusCountLabel() {
        final Label countLabel = new Label("", ContentMode.HTML);
        countLabel.setId(UIComponentIdProvider.BULK_UPLOAD_COUNT);
        countLabel.setVisible(false);

        return countLabel;
    }

    private ProxyBulkUploadWindow getBulkUploadInputsBean() {
        final ProxyBulkUploadWindow bean = new ProxyBulkUploadWindow();
        bean.setDistributionSetId(binder.getBean().getDistributionSetId());
        bean.setTagIdsWithNameToAssign(getTagIdsWithNameToAssign());
        bean.setDescription(binder.getBean().getDescription());

        return bean;
    }

    private Map<Long, String> getTagIdsWithNameToAssign() {
        return tagsComponent.getSelectedTagsForAssignment().stream()
                .collect(Collectors.toMap(ProxyTag::getId, ProxyTag::getName));
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

    private Button getCloseButton() {
        final Button closeBtn = SPUIComponentProvider.getButton(UIComponentIdProvider.BULK_UPLOAD_CLOSE_BUTTON_ID, "",
                "", "", true, VaadinIcons.CLOSE, SPUIButtonStyleNoBorder.class);
        closeBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        closeBtn.addClickListener(event -> closePopup());
        return closeBtn;
    }

    private void closePopup() {
        if (!targetBulkUploadUiState.isInProgress()) {
            clearPreviousSessionData();
            reset();
        }
        bulkUploadWindow.close();
    }

    private void clearPreviousSessionData() {
        targetBulkUploadUiState.setDsId(null);
        targetBulkUploadUiState.getTagIdsWithNameToAssign().clear();
        targetBulkUploadUiState.setDescription(null);
    }

    /**
     * Reset the values in popup.
     */
    private void reset() {
        binder.setBean(new ProxyBulkUploadWindow());

        // init with dummy master entity in order to init tag panel
        tagsComponent.updateMasterEntityFilter(new ProxyTarget());

        progressBar.setValue(0F);
        progressBar.setVisible(false);

        targetsCountLabel.setValue("");
        targetsCountLabel.setVisible(false);
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

        tagsLayout.addComponent(tagsComponent.getTagPanel());

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
        targetBulkUploadUiState.setDsId(binder.getBean().getDistributionSetId());
        targetBulkUploadUiState.setTagIdsWithNameToAssign(getTagIdsWithNameToAssign());
        targetBulkUploadUiState.setDescription(binder.getBean().getDescription());

        targetsCountLabel.setVisible(true);
        targetsCountLabel.setValue(i18n.getMessage("message.bulk.upload.upload.started"));

        disableInputs();
    }

    public void onStartOfProvisioning() {
        targetsCountLabel.setValue(i18n.getMessage("message.bulk.upload.provisioning.started"));
    }

    public void setProgressBarValue(final float value) {
        progressBar.setValue(value);
        progressBar.setVisible(true);
    }

    public void onStartOfAssignment() {
        targetsCountLabel.setValue(i18n.getMessage("message.bulk.upload.assignment.started"));
    }

    /**
     * Restore the target bulk upload layout field values.
     */
    public void restoreComponentsValue() {
        final ProxyBulkUploadWindow bulkUploadInputsToRestore = new ProxyBulkUploadWindow();
        bulkUploadInputsToRestore.setDistributionSetId(targetBulkUploadUiState.getDsId());
        bulkUploadInputsToRestore.setDescription(targetBulkUploadUiState.getDescription());
        bulkUploadInputsToRestore.setTagIdsWithNameToAssign(targetBulkUploadUiState.getTagIdsWithNameToAssign());

        binder.setBean(bulkUploadInputsToRestore);
        tagsComponent.getTagsById(targetBulkUploadUiState.getTagIdsWithNameToAssign().keySet())
                .forEach(tagsComponent::assignTag);
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
    public void onUploadCompletion(final int successCount, final int failCount) {
        final String targetCountLabel = getFormattedCountLabelValue(successCount, failCount);
        targetsCountLabel.setValue(targetCountLabel);

        enableInputs();
    }

    public void onUploadFailure(final String failureReason) {
        targetsCountLabel.setValue(
                new StringBuilder().append("<font color=RED>").append(failureReason).append("</font>").toString());

        uinotification.displayValidationError(failureReason);
        enableInputs();
    }

    public void onAssignmentFailure(final String failureReason) {
        uinotification.displayValidationError(failureReason);
    }

    /**
     * create and return window
     * 
     * @return Window window
     */
    public Window getWindow() {
        if (!targetBulkUploadUiState.isInProgress()) {
            reset();
        }
        bulkUploadWindow = new WindowBuilder(SPUIDefinitions.CREATE_UPDATE_WINDOW).caption("").content(this)
                .buildWindow();
        bulkUploadWindow.addStyleName("bulk-upload-window");

        return bulkUploadWindow;
    }

    private void disableInputs() {
        changeInputsState(false);
    }

    private void changeInputsState(final boolean enabled) {
        dsCombo.setEnabled(enabled);
        tagsComponent.getTagPanel().setEnabled(enabled);
        descTextArea.setEnabled(enabled);
        uploadButton.setEnabled(enabled);
    }

    private void enableInputs() {
        changeInputsState(true);
    }
}
