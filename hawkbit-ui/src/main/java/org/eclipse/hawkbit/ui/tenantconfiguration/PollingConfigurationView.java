/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

import org.eclipse.hawkbit.ControllerPollProperties;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.TenantConfigurationValue;
import org.eclipse.hawkbit.tenancy.configuration.DurationHelper;
import org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.tenantconfiguration.polling.DurationConfigField;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.data.Result;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.validator.DateRangeValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * View to configure the polling interval and the overdue time.
 */
public class PollingConfigurationView extends CustomComponent
        implements ConfigurationItem.ConfigurationItemChangeListener {

    private static final long serialVersionUID = 1L;

    private static final ZoneId ZONEID_UTC = ZoneId.of("+0");

    private final transient TenantConfigurationManagement tenantConfigurationManagement;

    private final DurationConfigField fieldPollTime;
    private final DurationConfigField fieldPollingOverdueTime;

    private transient Duration tenantPollTime;
    private transient Duration tenantOverdueTime;

    private final Binder<ProxySystemConfigWindow> binder;

    PollingConfigurationView(final VaadinMessageSource i18n, final ControllerPollProperties controllerPollProperties,
            final TenantConfigurationManagement tenantConfigurationManagement, Binder<ProxySystemConfigWindow> binder) {
        this.tenantConfigurationManagement = tenantConfigurationManagement;
        this.binder = binder;

        final Duration minDuration = DurationHelper.formattedStringToDuration(
                controllerPollProperties.getMinPollingTime());
        final Duration maxDuration = DurationHelper.formattedStringToDuration(
                controllerPollProperties.getMaxPollingTime());
        final Duration globalPollTime = DurationHelper.formattedStringToDuration(
                tenantConfigurationManagement.getGlobalConfigurationValue(TenantConfigurationKey.POLLING_TIME_INTERVAL,
                        String.class));
        final Duration globalOverdueTime = DurationHelper.formattedStringToDuration(
                tenantConfigurationManagement.getGlobalConfigurationValue(
                        TenantConfigurationKey.POLLING_OVERDUE_TIME_INTERVAL, String.class));

        final TenantConfigurationValue<String> pollTimeConfValue = tenantConfigurationManagement.getConfigurationValue(
                TenantConfigurationKey.POLLING_TIME_INTERVAL, String.class);
        if (!pollTimeConfValue.isGlobal()) {
            tenantPollTime = DurationHelper.formattedStringToDuration(pollTimeConfValue.getValue());
        }

        final TenantConfigurationValue<String> overdueTimeConfValue = tenantConfigurationManagement.getConfigurationValue(
                TenantConfigurationKey.POLLING_OVERDUE_TIME_INTERVAL, String.class);
        if (!overdueTimeConfValue.isGlobal()) {
            tenantOverdueTime = DurationHelper.formattedStringToDuration(overdueTimeConfValue.getValue());
        }

        final Panel rootPanel = new Panel();
        rootPanel.setSizeFull();
        rootPanel.addStyleName("config-panel");

        final VerticalLayout vLayout = new VerticalLayout();
        vLayout.setSpacing(false);
        vLayout.setMargin(true);

        final Label headerDisSetType = new Label(i18n.getMessage("configuration.polling.title"));
        headerDisSetType.addStyleName("config-panel-header");
        vLayout.addComponent(headerDisSetType);

        fieldPollTime = DurationConfigField.builder(UIComponentIdProvider.SYSTEM_CONFIGURATION_POLLING)
                .caption(i18n.getMessage("configuration.polling.time"))
                .checkBoxTooltip(i18n.getMessage("configuration.polling.custom.value"))
                .range(minDuration, maxDuration)
                .globalDuration(globalPollTime)
                .tenantDuration(tenantPollTime)
                .build();

        binder.forField(fieldPollTime.getCheckBox())
                .bind(ProxySystemConfigWindow::isPollingTime, ProxySystemConfigWindow::setPollingTime);

        binder.forField(fieldPollTime.getDurationField())
                //TODO: use i18n
                .asRequired("should not be empty")
                .withConverter(PollingConfigurationView::localDateTimeToDuration,
                        PollingConfigurationView::durationToLocalDateTime)
                .bind(ProxySystemConfigWindow::getPollingTimeDuration, ProxySystemConfigWindow::setPollingTimeDuration);

        vLayout.addComponent(fieldPollTime);

        fieldPollingOverdueTime = DurationConfigField.builder(UIComponentIdProvider.SYSTEM_CONFIGURATION_OVERDUE)
                .caption(i18n.getMessage("configuration.polling.overduetime"))
                .checkBoxTooltip(i18n.getMessage("configuration.polling.custom.value"))
                .range(minDuration, maxDuration)
                .globalDuration(globalOverdueTime)
                .tenantDuration(tenantOverdueTime)
                .build();
        binder.forField(fieldPollingOverdueTime.getCheckBox())
                .bind(ProxySystemConfigWindow::isPollingOverdue, ProxySystemConfigWindow::setPollingOverdue);

        binder.forField(fieldPollingOverdueTime.getDurationField())
                //TODO: use i18n
                .asRequired("should not be empty")
                .withConverter(PollingConfigurationView::localDateTimeToDuration,
                        PollingConfigurationView::durationToLocalDateTime)
                .bind(ProxySystemConfigWindow::getPollingOverdueDuration,
                        ProxySystemConfigWindow::setPollingOverdueDuration);
        //        fieldPollingOverdueTime.addChangeListener(this);
        vLayout.addComponent(fieldPollingOverdueTime);

        rootPanel.setContent(vLayout);
        setCompositionRoot(rootPanel);
    }

    private static Duration localDateTimeToDuration(final LocalDateTime date) {
        final LocalTime endExclusive = LocalDateTime.ofInstant(date.toInstant(ZoneOffset.UTC), ZONEID_UTC)
                .toLocalTime();
        return Duration.between(LocalTime.MIDNIGHT, LocalTime.from(endExclusive));
    }

    private static LocalDateTime durationToLocalDateTime(final Duration duration) {
        final LocalTime lt = LocalTime.ofNanoOfDay(duration.toNanos());
        final Date date = Date.from(lt.atDate(LocalDate.now(ZONEID_UTC)).atZone(ZONEID_UTC).toInstant());
        return LocalDateTime.ofInstant(date.toInstant(), ZONEID_UTC);
    }

    //    @Override
    public void save() {
        // make sure values are only saved, when the value has been changed
        //
        //        if (!compareDurations(tenantPollTime, fieldPollTime.getValue())) {
        //            tenantPollTime = fieldPollTime.getValue();
        //            saveDurationConfigurationValue(TenantConfigurationKey.POLLING_TIME_INTERVAL, tenantPollTime);
        //        }
        //
        //        if (!compareDurations(tenantOverdueTime, fieldPollingOverdueTime.getValue())) {
        //            tenantOverdueTime = fieldPollingOverdueTime.getValue();
        //            saveDurationConfigurationValue(TenantConfigurationKey.POLLING_OVERDUE_TIME_INTERVAL, tenantOverdueTime);
        //        }
    }

    @Override
    public void configurationHasChanged() {

    }

    private void saveDurationConfigurationValue(final String key, final Duration duration) {
        if (duration == null) {
            tenantConfigurationManagement.deleteConfiguration(key);
        } else {
            tenantConfigurationManagement.addOrUpdateConfiguration(key,
                    DurationHelper.durationToFormattedString(duration));
        }
    }

    //    @Override
    //    public void undo() {
    //        //        fieldPollTime.setValue(tenantPollTime);
    //        //        fieldPollingOverdueTime.setValue(tenantOverdueTime);
    //    }

    //    @Override
    //    public boolean isUserInputValid() {
    //        return fieldPollTime.isUserInputValid() && fieldPollingOverdueTime.isUserInputValid();
    //    }
    //
    //    @Override
    //    public void configurationHasChanged() {
    //        notifyConfigurationChanged();
    //    }
    //
    //    private static boolean compareDurations(final Duration d1, final Duration d2) {
    //        if (d1 == null && d2 == null) {
    //            return true;
    //        }
    //
    //        if (d1 != null) {
    //            return d1.equals(d2);
    //        }
    //
    //        // d1 == null, d2 != null
    //        return false;
    //    }
}
