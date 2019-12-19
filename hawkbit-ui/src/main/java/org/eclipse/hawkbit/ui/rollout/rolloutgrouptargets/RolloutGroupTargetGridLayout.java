/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rolloutgrouptargets;

import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetWithActionStatusToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.providers.RolloutGroupTargetsDataProvider;
import org.eclipse.hawkbit.ui.common.grid.AbstractFooterSupport;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Label;

/**
 * Rollout Group Targets List View.
 */
public class RolloutGroupTargetGridLayout extends AbstractGridComponentLayout {
    private static final long serialVersionUID = 1L;

    private final RolloutGroupTargetGridHeader rolloutGroupTargetsListHeader;
    private final RolloutGroupTargetGrid rolloutGroupTargetsListGrid;

    public RolloutGroupTargetGridLayout(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final RolloutUIState rolloutUIState, final RolloutGroupManagement rolloutGroupManagement) {
        final RolloutGroupTargetsDataProvider rolloutGroupTargetsDataProvider = new RolloutGroupTargetsDataProvider(
                rolloutGroupManagement, rolloutUIState, new TargetWithActionStatusToProxyTargetMapper());

        this.rolloutGroupTargetsListHeader = new RolloutGroupTargetGridHeader(eventBus, i18n, rolloutUIState);
        this.rolloutGroupTargetsListGrid = new RolloutGroupTargetGrid(i18n, eventBus, rolloutUIState,
                rolloutGroupTargetsDataProvider);

        buildLayout(rolloutGroupTargetsListHeader, rolloutGroupTargetsListGrid,
                new RolloutTargetsCountFooterSupport(i18n, rolloutUIState, rolloutGroupTargetsListGrid));
    }

    private static class RolloutTargetsCountFooterSupport extends AbstractFooterSupport {
        private final VaadinMessageSource i18n;
        private final RolloutUIState rolloutUIState;
        private final RolloutGroupTargetGrid rolloutGroupTargetsListGrid;

        RolloutTargetsCountFooterSupport(final VaadinMessageSource i18n, final RolloutUIState rolloutUIState,
                final RolloutGroupTargetGrid rolloutGroupTargetsListGrid) {
            this.i18n = i18n;
            this.rolloutUIState = rolloutUIState;
            this.rolloutGroupTargetsListGrid = rolloutGroupTargetsListGrid;
        }

        @Override
        protected Label getFooterMessageLabel() {
            // TODO: do we really need to pass Grid here???
            final RolloutGroupTargetsCountLabelMessage countMessageLabel = new RolloutGroupTargetsCountLabelMessage(
                    rolloutUIState, rolloutGroupTargetsListGrid, i18n);
            countMessageLabel.setId(UIComponentIdProvider.ROLLOUT_GROUP_TARGET_LABEL);

            return countMessageLabel;
        }
    }
}
