/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import org.eclipse.hawkbit.ui.common.grid.header.support.HeaderSupport;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

public class MetaDataDetailsHeaderSupport implements HeaderSupport {
    private final VaadinMessageSource i18n;

    private final String metaDataIconId;
    private final Runnable showItemMetaDataCallback;

    private final Button metaDataIcon;

    public MetaDataDetailsHeaderSupport(final VaadinMessageSource i18n, final String metaDataIconId,
            final Runnable showItemMetaDataCallback) {
        this.i18n = i18n;
        this.metaDataIconId = metaDataIconId;
        this.showItemMetaDataCallback = showItemMetaDataCallback;

        this.metaDataIcon = createMetaDataIcon();
    }

    private Button createMetaDataIcon() {
        final Button metaDataButton = SPUIComponentProvider.getButton("", "",
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_METADATA_ICON), null, false, VaadinIcons.LIST,
                SPUIButtonStyleNoBorder.class);

        metaDataButton.setId(metaDataIconId);
        metaDataButton.addClickListener(event -> showItemMetaDataCallback.run());
        metaDataButton.setEnabled(false);

        return metaDataButton;
    }

    @Override
    public Component getHeaderComponent() {
        return metaDataIcon;
    }

    public void enableMetaDataIcon() {
        metaDataIcon.setEnabled(true);
    }

    public void disableMetaDataIcon() {
        metaDataIcon.setEnabled(false);
    }
}
