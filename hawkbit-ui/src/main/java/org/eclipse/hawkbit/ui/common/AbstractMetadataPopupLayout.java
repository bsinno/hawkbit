/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.hawkbit.repository.model.MetaData;
import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.CommonDialogWindow.SaveDialogCloseListener;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextAreaBuilder;
import org.eclipse.hawkbit.ui.common.builder.TextFieldBuilder;
import org.eclipse.hawkbit.ui.common.builder.WindowBuilder;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleNoBorder;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.event.selection.SelectionEvent;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * 
 * Abstract pop up layout
 *
 * @param <E>
 *            E id the entity for which metadata is displayed
 * @param <M>
 *            M is the metadata
 * 
 */
public abstract class AbstractMetadataPopupLayout<E extends NamedEntity, M extends MetaData> extends CustomComponent {
    private static final long serialVersionUID = 1L;

    private static final String DELETE_BUTTON = "DELETE_BUTTON";
    private static final int INPUT_DEBOUNCE_TIMEOUT = 250;

    protected static final String VALUE = "value";
    protected static final String KEY = "key";
    protected static final int MAX_METADATA_QUERY = 500;

    protected VaadinMessageSource i18n;
    private final UINotification uiNotification;
    protected transient EventBus.UIEventBus eventBus;

    private TextField keyTextField;
    private TextArea valueTextArea;
    private Button addIcon;
    private List<ProxyMetaData> metaDataList;
    private MetaDataGrid metaDataGrid;
    private Label headerCaption;
    private CommonDialogWindow metadataWindow;

    private E selectedEntity;

    private HorizontalLayout mainLayout;
    protected SpPermissionChecker permChecker;

    protected AbstractMetadataPopupLayout(final VaadinMessageSource i18n, final UINotification uiNotification,
            final UIEventBus eventBus, final SpPermissionChecker permChecker) {
        this.i18n = i18n;
        this.uiNotification = uiNotification;
        this.eventBus = eventBus;
        this.permChecker = permChecker;

        createComponents();
        buildLayout();
    }

    /**
     * Save the metadata and never close the window after saving.
     */
    private final class SaveOnDialogCloseListener implements SaveDialogCloseListener {
        @Override
        public void saveOrUpdate() {
            onSave();
        }

        @Override
        public boolean canWindowClose() {
            return false;
        }

        @Override
        public boolean canWindowSaveOrUpdate() {
            return true;
        }

    }

    /**
     * Returns metadata popup.
     * 
     * @param entity
     *            entity for which metadata data is displayed
     * @param metaDatakey
     *            metadata key to be selected
     * @return {@link CommonDialogWindow}
     */
    public CommonDialogWindow getWindow(final E entity, final String metaDatakey) {
        selectedEntity = entity;

        metadataWindow = new WindowBuilder(SPUIDefinitions.CREATE_UPDATE_WINDOW).caption(getMetadataCaption())
                .content(this).cancelButtonClickListener(event -> onCancel())
                .id(UIComponentIdProvider.METADATA_POPUP_ID).i18n(i18n)
                .saveDialogCloseListener(new SaveOnDialogCloseListener()).buildCommonDialogWindow();

        metadataWindow.setHeight(550, Unit.PIXELS);
        metadataWindow.setWidth(800, Unit.PIXELS);
        metadataWindow.getMainLayout().setSizeFull();
        metadataWindow.getButtonsLayout().setHeight("45px");
        setUpDetails(entity.getId(), metaDatakey);
        return metadataWindow;
    }

    private void setUpDetails(final Long swId, final String metaDatakey) {
        resetDetails();
        if (swId != null) {
            metaDataList.clear();
            populateGrid();
            metaDataGrid.deselectAll();
            if (!metaDataList.isEmpty()) {
                if (metaDatakey == null) {
                    metaDataGrid.select(metaDataList.get(0));
                } else {
                    metaDataGrid.select(metaDataList.stream().filter(metaData -> metaData.getKey().equals(metaDatakey))
                            .findAny().orElse(null));
                }
            } else if (hasCreatePermission()) {
                enableEditing();
                addIcon.setEnabled(false);
            }
        }
    }

