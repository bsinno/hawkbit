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
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Header for entity details with edit and metadata support.
 */
public abstract class DetailsHeader<T extends ProxyNamedEntity> extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    protected final UINotification uiNotification;

    private final Label headerCaptionPrefix;
    private final Label headerCaptionEntity;

    private final transient EditDetailsHeaderSupport editDetailsHeaderSupport;
    private final transient MetaDataDetailsHeaderSupport metaDataDetailsHeaderSupport;

    protected T selectedEntity;

    public DetailsHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus, final UINotification uiNotification) {
        super(i18n, permChecker, eventBus);

        this.uiNotification = uiNotification;

        this.headerCaptionPrefix = buildHeaderCaptionPrefix();
        this.headerCaptionEntity = buildHeaderCaptionEntity();

        if (hasEditPermission()) {
            this.editDetailsHeaderSupport = new EditDetailsHeaderSupport(i18n, getEditIconId(), this::onEdit);
            this.metaDataDetailsHeaderSupport = new MetaDataDetailsHeaderSupport(i18n, getMetaDataIconId(),
                    this::showMetaData);

            addHeaderSupports(Arrays.asList(editDetailsHeaderSupport, metaDataDetailsHeaderSupport));
        } else {
            this.editDetailsHeaderSupport = null;
            this.metaDataDetailsHeaderSupport = null;
        }
    }

    @Override
    protected void init() {
        setSpacing(false);
        setMargin(false);
    }

    private Label buildHeaderCaptionPrefix() {
        final Label caption = new Label(getEntityType() + " : ");
        caption.addStyleName(ValoTheme.LABEL_SMALL);
        caption.addStyleName(ValoTheme.LABEL_BOLD);

        return caption;
    }

    // TODO: remove duplication with ActionHistoryGridHeader
    private Label buildHeaderCaptionEntity() {
        final Label caption = new Label();
        caption.setId(getDetailsHeaderCaptionId());
        caption.setWidth("100%");
        caption.addStyleName(ValoTheme.LABEL_SMALL);
        caption.addStyleName("text-bold");
        caption.addStyleName("text-cut");

        return caption;
    }

    @Override
    protected Component getHeaderCaption() {
        final HorizontalLayout headerCaptionLayout = new HorizontalLayout();
        headerCaptionLayout.setMargin(false);
        headerCaptionLayout.setSpacing(true);
        headerCaptionLayout.setSizeFull();
        headerCaptionLayout.addStyleName("header-caption");

        headerCaptionLayout.addComponent(headerCaptionPrefix);
        headerCaptionLayout.setComponentAlignment(headerCaptionPrefix, Alignment.TOP_LEFT);
        headerCaptionLayout.setExpandRatio(headerCaptionPrefix, 0.0F);

        headerCaptionLayout.addComponent(headerCaptionEntity);
        headerCaptionLayout.setComponentAlignment(headerCaptionEntity, Alignment.TOP_LEFT);
        headerCaptionLayout.setExpandRatio(headerCaptionEntity, 1.0F);

        return headerCaptionLayout;
    }

    // TODO: Check if it could be done by binder
    public void masterEntityChanged(final T entity) {
        if (entity == null) {
            disableEdit();
            disableMetaData();
            headerCaptionEntity.setValue(null);
        } else {
            enableEdit();
            enableMetaData();
            headerCaptionEntity.setValue(getEntityName(entity));
        }

        selectedEntity = entity;
    }

    private void disableEdit() {
        if (editDetailsHeaderSupport != null) {
            editDetailsHeaderSupport.disableEditIcon();
        }
    }

    private void disableMetaData() {
        if (metaDataDetailsHeaderSupport != null) {
            metaDataDetailsHeaderSupport.disableMetaDataIcon();
        }
    }

    private void enableEdit() {
        if (editDetailsHeaderSupport != null) {
            editDetailsHeaderSupport.enableEditIcon();
        }
    }

    private void enableMetaData() {
        if (metaDataDetailsHeaderSupport != null) {
            metaDataDetailsHeaderSupport.enableMetaDataIcon();
        }
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
