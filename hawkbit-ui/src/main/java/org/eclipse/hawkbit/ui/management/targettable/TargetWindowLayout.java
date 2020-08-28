/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
import org.eclipse.hawkbit.ui.common.builder.BoundComponent;
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
    private final BoundComponent<TextField> targetName;
    private final TextArea targetDescription;

    /**
     * Constructor for AbstractTagWindowLayout
     * 
     * @param i18n
     *            I18N
     */
    public TargetWindowLayout(final VaadinMessageSource i18n) {
        super();
        this.targetComponentBuilder = new TargetWindowLayoutComponentBuilder(i18n);

        this.targetControllerId = targetComponentBuilder.createControllerIdField(binder);
        this.targetName = targetComponentBuilder.createNameField(binder);
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
        targetWindowLayout.addComponent(targetName.getComponent());
        targetWindowLayout.addComponent(targetDescription);
        return targetWindowLayout;
    }

    /**
     * Enable or disable the target controller id
     *
     * @param isIdEnabled
     *          boolean
     */
    public void setControllerIdEnabled(boolean isIdEnabled) {
        targetControllerId.setEnabled(isIdEnabled);
    }

    /**
     * Enable or disable the target name filed as required
     *
     * @param isNameRequired
     *          boolean
     */
    public void setNameRequired(boolean isNameRequired) {
        targetName.setRequired(isNameRequired);
    }
}
