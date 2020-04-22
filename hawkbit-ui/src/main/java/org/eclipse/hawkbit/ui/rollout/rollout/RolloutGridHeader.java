/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rollout;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.SearchFilterEventPayload;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.AddHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
import org.eclipse.hawkbit.ui.rollout.RolloutManagementUIState;
import org.eclipse.hawkbit.ui.rollout.window.RolloutWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Header layout of rollout list view.
 */
public class RolloutGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final RolloutManagementUIState rolloutManagementUIState;
    private final transient RolloutWindowBuilder rolloutWindowBuilder;

    private final transient SearchHeaderSupport searchHeaderSupport;
    private final transient AddHeaderSupport addHeaderSupport;

    RolloutGridHeader(final SpPermissionChecker permissionChecker,
            final RolloutManagementUIState rolloutManagementUIState, final UIEventBus eventBus,
            final VaadinMessageSource i18n, final RolloutWindowBuilder windowBuilder) {
        super(i18n, permissionChecker, eventBus);

        this.rolloutManagementUIState = rolloutManagementUIState;
        this.rolloutWindowBuilder = windowBuilder;

        this.searchHeaderSupport = new SearchHeaderSupport(i18n, UIComponentIdProvider.ROLLOUT_LIST_SEARCH_BOX_ID,
                UIComponentIdProvider.ROLLOUT_LIST_SEARCH_RESET_ICON_ID, this::getSearchTextFromUiState,
                this::searchBy);
        if (permChecker.hasRolloutCreatePermission()) {
            this.addHeaderSupport = new AddHeaderSupport(i18n, UIComponentIdProvider.ROLLOUT_ADD_ICON_ID,
                    this::addNewRollout, () -> false);
        } else {
            this.addHeaderSupport = null;
        }

        addHeaderSupports(Arrays.asList(searchHeaderSupport, addHeaderSupport));

        buildHeader();
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("message.rollouts")).buildCaptionLabel();
    }

    private String getSearchTextFromUiState() {
        return rolloutManagementUIState.getSearchText().orElse(null);
    }

    private void searchBy(final String newSearchText) {
        eventBus.publish(EventTopics.SEARCH_FILTER_CHANGED, this,
                new SearchFilterEventPayload(newSearchText, EventLayout.ROLLOUT_LIST, EventView.ROLLOUT));

        rolloutManagementUIState.setSearchText(newSearchText);
    }

    private void addNewRollout() {
        final Window addWindow = rolloutWindowBuilder.getWindowForAddRollout();

        addWindow.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.rollout")));
        UI.getCurrent().addWindow(addWindow);
        addWindow.setVisible(Boolean.TRUE);
    }
}
