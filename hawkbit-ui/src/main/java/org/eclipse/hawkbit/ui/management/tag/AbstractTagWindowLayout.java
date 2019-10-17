/**
 * Copyright (c) 2015-2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.tag;

import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.data.Binder;
import com.vaadin.ui.HorizontalLayout;

/**
 * Abstract class for tag add/update window layout.
 */
public abstract class AbstractTagWindowLayout extends HorizontalLayout {
    private static final long serialVersionUID = 1L;

    protected static final int MAX_TAGS = 500;

    protected final Binder<ProxyTag> proxyTagBinder;

    protected final TagWindowLayoutComponentBuilder componentBuilder;

    protected final VaadinMessageSource i18n;

    /**
     * Constructor for AbstractTagWindowLayout
     * 
     * @param i18n
     *            I18N
     */
    public AbstractTagWindowLayout(final VaadinMessageSource i18n) {
        this.i18n = i18n;
        this.proxyTagBinder = new Binder<>();
        this.componentBuilder = new TagWindowLayoutComponentBuilder(i18n);

        initLayout();
    }

    private void initLayout() {
        setSpacing(false);
        setMargin(false);
        setSizeUndefined();
    }

    public Binder<ProxyTag> getProxyTagBinder() {
        return proxyTagBinder;
    }

    public void addValidationListener(final Consumer<Boolean> validationCallback) {
        proxyTagBinder.addStatusChangeListener(event -> validationCallback.accept(event.getBinder().isValid()));
    }
}
