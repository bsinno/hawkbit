/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

//TODO: remove duplication with other builders
public class TargetWindowLayoutComponentBuilder {

    public static final String TEXTFIELD_CONTROLLER_ID = "prompt.target.id";
    public static final String TEXTFIELD_NAME = "textfield.name";
    public static final String TEXTFIELD_DESCRIPTION = "textfield.description";

    private final VaadinMessageSource i18n;

    private Binding<ProxyTarget, String> targetNameBinding;

    public TargetWindowLayoutComponentBuilder(final VaadinMessageSource i18n) {
        this.i18n = i18n;
    }

    public TextField createControllerIdField(final Binder<ProxyTarget> binder) {
        final TextField targetControllerId = new TextFieldBuilder(Target.CONTROLLER_ID_MAX_SIZE)
                .id(UIComponentIdProvider.TARGET_ADD_CONTROLLER_ID).caption(i18n.getMessage(TEXTFIELD_CONTROLLER_ID))
                .prompt(i18n.getMessage(TEXTFIELD_CONTROLLER_ID)).buildTextComponent();
        targetControllerId.setSizeUndefined();

        // TODO: use i18n for all the required fields messages
        binder.forField(targetControllerId).asRequired("You must provide controller id")
                .withValidator(new RegexpValidator(i18n.getMessage("message.target.whitespace.check"), "[.\\S]*"))
                .bind(ProxyTarget::getControllerId, ProxyTarget::setControllerId);

        return targetControllerId;
    }

    public TextField createNameField(final Binder<ProxyTarget> binder) {
        final TextField targetgName = new TextFieldBuilder(NamedEntity.NAME_MAX_SIZE)
                .id(UIComponentIdProvider.TARGET_ADD_NAME).caption(i18n.getMessage(TEXTFIELD_NAME))
                .prompt(i18n.getMessage(TEXTFIELD_NAME)).buildTextComponent();
        targetgName.setSizeUndefined();

        targetNameBinding = binder.forField(targetgName).bind(ProxyTarget::getName, ProxyTarget::setName);

        return targetgName;
    }

    public TextArea createDescriptionField(final Binder<ProxyTarget> binder) {
        final TextArea targetDescription = new TextAreaBuilder(NamedEntity.DESCRIPTION_MAX_SIZE)
                .id(UIComponentIdProvider.TARGET_ADD_DESC).caption(i18n.getMessage(TEXTFIELD_DESCRIPTION))
                .prompt(i18n.getMessage(TEXTFIELD_DESCRIPTION)).style("text-area-style").buildTextComponent();
        targetDescription.setSizeUndefined();

        binder.forField(targetDescription).bind(ProxyTarget::getDescription, ProxyTarget::setDescription);

        return targetDescription;
    }

    public Binding<ProxyTarget, String> getTargetNameBinding() {
        return targetNameBinding;
    }
}
