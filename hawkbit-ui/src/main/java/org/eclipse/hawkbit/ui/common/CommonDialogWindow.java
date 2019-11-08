/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common;

import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorderWithIcon;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 *
 * Table pop-up-windows including a minimize and close icon in the upper right
 * corner and a save and cancel button at the bottom. Is not intended to reuse.
 *
 */
public class CommonDialogWindow extends Window {

    private static final long serialVersionUID = 1L;

    private final VerticalLayout mainLayout;

    private final String caption;

    private final Component content;

    private final String helpLink;

    private Button saveButton;

    private Button cancelButton;

    private HorizontalLayout buttonsLayout;

    private final ClickListener cancelButtonClickListener;

    private final ClickListener closeClickListener;

    private final VaadinMessageSource i18n;

    private transient SaveDialogCloseListener closeListener;

    /**
     * Constructor.
     *
     * @param caption
     *            the caption
     * @param content
     *            the content
     * @param helpLink
     *            the helpLinks
     * @param closeListener
     *            the saveDialogCloseListener
     * @param cancelButtonClickListener
     *            the cancelButtonClickListener
     * @param i18n
     *            the i18n service
     */
    public CommonDialogWindow(final String caption, final Component content, final String helpLink,
            final SaveDialogCloseListener closeListener, final ClickListener cancelButtonClickListener,
            final VaadinMessageSource i18n) {
        this.caption = caption;
        this.content = content;
        this.helpLink = helpLink;
        this.closeListener = closeListener;
        this.cancelButtonClickListener = cancelButtonClickListener;
        this.i18n = i18n;

        this.mainLayout = new VerticalLayout();
        this.closeClickListener = this::onCloseEvent;

        init();
    }

    private void onCloseEvent(final ClickEvent clickEvent) {
        if (!clickEvent.getButton().equals(saveButton)) {
            close();
            return;
        }

        if (!closeListener.canWindowSaveOrUpdate()) {
            return;
        }
        closeListener.saveOrUpdate();

        if (closeListener.canWindowClose()) {
            close();
        }

    }

    @Override
    public void close() {
        super.close();
        this.saveButton.setEnabled(false);
    }

    private final void init() {
        mainLayout.setMargin(false);
        mainLayout.setSpacing(false);

        if (content instanceof GridLayout) {
            addStyleName("marginTop");
        }

        if (content != null) {
            mainLayout.addComponent(content);
            mainLayout.setExpandRatio(content, 1.0F);
        }

        createMandatoryLabel();

        final HorizontalLayout buttonLayout = createActionButtonsLayout();
        mainLayout.addComponent(buttonLayout);
        mainLayout.setComponentAlignment(buttonLayout, Alignment.TOP_CENTER);

        setCaption(caption);
        setCaptionAsHtml(true);
        setContent(mainLayout);
        setResizable(false);
        center();
        setModal(true);
        addStyleName("fontsize");
    }

    protected void addCloseListenerForSaveButton() {
        saveButton.addClickListener(closeClickListener);
    }

    protected void addCloseListenerForCancelButton() {
        cancelButton.addClickListener(closeClickListener);
    }

    private HorizontalLayout createActionButtonsLayout() {

        buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSizeFull();
        buttonsLayout.addStyleName("actionButtonsMargin");

        createSaveButton();
        createCancelButton();

        addHelpLink();

        return buttonsLayout;
    }

    private void createMandatoryLabel() {

        final Label mandatoryLabel = new Label(i18n.getMessage("label.mandatory.field"));
        mandatoryLabel.addStyleName(SPUIStyleDefinitions.SP_TEXTFIELD_ERROR + " " + ValoTheme.LABEL_TINY);

        mainLayout.addComponent(mandatoryLabel);
    }

    private void createCancelButton() {
        cancelButton = SPUIComponentProvider.getButton(UIComponentIdProvider.CANCEL_BUTTON,
                i18n.getMessage(UIMessageIdProvider.BUTTON_CANCEL), "", "", true, VaadinIcons.CLOSE,
                SPUIButtonStyleNoBorderWithIcon.class);
        cancelButton.setSizeUndefined();
        cancelButton.addStyleName("default-color");
        addCloseListenerForCancelButton();
        if (cancelButtonClickListener != null) {
            cancelButton.addClickListener(cancelButtonClickListener);
        }

        buttonsLayout.addComponent(cancelButton);
        buttonsLayout.setComponentAlignment(cancelButton, Alignment.MIDDLE_LEFT);
        buttonsLayout.setExpandRatio(cancelButton, 1.0F);
    }

    private void createSaveButton() {
        saveButton = SPUIComponentProvider.getButton(UIComponentIdProvider.SAVE_BUTTON,
                i18n.getMessage(UIMessageIdProvider.BUTTON_SAVE), "", "", true, VaadinIcons.SAFE,
                SPUIButtonStyleNoBorderWithIcon.class);
        saveButton.setSizeUndefined();
        saveButton.addStyleName("default-color");
        addCloseListenerForSaveButton();
        saveButton.setEnabled(false);
        saveButton.setClickShortcut(KeyCode.ENTER);
        buttonsLayout.addComponent(saveButton);
        buttonsLayout.setComponentAlignment(saveButton, Alignment.MIDDLE_RIGHT);
        buttonsLayout.setExpandRatio(saveButton, 1.0F);
    }

    private void addHelpLink() {

        if (StringUtils.isEmpty(helpLink)) {
            return;
        }
        final Link helpLinkComponent = SPUIComponentProvider.getHelpLink(i18n, helpLink);
        buttonsLayout.addComponent(helpLinkComponent);
        buttonsLayout.setComponentAlignment(helpLinkComponent, Alignment.MIDDLE_RIGHT);
    }

    public AbstractComponent getButtonsLayout() {
        return this.buttonsLayout;
    }

    public VerticalLayout getMainLayout() {
        return mainLayout;
    }

    public void setSaveButtonEnabled(final boolean enabled) {
        saveButton.setEnabled(enabled);
    }

    public void setCancelButtonEnabled(final boolean enabled) {
        cancelButton.setEnabled(enabled);
    }

    public void setCloseListener(final SaveDialogCloseListener closeListener) {
        this.closeListener = closeListener;
    }

    /**
     * Check if the safe action can executed. After a the save action the
     * listener checks if the dialog can closed.
     *
     */
    public interface SaveDialogCloseListener {

        /**
         * Checks if the safe action can executed.
         *
         * @return <true> = save action can executed <false> = cannot execute
         *         safe action .
         */
        boolean canWindowSaveOrUpdate();

        /**
         * Checks if the window can be closed after the save action is executed
         *
         * @return <true> = window will close <false> = will not closed.
         */
        default boolean canWindowClose() {
            return true;
        }

        /**
         * Saves/Updates action. Is called if canWindowSaveOrUpdate is <true>.
         *
         */
        void saveOrUpdate();
    }
}
