/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.im.authentication.SpPermission;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetFilterQueryToProxyTargetFilterMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.ShowFormEventPayload;
import org.eclipse.hawkbit.ui.common.event.ShowFormEventPayload.FormType;
import org.eclipse.hawkbit.ui.common.event.TargetFilterModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.View;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.filtermanagement.state.TargetFilterGridLayoutUiState;
import org.eclipse.hawkbit.ui.rollout.ProxyFontIcon;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Concrete implementation of TargetFilter grid which is displayed on the
 * Filtermanagement View.
 */
public class TargetFilterGrid extends AbstractGrid<ProxyTargetFilterQuery, String> {
    private static final long serialVersionUID = 1L;

    private static final String FILTER_NAME_ID = "filterName";
    private static final String FILTER_CREATED_BY_ID = "filterCreatedBy";
    private static final String FILTER_CREATED_DATE_ID = "filterCreatedDate";
    private static final String FILTER_MODIFIED_BY_ID = "filterModifiedBy";
    private static final String FILTER_MODIFIED_DATE_ID = "filterModifiedDate";
    private static final String FILTER_AUTOASSIGNMENT_TYPE_ID = "filterAutoAssignmentType";
    private static final String FILTER_AUTOASSIGNMENT_DS_ID = "filterAutoAssignmentDs";
    private static final String FILTER_DELETE_BUTTON_ID = "filterDeleteButton";

    private final UINotification notification;
    private final TargetFilterGridLayoutUiState uiState;
    private final transient TargetFilterQueryManagement targetFilterQueryManagement;

    private final Map<ActionType, ProxyFontIcon> actionTypeIconMap = new EnumMap<>(ActionType.class);

    private final ConfigurableFilterDataProvider<ProxyTargetFilterQuery, Void, String> targetFilterDataProvider;
    private final transient DeleteSupport<ProxyTargetFilterQuery> targetFilterDeleteSupport;

    private final transient AutoAssignmentWindowBuilder autoAssignmentWindowBuilder;

    public TargetFilterGrid(final VaadinMessageSource i18n, final UINotification notification,
            final UIEventBus eventBus, final TargetFilterGridLayoutUiState uiState,
            final TargetFilterQueryManagement targetFilterQueryManagement, final SpPermissionChecker permChecker,
            final AutoAssignmentWindowBuilder autoAssignmentWindowBuilder) {
        super(i18n, eventBus, permChecker);

        this.notification = notification;
        this.uiState = uiState;
        this.targetFilterQueryManagement = targetFilterQueryManagement;
        this.autoAssignmentWindowBuilder = autoAssignmentWindowBuilder;

        this.targetFilterDataProvider = new TargetFilterQueryDataProvider(targetFilterQueryManagement,
                new TargetFilterQueryToProxyTargetFilterMapper()).withConfigurableFilter();

        this.targetFilterDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("caption.filter.custom"),
                permChecker, notification, this::targetFiltersDeletionCallback);

        initActionTypeIconMap();
        init();
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.TARGET_FILTER_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyTargetFilterQuery, Void, String> getFilterDataProvider() {
        return targetFilterDataProvider;
    }

