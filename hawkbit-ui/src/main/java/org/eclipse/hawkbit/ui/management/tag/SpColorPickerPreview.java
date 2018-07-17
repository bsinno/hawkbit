/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.tag;

import java.lang.reflect.Field;

import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;

import com.google.common.base.Throwables;
import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.shared.Registration;
import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.TextField;
import com.vaadin.ui.components.colorpicker.ColorPickerPreview;

/**
 *
 *
 *
 * Tag ColorPicker Preview field CssLayout cannot be removed because of the deep
 * inheritance issue, since protected method fireEvent() of the superclass of
 * CssLayout is used in textChange() overridden method
 *
 *
 */
public final class SpColorPickerPreview extends ColorPickerPreview implements ValueChangeListener<String> {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * 
     * @param color
     *            of the picker
     */
    public SpColorPickerPreview(final Color color) {
        super(color);

        try {
            final Field textField = ColorPickerPreview.class.getDeclaredField("field");
            textField.setAccessible(true);
            ((TextField) textField.get(this)).setId(UIComponentIdProvider.COLOR_PREVIEW_FIELD);
            ((TextField) textField.get(this)).addValueChangeListener(this);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            Throwables.propagate(e);
        }
    }

    private static final class EventHolder implements HasValue<String> {
        private static final long serialVersionUID = 1L;
        private final ValueChangeEvent<String> event;

        private EventHolder(final ValueChangeEvent<String> event) {
            this.event = event;
        }

        @Override
        public String getValue() {
            return event.getValue();
        }

        @Override
        public void setValue(final String newValue) {
            // not needed as this property is only a hull for TextChangeEvent
            // payload
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public void setReadOnly(final boolean newStatus) {
            // not needed as this property is only a hull for TextChangeEvent
            // payload
        }

        @Override
        public Registration addValueChangeListener(final ValueChangeListener<String> listener) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean isRequiredIndicatorVisible() {
            // TODO Auto-generated method stub
            return false;
        }
    }

    @Override
    public void valueChange(final ValueChangeEvent<String> event) {
        super.valueChange(new ValueChangeEvent(field, applicationData, readOnly) {
            private static final long serialVersionUID = 1L;

            @Override
            public Property<String> getProperty() {
                return new EventHolder(event);
            }
        });
    }

}
