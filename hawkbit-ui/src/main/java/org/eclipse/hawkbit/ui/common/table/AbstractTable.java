/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.model.NamedEntity;
import org.eclipse.hawkbit.ui.artifacts.event.UploadArtifactUIEvent;
import org.eclipse.hawkbit.ui.common.ManagmentEntityState;
import org.eclipse.hawkbit.ui.common.UserDetailsFormatter;
import org.eclipse.hawkbit.ui.components.RefreshableContainer;
import org.eclipse.hawkbit.ui.utils.I18N;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUILabelDefinitions;
import org.eclipse.hawkbit.ui.utils.TableColumn;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Abstract table to handling entity
 *
 * @param <E>
 *            e is the entity class
 * @param <I>
 *            i is the id of the table
 */
public abstract class AbstractTable<E extends NamedEntity, I> extends Table implements RefreshableContainer {

    private static final float DEFAULT_COLUMN_NAME_MIN_SIZE = 0.8F;

    private static final long serialVersionUID = 4856562746502217630L;

    protected static final String ACTION_NOT_ALLOWED_MSG = "message.action.not.allowed";

    protected transient EventBus.UIEventBus eventBus;

    protected I18N i18n;

    protected UINotification notification;

    protected AbstractTable(final UIEventBus eventBus, final I18N i18n, final UINotification notification) {
        this.eventBus = eventBus;
        this.i18n = i18n;
        this.notification = notification;
        setStyleName("sp-table");
        setSizeFull();
        setImmediate(true);
        setHeight(100.0F, Unit.PERCENTAGE);
        addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        addStyleName(ValoTheme.TABLE_SMALL);
        setSortEnabled(false);
        setId(getTableId());
        addCustomGeneratedColumns();
        setDefault();
        addValueChangeListener(event -> onValueChange());
        setPageLength(SPUIDefinitions.PAGE_SIZE);

        eventBus.subscribe(this);
    }

    /**
     * Gets the selected item id or in multiselect mode a set of selected ids.
     * 
     * @param table
     *            the table to retrieve the selected ID(s)
     * @return the ID(s) which are selected in the table
     */
    public static <T> Set<T> getTableValue(final Table table) {
        @SuppressWarnings("unchecked")
        Set<T> values = (Set<T>) table.getValue();
        if (values == null) {
            values = Collections.emptySet();
        }
        return values.stream().filter(item -> item != null).collect(Collectors.toSet());
    }

    private void onValueChange() {
        eventBus.publish(this, UploadArtifactUIEvent.HIDE_DROP_HINTS);

        final Set<I> values = getTableValue(this);

        E entity = null;
        I lastId = null;
        if (!values.isEmpty()) {
            lastId = Iterables.getLast(values);
            entity = findEntityByTableValue(lastId);
        }
        setManagementEntitiyStateValues(values, lastId);
        publishEntityAfterValueChange(entity);
    }

    protected void setManagementEntitiyStateValues(final Set<I> values, final I lastId) {
        final ManagmentEntityState<I> managmentEntityState = getManagmentEntityState();
        if (managmentEntityState == null) {
            return;
        }
        managmentEntityState.setLastSelectedEntity(lastId);
        managmentEntityState.setSelectedEnitities(values);
    }

    private void setDefault() {
        setSelectable(true);
        setMultiSelect(true);
        setDragMode(TableDragMode.MULTIROW);
        setColumnCollapsingAllowed(false);
        setDropHandler(getTableDropHandler());
    }

    protected void addNewContainerDS() {
        final Container container = createContainer();
        addContainerProperties(container);
        setContainerDataSource(container);
        final int size = container.size();
        if (size == 0) {
            setData(SPUIDefinitions.NO_DATA);
        }
    }

    protected void selectRow() {
        if (!isMaximized()) {
            if (isFirstRowSelectedOnLoad()) {
                selectFirstRow();
            } else {
                setValue(getItemIdToSelect());
            }
        }
    }

    /**
     * Select all rows in the table.
     */
    protected void selectAll() {
        if (isMultiSelect()) {
            // only contains the ItemIds of the visible items in the table
            setValue(getItemIds());
        }
    }

    protected void setColumnProperties() {
        final List<TableColumn> columnList = getTableVisibleColumns();
        final List<Object> swColumnIds = new ArrayList<>();
        for (final TableColumn column : columnList) {
            setColumnHeader(column.getColumnPropertyId(), column.getColumnHeader());
            setColumnExpandRatio(column.getColumnPropertyId(), column.getExpandRatio());
            swColumnIds.add(column.getColumnPropertyId());
        }
        setVisibleColumns(swColumnIds.toArray());
    }

    private void selectFirstRow() {
        final Container container = getContainerDataSource();
        final int size = container.size();
        if (size > 0) {
            select(firstItemId());
        }
    }

    private void applyMaxTableSettings() {
        setColumnProperties();
        setValue(null);
        setSelectable(false);
        setMultiSelect(false);
        setDragMode(TableDragMode.NONE);
        setColumnCollapsingAllowed(true);
    }