    private void targetFiltersDeletionCallback(final Collection<ProxyTargetFilterQuery> targetFiltersToBeDeleted) {
        final Collection<Long> targetFilterIdsToBeDeleted = targetFiltersToBeDeleted.stream()
                .map(ProxyIdentifiableEntity::getId).collect(Collectors.toList());
        targetFilterIdsToBeDeleted.forEach(targetFilterQueryManagement::delete);

        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new TargetFilterModifiedEventPayload(
                EntityModifiedEventType.ENTITY_REMOVED, targetFilterIdsToBeDeleted));
    }

    public void setFilter(final String filter) {
        uiState.setLatestSearchFilterApplied(filter);
        filter(filter);
    }

    private void filter(String filter) {
        if (filter == null) {
            filter = "";
        }
        getFilterDataProvider().setFilter(String.format("%%%s%%", filter));
    }

    @Override
    public void addColumns() {
        addComponentColumn(this::buildFilterLink).setId(FILTER_NAME_ID).setCaption(i18n.getMessage("header.name"))
                .setExpandRatio(2);

        addColumn(ProxyTargetFilterQuery::getCreatedBy).setId(FILTER_CREATED_BY_ID)
                .setCaption(i18n.getMessage("header.createdBy")).setExpandRatio(2);

        addColumn(ProxyTargetFilterQuery::getCreatedDate).setId(FILTER_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate")).setExpandRatio(4);

        addColumn(ProxyTargetFilterQuery::getLastModifiedBy).setId(FILTER_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setExpandRatio(2);

        addColumn(ProxyTargetFilterQuery::getModifiedDate).setId(FILTER_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setExpandRatio(4);

        addComponentColumn(this::buildTypeIcon).setId(FILTER_AUTOASSIGNMENT_TYPE_ID)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN).setExpandRatio(1);

        addComponentColumn(this::buildAutoAssignmentLink).setId(FILTER_AUTOASSIGNMENT_DS_ID)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN).setExpandRatio(1);

        getDefaultHeaderRow().join(FILTER_AUTOASSIGNMENT_TYPE_ID, FILTER_AUTOASSIGNMENT_DS_ID)
                .setText(i18n.getMessage("header.auto.assignment.ds"));

        addComponentColumn(targetFilter -> buildActionButton(
                clickEvent -> targetFilterDeleteSupport.openConfirmationWindowDeleteAction(targetFilter,
                        targetFilter.getName()),
                VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.CUSTOM_FILTER_DELETE_ICON + "." + targetFilter.getId(),
                targetFilterDeleteSupport.hasDeletePermission())).setId(FILTER_DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.delete")).setExpandRatio(1);
    }

    private Button buildFilterLink(final ProxyTargetFilterQuery targetFilter) {
        final String filterLinkCaption = targetFilter.getName();
        final String filterLinkDescription = i18n.getMessage(UIMessageIdProvider.TOOLTIP_UPDATE_CUSTOM_FILTER);
        final String filterLinkId = new StringBuilder(UIComponentIdProvider.CUSTOM_FILTER_DETAIL_LINK).append('.')
                .append(targetFilter.getId()).toString();

        return buildLink(clickEvent -> onClickOfFilterName(targetFilter), filterLinkCaption, filterLinkDescription,
                filterLinkId, true);
    }

    private void onClickOfFilterName(final ProxyTargetFilterQuery targetFilter) {
        eventBus.publish(CommandTopics.SHOW_ENTITY_FORM_LAYOUT, this,
                new ShowFormEventPayload<ProxyTargetFilterQuery>(FormType.EDIT, targetFilter, View.TARGET_FILTER));
    }

    private void onClickOfAutoAssignmentLink(final ProxyTargetFilterQuery targetFilter) {
        if (permissionChecker.hasReadRepositoryPermission()) {
            final Window autoAssignmentWindow = autoAssignmentWindowBuilder.getWindowForAutoAssignment(targetFilter);

            autoAssignmentWindow.setCaption(i18n.getMessage(UIMessageIdProvider.CAPTION_SELECT_AUTO_ASSIGN_DS));
            autoAssignmentWindow.setWidth(40.0F, Sizeable.Unit.PERCENTAGE);

            UI.getCurrent().addWindow(autoAssignmentWindow);
            autoAssignmentWindow.setVisible(Boolean.TRUE);
        } else {
            notification.displayValidationError(
                    i18n.getMessage("message.permission.insufficient", SpPermission.READ_REPOSITORY));
        }
    }

    public void restoreState() {
        setFilter(uiState.getLatestSearchFilterApplied());
    }

    // TODO: remove duplication with ActionHistoryGrid
    private void initActionTypeIconMap() {
        actionTypeIconMap.put(ActionType.FORCED, new ProxyFontIcon(VaadinIcons.BOLT,
                SPUIStyleDefinitions.STATUS_ICON_FORCED, i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_FORCED)));
        actionTypeIconMap.put(ActionType.TIMEFORCED,
                new ProxyFontIcon(VaadinIcons.TIMER, SPUIStyleDefinitions.STATUS_ICON_TIME_FORCED,
                        i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_TIME_FORCED)));
        actionTypeIconMap.put(ActionType.SOFT, new ProxyFontIcon(VaadinIcons.STEP_FORWARD,
                SPUIStyleDefinitions.STATUS_ICON_SOFT, i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_SOFT)));
        actionTypeIconMap.put(ActionType.DOWNLOAD_ONLY,
                new ProxyFontIcon(VaadinIcons.DOWNLOAD, SPUIStyleDefinitions.STATUS_ICON_DOWNLOAD_ONLY,
                        i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_DOWNLOAD_ONLY)));
    }

    // TODO: remove duplication with RolloutGrid and buildActionButton()
    private static Button buildLink(final ClickListener clickListener, final String caption, final String description,
            final String buttonId, final boolean enabled) {
        final Button link = new Button();

        link.addClickListener(clickListener);
        link.setCaption(caption);
        link.setDescription(description);
        link.setEnabled(enabled);
        link.setId(buttonId);
        link.addStyleName("borderless");
        link.addStyleName("small");
        link.addStyleName("on-focus-no-border");
        link.addStyleName("link");

        return link;
    }

    // TODO: remove duplication with ActionHistoryGrid
    private Label buildTypeIcon(final ProxyTargetFilterQuery targetFilter) {
        final ProxyFontIcon actionTypeFontIcon = Optional
                .ofNullable(actionTypeIconMap.get(targetFilter.getAutoAssignActionType()))
                .orElse(new ProxyFontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
                        i18n.getMessage(UIMessageIdProvider.LABEL_UNKNOWN)));

        final String actionTypeId = new StringBuilder(UIComponentIdProvider.TARGET_FILTER_TABLE_TYPE_LABEL_ID)
                .append(".").append(targetFilter.getId()).toString();

        return buildLabelIcon(actionTypeFontIcon, actionTypeId);
    }

    // TODO: remove duplication
    private Button buildAutoAssignmentLink(final ProxyTargetFilterQuery targetFilter) {
        final ProxyDistributionSet autoAssignDistributionSet = targetFilter.getAutoAssignDistributionSet();

        final String autoAssignmenLinkCaption = autoAssignDistributionSet != null
                ? autoAssignDistributionSet.getNameVersion()
                : i18n.getMessage(UIMessageIdProvider.BUTTON_NO_AUTO_ASSIGNMENT);

        final String autoAssignmenLinkDescription = i18n
                .getMessage(UIMessageIdProvider.BUTTON_AUTO_ASSIGNMENT_DESCRIPTION);

        final String autoAssignmenLinkId = new StringBuilder("distSetButton").append('.').append(targetFilter.getId())
                .toString();

        return buildLink(clickEvent -> onClickOfAutoAssignmentLink(targetFilter), autoAssignmenLinkCaption,
                autoAssignmenLinkDescription, autoAssignmenLinkId, true);

    }

    // TODO: remove duplication with other Grids
    private Button buildActionButton(final ClickListener clickListener, final VaadinIcons icon,
            final String descriptionProperty, final String style, final String buttonId, final boolean enabled) {
        final Button actionButton = new Button();

        actionButton.addClickListener(clickListener);
        actionButton.setIcon(icon);
        actionButton.setDescription(i18n.getMessage(descriptionProperty));
        actionButton.setEnabled(enabled);
        actionButton.setId(buttonId);
        actionButton.addStyleName("tiny");
        actionButton.addStyleName("borderless");
        actionButton.addStyleName("button-no-border");
        actionButton.addStyleName("action-type-padding");
        actionButton.addStyleName(style);

        return actionButton;
    }
}