    private void resetDetails() {
        clearFields();
        disableEditing();
        metadataWindow.setSaveButtonEnabled(false);
        addIcon.setEnabled(true);
    }

    protected void disableEditing() {
        keyTextField.setEnabled(false);
        valueTextArea.setEnabled(false);
    }

    public E getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(final E selectedEntity) {
        this.selectedEntity = selectedEntity;
    }

    protected abstract boolean checkForDuplicate(E entity, String value);

    protected abstract M createMetadata(E entity, String key, String value);

    protected abstract M updateMetadata(E entity, String key, String value);

    protected abstract List<M> getMetadataList();

    protected abstract void deleteMetadata(E entity, String key);

    protected abstract boolean hasCreatePermission();

    protected abstract boolean hasUpdatePermission();

    protected void createComponents() {
        keyTextField = createKeyTextField();
        valueTextArea = createValueTextField();
        metaDataGrid = createMetaDataGrid();
        addIcon = createAddIcon();
        headerCaption = createHeaderCaption();
    }

    private void buildLayout() {
        final HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.addStyleName(SPUIStyleDefinitions.WIDGET_TITLE);
        headerLayout.setSpacing(false);
        headerLayout.setMargin(false);
        headerLayout.setSizeFull();
        headerLayout.addComponent(headerCaption);
        if (hasCreatePermission()) {
            headerLayout.addComponents(addIcon);
            headerLayout.setComponentAlignment(addIcon, Alignment.MIDDLE_RIGHT);
        }
        headerLayout.setExpandRatio(headerCaption, 1.0F);

        final HorizontalLayout headerWrapperLayout = new HorizontalLayout();
        headerWrapperLayout.addStyleName("bordered-layout" + " " + "no-border-bottom" + " " + "metadata-table-margin");
        headerWrapperLayout.addComponent(headerLayout);
        headerWrapperLayout.setWidth("100%");
        headerLayout.setHeight("30px");

        final VerticalLayout tableLayout = new VerticalLayout();
        tableLayout.setSizeFull();
        tableLayout.setHeight("100%");
        tableLayout.addComponent(headerWrapperLayout);
        tableLayout.addComponent(metaDataGrid);
        tableLayout.addStyleName("table-layout");
        tableLayout.setExpandRatio(metaDataGrid, 1.0F);

        final VerticalLayout metadataFieldsLayout = createMetadataFieldsLayout();

        mainLayout = new HorizontalLayout();
        mainLayout.addComponent(tableLayout);
        mainLayout.addComponent(metadataFieldsLayout);
        mainLayout.setExpandRatio(tableLayout, 0.5F);
        mainLayout.setExpandRatio(metadataFieldsLayout, 0.5F);
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        setCompositionRoot(mainLayout);
        setSizeFull();
    }

    protected VerticalLayout createMetadataFieldsLayout() {
        final VerticalLayout metadataFieldsLayout = new VerticalLayout();
        metadataFieldsLayout.setSizeFull();
        metadataFieldsLayout.setHeight("100%");
        metadataFieldsLayout.addComponent(keyTextField);
        metadataFieldsLayout.addComponent(valueTextArea);
        metadataFieldsLayout.setSpacing(true);
        metadataFieldsLayout.setExpandRatio(valueTextArea, 1F);
        return metadataFieldsLayout;
    }

    private TextField createKeyTextField() {
        final TextField keyField = new TextFieldBuilder(MetaData.KEY_MAX_SIZE).caption(i18n.getMessage("textfield.key"))
                .id(UIComponentIdProvider.METADATA_KEY_FIELD_ID).buildTextComponent();// .required(true,
                                                                                      // i18n)
        keyField.addValueChangeListener(this::onKeyChange);
        keyField.setValueChangeMode(ValueChangeMode.LAZY);
        keyField.setValueChangeTimeout(INPUT_DEBOUNCE_TIMEOUT);
        keyField.setWidth("100%");
        return keyField;
    }

