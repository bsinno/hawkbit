/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * Target add/update window layout.
 */
public class TargetWindowLayout extends AbstractEntityWindowLayout<ProxyTarget> {
    private final TargetWindowLayoutComponentBuilder targetComponentBuilder;

    private final TextField targetControllerId;
    private final TextField targetName;
    private final TextArea targetDescription;
    private final WindowType windowType;

    /**
     * Constructor for AbstractTagWindowLayout
     * 
     * @param i18n
     *            I18N
     */
    public TargetWindowLayout(final VaadinMessageSource i18n, WindowType windowType) {
        super();
        this.windowType = windowType;
        this.targetComponentBuilder = new TargetWindowLayoutComponentBuilder(i18n);

        this.targetControllerId = targetComponentBuilder.createControllerIdField(binder);
        if (windowType == WindowType.UPDATE) {
            this.targetName = targetComponentBuilder.createRequiredNameField(binder);
        } else {
            this.targetName = targetComponentBuilder.createOptionalNameField(binder);
        }
        this.targetDescription = targetComponentBuilder.createDescriptionField(binder);
    }

    @Override
    public ComponentContainer getRootComponent() {
        final FormLayout targetWindowLayout = new FormLayout();

        targetWindowLayout.setSpacing(true);
        targetWindowLayout.setMargin(true);
        targetWindowLayout.setSizeUndefined();

        targetWindowLayout.addComponent(targetControllerId);
        targetControllerId.focus();
        if (windowType == WindowType.UPDATE) {
            targetControllerId.setEnabled(false);
        }

        targetWindowLayout.addComponent(targetName);

        targetWindowLayout.addComponent(targetDescription);

        return targetWindowLayout;
    }
}
