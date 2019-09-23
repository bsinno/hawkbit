/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.Arrays;
import java.util.function.BooleanSupplier;

import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

/**
 * Header for entity details with edit and metadata support.
 */
public class DetailsHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final String detailsHeaderCaptionId;
    private final String entityType;

    private final Label headerCaption;

    private final transient EditDetailsHeaderSupport editDetailsHeaderSupport;
    private final transient MetaDataDetailsHeaderSupport metaDataDetailsHeaderSupport;

    public DetailsHeader(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final BooleanSupplier editPermissionSupplier, final String entityType, final String detailsHeaderCaptionId,
            final String editIconId, final Runnable editItemCallback, final String metaDataIconId,
            final Runnable showItemMetaDataCallback) {
        super(i18n, null, eventBus);

        this.detailsHeaderCaptionId = detailsHeaderCaptionId;
        this.entityType = entityType;

        this.headerCaption = buildHeaderCaption();

        if (editPermissionSupplier.getAsBoolean()) {
            this.editDetailsHeaderSupport = new EditDetailsHeaderSupport(i18n, editIconId, editItemCallback);
            this.metaDataDetailsHeaderSupport = new MetaDataDetailsHeaderSupport(i18n, metaDataIconId,
                    showItemMetaDataCallback);
        } else {
            this.editDetailsHeaderSupport = null;
            this.metaDataDetailsHeaderSupport = null;
        }

        addHeaderSupports(Arrays.asList(editDetailsHeaderSupport, metaDataDetailsHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    private Label buildHeaderCaption() {
        final Label caption = new Label(entityType + " : ", ContentMode.HTML);
        caption.setId(detailsHeaderCaptionId);
        caption.addStyleName("header-caption");

        return caption;
    }

    @Override
    protected boolean doSubscribeToEventBus() {
        return false;
    }

    @Override
    protected Component getHeaderCaption() {
        return headerCaption;
    }

    // TODO: Check if it could be done by binder
    public void updateHeaderDetails(final String entityName) {
        if (StringUtils.hasText(entityName)) {
            headerCaption.setValue(entityType + " : " + HawkbitCommonUtil.getBoldHTMLText(entityName));
        } else {
            headerCaption.setValue(entityType + " : ");
        }
    }

    public void enableEditComponents() {
        editDetailsHeaderSupport.enableEditIcon();
        metaDataDetailsHeaderSupport.enableMetaDataIcon();
    }

    public void disableEditComponents() {
        editDetailsHeaderSupport.disableEditIcon();
        metaDataDetailsHeaderSupport.disableMetaDataIcon();
    }
}
