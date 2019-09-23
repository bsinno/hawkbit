/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.UserDetailsFormatter;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyKeyValueDetails;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyNamedEntity;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.common.table.BaseUIEntityEvent;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.Binder;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Abstract Layout to show the entity details.
 *
 * @param <T>
 */
public abstract class AbstractGridDetailsLayout<T extends ProxyNamedEntity> extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    protected final VaadinMessageSource i18n;
    protected final SpPermissionChecker permChecker;
    protected final transient UIEventBus eventBus;

    protected final Binder<T> binder;

    protected final TabSheet detailsTab;
    protected final KeyValueDetailsComponent entityDetails;
    protected final TextArea entityDescription;
    protected final KeyValueDetailsComponent logDetails;

    private final transient Collection<Entry<String, Component>> detailsComponents;

    public AbstractGridDetailsLayout(final VaadinMessageSource i18n, final SpPermissionChecker permChecker,
            final UIEventBus eventBus) {
        this.i18n = i18n;
        this.permChecker = permChecker;
        this.eventBus = eventBus;

        this.binder = new Binder<>();

        this.detailsTab = buildDetailsTab();
        this.entityDetails = buildEntityDetails();
        this.entityDescription = buildEntityDescription();
        this.logDetails = buildLogDetails();

        this.detailsComponents = new ArrayList<>();

        init();
        if (doSubscribeToEventBus()) {
            eventBus.subscribe(this);
        }
    }

    /**
     * Subscribes the view to the eventBus. Method has to be overriden (return
     * false) if the view does not contain any listener to avoid Vaadin blowing
     * up our logs with warnings.
     */
    protected boolean doSubscribeToEventBus() {
        return true;
    }

    private TabSheet buildDetailsTab() {
        final TabSheet tab = SPUIComponentProvider.getDetailsTabSheet();

        tab.setWidth(98, Unit.PERCENTAGE);
        tab.setHeight(90, Unit.PERCENTAGE);
        tab.addStyleName(SPUIStyleDefinitions.DETAILS_LAYOUT_STYLE);
        tab.setId(getTabSheetId());

        return tab;
    }

    protected abstract String getTabSheetId();

    private KeyValueDetailsComponent buildEntityDetails() {
        final KeyValueDetailsComponent details = new KeyValueDetailsComponent();

        binder.forField(details).bind(this::getEntityDetails, null);

        return details;
    }

    protected abstract List<ProxyKeyValueDetails> getEntityDetails(final T entity);

    private TextArea buildEntityDescription() {
        final TextArea description = new TextArea();

        description.setId(getDetailsDescriptionId());
        description.setReadOnly(true);
        description.setWordWrap(true);
        description.setSizeFull();
        description.setStyleName(ValoTheme.TEXTAREA_BORDERLESS);
        description.addStyleName(ValoTheme.TEXTAREA_SMALL);
        description.addStyleName("details-description");

        binder.forField(description).bind(ProxyNamedEntity::getDescription, null);

        return description;
    }

    protected abstract String getDetailsDescriptionId();

    private KeyValueDetailsComponent buildLogDetails() {
        final KeyValueDetailsComponent logs = new KeyValueDetailsComponent();
        final String idPrefix = getLogLabelIdPrefix();

        binder.forField(logs)
                .bind(entity -> Arrays.asList(new ProxyKeyValueDetails(idPrefix + UIComponentIdProvider.CREATEDAT_ID,
                        i18n.getMessage("label.created.at"), SPDateTimeUtil.getFormattedDate(entity.getCreatedAt())),
                        new ProxyKeyValueDetails(idPrefix + UIComponentIdProvider.CREATEDBY_ID,
                                i18n.getMessage("label.created.by"),
                                entity.getCreatedBy() != null
                                        ? UserDetailsFormatter.loadAndFormatUsername(entity.getCreatedBy())
                                        : ""),
                        new ProxyKeyValueDetails(idPrefix + UIComponentIdProvider.MODIFIEDAT_ID,
                                i18n.getMessage("label.modified.date"),
                                SPDateTimeUtil.getFormattedDate(entity.getLastModifiedAt())),
                        new ProxyKeyValueDetails(idPrefix + UIComponentIdProvider.MODIFIEDBY_ID,
                                i18n.getMessage("label.modified.by"),
                                entity.getCreatedBy() != null
                                        ? UserDetailsFormatter.loadAndFormatUsername(entity.getLastModifiedBy())
                                        : "")),
                        null);

        return logs;
    }

    protected abstract String getLogLabelIdPrefix();

    private void init() {
        setSpacing(false);
        setMargin(false);
        setSizeFull();
        setHeightUndefined();
        addStyleName(SPUIStyleDefinitions.WIDGET_STYLE);
    }

    protected void buildDetails() {
        final DetailsHeader header = getDetailsHeader();
        addComponent(header);
        setComponentAlignment(header, Alignment.TOP_CENTER);

        detailsComponents.forEach(detailsComponentEntry -> {
            final String detailsComponentCaption = detailsComponentEntry.getKey();
            final Component detailsComponent = detailsComponentEntry.getValue();

            detailsTab.addTab(buildTabWrapperDetailsLayout(detailsComponent), detailsComponentCaption);
        });

        addComponent(detailsTab);
        setComponentAlignment(detailsTab, Alignment.TOP_CENTER);
    }

    protected abstract DetailsHeader getDetailsHeader();

    private VerticalLayout buildTabWrapperDetailsLayout(final Component detailsComponent) {
        final VerticalLayout tabWrapperDetailsLayout = new VerticalLayout();
        tabWrapperDetailsLayout.setSpacing(false);
        tabWrapperDetailsLayout.setMargin(false);
        tabWrapperDetailsLayout.setStyleName("details-layout");

        tabWrapperDetailsLayout.addComponent(detailsComponent);

        return tabWrapperDetailsLayout;
    }

    /**
     * Default implementation to handle an entity event.
     * 
     * @param baseEntityEvent
     *            the event
     */
    protected void onBaseEntityEvent(final BaseUIEntityEvent<T> baseEntityEvent) {
        final BaseEntityEventType eventType = baseEntityEvent.getEventType();
        if (BaseEntityEventType.SELECTED_ENTITY == eventType || BaseEntityEventType.UPDATED_ENTITY == eventType
                || BaseEntityEventType.REMOVE_ENTITY == eventType) {
            UI.getCurrent().access(() -> populateDetails(baseEntityEvent.getEntity()));
        } else if (BaseEntityEventType.MINIMIZED == eventType) {
            UI.getCurrent().access(() -> setVisible(true));
        } else if (BaseEntityEventType.MAXIMIZED == eventType) {
            UI.getCurrent().access(() -> setVisible(false));
        }
    }

    protected void populateDetails(final T entity) {
        if (entity == null) {
            getDetailsHeader().disableEditComponents();
        } else {
            getDetailsHeader().enableEditComponents();
        }

        binder.setBean(entity);
        // targetMetadataLayout.populateMetadata(entity);
    }

    protected void addDetailsComponents(final Collection<Entry<String, Component>> detailsComponents) {
        this.detailsComponents.addAll(detailsComponents);
    }
}
