/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration;

import static org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey.ACTION_CLEANUP_ACTION_EXPIRY;
import static org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey.ACTION_CLEANUP_ACTION_STATUS;
import static org.eclipse.hawkbit.ui.tenantconfiguration.repository.ActionAutocleanupConfigurationItem.ACTION_STATUS_OPTIONS;
import static org.eclipse.hawkbit.ui.tenantconfiguration.repository.ActionAutocleanupConfigurationItem.EMPTY_STATUS_SET;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.eclipse.hawkbit.ControllerPollProperties;
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.TenantConfigurationValue;
import org.eclipse.hawkbit.security.SecurityTokenGenerator;
import org.eclipse.hawkbit.tenancy.configuration.DurationHelper;
import org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey;
import org.eclipse.hawkbit.ui.AbstractHawkbitUI;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.tenantconfiguration.ConfigurationItem.ConfigurationItemChangeListener;
import org.eclipse.hawkbit.ui.tenantconfiguration.authentication.AnonymousDownloadAuthenticationConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.authentication.CertificateAuthenticationConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.authentication.GatewaySecurityTokenAuthenticationConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.authentication.TargetSecurityTokenAuthenticationConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.repository.ActionAutocleanupConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.repository.ActionAutocleanupConfigurationItem.ActionStatusOption;
import org.eclipse.hawkbit.ui.tenantconfiguration.repository.ActionAutocloseConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.repository.MultiAssignmentsConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.rollout.ApprovalConfigurationItem;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.vaadin.data.Binder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Main UI for the system configuration view.
 */
@ViewScope
@SpringView(name = TenantConfigurationDashboardView.VIEW_NAME, ui = AbstractHawkbitUI.class)
public class TenantConfigurationDashboardView extends CustomComponent implements View, ConfigurationItemChangeListener {

    public static final String VIEW_NAME = "spSystemConfig";
    private static final long serialVersionUID = 1L;
    private final DefaultDistributionSetTypeLayout defaultDistributionSetTypeLayout;

    private final RepositoryConfigurationView repositoryConfigurationView;

    private final AuthenticationConfigurationView authenticationConfigurationView;

    private final PollingConfigurationView pollingConfigurationView;

    private final RolloutConfigurationView rolloutConfigurationView;

    private final VaadinMessageSource i18n;

    private final UiProperties uiProperties;

    private final UINotification uINotification;
    private final CertificateAuthenticationConfigurationItem certificateAuthenticationConfigurationItem;
    private final GatewaySecurityTokenAuthenticationConfigurationItem gatewaySecurityTokenAuthenticationConfigurationItem;
    private final AnonymousDownloadAuthenticationConfigurationItem anonymousDownloadAuthenticationConfigurationItem;

    private Button saveConfigurationBtn;
    private Button undoConfigurationBtn;
    private final SystemManagement systemManagement;
    private final ApprovalConfigurationItem approvalConfigurationItem;
    private final ActionAutocloseConfigurationItem actionAutocloseConfigurationItem;
    private final MultiAssignmentsConfigurationItem multiAssignmentsConfigurationItem;
    private final ActionAutocleanupConfigurationItem actionAutocleanupConfigurationItem;
    private final TargetSecurityTokenAuthenticationConfigurationItem targetSecurityTokenAuthenticationConfigurationItem;
    private final TenantConfigurationManagement tenantConfigurationManagement;
    private final Binder<ProxySystemConfigWindow> binder;
    private final List<ConfigurationGroup> configurationViews = Lists.newArrayListWithExpectedSize(3);
    private final List<CustomComponent> customComponents = Lists.newArrayListWithExpectedSize(3);
    private final SecurityTokenGenerator securityTokenGenerator;
    @Autowired(required = false)
    private Collection<ConfigurationGroup> customConfigurationViews;

