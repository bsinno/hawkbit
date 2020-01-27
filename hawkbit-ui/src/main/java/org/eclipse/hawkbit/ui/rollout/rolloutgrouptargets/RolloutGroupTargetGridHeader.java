/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.rolloutgrouptargets;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.CloseHeaderSupport;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.rollout.state.RolloutGroupTargetLayoutUIState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Header Layout of Rollout Group Targets list view.
 */
// TODO: consider extending RolloutGroupsListHeader in order to remove
// duplication
public class RolloutGroupTargetGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final RolloutGroupTargetLayoutUIState rolloutUIState;

    private final Button rolloutNameLink;
    private final Label headerCaptionDetails;

    private final transient CloseHeaderSupport closeHeaderSupport;

    public RolloutGroupTargetGridHeader(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final RolloutGroupTargetLayoutUIState rolloutUiState) {
        super(i18n, null, eventBus);

        this.rolloutUIState = rolloutUiState;

        this.rolloutNameLink = createRolloutNameLink();
        this.headerCaptionDetails = createHeaderCaptionDetails();

        this.closeHeaderSupport = new CloseHeaderSupport(i18n,
                UIComponentIdProvider.ROLLOUT_TARGET_VIEW_CLOSE_BUTTON_ID, this::closeRolloutGroupTargets);
        addHeaderSupports(Arrays.asList(closeHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    private Button createRolloutNameLink() {
        final Button rolloutListLink = SPUIComponentProvider.getButton(null, "", "", null, false, null,
                SPUIButtonStyleNoBorder.class);

        rolloutListLink.setStyleName(ValoTheme.LINK_SMALL + " on-focus-no-border link rollout-caption-links");
        rolloutListLink.addStyleName("breadcrumbPaddingLeft");
        rolloutListLink.setDescription(i18n.getMessage("dashboard.rollouts.caption"));
        rolloutListLink.addClickListener(value -> closeRolloutGroupTargets());

        return rolloutListLink;
    }

    private void closeRolloutGroupTargets() {
        rolloutUIState.setParentRolloutName("");
        rolloutUIState.setSelectedRolloutGroupId(null);
        rolloutUIState.setSelectedRolloutGroupName("");

        eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this,
                new LayoutVisibilityEventPayload(VisibilityType.HIDE, Layout.ROLLOUT_GROUP_TARGET_LIST, View.ROLLOUT));
    }

    private Label createHeaderCaptionDetails() {
        final Label captionDetails = new LabelBuilder().name("").buildCaptionLabel();

        captionDetails.setStyleName(ValoTheme.LABEL_BOLD + " " + ValoTheme.LABEL_SMALL);
        captionDetails.addStyleName("breadcrumbPaddingLeft");

        return captionDetails;
    }

    @Override
    protected Component getHeaderCaption() {
        final Button rolloutsListViewLink = SPUIComponentProvider.getButton(null, "", "", null, false, null,
                SPUIButtonStyleNoBorder.class);
        rolloutsListViewLink.setStyleName(ValoTheme.LINK_SMALL + " " + "on-focus-no-border link rollout-caption-links");
        rolloutsListViewLink.setDescription(i18n.getMessage("message.rollouts"));
        rolloutsListViewLink.setCaption(i18n.getMessage("message.rollouts"));
        rolloutsListViewLink.addClickListener(value -> showRolloutListView());

        final HorizontalLayout headerCaptionLayout = new HorizontalLayout();
        headerCaptionLayout.setMargin(false);
        headerCaptionLayout.setSpacing(false);

        headerCaptionLayout.addComponent(rolloutsListViewLink);
        headerCaptionLayout.addComponent(new Label(">"));
        headerCaptionLayout.addComponent(rolloutNameLink);
        headerCaptionLayout.addComponent(new Label(">"));
        headerCaptionLayout.addComponent(headerCaptionDetails);

        return headerCaptionLayout;
    }

    private void showRolloutListView() {
        // TODO: do something with state
        eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this,
                new LayoutVisibilityEventPayload(VisibilityType.SHOW, Layout.ROLLOUT_LIST, View.ROLLOUT));
    }

    @Override
    protected void restoreCaption() {
        rolloutNameLink.setCaption(rolloutUIState.getParentRolloutName());
        headerCaptionDetails.setValue(rolloutUIState.getSelectedRolloutGroupName());
    }

    public void setRolloutName(final String rolloutName) {
        rolloutNameLink.setCaption(rolloutName);
    }

    public void setRolloutGroupName(final String groupName) {
        headerCaptionDetails.setValue(groupName);
    }
}
