/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.distributions.dstable.DsMetadataPopupLayout;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.UI;

/**
 * DistributionSet Metadata details layout.
 *
 */
public class DistributionSetMetadataDetailsLayout extends CustomField<ProxyDistributionSet> {
    private static final long serialVersionUID = 1L;

    private final MetadataDetailsGrid metadataDetailsGrid;

    private final transient DistributionSetManagement distributionSetManagement;

    private final DsMetadataPopupLayout dsMetadataPopupLayout;

    private ProxyDistributionSet selectedDistSet;

    /**
     * Initialize the layout.
     * 
     * @param i18n
     *            the i18n service
     * @param distributionSetManagement
     *            the distribution set management service
     * @param dsMetadataPopupLayout
     *            the distribution set metadata popup layout
     */
    public DistributionSetMetadataDetailsLayout(final VaadinMessageSource i18n,
            final DistributionSetManagement distributionSetManagement,
            final DsMetadataPopupLayout dsMetadataPopupLayout) {
        this.distributionSetManagement = distributionSetManagement;
        this.dsMetadataPopupLayout = dsMetadataPopupLayout;

        this.metadataDetailsGrid = new MetadataDetailsGrid(i18n, UIComponentIdProvider.DS_METADATA_DETAIL_LINK,
                this::showMetadataDetails);
    }

    private void showMetadataDetails(final String metadataKey) {
        distributionSetManagement.get(selectedDistSet.getId())
                .ifPresent(distSet -> UI.getCurrent().addWindow(dsMetadataPopupLayout.getWindow(distSet, metadataKey)));
    }

    @Override
    public ProxyDistributionSet getValue() {
        return new ProxyDistributionSet();
    }

    @Override
    protected Component initContent() {
        return metadataDetailsGrid;
    }

    @Override
    protected void doSetValue(final ProxyDistributionSet value) {
        selectedDistSet = value;
        populateMetadata();
    }

    private void populateMetadata() {
        if (selectedDistSet == null) {
            metadataDetailsGrid.setVisible(false);
            metadataDetailsGrid.clearData();
        } else {
            metadataDetailsGrid.setVisible(true);
            distributionSetManagement
                    .findMetaDataByDistributionSetId(PageRequest.of(0, MetadataDetailsGrid.MAX_METADATA_QUERY),
                            selectedDistSet.getId())
                    .getContent().forEach(metadataDetailsGrid::addMetaDataToList);
            // TODO: should we call refreshAll here?
        }
    }
}
