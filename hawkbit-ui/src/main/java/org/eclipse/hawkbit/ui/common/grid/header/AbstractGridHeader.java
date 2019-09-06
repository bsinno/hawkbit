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
import java.util.Objects;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.grid.header.support.HeaderSupport;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Abstract grid header.
 */
public abstract class AbstractGridHeader extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    protected final VaadinMessageSource i18n;
    protected final SpPermissionChecker permChecker;
    protected final transient UIEventBus eventBus;

    private final transient Collection<HeaderSupport> headerSupports;

    public AbstractGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus) {
        this.i18n = i18n;
        this.permChecker = permChecker;
        this.eventBus = eventBus;

        this.headerSupports = new ArrayList<>();

        init();
        if (doSubscribeToEventBus()) {
            eventBus.subscribe(this);
        }
    }

    protected void restoreCaption() {
        // empty by default for stateless header captions
    }

    protected void addHeaderSupports(final Collection<HeaderSupport> headerSupports) {
        this.headerSupports.addAll(headerSupports);
    }

    private void init() {
        setSpacing(false);
        setMargin(false);

        addStyleName("bordered-layout");
        addStyleName("no-border-bottom");

        setHeight("50px");
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
        final HorizontalLayout headerComponentsLayout = new HorizontalLayout();

        headerComponentsLayout.addStyleName(SPUIStyleDefinitions.WIDGET_TITLE);
        headerComponentsLayout.setSpacing(false);
        headerComponentsLayout.setMargin(false);
        headerComponentsLayout.setSizeFull();

        final Component headerCaption = getHeaderCaption();
        headerComponentsLayout.addComponent(headerCaption);
        headerComponentsLayout.setComponentAlignment(headerCaption, Alignment.TOP_LEFT);
        headerComponentsLayout.setExpandRatio(headerCaption, 0.4F);

        // TODO: adapt Expand Ratios for header support components
        headerSupports.stream().filter(Objects::nonNull).forEach(headerSupport -> {
            final Component headerComponent = headerSupport.getHeaderComponent();

            headerComponentsLayout.addComponent(headerComponent);
            headerComponentsLayout.setComponentAlignment(headerComponent, Alignment.TOP_RIGHT);
        });

        addComponent(headerComponentsLayout);
    }

    protected abstract Component getHeaderCaption();

    protected void restoreHeaderState() {
        restoreCaption();
        headerSupports.stream().filter(Objects::nonNull).forEach(HeaderSupport::restoreState);
    }
}
