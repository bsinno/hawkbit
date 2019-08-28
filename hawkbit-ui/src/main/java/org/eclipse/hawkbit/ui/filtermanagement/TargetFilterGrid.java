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

import org.eclipse.hawkbit.im.authentication.SpPermission;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetFilterQueryToProxyTargetFilterMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.filtermanagement.event.CustomFilterUIEvent;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
import org.eclipse.hawkbit.ui.rollout.FontIcon;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

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
    private final FilterManagementUIState filterManagementUIState;
    private final transient TargetFilterQueryManagement targetFilterQueryManagement;
    private final transient TargetManagement targetManagement;

    private final Map<ActionType, FontIcon> actionTypeIconMap = new EnumMap<>(ActionType.class);

    private final ConfigurableFilterDataProvider<ProxyTargetFilterQuery, Void, String> targetFilterDataProvider;
    private final DeleteSupport<ProxyTargetFilterQuery> targetFilterDeleteSupport;

    public TargetFilterGrid(final VaadinMessageSource i18n, final UINotification notification,
            final UIEventBus eventBus, final FilterManagementUIState filterManagementUIState,
            final TargetFilterQueryManagement targetFilterQueryManagement, final TargetManagement targetManagement,
            final SpPermissionChecker permChecker) {
        super(i18n, eventBus, permChecker);

        this.notification = notification;
        this.filterManagementUIState = filterManagementUIState;
        this.targetFilterQueryManagement = targetFilterQueryManagement;
        this.targetManagement = targetManagement;

        this.targetFilterDataProvider = new TargetFilterQueryDataProvider(targetFilterQueryManagement,
                new TargetFilterQueryToProxyTargetFilterMapper()).withConfigurableFilter();

        this.targetFilterDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("caption.filter.custom"),
                permChecker, notification, this::targetFilterIdsDeletionCallback);

        initActionTypeIconMap();
        init();

        // TODO: check if relevant or should be defined in AbstractGrid
        // setStyleName("sp-table");
        // setHeight(100.0F, Unit.PERCENTAGE);
        // addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        // addStyleName(ValoTheme.TABLE_SMALL);
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.TARGET_FILTER_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyTargetFilterQuery, Void, String> getFilterDataProvider() {
        return targetFilterDataProvider;
    }

    private void targetFilterIdsDeletionCallback(final Collection<Long> targetFilterIdsToBeDeleted) {
        targetFilterIdsToBeDeleted.forEach(targetFilterQueryManagement::delete);
    }

    // TODO: remove duplication with ActionHistoryGrid
    private void initActionTypeIconMap() {
        actionTypeIconMap.put(ActionType.FORCED, new FontIcon(VaadinIcons.BOLT, SPUIStyleDefinitions.STATUS_ICON_FORCED,
                i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_FORCED)));
        actionTypeIconMap.put(ActionType.TIMEFORCED,
                new FontIcon(VaadinIcons.TIMER, SPUIStyleDefinitions.STATUS_ICON_TIME_FORCED,
                        i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_TIME_FORCED)));
        actionTypeIconMap.put(ActionType.SOFT, new FontIcon(VaadinIcons.STEP_FORWARD,
                SPUIStyleDefinitions.STATUS_ICON_SOFT, i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_SOFT)));
        actionTypeIconMap.put(ActionType.DOWNLOAD_ONLY,
                new FontIcon(VaadinIcons.DOWNLOAD, SPUIStyleDefinitions.STATUS_ICON_DOWNLOAD_ONLY,
                        i18n.getMessage(UIMessageIdProvider.CAPTION_ACTION_DOWNLOAD_ONLY)));
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final CustomFilterUIEvent filterEvent) {
        if (filterEvent == CustomFilterUIEvent.FILTER_BY_CUST_FILTER_TEXT
                || filterEvent == CustomFilterUIEvent.FILTER_BY_CUST_FILTER_TEXT_REMOVE
                || filterEvent == CustomFilterUIEvent.CREATE_TARGET_FILTER_QUERY
                || filterEvent == CustomFilterUIEvent.UPDATED_TARGET_FILTER_QUERY) {
            UI.getCurrent().access(this::refreshFilter);
        }
    }

    private void refreshFilter() {
        getFilterDataProvider().setFilter(getSearchTextFromUiState());
    }

    private String getSearchTextFromUiState() {
        return filterManagementUIState.getCustomFilterSearchText()
                .filter(searchText -> !StringUtils.isEmpty(searchText)).map(value -> String.format("%%%s%%", value))
                .orElse(null);
    }

    @Override
    public void addColumns() {

        addComponentColumn(this::buildFilterLink).setId(FILTER_NAME_ID).setCaption(i18n.getMessage("header.name"))
                .setExpandRatio(2);

        addColumn(ProxyTargetFilterQuery::getCreatedBy).setId(FILTER_CREATED_BY_ID)
                .setCaption(i18n.getMessage("header.createdBy")).setExpandRatio(1);

        addColumn(ProxyTargetFilterQuery::getCreatedDate).setId(FILTER_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate")).setExpandRatio(2);

        addColumn(ProxyTargetFilterQuery::getLastModifiedBy).setId(FILTER_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setExpandRatio(1);

        addColumn(ProxyTargetFilterQuery::getModifiedDate).setId(FILTER_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setExpandRatio(2);

        addComponentColumn(this::buildTypeIcon).setId(FILTER_AUTOASSIGNMENT_TYPE_ID)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);

        addComponentColumn(this::buildAutoAssignmentLink).setId(FILTER_AUTOASSIGNMENT_DS_ID)
                .setStyleGenerator(item -> AbstractGrid.CENTER_ALIGN);

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

    // TODO: remove duplication with RolloutGrid and buildActionButton()
    private Button buildLink(final ClickListener clickListener, final String caption, final String description,
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

    private void onClickOfFilterName(final ProxyTargetFilterQuery targetFilter) {
        filterManagementUIState.setFilterQueryValue(targetFilter.getQuery());
        filterManagementUIState.setTfQuery(targetFilter);
        filterManagementUIState.setEditViewDisplayed(true);
        eventBus.publish(this, CustomFilterUIEvent.TARGET_FILTER_DETAIL_VIEW);
    }

    // TODO: remove duplication with ActionHistoryGrid
    private Label buildTypeIcon(final ProxyTargetFilterQuery targetFilter) {
        final FontIcon actionTypeFontIcon = Optional
                .ofNullable(actionTypeIconMap.get(targetFilter.getAutoAssignActionType()))
                .orElse(new FontIcon(VaadinIcons.QUESTION_CIRCLE, SPUIStyleDefinitions.STATUS_ICON_BLUE,
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

        return buildLink(clickEvent -> onClickOfAutoAssignmentLink(targetFilter.getId()), autoAssignmenLinkCaption,
                autoAssignmenLinkDescription, autoAssignmenLinkId, true);

    }

    private void onClickOfAutoAssignmentLink(final Long targetFilterId) {
        if (permissionChecker.hasReadRepositoryPermission()) {
            final DistributionSetSelectWindow dsSelectWindow = new DistributionSetSelectWindow(i18n, eventBus,
                    notification, targetManagement, targetFilterQueryManagement);
            dsSelectWindow.showForTargetFilter(targetFilterId);
        } else {
            notification.displayValidationError(
                    i18n.getMessage("message.permission.insufficient", SpPermission.READ_REPOSITORY));
        }
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