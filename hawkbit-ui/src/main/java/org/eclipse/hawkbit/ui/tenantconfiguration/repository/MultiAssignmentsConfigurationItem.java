/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.tenantconfiguration.repository;

import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySystemConfigWindow;
import org.eclipse.hawkbit.ui.tenantconfiguration.generic.AbstractBooleanTenantConfigurationItem;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * This class represents the UI item for enabling /disabling the
 * Multi-Assignments feature as part of the repository configuration view.
 */
public class MultiAssignmentsConfigurationItem extends AbstractBooleanTenantConfigurationItem {

    private static final long serialVersionUID = 1L;

    private static final String MSG_KEY_CHECKBOX = "label.configuration.repository.multiassignments";
    private static final String MSG_KEY_NOTICE = "label.configuration.repository.multiassignments.notice";

    private final VerticalLayout container;
    private final VaadinMessageSource i18n;

    private final Binder<ProxySystemConfigWindow> binder;

    /**
     * Constructor.
     *
     * @param tenantConfigurationManagement
     *         to read /write tenant-specific configuration properties
     * @param i18n
     * @param binder
     */
    public MultiAssignmentsConfigurationItem(final TenantConfigurationManagement tenantConfigurationManagement,
            final VaadinMessageSource i18n, Binder<ProxySystemConfigWindow> binder) {
        super(TenantConfigurationKey.MULTI_ASSIGNMENTS_ENABLED, tenantConfigurationManagement, i18n);
        this.i18n = i18n;
        this.binder = binder;
        super.init(MSG_KEY_CHECKBOX);
        container = new VerticalLayout();
        container.addComponent(newLabel(MSG_KEY_NOTICE));
        if (binder.getBean().isMultiAssignments()) {
            setSettingsVisible(true);
        }
    }

    @Override
    public void configEnable() {
        setSettingsVisible(true);
    }

    @Override
    public void configDisable() {
        setSettingsVisible(false);
    }

    @Override
    public void save() {
    }

    @Override
    public void undo() {
    }

    public void setSettingsVisible(final boolean visible) {
        if (visible) {
            addComponent(container);
        } else {
            removeComponent(container);
        }
    }

    private Label newLabel(final String msgKey) {
        final Label label = new LabelBuilder().name(i18n.getMessage(msgKey)).buildLabel();
        label.setWidthUndefined();
        return label;
    }

}
