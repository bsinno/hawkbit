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
import org.eclipse.hawkbit.ui.common.event.ChangeUiElementPayload;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.CloseHeaderSupport;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.filtermanagement.state.TargetFilterDetailsLayoutUiState;
import org.eclipse.hawkbit.ui.filtermanagement.state.TargetFilterDetailsLayoutUiState.Mode;
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

    private final Label headerCaptionDetails;
    private final TargetFilterDetailsLayoutUiState uiState;

    private final transient CloseHeaderSupport closeHeaderSupport;

    private final transient TargetFilterAddUpdateLayout targetFilterAddUpdateLayout;
    private final transient AddTargetFilterController addTargetFilterController;
    private final transient UpdateTargetFilterController updateTargetFilterController;

    public TargetFilterDetailsGridHeader(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final TargetFilterAddUpdateLayout targetFilterAddUpdateLayout,
            final AddTargetFilterController addTargetFilterController,
            final UpdateTargetFilterController updateTargetFilterController,
            final TargetFilterDetailsLayoutUiState uiState) {
        super(i18n, null, eventBus);

        this.targetFilterAddUpdateLayout = targetFilterAddUpdateLayout;
        this.addTargetFilterController = addTargetFilterController;
        this.updateTargetFilterController = updateTargetFilterController;

        this.headerCaptionDetails = createHeaderCaptionDetails();
        this.uiState = uiState;
        this.closeHeaderSupport = new CloseHeaderSupport(i18n, UIComponentIdProvider.CUSTOM_FILTER_CLOSE,
                this::closeDetails);
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
        uiState.setCurrentMode(Mode.CREATE);
        final String captionMessage = i18n.getMessage(UIMessageIdProvider.LABEL_CREATE_FILTER);

        final ProxyTargetFilterQuery restoredEntity = new ProxyTargetFilterQuery();
        restoredEntity.setName(uiState.getNameInput());
        restoredEntity.setQuery(uiState.getFilterQueryValueInput());

        showAddUpdateFilterLayout(captionMessage, addTargetFilterController, restoredEntity);
    }

    public void showEditFilterLayout(final ProxyTargetFilterQuery proxyEntity) {
        uiState.setCurrentMode(Mode.EDIT);
        // TODO: should we update entity according to the previous ui state?
        showAddUpdateFilterLayout(proxyEntity.getName(), updateTargetFilterController, proxyEntity);
    }

    private void showAddUpdateFilterLayout(final String captionMessage,
            final AbstractEntityWindowController<ProxyTargetFilterQuery, ProxyTargetFilterQuery> controller,
            final ProxyTargetFilterQuery proxyEntity) {
        headerCaptionDetails.setValue(captionMessage);
        controller.populateWithData(proxyEntity);
        targetFilterAddUpdateLayout.setSaveCallback(controller.getSaveDialogCloseListener());
    }

    private static Label createHeaderCaptionDetails() {
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
        targetFilterViewLink.addClickListener(value -> closeDetails());

        final HorizontalLayout headerCaptionLayout = new HorizontalLayout();
        headerCaptionLayout.setMargin(false);
        headerCaptionLayout.setSpacing(false);

        headerCaptionLayout.addComponent(targetFilterViewLink);
        headerCaptionLayout.addComponent(new Label(">"));
        headerCaptionLayout.addComponent(headerCaptionDetails);

        return headerCaptionLayout;
    }

    private void closeDetails() {
        eventBus.publish(EventTopics.CHANGE_UI_ELEMENT_STATE, this, ChangeUiElementPayload.CLOSE);
    }

    public void restoreState() {
        if (Mode.EDIT == uiState.getCurrentMode()) {
            uiState.getTargetFilterQueryforEdit().ifPresent(this::showEditFilterLayout);
        } else if (Mode.CREATE == uiState.getCurrentMode()) {
            this.showAddFilterLayout();
        }
    }
}
