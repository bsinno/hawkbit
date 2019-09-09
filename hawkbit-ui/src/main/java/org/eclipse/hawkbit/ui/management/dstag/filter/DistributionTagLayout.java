/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag.filter;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.DistributionSetTagFilterHeaderEvent;
import org.eclipse.hawkbit.ui.common.event.FilterHeaderEvent.FilterHeaderEnum;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterLayout;
import org.eclipse.hawkbit.ui.components.RefreshableContainer;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUITagButtonStyle;
import org.eclipse.hawkbit.ui.management.event.DistributionSetTagTableEvent;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

/**
 * Layout for Distribution Tags
 *
 */
public class DistributionTagLayout extends AbstractFilterLayout implements RefreshableContainer {
    private static final long serialVersionUID = 1L;

    private final VaadinMessageSource i18n;
    private final ManagementUIState managementUIState;

    private final DistributionTagFilterHeader distributionTagFilterHeader;
    private final Button noTagButton;
    private final DistributionTagButtons distributionTagButtons;

    /**
     * Constructor
     * 
     * @param eventBus
     *            UIEventBus
     * @param managementUIState
     *            ManagementUIState
     * @param i18n
     *            VaadinMessageSource
     * @param permChecker
     *            SpPermissionChecker
     * @param distributionSetTagManagement
     *            DistributionSetTagManagement
     * @param entityFactory
     *            EntityFactory
     * @param uiNotification
     *            UINotification
     */
    public DistributionTagLayout(final UIEventBus eventBus, final ManagementUIState managementUIState,
            final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final DistributionSetTagManagement distributionSetTagManagement, final EntityFactory entityFactory,
            final UINotification uiNotification, final DistributionSetManagement distributionSetManagement) {
        super(eventBus);

        this.i18n = i18n;
        this.managementUIState = managementUIState;

        this.noTagButton = buildNoTagButton();
        // TODO: check if we could find better solution as to pass
        // distributionTagButtons into distributionTagFilterHeader
        this.distributionTagButtons = new DistributionTagButtons(eventBus, managementUIState, entityFactory, i18n,
                uiNotification, permChecker, distributionSetTagManagement, distributionSetManagement);
        this.distributionTagFilterHeader = new DistributionTagFilterHeader(i18n, managementUIState, permChecker,
                eventBus, distributionSetTagManagement, entityFactory, uiNotification, distributionTagButtons);

        buildLayout();
        restoreState();
    }

    @Override
    protected DistributionTagFilterHeader getFilterHeader() {
        return distributionTagFilterHeader;
    }

    // TODO: remove duplication with other type layouts
    @Override
    protected Component getFilterButtons() {
        final VerticalLayout filterButtonsLayout = new VerticalLayout();
        filterButtonsLayout.setMargin(false);
        filterButtonsLayout.setSpacing(false);

        filterButtonsLayout.addComponent(noTagButton);
        filterButtonsLayout.addComponent(distributionTagButtons);

        filterButtonsLayout.setComponentAlignment(noTagButton, Alignment.TOP_LEFT);
        filterButtonsLayout.setComponentAlignment(distributionTagButtons, Alignment.TOP_LEFT);

        filterButtonsLayout.setExpandRatio(distributionTagButtons, 1.0F);

        return filterButtonsLayout;
    }

    // TODO: remove duplication with MultipleTargetFilter
    private Button buildNoTagButton() {
        final Button noTagButton = SPUIComponentProvider.getButton(
                SPUIDefinitions.DISTRIBUTION_TAG_ID_PREFIXS + SPUIDefinitions.NO_TAG_BUTTON_ID,
                i18n.getMessage(UIMessageIdProvider.LABEL_NO_TAG),
                i18n.getMessage(UIMessageIdProvider.TOOLTIP_CLICK_TO_FILTER), null, false, null,
                SPUITagButtonStyle.class);

        final ProxyTag dummyNoTag = new ProxyTag();
        dummyNoTag.setNoTag(true);

        noTagButton.addClickListener(event -> distributionTagButtons.getFilterButtonClickBehaviour()
                .processFilterButtonClick(event.getButton(), dummyNoTag));

        if (managementUIState.getDistributionTableFilters().isNoTagSelected()) {
            distributionTagButtons.getFilterButtonClickBehaviour().setDefaultClickedButton(noTagButton);
        }

        return noTagButton;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent event) {
        if (event == ManagementUIEvent.HIDE_DISTRIBUTION_TAG_LAYOUT) {
            managementUIState.setDistTagFilterClosed(true);
            setVisible(false);
        }
        if (event == ManagementUIEvent.SHOW_DISTRIBUTION_TAG_LAYOUT) {
            managementUIState.setDistTagFilterClosed(false);
            setVisible(true);
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onDistributionSetTagTableEvent(final DistributionSetTagTableEvent distributionSetTagTableEvent) {
        refreshContainer();
        eventBus.publish(this, new DistributionSetTagFilterHeaderEvent(FilterHeaderEnum.SHOW_MENUBAR));
    }

    @Override
    public Boolean isFilterLayoutClosedOnLoad() {
        return managementUIState.isDistTagFilterClosed();
    }

    @Override
    public void refreshContainer() {
        distributionTagButtons.refreshContainer();
    }
}
