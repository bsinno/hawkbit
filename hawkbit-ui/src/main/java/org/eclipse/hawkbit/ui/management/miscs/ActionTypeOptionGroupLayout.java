/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.miscs;

import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.RadioButtonGroup;

/**
 * Action type option group layout.
 */
public class ActionTypeOptionGroupLayout extends HorizontalLayout {

    private static final long serialVersionUID = 1L;

    private static final String STYLE_DIST_WINDOW_ACTIONTYPE = "dist-window-actiontype";

    private final VaadinMessageSource i18n;

    private RadioButtonGroup<ActionTypeOption> actionTypeOptionGroup;

    private DateField forcedTimeDateField;

    /**
     * Constructor
     * 
     * @param i18n
     *            VaadinMessageSource
     */
    public ActionTypeOptionGroupLayout(final VaadinMessageSource i18n) {
        this.i18n = i18n;

        createOptionGroup();
        addValueChangeListener();
        setStyleName("dist-window-actiontype-horz-layout");
        setSizeUndefined();
    }

    private void addValueChangeListener() {
        actionTypeOptionGroup.addValueChangeListener(event -> {
            if (event.getValue().equals(ActionTypeOption.AUTO_FORCED)) {
                forcedTimeDateField.setEnabled(true);
                forcedTimeDateField.setRequired(true);
                forcedTimeDateField.setRequiredIndicatorVisible(true);
            } else {
                forcedTimeDateField.setEnabled(false);
                forcedTimeDateField.setRequired(false);
                forcedTimeDateField.setRequiredIndicatorVisible(false);
            }
        });
    }

    private void createOptionGroup() {
        actionTypeOptionGroup = new RadioButtonGroup<>();
        actionTypeOptionGroup.setItems(ActionTypeOption.SOFT, ActionTypeOption.FORCED, ActionTypeOption.AUTO_FORCED);
        selectDefaultOption();

        // final FlexibleOptionGroupItemComponent forceItem =
        // actionTypeOptionGroup
        // .getItemComponent(ActionTypeOption.FORCED);
        // forceItem.setStyleName(STYLE_DIST_WINDOW_ACTIONTYPE);
        // forceItem.setId(UIComponentIdProvider.SAVE_ACTION_RADIO_FORCED);
        // addComponent(forceItem);
        // final Label forceLabel = new Label();
        // forceLabel.setStyleName("statusIconPending");
        // forceLabel.setIcon(VaadinIcons.BOLT);
        // forceLabel.setCaption(i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_FORCED));
        // forceLabel.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_FORCED_ITEM));
        // forceLabel.setStyleName("padding-right-style");
        // addComponent(forceLabel);
        //
        // final FlexibleOptionGroupItemComponent softItem =
        // actionTypeOptionGroup.getItemComponent(ActionTypeOption.SOFT);
        // softItem.setId(UIComponentIdProvider.ACTION_DETAILS_SOFT_ID);
        // softItem.setStyleName(STYLE_DIST_WINDOW_ACTIONTYPE);
        // addComponent(softItem);
        // final Label softLabel = new Label();
        // softLabel.setSizeFull();
        // softLabel.setCaption(i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_SOFT));
        // softLabel.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_SOFT_ITEM));
        // softLabel.setStyleName("padding-right-style");
        // addComponent(softLabel);
        //
        // final FlexibleOptionGroupItemComponent autoForceItem =
        // actionTypeOptionGroup
        // .getItemComponent(ActionTypeOption.AUTO_FORCED);
        // autoForceItem.setStyleName(STYLE_DIST_WINDOW_ACTIONTYPE);
        // autoForceItem.setId(UIComponentIdProvider.ACTION_TYPE_OPTION_GROUP_SAVE_TIMEFORCED);
        // addComponent(autoForceItem);
        // final Label autoForceLabel = new Label();
        // autoForceLabel.setStyleName("statusIconPending");
        // autoForceLabel.setIcon(VaadinIcons.HISTORY);
        // autoForceLabel.setCaption(i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_TIME_FORCED));
        // autoForceLabel.setDescription(i18n.getMessage(UIMessageIdProvider.TOOLTIP_TIMEFORCED_ITEM));
        // autoForceLabel.setStyleName(STYLE_DIST_WINDOW_ACTIONTYPE);
        // addComponent(autoForceLabel);
        //
        // forcedTimeDateField = new DateField();
        // forcedTimeDateField.setInvalidAllowed(false);
        // forcedTimeDateField.setInvalidCommitted(false);
        // forcedTimeDateField.setEnabled(false);
        // forcedTimeDateField.setStyleName("dist-window-forcedtime");
        //
        // final TimeZone tz = SPDateTimeUtil.getBrowserTimeZone();
        // forcedTimeDateField.setValue(
        // Date.from(LocalDateTime.now().plusWeeks(2).atZone(SPDateTimeUtil.getTimeZoneId(tz)).toInstant()));
        // forcedTimeDateField.setImmediate(true);
        // forcedTimeDateField.setTimeZone(tz);
        // forcedTimeDateField.setLocale(HawkbitCommonUtil.getLocale());
        // forcedTimeDateField.setResolution(Resolution.MINUTE);
        // forcedTimeDateField.addStyleName(ValoTheme.DATEFIELD_SMALL);
        // addComponent(forcedTimeDateField);
    }

    /**
     * To Set Default option for save.
     */

    public void selectDefaultOption() {
        actionTypeOptionGroup.setSelectedItem(ActionTypeOption.FORCED);
    }

    /**
     * Enum which described the options for the action type
     *
     */
    public enum ActionTypeOption {
        FORCED(ActionType.FORCED), SOFT(ActionType.SOFT), AUTO_FORCED(ActionType.TIMEFORCED);

        private final ActionType actionType;

        ActionTypeOption(final ActionType actionType) {
            this.actionType = actionType;
        }

        public ActionType getActionType() {
            return actionType;
        }
    }

    public RadioButtonGroup<ActionTypeOption> getActionTypeOptionGroup() {
        return actionTypeOptionGroup;
    }

    public DateField getForcedTimeDateField() {
        return forcedTimeDateField;
    }

}
