/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;

import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.ui.artifacts.smtable.SmMetaDataWindowBuilder;
import org.eclipse.hawkbit.ui.common.data.providers.SmMetaDataDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyKeyValueDetails;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Abstract class which contains common code for Software Module Details
 *
 */
public abstract class AbstractSoftwareModuleDetails extends AbstractGridDetailsLayout<ProxySoftwareModule> {
    private static final long serialVersionUID = 1L;

    private final MetadataDetailsGrid<Long> smMetadataGrid;

    private final transient SmMetaDataWindowBuilder smMetaDataWindowBuilder;

    protected AbstractSoftwareModuleDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SoftwareModuleManagement softwareManagement, final SmMetaDataWindowBuilder smMetaDataWindowBuilder) {
        super(i18n);

        this.smMetaDataWindowBuilder = smMetaDataWindowBuilder;

        this.smMetadataGrid = new MetadataDetailsGrid<>(i18n, eventBus, UIComponentIdProvider.SW_METADATA_DETAIL_LINK,
                this::showMetadataDetails, new SmMetaDataDataProvider(softwareManagement));

        addDetailsComponents(Arrays.asList(new SimpleEntry<>(i18n.getMessage("caption.tab.details"), entityDetails),
                new SimpleEntry<>(i18n.getMessage("caption.tab.description"), entityDescription),
                new SimpleEntry<>(i18n.getMessage("caption.logs.tab"), logDetails),
                new SimpleEntry<>(i18n.getMessage("caption.metadata"), smMetadataGrid)));
    }

    @Override
    protected String getTabSheetId() {
        return UIComponentIdProvider.DIST_SW_MODULE_DETAILS_TABSHEET_ID;
    }

    @Override
    protected List<ProxyKeyValueDetails> getEntityDetails(final ProxySoftwareModule entity) {
        return Arrays.asList(
                new ProxyKeyValueDetails(UIComponentIdProvider.DETAILS_VENDOR_LABEL_ID,
                        i18n.getMessage("label.dist.details.vendor"), entity.getVendor()),
                new ProxyKeyValueDetails(UIComponentIdProvider.DETAILS_TYPE_LABEL_ID,
                        i18n.getMessage("label.dist.details.type"), entity.getProxyType().getName()),
                new ProxyKeyValueDetails(UIComponentIdProvider.SWM_DTLS_MAX_ASSIGN,
                        i18n.getMessage("label.assigned.type"),
                        entity.getProxyType().getMaxAssignments() == 1 ? i18n.getMessage("label.singleAssign.type")
                                : i18n.getMessage("label.multiAssign.type")));
    }

    @Override
    protected String getDetailsDescriptionId() {
        return UIComponentIdProvider.SM_DETAILS_DESCRIPTION_LABEL_ID;
    }

    @Override
    protected String getLogLabelIdPrefix() {
        // TODO: fix with constant
        return "sm.";
    }

    private void showMetadataDetails(final ProxyMetaData metadata) {
        if (binder.getBean() == null) {
            return;
        }

        final Window metaDataWindow = smMetaDataWindowBuilder.getWindowForShowSmMetaData(binder.getBean().getId(),
                metadata);

        metaDataWindow.setCaption(i18n.getMessage("caption.metadata.popup") + binder.getBean().getNameAndVersion());
        UI.getCurrent().addWindow(metaDataWindow);
        metaDataWindow.setVisible(Boolean.TRUE);
    }

    @Override
    public void masterEntityChanged(final ProxySoftwareModule entity) {
        super.masterEntityChanged(entity);

        // TODO: consider populating the grid only when metadata tab is/becomes
        // active (lazy loading)
        smMetadataGrid.updateMasterEntityFilter(entity != null ? entity.getId() : null);
    }
}
