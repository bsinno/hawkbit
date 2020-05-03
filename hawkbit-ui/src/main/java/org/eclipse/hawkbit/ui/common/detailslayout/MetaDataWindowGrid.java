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
import java.util.function.Predicate;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.GridComponentBuilder;
import org.eclipse.hawkbit.ui.common.data.providers.AbstractMetaDataDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;

/**
 * Grid for MetaData pop up layout.
 */
public class MetaDataWindowGrid<F> extends AbstractGrid<ProxyMetaData, F> implements MasterEntityAwareComponent<F> {
    private static final long serialVersionUID = 1L;

    public static final String META_DATA_KEY_ID = "metaDataKey";
    public static final String META_DATA_VALUE_ID = "metaDataValue";
    public static final String META_DATA_DELETE_BUTTON_ID = "metaDataDeleteButton";

    private final ConfigurableFilterDataProvider<ProxyMetaData, Void, F> metaDataDataProvider;

    private final transient DeleteSupport<ProxyMetaData> metaDataDeleteSupport;

    public MetaDataWindowGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final AbstractMetaDataDataProvider<?, F> dataProvider,
            final Predicate<Collection<ProxyMetaData>> itemsDeletionCallback) {
        super(i18n, eventBus, permissionChecker);

        this.metaDataDataProvider = dataProvider.withConfigurableFilter();

        this.metaDataDeleteSupport = new DeleteSupport<>(this, i18n, notification, i18n.getMessage("caption.metadata"),
                ProxyMetaData::getKey, itemsDeletionCallback,
                UIComponentIdProvider.METADATA_DELETE_CONFIRMATION_DIALOG);

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
    public String getGridId() {
        return UIComponentIdProvider.METDATA_WINDOW_TABLE_ID;
    }

    @Override
    public void addColumns() {
        addColumn(ProxyMetaData::getKey).setId(META_DATA_KEY_ID).setCaption(i18n.getMessage("header.key"))
                .setExpandRatio(1);

        addColumn(ProxyMetaData::getValue).setId(META_DATA_VALUE_ID).setCaption(i18n.getMessage("header.value"))
                .setHidden(true);

        addComponentColumn(metaData -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> metaDataDeleteSupport.openConfirmationWindowDeleteAction(metaData), VaadinIcons.TRASH,
                UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.META_DATA_DELET_ICON + "." + metaData.getId(),
                permissionChecker.hasDeleteRepositoryPermission())).setId(META_DATA_DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.delete")).setMinimumWidth(50d).setMaximumWidth(50d)
                        .setHidable(false).setHidden(false);
    }

    @Override
    public void masterEntityChanged(final F masterEntity) {
        getFilterDataProvider().setFilter(masterEntity);
    }
}
