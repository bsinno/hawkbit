/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rolloutgrouptargets;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.rollout.event.RolloutEvent;
import org.eclipse.hawkbit.ui.rollout.state.RolloutManagementUIState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.Query;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;

/**
 * Count message label for the targets of the rollout group.
 */
public class RolloutGroupTargetsCountLabelMessage extends Label {

    private static final long serialVersionUID = 1L;

    private final RolloutManagementUIState rolloutUIState;

    private final Grid<ProxyTarget> rolloutGroupTargetsListGrid;

    private final VaadinMessageSource i18n;

    RolloutGroupTargetsCountLabelMessage(final RolloutManagementUIState rolloutUIState,
            final Grid<ProxyTarget> rolloutGroupTargetsListGrid, final VaadinMessageSource i18n) {
        this.rolloutUIState = rolloutUIState;
        this.rolloutGroupTargetsListGrid = rolloutGroupTargetsListGrid;
        this.i18n = i18n;

        applyStyle();
        displayRolloutGroupTargetMessage();
    }

    /**
     * TenantAwareEvent Listener to show the message count.
     * 
     * @param event
     */
    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final RolloutEvent event) {
        if (event == RolloutEvent.SHOW_ROLLOUT_GROUP_TARGETS_COUNT) {
            displayRolloutGroupTargetMessage();
        }
    }

    private void applyStyle() {
        /* Create label for Targets count message displaying below the table */
        addStyleName(SPUIStyleDefinitions.SP_LABEL_MESSAGE_STYLE);
        setContentMode(ContentMode.HTML);
        setId(UIComponentIdProvider.COUNT_LABEL);
    }

    private void displayRolloutGroupTargetMessage() {
        // TODO: what about sortOrders/filters, are they respected here?
        // Evaluate grid.getDataCommunicator.getDataProviderSize()
        long totalTargetTableEntries = rolloutGroupTargetsListGrid.getDataProvider().size(new Query<>());

        if (rolloutUIState.getRolloutGroupTargetsTruncated() != null) {
            // set the icon
            setIcon(VaadinIcons.INFO_CIRCLE);
            setDescription(i18n.getMessage("rollout.group.label.target.truncated",
                    rolloutUIState.getRolloutGroupTargetsTruncated(), SPUIDefinitions.MAX_TABLE_ENTRIES));
            totalTargetTableEntries += rolloutUIState.getRolloutGroupTargetsTruncated();
        } else {
            setIcon(null);
            setDescription(null);
        }

        final StringBuilder message = new StringBuilder(i18n.getMessage("label.target.filter.count"));
        message.append(rolloutUIState.getRolloutGroupTargetsTotalCount());

        if (totalTargetTableEntries > SPUIDefinitions.MAX_TABLE_ENTRIES) {
            message.append(HawkbitCommonUtil.SP_STRING_PIPE);
            message.append(i18n.getMessage("label.filter.shown"));
            message.append(SPUIDefinitions.MAX_TABLE_ENTRIES);
        }

        setCaption(message.toString());
    }
}
