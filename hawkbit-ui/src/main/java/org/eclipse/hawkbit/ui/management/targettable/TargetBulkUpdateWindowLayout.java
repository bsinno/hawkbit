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
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilder;
import org.eclipse.hawkbit.ui.common.builder.WindowBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetToProxyDistributionMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.tagdetails.TagPanelLayout;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.management.event.BulkUploadPopupEvent;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.ProgressBar;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Bulk target upload layout.
 */
public class TargetBulkUpdateWindowLayout extends CustomComponent {
    private static final long serialVersionUID = 1L;

    private final VaadinMessageSource i18n;

    private final transient TargetManagement targetManagement;
    private final transient DistributionSetManagement distributionSetManagement;
    private final transient TargetTagManagement tagManagement;

    private final transient EntityFactory entityFactory;

    private final transient Executor uiExecutor;

    private final transient EventBus.UIEventBus eventBus;

    private final TargetBulkTokenTags targetBulkTokenTags;

    private final TargetBulkUploadUiState targetBulkUploadUiState;

    private final transient DeploymentManagement deploymentManagement;

    private final UiProperties uiproperties;

    private VerticalLayout tokenVerticalLayout;
    private TextArea descTextArea;
    private ComboBox<ProxyDistributionSet> dsNamecomboBox;
    private BulkUploadHandler bulkUploader;
    private VerticalLayout mainLayout;
    private ProgressBar progressBar;
    private Label targetsCountLabel;
    private Link linkToSystemConfigHelp;
    private Window bulkUploadWindow;
    private Label windowCaption;
    private Button minimizeButton;
    private Button closeButton;

    private final DistributionSetStatelessDataProvider dsComboDataProvider;

    TargetBulkUpdateWindowLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker checker, final UINotification uinotification,
            final TargetManagement targetManagement, final DeploymentManagement deploymentManagement,
            final TargetTagManagement tagManagement, final DistributionSetManagement distributionSetManagement,
            final EntityFactory entityFactory, final UiProperties uiproperties, final Executor uiExecutor,
            final TargetBulkUploadUiState targetBulkUploadUiState) {
        this.i18n = i18n;
        this.targetManagement = targetManagement;
        this.eventBus = eventBus;
        this.targetBulkUploadUiState = targetBulkUploadUiState;
        this.deploymentManagement = deploymentManagement;
        this.uiproperties = uiproperties;
        this.tagManagement = tagManagement;
        this.distributionSetManagement = distributionSetManagement;
        this.entityFactory = entityFactory;
        this.uiExecutor = uiExecutor;

        this.dsComboDataProvider = new DistributionSetStatelessDataProvider(distributionSetManagement,
                new DistributionSetToProxyDistributionMapper());

        this.targetBulkTokenTags = new TargetBulkTokenTags(i18n, eventBus, checker, uinotification, tagManagement,
                targetBulkUploadUiState);

        createRequiredComponents();
        buildLayout();

        setCompositionRoot(mainLayout);
    }

    protected void onStartOfUpload() {
        targetBulkUploadUiState
                .setDsNameAndVersion(dsNamecomboBox.getSelectedItem().map(ProxyIdentifiableEntity::getId).orElse(null));
        targetBulkUploadUiState.setDescription(descTextArea.getValue());
        targetBulkUploadUiState.setProgressBarCurrentValue(0F);
        targetBulkUploadUiState.setFailedUploadCount(0);
        targetBulkUploadUiState.setSucessfulUploadCount(0);
        closeButton.setEnabled(false);
        minimizeButton.setEnabled(true);
    }

    protected void setProgressBarValue(final Float value) {
        progressBar.setValue(value);
        progressBar.setVisible(true);
    }

    private void createRequiredComponents() {
        tokenVerticalLayout = getTokenFieldLayout();
        dsNamecomboBox = getDsComboField();
        descTextArea = getDescriptionTextArea();
        progressBar = creatreProgressBar();
        targetsCountLabel = getStatusCountLabel();
        bulkUploader = getBulkUploadHandler();
        linkToSystemConfigHelp = SPUIComponentProvider.getHelpLink(i18n,
                uiproperties.getLinks().getDocumentation().getDeploymentView());
        windowCaption = new Label(i18n.getMessage("caption.bulk.upload.targets"));
        minimizeButton = getMinimizeButton();
        closeButton = getCloseButton();
    }

    private static ProgressBar creatreProgressBar() {
        final ProgressBar progressBarIndicator = new ProgressBar(0F);
        progressBarIndicator.addStyleName("bulk-upload-label");
        progressBarIndicator.setSizeFull();
        return progressBarIndicator;
    }

    private Button getCloseButton() {
        final Button closeBtn = SPUIComponentProvider.getButton(UIComponentIdProvider.BULK_UPLOAD_CLOSE_BUTTON_ID, "",
                "", "", true, FontAwesome.TIMES, SPUIButtonStyleNoBorder.class);
        closeBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        closeBtn.addClickListener(event -> closePopup());
        return closeBtn;
    }

    private Button getMinimizeButton() {
        final Button minimizeBtn = SPUIComponentProvider.getButton(UIComponentIdProvider.BULK_UPLOAD_MINIMIZE_BUTTON_ID,
                "", "", "", true, FontAwesome.MINUS, SPUIButtonStyleNoBorder.class);
        minimizeBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        minimizeBtn.addClickListener(event -> minimizeWindow());
        minimizeBtn.setEnabled(false);
        return minimizeBtn;
    }

    private BulkUploadHandler getBulkUploadHandler() {
        final BulkUploadHandler bulkUploadHandler = new BulkUploadHandler(this, targetManagement, tagManagement,
                distributionSetManagement, deploymentManagement, i18n, entityFactory, UI.getCurrent(), uiExecutor,
                targetBulkUploadUiState);
        bulkUploadHandler.buildLayout();
        bulkUploadHandler.addStyleName(SPUIStyleDefinitions.BULK_UPLOAD_BUTTON);
        return bulkUploadHandler;
    }

    private static Label getStatusCountLabel() {
        final Label countLabel = new Label();

        countLabel.addStyleName("bulk-upload-label");
        countLabel.setVisible(false);
        countLabel.setCaptionAsHtml(true);
        countLabel.setId(UIComponentIdProvider.BULK_UPLOAD_COUNT);
        return countLabel;
    }

    private TextArea getDescriptionTextArea() {
        final TextArea description = new TextAreaBuilder(Target.DESCRIPTION_MAX_SIZE)
                .caption(i18n.getMessage("textfield.description")).style("text-area-style")
                .id(UIComponentIdProvider.BULK_UPLOAD_DESC).buildTextComponent();
        description.setWidth("100%");
        return description;
    }

    private ComboBox getDsComboField() {
        final ComboBox<ProxyDistributionSet> dsCombo = new com.vaadin.ui.ComboBox<>(
                i18n.getMessage("bulkupload.ds.name"));
        dsCombo.setId(UIComponentIdProvider.BULK_UPLOAD_DS_COMBO);
        dsCombo.setDescription(i18n.getMessage("bulkupload.ds.name"));
        dsCombo.setSizeUndefined();
        dsCombo.setWidth("100%");
        dsCombo.addStyleName(SPUIDefinitions.BULK_UPLOD_DS_COMBO_STYLE);
        dsCombo.setItemCaptionGenerator(ProxyDistributionSet::getNameVersion);
        dsCombo.setPageLength(7);
        dsCombo.setDataProvider(dsComboDataProvider);

        return dsCombo;
    }

    private VerticalLayout getTokenFieldLayout() {
        final TagPanelLayout tagPanelLayout = targetBulkTokenTags.getTagPanel();
        tagPanelLayout.setMargin(false);

        final VerticalLayout tokenLayout = new VerticalLayout();
        tokenLayout.setId(UIComponentIdProvider.BULK_UPLOAD_TAG);
        tokenLayout.setSpacing(false);
        tokenLayout.setMargin(false);
        tokenLayout.setSizeFull();
        tokenLayout.setHeight("100px");
        tokenLayout.addStyleName("bulk-target-tags-layout");

        tokenLayout.addComponent(tagPanelLayout);

        return tokenLayout;
    }

    private void closePopup() {
        clearPreviousSessionData();
        bulkUploadWindow.close();
        eventBus.publish(this, BulkUploadPopupEvent.CLOSED);
    }

    private void buildLayout() {
        mainLayout = new VerticalLayout();
        mainLayout.setSpacing(Boolean.TRUE);
        mainLayout.setSizeUndefined();
        mainLayout.setWidth("200px");

        final HorizontalLayout captionLayout = new HorizontalLayout();
        captionLayout.setSizeFull();
        captionLayout.addComponents(windowCaption, minimizeButton, closeButton);
        captionLayout.setExpandRatio(windowCaption, 1.0F);
        captionLayout.addStyleName("v-window-header");

        final HorizontalLayout uploaderLayout = new HorizontalLayout();
        uploaderLayout.addComponent(bulkUploader);
        uploaderLayout.addComponent(linkToSystemConfigHelp);
        uploaderLayout.setComponentAlignment(linkToSystemConfigHelp, Alignment.BOTTOM_RIGHT);
        uploaderLayout.setExpandRatio(bulkUploader, 1.0F);
        uploaderLayout.setSizeFull();
        mainLayout.addComponents(captionLayout, dsNamecomboBox, descTextArea, tokenVerticalLayout, descTextArea,
                progressBar, targetsCountLabel, uploaderLayout);
    }

    /**
     * Reset the values in popup.
     */
    public void resetComponents() {
        dsNamecomboBox.clear();
        descTextArea.clear();
        // targetBulkTokenTags.initializeTags();
        progressBar.setValue(0F);
        progressBar.setVisible(false);
        targetBulkUploadUiState.setProgressBarCurrentValue(0F);
        targetsCountLabel.setVisible(false);
    }

    private void clearPreviousSessionData() {
        targetBulkUploadUiState.setDescription(null);
        targetBulkUploadUiState.setDsNameAndVersion(null);
        targetBulkUploadUiState.setFailedUploadCount(0);
        targetBulkUploadUiState.setSucessfulUploadCount(0);
        targetBulkUploadUiState.getAssignedTagNames().clear();
        targetBulkUploadUiState.getTargetsCreated().clear();
    }

    /**
     * Restore the target bulk upload layout field values.
     */
    public void restoreComponentsValue() {
        progressBar.setValue(targetBulkUploadUiState.getProgressBarCurrentValue());

        final ProxyDistributionSet selectedDs = new ProxyDistributionSet();
        selectedDs.setId(targetBulkUploadUiState.getDsNameAndVersion());
        dsNamecomboBox.setValue(selectedDs);

        descTextArea.setValue(targetBulkUploadUiState.getDescription());
        // targetBulkTokenTags.initializeTags();

        if (targetBulkUploadUiState.getProgressBarCurrentValue() >= 1) {
            targetsCountLabel.setVisible(true);
            targetsCountLabel.setCaption(getFormattedCountLabelValue(targetBulkUploadUiState.getSucessfulUploadCount(),
                    targetBulkUploadUiState.getFailedUploadCount()));
        }
    }

    /**
     * Actions once bulk upload is completed.
     */
    public void onUploadCompletion() {
        final String targetCountLabel = getFormattedCountLabelValue(targetBulkUploadUiState.getSucessfulUploadCount(),
                targetBulkUploadUiState.getFailedUploadCount());
        getTargetsCountLabel().setVisible(true);
        getTargetsCountLabel().setCaption(targetCountLabel);

        closeButton.setEnabled(true);
        minimizeButton.setEnabled(false);
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

        bulkUploader.getUpload().setEnabled(targetBulkUploadUiState.getProgressBarCurrentValue() <= 0);

        return bulkUploadWindow;
    }

    private void minimizeWindow() {
        bulkUploadWindow.close();
        // TODO: check if neede
        // managementUIState.setBulkUploadWindowMinimised(true);
        eventBus.publish(this, BulkUploadPopupEvent.MINIMIZED);
    }

    /**
     * @return the descTextArea
     */
    public TextArea getDescTextArea() {
        return descTextArea;
    }

    /**
     * @return the dsNamecomboBox
     */
    public ComboBox<ProxyDistributionSet> getDsNamecomboBox() {
        return dsNamecomboBox;
    }

    /**
     * @return the progressBar
     */
    public ProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * @return the targetBulkTokenTags
     */
    public TargetBulkTokenTags getTargetBulkTokenTags() {
        return targetBulkTokenTags;
    }

    /**
     * @return the targetsCountLabel
     */
    public Label getTargetsCountLabel() {
        return targetsCountLabel;
    }

    /**
     * @return the eventBus
     */
    public EventBus.UIEventBus getEventBus() {
        return eventBus;
    }

    /**
     * @return the bulkUploader
     */
    public BulkUploadHandler getBulkUploader() {
        return bulkUploader;
    }

}
