/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window.layouts;

import org.eclipse.hawkbit.ui.common.AbstractEntityWindowLayout;
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
public abstract class AbstractRolloutWindowLayout extends AbstractEntityWindowLayout<ProxyRolloutWindow> {
    protected final RolloutWindowLayoutComponentBuilder rolloutComponentBuilder;

    protected AbstractRolloutWindowLayout(final RolloutWindowDependencies dependencies) {
        super();

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

    protected abstract void addComponents(final GridLayout rootLayout);
}
