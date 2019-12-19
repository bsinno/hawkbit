/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.disttype;

import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.management.tag.TagWindowLayout;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.TextField;

public class DsTypeWindowLayout extends TagWindowLayout<ProxyType> {
    private final DsTypeWindowLayoutComponentBuilder dsTypeComponentBuilder;

    private final TextField typeKey;
    private final DsTypeSmSelectLayout dsTypeSmSelectLayout;

    public DsTypeWindowLayout(final VaadinMessageSource i18n,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement) {
        super(i18n);

        this.dsTypeComponentBuilder = new DsTypeWindowLayoutComponentBuilder(i18n, softwareModuleTypeManagement);

        this.typeKey = dsTypeComponentBuilder.createKeyField(binder);
        this.dsTypeSmSelectLayout = dsTypeComponentBuilder.createDsTypeSmSelectLayout(binder);

        this.colorPickerComponent.getColorPickerBtn().setCaption(i18n.getMessage("label.choose.type.color"));
    }

    @Override
    protected void buildFormLayout() {
        super.buildFormLayout();

        formLayout.addComponent(typeKey, formLayout.getComponentCount() - 1);
    }

    @Override
    public ComponentContainer getRootComponent() {
        final ComponentContainer rootLayout = super.getRootComponent();

        rootLayout.addComponent(dsTypeSmSelectLayout);

        return rootLayout;
    }

    public void disableTypeKey() {
        typeKey.setEnabled(false);
    }

    public void disableDsTypeSmSelectLayout() {
        dsTypeSmSelectLayout.setEnabled(false);
    }
}
