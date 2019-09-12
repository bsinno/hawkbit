/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyNamedEntity;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.VerticalLayout;

/**
 * Abstract Layout to show the entity details.
 *
 * @param <T>
 */
public class AbstractGridDetailsLayout<T extends ProxyNamedEntity> extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    protected final VaadinMessageSource i18n;
    protected final SpPermissionChecker permChecker;
    protected final transient UIEventBus eventBus;

    public AbstractGridDetailsLayout(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus) {
        this.i18n = i18n;
        this.permChecker = permChecker;
        this.eventBus = eventBus;

        init();
        if (doSubscribeToEventBus()) {
            eventBus.subscribe(this);
        }
    }

    /**
     * Subscribes the view to the eventBus. Method has to be overriden (return
     * false) if the view does not contain any listener to avoid Vaadin blowing
     * up our logs with warnings.
     */
    protected boolean doSubscribeToEventBus() {
        return true;
    }

    private void init() {
        // TODO
    }

    protected void buildDetails() {
        // TODO
    }

    protected void populateDetails() {
        // TODO
    }
}
