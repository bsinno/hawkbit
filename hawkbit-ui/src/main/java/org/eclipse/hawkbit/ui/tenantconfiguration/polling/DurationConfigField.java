/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration.polling;

import java.time.Duration;

import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;

/**
 * The DurationConfigField consists of three vaadin fields. A {@link #Label}
 * {@link #DurationField} and a {@link #CheckBox}. The user can then enter a
 * duration in the DurationField or he can configure using the global duration
 * by changing the CheckBox.
 */
public final class DurationConfigField extends HorizontalLayout {
    private final CheckBox checkBox = new CheckBox();
    private final DurationField durationField = new DurationField();
    private transient Duration globalDuration;

    private DurationConfigField(final String id, VaadinMessageSource i18n) {
        this.setId(id);

        //ToDo: duration-config-field css does not exist. add styling for bottom margin
        this.addStyleName("duration-config-field");
        this.setSpacing(true);
        this.setMargin(false);

        durationField.setId(id + ".field");
        checkBox.setId(id + ".checkbox");
        durationField.setEnabled(false);
        durationField.setI18n(i18n);

        this.addComponent(checkBox);
        this.setComponentAlignment(checkBox, Alignment.MIDDLE_LEFT);

        this.addComponent(durationField);
        this.setExpandRatio(durationField, 1.0F);
        this.setComponentAlignment(durationField, Alignment.MIDDLE_LEFT);

        checkBox.addValueChangeListener(event -> checkBoxChanged(event.getValue()));
    }

    private void checkBoxChanged(final Boolean isActivated) {
        durationField.setEnabled(isActivated);

        if (!isActivated) {
            durationField.setDuration(globalDuration);
        }
    }

    /**
     * has to be called before using, see Builder Implementation.
     *
     * @param tenantDuration
     *         tenant specific duration value
     * @param globalDuration
     *         duration value which is stored in the global configuration
     */
    private void init(final Duration globalDuration, final Duration tenantDuration) {
        this.globalDuration = globalDuration;
    }

    private void setCheckBoxTooltip(final String label) {
        checkBox.setDescription(label);
    }

    private void setAllowedRange(final Duration minimumDuration, final Duration maximumDuration) {
        durationField.setMinimumDuration(minimumDuration);
        durationField.setMaximumDuration(maximumDuration);
    }

    public CheckBox getCheckBox() {
        return checkBox;
    }

    public DurationField getDurationField() {
        return durationField;
    }

    /**
     * Create a DurationConfigFieldBuilder.
     *
     * @return the builder
     */
    public static DurationConfigFieldBuilder builder(final String id, final VaadinMessageSource i18n) {
        return new DurationConfigFieldBuilder(id, i18n);
    }

    /**
     * Builder for the calendar widget.
     */
    public static final class DurationConfigFieldBuilder {
        private final DurationConfigField field;

        private Duration globalDuration;
        private Duration tenantDuration;

        private DurationConfigFieldBuilder(final String id, VaadinMessageSource i18n) {
            field = new DurationConfigField(id, i18n);
        }

        /**
         * set the checkbox tooltip.
         *
         * @param label
         *         the tooltip
         *
         * @return the builder
         */
        public DurationConfigFieldBuilder checkBoxTooltip(final String label) {
            field.setCheckBoxTooltip(label);
            return this;
        }

        /**
         * set the global duration.
         *
         * @param globalDuration
         *         the global duration
         *
         * @return the builder
         */
        public DurationConfigFieldBuilder globalDuration(final Duration globalDuration) {
            this.globalDuration = globalDuration;
            return this;
        }

        /**
         * set the caption.
         *
         * @param caption
         *         the caption
         *
         * @return the builder
         */
        public DurationConfigFieldBuilder caption(final String caption) {
            field.setCaption(caption);
            return this;
        }

        /**
         * set the range.
         *
         * @param minDuration
         *         min duration
         * @param maxDuration
         *         max duration
         *
         * @return the builder
         */
        public DurationConfigFieldBuilder range(final Duration minDuration, final Duration maxDuration) {
            field.setAllowedRange(minDuration, maxDuration);
            return this;
        }

        /**
         * set the tenant duration.
         *
         * @param tenantDuration
         *         the duration
         *
         * @return the builder
         */
        public DurationConfigFieldBuilder tenantDuration(final Duration tenantDuration) {
            this.tenantDuration = tenantDuration;
            return this;
        }

        /**
         * Create the {@link DurationConfigField}.
         *
         * @return the {@link DurationConfigField}
         */
        public DurationConfigField build() {
            if (globalDuration == null) {
                throw new IllegalStateException(
                        "Cannot build DurationConfigField without a value for global duration.");
            }

            field.init(globalDuration, tenantDuration);
            return field;
        }
    }
}
