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

import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.CloseHeaderSupport;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.rollout.event.RolloutEvent;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

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
public class RolloutGroupTargetsListHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private final RolloutUIState rolloutUIState;

    private final Button rolloutNameLink;
    private final Label headerCaptionDetails;

    private final transient CloseHeaderSupport closeHeaderSupport;

    public RolloutGroupTargetsListHeader(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final RolloutUIState rolloutUiState) {
        super(i18n, null, eventBus);

        this.rolloutUIState = rolloutUiState;

        this.rolloutNameLink = createRolloutNameLink();
        this.headerCaptionDetails = createHeaderCaptionDetails();

        this.closeHeaderSupport = new CloseHeaderSupport(i18n,
                UIComponentIdProvider.ROLLOUT_TARGET_VIEW_CLOSE_BUTTON_ID, this::showRolloutGroupListView);
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
        rolloutListLink.addClickListener(value -> showRolloutGroupListView());

        return rolloutListLink;
    }

    private void showRolloutGroupListView() {
        eventBus.publish(this, RolloutEvent.SHOW_ROLLOUT_GROUPS);
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
        eventBus.publish(this, RolloutEvent.SHOW_ROLLOUTS);
    }

    @Override
    protected void restoreCaption() {
        setCaptionDetails();
    }

    private void setCaptionDetails() {
        rolloutUIState.getRolloutGroup().map(RolloutGroup::getName).ifPresent(headerCaptionDetails::setValue);
        rolloutNameLink.setCaption(rolloutUIState.getRolloutName().orElse(""));
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final RolloutEvent event) {
        if (event == RolloutEvent.SHOW_ROLLOUT_GROUP_TARGETS) {
            setCaptionDetails();
        }
    }
}
