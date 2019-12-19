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

        targetWindowLayout.addComponent(targetName);

        targetWindowLayout.addComponent(targetDescription);

        return targetWindowLayout;
    }

    public void disableControllerId() {
        targetControllerId.setEnabled(false);
    }

    public void setNameAsRequired() {
        // as of now vaadin does not allow modifying existing bindings, so we
        // need to rebind name field again with required validator
        targetComponentBuilder.getTargetNameBinding().unbind();
        // TODO: use i18n for all the required fields messages
        binder.forField(targetName).asRequired("You must provide target name").bind(ProxyTarget::getName,
                ProxyTarget::setName);
    }
}
