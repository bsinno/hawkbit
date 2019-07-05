/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid;

import java.util.Collections;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.components.RefreshableContainer;
import org.eclipse.hawkbit.ui.rollout.FontIcon;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import com.vaadin.data.provider.Query;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.components.grid.Header.Row;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.components.grid.SingleSelectionModel;

/**
 * Abstract grid that offers various capabilities (aka support) to offer
 * convenient enhancements to the vaadin standard grid.
 *
 * @param <T>
 *            The container-type used by the grid
 */
public abstract class AbstractGrid<T> extends Grid<T> implements RefreshableContainer {
    private static final long serialVersionUID = 1L;

    protected final VaadinMessageSource i18n;
    protected final transient EventBus.UIEventBus eventBus;
    protected final SpPermissionChecker permissionChecker;

    private transient AbstractMaximizeSupport maximizeSupport;
    private transient SingleSelectionSupport singleSelectionSupport;
    private transient DetailsSupport detailsSupport;

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
        if (!hasSingleSelectionSupport()) {
            setSelectionMode(SelectionMode.NONE);
        }
        setColumnReorderingAllowed(true);
        setDataProvider();
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

    /**
     * Refresh the container.
     */
    @Override
    public void refreshContainer() {
        getDataProvider().refreshAll();
    }

    /**
     * Method for setting the data provider in order to populate the grid with
     * data.
     */
    protected abstract void setDataProvider();

    /**
     * Method for setting up the required columns together with their definition
     * and rendering options.
     */
    protected abstract void addColumns();

    /**
     * Enables maximize-support for the grid by setting a MaximizeSupport
     * implementation.
     *
     * @param maximizeSupport
     *            encapsulates behavior for minimize and maximize.
     */
    protected void setMaximizeSupport(final AbstractMaximizeSupport maximizeSupport) {
        this.maximizeSupport = maximizeSupport;
    }

    /**
     * Gets the MaximizeSupport implementation describing behavior for minimize
     * and maximize.
     *
     * @return maximizeSupport that encapsulates behavior for minimize and
     *         maximize.
     */
    protected AbstractMaximizeSupport getMaximizeSupport() {
        return maximizeSupport;
    }

    /**
     * Checks whether maximize-support is enabled.
     *
     * @return <code>true</code> if maximize-support is enabled, otherwise
     *         <code>false</code>
     */
    protected boolean hasMaximizeSupport() {
        return maximizeSupport != null;
    }

    /**
     * Enables single-selection-support for the grid by setting
     * SingleSelectionSupport configuration.
     *
     * @param singleSelectionSupport
     *            encapsulates behavior for single-selection and offers some
     *            convenient functionality.
     */
    protected void setSingleSelectionSupport(final SingleSelectionSupport singleSelectionSupport) {
        this.singleSelectionSupport = singleSelectionSupport;
    }

    /**
     * Gets the SingleSelectionSupport implementation configuring
     * single-selection.
     *
     * @return singleSelectionSupport that configures single-selection.
     */
    protected SingleSelectionSupport getSingleSelectionSupport() {
        return singleSelectionSupport;
    }

    /**
     * Checks whether single-selection-support is enabled.
     *
     * @return <code>true</code> if single-selection-support is enabled,
     *         otherwise <code>false</code>
     */
    protected boolean hasSingleSelectionSupport() {
        return singleSelectionSupport != null;
    }

    /**
     * Enables details-support for the grid by setting DetailsSupport
     * configuration. If details-support is enabled, the grid handles
     * details-data that depends on a master-selection.
     *
     * @param detailsSupport
     *            encapsulates behavior for changes of master-selection.
     */
    protected void setDetailsSupport(final DetailsSupport detailsSupport) {
        this.detailsSupport = detailsSupport;
    }

    /**
     * Gets the DetailsSupport implementation configuring master-details
     * relation.
     *
     * @return detailsSupport that configures master-details relation.
     */
    public DetailsSupport getDetailsSupport() {
        return detailsSupport;
    }

    /**
     * Checks whether details-support is enabled.
     *
     * @return <code>true</code> if details-support is enabled, otherwise
     *         <code>false</code>
     */
    public boolean hasDetailsSupport() {
        return detailsSupport != null;
    }

    /**
     * Gets id of the grid.
     *
     * @return id of the grid
     */
    protected abstract String getGridId();

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

    /**
     * Support for master-details relation for grid. This means that grid
     * content (=details) is updated as soon as master-data changes.
     */
    public class DetailsSupport {

