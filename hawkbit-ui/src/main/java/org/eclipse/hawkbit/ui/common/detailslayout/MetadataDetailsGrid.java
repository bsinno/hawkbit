/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.function.Consumer;

import org.eclipse.hawkbit.ui.common.data.providers.AbstractMetaDataDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.cronutils.utils.StringUtils;
import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Metadata grid for entities.
 */
public class MetadataDetailsGrid<F> extends AbstractGrid<ProxyMetaData, F> {
    private static final long serialVersionUID = 1L;

    private static final String METADATA_KEY_ID = "Key";

    private final String detailLinkIdPrefix;
    private final transient Consumer<ProxyMetaData> showMetadataDetailsCallback;
    private final ConfigurableFilterDataProvider<ProxyMetaData, Void, F> metaDataDataProvider;

    public MetadataDetailsGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final String detailLinkIdPrefix, final Consumer<ProxyMetaData> showMetadataDetailsCallback,
            final AbstractMetaDataDataProvider<?, F> metaDataDataProvider) {
        super(i18n, eventBus);

        this.detailLinkIdPrefix = detailLinkIdPrefix;
        this.showMetadataDetailsCallback = showMetadataDetailsCallback;
        this.metaDataDataProvider = metaDataDataProvider.withConfigurableFilter();

        init();
    }

    @Override
    protected void init() {
        super.init();

        setHeaderVisible(false);
        setHeightMode(HeightMode.UNDEFINED);

        addStyleName("metadata-details");
        addStyleName(ValoTheme.TABLE_NO_STRIPES);
        addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        addStyleName(ValoTheme.TABLE_BORDERLESS);
        addStyleName(ValoTheme.TABLE_COMPACT);
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyMetaData, Void, F> getFilterDataProvider() {
        return metaDataDataProvider;
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.METDATA_TABLE_ID;
    }

    @Override
    public void addColumns() {
        addComponentColumn(this::buildKeyLink).setId(METADATA_KEY_ID).setCaption(i18n.getMessage("header.key"))
                .setExpandRatio(7);
    }

    // TODO: remove duplication with RolloutListGrid
    private Button buildKeyLink(final ProxyMetaData metaData) {
        final String metaDataKey = metaData.getKey();
        final String keyLinkId = new StringBuilder(detailLinkIdPrefix).append('.').append(metaDataKey).toString();
        final Button metaDataKeyLink = new Button();

        metaDataKeyLink.setId(keyLinkId);
        metaDataKeyLink.addStyleName("borderless");
        metaDataKeyLink.addStyleName("small");
        metaDataKeyLink.addStyleName("on-focus-no-border");
        metaDataKeyLink.addStyleName("link");
        metaDataKeyLink.setCaption(metaDataKey);
        // TODO: use i18n here
        metaDataKeyLink.setDescription("View " + metaDataKey + "  Metadata details");
        // this is to allow the button to disappear, if the text is null
        metaDataKeyLink.setVisible(!StringUtils.isEmpty(metaDataKey));

        metaDataKeyLink.addClickListener(event -> showMetadataDetailsCallback.accept(metaData));

        return metaDataKeyLink;
    }

    public void updateMasterEntityFilter(final F masterEntityFilter) {
        getFilterDataProvider().setFilter(masterEntityFilter);
    }
}
