/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.smtable;

import java.util.Collection;

import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.providers.SmMetaDataDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractMetaDataWindowGrid;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;

/**
 * Grid for Software Module MetaData pop up layout.
 */
public class SmMetaDataWindowGrid extends AbstractMetaDataWindowGrid<Long> {
    private static final long serialVersionUID = 1L;

    private final SoftwareModuleManagement softwareModuleManagement;

    private final ConfigurableFilterDataProvider<ProxyMetaData, Void, Long> metaDataDataProvider;

    public SmMetaDataWindowGrid(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final SoftwareModuleManagement softwareModuleManagement) {
        super(i18n, eventBus, permissionChecker, notification);

        this.softwareModuleManagement = softwareModuleManagement;

        this.metaDataDataProvider = new SmMetaDataDataProvider(softwareModuleManagement).withConfigurableFilter();
    }

    @Override
    protected void deleteMetaData(final Collection<ProxyMetaData> metaDataToDelete) {
        if (masterEntityFilter != null && !CollectionUtils.isEmpty(metaDataToDelete)) {
            // as of now we only allow deletion of single metadata entry
            final String metaDataKey = metaDataToDelete.iterator().next().getKey();
            softwareModuleManagement.deleteMetaData(masterEntityFilter, metaDataKey);
        } else {
            // TODO: use i18n
            notification.displayValidationError(
                    "No software module is currently selected or metadata to delete is missing");
        }
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyMetaData, Void, Long> getFilterDataProvider() {
        return metaDataDataProvider;
    }
}
