/** Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.builder;

import com.vaadin.data.Binder.Binding;
import com.vaadin.ui.Component;

/**
 * Holds a {@link Component} and its {@link Binding}
 * 
 * @param <T>
 *            Component type
 */
public class BoundComponent<T extends Component> {
    private T component;
    private Binding<?, ?> binding;

    /**
     * Constructor
     * 
     * @param component
     *            component
     * @param binding
     *            binding of the component
     */
    public BoundComponent(T component, Binding<?, ?> binding) {
        this.component = component;
        this.binding = binding;
    }

    public T getComponent() {
        return component;
    }

    public void setRequired(boolean isRequired) {
        binding.setAsRequiredEnabled(isRequired);
    }
}
