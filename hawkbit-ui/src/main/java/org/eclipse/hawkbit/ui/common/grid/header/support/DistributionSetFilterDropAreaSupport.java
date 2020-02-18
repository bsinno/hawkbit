/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.grid.header.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.event.EntityDraggingEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityDraggingEventPayload.DraggingEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.FilterByDsEventPayload;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.management.targettable.TargetGridLayoutUiState;
import org.eclipse.hawkbit.ui.management.targettag.filter.TargetTagFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.SPUITargetDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.dnd.event.DropEvent;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Allows to drop a Distribution Set in order to filter for it
 *
 */
public class DistributionSetFilterDropAreaSupport implements HeaderSupport {

    private final VaadinMessageSource i18n;
    private final UIEventBus eventBus;
    private final UINotification notification;

    private final HorizontalLayout currentDsFilterInfo;
    private final HorizontalLayout dropAreaLayout;

    /**
     * Constructor
     * 
     * @param i18n
     *  i18n
     * @param eventBus
     *  for sending filter event and get informed about started dragging
     * @param notification
     *  to display notification
     */
    public DistributionSetFilterDropAreaSupport(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final UINotification notification) {
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.notification = notification;

        this.currentDsFilterInfo = buildCurrentDsFilterInfo();
        this.dropAreaLayout = buildDsDropArea();

        dropAreaLayout.addAttachListener(event -> eventBus.subscribe(this, EventTopics.ENTITY_DRAGGING));
        dropAreaLayout.addDetachListener(event -> eventBus.unsubscribe(this));
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    private void onEntityDraggingEvent(final EntityDraggingEventPayload eventPayload) {
        final String sourceGridId = eventPayload.getSourceGridId();
        final String style = "show-drop-hint";

        if (UIComponentIdProvider.DIST_TABLE_ID.equals(sourceGridId)
                && eventPayload.getDraggingEventType() == DraggingEventType.STARTED) {
            dropAreaLayout.addStyleName(style);
        } else {
            dropAreaLayout.removeStyleName(style);
        }
    }

    private static HorizontalLayout buildCurrentDsFilterInfo() {
        final HorizontalLayout dropArea = new HorizontalLayout();

        dropArea.setId(UIComponentIdProvider.TARGET_DROP_FILTER_ICON);
        dropArea.setStyleName("target-dist-filter-info");
        dropArea.setSizeUndefined();

        return dropArea;
    }

    private HorizontalLayout buildDsDropArea() {
        final HorizontalLayout hintDropFilterLayout = new HorizontalLayout();

        hintDropFilterLayout.addStyleName("filter-drop-hint-layout");
        hintDropFilterLayout.setWidth(100, Unit.PERCENTAGE);

        final DropTargetExtension<HorizontalLayout> dropExtension = new DropTargetExtension<>(hintDropFilterLayout);
        dropExtension.addDropListener(event -> {
            final List<ProxyDistributionSet> dSets = getDroppedDistributionSets(event);
            if (dSets.size() == 1) {
                ProxyDistributionSet droppedDs = dSets.get(0);
                eventBus.publish(EventTopics.FILTER_BY_DS_CHANGED, this, new FilterByDsEventPayload(droppedDs.getId()));
                addDsFilterDropAreaTextField(droppedDs.getNameVersion());
            } else {
                notification.displayValidationError(i18n.getMessage("message.action.did.not.work"));
            }
        });

        hintDropFilterLayout.addComponent(currentDsFilterInfo);
        hintDropFilterLayout.setComponentAlignment(currentDsFilterInfo, Alignment.TOP_CENTER);
        hintDropFilterLayout.setExpandRatio(currentDsFilterInfo, 1.0F);

        return hintDropFilterLayout;
    }

    private static List<ProxyDistributionSet> getDroppedDistributionSets(final DropEvent<?> dropEvent) {
        final List<ProxyDistributionSet> list = new ArrayList<>();
        dropEvent.getDragSourceExtension().ifPresent(dragSource -> {
            final Object dragData = dragSource.getDragData();
            if (dragData instanceof ProxyDistributionSet) {
                list.add((ProxyDistributionSet) dragData);
            }
            if (dragData instanceof List
                    && ((List<?>) dragData).stream().allMatch(element -> element instanceof ProxyDistributionSet)) {
                list.addAll(((List<?>) dragData).stream().map(element -> (ProxyDistributionSet) element)
                        .collect(Collectors.toList()));
            }
        });
        return list;
    }

    private void addDsFilterDropAreaTextField(final String nv) {
        final Button filterLabelClose = SPUIComponentProvider.getButton("drop.filter.close", "", "", "", true,
                VaadinIcons.CLOSE_CIRCLE, SPUIButtonStyleNoBorder.class);
        filterLabelClose.addClickListener(clickEvent -> restoreState());

        final Label filteredDistLabel = new Label();
        filteredDistLabel.setStyleName(ValoTheme.LABEL_COLORED + " " + ValoTheme.LABEL_SMALL);
        String name = nv;
        if (name.length() > SPUITargetDefinitions.DISTRIBUTION_NAME_MAX_LENGTH_ALLOWED) {
            name = new StringBuilder(name.substring(0, SPUITargetDefinitions.DISTRIBUTION_NAME_LENGTH_ON_FILTER))
                    .append("...").toString();
        }
        filteredDistLabel.setValue(name);
        filteredDistLabel.setSizeUndefined();

        currentDsFilterInfo.removeAllComponents();
        currentDsFilterInfo.setSizeFull();
        currentDsFilterInfo.addComponent(filteredDistLabel);
        currentDsFilterInfo.addComponent(filterLabelClose);
        currentDsFilterInfo.setExpandRatio(filteredDistLabel, 1.0F);
    }

    @Override
    public Component getHeaderComponent() {
        return dropAreaLayout;
    }

    @Override
    public void restoreState() {
        currentDsFilterInfo.removeAllComponents();
        currentDsFilterInfo.setSizeUndefined();
        eventBus.publish(EventTopics.FILTER_BY_DS_CHANGED, this, new FilterByDsEventPayload(null));
    }
}
