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

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyNamedEntity;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Header for entity details with edit and metadata support.
 */
public abstract class DetailsHeader<T extends ProxyNamedEntity> extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    protected final UINotification uiNotification;

    private final Label headerCaption;

    private final transient EditDetailsHeaderSupport editDetailsHeaderSupport;
    private final transient MetaDataDetailsHeaderSupport metaDataDetailsHeaderSupport;

    protected T selectedEntity;

    public DetailsHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final UINotification uiNotification) {
        super(i18n, permChecker, eventBus);

        this.uiNotification = uiNotification;

        this.headerCaption = buildHeaderCaption();

        if (hasEditPermission()) {
            this.editDetailsHeaderSupport = new EditDetailsHeaderSupport(i18n, getEditIconId(), this::onEdit);
            this.metaDataDetailsHeaderSupport = new MetaDataDetailsHeaderSupport(i18n, getMetaDataIconId(),
                    this::showMetaData);
        } else {
            this.editDetailsHeaderSupport = null;
            this.metaDataDetailsHeaderSupport = null;
        }

        addHeaderSupports(Arrays.asList(editDetailsHeaderSupport, metaDataDetailsHeaderSupport));
    }

    @Override
    protected void init() {
        setSpacing(false);
        setMargin(false);
    }

    // TODO: remove duplication with ActionHistoryGridHeader
    private Label buildHeaderCaption() {
        final Label caption = new Label(getEntityType() + " : ", ContentMode.HTML);
        caption.setId(getDetailsHeaderCaptionId());
        caption.addStyleName(ValoTheme.LABEL_SMALL);
        caption.addStyleName(ValoTheme.LABEL_BOLD);
        caption.addStyleName("header-caption");

        return caption;
    }

    @Override
    protected Component getHeaderCaption() {
        return headerCaption;
    }

    // TODO: Check if it could be done by binder
    public void masterEntityChanged(final T entity) {
        if (entity == null) {
            editDetailsHeaderSupport.disableEditIcon();
            metaDataDetailsHeaderSupport.disableMetaDataIcon();
            headerCaption.setValue(getEntityType() + " : ");
        } else {
            editDetailsHeaderSupport.enableEditIcon();
            metaDataDetailsHeaderSupport.enableMetaDataIcon();
            headerCaption.setValue(getEntityType() + " : " + HawkbitCommonUtil.getBoldHTMLText(getEntityName(entity)));
        }

        selectedEntity = entity;
    }

    // can be overriden for entities with version
    protected String getEntityName(final T entity) {
        return entity.getName();
    }

    protected abstract boolean hasEditPermission();

    protected abstract String getEntityType();

    protected abstract String getDetailsHeaderCaptionId();

    protected abstract String getEditIconId();

    protected abstract void onEdit();

    protected abstract String getMetaDataIconId();

    protected abstract void showMetaData();
}
