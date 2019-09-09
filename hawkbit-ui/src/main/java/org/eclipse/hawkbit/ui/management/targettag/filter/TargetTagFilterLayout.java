/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag.filter;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.event.FilterHeaderEvent.FilterHeaderEnum;
import org.eclipse.hawkbit.ui.common.event.TargetTagFilterHeaderEvent;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterLayout;
import org.eclipse.hawkbit.ui.components.RefreshableContainer;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTagTableEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.Component;

/**
 * Target Tag filter layout.
 */
public class TargetTagFilterLayout extends AbstractFilterLayout implements RefreshableContainer {
    private static final long serialVersionUID = 1L;

    private final ManagementUIState managementUIState;

    private final TargetTagFilterHeader targetTagFilterHeader;
    private final MultipleTargetFilter multipleTargetFilter;

    /**
     * Constructor
     * 
     * @param i18n
     *            VaadinMessageSource
     * @param managementUIState
     *            ManagementUIState
     * @param permChecker
     *            SpPermissionChecker
     * @param eventBus
     *            UIEventBus
     * @param notification
     *            UINotification
     * @param entityFactory
     *            EntityFactory
     * @param targetFilterQueryManagement
     *            TargetFilterQueryManagement
     * @param targetTagManagement
     *            TargetTagManagement
     */
    public TargetTagFilterLayout(final VaadinMessageSource i18n, final ManagementUIState managementUIState,
            final SpPermissionChecker permChecker, final UIEventBus eventBus, final UINotification notification,
            final EntityFactory entityFactory, final TargetFilterQueryManagement targetFilterQueryManagement,
            final TargetTagManagement targetTagManagement, final TargetManagement targetManagement) {
        super(eventBus);

        this.managementUIState = managementUIState;

        // TODO: check if we could find better solution as to pass
        // targetTagButtons into targetTagFilterHeader
        this.multipleTargetFilter = new MultipleTargetFilter(permChecker, managementUIState, i18n, eventBus,
                notification, entityFactory, targetFilterQueryManagement, targetTagManagement, targetManagement);
        this.targetTagFilterHeader = new TargetTagFilterHeader(i18n, managementUIState, permChecker, eventBus,
                notification, entityFactory, targetTagManagement, multipleTargetFilter.getTargetTagFilterButtons());

        buildLayout();
        restoreState();
    }

    @Override
    protected TargetTagFilterHeader getFilterHeader() {
        return targetTagFilterHeader;
    }

    @Override
    protected Component getFilterButtons() {
        return multipleTargetFilter;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ManagementUIEvent event) {
        if (event == ManagementUIEvent.HIDE_TARGET_TAG_LAYOUT) {
            managementUIState.setTargetTagFilterClosed(true);
            setVisible(false);
        }
        if (event == ManagementUIEvent.SHOW_TARGET_TAG_LAYOUT) {
            managementUIState.setTargetTagFilterClosed(false);
            setVisible(true);
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onTargetTagTableEvent(final TargetTagTableEvent tableEvent) {
        refreshContainer();
        eventBus.publish(this, new TargetTagFilterHeaderEvent(FilterHeaderEnum.SHOW_MENUBAR));
    }

    @Override
    public Boolean isFilterLayoutClosedOnLoad() {
        return managementUIState.isTargetTagFilterClosed();
    }

    @Override
    public void refreshContainer() {
        multipleTargetFilter.getTargetTagFilterButtons().refreshContainer();
    }
}
