/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.smtable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.upload.FileUploadProgress;
import org.eclipse.hawkbit.ui.common.builder.GridComponentBuilder;
import org.eclipse.hawkbit.ui.common.data.filters.SwFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.AssignedSoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.mappers.SoftwareModuleToProxyMapper;
import org.eclipse.hawkbit.ui.common.data.providers.SoftwareModuleDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.FilterSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterEntitySupport;
import org.eclipse.hawkbit.ui.common.grid.support.SelectionSupport;
import org.eclipse.hawkbit.ui.common.state.GridLayoutUiState;
import org.eclipse.hawkbit.ui.common.state.TypeFilterLayoutUiState;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;

/**
 * Software Module grid.
 */
public class SoftwareModuleGrid extends AbstractGrid<ProxySoftwareModule, SwFilterParams> {
    private static final long serialVersionUID = 1L;

    private static final String SM_NAME_ID = "smName";
    private static final String SM_VERSION_ID = "smVersion";
    private static final String SM_CREATED_BY_ID = "smCreatedBy";
    private static final String SM_CREATED_DATE_ID = "smCreatedDate";
    private static final String SM_MODIFIED_BY_ID = "smModifiedBy";
    private static final String SM_MODIFIED_DATE_ID = "smModifiedDate";
    private static final String SM_DESC_ID = "smDescription";
    private static final String SM_VENDOR_ID = "smVendor";
    private static final String SM_DELETE_BUTTON_ID = "smDeleteButton";

    private final UINotification notification;

    private final TypeFilterLayoutUiState smTypeFilterLayoutUiState;
    private final GridLayoutUiState smGridLayoutUiState;

    private final transient SoftwareModuleManagement softwareModuleManagement;
    private final transient SoftwareModuleToProxyMapper softwareModuleToProxyMapper;

    private final transient DeleteSupport<ProxySoftwareModule> swModuleDeleteSupport;
    private transient MasterEntitySupport<ProxyDistributionSet> masterEntitySupport;

    private final Map<Long, Integer> numberOfArtifactUploadsForSm;

    public SoftwareModuleGrid(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final TypeFilterLayoutUiState smTypeFilterLayoutUiState, final GridLayoutUiState smGridLayoutUiState,
            final SoftwareModuleManagement softwareModuleManagement, final EventView view) {
        super(i18n, eventBus, permissionChecker);

        this.smTypeFilterLayoutUiState = smTypeFilterLayoutUiState;
        this.smGridLayoutUiState = smGridLayoutUiState;
        this.notification = notification;
        this.softwareModuleManagement = softwareModuleManagement;
        this.softwareModuleToProxyMapper = new SoftwareModuleToProxyMapper();

        setSelectionSupport(new SelectionSupport<ProxySoftwareModule>(this, eventBus, EventLayout.SM_LIST, view,
                this::mapIdToProxyEntity, this::getSelectedEntityIdFromUiState, this::setSelectedEntityIdToUiState));
        if (smGridLayoutUiState.isMaximized()) {
            getSelectionSupport().disableSelection();
        } else {
            getSelectionSupport().enableMultiSelection();
        }

        this.swModuleDeleteSupport = new DeleteSupport<>(this, i18n, notification,
                i18n.getMessage("caption.software.module"), ProxySoftwareModule::getNameAndVersion,
                this::deleteSoftwareModules, UIComponentIdProvider.SM_DELETE_CONFIRMATION_DIALOG);

        setFilterSupport(new FilterSupport<>(
                new SoftwareModuleDataProvider(softwareModuleManagement,
                        new AssignedSoftwareModuleToProxyMapper(softwareModuleToProxyMapper)),
                getSelectionSupport()::deselectAll));
        initFilterMappings();
        getFilterSupport().setFilter(new SwFilterParams());

        this.numberOfArtifactUploadsForSm = new HashMap<>();
    }

    private void initFilterMappings() {
        getFilterSupport().addMapping(FilterType.SEARCH, SwFilterParams::setSearchText,
                smGridLayoutUiState.getSearchFilter());
        getFilterSupport().addMapping(FilterType.TYPE, SwFilterParams::setSoftwareModuleTypeId,
                smTypeFilterLayoutUiState.getClickedTypeId());
    }

    @Override
    public void init() {
        super.init();

        addStyleName("grid-row-border");
    }

    public Optional<ProxySoftwareModule> mapIdToProxyEntity(final long entityId) {
        return softwareModuleManagement.get(entityId).map(softwareModuleToProxyMapper::map);
    }

    private Long getSelectedEntityIdFromUiState() {
        return smGridLayoutUiState.getSelectedEntityId();
    }

    private void setSelectedEntityIdToUiState(final Long entityId) {
        smGridLayoutUiState.setSelectedEntityId(entityId);
    }

