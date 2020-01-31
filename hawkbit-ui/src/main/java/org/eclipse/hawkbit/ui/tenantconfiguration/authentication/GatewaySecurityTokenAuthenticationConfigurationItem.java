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
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This class represents the UI item for the gateway security token section in
 * the authentication configuration view.
 */
public class GatewaySecurityTokenAuthenticationConfigurationItem extends AbstractBooleanTenantConfigurationItem {

    private static final long serialVersionUID = 1L;

    private final transient SecurityTokenGenerator securityTokenGenerator;
    private final TextField gatewayTokenField;
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

        detailLayout = new VerticalLayout();
        detailLayout.setMargin(false);
        detailLayout.setSpacing(false);

        final Button gatewaytokenBtn = SPUIComponentProvider.getButton(null,
                i18n.getMessage("configuration.button.regenerateKey"), "", ValoTheme.BUTTON_TINY + " " + "redicon",
                true, null, SPUIButtonStyleSmall.class);

        gatewaytokenBtn.setIcon(VaadinIcons.REFRESH);
        gatewaytokenBtn.addClickListener(event -> refreshGatewayToken());

        gatewayTokenField = new TextFieldBuilder(TenantConfiguration.VALUE_MAX_SIZE).buildTextComponent();
        gatewayTokenField.setWidth(300, Unit.PIXELS);
        gatewayTokenField.setId("gatewaysecuritytokenkey");
        gatewayTokenField.setReadOnly(true);
        binder.bind(gatewayTokenField, ProxySystemConfigWindow::getGatewaySecurityToken,
                ProxySystemConfigWindow::setGatewaySecurityToken);
        final HorizontalLayout keyGenerationLayout = new HorizontalLayout();
        keyGenerationLayout.setSpacing(true);
        keyGenerationLayout.addComponent(gatewayTokenField);
        keyGenerationLayout.addComponent(gatewaytokenBtn);
        detailLayout.addComponent(keyGenerationLayout);
        if (binder.getBean().isGatewaySecToken()) {
            setDetailVisible(true);
        }
    }

    public void setDetailVisible(final boolean visible) {
        if (visible) {
            addComponent(detailLayout);
        } else {
            removeComponent(detailLayout);
        }
    }

    private void refreshGatewayToken() {
        binder.getBean().setGatewaySecurityToken(securityTokenGenerator.generateToken());
        binder.readBean(binder.getBean());
        UI.getCurrent().push();
    }

    @Override
    public void configEnable() {

        setDetailVisible(true);
        String gatewayTokenKey = binder.getBean().getGatewaySecurityToken();
        if (StringUtils.isEmpty(gatewayTokenKey)) {
            gatewayTokenKey = securityTokenGenerator.generateToken();
        }
        //        refreshGatewayToken();
        gatewayTokenField.setValue(gatewayTokenKey);
    }

    @Override
    public void configDisable() {
        setDetailVisible(false);
    }

    @Override
    public void save() {
    }

    @Override
    public void undo() {
    }

}
