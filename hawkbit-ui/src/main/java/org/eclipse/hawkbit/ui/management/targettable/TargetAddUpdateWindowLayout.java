/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
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
import org.eclipse.hawkbit.ui.common.CommonDialogWindowV7;
import org.eclipse.hawkbit.ui.common.CommonDialogWindowV7.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilderV7;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilderV7;
import org.eclipse.hawkbit.ui.common.builder.WindowBuilderV7;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.google.common.collect.Sets;
import com.vaadin.v7.data.validator.RegexpValidator;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.v7.ui.TextField;
import com.vaadin.ui.Window;

/**
 * Add and Update Target.
 */
public class TargetAddUpdateWindowLayout extends CustomComponent {

    private static final long serialVersionUID = -6659290471705262389L;

    private final VaadinMessageSource i18n;

    private final transient TargetManagement targetManagement;

    private final transient EventBus.UIEventBus eventBus;

    private final UINotification uINotification;

    private final transient EntityFactory entityFactory;

    private TextField controllerIDTextField;
    private TextField nameTextField;
    private TextArea descTextArea;
    private boolean editTarget;
    private String controllerId;
    private FormLayout formLayout;
    private CommonDialogWindowV7 window;

    private final TargetTable targetTable;

    TargetAddUpdateWindowLayout(final VaadinMessageSource i18n, final TargetManagement targetManagement,
            final UIEventBus eventBus, final UINotification uINotification, final EntityFactory entityFactory,
            final TargetTable targetTable) {
        this.i18n = i18n;
        this.targetManagement = targetManagement;
        this.eventBus = eventBus;
        this.uINotification = uINotification;
        this.entityFactory = entityFactory;
        this.targetTable = targetTable;
        createRequiredComponents();
        buildLayout();
        setCompositionRoot(formLayout);
    }

    /**
     * Save or update the target.
     */
    private final class SaveOnDialogCloseListener implements SaveDialogCloseListener {
        @Override
        public void saveOrUpdate() {
            if (editTarget) {
                updateTarget();
                return;
            }
            addNewTarget();
        }

        @Override
        public boolean canWindowSaveOrUpdate() {
            return editTarget || !isDuplicate();
        }

    }

    private void createRequiredComponents() {
        controllerIDTextField = new TextFieldBuilderV7(Target.CONTROLLER_ID_MAX_SIZE)
                .caption(i18n.getMessage("prompt.target.id")).required(true, i18n)
                .id(UIComponentIdProvider.TARGET_ADD_CONTROLLER_ID).buildTextComponent();
        controllerIDTextField
                .addValidator(new RegexpValidator("[.\\S]*", i18n.getMessage("message.target.whitespace.check")));
        nameTextField = new TextFieldBuilderV7(Target.NAME_MAX_SIZE).caption(i18n.getMessage("textfield.name"))
                .id(UIComponentIdProvider.TARGET_ADD_NAME).buildTextComponent();
        nameTextField.setRequired(false);

        descTextArea = new TextAreaBuilderV7(Target.DESCRIPTION_MAX_SIZE)
                .caption(i18n.getMessage("textfield.description")).style("text-area-style")
                .id(UIComponentIdProvider.TARGET_ADD_DESC).buildTextComponent();
    }

    private void buildLayout() {
        setSizeUndefined();
        formLayout = new FormLayout();
        formLayout.addComponent(controllerIDTextField);
        formLayout.addComponent(nameTextField);
        formLayout.addComponent(descTextArea);

        controllerIDTextField.focus();
    }

    /**
     * Update the Target if modified.
     */
    public void updateTarget() {
        /* save updated entity */
        final Target target = targetManagement.update(entityFactory.target().update(controllerId)
                .name(nameTextField.getValue()).description(descTextArea.getValue()));
        /* display success msg */
        uINotification.displaySuccess(i18n.getMessage("message.update.success", target.getName()));
        // publishing through event bus
        eventBus.publish(this, new TargetTableEvent(BaseEntityEventType.UPDATED_ENTITY, target));
    }

    private void addNewTarget() {
        final String newControllerId = controllerIDTextField.getValue();
        final String newName = nameTextField.getValue();
        final String newDesc = descTextArea.getValue();

        final Target newTarget = targetManagement.create(
                entityFactory.target().create().controllerId(newControllerId).name(newName).description(newDesc));

        eventBus.publish(this, new TargetTableEvent(BaseEntityEventType.ADD_ENTITY, newTarget));
        uINotification.displaySuccess(i18n.getMessage("message.save.success", newTarget.getName()));
        targetTable.setValue(Sets.newHashSet(newTarget.getId()));
    }

    public Window createNewWindow() {
        window = new WindowBuilderV7(SPUIDefinitions.CREATE_UPDATE_WINDOW)
                .caption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.target"))).content(this)
                .layout(formLayout).i18n(i18n).saveDialogCloseListener(new SaveOnDialogCloseListener())
                .buildCommonDialogWindow();
        return window;
    }

    /**
     * Returns Target Update window based on the selected Entity Id in the
     * target table.
     * 
     * @param controllerId
     *            the target controller id
     * @return window or {@code null} if target is not exists.
     */
    public Window getWindow(final String controllerId) {
        final Optional<Target> target = targetManagement.getByControllerID(controllerId);
        if (!target.isPresent()) {
            uINotification.displayWarning(i18n.getMessage("target.not.exists", controllerId));
            return null;
        }
        populateValuesOfTarget(target.get());
        createNewWindow();
        window.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.target")));
        window.addStyleName("target-update-window");
        return window;
    }

    /**
     * clear all fields of Target Edit Window.
     */
    public void resetComponents() {
        nameTextField.clear();
        nameTextField.removeStyleName(SPUIStyleDefinitions.SP_TEXTFIELD_ERROR);
        controllerIDTextField.setEnabled(Boolean.TRUE);
        controllerIDTextField.removeStyleName(SPUIStyleDefinitions.SP_TEXTFIELD_ERROR);
        controllerIDTextField.clear();
        descTextArea.clear();
        editTarget = Boolean.FALSE;
    }

    private boolean isDuplicate() {
        final String newControlllerId = controllerIDTextField.getValue();
        final Optional<Target> existingTarget = targetManagement.getByControllerID(newControlllerId.trim());
        if (existingTarget.isPresent()) {
            uINotification.displayValidationError(i18n.getMessage("message.target.duplicate.check", newControlllerId));
            return true;
        } else {
            return false;
        }

    }

    private void populateValuesOfTarget(final Target target) {
        resetComponents();
        this.controllerId = target.getControllerId();
        editTarget = Boolean.TRUE;

        controllerIDTextField.setValue(target.getControllerId());
        controllerIDTextField.setEnabled(Boolean.FALSE);
        nameTextField.setValue(target.getName());
        nameTextField.setRequired(true);
        descTextArea.setValue(target.getDescription());
    }

    public FormLayout getFormLayout() {
        return formLayout;
    }

}
