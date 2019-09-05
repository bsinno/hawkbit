/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.header;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.grid.header.support.AddHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.BulkUploadHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.CloseHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.CrudMenuHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.FilterButtonsHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
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

    private transient SearchHeaderSupport searchHeaderSupport;
    private transient AddHeaderSupport addHeaderSupport;
    private transient CloseHeaderSupport closeHeaderSupport;
    private transient FilterButtonsHeaderSupport filterButtonsHeaderSupport;
    private transient ResizeHeaderSupport resizeHeaderSupport;
    private transient BulkUploadHeaderSupport bulkUploadHeaderSupport;
    private transient CrudMenuHeaderSupport crudMenuHeaderSupport;

    public AbstractGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permissionChecker,
            final UIEventBus eventBus) {
        this.i18n = i18n;
        this.permissionChecker = permissionChecker;
        this.eventBus = eventBus;

        this.headerCaption = getHeaderCaption();

        init();
        if (doSubscribeToEventBus()) {
            eventBus.subscribe(this);
        }
    }

    protected abstract Component getHeaderCaption();

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

        if (searchHeaderSupport != null) {

        }

        if (filterButtonsHeaderSupport != null) {

        }

        if (addHeaderSupport != null) {

        }

        if (bulkUploadHeaderSupport != null) {

        }

        if (resizeHeaderSupport != null) {

        }

        if (crudMenuHeaderSupport != null) {

        }

        if (closeHeaderSupport != null) {

        }
    }

    public SearchHeaderSupport getSearchHeaderSupport() {
        return searchHeaderSupport;
    }

    public void setSearchHeaderSupport(final SearchHeaderSupport searchHeaderSupport) {
        this.searchHeaderSupport = searchHeaderSupport;
    }

    public AddHeaderSupport getAddHeaderSupport() {
        return addHeaderSupport;
    }

    public void setAddHeaderSupport(final AddHeaderSupport addHeaderSupport) {
        this.addHeaderSupport = addHeaderSupport;
    }

    public CloseHeaderSupport getCloseHeaderSupport() {
        return closeHeaderSupport;
    }

    public void setCloseHeaderSupport(final CloseHeaderSupport closeHeaderSupport) {
        this.closeHeaderSupport = closeHeaderSupport;
    }

    public FilterButtonsHeaderSupport getFilterButtonsHeaderSupport() {
        return filterButtonsHeaderSupport;
    }

    public void setFilterButtonsHeaderSupport(final FilterButtonsHeaderSupport filterButtonsHeaderSupport) {
        this.filterButtonsHeaderSupport = filterButtonsHeaderSupport;
    }

    public ResizeHeaderSupport getResizeHeaderSupport() {
        return resizeHeaderSupport;
    }

    public void setResizeHeaderSupport(final ResizeHeaderSupport resizeHeaderSupport) {
        this.resizeHeaderSupport = resizeHeaderSupport;
    }

    public BulkUploadHeaderSupport getBulkUploadHeaderSupport() {
        return bulkUploadHeaderSupport;
    }

    public void setBulkUploadHeaderSupport(final BulkUploadHeaderSupport bulkUploadHeaderSupport) {
        this.bulkUploadHeaderSupport = bulkUploadHeaderSupport;
    }

    public CrudMenuHeaderSupport getCrudMenuHeaderSupport() {
        return crudMenuHeaderSupport;
    }

    public void setCrudMenuHeaderSupport(final CrudMenuHeaderSupport crudMenuHeaderSupport) {
        this.crudMenuHeaderSupport = crudMenuHeaderSupport;
    }
}
