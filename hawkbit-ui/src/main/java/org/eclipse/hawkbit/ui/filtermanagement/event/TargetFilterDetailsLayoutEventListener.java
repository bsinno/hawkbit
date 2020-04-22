/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.filtermanagement.event;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetFilterQuery;
import org.eclipse.hawkbit.ui.common.event.CommandTopics;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload;
import org.eclipse.hawkbit.ui.common.event.LayoutVisibilityEventPayload.VisibilityType;
import org.eclipse.hawkbit.ui.common.event.ShowFormEventPayload;
import org.eclipse.hawkbit.ui.common.event.ShowFormEventPayload.FormType;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.filtermanagement.TargetFilterDetailsLayout;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

public class TargetFilterDetailsLayoutEventListener {
    private final TargetFilterDetailsLayout targetFilterDetailsLayout;
    private final UIEventBus eventBus;
    private final List<Object> eventListeners;

    public TargetFilterDetailsLayoutEventListener(final TargetFilterDetailsLayout targetFilterDetailsLayout,
            final UIEventBus eventBus) {
        this.targetFilterDetailsLayout = targetFilterDetailsLayout;
        this.eventBus = eventBus;

        this.eventListeners = new ArrayList<>();
        registerEventListeners();
    }

    private void registerEventListeners() {
        eventListeners.add(new ShowTargetFilterQueryFormLayoutListener());
    }

    private class ShowTargetFilterQueryFormLayoutListener {
        public ShowTargetFilterQueryFormLayoutListener() {
            eventBus.subscribe(this, CommandTopics.SHOW_ENTITY_FORM_LAYOUT);
        }

        @EventBusListenerMethod(scope = EventScope.UI)
        private void onShowFormEvent(final ShowFormEventPayload<ProxyTargetFilterQuery> eventPayload) {
            if (eventPayload.getView() != EventView.TARGET_FILTER
                    || eventPayload.getEntityType() != ProxyTargetFilterQuery.class) {
                return;
            }

            if (eventPayload.getFormType() == FormType.ADD) {
                targetFilterDetailsLayout.showAddFilterUi();
            } else {
                targetFilterDetailsLayout.showEditFilterUi(eventPayload.getEntity());
            }

            eventBus.publish(CommandTopics.CHANGE_LAYOUT_VISIBILITY, this, new LayoutVisibilityEventPayload(
                    VisibilityType.SHOW, EventLayout.TARGET_FILTER_QUERY_FORM, EventView.TARGET_FILTER));
        }
    }

    public void unsubscribeListeners() {
        eventListeners.forEach(eventBus::unsubscribe);
    }
}