    private TextArea createValueTextField() {
        valueTextArea = new TextAreaBuilder(MetaData.VALUE_MAX_SIZE).caption(i18n.getMessage("textfield.value"))
                .id(UIComponentIdProvider.METADATA_VALUE_ID).buildTextComponent();// .required(true,
                                                                                  // i18n)
        valueTextArea.setSizeFull();
        valueTextArea.setHeight(100, Unit.PERCENTAGE);
        valueTextArea.addValueChangeListener(this::onValueChange);
        valueTextArea.setValueChangeMode(ValueChangeMode.LAZY);
        valueTextArea.setValueChangeTimeout(INPUT_DEBOUNCE_TIMEOUT);
        return valueTextArea;
    }

    protected MetaDataGrid createMetaDataGrid() {
        metaDataList = new ArrayList<>();

        final MetaDataGrid grid = new MetaDataGrid(i18n, permChecker, uiNotification, this::handleOkDeleteMetadata);
        grid.setItems(metaDataList);
        grid.addSelectionListener(this::onRowClick);

        return grid;
    }

    private void handleOkDeleteMetadata(final Collection<ProxyMetaData> metaDataItemsToDelete) {
        // MetaDataGrid supports deletion of only one item due to Single
        // selection mode
        final ProxyMetaData metaDataItemToDelete = metaDataItemsToDelete.iterator().next();

        deleteMetadata(getSelectedEntity(), metaDataItemToDelete.getKey());
        metaDataList.remove(metaDataItemToDelete);
        // TODO: should we call refreshAll here?

        // adapt selection
        final ProxyMetaData selectedMetaDataItem = metaDataGrid.asSingleSelect().getFirstSelectedItem().orElse(null);
        metaDataGrid.clearSortOrder();
        if (!metaDataList.isEmpty()) {
            if (selectedMetaDataItem != null) {
                if (selectedMetaDataItem.equals(metaDataItemToDelete)) {
                    metaDataGrid.select(metaDataList.get(0));
                } else {
                    metaDataGrid.select(selectedMetaDataItem);
                }
            }
        } else {
            resetFields();
        }
    }

    protected void onRowClick(final SelectionEvent<ProxyMetaData> event) {
        final ProxyMetaData selectedMetaDataItem = event.getFirstSelectedItem().orElse(null);
        if (selectedMetaDataItem != null) {
            populateKeyValue(selectedMetaDataItem);
            addIcon.setEnabled(true);
        } else {
            clearFields();
            if (hasCreatePermission()) {
                enableEditing();
                addIcon.setEnabled(false);
            } else {
                keyTextField.setEnabled(false);
                valueTextArea.setEnabled(false);
            }
        }
        metadataWindow.setSaveButtonEnabled(false);
    }

    protected void populateKeyValue(final ProxyMetaData selectedMetaDataItem) {
        keyTextField.setValue(selectedMetaDataItem.getKey());
        valueTextArea.setValue(selectedMetaDataItem.getValue());
        keyTextField.setEnabled(false);
        if (hasUpdatePermission()) {
            valueTextArea.setEnabled(true);
        }
    }

    protected void clearFields() {
        valueTextArea.clear();
        keyTextField.clear();
    }

    protected void enableEditing() {
        keyTextField.setEnabled(true);
        valueTextArea.setEnabled(true);
    }

    private void resetFields() {
        clearFields();
        metaDataGrid.select(null);
        if (hasCreatePermission()) {
            enableEditing();
            addIcon.setEnabled(false);
        }
    }

    private Button createAddIcon() {
        addIcon = SPUIComponentProvider.getButton(UIComponentIdProvider.METADTA_ADD_ICON_ID,
                i18n.getMessage("button.save"), null, null, false, VaadinIcons.PLUS, SPUIButtonStyleNoBorder.class);
        addIcon.addClickListener(event -> onAdd());
        return addIcon;
    }

    private Label createHeaderCaption() {
        return new LabelBuilder().name(i18n.getMessage("caption.metadata")).buildCaptionLabel();
    }

    private void populateGrid() {
        final List<M> metadataList = getMetadataList();
        for (final M metaData : metadataList) {
            addItemToGrid(metaData);
        }
    }

