/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * Target add/update window layout.
 */
public class TargetWindowLayout extends FormLayout {
    private static final long serialVersionUID = 1L;

    protected final Binder<ProxyTarget> binder;

    protected final TargetWindowLayoutComponentBuilder componentBuilder;

    protected final VaadinMessageSource i18n;

    protected final TextField targetControllerId;
    protected final TextField targetName;
    protected final TextArea targetDescription;

    /**
     * Constructor for AbstractTagWindowLayout
     * 
     * @param i18n
     *            I18N
     */
    public TargetWindowLayout(final VaadinMessageSource i18n) {
        this.i18n = i18n;
        this.binder = new Binder<>();
        this.componentBuilder = new TargetWindowLayoutComponentBuilder(i18n);

        this.targetControllerId = componentBuilder.createControllerIdField(binder);
        this.targetName = componentBuilder.createNameField(binder);
        this.targetDescription = componentBuilder.createDescriptionField(binder);

        initLayout();
        buildLayout();
    }

    private void initLayout() {
        setSpacing(true);
        setMargin(true);
        setSizeUndefined();
    }

    private void buildLayout() {
        addComponent(targetControllerId);
        targetControllerId.focus();

        addComponent(targetName);

        addComponent(targetDescription);
    }

    public Binder<ProxyTarget> getBinder() {
        return binder;
    }

    public void addValidationListener(final Consumer<Boolean> validationCallback) {
        binder.addStatusChangeListener(event -> validationCallback.accept(event.getBinder().isValid()));
    }

    public void disableControllerId() {
        targetControllerId.setEnabled(false);
    }

    public void setNameAsRequired() {
        // as of now vaadin does not allow modifying existing bindings, so we
        // need to rebind name field again with required validator
        componentBuilder.getTargetNameBinding().unbind();
        // TODO: use i18n for all the required fields messages
        binder.forField(targetName).asRequired("You must provide target name").bind(ProxyTarget::getName,
                ProxyTarget::setName);
    }
}
