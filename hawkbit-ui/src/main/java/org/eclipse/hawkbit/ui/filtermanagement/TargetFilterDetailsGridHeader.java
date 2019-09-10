/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import java.util.Arrays;

import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.CloseHeaderSupport;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.filtermanagement.event.CustomFilterUIEvent;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
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
 * Layout for Custom Filter view
 */
public class TargetFilterDetailsGridHeader extends AbstractGridHeader {
    private static final long serialVersionUID = 1L;

    private static final String BREADCRUMB_CUSTOM_FILTERS = "breadcrumb.target.filter.custom.filters";

    private final FilterManagementUIState filterManagementUIState;

    private final Label headerCaptionDetails;

    private final transient CloseHeaderSupport closeHeaderSupport;

    /**
     * Constructor for TargetFilterDetailsHeader
     * 
     * @param eventBus
     *            UIEventBus
     * @param filterManagementUIState
     *            FilterManagementUIState
     * @param i18n
     *            VaadinMessageSource
     */
    public TargetFilterDetailsGridHeader(final UIEventBus eventBus, final FilterManagementUIState filterManagementUIState,
            final VaadinMessageSource i18n) {
        super(i18n, null, eventBus);

        this.filterManagementUIState = filterManagementUIState;

        this.headerCaptionDetails = createHeaderCaptionDetails();

        this.closeHeaderSupport = new CloseHeaderSupport(i18n, UIComponentIdProvider.CUSTOM_FILTER_CLOSE,
                this::showTargetFilterView);
        addHeaderSupports(Arrays.asList(closeHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    private Label createHeaderCaptionDetails() {
        final Label captionDetails = new LabelBuilder().id(UIComponentIdProvider.TARGET_FILTER_QUERY_NAME_LABEL_ID)
                .name("").buildCaptionLabel();

        captionDetails.addStyleName("breadcrumbPaddingLeft");

        return captionDetails;
    }

    // TODO: remove duplication with RolloutGroupsListHeader
    @Override
    protected Component getHeaderCaption() {
        final Button targetFilterViewLink = SPUIComponentProvider.getButton(null, "", "", null, false, null,
                SPUIButtonStyleNoBorder.class);
        targetFilterViewLink.setStyleName(ValoTheme.LINK_SMALL + " on-focus-no-border link rollout-caption-links");
        targetFilterViewLink.setDescription(i18n.getMessage(BREADCRUMB_CUSTOM_FILTERS));
        targetFilterViewLink.setCaption(i18n.getMessage(BREADCRUMB_CUSTOM_FILTERS));
        targetFilterViewLink.addClickListener(value -> showTargetFilterView());

        final HorizontalLayout headerCaptionLayout = new HorizontalLayout();
        headerCaptionLayout.setMargin(false);
        headerCaptionLayout.setSpacing(false);

        headerCaptionLayout.addComponent(targetFilterViewLink);
        headerCaptionLayout.addComponent(new Label(">"));
        headerCaptionLayout.addComponent(headerCaptionDetails);

        return headerCaptionLayout;
    }

    private void showTargetFilterView() {
        // TODO: check if we really need to reset state here
        filterManagementUIState.setFilterQueryValue(null);
        filterManagementUIState.setCreateFilterBtnClicked(false);
        filterManagementUIState.setEditViewDisplayed(false);
        filterManagementUIState.setTfQuery(null);

        eventBus.publish(this, CustomFilterUIEvent.SHOW_FILTER_MANAGEMENT);
    }

    @Override
    protected void restoreCaption() {
        setCaptionDetails();
    }

    private void setCaptionDetails() {
        if (filterManagementUIState.isCreateFilterViewDisplayed()) {
            headerCaptionDetails.setValue(i18n.getMessage(UIMessageIdProvider.LABEL_CREATE_FILTER));
        } else {
            filterManagementUIState.getTfQuery().map(ProxyTargetFilterQuery::getName)
                    .ifPresent(headerCaptionDetails::setValue);
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final CustomFilterUIEvent event) {
        if (event == CustomFilterUIEvent.TARGET_FILTER_DETAIL_VIEW
                || event == CustomFilterUIEvent.CREATE_NEW_FILTER_CLICK) {
            setCaptionDetails();
        }
    }
}
