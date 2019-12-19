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

import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
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

    private final ManagementUIState managementUIState;

    private final Label headerCaption;

    private final transient ResizeHeaderSupport resizeHeaderSupport;

    public ActionHistoryGridHeader(final VaadinMessageSource i18n, final ManagementUIState managementUIState,
            final UIEventBus eventBus) {
        super(i18n, null, eventBus);

        this.managementUIState = managementUIState;

        this.headerCaption = buildHeaderCaption();

        this.resizeHeaderSupport = new ResizeHeaderSupport(i18n, SPUIDefinitions.EXPAND_ACTION_HISTORY,
                this::maximizeTable, this::minimizeTable, this::onLoadIsTableMaximized);
        addHeaderSupports(Arrays.asList(resizeHeaderSupport));

        restoreHeaderState();
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
        // TODO: check if it is needed
        // details.populateMasterDataAndRecreateContainer(masterForDetails);
        managementUIState.setActionHistoryMaximized(Boolean.TRUE);
        eventBus.publish(this, ManagementUIEvent.MAX_ACTION_HISTORY);
    }

    private void minimizeTable() {
        managementUIState.setActionHistoryMaximized(Boolean.FALSE);
        eventBus.publish(this, ManagementUIEvent.MIN_ACTION_HISTORY);
    }

    private Boolean onLoadIsTableMaximized() {
        return managementUIState.isActionHistoryMaximized();
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
