/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag.filter;

import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUITagButtonStyle;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.management.targettag.TargetTagWindowBuilder;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet.SelectedTabChangeListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Target filter tabsheet with 'simple' and 'complex' filter options.
 */
public class MultipleTargetFilter extends Accordion implements SelectedTabChangeListener {

    private static final long serialVersionUID = 1L;

    private final TargetTagFilterButtons filterByButtons;

    private final TargetFilterQueryButtons targetFilterQueryButtonsTab;

    private final FilterByStatusLayout filterByStatusFooter;

    private final ManagementUIState managementUIState;

    private final VaadinMessageSource i18n;

    private final transient EventBus.UIEventBus eventBus;

    private VerticalLayout simpleFilterTab;

    private VerticalLayout targetTagTableLayout;

    MultipleTargetFilter(final SpPermissionChecker permChecker, final ManagementUIState managementUIState,
            final VaadinMessageSource i18n, final UIEventBus eventBus, final UINotification notification,
            final TargetFilterQueryManagement targetFilterQueryManagement,
            final TargetTagManagement targetTagManagement, final TargetManagement targetManagement,
            final TargetTagWindowBuilder targetTagWindowBuilder) {
        this.managementUIState = managementUIState;
        this.i18n = i18n;
        this.eventBus = eventBus;

        this.filterByButtons = new TargetTagFilterButtons(eventBus, managementUIState, i18n, notification, permChecker,
                targetTagManagement, targetManagement, targetTagWindowBuilder);
        this.targetFilterQueryButtonsTab = new TargetFilterQueryButtons(managementUIState, eventBus,
                targetFilterQueryManagement);
        this.filterByStatusFooter = new FilterByStatusLayout(i18n, eventBus, managementUIState);

        buildComponents();
    }

    private void buildComponents() {
        filterByStatusFooter.init();

        addStyleName(ValoTheme.ACCORDION_BORDERLESS);
        addTabs();
        setSizeFull();
        switchToTabSelectedOnLoad();
        addSelectedTabChangeListener(this);
    }

    private void switchToTabSelectedOnLoad() {
        if (managementUIState.isCustomFilterSelected()) {
            this.setSelectedTab(targetFilterQueryButtonsTab);
        } else {
            this.setSelectedTab(simpleFilterTab);
        }
    }

    private void addTabs() {
        this.addTab(getSimpleFilterTab()).setId(UIComponentIdProvider.SIMPLE_FILTER_ACCORDION_TAB);
        this.addTab(getComplexFilterTab()).setId(UIComponentIdProvider.CUSTOM_FILTER_ACCORDION_TAB);
    }

    // TODO: use AbstractFilterLayout here
    private Component getSimpleFilterTab() {
        simpleFilterTab = new VerticalLayout();
        simpleFilterTab.setSpacing(false);
        simpleFilterTab.setMargin(false);
        simpleFilterTab.setSizeFull();
        simpleFilterTab.setCaption(i18n.getMessage("caption.filter.simple"));
        simpleFilterTab.addStyleName(SPUIStyleDefinitions.SIMPLE_FILTER_HEADER);

        targetTagTableLayout = new VerticalLayout();
        targetTagTableLayout.setSpacing(false);
        targetTagTableLayout.setMargin(false);
        targetTagTableLayout.setSizeFull();
        targetTagTableLayout.setId(UIComponentIdProvider.TARGET_TAG_DROP_AREA_ID);

        targetTagTableLayout.addComponent(buildNoTagButton());
        targetTagTableLayout.addComponent(filterByButtons);
        targetTagTableLayout.setComponentAlignment(filterByButtons, Alignment.MIDDLE_CENTER);
        targetTagTableLayout.setExpandRatio(filterByButtons, 1.0F);

        simpleFilterTab.addComponent(targetTagTableLayout);
        simpleFilterTab.setExpandRatio(targetTagTableLayout, 1.0F);
        simpleFilterTab.addComponent(filterByStatusFooter);
        simpleFilterTab.setComponentAlignment(filterByStatusFooter, Alignment.MIDDLE_CENTER);

        return simpleFilterTab;
    }

    private Button buildNoTagButton() {
        final Button noTagButton = SPUIComponentProvider.getButton(
                SPUIDefinitions.TARGET_TAG_ID_PREFIXS + SPUIDefinitions.NO_TAG_BUTTON_ID,
                i18n.getMessage(UIMessageIdProvider.LABEL_NO_TAG),
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_CLICK_TO_FILTER), null, false, null,
                SPUITagButtonStyle.class);

        final ProxyTag dummyNoTag = new ProxyTag();
        dummyNoTag.setNoTag(true);

        noTagButton.addClickListener(event -> filterByButtons.getFilterButtonClickBehaviour()
                .processFilterButtonClick(event.getButton(), dummyNoTag));

        if (managementUIState.getTargetTableFilters().isNoTagSelected()) {
            filterByButtons.getFilterButtonClickBehaviour().setDefaultClickedButton(noTagButton);
        }

        return noTagButton;
    }

    private Component getComplexFilterTab() {
        targetFilterQueryButtonsTab.setCaption(i18n.getMessage(UIMessageIdProvider.CAPTION_FILTER_CUSTOM));
        return targetFilterQueryButtonsTab;
    }

    @Override
    public void selectedTabChange(final SelectedTabChangeEvent event) {
        final String selectedTabId = getTab(getSelectedTab()).getId();

        if (UIComponentIdProvider.SIMPLE_FILTER_ACCORDION_TAB.equals(selectedTabId)) {
            managementUIState.setCustomFilterSelected(false);
            eventBus.publish(this, ManagementUIEvent.RESET_TARGET_FILTER_QUERY);
        } else {
            managementUIState.setCustomFilterSelected(true);
            eventBus.publish(this, ManagementUIEvent.RESET_SIMPLE_FILTERS);
        }
    }

    public TargetTagFilterButtons getTargetTagFilterButtons() {
        return filterByButtons;
    }

    public VerticalLayout getTargetTagTableLayout() {
        return targetTagTableLayout;
    }
}