    private void applyMinTableSettings() {
        setDefault();
        setColumnProperties();
        selectRow();
    }

    protected void refreshFilter() {
        addNewContainerDS();
        setColumnProperties();
        selectRow();
    }

    @SuppressWarnings("unchecked")
    protected void updateEntity(final E baseEntity, final Item item) {
        item.getItemProperty(SPUILabelDefinitions.VAR_NAME).setValue(baseEntity.getName());
        item.getItemProperty(SPUILabelDefinitions.VAR_ID).setValue(baseEntity.getId());
        item.getItemProperty(SPUILabelDefinitions.VAR_DESC).setValue(baseEntity.getDescription());
        item.getItemProperty(SPUILabelDefinitions.VAR_CREATED_BY)
                .setValue(UserDetailsFormatter.loadAndFormatCreatedBy(baseEntity));
        item.getItemProperty(SPUILabelDefinitions.VAR_LAST_MODIFIED_BY)
                .setValue(UserDetailsFormatter.loadAndFormatLastModifiedBy(baseEntity));
        item.getItemProperty(SPUILabelDefinitions.VAR_CREATED_DATE)
                .setValue(SPDateTimeUtil.getFormattedDate(baseEntity.getCreatedAt()));
        item.getItemProperty(SPUILabelDefinitions.VAR_LAST_MODIFIED_DATE)
                .setValue(SPDateTimeUtil.getFormattedDate(baseEntity.getLastModifiedAt()));

    }

    protected void onBaseEntityEvent(final BaseUIEntityEvent<E> event) {
        if (BaseEntityEventType.MINIMIZED == event.getEventType()) {
            UI.getCurrent().access(this::applyMinTableSettings);
        } else if (BaseEntityEventType.MAXIMIZED == event.getEventType()) {
            UI.getCurrent().access(this::applyMaxTableSettings);
        } else if (BaseEntityEventType.ADD_ENTITY == event.getEventType()
                || BaseEntityEventType.REMOVE_ENTITY == event.getEventType()) {
            UI.getCurrent().access(this::refreshContainer);
        }
    }

    /**
     * Return the entity which should be deleted by a transferable
     * 
     * @param transferable
     *            the table transferable
     * @return set of entities id which will deleted
     */
    @SuppressWarnings("unchecked")
    public Set<I> getDeletedEntityByTransferable(final TableTransferable transferable) {
        final Set<I> selectedEntities = (Set<I>) getTableValue(this);
        final Set<I> ids = new HashSet<>();
        final Object tranferableData = transferable.getData(SPUIDefinitions.ITEMID);
        if (tranferableData == null) {
            return ids;
        }

        if (!selectedEntities.contains(tranferableData)) {
            ids.add((I) tranferableData);
        } else {
            ids.addAll(selectedEntities);
        }
        return ids;
    }

    protected abstract E findEntityByTableValue(I lastSelectedId);

    protected abstract void publishEntityAfterValueChange(E selectedLastEntity);

    protected abstract ManagmentEntityState<I> getManagmentEntityState();

    /**
     * Get Id of the table.
     * 
     * @return Id.
     */
    protected abstract String getTableId();

    /**
     * Create container of the data to be displayed by the table.
     */
    protected abstract Container createContainer();

    /**
     * Add container properties to the container passed in the reference.
     * 
     * @param container
     *            reference of {@link Container}
     */
    protected abstract void addContainerProperties(Container container);

    /**
     * Add any generated columns if required.
     */
    protected void addCustomGeneratedColumns() {
        // can be overriden
    }

    /**
     * Check if first row should be selected by default on load.
     * 
     * @return true if it should be selected otherwise return false.
     */
    protected abstract boolean isFirstRowSelectedOnLoad();

    /**
     * Get Item Id should be displayed as selected.
     * 
     * @return reference of Item Id of the Row.
     */
    protected abstract Object getItemIdToSelect();

    /**
     * Check if the table is maximized or minimized.
     * 
     * @return true if maximized, otherwise false.
     */
    protected abstract boolean isMaximized();

