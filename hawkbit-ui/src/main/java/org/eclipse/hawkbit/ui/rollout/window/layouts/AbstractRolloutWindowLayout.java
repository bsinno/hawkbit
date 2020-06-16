/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.EntityWindowLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowLayoutComponentBuilder;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;

/**
 * Abstract Grid Rollout window layout.
 */
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2160" })
public abstract class AbstractRolloutWindowLayout implements EntityWindowLayout<ProxyRolloutWindow> {
    protected final RolloutWindowLayoutComponentBuilder rolloutComponentBuilder;

    protected Consumer<Boolean> validationCallback;

    protected AbstractRolloutWindowLayout(final RolloutWindowDependencies dependencies) {
        this.rolloutComponentBuilder = new RolloutWindowLayoutComponentBuilder(dependencies);
    }

    @Override
    public ComponentContainer getRootComponent() {
        final GridLayout rootLayout = new GridLayout();

        rootLayout.setSpacing(true);
        rootLayout.setSizeUndefined();
        rootLayout.setColumns(4);
        rootLayout.setStyleName("marginTop");
        rootLayout.setColumnExpandRatio(3, 1);
        rootLayout.setWidth(850, Unit.PIXELS);

        addComponents(rootLayout);

        return rootLayout;
    }

    @Override
    public void addValidationListener(final Consumer<Boolean> validationCallback) {
        this.validationCallback = validationCallback;
    }

    protected abstract void addComponents(final GridLayout rootLayout);
}
