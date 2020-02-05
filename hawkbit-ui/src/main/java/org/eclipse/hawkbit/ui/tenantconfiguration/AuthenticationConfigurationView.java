/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration;

import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.tenantconfiguration.authentication.AnonymousDownloadAuthenticationConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.authentication.CertificateAuthenticationConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.authentication.GatewaySecurityTokenAuthenticationConfigurationItem;
import org.eclipse.hawkbit.ui.tenantconfiguration.authentication.TargetSecurityTokenAuthenticationConfigurationItem;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * View to configure the authentication mode.
 */
public class AuthenticationConfigurationView extends CustomComponent {

    private static final String DIST_CHECKBOX_STYLE = "dist-checkbox-style";

    private static final long serialVersionUID = 1L;

    private final VaadinMessageSource i18n;

    private final CertificateAuthenticationConfigurationItem certificateAuthenticationConfigurationItem;

    private final TargetSecurityTokenAuthenticationConfigurationItem targetSecurityTokenAuthenticationConfigurationItem;

    private final GatewaySecurityTokenAuthenticationConfigurationItem gatewaySecurityTokenAuthenticationConfigurationItem;

    private final AnonymousDownloadAuthenticationConfigurationItem anonymousDownloadAuthenticationConfigurationItem;

    private final UiProperties uiProperties;

    private CheckBox gatewaySecTokenCheckBox;

    private CheckBox targetSecTokenCheckBox;

    private CheckBox certificateAuthCheckbox;

    private CheckBox downloadAnonymousCheckBox;

    private final Binder<ProxySystemConfigWindow> binder;

    AuthenticationConfigurationView(final VaadinMessageSource i18n,
            final TargetSecurityTokenAuthenticationConfigurationItem targetSecurityTokenAuthenticationConfigurationItem,
            final CertificateAuthenticationConfigurationItem certificateAuthenticationConfigurationItem,
            final GatewaySecurityTokenAuthenticationConfigurationItem gatewaySecurityTokenAuthenticationConfigurationItem,
            final AnonymousDownloadAuthenticationConfigurationItem anonymousDownloadAuthenticationConfigurationItem,
            final UiProperties uiProperties, final Binder<ProxySystemConfigWindow> binder) {
        this.i18n = i18n;
        this.targetSecurityTokenAuthenticationConfigurationItem = targetSecurityTokenAuthenticationConfigurationItem;
        this.certificateAuthenticationConfigurationItem = certificateAuthenticationConfigurationItem;
        this.gatewaySecurityTokenAuthenticationConfigurationItem = gatewaySecurityTokenAuthenticationConfigurationItem;
        this.anonymousDownloadAuthenticationConfigurationItem = anonymousDownloadAuthenticationConfigurationItem;
        this.uiProperties = uiProperties;
        this.binder = binder;

        init();
    }

    private void init() {

        final Panel rootPanel = new Panel();
        rootPanel.setSizeFull();

        rootPanel.addStyleName("config-panel");

        final VerticalLayout vLayout = new VerticalLayout();
        vLayout.setSpacing(false);
        vLayout.setMargin(true);
        vLayout.setSizeFull();

        final Label header = new Label(i18n.getMessage("configuration.authentication.title"));
        header.addStyleName("config-panel-header");
        vLayout.addComponent(header);

        final GridLayout gridLayout = new GridLayout(3, 4);
        gridLayout.setSpacing(true);

        gridLayout.setSizeFull();
        gridLayout.setColumnExpandRatio(1, 1.0F);
        certificateAuthCheckbox = new CheckBox();
        certificateAuthCheckbox.setStyleName(DIST_CHECKBOX_STYLE);
        certificateAuthCheckbox.addValueChangeListener(
                valueChangeEvent -> certificateAuthenticationConfigurationItem.setDetailVisible(
                        valueChangeEvent.getValue()));
        binder.bind(certificateAuthCheckbox, ProxySystemConfigWindow::isCertificateAuth,
                ProxySystemConfigWindow::setCertificateAuth);
        gridLayout.addComponent(certificateAuthCheckbox, 0, 0);
        gridLayout.addComponent(certificateAuthenticationConfigurationItem, 1, 0);
        targetSecTokenCheckBox = new CheckBox();
        targetSecTokenCheckBox.setStyleName(DIST_CHECKBOX_STYLE);
        binder.bind(targetSecTokenCheckBox, ProxySystemConfigWindow::isTargetSecToken,
                ProxySystemConfigWindow::setTargetSecToken);

        gridLayout.addComponent(targetSecTokenCheckBox, 0, 1);
        gridLayout.addComponent(targetSecurityTokenAuthenticationConfigurationItem, 1, 1);

        gatewaySecTokenCheckBox = new CheckBox();
        gatewaySecTokenCheckBox.setStyleName(DIST_CHECKBOX_STYLE);
        gatewaySecTokenCheckBox.setId("gatewaysecuritycheckbox");
        gatewaySecTokenCheckBox.addValueChangeListener(
                valueChangeEvent -> gatewaySecurityTokenAuthenticationConfigurationItem.setDetailVisible(
                        valueChangeEvent.getValue()));
        binder.bind(gatewaySecTokenCheckBox, ProxySystemConfigWindow::isGatewaySecToken,
                ProxySystemConfigWindow::setGatewaySecToken);
        gridLayout.addComponent(gatewaySecTokenCheckBox, 0, 2);
        gridLayout.addComponent(gatewaySecurityTokenAuthenticationConfigurationItem, 1, 2);

        downloadAnonymousCheckBox = new CheckBox();
        downloadAnonymousCheckBox.setStyleName(DIST_CHECKBOX_STYLE);
        downloadAnonymousCheckBox.setId(UIComponentIdProvider.DOWNLOAD_ANONYMOUS_CHECKBOX);
        binder.bind(downloadAnonymousCheckBox, ProxySystemConfigWindow::isDownloadAnonymous,
                ProxySystemConfigWindow::setDownloadAnonymous);

        gridLayout.addComponent(downloadAnonymousCheckBox, 0, 3);
        gridLayout.addComponent(anonymousDownloadAuthenticationConfigurationItem, 1, 3);

        final Link linkToSecurityHelp = SPUIComponentProvider.getHelpLink(i18n,
                uiProperties.getLinks().getDocumentation().getSecurity());
        gridLayout.addComponent(linkToSecurityHelp, 2, 3);
        gridLayout.setComponentAlignment(linkToSecurityHelp, Alignment.BOTTOM_RIGHT);

        vLayout.addComponent(gridLayout);
        rootPanel.setContent(vLayout);
        setCompositionRoot(rootPanel);
    }
}
