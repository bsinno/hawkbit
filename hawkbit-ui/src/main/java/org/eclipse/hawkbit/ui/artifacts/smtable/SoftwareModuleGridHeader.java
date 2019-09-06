/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.event.RefreshSoftwareModuleByFilterEvent;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent;
import org.eclipse.hawkbit.ui.artifacts.event.UploadArtifactUIEvent;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.AddHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.FilterButtonsHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Header of Software module table.
 */
public class SoftwareModuleGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final ArtifactUploadState artifactUploadState;

    private final SoftwareModuleAddUpdateWindow softwareModuleAddUpdateWindow;

    private final transient SearchHeaderSupport searchHeaderSupport;
    private final transient FilterButtonsHeaderSupport filterButtonsHeaderSupport;
    private final transient AddHeaderSupport addHeaderSupport;
    private final transient ResizeHeaderSupport resizeHeaderSupport;

    SoftwareModuleGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final ArtifactUploadState artifactUploadState,
            final SoftwareModuleAddUpdateWindow softwareModuleAddUpdateWindow) {
        super(i18n, permChecker, eventBus);

        this.artifactUploadState = artifactUploadState;

        this.softwareModuleAddUpdateWindow = softwareModuleAddUpdateWindow;

        this.searchHeaderSupport = new SearchHeaderSupport(i18n, UIComponentIdProvider.SW_MODULE_SEARCH_TEXT_FIELD,
                UIComponentIdProvider.SW_MODULE_SEARCH_RESET_ICON, this::getSearchTextFromUiState, this::searchBy,
                this::resetSearchText);
        this.filterButtonsHeaderSupport = new FilterButtonsHeaderSupport(i18n, UIComponentIdProvider.SHOW_SM_TYPE_ICON,
                this::showFilterButtonsLayout, this::onLoadIsShowFilterButtonDisplayed);
        // TODO: consider moving permission check to header support or parent
        // header
        if (permChecker.hasCreateRepositoryPermission()) {
            this.addHeaderSupport = new AddHeaderSupport(i18n, UIComponentIdProvider.SW_MODULE_ADD_BUTTON,
                    this::addNewItem, this::onLoadIsTableMaximized);
        } else {
            this.addHeaderSupport = null;
        }
        this.resizeHeaderSupport = new ResizeHeaderSupport(i18n, UIComponentIdProvider.SW_MAX_MIN_TABLE_ICON,
                this::maximizeTable, this::minimizeTable, this::onLoadIsTableMaximized);
        addHeaderSupports(
                Arrays.asList(searchHeaderSupport, filterButtonsHeaderSupport, addHeaderSupport, resizeHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("upload.swModuleTable.header")).buildCaptionLabel();
    }

    private String getSearchTextFromUiState() {
        return artifactUploadState.getSoftwareModuleFilters().getSearchText().orElse(null);
    }

    private void searchBy(final String newSearchText) {
        artifactUploadState.getSoftwareModuleFilters().setSearchText(newSearchText);
        eventBus.publish(this, new RefreshSoftwareModuleByFilterEvent());
    }

    // TODO: check if needed or can be done by searchBy
    private void resetSearchText() {
        if (artifactUploadState.getSoftwareModuleFilters().getSearchText().isPresent()) {
            artifactUploadState.getSoftwareModuleFilters().setSearchText(null);
            eventBus.publish(this, new RefreshSoftwareModuleByFilterEvent());
        }
    }

    private void showFilterButtonsLayout() {
        artifactUploadState.setSwTypeFilterClosed(false);
        eventBus.publish(this, UploadArtifactUIEvent.SHOW_FILTER_BY_TYPE);
    }

    private Boolean onLoadIsShowFilterButtonDisplayed() {
        return artifactUploadState.isSwTypeFilterClosed();
    }

    private void addNewItem() {
        final Window addSoftwareModule = softwareModuleAddUpdateWindow.createAddSoftwareModuleWindow();
        addSoftwareModule.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.software.module")));
        UI.getCurrent().addWindow(addSoftwareModule);
        addSoftwareModule.setVisible(Boolean.TRUE);
    }

    private Boolean onLoadIsTableMaximized() {
        return artifactUploadState.isSwModuleTableMaximized();
    }

    private void maximizeTable() {
        if (addHeaderSupport != null) {
            addHeaderSupport.hideAddIcon();
        }

        artifactUploadState.setSwModuleTableMaximized(Boolean.TRUE);
        eventBus.publish(this, new SoftwareModuleEvent(BaseEntityEventType.MAXIMIZED));
    }

    private void minimizeTable() {
        if (addHeaderSupport != null) {
            addHeaderSupport.showAddIcon();
        }

        artifactUploadState.setSwModuleTableMaximized(Boolean.FALSE);
        eventBus.publish(this, new SoftwareModuleEvent(BaseEntityEventType.MINIMIZED));
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final UploadArtifactUIEvent event) {
        if (event == UploadArtifactUIEvent.HIDE_FILTER_BY_TYPE) {
            filterButtonsHeaderSupport.showFilterButtonsIcon();
        }
    }
}
