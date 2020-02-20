/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.builder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TimeZone;

import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.repository.model.NamedVersionedEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ActionCreator;
import org.eclipse.hawkbit.ui.common.data.proxies.Describable;
import org.eclipse.hawkbit.ui.common.data.proxies.Named;
import org.eclipse.hawkbit.ui.common.data.proxies.Versioned;
import org.eclipse.hawkbit.ui.management.miscs.ActionTypeOptionGroupAssignmentLayout;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.data.Binder.BindingBuilder;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * Builder class for from components
 */
public final class FormComponentBuilder {
    public static final String TEXTFIELD_NAME = "textfield.name";
    public static final String TEXTFIELD_VERSION = "textfield.version";
    public static final String TEXTFIELD_DESCRIPTION = "textfield.description";

    private FormComponentBuilder() {
    }

    /**
     * Specifies the type of a binding
     */
    public enum BindType {
        OPTIONAL, REQUIRED
    }

    /**
     * Create an input field for a name
     * 
     * @param <T>
     *            type of the binder
     * @param binder
     *            that is bound to the field
     * @param i18n
     *            message source
     * @param fieldId
     *            id of the field
     * @param bindType
     *            is the field required
     * @return the TextField
     */
    public static <T extends Named> TextField createNameInput(final Binder<T> binder, VaadinMessageSource i18n,
            String fieldId, BindType bindType) {
        final TextField nameInput = new TextFieldBuilder(NamedEntity.NAME_MAX_SIZE).id(fieldId)
                .caption(i18n.getMessage(TEXTFIELD_NAME)).prompt(i18n.getMessage(TEXTFIELD_NAME)).buildTextComponent();
        nameInput.setSizeUndefined();

        BindingBuilder<T, String> bindingBuilder = binder.forField(nameInput);
        if (bindType == BindType.REQUIRED) {
            bindingBuilder.asRequired(UIMessageIdProvider.MESSAGE_ERROR_NAMEREQUIRED);
        }
        bindingBuilder.bind(T::getName, T::setName);
        return nameInput;
    }

    /**
     * Create a required input field for a version
     * 
     * @param <T>
     *            type of the binder
     * @param binder
     *            that is bound to the field
     * @param i18n
     *            message source
     * @param fieldId
     *            id of the field
     * @return the TextField
     */
    public static <T extends Versioned> TextField createVersionInput(final Binder<T> binder, VaadinMessageSource i18n,
            String fieldId) {
        final TextField versionInput = new TextFieldBuilder(NamedVersionedEntity.VERSION_MAX_SIZE).id(fieldId)
                .caption(i18n.getMessage(TEXTFIELD_VERSION)).prompt(i18n.getMessage(TEXTFIELD_VERSION))
                .buildTextComponent();
        versionInput.setSizeUndefined();

        binder.forField(versionInput).asRequired(UIMessageIdProvider.MESSAGE_ERROR_VERSIONREQUIRED).bind(T::getVersion,
                T::setVersion);
        return versionInput;
    }

    /**
     * Create an optional input field for a description
     * 
     * @param <T>
     *            type of the binder
     * @param binder
     *            that is bound to the field
     * @param i18n
     *            message source
     * @param fieldId
     *            id of the field
     * @return the TextArea
     */
    public static <T extends Describable> TextArea createDescriptionInput(final Binder<T> binder,
            VaadinMessageSource i18n, String fieldId) {
        final TextArea descriptionInput = new TextAreaBuilder(NamedEntity.DESCRIPTION_MAX_SIZE).id(fieldId)
                .caption(i18n.getMessage(TEXTFIELD_DESCRIPTION)).prompt(i18n.getMessage(TEXTFIELD_DESCRIPTION))
                .style("text-area-style").buildTextComponent();
        descriptionInput.setSizeUndefined();

        binder.forField(descriptionInput).bind(T::getDescription, T::setDescription);
        return descriptionInput;
    }

    /**
     * Create a bound {@link ActionTypeOptionGroupAssignmentLayout}
     * 
     * @param <T>
     *            type of the binder
     * @param binder
     *            that is bound to the layout
     * @param i18n
     *            message source
     * @param componentId
     *            id of the field layout
     * @return a bound layout
     */
    public static <T extends ActionCreator> ActionTypeOptionGroupAssignmentLayout createActionTypeOptionGroupLayout(
            final Binder<T> binder, VaadinMessageSource i18n, String componentId) {
        final ActionTypeOptionGroupAssignmentLayout actionTypeOptionGroupLayout = new ActionTypeOptionGroupAssignmentLayout(
                i18n, componentId);

        binder.forField(actionTypeOptionGroupLayout.getActionTypeOptionGroup()).bind(T::getActionType,
                T::setActionType);

        final TimeZone tz = SPDateTimeUtil.getBrowserTimeZone();
        binder.forField(actionTypeOptionGroupLayout.getForcedTimeDateField()).withConverter(localDateTime -> {
            if (localDateTime == null) {
                return null;
            }

            return localDateTime.atZone(SPDateTimeUtil.getTimeZoneId(tz)).toInstant().toEpochMilli();
        }, forcedTime -> {
            if (forcedTime == null) {
                return null;
            }

            return LocalDateTime.ofInstant(Instant.ofEpochMilli(forcedTime), SPDateTimeUtil.getTimeZoneId(tz));
        }).bind(T::getForcedTime, T::setForcedTime);

        return actionTypeOptionGroupLayout;
    }
    // TODO DS combo box?

}