    private boolean deleteSoftwareModules(final Collection<ProxySoftwareModule> swModulesToBeDeleted) {
        final Collection<Long> swModuleToBeDeletedIds = swModulesToBeDeleted.stream()
                .map(ProxyIdentifiableEntity::getId).collect(Collectors.toList());
        if (isUploadInProgressForSoftwareModule(swModuleToBeDeletedIds)) {
            notification.displayValidationError(i18n.getMessage("message.error.swModule.notDeleted"));

            return false;
        }

        softwareModuleManagement.delete(swModuleToBeDeletedIds);

        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_REMOVED, ProxySoftwareModule.class, swModuleToBeDeletedIds));

        return true;
    }

    private boolean isUploadInProgressForSoftwareModule(final Collection<Long> swModuleToBeDeletedIds) {
        return swModuleToBeDeletedIds.stream().anyMatch(
                smId -> numberOfArtifactUploadsForSm.containsKey(smId) && numberOfArtifactUploadsForSm.get(smId) > 0);
    }

    public void onUploadChanged(final FileUploadProgress fileUploadProgress) {
        final FileUploadProgress.FileUploadStatus uploadProgressEventType = fileUploadProgress.getFileUploadStatus();
        final Long fileUploadSmId = fileUploadProgress.getFileUploadId().getSoftwareModuleId();

        if (fileUploadSmId == null) {
            return;
        }

        if (FileUploadProgress.FileUploadStatus.UPLOAD_STARTED == uploadProgressEventType) {
            numberOfArtifactUploadsForSm.merge(fileUploadSmId, 1, Integer::sum);
        }

        if (FileUploadProgress.FileUploadStatus.UPLOAD_FINISHED == uploadProgressEventType) {
            numberOfArtifactUploadsForSm.computeIfPresent(fileUploadSmId, (smId, oldCount) -> {
                final Integer newCount = oldCount - 1;
                return newCount.equals(0) ? null : newCount;
            });
        }
    }

    public void addDragAndDropSupport() {
        setDragAndDropSupportSupport(
                new DragAndDropSupport<>(this, i18n, notification, Collections.emptyMap(), eventBus));
        if (!smGridLayoutUiState.isMaximized()) {
            getDragAndDropSupportSupport().addDragSource();
        }
    }

    public void addMasterSupport() {
        getFilterSupport().addMapping(FilterType.MASTER, SwFilterParams::setLastSelectedDistributionId);

        masterEntitySupport = new MasterEntitySupport<>(getFilterSupport());

        initMasterDsStyleGenerator();
    }

    private void initMasterDsStyleGenerator() {
        setStyleGenerator(sm -> {
            if (masterEntitySupport.getMasterId() == null || !sm.isAssigned()) {
                return null;
            }

            return String.join("-", UIComponentIdProvider.SM_TYPE_COLOR_CLASS,
                    String.valueOf(sm.getProxyType().getId()));
        });
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.SOFTWARE_MODULE_TABLE;
    }

    @Override
    public void addColumns() {
        addNameColumn().setMinimumWidth(100d).setMaximumWidth(330d).setExpandRatio(2);

        addVersionColumn().setMinimumWidth(100d).setMaximumWidth(150d).setExpandRatio(1);

        addDeleteColumn().setWidth(75d);
    }

    private Column<ProxySoftwareModule, String> addNameColumn() {
        return addColumn(ProxySoftwareModule::getName).setId(SM_NAME_ID).setCaption(i18n.getMessage("header.name"));
    }

    private Column<ProxySoftwareModule, String> addVersionColumn() {
        return addColumn(ProxySoftwareModule::getVersion).setId(SM_VERSION_ID)
                .setCaption(i18n.getMessage("header.version"));
    }

    private Column<ProxySoftwareModule, Button> addDeleteColumn() {
        return addComponentColumn(sm -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> swModuleDeleteSupport.openConfirmationWindowDeleteAction(sm), VaadinIcons.TRASH,
                UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.SM_DELET_ICON + "." + sm.getId(),
                permissionChecker.hasDeleteRepositoryPermission())).setId(SM_DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.delete"));
    }

    @Override
    protected void addMaxColumns() {
        addNameColumn().setMinimumWidth(100d).setExpandRatio(7);

        addCreatedByColumn().setMinimumWidth(100d).setExpandRatio(1);
        addCreatedDateColumn().setMinimumWidth(100d).setExpandRatio(1);
        addModifiedByColumn().setMinimumWidth(100d).setExpandRatio(1);
        addModifiedDateColumn().setMinimumWidth(100d).setExpandRatio(1);

        addDescriptionColumn().setMinimumWidth(100d).setExpandRatio(5);

        addVersionColumn().setMinimumWidth(100d).setExpandRatio(1);

        addVendorColumn().setMinimumWidth(100d).setExpandRatio(1);

        addDeleteColumn().setWidth(75d);

        getColumns().forEach(column -> column.setHidable(true));
    }

    private Column<ProxySoftwareModule, String> addCreatedByColumn() {
        return addColumn(ProxySoftwareModule::getCreatedBy).setId(SM_CREATED_BY_ID)
                .setCaption(i18n.getMessage("header.createdBy"));
    }

    private Column<ProxySoftwareModule, String> addCreatedDateColumn() {
        return addColumn(ProxySoftwareModule::getCreatedDate).setId(SM_CREATED_DATE_ID)
                .setCaption(i18n.getMessage("header.createdDate"));
    }

    private Column<ProxySoftwareModule, String> addModifiedByColumn() {
        return addColumn(ProxySoftwareModule::getLastModifiedBy).setId(SM_MODIFIED_BY_ID)
                .setCaption(i18n.getMessage("header.modifiedBy"));
    }

    private Column<ProxySoftwareModule, String> addModifiedDateColumn() {
        return addColumn(ProxySoftwareModule::getModifiedDate).setId(SM_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("header.modifiedDate"));
    }

    private Column<ProxySoftwareModule, String> addDescriptionColumn() {
        return addColumn(ProxySoftwareModule::getDescription).setId(SM_DESC_ID)
                .setCaption(i18n.getMessage("header.description"));
    }

    private Column<ProxySoftwareModule, String> addVendorColumn() {
        return addColumn(ProxySoftwareModule::getVendor).setId(SM_VENDOR_ID)
                .setCaption(i18n.getMessage("header.vendor"));
    }

    public MasterEntitySupport<ProxyDistributionSet> getMasterEntitySupport() {
        return masterEntitySupport;
    }
}
