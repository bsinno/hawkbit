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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.grid.selection.RangeSelectionModel;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.components.RefreshableContainer;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.components.grid.GridSelectionModel;
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
public abstract class AbstractGrid<T extends ProxyIdentifiableEntity, F> extends Grid<T>
        implements RefreshableContainer {
    private static final long serialVersionUID = 1L;

    protected static final String CENTER_ALIGN = "v-align-center";
    public static final String MULTI_SELECT_STYLE = "multi-selection-grid";

    protected final VaadinMessageSource i18n;
    protected final transient UIEventBus eventBus;
    protected final SpPermissionChecker permissionChecker;

    private transient ResizeSupport resizeSupport;
    private transient SelectionSupport<T> selectionSupport;

    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     */
    protected AbstractGrid(final VaadinMessageSource i18n, final UIEventBus eventBus) {
        this(i18n, eventBus, null);
    }

    @Override
    public GridSelectionModel<T> setSelectionMode(final SelectionMode mode) {
        if (mode == SelectionMode.MULTI) {
            final RangeSelectionModel<T> model = new RangeSelectionModel<>();
            setSelectionModel(model);

            // used to deactivate cell text selection by user
            addStyleName(MULTI_SELECT_STYLE);

            return model;
        }

        return super.setSelectionMode(mode);
    }

    /**
     * Constructor.
     *
     * @param i18n
     * @param eventBus
     * @param permissionChecker
     */
    protected AbstractGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker) {
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.permissionChecker = permissionChecker;
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
        if (selectionSupport == null) {
            selectionSupport = new SelectionSupport<>(this);
            selectionSupport.disableSelection();
        }
        setColumnReorderingAllowed(false);
        setDataProvider(getFilterDataProvider());
        addColumns();
        disableColumnSorting();
        setFrozenColumnCount(-1);
    }

    // TODO: check if it is needed or could be called directly
    /**
     * Refresh the container.
     */
    @Override
    public void refreshContainer() {
        getFilterDataProvider().refreshAll();
    }

    public void refreshItem(final T item) {
        getFilterDataProvider().refreshItem(item);
    }

    public int getDataSize() {
        return getDataCommunicator().getDataProviderSize();
    }

    /**
     * Method for setting up the required columns together with their definition
     * and rendering options.
     */
    public abstract void addColumns();

    private void disableColumnSorting() {
        for (final Column<T, ?> c : getColumns()) {
            c.setSortable(false);
        }
    }

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
     * Enables selection-support for the grid by setting SelectionSupport
     * configuration.
     *
     * @param SelectionSupport
     *            encapsulates behavior for selection and offers some convenient
     *            functionality.
     */
    protected void setSelectionSupport(final SelectionSupport<T> selectionSupport) {
        this.selectionSupport = selectionSupport;
    }

    /**
     * Gets the SelectionSupport implementation configuring selection.
     *
     * @return selectionSupport that configures selection.
     */
    public SelectionSupport<T> getSelectionSupport() {
        return selectionSupport;
    }

    /**
     * Checks whether single or multi selection-support is enabled.
     *
     * @return <code>true</code> if single or multi selection-support is
     *         enabled, otherwise <code>false</code>
     */
    public boolean hasSelectionSupport() {
        return selectionSupport != null && !selectionSupport.isNoSelectionModel();
    }

    /**
     * Gets the wrapped in {@link ConfigurableFilterDataProvider} dataprovider
     * in order to dynamically update the filters.
     *
     * @return {@link ConfigurableFilterDataProvider} wrapper of dataprovider.
     */
    public abstract ConfigurableFilterDataProvider<T, Void, F> getFilterDataProvider();

    /**
     * Gets id of the grid.
     *
     * @return id of the grid
     */
    public abstract String getGridId();

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

    // TODO move to GridComponentBuilder
    protected Label buildLabelIcon(final ProxyFontIcon fontIcon, final String id) {
        if (fontIcon == null) {
            return new Label("");
        }

        final Label labelIcon = new Label(fontIcon.getHtml(), ContentMode.HTML);
        labelIcon.setId(id);
        labelIcon.setDescription(fontIcon.getDescription());
        labelIcon.addStyleName("small");
        labelIcon.addStyleName("font-icon");
        labelIcon.addStyleName(fontIcon.getStyle());

        return labelIcon;
    }
}