    protected ProxyMetaData addItemToGrid(final M metaData) {
        final ProxyMetaData newMetaDataItem = new ProxyMetaData();

        newMetaDataItem.setEntityId(metaData.getEntityId());
        newMetaDataItem.setKey(metaData.getKey());
        newMetaDataItem.setValue(metaData.getValue());
        metaDataList.add(newMetaDataItem);
        // TODO: should we call refreshAll here?

        return newMetaDataItem;
    }

    protected ProxyMetaData updateItemInGrid(final String key) {
        final ProxyMetaData metaDataItem = metaDataList.stream().filter(metaData -> metaData.getKey().equals(key))
                .findAny().orElse(null);

        if (metaDataItem != null) {
            metaDataItem.setValue(valueTextArea.getValue());
        }
        // TODO: should we call refreshItem here?

        return metaDataItem;
    }

    private void onAdd() {
        metaDataGrid.deselectAll();
        clearFields();
        enableEditing();
        addIcon.setEnabled(true);
    }

    protected void onSave() {
        final String key = keyTextField.getValue();
        final String value = valueTextArea.getValue();
        if (mandatoryCheck()) {
            final E entity = selectedEntity;
            if (metaDataGrid.getSelectedItems().isEmpty()) {
                if (!duplicateCheck(entity)) {
                    final M metadata = createMetadata(entity, key, value);
                    uiNotification.displaySuccess(i18n.getMessage("message.metadata.saved", metadata.getKey()));
                    final ProxyMetaData newMetadataItem = addItemToGrid(metadata);
                    metaDataGrid.scrollToEnd();
                    metaDataGrid.select(newMetadataItem);
                    addIcon.setEnabled(true);
                    metadataWindow.setSaveButtonEnabled(false);
                    if (!hasUpdatePermission()) {
                        valueTextArea.setEnabled(false);
                    }
                }
            } else {
                final M metadata = updateMetadata(entity, key, value);
                uiNotification.displaySuccess(i18n.getMessage("message.metadata.updated", metadata.getKey()));
                final ProxyMetaData updatedMetadataItem = updateItemInGrid(metadata.getKey());
                metaDataGrid.select(updatedMetadataItem);
                addIcon.setEnabled(true);
                metadataWindow.setSaveButtonEnabled(false);
            }
        }
    }

    private boolean mandatoryCheck() {
        if (keyTextField.getValue().isEmpty()) {
            uiNotification.displayValidationError(i18n.getMessage("message.key.missing"));
            return false;
        }
        if (valueTextArea.getValue().isEmpty()) {
            uiNotification.displayValidationError(i18n.getMessage("message.value.missing"));
            return false;
        }
        return true;
    }

    private boolean duplicateCheck(final E entity) {
        if (!checkForDuplicate(entity, keyTextField.getValue())) {
            return false;
        }

        uiNotification
                .displayValidationError(i18n.getMessage("message.metadata.duplicate.check", keyTextField.getValue()));
        return true;
    }

    private String getMetadataCaption() {
        final StringBuilder caption = new StringBuilder();
        caption.append(HawkbitCommonUtil.DIV_DESCRIPTION_START + i18n.getMessage("caption.metadata.popup") + " "
                + HawkbitCommonUtil.getBoldHTMLText(getElementTitle()));
        caption.append(HawkbitCommonUtil.DIV_DESCRIPTION_END);
        return caption.toString();
    }

    protected String getElementTitle() {
        return getSelectedEntity().getName();
    }

    private void onCancel() {
        metadataWindow.close();
        UI.getCurrent().removeWindow(metadataWindow);
    }

    private void onKeyChange(final ValueChangeEvent<String> event) {
        if (hasCreatePermission() || hasUpdatePermission()) {
            metadataWindow.setSaveButtonEnabled(!valueTextArea.getValue().isEmpty() && !event.getValue().isEmpty());
        }
    }

    private void onValueChange(final ValueChangeEvent<String> event) {
        if (hasCreatePermission() || hasUpdatePermission()) {
            metadataWindow.setSaveButtonEnabled(!keyTextField.getValue().isEmpty() && !event.getValue().isEmpty());
        }
    }

    protected TextArea getValueTextArea() {
        return valueTextArea;
    }

    protected TextField getKeyTextField() {
        return keyTextField;
    }

    protected CommonDialogWindow getMetadataWindow() {
        return metadataWindow;
    }

}
