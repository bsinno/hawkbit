/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement.footer;

import org.eclipse.hawkbit.ui.common.grid.AbstractFooterSupport;
import org.eclipse.hawkbit.ui.filtermanagement.event.CustomFilterUIEvent;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

/**
 * Count message label which display current filter details and details on
 * pinning.
 */
public class TargetFilterCountMessageLabel extends AbstractFooterSupport {
    private final FilterManagementUIState filterManagementUIState;

    private final VaadinMessageSource i18n;

    private final Label targetCountLabel;

    public TargetFilterCountMessageLabel(final FilterManagementUIState filterManagementUIState,
            final VaadinMessageSource i18n, final UIEventBus eventBus) {
        this.filterManagementUIState = filterManagementUIState;
        this.i18n = i18n;

        this.targetCountLabel = new Label();

        init();
        eventBus.subscribe(this);
    }

    private void init() {
        targetCountLabel.setId(UIComponentIdProvider.COUNT_LABEL);
        targetCountLabel.setContentMode(ContentMode.HTML);
        targetCountLabel.addStyleName(SPUIStyleDefinitions.SP_LABEL_MESSAGE_STYLE);

        targetCountLabel
                .setValue(new StringBuilder(i18n.getMessage("label.target.filtered.total")).append(0).toString());
    }

    @Override
    protected Label getFooterMessageLabel() {
        return targetCountLabel;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final CustomFilterUIEvent custFUIEvent) {
        if (custFUIEvent == CustomFilterUIEvent.TARGET_DETAILS_VIEW
                || custFUIEvent == CustomFilterUIEvent.CREATE_NEW_FILTER_CLICK
                || custFUIEvent == CustomFilterUIEvent.SHOW_FILTER_MANAGEMENT
                || custFUIEvent == CustomFilterUIEvent.UPDATE_TARGET_FILTER_SEARCH_ICON) {
            UI.getCurrent().access(this::displayTargetFilterMessage);
        }
    }

    // TODO: rework
    public void displayTargetFilterMessage() {
        long totalTargets = 0;
        if (filterManagementUIState.isCreateFilterViewDisplayed() || filterManagementUIState.isEditViewDisplayed()) {
            if (filterManagementUIState.getFilterQueryValue() != null) {
                totalTargets = filterManagementUIState.getTargetsCountAll().get();
            }
            final StringBuilder targetMessage = new StringBuilder(i18n.getMessage("label.target.filtered.total"));
            if (filterManagementUIState.getTargetsTruncated() != null) {
                // set the icon
                targetCountLabel.setIcon(VaadinIcons.INFO_CIRCLE);
                targetCountLabel.setDescription(i18n.getMessage("label.target.filter.truncated",
                        filterManagementUIState.getTargetsTruncated(), SPUIDefinitions.MAX_TABLE_ENTRIES));

            } else {
                targetCountLabel.setIcon(null);
                targetCountLabel.setDescription(null);
            }
            targetMessage.append(totalTargets);

            if (totalTargets > SPUIDefinitions.MAX_TABLE_ENTRIES) {
                targetMessage.append(HawkbitCommonUtil.SP_STRING_PIPE);
                targetMessage.append(i18n.getMessage("label.filter.shown"));
                targetMessage.append(SPUIDefinitions.MAX_TABLE_ENTRIES);
            }
            targetCountLabel.setCaption(targetMessage.toString());
        }
    }
}
