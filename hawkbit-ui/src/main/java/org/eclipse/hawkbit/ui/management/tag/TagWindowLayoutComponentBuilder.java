/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.tag;

import org.eclipse.hawkbit.ui.common.builder.FormComponentBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyFilterButton;
import org.eclipse.hawkbit.ui.components.ColorPickerComponent;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.components.colorpicker.ColorUtil;

//TODO: remove duplication with other builders
public class TagWindowLayoutComponentBuilder {

    private final VaadinMessageSource i18n;
    private final UINotification uiNotification;

    public TagWindowLayoutComponentBuilder(final VaadinMessageSource i18n, final UINotification uiNotification) {
        this.i18n = i18n;
        this.uiNotification = uiNotification;
    }

    /**
     * create name field
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public TextField createNameField(final Binder<? extends ProxyFilterButton> binder) {
        return FormComponentBuilder.createNameInput(binder, i18n, UIComponentIdProvider.TAG_POPUP_NAME).getComponent();
    }

    /**
     * create description field
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public TextArea createDescription(final Binder<? extends ProxyFilterButton> binder) {
        return FormComponentBuilder.createDescriptionInput(binder, i18n, UIComponentIdProvider.TAG_POPUP_DESCRIPTION)
                .getComponent();
    }

    public ColorPickerComponent createColorPickerComponent(final Binder<? extends ProxyFilterButton> binder) {
        final ColorPickerComponent colorPickerComponent = new ColorPickerComponent(
                UIComponentIdProvider.TAG_COLOR_PREVIEW_ID, i18n.getMessage("label.choose.tag.color"));

        // TODO: check if we need to convert the value to colour css insted of
        // rgb string
        binder.forField(colorPickerComponent)
                .withConverter(color -> "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")",
                        colorString -> {
                            try {
                                return ColorUtil.stringToColor(colorString);
                            } catch (final NumberFormatException e) {
                                try {
                                    return BasicColor.valueOf(colorString.trim().toUpperCase()).getColor();
                                } catch (final IllegalArgumentException ex) {
                                    // TODO: use i18n
                                    uiNotification.displayValidationError(
                                            "There is no mapping for the provided colour: " + colorString);
                                    return ColorUtil.stringToColor("#2c9720");
                                }
                            }
                        })
                .bind(ProxyFilterButton::getColour, ProxyFilterButton::setColour);

        return colorPickerComponent;
    }

    private enum BasicColor {
        BLACK(Color.BLACK), BLUE(Color.BLUE), CYAN(Color.CYAN), GREEN(Color.GREEN), MAGENTA(Color.MAGENTA), RED(
                Color.RED), WHITE(Color.WHITE), YELLOW(Color.YELLOW);

        private final Color color;

        private BasicColor(final Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }
    }
}
