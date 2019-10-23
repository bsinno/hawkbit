/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtype;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType.SmTypeAssign;
import org.eclipse.hawkbit.ui.management.tag.TagWindowLayout;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.RadioButtonGroup;
import com.vaadin.ui.TextField;

public class SmTypeWindowLayout extends TagWindowLayout<ProxyType> {
    private final SmTypeWindowLayoutComponentBuilder smTypeComponentBuilder;

    private final TextField typeKey;
    private final RadioButtonGroup<SmTypeAssign> smTypeAssignOptionGroup;

    public SmTypeWindowLayout(final VaadinMessageSource i18n) {
        super(i18n);

        this.smTypeComponentBuilder = new SmTypeWindowLayoutComponentBuilder(i18n);

        this.typeKey = smTypeComponentBuilder.createKeyField(binder);
        this.smTypeAssignOptionGroup = smTypeComponentBuilder.createSmTypeAssignOptionGroup(binder);

        this.colorPickerComponent.getColorPickerBtn().setCaption(i18n.getMessage("label.choose.type.color"));
    }

    @Override
    protected void buildFormLayout() {
        super.buildFormLayout();

        formLayout.addComponent(typeKey, formLayout.getComponentCount() - 1);
        formLayout.addComponent(smTypeAssignOptionGroup, formLayout.getComponentCount() - 1);
    }

    public void disableTypeKey() {
        typeKey.setEnabled(false);
    }

    public void disableTypeAssignOptionGroup() {
        smTypeAssignOptionGroup.setEnabled(false);
    }
}
