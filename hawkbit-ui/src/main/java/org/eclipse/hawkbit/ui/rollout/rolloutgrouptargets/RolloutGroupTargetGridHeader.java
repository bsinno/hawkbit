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
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRollout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyRolloutGroup;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.Layout;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.View;
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
 * Header Layout of Rollout Group Targets list view.
 */
// TODO: consider extending RolloutGroupsListHeader in order to remove
// duplication
public class RolloutGroupTargetGridHeader extends AbstractGridHeader
        implements MasterEntityAwareComponent<ProxyRolloutGroup> {
    private static final long serialVersionUID = 1L;

    private final RolloutManagementUIState rolloutManagementUIState;

    private final Button rolloutNameLink;
    private final Label headerCaptionDetails;

    private final transient CloseHeaderSupport closeHeaderSupport;

    public RolloutGroupTargetGridHeader(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final RolloutManagementUIState rolloutManagementUIState) {
        super(i18n, null, eventBus);

        this.rolloutManagementUIState = rolloutManagementUIState;

        this.rolloutNameLink = createRolloutNameLink();
        this.headerCaptionDetails = createHeaderCaptionDetails();

        this.closeHeaderSupport = new CloseHeaderSupport(i18n,
                UIComponentIdProvider.ROLLOUT_TARGET_VIEW_CLOSE_BUTTON_ID, this::closeRolloutGroupTargets);
        addHeaderSupports(Arrays.asList(closeHeaderSupport));

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
        eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this,
                new LayoutVisibilityEventPayload(VisibilityType.SHOW, Layout.ROLLOUT_LIST, View.ROLLOUT));
    }

    public void rolloutChanged(final ProxyRollout rollout) {
        rolloutNameLink.setCaption(rollout != null ? rollout.getName() : "");
    }

    @Override
    public void masterEntityChanged(final ProxyRolloutGroup masterEntity) {
        headerCaptionDetails.setValue(masterEntity != null ? masterEntity.getName() : "");
    }

    @Override
    protected void restoreCaption() {
        rolloutNameLink.setCaption(rolloutManagementUIState.getSelectedRolloutName());
        headerCaptionDetails.setValue(rolloutManagementUIState.getSelectedRolloutGroupName());
    }
}
