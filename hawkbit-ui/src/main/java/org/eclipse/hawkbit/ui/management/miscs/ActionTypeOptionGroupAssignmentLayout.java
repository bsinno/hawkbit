/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.miscs;

import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.hene.flexibleoptiongroup.FlexibleOptionGroup;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.DateField;

/**
 * Action type option group layout for manual assignment.
 */
public class ActionTypeOptionGroupAssignmentLayout extends AbstractActionTypeOptionGroupLayout {
    private static final long serialVersionUID = 1L;

    private DateField forcedTimeDateField;

    /**
     * Constructor
     * 
     * @param i18n
     *            VaadinMessageSource
     */
    public ActionTypeOptionGroupAssignmentLayout(final VaadinMessageSource i18n) {
        super(i18n);
        addValueChangeListener();
    }

    private void addValueChangeListener() {
        actionTypeOptionGroup.addValueChangeListener(new ValueChangeListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void valueChange(final ValueChangeEvent event) {
                if (event.getProperty().getValue().equals(ActionTypeOption.AUTO_FORCED)) {
                    forcedTimeDateField.setEnabled(true);
                    forcedTimeDateField.setRequired(true);
                } else {
                    forcedTimeDateField.setEnabled(false);
                    forcedTimeDateField.setRequired(false);
                }
            }
        });
    }

    @Override
    protected void createOptionGroup() {
        forcedTimeDateField = new DateField();
        actionTypeOptionGroup = new FlexibleOptionGroup();
        actionTypeOptionGroup.addItem(ActionTypeOption.SOFT);
        actionTypeOptionGroup.addItem(ActionTypeOption.FORCED);
        actionTypeOptionGroup.addItem(ActionTypeOption.AUTO_FORCED);
        actionTypeOptionGroup.addItem(ActionTypeOption.DOWNLOAD_ONLY);
        selectDefaultOption();

        addForcedItemWithLabel();
        addSoftItemWithLabel();
        addAutoForceItemWithLabelAndDateField(forcedTimeDateField);
        addDownloadOnlyItemWithLabel();
    }

    public DateField getForcedTimeDateField() {
        return forcedTimeDateField;
    }
}
