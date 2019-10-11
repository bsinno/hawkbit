/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.components;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.RemoteEventsMatcher.EntityModifiedEventPayloadIdentifier;
import org.eclipse.hawkbit.ui.push.EventContainer;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Button which shows all notification in a popup.
 */
@SpringComponent
@UIScope
public class NotificationUnreadButton extends Button {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(NotificationUnreadButton.class);

    private static final String TITLE = "notification.unread.button.title";
    private static final String DESCRIPTION = "notification.unread.button.description";

    private static final String STYLE = "notifications-unread";
    private static final String STYLE_UNREAD_COUNTER = "unread";
    private static final String STYLE_POPUP = "notifications-unread-popup";
    private static final String STYLE_NO_CLOSEBOX = "no-closebox";

    private final transient VaadinMessageSource i18n;
    private final transient UIEventBus eventBus;

    private int unreadNotificationCounter;
    private AbstractNotificationView currentView;
    private Window notificationsWindow;
    private transient Map<Class<?>, NotificationUnreadValue> unreadNotifications;
    private final transient Map<EntityModifiedEventPayloadIdentifier, Collection<Long>> remotelyOriginatedEventsStore;

    /**
     * Constructor.
     * 
     * @param i18n
     *            i18n
     */
    @Autowired
    NotificationUnreadButton(final VaadinMessageSource i18n, final UIEventBus eventBus) {
        this.i18n = i18n;
        this.eventBus = eventBus;

        this.unreadNotifications = new ConcurrentHashMap<>();
        this.remotelyOriginatedEventsStore = new ConcurrentHashMap<>();

        setId(UIComponentIdProvider.NOTIFICATION_UNREAD_ID);
        setIcon(VaadinIcons.BELL);
        setCaptionAsHtml(true);
        setEnabled(false);
        addStyleName(ValoTheme.BUTTON_SMALL);
        addStyleName(STYLE);

        createNotificationWindow();
        addClickListener(this::dispatchRemotelyOriginatedEvents);
        // TODO: remove after refactoring all Views
        addClickListener(this::toggleWindow);
    }

    private void createNotificationWindow() {
        notificationsWindow = new Window();

        notificationsWindow.setId(UIComponentIdProvider.NOTIFICATION_UNREAD_POPUP_ID);
        notificationsWindow.setWidth(300.0F, Unit.PIXELS);
        notificationsWindow.setClosable(true);
        notificationsWindow.setResizable(false);
        notificationsWindow.setDraggable(false);
        notificationsWindow.addStyleName(STYLE_POPUP);
        notificationsWindow.addStyleName(STYLE_NO_CLOSEBOX);

        notificationsWindow.addCloseListener(event -> refreshCaption());
        notificationsWindow.addBlurListener(this::closeWindow);
    }

    private void refreshCaption() {
        setCaption(null);
        setEnabled(notificationsWindow.isAttached());
        if (unreadNotificationCounter > 0) {
            setVisible(true);
            setEnabled(true);
            setCaption("<div class='" + STYLE_UNREAD_COUNTER + "'>" + unreadNotificationCounter + "</div>");
        }
        setDescription(i18n.getMessage(DESCRIPTION, unreadNotificationCounter));
    }

    private void closeWindow(final BlurEvent event) {
        getUI().removeWindow((Window) event.getComponent());
    }

    private void toggleWindow(final ClickEvent event) {
        if (notificationsWindow.isAttached()) {
            getUI().removeWindow(notificationsWindow);
            return;
        }

        createUnreadMessagesLayout();
        notificationsWindow.setPositionY(event.getClientY() - event.getRelativeY() + 40);
        getUI().addWindow(notificationsWindow);

        if (currentView != null) {
            currentView.refreshView(unreadNotifications.keySet());
        }

        clear();
        notificationsWindow.focus();
    }

    private void createUnreadMessagesLayout() {
        final VerticalLayout notificationsLayout = new VerticalLayout();
        notificationsLayout.setMargin(true);
        notificationsLayout.setSpacing(true);

        final Label title = new Label(i18n.getMessage(TITLE));
        title.addStyleName(ValoTheme.LABEL_H3);
        title.addStyleName(ValoTheme.LABEL_NO_MARGIN);

        notificationsLayout.addComponent(title);

        unreadNotifications.values().stream().forEach(value -> createNotification(notificationsLayout, value));
        notificationsWindow.setContent(notificationsLayout);
    }

    private void createNotification(final VerticalLayout notificationsLayout,
            final NotificationUnreadValue notificationUnreadValue) {
        final Label contentLabel = new Label(notificationUnreadValue.getUnreadNotifications() + " "
                + i18n.getMessage(notificationUnreadValue.getUnreadNotificationMessageKey()));
        notificationsLayout.addComponent(contentLabel);
    }

    private void clear() {
        unreadNotificationCounter = 0;
        unreadNotifications.clear();
        remotelyOriginatedEventsStore.clear();
        refreshCaption();
    }

