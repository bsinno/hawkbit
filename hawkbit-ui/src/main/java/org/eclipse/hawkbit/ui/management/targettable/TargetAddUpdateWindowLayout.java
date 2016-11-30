/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetIdName;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.builder.WindowBuilder;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.management.event.DragEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.I18N;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

/**
 * Add and Update Target.
 */
public class TargetAddUpdateWindowLayout extends CustomComponent {

    private static final long serialVersionUID = -6659290471705262389L;

    private final I18N i18n;

    private final transient TargetManagement targetManagement;

    private final transient EventBus.UIEventBus eventBus;

    private final UINotification uINotification;

    private final transient EntityFactory entityFactory;

    private final TargetTable targetTable;

    private TextField controllerIDTextField;
    private TextField nameTextField;
    private TextArea descTextArea;
    private boolean editTarget;
    private String controllerId;
    private FormLayout formLayout;
    private CommonDialogWindow window;

    TargetAddUpdateWindowLayout(final I18N i18n, final TargetManagement targetManagement, final UIEventBus eventBus,
            final UINotification uINotification, final EntityFactory entityFactory, final TargetTable targetTable) {
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
        controllerIDTextField = createTextField("prompt.target.id", UIComponentIdProvider.TARGET_ADD_CONTROLLER_ID);
        controllerIDTextField.addValidator(new RegexpValidator("[.\\S]*", i18n.get("message.target.whitespace.check")));
        nameTextField = createTextField("textfield.name", UIComponentIdProvider.TARGET_ADD_NAME);
        nameTextField.setRequired(false);

        descTextArea = new TextAreaBuilder().caption(i18n.get("textfield.description")).style("text-area-style")
                .prompt(i18n.get("textfield.description")).immediate(true).id(UIComponentIdProvider.TARGET_ADD_DESC)
                .buildTextComponent();
        descTextArea.setNullRepresentation(HawkbitCommonUtil.SP_STRING_EMPTY);
    }

    private TextField createTextField(final String in18Key, final String id) {
        return new TextFieldBuilder().caption(i18n.get(in18Key)).required(true).prompt(i18n.get(in18Key))
                .immediate(true).id(id).buildTextComponent();
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
        final Target target = targetManagement.updateTarget(entityFactory.target().update(controllerId)
                .name(nameTextField.getValue()).description(descTextArea.getValue()));
        /* display success msg */
        uINotification.displaySuccess(i18n.get("message.update.success", new Object[] { target.getName() }));
        // publishing through event bus
        eventBus.publish(this, new TargetTableEvent(BaseEntityEventType.UPDATED_ENTITY, target));
    }

    private void addNewTarget() {
        final String newControllerId = HawkbitCommonUtil.trimAndNullIfEmpty(controllerIDTextField.getValue());
        final String newName = HawkbitCommonUtil.trimAndNullIfEmpty(nameTextField.getValue());
        final String newDesc = HawkbitCommonUtil.trimAndNullIfEmpty(descTextArea.getValue());

        /* save new target */
        final Target newTarget = targetManagement.createTarget(
                entityFactory.target().create().controllerId(newControllerId).name(newName).description(newDesc));

        final Set<TargetIdName> s = new HashSet<>();
        s.add(newTarget.getTargetIdName());
        targetTable.setValue(s);

        /* display success msg */
        uINotification.displaySuccess(i18n.get("message.save.success", new Object[] { newTarget.getName() }));
    }

    public Window getWindow() {
        eventBus.publish(this, DragEvent.HIDE_DROP_HINT);
        window = new WindowBuilder(SPUIDefinitions.CREATE_UPDATE_WINDOW).caption(i18n.get("caption.add.new.target"))
                .content(this).layout(formLayout).i18n(i18n).saveDialogCloseListener(new SaveOnDialogCloseListener())
                .buildCommonDialogWindow();

        return window;
    }

    /**
     * Returns Target Update window based on the selected Entity Id in the
     * target table.
     * 
     * @param entityId
     * @return window
     */
    public Window getWindow(final String entityId) {
        populateValuesOfTarget(entityId);
        getWindow();
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
        final Target existingTarget = targetManagement.findTargetByControllerID(newControlllerId.trim());
        if (existingTarget != null) {
            uINotification.displayValidationError(
                    i18n.get("message.target.duplicate.check", new Object[] { newControlllerId }));
            return true;
        } else {
            return false;
        }

    }

    /**
     * @param controllerId
     */
    private void populateValuesOfTarget(final String controllerId) {
        resetComponents();
        this.controllerId = controllerId;
        editTarget = Boolean.TRUE;
        final Target target = targetManagement.findTargetByControllerID(controllerId);
        controllerIDTextField.setValue(target.getControllerId());
        controllerIDTextField.setEnabled(Boolean.FALSE);
        nameTextField.setValue(target.getName());
        if (target.getDescription() != null) {
            descTextArea.setValue(target.getDescription());
        }
    }

    public FormLayout getFormLayout() {
        return formLayout;
    }

}
