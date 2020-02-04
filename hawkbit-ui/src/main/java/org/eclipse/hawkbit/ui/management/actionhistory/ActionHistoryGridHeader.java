/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.actionhistory;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutResizeEventPayload.ResizeType;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Header for ActionHistory with maximize-support.
 */
public class ActionHistoryGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final ActionHistoryGridLayoutUiState actionHistoryGridLayoutUiState;

    private final Label headerCaption;

    private final transient ResizeHeaderSupport resizeHeaderSupport;

    public ActionHistoryGridHeader(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final ActionHistoryGridLayoutUiState actionHistoryGridLayoutUiState) {
        super(i18n, null, eventBus);

        this.actionHistoryGridLayoutUiState = actionHistoryGridLayoutUiState;

        this.headerCaption = buildHeaderCaption();

        this.resizeHeaderSupport = new ResizeHeaderSupport(i18n, SPUIDefinitions.EXPAND_ACTION_HISTORY,
                this::maximizeTable, this::minimizeTable, this::onLoadIsTableMaximized);
        addHeaderSupports(Arrays.asList(resizeHeaderSupport));

        restoreState();
        buildHeader();
    }

    private Label buildHeaderCaption() {
        final Label caption = new Label(i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_HISTORY), ContentMode.HTML);

        caption.addStyleName(ValoTheme.LABEL_SMALL);
        caption.addStyleName(ValoTheme.LABEL_BOLD);
        caption.addStyleName("header-caption");

        return caption;
    }

    @Override
    protected Component getHeaderCaption() {
        return headerCaption;
    }

    private void maximizeTable() {
        eventBus.publish(CommandTopics.RESIZE_LAYOUT, this,
                new LayoutResizeEventPayload(ResizeType.MAXIMIZE, Layout.ACTION_HISTORY_LIST, View.DEPLOYMENT));

        actionHistoryGridLayoutUiState.setMaximized(true);
    }

    private void minimizeTable() {
        eventBus.publish(CommandTopics.RESIZE_LAYOUT, this,
                new LayoutResizeEventPayload(ResizeType.MINIMIZE, Layout.ACTION_HISTORY_LIST, View.DEPLOYMENT));

        actionHistoryGridLayoutUiState.setMaximized(false);
    }

    private Boolean onLoadIsTableMaximized() {
        return actionHistoryGridLayoutUiState.isMaximized();
    }

    /**
     * Updates header with target name.
     *
     * @param targetName
     *            name of the target
     */
    public void updateActionHistoryHeader(final String targetName) {
        if (StringUtils.hasText(targetName)) {
            headerCaption.setValue(i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_HISTORY_FOR) + " "
                    + HawkbitCommonUtil.getBoldHTMLText(targetName));
        } else {
            headerCaption.setValue(i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_HISTORY));
        }
    }
}
