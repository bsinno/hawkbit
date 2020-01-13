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
import org.eclipse.hawkbit.ui.management.targettag.TargetTagWindowBuilder;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Target filter tabsheet with 'simple' and 'complex' filter options.
 */
public class MultipleTargetFilter extends Accordion {
    private static final long serialVersionUID = 1L;

    private final VaadinMessageSource i18n;
    private final transient UIEventBus eventBus;

    private final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState;

    private final VerticalLayout simpleFilterTab;
    private final TargetTagFilterButtons filterByButtons;
    private final FilterByStatusLayout filterByStatusFooter;
    private final TargetFilterQueryButtons customFilterTab;

    MultipleTargetFilter(final SpPermissionChecker permChecker, final VaadinMessageSource i18n,
            final UIEventBus eventBus, final UINotification notification,
            final TargetFilterQueryManagement targetFilterQueryManagement,
            final TargetTagManagement targetTagManagement, final TargetManagement targetManagement,
            final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState,
            final TargetTagWindowBuilder targetTagWindowBuilder) {
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.targetTagFilterLayoutUiState = targetTagFilterLayoutUiState;

        this.filterByButtons = new TargetTagFilterButtons(i18n, eventBus, notification, permChecker,
                targetTagManagement, targetManagement, targetTagFilterLayoutUiState, targetTagWindowBuilder);
        this.filterByStatusFooter = new FilterByStatusLayout(i18n, eventBus, targetTagFilterLayoutUiState);
        this.simpleFilterTab = buildSimpleFilterTab();
        this.customFilterTab = new TargetFilterQueryButtons(i18n, eventBus, targetFilterQueryManagement,
                targetTagFilterLayoutUiState);

        init();
        addTabs();
        restoreState();
        addSelectedTabChangeListener(event -> selectedTabChanged());
    }

    private void init() {
        setSizeFull();
        addStyleName(ValoTheme.ACCORDION_BORDERLESS);
    }

    public void selectedTabChanged() {
        final String selectedTabId = getTab(getSelectedTab()).getId();

        if (UIComponentIdProvider.SIMPLE_FILTER_ACCORDION_TAB.equals(selectedTabId)) {
            customFilterTab.clearAppliedTargetFilterQuery();
            targetTagFilterLayoutUiState.setCustomFilterTabSelected(false);
            // TODO: publish event to disab/enable target search filter and
            // count message footer
            // eventBus.publish(this,
            // ManagementUIEvent.RESET_TARGET_FILTER_QUERY);
        } else {
            filterByButtons.clearTargetTagFilters();
            filterByStatusFooter.clearStatusAndOverdueFilters();
            targetTagFilterLayoutUiState.setCustomFilterTabSelected(true);
            // TODO: publish event to disab/enable target search filter and
            // count message footer
            // eventBus.publish(this,
            // ManagementUIEvent.RESET_SIMPLE_FILTERS);
        }
    }

    private void addTabs() {
        addTab(simpleFilterTab).setId(UIComponentIdProvider.SIMPLE_FILTER_ACCORDION_TAB);
        addTab(customFilterTab).setId(UIComponentIdProvider.CUSTOM_FILTER_ACCORDION_TAB);
    }

    private VerticalLayout buildSimpleFilterTab() {
        final VerticalLayout simpleTab = new VerticalLayout();
        simpleTab.setSpacing(false);
        simpleTab.setMargin(false);
        simpleTab.setSizeFull();
        simpleTab.setCaption(i18n.getMessage("caption.filter.simple"));
        simpleTab.addStyleName(SPUIStyleDefinitions.SIMPLE_FILTER_HEADER);

        final VerticalLayout targetTagGridLayout = new VerticalLayout();
        targetTagGridLayout.setSpacing(false);
        targetTagGridLayout.setMargin(false);
        targetTagGridLayout.setSizeFull();
        targetTagGridLayout.setId(UIComponentIdProvider.TARGET_TAG_DROP_AREA_ID);

        targetTagGridLayout.addComponent(buildNoTagButton());
        targetTagGridLayout.addComponent(filterByButtons);
        targetTagGridLayout.setComponentAlignment(filterByButtons, Alignment.MIDDLE_CENTER);
        targetTagGridLayout.setExpandRatio(filterByButtons, 1.0F);

        simpleTab.addComponent(targetTagGridLayout);
        simpleTab.setExpandRatio(targetTagGridLayout, 1.0F);

        simpleTab.addComponent(filterByStatusFooter);
        simpleTab.setComponentAlignment(filterByStatusFooter, Alignment.MIDDLE_CENTER);

        return simpleTab;
    }

    private Button buildNoTagButton() {
        final Button noTagButton = SPUIComponentProvider.getButton(
                SPUIDefinitions.TARGET_TAG_ID_PREFIXS + SPUIDefinitions.NO_TAG_BUTTON_ID,
                i18n.getMessage(UIMessageIdProvider.LABEL_NO_TAG),
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_CLICK_TO_FILTER), null, false, null,
                SPUITagButtonStyle.class);

        final ProxyTag dummyNoTag = new ProxyTag();
        dummyNoTag.setNoTag(true);

        noTagButton.addClickListener(
                event -> filterByButtons.getFilterButtonClickBehaviour().processFilterClick(dummyNoTag));

        return noTagButton;
    }

    private void restoreState() {
        if (targetTagFilterLayoutUiState.isCustomFilterTabSelected()) {
            this.setSelectedTab(customFilterTab);
            // TODO: add restoreState on targetFilterQueryButtonsTab?
        } else {
            this.setSelectedTab(simpleFilterTab);
            // TODO: add restoreState on filterByButtons and
            // filterByStatusFooter?
        }
    }

    public TargetTagFilterButtons getTargetTagFilterButtons() {
        return filterByButtons;
    }
}