    @Autowired
    TenantConfigurationDashboardView(final VaadinMessageSource i18n, final UiProperties uiProperties,
            final UINotification uINotification, final SystemManagement systemManagement,
            final DistributionSetTypeManagement distributionSetTypeManagement,
            final TenantConfigurationManagement tenantConfigurationManagement,
            final SecurityTokenGenerator securityTokenGenerator,
            final ControllerPollProperties controllerPollProperties, final SpPermissionChecker permChecker) {
        this.systemManagement = systemManagement;
        this.tenantConfigurationManagement = tenantConfigurationManagement;
        this.securityTokenGenerator = securityTokenGenerator;
        this.binder = new Binder<>();
        binder.setBean(populateAndGetSystemConfig());
        this.targetSecurityTokenAuthenticationConfigurationItem = new TargetSecurityTokenAuthenticationConfigurationItem(
                tenantConfigurationManagement, i18n);
        this.actionAutocloseConfigurationItem = new ActionAutocloseConfigurationItem(tenantConfigurationManagement,
                i18n);
        this.multiAssignmentsConfigurationItem = new MultiAssignmentsConfigurationItem(tenantConfigurationManagement,
                i18n, binder);
        this.actionAutocleanupConfigurationItem = new ActionAutocleanupConfigurationItem(tenantConfigurationManagement,
                binder, i18n);
        this.approvalConfigurationItem = new ApprovalConfigurationItem(tenantConfigurationManagement, i18n);
        this.certificateAuthenticationConfigurationItem = new CertificateAuthenticationConfigurationItem(
                tenantConfigurationManagement, i18n, binder);

        this.gatewaySecurityTokenAuthenticationConfigurationItem = new GatewaySecurityTokenAuthenticationConfigurationItem(
                tenantConfigurationManagement, i18n, securityTokenGenerator, binder);
        this.anonymousDownloadAuthenticationConfigurationItem = new AnonymousDownloadAuthenticationConfigurationItem(
                tenantConfigurationManagement, i18n);

        this.defaultDistributionSetTypeLayout = new DefaultDistributionSetTypeLayout(systemManagement, i18n,
                permChecker, binder, distributionSetTypeManagement);

        this.authenticationConfigurationView = new AuthenticationConfigurationView(i18n,
                targetSecurityTokenAuthenticationConfigurationItem, certificateAuthenticationConfigurationItem,
                gatewaySecurityTokenAuthenticationConfigurationItem, anonymousDownloadAuthenticationConfigurationItem,
                uiProperties, binder);
        this.pollingConfigurationView = new PollingConfigurationView(i18n, controllerPollProperties,
                tenantConfigurationManagement, binder);
        this.repositoryConfigurationView = new RepositoryConfigurationView(i18n, uiProperties,
                actionAutocloseConfigurationItem, actionAutocleanupConfigurationItem, multiAssignmentsConfigurationItem,
                binder);
        this.rolloutConfigurationView = new RolloutConfigurationView(i18n, approvalConfigurationItem, uiProperties,
                binder);

        this.i18n = i18n;
        this.uiProperties = uiProperties;
        this.uINotification = uINotification;
    }

    /**
     * Init method adds all Configuration Views to the list of Views.
     */
    @PostConstruct
    public void init() {
        if (defaultDistributionSetTypeLayout.getComponentCount() > 0) {
            configurationViews.add(defaultDistributionSetTypeLayout);
        }
        configurationViews.add(repositoryConfigurationView);
        customComponents.add(rolloutConfigurationView);
        configurationViews.add(authenticationConfigurationView);
        customComponents.add(pollingConfigurationView);
        if (customConfigurationViews != null) {
            configurationViews.addAll(
                    customConfigurationViews.stream().filter(ConfigurationGroup::show).collect(Collectors.toList()));
        }

        final Panel rootPanel = new Panel();
        rootPanel.setStyleName("tenantconfig-root");

        final VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSizeFull();
        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);
        customComponents.forEach(rootLayout::addComponent);
        configurationViews.forEach(rootLayout::addComponent);

        final HorizontalLayout buttonContent = saveConfigurationButtonsLayout();
        rootLayout.addComponent(buttonContent);
        rootLayout.setComponentAlignment(buttonContent, Alignment.BOTTOM_LEFT);
        rootPanel.setContent(rootLayout);
        setCompositionRoot(rootPanel);

