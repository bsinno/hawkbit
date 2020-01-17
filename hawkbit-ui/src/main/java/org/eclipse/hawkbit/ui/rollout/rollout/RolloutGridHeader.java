/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rollout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.AddHeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.HeaderSupport;
import org.eclipse.hawkbit.ui.common.grid.header.support.SearchHeaderSupport;
import org.eclipse.hawkbit.ui.rollout.state.RolloutLayoutUIState;
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

    private final RolloutLayoutUIState rolloutUIState;
    private final transient RolloutWindowBuilder rolloutWindowBuilder;

    RolloutGridHeader(final SpPermissionChecker permissionChecker, final RolloutLayoutUIState rolloutUIState,
            final UIEventBus eventBus, final VaadinMessageSource i18n, final RolloutWindowBuilder windowBuilder) {
        super(i18n, permissionChecker, eventBus);

        this.rolloutUIState = rolloutUIState;

        this.rolloutWindowBuilder = windowBuilder;

        final List<HeaderSupport> headerSupports = new ArrayList<>();

        headerSupports.add(new SearchHeaderSupport(i18n, UIComponentIdProvider.ROLLOUT_LIST_SEARCH_BOX_ID,
                UIComponentIdProvider.ROLLOUT_LIST_SEARCH_RESET_ICON_ID, this::getSearchTextFromUiState, this::searchBy,
                () -> searchBy(null)));
        if (permChecker.hasRolloutCreatePermission()) {
            headerSupports.add(new AddHeaderSupport(i18n, UIComponentIdProvider.ROLLOUT_ADD_ICON_ID,
                    this::addNewRollout, () -> false));
        }
        addHeaderSupports(headerSupports);

        restoreHeaderState();
        buildHeader();
    }

    @Override
    protected Component getHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("message.rollouts")).buildCaptionLabel();
    }

    private String getSearchTextFromUiState() {
        return rolloutUIState.getSearchText().orElse(null);
    }

    private void searchBy(final String newSearchText) {
        rolloutUIState.setSearchText(newSearchText);
        eventBus.publish(EventTopics.SEARCH_FILTER_CHANGED, this, newSearchText);
    }

    private void addNewRollout() {
        final Window addWindow = rolloutWindowBuilder.getWindowForAddRollout();

        addWindow.setCaption(i18n.getMessage("caption.create.new", i18n.getMessage("caption.rollout")));
        UI.getCurrent().addWindow(addWindow);
        addWindow.setVisible(Boolean.TRUE);
    }
}
