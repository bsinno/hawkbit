/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.data.mappers.TypeToProxyTypeMapper;
import org.eclipse.hawkbit.ui.common.data.providers.SoftwareModuleTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * Target add/update window layout.
 */
public class SmWindowLayout extends AbstractEntityWindowLayout<ProxySoftwareModule> {
    private final SmWindowLayoutComponentBuilder smComponentBuilder;

    private final ComboBox<ProxyType> smTypeSelect;
    private final TextField smName;
    private final TextField smVersion;
    private final TextField smVendor;
    private final TextArea smDescription;

    /**
     * Constructor for AbstractTagWindowLayout
     * 
     * @param i18n
     *            I18N
     */
    public SmWindowLayout(final VaadinMessageSource i18n, final SoftwareModuleTypeManagement smTypeManagement) {
        super();

        final SoftwareModuleTypeDataProvider smTypeDataProvider = new SoftwareModuleTypeDataProvider(smTypeManagement,
                new TypeToProxyTypeMapper<SoftwareModuleType>());
        this.smComponentBuilder = new SmWindowLayoutComponentBuilder(i18n, smTypeDataProvider);

        this.smTypeSelect = smComponentBuilder.createSoftwareModuleTypeCombo(binder);
        this.smName = smComponentBuilder.createNameField(binder);
        this.smVersion = smComponentBuilder.createVersionField(binder);
        this.smVendor = smComponentBuilder.createVendorField(binder);
        this.smDescription = smComponentBuilder.createDescription(binder);
    }

    @Override
    public ComponentContainer getRootComponent() {
        final FormLayout smWindowLayout = new FormLayout();

        smWindowLayout.setSpacing(true);
        smWindowLayout.setMargin(true);
        smWindowLayout.setSizeUndefined();

        smWindowLayout.addComponent(smTypeSelect);

        smWindowLayout.addComponent(smName);
        smName.focus();

        smWindowLayout.addComponent(smVersion);

        smWindowLayout.addComponent(smVendor);

        smWindowLayout.addComponent(smDescription);

        return smWindowLayout;
    }

    public void disableSmTypeSelect() {
        smTypeSelect.setEnabled(false);
    }

    public void disableNameField() {
        smName.setEnabled(false);
    }

    public void disableVersionField() {
        smVersion.setEnabled(false);
    }
}