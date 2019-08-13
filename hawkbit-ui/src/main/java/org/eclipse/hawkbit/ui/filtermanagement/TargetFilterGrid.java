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

import org.eclipse.hawkbit.im.authentication.SpPermission;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.filters.SearchTextFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetFilterQueryToProxyTargetFilterMapper;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorderWithIcon;
import org.eclipse.hawkbit.ui.filtermanagement.event.CustomFilterUIEvent;
import org.eclipse.hawkbit.ui.filtermanagement.state.FilterManagementUIState;
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
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Concrete implementation of TargetFilter grid which is displayed on the
 * Filtermanagement View.
 */
public class TargetFilterGrid extends AbstractGrid<ProxyTargetFilterQuery, SearchTextFilterParams> {

    private static final long serialVersionUID = 1L;

    private static final String FILTER_NAME_ID = "filterName";
    private static final String FILTER_CREATED_BY_ID = "filterCreatedBy";
    private static final String FILTER_CREATED_DATE_ID = "filterCreatedDate";
    private static final String FILTER_MODIFIED_BY_ID = "filterModifiedBy";
    private static final String FILTER_MODIFIED_DATE_ID = "filterModifiedDate";
    private static final String FILTER_AUTOASSIGNMENT_ID = "filterAutoAssignment";
    private static final String FILTER_DELETE_BUTTON_ID = "filterDeleteButton";

    private final VaadinMessageSource i18n;

    private final UINotification notification;

    private final transient UIEventBus eventBus;

    private final FilterManagementUIState filterManagementUIState;

    private final transient TargetFilterQueryManagement targetFilterQueryManagement;

    private final transient TargetManagement targetManagement;

    private final SpPermissionChecker permChecker;

    private final DeleteSupport<ProxyTargetFilterQuery> targetFilterDeleteSupport;

    private final ConfigurableFilterDataProvider<ProxyTargetFilterQuery, Void, SearchTextFilterParams> targetFilterDataProvider;

    public TargetFilterGrid(final VaadinMessageSource i18n, final UINotification notification,
            final UIEventBus eventBus, final FilterManagementUIState filterManagementUIState,
            final TargetFilterQueryManagement targetFilterQueryManagement, final TargetManagement targetManagement,
            final SpPermissionChecker permChecker) {
        super(i18n, eventBus, permChecker);

        this.i18n = i18n;
        this.notification = notification;
        this.eventBus = eventBus;
        this.filterManagementUIState = filterManagementUIState;
        this.targetFilterQueryManagement = targetFilterQueryManagement;
        this.targetManagement = targetManagement;
        this.permChecker = permChecker;

        this.targetFilterDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("caption.filter.custom"),
                permChecker, notification, this::targetFilterIdsDeletionCallback);

        this.targetFilterDataProvider = new TargetFilterQueryDataProvider(targetFilterQueryManagement,
                new TargetFilterQueryToProxyTargetFilterMapper()).withConfigurableFilter();

        setStyleName("sp-table");
        setSizeFull();
        setHeight(100.0F, Unit.PERCENTAGE);
        addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        addStyleName(ValoTheme.TABLE_SMALL);
        setId(UIComponentIdProvider.TARGET_FILTER_TABLE_ID);
        eventBus.subscribe(this);

        init();
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.TARGET_FILTER_TABLE_ID;
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

