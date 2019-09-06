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

import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.ResizeHeaderSupport;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

/**
 * Header for ActionHistory with maximize-support.
 */
public class ActionHistoryGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final ManagementUIState managementUIState;

    private final Label headerCaptionTitle;
    private final Label headerCaptionDetails;

    private final transient ResizeHeaderSupport resizeHeaderSupport;

    public ActionHistoryGridHeader(final VaadinMessageSource i18n, final ManagementUIState managementUIState) {
        super(i18n, null, null);

        this.managementUIState = managementUIState;

        this.headerCaptionTitle = buildHeaderCaptionTitle();
        this.headerCaptionDetails = buildHeaderCaptionDetails();

        this.resizeHeaderSupport = new ResizeHeaderSupport(i18n, SPUIDefinitions.EXPAND_ACTION_HISTORY,
                this::maximizeTable, this::minimizeTable, this::onLoadIsTableMaximized);
        addHeaderSupports(Arrays.asList(resizeHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    private Label buildHeaderCaptionTitle() {
        return new LabelBuilder().name(i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_HISTORY)).buildCaptionLabel();
    }

    private Label buildHeaderCaptionDetails() {
        return new Label("", ContentMode.HTML);
    }

    @Override
    protected boolean doSubscribeToEventBus() {
        return false;
    }

    @Override
    protected Component getHeaderCaption() {
        final HorizontalLayout headerCaptionLayout = new HorizontalLayout();
        headerCaptionLayout.setMargin(false);
        headerCaptionLayout.setSpacing(false);

        headerCaptionLayout.addComponent(headerCaptionTitle);
        headerCaptionLayout.addComponent(headerCaptionDetails);

        return headerCaptionLayout;
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
            headerCaptionTitle.setValue(i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_HISTORY_FOR));
            headerCaptionDetails.setValue(HawkbitCommonUtil.getBoldHTMLText(targetName));
        } else {
            headerCaptionTitle.setValue(i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_HISTORY));
            headerCaptionDetails.setValue("");
        }
    }
}
