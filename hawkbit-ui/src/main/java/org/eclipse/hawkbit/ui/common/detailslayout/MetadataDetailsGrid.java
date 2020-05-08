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
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.cronutils.utils.StringUtils;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Metadata grid for entities.
 */
public class MetadataDetailsGrid<F> extends AbstractGrid<ProxyMetaData, F> implements MasterEntityAwareComponent<F> {
    private static final long serialVersionUID = 1L;

    private static final String METADATA_KEY_ID = "Key";

    private final String typePrefix;
    private final transient Consumer<ProxyMetaData> showMetadataDetailsCallback;

    public MetadataDetailsGrid(final VaadinMessageSource i18n, final UIEventBus eventBus, final String typePrefix,
            final Consumer<ProxyMetaData> showMetadataDetailsCallback,
            final AbstractMetaDataDataProvider<?, F> metaDataDataProvider) {
        super(i18n, eventBus);

        this.typePrefix = typePrefix;
        this.showMetadataDetailsCallback = showMetadataDetailsCallback;
        setFilterSupport(new FilterSupport<>(metaDataDataProvider));

        init();
        setVisible(false);
    }

    @Override
    public void init() {
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
    public String getGridId() {
        return typePrefix + "." + UIComponentIdProvider.METDATA_DETAILS_TABLE_ID;
    }

    @Override
    public void addColumns() {
        addComponentColumn(this::buildKeyLink).setId(METADATA_KEY_ID).setCaption(i18n.getMessage("header.key"))
                .setExpandRatio(7);
    }

    // TODO: remove duplication with RolloutListGrid
    private Button buildKeyLink(final ProxyMetaData metaData) {
        final String metaDataKey = metaData.getKey();
        final String keyLinkId = new StringBuilder(typePrefix).append('.')
                .append(UIComponentIdProvider.METADATA_DETAIL_LINK).append('.').append(metaDataKey).toString();
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

    @Override
    public void masterEntityChanged(final F masterEntity) {
        getFilterSupport().setFilter(masterEntity);
        getFilterSupport().refreshFilter();

        setVisible(masterEntity != null);
    }
}
