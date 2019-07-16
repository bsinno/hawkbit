/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SingleSelectionSupport;
import org.eclipse.hawkbit.ui.components.RefreshableContainer;
import org.eclipse.hawkbit.ui.rollout.FontIcon;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.components.grid.Header.Row;
import com.vaadin.ui.components.grid.HeaderRow;

/**
 * Abstract grid that offers various capabilities (aka support) to offer
 * convenient enhancements to the vaadin standard grid.
 *
 * @param <T>
 *            The container-type used by the grid
 * @param <F>
 *            The filter-type used by the grid
 */
public abstract class AbstractGrid<T, F> extends Grid<T> implements RefreshableContainer {
    private static final long serialVersionUID = 1L;

    protected static final String CENTER_ALIGN = "v-align-center";

    protected final VaadinMessageSource i18n;
    protected final transient EventBus.UIEventBus eventBus;
    protected final SpPermissionChecker permissionChecker;

    private final ConfigurableFilterDataProvider<T, Void, F> filterDataProvider;

    private transient ResizeSupport resizeSupport;
    private transient SingleSelectionSupport<T> singleSelectionSupport;

    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     * @param permissionChecker
     * @param filterDataProvider
     */
    protected AbstractGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker,
            final ConfigurableFilterDataProvider<T, Void, F> filterDataProvider) {
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.permissionChecker = permissionChecker;

        this.filterDataProvider = filterDataProvider;
    }

    /**
     * Initializes the grid.
     * <p>
     *
     * <b>NOTE:</b> Sub-classes should configure the grid before calling this
     * method (this means: set all support-classes needed, and then call init).
     */
    protected void init() {
        setSizeFull();
        setId(getGridId());
        if (!hasSingleSelectionSupport()) {
            setSelectionMode(SelectionMode.NONE);
        }
        setColumnReorderingAllowed(true);
        setDataProvider(getFilterDataProvider());
        addColumns();
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

    // TODO: check if it is needed
    /**
     * Refresh the container.
     */
    @Override
    public void refreshContainer() {
        getDataProvider().refreshAll();
    }

    /**
     * Method for setting up the required columns together with their definition
     * and rendering options.
     */
    public abstract void addColumns();

    /**
     * Enables resize support for the grid by setting a ResizeSupport
     * implementation.
     *
     * @param resizeSupport
     *            encapsulates behavior for minimize and maximize.
     */
    protected void setResizeSupport(final ResizeSupport resizeSupport) {
        this.resizeSupport = resizeSupport;
    }

    /**
     * Gets the ResizeSupport implementation describing behavior for minimize
     * and maximize.
     *
     * @return resizeSupport that encapsulates behavior for minimize and
     *         maximize.
     */
    protected ResizeSupport getResizeSupport() {
        return resizeSupport;
    }

    /**
     * Checks whether maximize-support is enabled.
     *
     * @return <code>true</code> if maximize-support is enabled, otherwise
     *         <code>false</code>
     */
    protected boolean hasResizeSupport() {
        return resizeSupport != null;
    }

    /**
     * Enables single-selection-support for the grid by setting
     * SingleSelectionSupport configuration.
     *
     * @param singleSelectionSupport
     *            encapsulates behavior for single-selection and offers some
     *            convenient functionality.
     */
    protected void setSingleSelectionSupport(final SingleSelectionSupport<T> singleSelectionSupport) {
        this.singleSelectionSupport = singleSelectionSupport;
    }

    /**
     * Gets the SingleSelectionSupport implementation configuring
     * single-selection.
     *
     * @return singleSelectionSupport that configures single-selection.
     */
    public SingleSelectionSupport<T> getSingleSelectionSupport() {
        return singleSelectionSupport;
    }

    /**
     * Checks whether single-selection-support is enabled.
     *
     * @return <code>true</code> if single-selection-support is enabled,
     *         otherwise <code>false</code>
     */
    public boolean hasSingleSelectionSupport() {
        return singleSelectionSupport != null;
    }

    /**
     * Gets the wrapped in {@link ConfigurableFilterDataProvider} dataprovider
     * in order to dynamically update the filters.
     *
     * @return {@link ConfigurableFilterDataProvider} wrapper of dataprovider.
     */
    public ConfigurableFilterDataProvider<T, Void, F> getFilterDataProvider() {
        return filterDataProvider;
    }

    /**
     * Gets id of the grid.
     *
     * @return id of the grid
     */
    protected abstract String getGridId();

    // TODO: check if it is needed
    /**
     * Resets the default row of the header. This means the current default row
     * is removed and replaced with a newly created one.
     *
     * @return the new and clean header row.
     */
    protected HeaderRow resetHeaderDefaultRow() {
        getHeader().removeRow(getHeader().getDefaultRow());
        final Row newHeaderRow = getHeader().addRowAt(0);
        getHeader().setDefaultRow(newHeaderRow);
        return newHeaderRow;
    }

    protected Label buildLabelIcon(final FontIcon fontIcon, final String id) {
        final String fontIconHtml = fontIcon.getIcon() != null ? fontIcon.getIcon().getHtml() : "<span></span>";

        final Label labelIcon = new Label(fontIconHtml, ContentMode.HTML);
        labelIcon.setId(id);
        labelIcon.setDescription(fontIcon.getDescription());
        labelIcon.addStyleName("small");
        labelIcon.addStyleName("font-icon");
        labelIcon.addStyleName(fontIcon.getStyle());

        return labelIcon;
    }
}
