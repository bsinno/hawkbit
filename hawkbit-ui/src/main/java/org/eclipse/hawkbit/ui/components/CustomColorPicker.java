/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.components;

import com.vaadin.ui.ColorPicker;

public class CustomColorPicker extends ColorPicker {
    private static final long serialVersionUID = 1L;

    private final ColorPickerComponent colorPickerLayout;

    public CustomColorPicker() {
        super();

        this.colorPickerLayout = new ColorPickerComponent();
        this.getColorPickerLayout().setVisible(false);
        this.getColorPickerLayout().addValueChangeListener(event -> setValue(event.getValue()));
    }

    @Override
    protected void showPopup(final boolean open) {
        getColorPickerLayout().setVisible(open);
    }

    public ColorPickerComponent getColorPickerLayout() {
        return colorPickerLayout;
    }
}
