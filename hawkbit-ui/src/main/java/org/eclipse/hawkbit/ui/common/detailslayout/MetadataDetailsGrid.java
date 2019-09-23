/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.hawkbit.repository.model.MetaData;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.cronutils.utils.StringUtils;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Metadata grid for entities.
 */
public class MetadataDetailsGrid extends Grid<ProxyMetaData> {
    private static final long serialVersionUID = 1L;

    public static final int MAX_METADATA_QUERY = 500;

    private static final String METADATA_KEY_ID = "Key";

    private final List<ProxyMetaData> metaDataList;

    private final VaadinMessageSource i18n;
    private final String detailLinkIdPrefix;
    private final transient Consumer<String> showMetadataDetailsCallback;

    public MetadataDetailsGrid(final VaadinMessageSource i18n, final String detailLinkIdPrefix,
            final Consumer<String> showMetadataDetailsCallback) {
        this.i18n = i18n;
        this.detailLinkIdPrefix = detailLinkIdPrefix;
        this.showMetadataDetailsCallback = showMetadataDetailsCallback;
        this.metaDataList = new ArrayList<>();

        init();
    }

    private void init() {
        addStyleName(ValoTheme.TABLE_NO_HORIZONTAL_LINES);
        addStyleName(ValoTheme.TABLE_NO_STRIPES);
        setSelectionMode(SelectionMode.NONE);
        setSizeFull();
        // same as height of other tabs in details tabsheet
        // setHeight(116, Unit.PIXELS);

        addColumns();

        setItems(metaDataList);
    }

    private void addColumns() {
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

        metaDataKeyLink.addClickListener(event -> showMetadataDetailsCallback.accept(metaDataKey));

        return metaDataKeyLink;
    }

    public void addMetaDataToList(final MetaData metaData) {
        final ProxyMetaData metaDataItem = new ProxyMetaData();

        metaDataItem.setEntityId(metaData.getEntityId());
        metaDataItem.setKey(metaData.getKey());
        metaDataItem.setValue(metaData.getValue());

        metaDataList.add(metaDataItem);
    }

    public void clearData() {
        metaDataList.clear();
        // TODO: should we call refreshAll here?
    }
}
