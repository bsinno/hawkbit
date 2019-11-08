/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.Collection;
import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.providers.AbstractMetaDataDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;

/**
 * Grid for MetaData pop up layout.
 */
public class MetaDataWindowGrid<F> extends AbstractGrid<ProxyMetaData, F> {
    private static final long serialVersionUID = 1L;

    public static final String META_DATA_KEY_ID = "metaDataKey";
    public static final String META_DATA_VALUE_ID = "metaDataValue";
    public static final String META_DATA_DELETE_BUTTON_ID = "metaDataDeleteButton";

    private final ConfigurableFilterDataProvider<ProxyMetaData, Void, F> metaDataDataProvider;

    private final DeleteSupport<ProxyMetaData> metaDataDeleteSupport;

    public MetaDataWindowGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final AbstractMetaDataDataProvider<?, F> dataProvider,
            final Consumer<Collection<ProxyMetaData>> itemsDeletionCallback) {
        super(i18n, eventBus, permissionChecker);

        this.metaDataDataProvider = dataProvider.withConfigurableFilter();

        this.metaDataDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("caption.metadata"),
                permissionChecker, notification, itemsDeletionCallback);

        // TODO: we don't need to send selection events, because details layout
        // is part of MetaData Window
        setSelectionSupport(new SelectionSupport<ProxyMetaData>(this));
        getSelectionSupport().enableSingleSelection();

        init();
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyMetaData, Void, F> getFilterDataProvider() {
        return metaDataDataProvider;
    }

    @Override
    protected boolean doSubscribeToEventBus() {
        return false;
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.METDATA_TABLE_ID;
    }

    @Override
    public void addColumns() {
        addColumn(ProxyMetaData::getKey).setId(META_DATA_KEY_ID).setCaption(i18n.getMessage("header.key"))
                .setExpandRatio(1);

        addColumn(ProxyMetaData::getValue).setId(META_DATA_VALUE_ID).setCaption(i18n.getMessage("header.value"))
                .setHidden(true);

        addComponentColumn(metaData -> buildActionButton(
                clickEvent -> metaDataDeleteSupport.openConfirmationWindowDeleteAction(metaData, metaData.getKey()),
                VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.META_DATA_DELET_ICON + "." + metaData.getId(),
                metaDataDeleteSupport.hasDeletePermission())).setId(META_DATA_DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.delete")).setMinimumWidth(50d).setMaximumWidth(50d)
                        .setHidable(false).setHidden(false);
    }

    // TODO: remove duplication
    private Button buildActionButton(final ClickListener clickListener, final VaadinIcons icon,
            final String descriptionProperty, final String style, final String buttonId, final boolean enabled) {
        final Button actionButton = new Button();

        actionButton.addClickListener(clickListener);
        actionButton.setIcon(icon);
        actionButton.setDescription(i18n.getMessage(descriptionProperty));
        actionButton.setEnabled(enabled);
        actionButton.setId(buttonId);
        actionButton.addStyleName("tiny");
        actionButton.addStyleName("borderless");
        actionButton.addStyleName("button-no-border");
        actionButton.addStyleName("action-type-padding");
        actionButton.addStyleName(style);

        return actionButton;
    }

    public void updateMasterEntityFilter(final F masterEntityFilter) {
        getFilterDataProvider().setFilter(masterEntityFilter);
    }
}
