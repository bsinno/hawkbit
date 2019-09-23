/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.distributions.smtable.SwMetadataPopupLayout;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.UI;

/**
 * SoftwareModule Metadata details layout.
 *
 */
public class SoftwareModuleMetadataDetailsLayout extends CustomField<ProxySoftwareModule> {
    private static final long serialVersionUID = 1L;

    private final MetadataDetailsGrid metadataDetailsGrid;

    private transient SoftwareModuleManagement softwareModuleManagement;

    private final SwMetadataPopupLayout swMetadataPopupLayout;

    private ProxySoftwareModule selectedSWModule;

    /**
     * Initialize the layout.
     * 
     * @param i18n
     *            the i18n service
     * @param softwareManagement
     *            the software management service
     * @param swMetadataPopupLayout
     *            the software module metadata popup layout
     */
    public SoftwareModuleMetadataDetailsLayout(final VaadinMessageSource i18n,
            final SoftwareModuleManagement softwareManagement, final SwMetadataPopupLayout swMetadataPopupLayout) {
        this.softwareModuleManagement = softwareManagement;
        this.swMetadataPopupLayout = swMetadataPopupLayout;

        this.metadataDetailsGrid = new MetadataDetailsGrid(i18n, UIComponentIdProvider.SW_METADATA_DETAIL_LINK,
                this::showMetadataDetails);
    }

    private void showMetadataDetails(final String metadataKey) {
        softwareModuleManagement.get(selectedSWModule.getId()).ifPresent(
                swmodule -> UI.getCurrent().addWindow(swMetadataPopupLayout.getWindow(swmodule, metadataKey)));
    }

    @Override
    public ProxySoftwareModule getValue() {
        return new ProxySoftwareModule();
    }

    @Override
    protected Component initContent() {
        return metadataDetailsGrid;
    }

    @Override
    protected void doSetValue(final ProxySoftwareModule value) {
        selectedSWModule = value;
        populateMetadata();
    }

    private void populateMetadata() {
        if (selectedSWModule == null) {
            metadataDetailsGrid.setVisible(false);
            metadataDetailsGrid.clearData();
        } else {
            metadataDetailsGrid.setVisible(true);
            softwareModuleManagement
                    .findMetaDataBySoftwareModuleId(PageRequest.of(0, MetadataDetailsGrid.MAX_METADATA_QUERY),
                            selectedSWModule.getId())
                    .getContent().forEach(metadataDetailsGrid::addMetaDataToList);
            // TODO: should we call refreshAll here?
        }
    }
}
