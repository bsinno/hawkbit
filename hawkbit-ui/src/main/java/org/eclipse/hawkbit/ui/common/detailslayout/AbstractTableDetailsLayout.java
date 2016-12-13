/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.common.table.BaseUIEntityEvent;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleSmallNoBorder;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.I18N;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Abstract Layout to show the entity details.
 *
 */
public abstract class AbstractTableDetailsLayout<T extends NamedEntity> extends VerticalLayout {

    private static final long serialVersionUID = 4862529368471627190L;

    private final I18N i18n;

    private final transient EventBus.UIEventBus eventBus;

    private final SpPermissionChecker permissionChecker;

    private T selectedBaseEntity;

    private Label caption;

    private Button editButton;

    private Button manageMetadataBtn;

    protected TabSheet detailsTab;

    private final VerticalLayout detailsLayout;

    private final VerticalLayout descriptionLayout;

    private final VerticalLayout logLayout;

    private final VerticalLayout attributesLayout;

    protected final ManagementUIState managementUIState;

    protected AbstractTableDetailsLayout(final I18N i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final ManagementUIState managementUIState) {
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.permissionChecker = permissionChecker;
        this.managementUIState = managementUIState;
        detailsLayout = getTabLayout();
        descriptionLayout = getTabLayout();
        logLayout = getTabLayout();
        attributesLayout = getTabLayout();
        createComponents();
        buildLayout();
        eventBus.subscribe(this);
    }

    protected SpPermissionChecker getPermissionChecker() {
        return permissionChecker;
    }

    protected EventBus.UIEventBus getEventBus() {
        return eventBus;
    }

    protected I18N getI18n() {
        return i18n;
    }

    protected T getSelectedBaseEntity() {
        return selectedBaseEntity;
    }

    public void setSelectedBaseEntity(final T selectedBaseEntity) {
        this.selectedBaseEntity = selectedBaseEntity;
    }

    /**
     * Default implementation to handle an entity event.
     * 
     * @param baseEntityEvent
     *            the event
     */
    protected void onBaseEntityEvent(final BaseUIEntityEvent<T> baseEntityEvent) {
        final BaseEntityEventType eventType = baseEntityEvent.getEventType();
        if (BaseEntityEventType.SELECTED_ENTITY == eventType || BaseEntityEventType.UPDATED_ENTITY == eventType) {
            UI.getCurrent().access(() -> populateData(baseEntityEvent.getEntity()));
        } else if (BaseEntityEventType.MINIMIZED == eventType) {
            UI.getCurrent().access(() -> setVisible(true));
        } else if (BaseEntityEventType.MAXIMIZED == eventType) {
            UI.getCurrent().access(() -> setVisible(false));
        }
    }

    private void createComponents() {
        caption = createHeaderCaption();
        caption.setImmediate(true);
        caption.setContentMode(ContentMode.HTML);
        caption.setId(getDetailsHeaderCaptionId());

        editButton = SPUIComponentProvider.getButton("", "", "", null, false, FontAwesome.PENCIL_SQUARE_O,
                SPUIButtonStyleSmallNoBorder.class);
        editButton.setId(getEditButtonId());
        editButton.addClickListener(this::onEdit);
        editButton.setEnabled(false);

        manageMetadataBtn = SPUIComponentProvider.getButton("", "", "", null, false, FontAwesome.LIST_ALT,
                SPUIButtonStyleSmallNoBorder.class);
        manageMetadataBtn.setId(getEditButtonId());
        manageMetadataBtn.setDescription(i18n.get("tooltip.metadata.icon"));
        manageMetadataBtn.addClickListener(this::showMetadata);
        manageMetadataBtn.setEnabled(false);

        detailsTab = SPUIComponentProvider.getDetailsTabSheet();
        detailsTab.setImmediate(true);
        detailsTab.setWidth(98, Unit.PERCENTAGE);
        detailsTab.setHeight(90, Unit.PERCENTAGE);
        detailsTab.addStyleName(SPUIStyleDefinitions.DETAILS_LAYOUT_STYLE);
        detailsTab.setId(getTabSheetId());
    }

    private void buildLayout() {
        final HorizontalLayout nameEditLayout = new HorizontalLayout();
        nameEditLayout.setWidth(100.0F, Unit.PERCENTAGE);
        nameEditLayout.addComponent(caption);
        nameEditLayout.setComponentAlignment(caption, Alignment.TOP_LEFT);
        if (hasEditPermission()) {
            nameEditLayout.addComponent(editButton);
            nameEditLayout.setComponentAlignment(editButton, Alignment.TOP_RIGHT);
            if (isMetadataIconToBeDisplayed()) {
                nameEditLayout.addComponent(manageMetadataBtn);
                nameEditLayout.setComponentAlignment(manageMetadataBtn, Alignment.TOP_RIGHT);
            }
        }
        nameEditLayout.setExpandRatio(caption, 1.0F);
        nameEditLayout.addStyleName(SPUIStyleDefinitions.WIDGET_TITLE);

        addComponent(nameEditLayout);
        setComponentAlignment(nameEditLayout, Alignment.TOP_CENTER);
        addComponent(detailsTab);
        setComponentAlignment(nameEditLayout, Alignment.TOP_CENTER);

        setSizeFull();
        setHeightUndefined();
        addStyleName(SPUIStyleDefinitions.WIDGET_STYLE);
    }

