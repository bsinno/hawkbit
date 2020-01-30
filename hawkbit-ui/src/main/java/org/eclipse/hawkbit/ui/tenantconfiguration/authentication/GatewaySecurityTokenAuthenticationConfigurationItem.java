/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration.authentication;

import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.TenantConfiguration;
import org.eclipse.hawkbit.security.SecurityTokenGenerator;
import org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleSmall;
import org.eclipse.hawkbit.ui.tenantconfiguration.generic.AbstractBooleanTenantConfigurationItem;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;

import com.vaadin.data.Binder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This class represents the UI item for the gateway security token section in
 * the authentication configuration view.
 */
public class GatewaySecurityTokenAuthenticationConfigurationItem extends AbstractBooleanTenantConfigurationItem {

    private static final long serialVersionUID = 1L;

    private final transient SecurityTokenGenerator securityTokenGenerator;

//    private final Label gatewayTokenkeyLabel;

    private final TextField gatewayTokenField;

    private boolean configurationEnabled;
    private boolean configurationEnabledChange;

    private boolean keyChanged;

    private final VerticalLayout detailLayout;
    private final Binder<ProxySystemConfigWindow> binder;

    public GatewaySecurityTokenAuthenticationConfigurationItem(
            final TenantConfigurationManagement tenantConfigurationManagement, final VaadinMessageSource i18n,
            final SecurityTokenGenerator securityTokenGenerator, Binder<ProxySystemConfigWindow> binder) {
        super(TenantConfigurationKey.AUTHENTICATION_MODE_GATEWAY_SECURITY_TOKEN_ENABLED, tenantConfigurationManagement,
                i18n);
        this.securityTokenGenerator = securityTokenGenerator;
        this.binder = binder;

        super.init("label.configuration.auth.gatewaytoken");

        configurationEnabled = isConfigEnabled();

        detailLayout = new VerticalLayout();
        detailLayout.setMargin(false);
        detailLayout.setSpacing(false);

        final Button gatewaytokenBtn = SPUIComponentProvider.getButton(null,
                i18n.getMessage("configuration.button.regenerateKey"), "", ValoTheme.BUTTON_TINY + " " + "redicon",
                true, null, SPUIButtonStyleSmall.class);

        gatewaytokenBtn.setIcon(VaadinIcons.REFRESH);
        gatewaytokenBtn.addClickListener(event -> generateGatewayToken());


//        gatewayTokenkeyLabel = new LabelBuilder().id("gatewaysecuritytokenkey").name("").buildLabel();
//        gatewayTokenkeyLabel.addStyleName("gateway-token-label");
        gatewayTokenField = new TextFieldBuilder(TenantConfiguration.VALUE_MAX_SIZE).buildTextComponent();
        gatewayTokenField.setWidth(300, Unit.PIXELS);
        gatewayTokenField.setId("gatewaysecuritytokenkey");
//        gatewayTokenField.setEnabled(false);
        gatewayTokenField.setReadOnly(true);
        binder.bind(gatewayTokenField, ProxySystemConfigWindow::getGatewaySecurityToken, ProxySystemConfigWindow::setGatewaySecurityToken);
        final HorizontalLayout keyGenerationLayout = new HorizontalLayout();
        keyGenerationLayout.setSpacing(true);

        keyGenerationLayout.addComponent(gatewayTokenField);
        keyGenerationLayout.addComponent(gatewaytokenBtn);

        detailLayout.addComponent(keyGenerationLayout);

        if (binder.getBean().isGatewaySecToken()) {
//            gatewayTokenField.setValue(getSecurityTokenKey());
            setDetailVisible(true);
        }
    }

    private void setDetailVisible(final boolean visible) {
        if (visible) {
            addComponent(detailLayout);
        } else {
            removeComponent(detailLayout);
        }

    }

    private void generateGatewayToken() {
        binder.getBean().setGatewaySecurityToken(securityTokenGenerator.generateToken());
//        gatewayTokenField.setValue(securityTokenGenerator.generateToken());
        keyChanged = true;
        notifyConfigurationChanged();
    }

    @Override
    public void configEnable() {
        if (!configurationEnabled) {
            configurationEnabledChange = true;
        }

        configurationEnabled = true;
        setDetailVisible(true);
        String gatewayTokenKey = getSecurityTokenKey();
        if (StringUtils.isEmpty(gatewayTokenKey)) {
            gatewayTokenKey = securityTokenGenerator.generateToken();
            keyChanged = true;
        }

        binder.getBean().setGatewaySecurityToken(gatewayTokenKey);

//        gatewayTokenField.setValue(gatewayTokenKey);
    }

    private String getSecurityTokenKey() {
        return getTenantConfigurationManagement().getConfigurationValue(
                TenantConfigurationKey.AUTHENTICATION_MODE_GATEWAY_SECURITY_TOKEN_KEY, String.class).getValue();
    }

    @Override
    public void configDisable() {
        if (configurationEnabled) {
            configurationEnabledChange = true;
        }
        configurationEnabled = false;
        setDetailVisible(false);
    }

    @Override
    public void save() {
        if (configurationEnabledChange) {
            getTenantConfigurationManagement().addOrUpdateConfiguration(
                    TenantConfigurationKey.AUTHENTICATION_MODE_GATEWAY_SECURITY_TOKEN_ENABLED, configurationEnabled);
        }

        if (keyChanged) {
            getTenantConfigurationManagement().addOrUpdateConfiguration(
                    TenantConfigurationKey.AUTHENTICATION_MODE_GATEWAY_SECURITY_TOKEN_KEY,
                    gatewayTokenField.getValue());
        }
    }

    @Override
    public void undo() {
//        configurationEnabledChange = false;
//        keyChanged = false;
//        gatewayTokenField.setValue(getSecurityTokenKey());
    }

}
