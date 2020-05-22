/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration.authentication;

import org.eclipse.hawkbit.repository.model.TenantConfiguration;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * This class represents the UI item for the certificate authenticated by an
 * reverse proxy in the authentication configuration view.
 */
public class CertificateAuthenticationConfigurationItem extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private final VerticalLayout detailLayout;
    private final TextField caRootAuthorityTextField;

    public CertificateAuthenticationConfigurationItem(final VaadinMessageSource i18n,
            final Binder<ProxySystemConfigWindow> binder) {
        this.setSpacing(false);
        this.setMargin(false);
        addComponent(new LabelBuilder().name(i18n.getMessage("label.configuration.auth.header")).buildLabel());
        detailLayout = new VerticalLayout();
        detailLayout.setMargin(false);
        detailLayout.setSpacing(false);

        final HorizontalLayout caRootAuthorityLayout = new HorizontalLayout();
        caRootAuthorityLayout.setSpacing(true);

        final Label caRootAuthorityLabel = new LabelBuilder()
                .name(i18n.getMessage("label.configuration.auth.hashField")).buildLabel();
        caRootAuthorityLabel.setDescription(i18n.getMessage("label.configuration.auth.hashField.tooltip"));
        caRootAuthorityLabel.setWidthUndefined();

        caRootAuthorityTextField = new TextFieldBuilder(TenantConfiguration.VALUE_MAX_SIZE).buildTextComponent();
        caRootAuthorityTextField.setWidth("100%");
        binder.bind(caRootAuthorityTextField, ProxySystemConfigWindow::getCaRootAuthority,
                ProxySystemConfigWindow::setCaRootAuthority);
        caRootAuthorityLayout.addComponent(caRootAuthorityLabel);
        caRootAuthorityLayout.setExpandRatio(caRootAuthorityLabel, 0);
        caRootAuthorityLayout.addComponent(caRootAuthorityTextField);
        caRootAuthorityLayout.setExpandRatio(caRootAuthorityTextField, 1);
        caRootAuthorityLayout.setWidth("100%");
        detailLayout.addComponent(caRootAuthorityLayout);
        if (binder.getBean().isCertificateAuth()) {
            showDetails();
        }
    }

    public void showDetails() {
        addComponent(detailLayout);
    }

    public void hideDetails() {
        removeComponent(detailLayout);
    }
}