    private Label createHeaderCaption() {
        return new LabelBuilder().name(getDefaultCaption()).buildCaptionLabel();
    }

    protected VerticalLayout getTabLayout() {
        final VerticalLayout tabLayout = SPUIComponentProvider.getDetailTabLayout();
        tabLayout.addStyleName("details-layout");
        return tabLayout;
    }

    protected void setName(final String headerCaption, final String value) {
        caption.setValue(HawkbitCommonUtil.getSoftwareModuleName(headerCaption, value));
    }

    protected void restoreState() {
        if (onLoadIsTableRowSelected()) {
            populateData(null);
            editButton.setEnabled(true);
        }
        if (onLoadIsTableMaximized()) {
            setVisible(false);
        }
    }

    /**
     * If no data in table (i,e no row selected),then disable the edit button.
     * If row is selected ,enable edit button.
     */
    private void populateData(final T selectedBaseEntity) {
        this.selectedBaseEntity = selectedBaseEntity;
        editButton.setEnabled(selectedBaseEntity != null);
        manageMetadataBtn.setEnabled(selectedBaseEntity != null);
        if (selectedBaseEntity == null) {
            setName(getDefaultCaption(), StringUtils.EMPTY);
        } else {
            setName(getDefaultCaption(), getName());
        }
        populateLog();
        populateDescription();
        populateDetailsWidget();
    }

    protected void populateLog() {
        logLayout.removeAllComponents();

        logLayout.addComponent(SPUIComponentProvider.createNameValueLabel(i18n.get("label.created.at"),
                SPDateTimeUtil.formatCreatedAt(selectedBaseEntity)));

        logLayout.addComponent(SPUIComponentProvider.createCreatedByLabel(i18n, selectedBaseEntity));

        if (selectedBaseEntity == null || selectedBaseEntity.getLastModifiedAt() == null) {
            return;
        }

        logLayout.addComponent(SPUIComponentProvider.createNameValueLabel(i18n.get("label.modified.date"),
                SPDateTimeUtil.formatLastModifiedAt(selectedBaseEntity)));

        logLayout.addComponent(SPUIComponentProvider.createLastModifiedByLabel(i18n, selectedBaseEntity));
    }

    protected void updateDescriptionLayout(final String descriptionLabel, final String description) {
        descriptionLayout.removeAllComponents();
        final Label descLabel = SPUIComponentProvider.createNameValueLabel(descriptionLabel,
                HawkbitCommonUtil.trimAndNullIfEmpty(description) == null ? "" : description);
        /**
         * By default text will be truncated based on layout width .so removing
         * it as we need full description.
         */
        descLabel.removeStyleName("label-style");
        descLabel.setId(UIComponentIdProvider.DETAILS_DESCRIPTION_LABEL_ID);
        descriptionLayout.addComponent(descLabel);
    }

    /*
     * display Attributes details in Target details.
     */

    protected void updateAttributesLayout(final Target target) {
        if (null != target && null != target.getTargetInfo()
                && null != target.getTargetInfo().getControllerAttributes()) {
            attributesLayout.removeAllComponents();
            for (final Map.Entry<String, String> entry : target.getTargetInfo().getControllerAttributes().entrySet()) {
                final Label conAttributeLabel = SPUIComponentProvider.createNameValueLabel(
                        entry.getKey().concat("  :  "),
                        HawkbitCommonUtil.trimAndNullIfEmpty(entry.getValue()) == null ? "" : entry.getValue());
                conAttributeLabel.setDescription(entry.getKey().concat("  :  ") + entry.getValue());
                conAttributeLabel.addStyleName("label-style");
                attributesLayout.addComponent(conAttributeLabel);

            }
        }
    }

    protected VerticalLayout createLogLayout() {
        return logLayout;
    }

    protected VerticalLayout createAttributesLayout() {
        return attributesLayout;
    }

    protected VerticalLayout createDetailsLayout() {
        return detailsLayout;
    }

    protected VerticalLayout createDescriptionLayout() {
        return descriptionLayout;
    }

    /**
     * Default caption of header to be displayed when no data row selected in
     * table.
     * 
     * @return String
     */
    protected abstract String getDefaultCaption();

    /**
     * Add tabs.
     * 
     * @param detailsTab
     */
    protected abstract void addTabs(final TabSheet detailsTab);

    /**
     * Click listener for edit button.
     * 
     * @param event
     */
    protected abstract void onEdit(Button.ClickEvent event);

    protected abstract String getEditButtonId();

    protected abstract Boolean onLoadIsTableRowSelected();

    protected abstract Boolean onLoadIsTableMaximized();

    protected abstract String getTabSheetId();

    protected abstract Boolean hasEditPermission();

    public VerticalLayout getDetailsLayout() {
        return detailsLayout;
    }

    private void populateDescription() {
        if (selectedBaseEntity != null) {
            updateDescriptionLayout(i18n.get("label.description"), selectedBaseEntity.getDescription());
        } else {
            updateDescriptionLayout(i18n.get("label.description"), null);
        }
    }

    protected abstract void populateDetailsWidget();

    protected abstract void populateMetadataDetails();

    protected Long getSelectedBaseEntityId() {
        return selectedBaseEntity == null ? null : selectedBaseEntity.getId();
    }

    protected abstract String getDetailsHeaderCaptionId();

    protected abstract String getName();

    protected abstract Boolean isMetadataIconToBeDisplayed();

    protected abstract void showMetadata(Button.ClickEvent event);

}
