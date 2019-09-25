/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid;

import org.eclipse.hawkbit.ui.common.detailslayout.AbstractGridDetailsLayout;
import org.eclipse.hawkbit.ui.common.detailslayout.DetailsHeader;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

/**
 * Abstract grid layout class which builds layout with grid header
 * {@link AbstractGridHeader}, grid {@link AbstractGrid}, optional grid details
 * {@link AbstractGridDetailsLayout} and optional footer.
 */
public abstract class AbstractGridComponentLayout extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    protected final VaadinMessageSource i18n;
    protected final transient EventBus.UIEventBus eventBus;

    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     */
    public AbstractGridComponentLayout(final VaadinMessageSource i18n, final UIEventBus eventBus) {
        this.i18n = i18n;
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
        setSizeFull();
        setSpacing(true);
        setMargin(false);
        setStyleName("group");
    }

    /**
     * Initializes this layout that presents a header and a grid.
     */
    protected void buildLayout(final AbstractGridHeader gridHeader, final AbstractGrid<?, ?> grid) {
        final VerticalLayout gridHeaderLayout = new VerticalLayout();
        gridHeaderLayout.setSizeFull();
        gridHeaderLayout.setSpacing(false);
        gridHeaderLayout.setMargin(false);

        gridHeaderLayout.setStyleName("table-layout");

        gridHeaderLayout.addComponent(gridHeader);
        gridHeaderLayout.setComponentAlignment(gridHeader, Alignment.TOP_CENTER);

        gridHeaderLayout.addComponent(grid);
        gridHeaderLayout.setComponentAlignment(grid, Alignment.TOP_CENTER);
        gridHeaderLayout.setExpandRatio(grid, 1.0F);

        addComponent(gridHeaderLayout);
        setComponentAlignment(gridHeaderLayout, Alignment.TOP_CENTER);
        setExpandRatio(gridHeaderLayout, 1.0F);
    }

    /**
     * Initializes this layout that presents a header, a grid, details header
     * and grid details.
     */
    protected void buildLayout(final AbstractGridHeader gridHeader, final AbstractGrid<?, ?> grid,
            final DetailsHeader<?> detailsHeader, final AbstractGridDetailsLayout<?> detailsLayout) {
        buildLayout(gridHeader, grid);

        final VerticalLayout detailsHeaderLayout = new VerticalLayout();
        detailsHeaderLayout.setSizeFull();
        detailsHeaderLayout.setSpacing(false);
        detailsHeaderLayout.setMargin(false);
        detailsHeaderLayout.setHeightUndefined();
        detailsHeaderLayout.addStyleName(SPUIStyleDefinitions.WIDGET_STYLE);

        detailsHeaderLayout.addComponent(detailsHeader);
        detailsHeaderLayout.setComponentAlignment(detailsHeader, Alignment.TOP_CENTER);

        detailsHeaderLayout.addComponent(detailsLayout);
        detailsHeaderLayout.setComponentAlignment(detailsLayout, Alignment.TOP_CENTER);
        detailsHeaderLayout.setExpandRatio(detailsLayout, 1.0F);

        addComponent(detailsHeaderLayout);
        setComponentAlignment(detailsHeaderLayout, Alignment.TOP_CENTER);
    }

    /**
     * Initializes this layout that presents a header, a grid and a footer.
     */
    protected void buildLayout(final AbstractGridHeader gridHeader, final AbstractGrid<?, ?> grid,
            final AbstractFooterSupport footerSupport) {
        buildLayout(gridHeader, grid);

        final Layout footerLayout = footerSupport.createFooterMessageComponent();
        addComponent(footerLayout);
        setComponentAlignment(footerLayout, Alignment.BOTTOM_CENTER);
    }

    /**
     * Initializes this layout that presents a header, a grid, grid details and
     * a footer.
     */
    protected void buildLayout(final AbstractGridHeader gridHeader, final AbstractGrid<?, ?> grid,
            final DetailsHeader<?> detailsHeader, final AbstractGridDetailsLayout<?> detailsLayout,
            final AbstractFooterSupport footerSupport) {
        buildLayout(gridHeader, grid, detailsHeader, detailsLayout);

        final Layout footerLayout = footerSupport.createFooterMessageComponent();
        addComponent(footerLayout);
        setComponentAlignment(footerLayout, Alignment.BOTTOM_CENTER);
    }
}
