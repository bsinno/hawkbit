/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.components;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PreDestroy;

import org.eclipse.hawkbit.repository.event.TenantAwareEvent;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.common.table.BaseUIEntityEvent;
import org.eclipse.hawkbit.ui.menu.DashboardMenuItem;
import org.eclipse.hawkbit.ui.push.EventContainer;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.VerticalLayout;

/**
 * Abstract view for all views, which show notifications.
 */
public abstract class AbstractNotificationView extends VerticalLayout implements View {
    private static final long serialVersionUID = 1L;

    private final EnumSet<BaseEntityEventType> entityModifiedEvents = EnumSet.of(BaseEntityEventType.ADD_ENTITY,
            BaseEntityEventType.REMOVE_ENTITY, BaseEntityEventType.UPDATED_ENTITY);

    protected final transient EventBus.UIEventBus eventBus;

    private final transient Cache<BaseUIEntityEvent<?>, Object> skipUiEventsCache;

    private final NotificationUnreadButton notificationUnreadButton;

    private final AtomicInteger viewUnreadNotifications;

    private transient Map<Class<?>, RefreshableContainer> supportedEvents;

    /**
     * Constructor.
     * 
     * @param eventBus
     *            the ui event bus
     * @param notificationUnreadButton
     *            the notificationUnreadButton
     */
    public AbstractNotificationView(final EventBus.UIEventBus eventBus,
            final NotificationUnreadButton notificationUnreadButton) {
        this.eventBus = eventBus;
        this.notificationUnreadButton = notificationUnreadButton;
        this.viewUnreadNotifications = new AtomicInteger(0);

        this.skipUiEventsCache = CacheBuilder.newBuilder().expireAfterAccess(10, SECONDS).build();

        if (doSubscribeToEventBus()) {
            eventBus.subscribe(this);
        }

        setMargin(false);
        setSpacing(false);
    }

    /**
     * Subscribes the view to the eventBus. Method has to be overriden (return
     * false) if the view does not contain any listener to avoid Vaadin blowing
     * up our logs with warnings.
     */
    protected boolean doSubscribeToEventBus() {
        return true;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEventContainerEvent(final EventContainer<?> eventContainer) {
        if (!supportNotificationEventContainer(eventContainer.getClass()) || eventContainer.getEvents().isEmpty()) {
            return;
        }

        eventContainer.getEvents().stream().filter(Objects::nonNull).filter(this::noEventMatch).forEach(event -> {
            notificationUnreadButton.incrementUnreadNotification(this, eventContainer);
            viewUnreadNotifications.incrementAndGet();
        });
        getDashboardMenuItem().setNotificationUnreadValue(viewUnreadNotifications);
    }

    private boolean supportNotificationEventContainer(final Class<?> eventContainerClass) {
        return getSupportedEvents().containsKey(eventContainerClass);
    }

    private Map<Class<?>, RefreshableContainer> getSupportedEvents() {
        if (supportedEvents == null) {
            supportedEvents = getSupportedPushEvents();
        }

        return supportedEvents;
    }

    /**
     * @return a map with all supported events and this related component which
     *         should be refreshed after a change.
     */
    protected abstract Map<Class<?>, RefreshableContainer> getSupportedPushEvents();

    private boolean noEventMatch(final TenantAwareEvent tenantAwareEvent) {
        return skipUiEventsCache.asMap().keySet().stream()
                .noneMatch(uiEvent -> uiEvent.matchRemoteEvent(tenantAwareEvent));
    }

    /**
     * @return the related dashboard menu item for this view.
     */
    protected abstract DashboardMenuItem getDashboardMenuItem();

    @EventBusListenerMethod(scope = EventScope.UI)
    void onUiEvent(final BaseUIEntityEvent<?> event) {
        if (!entityModifiedEvents.contains(event.getEventType())) {
            return;
        }

        skipUiEventsCache.put(event, new Object());
    }

    /**
     * Refresh the view by event container changes.
     * 
     * @param eventContainers
     *            event container which container changed
     * 
     */
    public void refreshView(final Set<Class<?>> eventContainers) {
        eventContainers.stream().filter(this::supportNotificationEventContainer).forEach(this::refreshContainer);
        clear();
    }

    private void refreshContainer(final Class<?> containerClazz) {
        getSupportedEvents().get(containerClazz).refreshContainer();
    }

    private void clear() {
        viewUnreadNotifications.set(0);
        getDashboardMenuItem().setNotificationUnreadValue(viewUnreadNotifications);
    }

    /**
     * Refresh the view by event container changes.
     */
    public void refreshView() {
        if (viewUnreadNotifications.get() <= 0) {
            return;
        }

        refreshAllContainers();
        clear();
    }

    private void refreshAllContainers() {
        getSupportedEvents().values().stream().forEach(RefreshableContainer::refreshContainer);
    }

    @Override
    public void enter(final ViewChangeEvent event) {
        // intended to be overridden
    }

    @PreDestroy
    protected void destroy() {
        eventBus.unsubscribe(this);
    }
}
