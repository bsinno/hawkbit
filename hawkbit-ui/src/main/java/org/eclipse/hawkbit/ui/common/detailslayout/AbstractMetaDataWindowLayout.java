/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;

/**
 * Class for metadata add/update window layout.
 */
public abstract class AbstractMetaDataWindowLayout<F> extends HorizontalLayout {
    private static final long serialVersionUID = 1L;

    protected final VaadinMessageSource i18n;
    protected final UIEventBus eventBus;

    private final MetadataWindowGridHeader metadataWindowGridHeader;

    private Consumer<SaveDialogCloseListener> saveCallback;

    protected F masterEntityFilter;

    /**
     * Constructor for AbstractTagWindowLayout
     * 
     * @param i18n
     *            I18N
     */
    public AbstractMetaDataWindowLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permChecker) {
        this.i18n = i18n;
        this.eventBus = eventBus;

        this.metadataWindowGridHeader = new MetadataWindowGridHeader(i18n, permChecker, eventBus,
                this::showAddMetaDataLayout);
    }

    private void showAddMetaDataLayout() {
        getMetaDataWindowGrid().deselectAll();
        getAddMetaDataWindowController().populateWithData(null);
        saveCallback.accept(getAddMetaDataWindowController().getSaveDialogCloseListener());

        resetSaveButton();
    }

    protected abstract MetaDataWindowGrid<F> getMetaDataWindowGrid();

    public abstract AddMetaDataWindowController getAddMetaDataWindowController();

    // TODO: check if could be substituted by read/write bean and binder has
    // value changed validation
    private void resetSaveButton() {
        // used to disable save button after setting the initial bean values
        getMetaDataAddUpdateWindowLayout().getValidationCallback()
                .ifPresent(validationCallback -> validationCallback.accept(false));
    }

    public abstract MetaDataAddUpdateWindowLayout getMetaDataAddUpdateWindowLayout();

    public void addValidationListener(final Consumer<Boolean> validationCallback) {
        getMetaDataAddUpdateWindowLayout().addValidationListener(validationCallback);
    }

    public void setMasterEntityFilter(final F masterEntityFilter, final ProxyMetaData metaData) {
        this.masterEntityFilter = masterEntityFilter;

        getMetaDataWindowGrid().updateMasterEntityFilter(masterEntityFilter);

        if (metaData == null) {
            if (!getMetaDataWindowGrid().getSelectionSupport().selectFirstRow()) {
                showAddMetaDataLayout();
            }
        } else {
            getMetaDataWindowGrid().select(metaData);
        }
    }

    public void setSaveCallback(final Consumer<SaveDialogCloseListener> saveCallback) {
        this.saveCallback = saveCallback;
    }

    protected void buildLayout() {
        setSpacing(true);
        setMargin(false);
        setSizeFull();

        final MetaDataWindowGridLayout metaDataWindowGridLayout = new MetaDataWindowGridLayout(i18n, eventBus,
                metadataWindowGridHeader, getMetaDataWindowGrid());
        final ComponentContainer addUpdateWindowLayout = getMetaDataAddUpdateWindowLayout().getRootComponent();

        addComponent(metaDataWindowGridLayout);
        addComponent(addUpdateWindowLayout);

        setExpandRatio(metaDataWindowGridLayout, 0.5F);
        setExpandRatio(addUpdateWindowLayout, 0.5F);
    }

    protected void addGridSelectionListener() {
        getMetaDataWindowGrid().addSelectionListener(event -> {
            final Optional<ProxyMetaData> selectedEntity = event.getFirstSelectedItem();
            selectedEntity.ifPresent(this::showEditMetaDataLayout);

            if (!selectedEntity.isPresent()) {
                showAddMetaDataLayout();
            }
        });
    }

    private void showEditMetaDataLayout(final ProxyMetaData proxyEntity) {
        getUpdateMetaDataWindowController().populateWithData(proxyEntity);
        saveCallback.accept(getUpdateMetaDataWindowController().getSaveDialogCloseListener());

        resetSaveButton();
    }

    public abstract UpdateMetaDataWindowController getUpdateMetaDataWindowController();

    protected void onMetaDataModified(final ProxyMetaData metaData) {
        getMetaDataWindowGrid().refreshContainer();
        getMetaDataWindowGrid().select(metaData);

        resetSaveButton();
    }

    private static class MetaDataWindowGridLayout extends AbstractGridComponentLayout {
        private static final long serialVersionUID = 1L;

        public MetaDataWindowGridLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
                final MetadataWindowGridHeader metadataWindowGridHeader,
                final MetaDataWindowGrid<?> metaDataWindowGrid) {
            super(i18n, eventBus);

            super.buildLayout(metadataWindowGridHeader, metaDataWindowGrid);
        }
    }
}
