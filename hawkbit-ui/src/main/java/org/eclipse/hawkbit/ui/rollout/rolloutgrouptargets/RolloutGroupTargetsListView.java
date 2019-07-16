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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Label;

/**
 * Rollout Group Targets List View.
 */
public class RolloutGroupTargetsListView extends AbstractGridComponentLayout<ProxyTarget> {
    private static final long serialVersionUID = 1L;

    private final RolloutUIState rolloutUIState;

    private final RolloutGroupTargetsListHeader rolloutGroupTargetsListHeader;
    private final RolloutGroupTargetsListGrid rolloutGroupTargetsListGrid;

    public RolloutGroupTargetsListView(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final RolloutUIState rolloutUIState, final RolloutGroupManagement rolloutGroupManagement) {
        super(i18n, eventBus);
        this.rolloutUIState = rolloutUIState;

        final RolloutGroupTargetsDataProvider rolloutGroupTargetsDataProvider = new RolloutGroupTargetsDataProvider(
                rolloutGroupManagement, rolloutUIState, new TargetWithActionStatusToProxyTargetMapper());

        this.rolloutGroupTargetsListHeader = new RolloutGroupTargetsListHeader(getEventBus(), getI18n(),
                rolloutUIState);
        this.rolloutGroupTargetsListGrid = new RolloutGroupTargetsListGrid(getI18n(), getEventBus(), rolloutUIState,
                rolloutGroupTargetsDataProvider);

        this.setFooterSupport(new RolloutTargetsCountFooterSupport());

        init();
    }

    @Override
    protected boolean doSubscribeToEventBus() {
        return false;
    }

    @Override
    public RolloutGroupTargetsListHeader getGridHeader() {
        return rolloutGroupTargetsListHeader;
    }

    @Override
    public RolloutGroupTargetsListGrid getGrid() {
        return rolloutGroupTargetsListGrid;
    }

    class RolloutTargetsCountFooterSupport extends AbstractFooterSupport {

        @Override
        protected Label getFooterMessageLabel() {
            final RolloutGroupTargetsCountLabelMessage countMessageLabel = new RolloutGroupTargetsCountLabelMessage(
                    rolloutUIState, getGrid(), getI18n(), getEventBus());
            countMessageLabel.setId(UIComponentIdProvider.ROLLOUT_GROUP_TARGET_LABEL);

            return countMessageLabel;
        }
    }
}
