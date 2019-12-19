/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import org.eclipse.hawkbit.ui.common.AbstractEntityWindowBuilder;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Window;

public abstract class AbstractMetaDataWindowBuilder<F> extends AbstractEntityWindowBuilder<ProxyMetaData> {

    public AbstractMetaDataWindowBuilder(final VaadinMessageSource i18n) {
        super(i18n);
    }

    @Override
    protected String getWindowId() {
        return UIComponentIdProvider.METADATA_POPUP_ID;
    }

    protected Window getWindowForShowMetaData(final AbstractMetaDataWindowLayout<F> metaDataWindowLayout,
            final F selectedEntityFilter, final ProxyMetaData proxyMetaData) {
        final CommonDialogWindow window = createWindow(metaDataWindowLayout, null);

        metaDataWindowLayout.addValidationListener(window::setSaveButtonEnabled);
        metaDataWindowLayout.setSaveCallback(window::setCloseListener);
        metaDataWindowLayout.setMasterEntityFilter(selectedEntityFilter, proxyMetaData);

        window.setHeight(600, Unit.PIXELS);
        window.setWidth(800, Unit.PIXELS);

        return window;
    }
}