        configurationViews.forEach(view -> view.addChangeListener(this));
        binder.addStatusChangeListener(event -> {
            saveConfigurationBtn.setEnabled(event.getBinder().isValid());
            undoConfigurationBtn.setEnabled(event.getBinder().isValid());
        });
    }

    private ProxySystemConfigWindow populateAndGetSystemConfig() {
        final ProxySystemConfigWindow configBean = new ProxySystemConfigWindow();

        configBean.setDistributionSetTypeId(systemManagement.getTenantMetadata().getDefaultDsType().getId());
        configBean.setRolloutApproval(readConfigOption(TenantConfigurationKey.ROLLOUT_APPROVAL_ENABLED));
        configBean.setActionAutoclose(readConfigOption(TenantConfigurationKey.REPOSITORY_ACTIONS_AUTOCLOSE_ENABLED));
        configBean.setMultiAssignments(readConfigOption(TenantConfigurationKey.MULTI_ASSIGNMENTS_ENABLED));
        configBean.setActionAutocleanup(readConfigOption(TenantConfigurationKey.ACTION_CLEANUP_ENABLED));
        configBean.setCertificateAuth(readConfigOption(TenantConfigurationKey.AUTHENTICATION_MODE_HEADER_ENABLED));
        configBean.setTargetSecToken(
                readConfigOption(TenantConfigurationKey.AUTHENTICATION_MODE_TARGET_SECURITY_TOKEN_ENABLED));
        configBean.setGatewaySecToken(
                readConfigOption(TenantConfigurationKey.AUTHENTICATION_MODE_GATEWAY_SECURITY_TOKEN_ENABLED));
        configBean.setDownloadAnonymous(readConfigOption(TenantConfigurationKey.ANONYMOUS_DOWNLOAD_MODE_ENABLED));
        String securityToken = tenantConfigurationManagement.getConfigurationValue(
                TenantConfigurationKey.AUTHENTICATION_MODE_GATEWAY_SECURITY_TOKEN_KEY, String.class).getValue();
        if (StringUtils.isEmpty(securityToken)) {
            securityToken = this.securityTokenGenerator.generateToken();
        }
        configBean.setGatewaySecurityToken(securityToken);
        configBean.setCaRootAuthority(getCaRootAuthorityValue());
        configBean.setActionCleanupStatus(getActionStatusOption());
        configBean.setActionExpiryDays(String.valueOf(getActionExpiry()));

        final TenantConfigurationValue<String> pollingTimeConfValue = tenantConfigurationManagement.getConfigurationValue(
                TenantConfigurationKey.POLLING_TIME_INTERVAL, String.class);
        configBean.setPollingTime(!pollingTimeConfValue.isGlobal());
        configBean.setPollingTimeDuration(
                DurationHelper.formattedStringToDuration(pollingTimeConfValue.getValue()));

        final TenantConfigurationValue<String> overdueTimeConfValue = tenantConfigurationManagement.getConfigurationValue(
                TenantConfigurationKey.POLLING_OVERDUE_TIME_INTERVAL, String.class);
        configBean.setPollingOverdue(!overdueTimeConfValue.isGlobal());
        configBean.setPollingOverdueDuration(
                DurationHelper.formattedStringToDuration(overdueTimeConfValue.getValue()));

        return configBean;
    }

    private String getCaRootAuthorityValue() {
        return tenantConfigurationManagement.getConfigurationValue(
                TenantConfigurationKey.AUTHENTICATION_MODE_HEADER_AUTHORITY_NAME, String.class).getValue();
    }

    private long getActionExpiry() {
        return TimeUnit.MILLISECONDS.toDays(readConfigValue(ACTION_CLEANUP_ACTION_EXPIRY, Long.class).getValue());
    }

    private ActionStatusOption getActionStatusOption() {
        final Set<Action.Status> actionStatus = getActionStatus();
        return ACTION_STATUS_OPTIONS.stream()
                .filter(option -> actionStatus.equals(option.getStatus()))
                .findFirst()
                .orElse(ACTION_STATUS_OPTIONS.iterator().next());
    }

    private EnumSet<Action.Status> getActionStatus() {
        final TenantConfigurationValue<String> statusStr = readConfigValue(ACTION_CLEANUP_ACTION_STATUS, String.class);
        if (statusStr != null) {
            return Arrays.stream(statusStr.getValue().split("[;,]"))
                    .map(Action.Status::valueOf)
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(Action.Status.class)));
        }
        return EMPTY_STATUS_SET;
    }

    private <T extends Serializable> TenantConfigurationValue<T> readConfigValue(final String key,
            final Class<T> valueType) {
        return tenantConfigurationManagement.getConfigurationValue(key, valueType);
    }

    private HorizontalLayout saveConfigurationButtonsLayout() {
        final HorizontalLayout hlayout = new HorizontalLayout();
        hlayout.setSpacing(true);
        saveConfigurationBtn = SPUIComponentProvider.getButton(UIComponentIdProvider.SYSTEM_CONFIGURATION_SAVE, "", "",
                "", true, VaadinIcons.HARDDRIVE, SPUIButtonStyleNoBorder.class);
        saveConfigurationBtn.setEnabled(false);
        saveConfigurationBtn.setDescription(i18n.getMessage("configuration.savebutton.tooltip"));
        saveConfigurationBtn.addClickListener(event -> saveConfiguration());
        hlayout.addComponent(saveConfigurationBtn);

        undoConfigurationBtn = SPUIComponentProvider.getButton(UIComponentIdProvider.SYSTEM_CONFIGURATION_CANCEL, "",
                "", "", true, VaadinIcons.ARROW_BACKWARD, SPUIButtonStyleNoBorder.class);
        undoConfigurationBtn.setEnabled(false);
        undoConfigurationBtn.setDescription(i18n.getMessage("configuration.cancellbutton.tooltip"));
        undoConfigurationBtn.addClickListener(event -> undoConfiguration());
        hlayout.addComponent(undoConfigurationBtn);

        final Link linkToSystemConfigHelp = SPUIComponentProvider.getHelpLink(i18n,
                uiProperties.getLinks().getDocumentation().getSystemConfigurationView());
        hlayout.addComponent(linkToSystemConfigHelp);

        return hlayout;
    }

    private void saveSystemConfigBean() {
        final ProxySystemConfigWindow configWindowBean = binder.getBean();
        systemManagement.updateTenantMetadata(configWindowBean.getDistributionSetTypeId());
        writeConfigOption(TenantConfigurationKey.ROLLOUT_APPROVAL_ENABLED, configWindowBean.isRolloutApproval());
        writeConfigOption(TenantConfigurationKey.ACTION_CLEANUP_ENABLED, configWindowBean.isActionAutocleanup());
        writeConfigOption(TenantConfigurationKey.AUTHENTICATION_MODE_TARGET_SECURITY_TOKEN_ENABLED,
                configWindowBean.isTargetSecToken());
        writeConfigOption(TenantConfigurationKey.AUTHENTICATION_MODE_GATEWAY_SECURITY_TOKEN_ENABLED,
                configWindowBean.isGatewaySecToken());
        writeConfigOption(TenantConfigurationKey.ANONYMOUS_DOWNLOAD_MODE_ENABLED,
                configWindowBean.isDownloadAnonymous());

        if (configWindowBean.isGatewaySecToken()) {
            writeConfigOption(TenantConfigurationKey.AUTHENTICATION_MODE_GATEWAY_SECURITY_TOKEN_KEY,
                    configWindowBean.getGatewaySecurityToken());
        }

        if (configWindowBean.isActionAutocleanup()) {
            writeConfigOption(ACTION_CLEANUP_ACTION_STATUS, configWindowBean.getActionCleanupStatus()
                    .getStatus()
                    .stream()
                    .map(Action.Status::name)
                    .collect(Collectors.joining(",")));

            writeConfigOption(TenantConfigurationKey.ACTION_CLEANUP_ACTION_EXPIRY,
                    TimeUnit.DAYS.toMillis(Long.parseLong(configWindowBean.getActionExpiryDays())));
        }
        if (!readConfigOption(TenantConfigurationKey.MULTI_ASSIGNMENTS_ENABLED)) {
            writeConfigOption(TenantConfigurationKey.REPOSITORY_ACTIONS_AUTOCLOSE_ENABLED,
                    configWindowBean.isActionAutoclose());
        }

        if (configWindowBean.isMultiAssignments() && !readConfigOption(
                TenantConfigurationKey.MULTI_ASSIGNMENTS_ENABLED)) {
            writeConfigOption(TenantConfigurationKey.MULTI_ASSIGNMENTS_ENABLED, configWindowBean.isMultiAssignments());
            repositoryConfigurationView.disableMultipleAssignmentOption();
        }
        writeConfigOption(TenantConfigurationKey.AUTHENTICATION_MODE_HEADER_ENABLED,
                configWindowBean.isCertificateAuth());
        if (configWindowBean.isCertificateAuth()) {
            final String value =
                    configWindowBean.getCaRootAuthority() != null ? configWindowBean.getCaRootAuthority() : "";
            writeConfigOption(TenantConfigurationKey.AUTHENTICATION_MODE_HEADER_AUTHORITY_NAME, value);
        }
        populateAndGetSystemConfig();
    }

    private boolean readConfigOption(String configurationKey) {
        final TenantConfigurationValue<Boolean> enabled = tenantConfigurationManagement.getConfigurationValue(
                configurationKey, Boolean.class);

        return enabled.getValue() && !enabled.isGlobal();
    }

    private <T extends Serializable> void writeConfigOption(String key, T value) {
        tenantConfigurationManagement.addOrUpdateConfiguration(key, value);
    }

    private void saveConfiguration() {
        final boolean isUserInputValid = configurationViews.stream().allMatch(ConfigurationGroup::isUserInputValid);

        if (!isUserInputValid) {
            uINotification.displayValidationError(i18n.getMessage("notification.configuration.save.notpossible"));
            return;
        }
        saveSystemConfigBean();
        configurationViews.forEach(ConfigurationGroup::save);
        // More methods
        saveConfigurationBtn.setEnabled(false);
        undoConfigurationBtn.setEnabled(false);
        uINotification.displaySuccess(i18n.getMessage("notification.configuration.save.successful"));
    }

    private void undoConfiguration() {
        binder.setBean(populateAndGetSystemConfig());
        // More methods
        saveConfigurationBtn.setEnabled(false);
        undoConfigurationBtn.setEnabled(false);
    }

    @Override
    public void configurationHasChanged() {
        saveConfigurationBtn.setEnabled(true);
        undoConfigurationBtn.setEnabled(true);
    }

    @Override
    public void enter(final ViewChangeEvent event) {
        // This view is constructed in the init() method()
    }

}
