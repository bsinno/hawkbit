/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.tag;

import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyFilterButton;
import org.eclipse.hawkbit.ui.components.ColorPickerComponent;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.components.colorpicker.ColorUtil;

//TODO: remove duplication with other builders
public class TagWindowLayoutComponentBuilder {

    public static final String TEXTFIELD_NAME = "textfield.name";
    public static final String TEXTFIELD_DESCRIPTION = "textfield.description";

    private final VaadinMessageSource i18n;

    public TagWindowLayoutComponentBuilder(final VaadinMessageSource i18n) {
        this.i18n = i18n;
    }

    public TextField createNameField(final Binder<? extends ProxyFilterButton> binder) {
        final TextField tagName = new TextFieldBuilder(NamedEntity.NAME_MAX_SIZE)
                .id(UIComponentIdProvider.TAG_POPUP_NAME).caption(i18n.getMessage(TEXTFIELD_NAME))
                .prompt(i18n.getMessage(TEXTFIELD_NAME)).buildTextComponent();
        tagName.setSizeUndefined();

        // TODO: use i18n for all the required fields messages
        binder.forField(tagName).asRequired("You must provide tag name").bind(ProxyFilterButton::getName,
                ProxyFilterButton::setName);

        return tagName;
    }

    public TextArea createDescription(final Binder<? extends ProxyFilterButton> binder) {
        final TextArea tagDescription = new TextAreaBuilder(NamedEntity.DESCRIPTION_MAX_SIZE)
                .id(UIComponentIdProvider.TAG_POPUP_DESCRIPTION).caption(i18n.getMessage(TEXTFIELD_DESCRIPTION))
                .prompt(i18n.getMessage(TEXTFIELD_DESCRIPTION)).style("text-area-style").buildTextComponent();
        tagDescription.setSizeUndefined();

        binder.forField(tagDescription).bind(ProxyFilterButton::getDescription, ProxyFilterButton::setDescription);

        return tagDescription;
    }

    public ColorPickerComponent createColorPickerComponent(final Binder<? extends ProxyFilterButton> binder) {
        final ColorPickerComponent colorPickerComponent = new ColorPickerComponent(
                UIComponentIdProvider.TAG_COLOR_PREVIEW_ID, i18n.getMessage("label.choose.tag.color"));

        // TODO: check if we need to convert the value to colour css insted of
        // rgb string
        binder.forField(colorPickerComponent)
                .withConverter(color -> "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")",
                        ColorUtil::stringToColor)
                .bind(ProxyFilterButton::getColour, ProxyFilterButton::setColour);

        return colorPickerComponent;
    }
}