    @Override
    public void addColumns() {

        addComponentColumn(targetFilter -> buildLinkButton(this::onClickOfDetailButton,
                UIMessageIdProvider.TOOLTIP_UPDATE_CUSTOM_FILTER, getDetailLinkId(targetFilter.getName()),
                targetFilter.getName(), targetFilter, true)).setId(FILTER_NAME_ID)
                        .setCaption(i18n.getMessage("header.name")).setExpandRatio(2);

        addColumn(ProxyTargetFilterQuery::getCreatedBy).setId(FILTER_CREATED_BY_ID)
                .setCaption(i18n.getMessage("header.createdBy")).setExpandRatio(1);

        addColumn(ProxyTargetFilterQuery::getCreatedDate).setId(FILTER_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate")).setExpandRatio(2);

        addColumn(ProxyTargetFilterQuery::getLastModifiedBy).setId(FILTER_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy")).setExpandRatio(1);

        addColumn(ProxyTargetFilterQuery::getModifiedDate).setId(FILTER_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate")).setExpandRatio(2);

        addComponentColumn(targetFilter -> customFilterDistributionSetButton(targetFilter))
                .setId(FILTER_AUTOASSIGNMENT_ID).setCaption(i18n.getMessage("header.auto.assignment.ds"))
                .setExpandRatio(1);

        addComponentColumn(targetFilter -> buildActionButton(
                clickEvent -> targetFilterDeleteSupport.openConfirmationWindowDeleteAction(targetFilter),
                VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.CUSTOM_FILTER_DELETE_ICON + "." + targetFilter.getId(),
                targetFilterDeleteSupport.hasDeletePermission())).setId(FILTER_DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.delete")).setExpandRatio(1);
    }

    // TODO move to AbstractGrid or GridUtils
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

    // TODO move to AbstractGrid or GridUtils
    private Button buildLinkButton(final ClickListener clickListener, final String descriptionMessageId,
            final String buttonId, final String buttonName, final Object data, final boolean enabled) {
        final Button actionButton = SPUIComponentProvider.getButton(buttonId, buttonName,
                i18n.getMessage(descriptionMessageId), null, false, null, SPUIButtonStyleNoBorder.class);

        actionButton.addClickListener(clickListener);
        actionButton.setEnabled(enabled);
        actionButton.addStyleName(ValoTheme.LINK_SMALL + " " + "on-focus-no-border link");
        actionButton.setData(data);

        return actionButton;
    }

    // TODO move to AbstractGrid or GridUtils
    private Button buildLinkButton(final ClickListener clickListener, final VaadinIcons icon,
            final String descriptionMessageId, final String buttonId, final String buttonName, final Object data,
            final boolean enabled) {
        final Button actionButton = SPUIComponentProvider.getButton(buttonId, buttonName,
                i18n.getMessage(descriptionMessageId), null, false, null, SPUIButtonStyleNoBorderWithIcon.class);

        actionButton.addClickListener(clickListener);
        actionButton.setEnabled(enabled);
        actionButton.addStyleName(ValoTheme.LINK_SMALL + " " + "on-focus-no-border link");
        actionButton.setData(data);
        actionButton.setIcon(icon);

        return actionButton;
    }

    private Button customFilterDistributionSetButton(final ProxyTargetFilterQuery targetFilter) {
        final ProxyDistributionSet autoAssignDistributionSet = targetFilter.getAutoAssignDistributionSet();
        final ActionType actionType = targetFilter.getAutoAssignActionType();

        final String buttonId = "distSetButton";
        Button updateButton;
        if (autoAssignDistributionSet == null) {
            updateButton = buildLinkButton(this::onClickOfDistributionSetButton,
                    i18n.getMessage(UIMessageIdProvider.BUTTON_AUTO_ASSIGNMENT_DESCRIPTION), buttonId,
                    i18n.getMessage(UIMessageIdProvider.BUTTON_NO_AUTO_ASSIGNMENT), targetFilter, true);
        } else if (actionType == ActionType.FORCED) {
            updateButton = buildLinkButton(this::onClickOfDistributionSetButton, VaadinIcons.BOLT,
                    i18n.getMessage(UIMessageIdProvider.BUTTON_AUTO_ASSIGNMENT_DESCRIPTION), buttonId,
                    autoAssignDistributionSet.getNameVersion(), targetFilter, true);
            updateButton.setSizeUndefined();
        } else {
            updateButton = buildLinkButton(this::onClickOfDistributionSetButton,
                    i18n.getMessage(UIMessageIdProvider.BUTTON_AUTO_ASSIGNMENT_DESCRIPTION), buttonId,
                    autoAssignDistributionSet.getNameVersion(), targetFilter, true);
            updateButton.setSizeUndefined();
        }

        return updateButton;
    }

    @Override
    public void refreshContainer() {
        super.refreshContainer();
    }

    private void refreshFilter() {
        final SearchTextFilterParams filterParams = new SearchTextFilterParams(getSearchTextFromUiState());
        getFilterDataProvider().setFilter(filterParams);
    }

    private String getSearchTextFromUiState() {
        return filterManagementUIState.getCustomFilterSearchText()
                .filter(searchText -> !StringUtils.isEmpty(searchText)).map(value -> String.format("%%%s%%", value))
                .orElse(null);
    }

    private void onClickOfDistributionSetButton(final ClickEvent event) {
        final ProxyTargetFilterQuery targetFilter = (ProxyTargetFilterQuery) event.getButton().getData();

        if (permChecker.hasReadRepositoryPermission()) {
            final DistributionSetSelectWindow dsSelectWindow = new DistributionSetSelectWindow(i18n, eventBus,
                    notification, targetManagement, targetFilterQueryManagement);
            dsSelectWindow.showForTargetFilter(targetFilter.getId());
        } else {
            notification.displayValidationError(
                    i18n.getMessage("message.permission.insufficient", SpPermission.READ_REPOSITORY));
        }
    }

    private void onClickOfDetailButton(final ClickEvent event) {
        final String targetFilterName = (String) ((Button) event.getComponent()).getData();
        targetFilterQueryManagement.getByName(targetFilterName).ifPresent(targetFilterQuery -> {
            filterManagementUIState.setFilterQueryValue(targetFilterQuery.getQuery());
            filterManagementUIState.setTfQuery(targetFilterQuery);
            filterManagementUIState.setEditViewDisplayed(true);
            eventBus.publish(this, CustomFilterUIEvent.TARGET_FILTER_DETAIL_VIEW);
        });
    }

    private static String getDetailLinkId(final String filterName) {
        return new StringBuilder(UIComponentIdProvider.CUSTOM_FILTER_DETAIL_LINK).append('.').append(filterName)
                .toString();
    }

    private void targetFilterIdsDeletionCallback(final Collection<Long> targetFilterIdsToBeDeleted) {
        targetFilterIdsToBeDeleted.forEach(id -> targetFilterQueryManagement.delete(id));
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyTargetFilterQuery, Void, SearchTextFilterParams> getFilterDataProvider() {
        return null;
    }

}