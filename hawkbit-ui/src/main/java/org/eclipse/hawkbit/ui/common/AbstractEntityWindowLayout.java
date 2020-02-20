/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common;

import java.util.Optional;
import java.util.function.Consumer;

import com.vaadin.data.Binder;
import com.vaadin.ui.ComponentContainer;

public abstract class AbstractEntityWindowLayout<T> {
    
    /**
     * What a window can be used for
     */
    public enum WindowType{
        CREATE,
        UPDATE
    }
    
    protected final Binder<T> binder;

    protected Consumer<Boolean> validationCallback;

    protected AbstractEntityWindowLayout() {
        this.binder = new Binder<>();
    }

    public abstract ComponentContainer getRootComponent();

    public void setEntity(final T proxyEntity) {
        binder.setBean(proxyEntity);
    }

    public T getEntity() {
        return binder.getBean();
    }

    public void addValidationListener(final Consumer<Boolean> validationCallback) {
        binder.addStatusChangeListener(event -> validationCallback.accept(event.getBinder().isValid()));
        this.validationCallback = validationCallback;
    }

    public Optional<Consumer<Boolean>> getValidationCallback() {
        return Optional.ofNullable(validationCallback);
    }
}
