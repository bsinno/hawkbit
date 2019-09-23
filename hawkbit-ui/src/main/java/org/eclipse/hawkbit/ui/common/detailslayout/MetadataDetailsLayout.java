/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.eclipse.hawkbit.repository.model.MetaData;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

/**
 * Target Metadata details layout.
 *
 */
public class MetadataDetailsLayout<T extends ProxyIdentifiableEntity> extends CustomField<T> {
    private static final long serialVersionUID = 1L;

    private final MetadataDetailsGrid metadataDetailsGrid;

    private final transient Function<T, List<? extends MetaData>> entityMetaDataSupplier;

    private T selectedEntity;

    /**
     * Initialize the layout.
     * 
     * @param i18n
     *            the i18n service
     */
    public MetadataDetailsLayout(final VaadinMessageSource i18n, final String detailLinkIdPrefix,
            final Consumer<String> showMetadataDetailsCallback,
            final Function<T, List<? extends MetaData>> entityMetaDataSupplier) {
        this.metadataDetailsGrid = new MetadataDetailsGrid(i18n, detailLinkIdPrefix, showMetadataDetailsCallback);
        this.entityMetaDataSupplier = entityMetaDataSupplier;
    }

    @Override
    public T getValue() {
        return selectedEntity;
    }

    @Override
    protected Component initContent() {
        return metadataDetailsGrid;
    }

    @Override
    protected void doSetValue(final T value) {
        selectedEntity = value;
        populateMetadata();
    }

    private void populateMetadata() {
        if (selectedEntity == null) {
            metadataDetailsGrid.setVisible(false);
            metadataDetailsGrid.clearData();
        } else {
            metadataDetailsGrid.setVisible(true);
            entityMetaDataSupplier.apply(selectedEntity).forEach(metadataDetailsGrid::addMetaDataToList);
            // TODO: should we call refreshAll here?
        }
    }
}
