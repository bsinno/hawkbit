/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.distributions.dstable;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractMetaDataWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Window;

public class DsMetaDataWindowBuilder extends AbstractMetaDataWindowBuilder<Long> {
    private final EntityFactory entityFactory;
    private final UIEventBus eventBus;
    private final UINotification uiNotification;
    private final SpPermissionChecker permChecker;

    private final DistributionSetManagement dsManagement;

    public DsMetaDataWindowBuilder(final VaadinMessageSource i18n, final EntityFactory entityFactory,
            final UIEventBus eventBus, final UINotification uiNotification, final SpPermissionChecker permChecker,
            final DistributionSetManagement dsManagement) {
        super(i18n);

        this.entityFactory = entityFactory;
        this.eventBus = eventBus;
        this.uiNotification = uiNotification;
        this.permChecker = permChecker;

        this.dsManagement = dsManagement;
    }

    public Window getWindowForShowDsMetaData(final Long dsId) {
        return getWindowForShowMetaData(
                new DsMetaDataWindowLayout(i18n, eventBus, permChecker, uiNotification, entityFactory, dsManagement),
                dsId, null);
    }

    public Window getWindowForShowDsMetaData(final Long dsId, final ProxyMetaData proxyMetaData) {
        return getWindowForShowMetaData(
                new DsMetaDataWindowLayout(i18n, eventBus, permChecker, uiNotification, entityFactory, dsManagement),
                dsId, proxyMetaData);
    }
}
