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

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.rsql.RsqlValidationOracle;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.AbstractEntityWindowController;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.grid.header.AbstractGridHeader;
import org.eclipse.hawkbit.ui.common.grid.header.support.CloseHeaderSupport;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.filtermanagement.state.TargetFilterDetailsLayoutUiState;
import org.eclipse.hawkbit.ui.filtermanagement.state.TargetFilterDetailsLayoutUiState.Mode;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
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
            final UINotification uiNotification, final EntityFactory entityFactory,
            final TargetFilterQueryManagement targetFilterManagement, final UiProperties uiProperties,
            final RsqlValidationOracle rsqlValidationOracle, final TargetFilterDetailsLayoutUiState uiState) {
        super(i18n, null, eventBus);

        this.uiState = uiState;

        this.headerCaptionDetails = createHeaderCaptionDetails();

        this.closeHeaderSupport = new CloseHeaderSupport(i18n, UIComponentIdProvider.CUSTOM_FILTER_CLOSE,
                this::closeDetails);
        addHeaderSupport(closeHeaderSupport);

        this.targetFilterAddUpdateLayout = new TargetFilterAddUpdateLayout(i18n, uiProperties, uiState, eventBus,
                rsqlValidationOracle);

        this.addTargetFilterController = new AddTargetFilterController(i18n, entityFactory, eventBus, uiNotification,
                targetFilterManagement, targetFilterAddUpdateLayout, this::closeDetails);
        this.updateTargetFilterController = new UpdateTargetFilterController(i18n, entityFactory, eventBus,
                uiNotification, targetFilterManagement, targetFilterAddUpdateLayout, this::closeDetails);

        buildHeader();
    }

    @Override
    protected void init() {
        super.init();
        setHeightUndefined();
    }

    @Override
    public void buildHeader() {
        super.buildHeader();

        addComponent(targetFilterAddUpdateLayout.getRootComponent());
    }

    public void showAddFilterLayout() {
        uiState.setCurrentMode(Mode.CREATE);

        targetFilterAddUpdateLayout.filterTargetListByQuery(null);
        doShowAddFilterLayout(new ProxyTargetFilterQuery());
    }

    private void doShowAddFilterLayout(final ProxyTargetFilterQuery proxyEntity) {
        final String captionMessage = i18n.getMessage(UIMessageIdProvider.LABEL_CREATE_FILTER);
        showAddUpdateFilterLayout(captionMessage, addTargetFilterController, proxyEntity);
    }

    public void showEditFilterLayout(final ProxyTargetFilterQuery proxyEntity) {
        uiState.setCurrentMode(Mode.EDIT);
        uiState.setSelectedFilterId(proxyEntity.getId());
        uiState.setSelectedFilterName(proxyEntity.getName());

        targetFilterAddUpdateLayout.filterTargetListByQuery(proxyEntity.getQuery());
        doShowEditFilterLayout(proxyEntity.getName(), proxyEntity);
    }

    private void doShowEditFilterLayout(final String caption, final ProxyTargetFilterQuery proxyEntity) {
        showAddUpdateFilterLayout(caption, updateTargetFilterController, proxyEntity);
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
        uiState.setSelectedFilterId(null);
        uiState.setSelectedFilterName("");

        targetFilterAddUpdateLayout.setEntity(null);

        eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this, new LayoutVisibilityEventPayload(
                VisibilityType.HIDE, EventLayout.TARGET_FILTER_QUERY_FORM, EventView.TARGET_FILTER));
    }

    @Override
    public void restoreState() {
        final ProxyTargetFilterQuery targetFilterToRestore = restoreEntityFromState();

        if (Mode.EDIT == uiState.getCurrentMode()) {
            doShowEditFilterLayout(uiState.getSelectedFilterName(), targetFilterToRestore);
        } else if (Mode.CREATE == uiState.getCurrentMode()) {
            doShowAddFilterLayout(targetFilterToRestore);
        }
    }

    private ProxyTargetFilterQuery restoreEntityFromState() {
        final ProxyTargetFilterQuery restoredEntity = new ProxyTargetFilterQuery();

        if (Mode.EDIT == uiState.getCurrentMode()) {
            restoredEntity.setId(uiState.getSelectedFilterId());
        }
        restoredEntity.setName(uiState.getNameInput());
        restoredEntity.setQuery(uiState.getFilterQueryValueInput());

        return restoredEntity;
    }
}