        private Long master;

        /**
         * Set selected master-data as member of this grid-support (as all
         * presented grid-data is related to this master-data) and re-calculate
         * grid-container-content.
         *
         * @param master
         *            id of selected action
         */
        public void populateMasterDataAndRecalculateContainer(final Long master) {
            this.master = master;
            recalculateContainer();
            populateSelection();
        }

        /**
         * Set selected master-data as member of this grid-support (as all
         * presented grid-data is related to this master-data) and re-create
         * grid-container.
         *
         * @param master
         *            id of selected action
         */
        public void populateMasterDataAndRecreateContainer(final Long master) {
            this.master = master;
            recreateContainer();
            populateSelection();
        }

        /**
         * Propagates the selection if needed.
         *
         */
        public void populateSelection() {
            if (!hasSingleSelectionSupport()) {
                return;
            }

            if (master == null) {
                getSingleSelectionSupport().clearSelection();
                return;
            }
            getSingleSelectionSupport().selectFirstRow();
        }

        /**
         * Gets the master-data id.
         *
         * @return master-data id
         */
        public Long getMasterDataId() {
            return master;
        }

        /**
         * Invalidates container-data (but reused container) and refreshes it
         * with new details-data for the new selected master-data.
         */
        private void recalculateContainer() {
            clearSortOrder();
            refreshContainer();
        }

        /**
         * Invalidates container and replace it with a fresh instance for the
         * new selected master-data.
         */
        private void recreateContainer() {
            removeAllColumns();
            clearSortOrder();
            // TODO: check if it is sufficient
            addColumns();
        }
    }

    /**
     * Via implementations of this support capability an expand-mode is provided
     * that maximizes the grid size.
     */
    public abstract class AbstractMaximizeSupport {

        /**
         * Renews the content for maximized layout.
         */
        public void createMaximizedContent() {
            setMaximizedColumnProperties();
            setMaximizedHiddenColumns();
            setMaximizedHeaders();
            setMaximizedColumnExpandRatio();
        }

        /**
         * Renews the content for minimized layout.
         */
        public void createMinimizedContent() {
            // TODO: implement the column creation for minimized case
        }

        /**
         * Sets the column properties for maximized-state.
         */
        protected abstract void setMaximizedColumnProperties();

        /**
         * Sets the hidden columns for maximized-state.
         */
        protected abstract void setMaximizedHiddenColumns();

        /**
         * Sets additional headers for maximized-state.
         */
        protected abstract void setMaximizedHeaders();

        /**
         * Sets column expand ratio for maximized-state.
         */
        protected abstract void setMaximizedColumnExpandRatio();
    }

    /**
     * Support for single selection on the grid.
     */
    public class SingleSelectionSupport {

        public SingleSelectionSupport() {
            enable();
        }

        public final void enable() {
            setSelectionMode(SelectionMode.SINGLE);
        }

        public final void disable() {
            setSelectionMode(SelectionMode.NONE);
        }

        /**
         * Selects the first row if available and enabled.
         */
        public void selectFirstRow() {
            if (!isSingleSelectionModel()) {
                return;
            }

            final int size = getDataProvider().size(new Query<>());
            if (size > 0) {
                final T firstItem = getDataProvider().fetch(new Query<>(0, 1, Collections.emptyList(), null, null))
                        .findFirst().orElse(null);
                getDataProvider().refreshItem(firstItem);
                getSelectionModel().select(firstItem);
            } else {
                getSelectionModel().select(null);
            }
        }

        private boolean isSingleSelectionModel() {
            return getSelectionModel() instanceof SingleSelectionModel;
        }

        /**
         * Clears the selection.
         */
        public void clearSelection() {
            if (!isSingleSelectionModel()) {
                return;
            }
            getSelectionModel().select(null);
        }
    }

    // TODO: check if we really need it or is it better to inline it with lambda
    // expression
    /**
     * Converter that gets time-data as input of type <code>Long</code> and
     * converts to a formatted date string.
     */
    public class LongToFormattedDateStringConverter implements Converter<String, Long> {
        private static final long serialVersionUID = 1247513913478717845L;

        @Override
        public Result<Long> convertToModel(final String value, final ValueContext context) {
            // not needed
            return null;
        }

        @Override
        public String convertToPresentation(final Long value, final ValueContext context) {
            return SPDateTimeUtil.getFormattedDate(value, SPUIDefinitions.LAST_QUERY_DATE_FORMAT_SHORT);
        }
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
