/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid;

import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupport;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

/**
 * Abstract grid layout class which builds layout with grid {@link AbstractGrid}
 * and grid header {@link DefaultGridHeader}.
 * 
 * @param <T>
 *            The container-type used by the grid
 */
public abstract class AbstractGridComponentLayout<T> extends VerticalLayout {

    private static final long serialVersionUID = 1L;

    private final transient EventBus.UIEventBus eventBus;

    private final VaadinMessageSource i18n;

    private transient AbstractFooterSupport footerSupport;

    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     */
    public AbstractGridComponentLayout(final VaadinMessageSource i18n, final UIEventBus eventBus) {
        this.i18n = i18n;
        this.eventBus = eventBus;
    }

    /**
     * Initializes this layout that presents a header and a grid.
     */
    protected void init() {
        buildLayout();
        setSizeFull();
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

    /**
     * Layouts header, grid and optional footer.
     */
    protected void buildLayout() {
        final AbstractOrderedLayout gridHeader = getGridHeader();
        final Grid<T> grid = getGrid();

        setSizeFull();
        setSpacing(true);
        setMargin(false);
        setStyleName("group");
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
        if (hasFooterSupport()) {
            final Layout footerLayout = getFooterSupport().createFooterMessageComponent();
            addComponent(footerLayout);
            setComponentAlignment(footerLayout, Alignment.BOTTOM_CENTER);
        }

    }

    /**
     * Gets the grid instance displayed and owned by the layout.
     *
     * @return grid instance displayed and owned by the layout.
     */
    public abstract Grid<T> getGrid();

    /**
     * Gets the grid-header instance displayed and owned by the layout.
     *
     * @return grid-header instance displayed and owned by the layout.
     */
    public abstract AbstractOrderedLayout getGridHeader();

    /**
     * Enables footer-support for the grid by setting a FooterSupport
     * implementation.
     *
     * @param footerSupport
     *            encapsulates footer layout.
     */
    public void setFooterSupport(final AbstractFooterSupport footerSupport) {
        this.footerSupport = footerSupport;
    }

    /**
     * Gets the FooterSupport implementation describing footer layout.
     *
     * @return footerSupport that encapsulates footer layout.
     */
    public AbstractFooterSupport getFooterSupport() {
        return footerSupport;
    }

    /**
     * Checks whether footer-support is enabled.
     *
     * @return <code>true</code> if footer-support is enabled, otherwise
     *         <code>false</code>
     */
    public boolean hasFooterSupport() {
        return footerSupport != null;
    }

    /**
     * If footer support is enabled, the footer is placed below the component
     */
    public abstract class AbstractFooterSupport {

        /**
         * Creates a sub-layout for the footer.
         *
         * @return the footer sub-layout.
         */
        private Layout createFooterMessageComponent() {
            final HorizontalLayout footerLayout = new HorizontalLayout();
            footerLayout.addComponent(getFooterMessageLabel());
            footerLayout.setWidth(100, Unit.PERCENTAGE);
            footerLayout.setMargin(false);
            footerLayout.setSpacing(false);
            return footerLayout;
        }

        /**
         * Get the count message label.
         *
         * @return count message
         */
        protected abstract Label getFooterMessageLabel();
    }

    protected VaadinMessageSource getI18n() {
        return i18n;
    }

    protected EventBus.UIEventBus getEventBus() {
        return eventBus;
    }

    /**
     * Registers the selection of this grid as master for another layout that
     * displays the details.
     *
     * @param detailsSupport
     *            the details support of another layout the selection of this
     *            grid should be registered for as master.
     */
    public void registerDetails(final MasterDetailsSupport<T, ?> detailsSupport) {
        getGrid().addSelectionListener(event -> {
            final T selectedItem = event.getFirstSelectedItem().orElse(null);
            detailsSupport.masterItemChangedCallback(selectedItem);
        });
    }
}
