/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.miscs;

import org.eclipse.hawkbit.repository.MaintenanceScheduleHelper;
import org.eclipse.hawkbit.repository.exception.InvalidMaintenanceScheduleException;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.builder.FormComponentBuilder;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyAssignmentWindow;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationResult;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Builder for Assignment window components.
 */
public class AssignmentWindowLayoutComponentBuilder {

    private final VaadinMessageSource i18n;

    public AssignmentWindowLayoutComponentBuilder(final VaadinMessageSource i18n) {
        this.i18n = i18n;
    }

    /**
     * create bound {@link ActionTypeOptionGroupAssignmentLayout}
     * 
     * @param binder
     *            binder the input will be bound to
     * @return input component
     */
    public ActionTypeOptionGroupAssignmentLayout createActionTypeOptionGroupLayout(
            final Binder<ProxyAssignmentWindow> binder) {
        ActionTypeOptionGroupAssignmentLayout layout = FormComponentBuilder.createActionTypeOptionGroupLayout(binder, i18n,
                        UIComponentIdProvider.DEPLOYMENT_ASSIGNMENT_ACTION_TYPE_OPTIONS_ID);
        layout.addStyleName("margin-small");
        return layout;
    }

    public CheckBox createEnableMaintenanceWindowToggle(final Binder<ProxyAssignmentWindow> binder) {
        final CheckBox maintenanceWindowToggle = new CheckBox(i18n.getMessage("caption.maintenancewindow.enabled"),
                false);
        maintenanceWindowToggle.setId(UIComponentIdProvider.MAINTENANCE_WINDOW_ENABLED_ID);
        maintenanceWindowToggle.addStyleName(ValoTheme.CHECKBOX_SMALL);
        // TODO: check if it is needed
        maintenanceWindowToggle.addStyleName("dist-window-maintenance-window-enable");

        binder.forField(maintenanceWindowToggle).bind(ProxyAssignmentWindow::isMaintenanceWindowEnabled,
                ProxyAssignmentWindow::setMaintenanceWindowEnabled);

        return maintenanceWindowToggle;
    }

    public TextField createMaintenanceSchedule(final Binder<ProxyAssignmentWindow> binder) {
        final TextField maintenanceSchedule = createTextField("0 0 3 ? * 6",
                UIComponentIdProvider.MAINTENANCE_WINDOW_SCHEDULE_ID, Action.MAINTENANCE_WINDOW_SCHEDULE_LENGTH);
        maintenanceSchedule.setCaption(i18n.getMessage("caption.maintenancewindow.schedule"));

        // TODO: use i18n for all the required fields messages
        binder.forField(maintenanceSchedule).asRequired("You must provide the valid cron expression")
                .withValidator((cronSchedule, context) -> {
                    try {
                        MaintenanceScheduleHelper.validateCronSchedule(cronSchedule);
                        return ValidationResult.ok();
                    } catch (final InvalidMaintenanceScheduleException e) {
                        return ValidationResult.error(
                                i18n.getMessage(UIMessageIdProvider.CRON_VALIDATION_ERROR) + ": " + e.getMessage());
                    }
                }).bind(ProxyAssignmentWindow::getMaintenanceSchedule, ProxyAssignmentWindow::setMaintenanceSchedule);

        return maintenanceSchedule;
    }

    // TODO: remove duplication with RolloutWindowLayoutComponentBuilder
    private TextField createTextField(final String prompt, final String id, final int maxLength) {
        return new TextFieldBuilder(maxLength).prompt(prompt).id(id).buildTextComponent();
    }

    public TextField createMaintenanceDuration(final Binder<ProxyAssignmentWindow> binder) {
        final TextField maintenanceDuration = createTextField("hh:mm:ss",
                UIComponentIdProvider.MAINTENANCE_WINDOW_DURATION_ID, Action.MAINTENANCE_WINDOW_DURATION_LENGTH);
        maintenanceDuration.setCaption(i18n.getMessage("caption.maintenancewindow.duration"));

        binder.forField(maintenanceDuration).asRequired("You must provide the valid duration")
                .withValidator((duration, context) -> {
                    try {
                        MaintenanceScheduleHelper.validateDuration(duration);
                        return ValidationResult.ok();
                    } catch (final InvalidMaintenanceScheduleException e) {
                        return ValidationResult
                                .error(i18n.getMessage("message.maintenancewindow.duration.validation.error") + ": "
                                        + e.getDurationErrorIndex());
                    }
                }).bind(ProxyAssignmentWindow::getMaintenanceDuration, ProxyAssignmentWindow::setMaintenanceDuration);

        return maintenanceDuration;
    }

    public ComboBox<String> createMaintenanceTimeZoneCombo(final Binder<ProxyAssignmentWindow> binder) {
        final ComboBox<String> maintenanceTimeZoneCombo = new ComboBox<>();

        maintenanceTimeZoneCombo.setId(UIComponentIdProvider.MAINTENANCE_WINDOW_TIME_ZONE_ID);
        maintenanceTimeZoneCombo.setCaption(i18n.getMessage("caption.maintenancewindow.timezone"));
        maintenanceTimeZoneCombo.addStyleName(ValoTheme.COMBOBOX_SMALL);

        maintenanceTimeZoneCombo.setTextInputAllowed(false);
        maintenanceTimeZoneCombo.setEmptySelectionAllowed(false);

        maintenanceTimeZoneCombo.setItems(SPDateTimeUtil.getAllTimeZoneOffsetIds());

        binder.forField(maintenanceTimeZoneCombo).bind(ProxyAssignmentWindow::getMaintenanceTimeZone,
                ProxyAssignmentWindow::setMaintenanceTimeZone);

        return maintenanceTimeZoneCombo;
    }

    public Label createMaintenanceScheduleTranslator() {
        final Label maintenanceScheduleTranslator = new LabelBuilder()
                .id(UIComponentIdProvider.MAINTENANCE_WINDOW_SCHEDULE_TRANSLATOR_ID)
                .name(i18n.getMessage(UIMessageIdProvider.CRON_VALIDATION_ERROR)).buildLabel();
        maintenanceScheduleTranslator.addStyleName(ValoTheme.LABEL_TINY);

        return maintenanceScheduleTranslator;
    }

    public Link createMaintenanceHelpLink(final UiProperties uiProperties) {
        final String maintenanceWindowHelpUrl = uiProperties.getLinks().getDocumentation().getMaintenanceWindowView();
        final Link maintenanceHelpLink = new Link("", new ExternalResource(maintenanceWindowHelpUrl));

        maintenanceHelpLink.setTargetName("_blank");
        maintenanceHelpLink.setIcon(VaadinIcons.QUESTION_CIRCLE);
        maintenanceHelpLink.setDescription(i18n.getMessage("tooltip.documentation.link"));

        return maintenanceHelpLink;
    }
}
