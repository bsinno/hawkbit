/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rolloutgroup;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.CloseHeaderSupport;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.rollout.RolloutManagementUIState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Header Layout of Rollout Group list view.
 */
public class RolloutGroupGridHeader extends AbstractGridHeader implements MasterEntityAwareComponent<ProxyRollout> {
    private static final long serialVersionUID = 1L;

    private final RolloutManagementUIState rolloutManagementUIState;
    private final Label headerCaptionDetails;

    /**
     * Constructor for RolloutGroupsListHeader
     * 
     * @param eventBus
     *            UIEventBus
     * @param rolloutManagementUIState
     *            UIState
     * @param i18n
     *            I18N
     */
    public RolloutGroupGridHeader(final UIEventBus eventBus, final RolloutManagementUIState rolloutManagementUIState,
            final VaadinMessageSource i18n) {
        super(i18n, null, eventBus);

        this.headerCaptionDetails = createHeaderCaptionDetails();
        this.rolloutManagementUIState = rolloutManagementUIState;

        final CloseHeaderSupport closeHeaderSupport = new CloseHeaderSupport(i18n,
                UIComponentIdProvider.ROLLOUT_GROUP_CLOSE, this::closeRolloutGroups);
        addHeaderSupports(Arrays.asList(closeHeaderSupport));

        buildHeader();
    }

    private static Label createHeaderCaptionDetails() {
        final Label captionDetails = new LabelBuilder().id(UIComponentIdProvider.ROLLOUT_GROUP_HEADER_CAPTION).name("")
                .buildCaptionLabel();
        captionDetails.addStyleName("breadcrumbPaddingLeft");
        return captionDetails;
    }

    @Override
    protected Component getHeaderCaption() {
        final Button rolloutsListViewLink = SPUIComponentProvider.getButton(null, "", "", null, false, null,
                SPUIButtonStyleNoBorder.class);
        rolloutsListViewLink.setStyleName(ValoTheme.LINK_SMALL + " on-focus-no-border link rollout-caption-links");
        rolloutsListViewLink.setDescription(i18n.getMessage("message.rollouts"));
        rolloutsListViewLink.setCaption(i18n.getMessage("message.rollouts"));
        rolloutsListViewLink.addClickListener(value -> closeRolloutGroups());

        final HorizontalLayout headerCaptionLayout = new HorizontalLayout();
        headerCaptionLayout.setMargin(false);
        headerCaptionLayout.setSpacing(false);

        headerCaptionLayout.addComponent(rolloutsListViewLink);
        headerCaptionLayout.addComponent(new Label(">"));
        headerCaptionLayout.addComponent(headerCaptionDetails);

        return headerCaptionLayout;
    }

    public void closeRolloutGroups() {
        eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this,
                new LayoutVisibilityEventPayload(VisibilityType.HIDE, EventLayout.ROLLOUT_GROUP_LIST, EventView.ROLLOUT));
    }

    @Override
    public void masterEntityChanged(final ProxyRollout masterEntity) {
        headerCaptionDetails.setValue(masterEntity != null ? masterEntity.getName() : "");
    }

    @Override
    protected void restoreCaption() {
        headerCaptionDetails.setValue(rolloutManagementUIState.getSelectedRolloutName());
    }
}
