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

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutWindow;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowDependencies;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowLayoutComponentBuilder;

import com.vaadin.data.Binder;
import com.vaadin.ui.GridLayout;

/**
 * Abstract Grid Rollout window layout.
 */
@SuppressWarnings({ "squid:MaximumInheritanceDepth", "squid:S2160" })
public abstract class AbstractRolloutWindowLayout extends GridLayout {
    private static final long serialVersionUID = 1L;

    protected final Binder<ProxyRolloutWindow> proxyRolloutBinder;

    protected final RolloutWindowLayoutComponentBuilder componentBuilder;

    protected final RolloutWindowDependencies dependencies;

    protected AbstractRolloutWindowLayout(final RolloutWindowDependencies dependencies) {
        this.dependencies = dependencies;
        this.proxyRolloutBinder = new Binder<>();
        this.componentBuilder = new RolloutWindowLayoutComponentBuilder(dependencies);
        initLayout();
    }

    private void initLayout() {
        setSpacing(true);
        setSizeUndefined();
        setColumns(4);
        setStyleName("marginTop");
        setColumnExpandRatio(3, 1);
        setWidth(850, Unit.PIXELS);
    }

    public Binder<ProxyRolloutWindow> getProxyRolloutBinder() {
        return proxyRolloutBinder;
    }

    protected Long getTotalTargets() {
        return proxyRolloutBinder.getBean().getTotalTargets();
    }

    public void addValidationListener(final Consumer<Boolean> validationCallback) {
        proxyRolloutBinder.addStatusChangeListener(event -> validationCallback.accept(event.getBinder().isValid()));
    }
}