    /**
     * Based on table state (max/min) columns to be shown are returned.
     * 
     * @return List<TableColumn> list of visible columns
     */
    protected List<TableColumn> getTableVisibleColumns() {
        final List<TableColumn> columnList = new ArrayList<>();
        if (!isMaximized()) {
            columnList.add(new TableColumn(SPUILabelDefinitions.VAR_NAME, i18n.get("header.name"),
                    getColumnNameMinimizedSize()));
            return columnList;
        }
        columnList.add(new TableColumn(SPUILabelDefinitions.VAR_NAME, i18n.get("header.name"), 0.2F));
        columnList.add(new TableColumn(SPUILabelDefinitions.VAR_CREATED_BY, i18n.get("header.createdBy"), 0.1F));
        columnList.add(new TableColumn(SPUILabelDefinitions.VAR_CREATED_DATE, i18n.get("header.createdDate"), 0.1F));
        columnList.add(new TableColumn(SPUILabelDefinitions.VAR_LAST_MODIFIED_BY, i18n.get("header.modifiedBy"), 0.1F));
        columnList.add(
                new TableColumn(SPUILabelDefinitions.VAR_LAST_MODIFIED_DATE, i18n.get("header.modifiedDate"), 0.1F));
        columnList.add(new TableColumn(SPUILabelDefinitions.VAR_DESC, i18n.get("header.description"), 0.2F));
        setItemDescriptionGenerator((source, itemId, propertyId) -> {

            if (SPUILabelDefinitions.VAR_CREATED_BY.equals(propertyId)) {
                return getItem(itemId).getItemProperty(SPUILabelDefinitions.VAR_CREATED_BY).getValue().toString();
            }
            if (SPUILabelDefinitions.VAR_LAST_MODIFIED_BY.equals(propertyId)) {
                return getItem(itemId).getItemProperty(SPUILabelDefinitions.VAR_LAST_MODIFIED_BY).getValue().toString();
            }
            return null;
        });

        return columnList;
    }

    protected float getColumnNameMinimizedSize() {
        return DEFAULT_COLUMN_NAME_MIN_SIZE;
    }

    private DropHandler getTableDropHandler() {
        return new DropHandler() {
            private static final long serialVersionUID = 1L;

            @Override
            public AcceptCriterion getAcceptCriterion() {
                return getDropAcceptCriterion();
            }

            @Override
            public void drop(final DragAndDropEvent event) {
                if (!isDropValid(event)) {
                    return;
                }
                if (event.getTransferable().getSourceComponent() instanceof Table) {
                    onDropEventFromTable(event);
                } else if (event.getTransferable().getSourceComponent() instanceof DragAndDropWrapper) {
                    onDropEventFromWrapper(event);
                }
            }
        };
    }

    protected Set<I> getDraggedTargetList(final DragAndDropEvent event) {
        final com.vaadin.event.dd.TargetDetails targetDet = event.getTargetDetails();
        final Table targetTable = (Table) targetDet.getTarget();
        final Set<I> targetSelected = getTableValue(targetTable);

        final AbstractSelectTargetDetails dropData = (AbstractSelectTargetDetails) event.getTargetDetails();
        final Object targetItemId = dropData.getItemIdOver();

        if (!targetSelected.contains(targetItemId)) {
            return Sets.newHashSet((I) targetItemId);
        }

        return targetSelected;
    }

    private Set<Object> getDraggedTargetList(final TableTransferable transferable, final Table source) {
        @SuppressWarnings("unchecked")
        final AbstractTable<NamedEntity, Object> table = (AbstractTable<NamedEntity, Object>) source;
        return table.getDeletedEntityByTransferable(transferable);
    }

    private boolean validateDropList(final Set<?> droplist) {
        if (droplist.isEmpty()) {
            final String actionDidNotWork = i18n.get("message.action.did.not.work", new Object[] {});
            notification.displayValidationError(actionDidNotWork);
            return false;
        }
        return true;
    }

    protected boolean isDropValid(final DragAndDropEvent dragEvent) {
        final Transferable transferable = dragEvent.getTransferable();
        final Component compsource = transferable.getSourceComponent();

        if (!hasDropPermission()) {
            notification.displayValidationError(i18n.get("message.permission.insufficient"));
            return false;
        }

        if (compsource instanceof Table) {
            return validateTable((Table) compsource)
                    && validateDropList(getDraggedTargetList((TableTransferable) transferable, (Table) compsource));
        }

        if (compsource instanceof DragAndDropWrapper) {
            return validateDragAndDropWrapper((DragAndDropWrapper) compsource)
                    && validateDropList(getDraggedTargetList(dragEvent));
        }
        notification.displayValidationError(i18n.get(ACTION_NOT_ALLOWED_MSG));
        return false;
    }

    private boolean validateTable(final Table compsource) {
        if (!compsource.getId().equals(getDropTableId())) {
            notification.displayValidationError(ACTION_NOT_ALLOWED_MSG);
            return false;
        }
        return true;
    }

    /**
     * Refresh the container.
     */
    @Override
    public void refreshContainer() {
        final Container container = getContainerDataSource();
        if (!(container instanceof LazyQueryContainer)) {
            return;
        }
        ((LazyQueryContainer) getContainerDataSource()).refresh();
    }

    protected abstract boolean hasDropPermission();

    protected abstract boolean validateDragAndDropWrapper(final DragAndDropWrapper wrapperSource);

    protected abstract void onDropEventFromWrapper(DragAndDropEvent event);

    protected abstract void onDropEventFromTable(DragAndDropEvent event);

    protected abstract String getDropTableId();

    protected abstract AcceptCriterion getDropAcceptCriterion();

    protected abstract void setDataAvailable(boolean available);

}
