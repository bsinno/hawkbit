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

import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
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

    private final transient TargetFilterAddUpdateLayout targetFilterAddUpdateLayout;
    private final transient AddTargetFilterController addTargetFilterController;
    private final transient UpdateTargetFilterController updateTargetFilterController;

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
    public TargetFilterDetailsGridHeader(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final TargetFilterAddUpdateLayout targetFilterAddUpdateLayout,
            final AddTargetFilterController addTargetFilterController,
            final UpdateTargetFilterController updateTargetFilterController,
            final FilterManagementUIState filterManagementUIState) {
        super(i18n, null, eventBus);

        this.targetFilterAddUpdateLayout = targetFilterAddUpdateLayout;
        this.addTargetFilterController = addTargetFilterController;
        this.updateTargetFilterController = updateTargetFilterController;
        this.filterManagementUIState = filterManagementUIState;

        this.headerCaptionDetails = createHeaderCaptionDetails();

        this.closeHeaderSupport = new CloseHeaderSupport(i18n, UIComponentIdProvider.CUSTOM_FILTER_CLOSE,
                this::showTargetFilterView);
        addHeaderSupports(Arrays.asList(closeHeaderSupport));

        restoreHeaderState();
        buildHeader();
    }

    @Override
    protected void init() {
        super.init();
        setHeightUndefined();
    }

    @Override
    protected void buildHeader() {
        super.buildHeader();

        addComponent(targetFilterAddUpdateLayout.getRootComponent());
    }

    public void showAddFilterLayout() {
        showAddUpdateFilterLayout(UIMessageIdProvider.LABEL_CREATE_FILTER, addTargetFilterController, null);
    }

    private void showAddUpdateFilterLayout(final String captionMsgKey,
            final AbstractEntityWindowController<ProxyTargetFilterQuery, ProxyTargetFilterQuery> controller,
            final ProxyTargetFilterQuery proxyEntity) {
        headerCaptionDetails.setValue(i18n.getMessage(captionMsgKey));

        controller.populateWithData(proxyEntity);
        targetFilterAddUpdateLayout.setSaveCallback(controller.getSaveDialogCloseListener());
        targetFilterAddUpdateLayout.disableSearchButton();
    }

    public void showEditFilterLayout(final ProxyTargetFilterQuery proxyEntity) {
        showAddUpdateFilterLayout(UIMessageIdProvider.LABEL_EDIT_FILTER, updateTargetFilterController, proxyEntity);
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

    // @Override
    // protected void restoreCaption() {
    // setCaptionDetails();
    // }

    // public void setCaptionDetails(final String detailsText) {
    // headerCaptionDetails.setValue(detailsText);
    // if (filterManagementUIState.isCreateFilterViewDisplayed()) {
    // headerCaptionDetails.setValue(i18n.getMessage(UIMessageIdProvider.LABEL_CREATE_FILTER));
    // } else {
    // filterManagementUIState.getTfQuery().map(ProxyTargetFilterQuery::getName)
    // .ifPresent(headerCaptionDetails::setValue);
    // }
    // }

    // @EventBusListenerMethod(scope = EventScope.UI)
    // void onEvent(final CustomFilterUIEvent event) {
    // if (event == CustomFilterUIEvent.TARGET_FILTER_DETAIL_VIEW
    // || event == CustomFilterUIEvent.CREATE_NEW_FILTER_CLICK) {
    // setCaptionDetails();
    // }
    // }
}
