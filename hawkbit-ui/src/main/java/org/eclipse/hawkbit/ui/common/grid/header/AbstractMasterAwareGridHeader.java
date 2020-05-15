/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.header;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Abstract header for master entity aware grids.
 */
public abstract class AbstractMasterAwareGridHeader<T> extends AbstractGridHeader
        implements MasterEntityAwareComponent<T> {
    private static final long serialVersionUID = 1L;

    private final Label entityDetailsCaption;
    private final Label masterEntityDetailsCaption;

    private final HorizontalLayout masterAwareCaptionLayout;

    public AbstractMasterAwareGridHeader(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus) {
        super(i18n, permChecker, eventBus);

        this.entityDetailsCaption = buildEntityDetailsCaption();
        this.masterEntityDetailsCaption = buildMasterEntityDetailsCaption();

        this.masterAwareCaptionLayout = buildMasterAwareCaptionLayout();
    }

    private Label buildEntityDetailsCaption() {
        final Label caption = new Label(i18n.getMessage(getEntityDetailsCaptionMsgKey()));

        caption.addStyleName(ValoTheme.LABEL_SMALL);
        caption.addStyleName(ValoTheme.LABEL_BOLD);

        return caption;
    }

    protected abstract String getEntityDetailsCaptionMsgKey();

    private Label buildMasterEntityDetailsCaption() {
        final Label caption = new Label();

        caption.setId(getMasterEntityDetailsCaptionId());
        caption.setWidthFull();
        caption.addStyleName(ValoTheme.LABEL_SMALL);
        caption.addStyleName(ValoTheme.LABEL_BOLD);
        caption.addStyleName("text-bold");
        caption.addStyleName("text-cut");

        return caption;
    }

    protected abstract String getMasterEntityDetailsCaptionId();

    private HorizontalLayout buildMasterAwareCaptionLayout() {
        final HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(false);
        layout.setSpacing(true);
        layout.setSizeFull();
        layout.addStyleName("header-caption");

        layout.addComponent(entityDetailsCaption);
        layout.setComponentAlignment(entityDetailsCaption, Alignment.TOP_LEFT);
        layout.setExpandRatio(entityDetailsCaption, 0.0F);

        layout.addComponent(masterEntityDetailsCaption);
        layout.setComponentAlignment(masterEntityDetailsCaption, Alignment.TOP_LEFT);
        layout.setExpandRatio(masterEntityDetailsCaption, 1.0F);

        return layout;
    }

    @Override
    protected Component getHeaderCaption() {
        return masterAwareCaptionLayout;
    }

    @Override
    public void masterEntityChanged(final T masterEntity) {
        final String masterEntityName = getMasterEntityName(masterEntity);

        if (StringUtils.hasText(masterEntityName)) {
            entityDetailsCaption.setValue(i18n.getMessage(getEntityDetailsCaptionOfMsgKey()));
            masterEntityDetailsCaption.setValue(masterEntityName);
        } else {
            entityDetailsCaption.setValue(i18n.getMessage(getEntityDetailsCaptionMsgKey()));
            masterEntityDetailsCaption.setValue("");
        }
    }

    protected abstract String getMasterEntityName(final T masterEntity);

    protected abstract String getEntityDetailsCaptionOfMsgKey();
}
