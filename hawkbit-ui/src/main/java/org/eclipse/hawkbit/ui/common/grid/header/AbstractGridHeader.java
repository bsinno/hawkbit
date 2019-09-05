/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.header;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.grid.header.support.HeaderSupport;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

/**
 * Abstract grid header.
 */
public abstract class AbstractGridHeader extends HorizontalLayout {
    private static final long serialVersionUID = 1L;

    protected final VaadinMessageSource i18n;
    protected final SpPermissionChecker permissionChecker;
    protected final transient UIEventBus eventBus;

    protected final Component headerCaption;
    private final Collection<HeaderSupport> headerSupports;

    public AbstractGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permissionChecker,
            final UIEventBus eventBus) {
        this.i18n = i18n;
        this.permissionChecker = permissionChecker;
        this.eventBus = eventBus;

        this.headerCaption = getHeaderCaption();
        this.headerSupports = new ArrayList<>();

        init();
        if (doSubscribeToEventBus()) {
            eventBus.subscribe(this);
        }
    }

    protected abstract Component getHeaderCaption();

    protected void addHeaderSupports(final Collection<HeaderSupport> headerSupports) {
        this.headerSupports.addAll(headerSupports);
    }

    private void init() {
        addStyleName(SPUIStyleDefinitions.WIDGET_TITLE);
        addStyleName("bordered-layout");
        addStyleName("no-border-bottom");

        setSpacing(false);
        setMargin(false);
        setSizeFull();
        setHeight("40px");
    }

    /**
     * Subscribes the view to the eventBus. Method has to be overriden (return
     * false) if the view does not contain any listener to avoid Vaadin blowing
     * up our logs with warnings.
     */
    protected boolean doSubscribeToEventBus() {
        return true;
    }

    protected void buildHeader() {
        addComponent(headerCaption);
        setComponentAlignment(headerCaption, Alignment.TOP_LEFT);

        headerSupports.forEach(headerSupport -> {
            addComponent(headerSupport.getHeaderIcon());
            setComponentAlignment(headerSupport.getHeaderIcon(), Alignment.TOP_RIGHT);
        });
    }

    protected void restoreHeaderState() {
        // TODO: check if we need to call restoreCaption here
        headerSupports.forEach(HeaderSupport::restoreState);
    }
}
