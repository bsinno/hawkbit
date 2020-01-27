/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.miscs;

import java.util.TimeZone;

import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.shared.ui.datefield.DateTimeResolution;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.DateTimeField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Action type option group layout for manual assignment.
 */
public class ActionTypeOptionGroupAssignmentLayout extends AbstractActionTypeOptionGroupLayout {
    private static final long serialVersionUID = 1L;

    protected static final String STYLE_DIST_WINDOW_FORCEDTIME = "dist-window-forcedtime";

    private DateTimeField forcedTimeDateField;

    /**
     * Constructor
     * 
     * @param i18n
     *            VaadinMessageSource
     */
    public ActionTypeOptionGroupAssignmentLayout(final VaadinMessageSource i18n) {
        super(i18n);
        actionTypeOptionGroup.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
        addValueChangeListener();
    }

    private void addValueChangeListener() {
        actionTypeOptionGroup.addValueChangeListener(event -> {
            if (event.getValue() == ActionType.TIMEFORCED) {
                forcedTimeDateField.setEnabled(true);
                forcedTimeDateField.setRequiredIndicatorVisible(true);
            } else {
                forcedTimeDateField.setEnabled(false);
                forcedTimeDateField.setRequiredIndicatorVisible(false);
            }
        });
    }

    @Override
    protected void addOptionGroup() {
        actionTypeOptionGroup.setItems(ActionType.FORCED, ActionType.SOFT, ActionType.DOWNLOAD_ONLY,
                ActionType.TIMEFORCED);
        addComponent(actionTypeOptionGroup);
        addTimeforcedDateTimeField();
    }

    private void addTimeforcedDateTimeField() {
        forcedTimeDateField = new DateTimeField();
        forcedTimeDateField.setEnabled(false);
        forcedTimeDateField.setStyleName(STYLE_DIST_WINDOW_FORCEDTIME);

        final TimeZone tz = SPDateTimeUtil.getBrowserTimeZone();
        forcedTimeDateField.setZoneId(SPDateTimeUtil.getTimeZoneId(tz));
        forcedTimeDateField.setLocale(HawkbitCommonUtil.getCurrentLocale());
        forcedTimeDateField.setResolution(DateTimeResolution.MINUTE);
        forcedTimeDateField.addStyleName(ValoTheme.DATEFIELD_TINY);
        forcedTimeDateField.setWidth("14em");
        addComponent(forcedTimeDateField);
        setComponentAlignment(forcedTimeDateField, Alignment.MIDDLE_LEFT);
    }

    public DateTimeField getForcedTimeDateField() {
        return forcedTimeDateField;
    }
}