    private void dispatchRemotelyOriginatedEvents(final ClickEvent event) {
        if (notificationsWindow.isAttached()) {
            getUI().removeWindow(notificationsWindow);
            return;
        }

        notificationsWindow.setContent(buildNotificationsWindowLayout());
        notificationsWindow.setPositionY(event.getClientY() - event.getRelativeY() + 40);
        getUI().addWindow(notificationsWindow);

        dispatchEntityModifiedEvents();

        clear();
        notificationsWindow.focus();
    }

    private VerticalLayout buildNotificationsWindowLayout() {
        final VerticalLayout notificationsLayout = new VerticalLayout();
        notificationsLayout.setMargin(true);
        notificationsLayout.setSpacing(true);

        final Label title = new Label(i18n.getMessage(TITLE));
        title.addStyleName(ValoTheme.LABEL_H3);
        title.addStyleName(ValoTheme.LABEL_NO_MARGIN);

        notificationsLayout.addComponent(title);

        final Label[] eventNotificationLabels = remotelyOriginatedEventsStore.entrySet().stream()
                .map(this::buildEventNotificationLabel).toArray(Label[]::new);

        notificationsLayout.addComponents(eventNotificationLabels);

        return notificationsLayout;
    }

    private Label buildEventNotificationLabel(
            final Entry<EntityModifiedEventPayloadIdentifier, Collection<Long>> remotelyOriginatedEvent) {
        return new Label(remotelyOriginatedEvent.getValue().size() + " "
                + i18n.getMessage(remotelyOriginatedEvent.getKey().getEventTypeMessageKey()));
    }

    private void dispatchEntityModifiedEvents() {
        final List<EntityModifiedEventPayload> eventPayloads = remotelyOriginatedEventsStore.entrySet().stream()
                .map(this::buildEntityModifiedEventPayload).filter(Objects::nonNull).collect(Collectors.toList());

        // TODO: check if the sender is correct
        eventPayloads
                .forEach(eventPayload -> eventBus.publish(EventTopics.ENTITY_MODIFIED, UI.getCurrent(), eventPayload));
    }

    private EntityModifiedEventPayload buildEntityModifiedEventPayload(
            final Entry<EntityModifiedEventPayloadIdentifier, Collection<Long>> remotelyOriginatedEvent) {
        final EntityModifiedEventPayloadIdentifier eventIdentifier = remotelyOriginatedEvent.getKey();
        final Collection<Long> entityIds = remotelyOriginatedEvent.getValue();

        try {
            return eventIdentifier.getEventPayloadType()
                    .getDeclaredConstructor(EntityModifiedEventType.class, Collection.class)
                    .newInstance(eventIdentifier.getModifiedEventType(), entityIds);
        } catch (final ReflectiveOperationException e) {
            LOG.error(
                    "Failed to create EntityModifiedEventPayload for received remote event identified by {} with entity ids {}!",
                    eventIdentifier, entityIds);
        }

        return null;
    }

    public void setCurrentView(final View currentView) {
        clear();
        this.currentView = null;

        if (!(currentView instanceof AbstractNotificationView)) {
            return;
        }
        this.currentView = (AbstractNotificationView) currentView;
        this.currentView.refreshView();
    }

    /**
     * Increment the counter.
     * 
     * @param view
     *            the view
     * @param newEventContainer
     *            the event container
     */
    public void incrementUnreadNotification(final AbstractNotificationView view,
            final EventContainer<?> newEventContainer) {
        if (!view.equals(currentView) || newEventContainer.getUnreadNotificationMessageKey() == null) {
            return;
        }
        NotificationUnreadValue notificationUnreadValue = unreadNotifications.get(newEventContainer.getClass());
        if (notificationUnreadValue == null) {
            notificationUnreadValue = new NotificationUnreadValue(0,
                    newEventContainer.getUnreadNotificationMessageKey());
            unreadNotifications.put(newEventContainer.getClass(), notificationUnreadValue);
        }

        notificationUnreadValue.incrementUnreadNotifications();
        unreadNotificationCounter++;
        refreshCaption();
    }

    public void incrementUnreadNotification(final EntityModifiedEventPayloadIdentifier eventPayloadIdentifier,
            final Collection<Long> eventEntityIds) {
        remotelyOriginatedEventsStore.merge(eventPayloadIdentifier, eventEntityIds,
                (oldEntityIds, newEntityIds) -> Stream.concat(oldEntityIds.stream(), newEntityIds.stream())
                        .collect(Collectors.toList()));

        unreadNotificationCounter += eventEntityIds.size();
        refreshCaption();
    }

    private static class NotificationUnreadValue {
        private Integer unreadNotifications;
        private final String unreadNotificationMessageKey;

        /**
         * @param unreadNotifications
         * @param unreadNotificationMessageKey
         */
        public NotificationUnreadValue(final Integer unreadNotifications, final String unreadNotificationMessageKey) {
            this.unreadNotifications = unreadNotifications;
            this.unreadNotificationMessageKey = unreadNotificationMessageKey;
        }

        /**
         * Increment the unread notifications.
         * 
         */
        public void incrementUnreadNotifications() {
            unreadNotifications++;
        }

        public String getUnreadNotificationMessageKey() {
            return unreadNotificationMessageKey;
        }

        public Integer getUnreadNotifications() {
            return unreadNotifications;
        }

    }
}
