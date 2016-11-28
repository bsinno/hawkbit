/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.details;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.ui.artifacts.event.ArtifactDetailsEvent;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent.SoftwareModuleEventType;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.common.ConfirmationDialog;
import org.eclipse.hawkbit.ui.common.builder.LabelBuilder;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.components.SPUIButton;
import org.eclipse.hawkbit.ui.components.SPUIComponentProvider;
import org.eclipse.hawkbit.ui.decorators.SPUIButtonStyleSmallNoBorder;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.I18N;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.SPUILabelDefinitions;
import org.eclipse.hawkbit.ui.utils.SpringContextHelper;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.vaadin.addons.lazyquerycontainer.BeanQueryFactory;
import org.vaadin.addons.lazyquerycontainer.LazyQueryContainer;
import org.vaadin.addons.lazyquerycontainer.LazyQueryDefinition;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.vaadin.data.Container;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Display the details of the artifacts for a selected software module.
 */
public class ArtifactDetailsLayout extends VerticalLayout {

    private static final long serialVersionUID = -5189069028037133891L;

    private static final String PROVIDED_FILE_NAME = "filename";

    private static final String LAST_MODIFIED_DATE = "lastModifiedAt";

    private static final String CREATE_MODIFIED_DATE_UPLOAD = "Created/Modified Date";

    private static final String ACTION = "action";

    private static final String CREATED_DATE = "createdAt";

    private static final String SIZE = "size";

    private static final String SHA1HASH = "sha1Hash";

    private static final String MD5HASH = "md5Hash";

    private final I18N i18n;

    private final EventBus.UIEventBus eventBus;

    private final ArtifactUploadState artifactUploadState;

    private final UINotification uINotification;

    private Label titleOfArtifactDetails;

    private SPUIButton maxMinButton;

    private Table artifactDetailsTable;

    private Table maxArtifactDetailsTable;

    private boolean fullWindowMode;

    private boolean readOnly;

    public ArtifactDetailsLayout(final I18N i18n, final UIEventBus eventBus,
            final ArtifactUploadState artifactUploadState, final UINotification uINotification) {
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.artifactUploadState = artifactUploadState;
        this.uINotification = uINotification;

        createComponents();
        buildLayout();
        eventBus.subscribe(this);
        if (artifactUploadState.getSelectedBaseSoftwareModule().isPresent()) {
            final SoftwareModule selectedSoftwareModule = artifactUploadState.getSelectedBaseSoftwareModule().get();
            populateArtifactDetails(selectedSoftwareModule.getId(), HawkbitCommonUtil
                    .getFormattedNameVersion(selectedSoftwareModule.getName(), selectedSoftwareModule.getVersion()));
        }
        if (isMaximized()) {
            maximizedArtifactDetailsView();
        }
    }

    private void createComponents() {
        final String labelStr = artifactUploadState.getSelectedBaseSoftwareModule()
                .map(softwareModule -> HawkbitCommonUtil.getFormattedNameVersion(softwareModule.getName(),
                        softwareModule.getVersion()))
                .orElse("");
        titleOfArtifactDetails = new LabelBuilder().name(HawkbitCommonUtil.getArtifactoryDetailsLabelId(labelStr))
                .buildCaptionLabel();
        titleOfArtifactDetails.setContentMode(ContentMode.HTML);
        titleOfArtifactDetails.setSizeFull();
        titleOfArtifactDetails.setImmediate(true);
        maxMinButton = createMaxMinButton();

        artifactDetailsTable = createArtifactDetailsTable();

        artifactDetailsTable.setContainerDataSource(createArtifactLazyQueryContainer());
        addGeneratedColumn(artifactDetailsTable);
        if (!readOnly) {
            addGeneratedColumnButton(artifactDetailsTable);
        }
        setTableColumnDetails(artifactDetailsTable);

    }

    /**
     * @return
     */
    private SPUIButton createMaxMinButton() {
        final SPUIButton button = (SPUIButton) SPUIComponentProvider.getButton(SPUIDefinitions.EXPAND_ACTION_HISTORY,
                "", "", null, true, FontAwesome.EXPAND, SPUIButtonStyleSmallNoBorder.class);
        button.addClickListener(event -> maxArtifactDetails());
        return button;
    }

    private void buildLayout() {

        final HorizontalLayout header = new HorizontalLayout();
        header.addStyleName("artifact-details-header");
        header.addStyleName("bordered-layout");
        header.addStyleName("no-border-bottom");
        header.setSpacing(false);
        header.setMargin(false);
        header.setSizeFull();
        header.setHeightUndefined();
        header.setImmediate(true);
        header.addComponents(titleOfArtifactDetails, maxMinButton);
        header.setComponentAlignment(titleOfArtifactDetails, Alignment.TOP_LEFT);
        header.setComponentAlignment(maxMinButton, Alignment.TOP_RIGHT);
        header.setExpandRatio(titleOfArtifactDetails, 1.0F);

        setSizeFull();
        setImmediate(true);
        addStyleName("artifact-table");
        addStyleName("table-layout");
        addComponent(header);
        setComponentAlignment(header, Alignment.MIDDLE_CENTER);
        addComponent(artifactDetailsTable);
        setComponentAlignment(artifactDetailsTable, Alignment.MIDDLE_CENTER);
        setExpandRatio(artifactDetailsTable, 1.0F);

    }

    private Container createArtifactLazyQueryContainer() {
        return getArtifactLazyQueryContainer(Collections.emptyMap());
    }

    private LazyQueryContainer getArtifactLazyQueryContainer(final Map<String, Object> queryConfig) {

        final BeanQueryFactory<ArtifactBeanQuery> artifactQF = new BeanQueryFactory<>(ArtifactBeanQuery.class);
        artifactQF.setQueryConfiguration(queryConfig);
        final LazyQueryContainer artifactCont = new LazyQueryContainer(new LazyQueryDefinition(true, 10, "id"),
                artifactQF);
        addArtifactTableProperties(artifactCont);
        return artifactCont;
    }

    private void addArtifactTableProperties(final LazyQueryContainer artifactCont) {
        artifactCont.addContainerProperty(PROVIDED_FILE_NAME, Label.class, "", false, false);
        artifactCont.addContainerProperty(SIZE, Long.class, null, false, false);
        artifactCont.addContainerProperty(SHA1HASH, String.class, null, false, false);
        artifactCont.addContainerProperty(MD5HASH, String.class, null, false, false);
        artifactCont.addContainerProperty(CREATED_DATE, Date.class, null, false, false);
        artifactCont.addContainerProperty(LAST_MODIFIED_DATE, Date.class, null, false, false);
        if (!readOnly) {
            artifactCont.addContainerProperty(ACTION, Label.class, null, false, false);
        }
    }

    private void addGeneratedColumn(final Table table) {
        table.addGeneratedColumn(CREATE_MODIFIED_DATE_UPLOAD, new ColumnGenerator() {
            private static final long serialVersionUID = -866800417175863258L;

            @Override
            public String generateCell(final Table source, final Object itemId, final Object columnId) {
                final Long createdDate = (Long) table.getContainerDataSource().getItem(itemId)
                        .getItemProperty(CREATED_DATE).getValue();
                final Long modifiedDATE = (Long) table.getContainerDataSource().getItem(itemId)
                        .getItemProperty(LAST_MODIFIED_DATE).getValue();
                if (modifiedDATE != null) {
                    return SPDateTimeUtil.getFormattedDate(modifiedDATE);
                }
                return SPDateTimeUtil.getFormattedDate(createdDate);
            }
        });

    }

    private void addGeneratedColumnButton(final Table table) {
        table.addGeneratedColumn(ACTION, new ColumnGenerator() {
            private static final long serialVersionUID = -866800417175863258L;

            @Override
            public Button generateCell(final Table source, final Object itemId, final Object columnId) {
                final String fileName = (String) table.getContainerDataSource().getItem(itemId)
                        .getItemProperty(PROVIDED_FILE_NAME).getValue();
                final Button deleteIcon = SPUIComponentProvider.getButton(
                        fileName + "-" + UIComponentIdProvider.UPLOAD_FILE_DELETE_ICON, "",
                        SPUILabelDefinitions.DISCARD, ValoTheme.BUTTON_TINY + " " + "redicon", true,
                        FontAwesome.TRASH_O, SPUIButtonStyleSmallNoBorder.class);
                deleteIcon.setData(itemId);
                deleteIcon.addClickListener(event -> confirmAndDeleteArtifact((Long) itemId, fileName));
                return deleteIcon;
            }
        });
    }

    private void confirmAndDeleteArtifact(final Long id, final String fileName) {

        final ConfirmationDialog confirmDialog = new ConfirmationDialog(i18n.get("caption.delete.artifact.confirmbox"),
                i18n.get("message.delete.artifact", new Object[] { fileName }), i18n.get("button.ok"),
                i18n.get("button.cancel"), ok -> {
                    if (ok) {
                        final ArtifactManagement artifactManagement = SpringContextHelper
                                .getBean(ArtifactManagement.class);
                        artifactManagement.deleteArtifact(id);
                        uINotification.displaySuccess(i18n.get("message.artifact.deleted", fileName));
                        if (artifactUploadState.getSelectedBaseSwModuleId().isPresent()) {
                            populateArtifactDetails(artifactUploadState.getSelectedBaseSwModuleId().get(),
                                    HawkbitCommonUtil.getFormattedNameVersion(
                                            artifactUploadState.getSelectedBaseSoftwareModule().get().getName(),
                                            artifactUploadState.getSelectedBaseSoftwareModule().get().getVersion()));
                        } else {
                            populateArtifactDetails(null, null);
                        }
                    }
                });
        UI.getCurrent().addWindow(confirmDialog.getWindow());
        confirmDialog.getWindow().bringToFront();

    }

    private void setTableColumnDetails(final Table table) {

        table.setColumnHeader(PROVIDED_FILE_NAME, i18n.get("upload.file.name"));
        table.setColumnHeader(SIZE, i18n.get("upload.size"));
        if (fullWindowMode) {
            table.setColumnHeader(SHA1HASH, i18n.get("upload.sha1"));
            table.setColumnHeader(MD5HASH, i18n.get("upload.md5"));
        }
        table.setColumnHeader(CREATE_MODIFIED_DATE_UPLOAD, i18n.get("upload.last.modified.date"));
        if (!readOnly) {
            table.setColumnHeader(ACTION, i18n.get("upload.action"));
        }

        table.setColumnExpandRatio(PROVIDED_FILE_NAME, 3.5F);
        table.setColumnExpandRatio(SIZE, 2f);
        if (fullWindowMode) {
            table.setColumnExpandRatio(SHA1HASH, 2.8F);
            table.setColumnExpandRatio(MD5HASH, 2.4F);
        }
        table.setColumnExpandRatio(CREATE_MODIFIED_DATE_UPLOAD, 3F);
        if (!readOnly) {
            table.setColumnExpandRatio(ACTION, 2.5F);
        }

        table.setVisibleColumns(getVisbleColumns().toArray());
    }

    private List<Object> getVisbleColumns() {
        final List<Object> visibileColumn = new ArrayList<>();
        visibileColumn.add(PROVIDED_FILE_NAME);
        visibileColumn.add(SIZE);
        if (fullWindowMode) {
            visibileColumn.add(SHA1HASH);
            visibileColumn.add(MD5HASH);
        }
        visibileColumn.add(CREATE_MODIFIED_DATE_UPLOAD);
        if (!readOnly) {
            visibileColumn.add(ACTION);
        }
        return visibileColumn;
    }

    private Table createArtifactDetailsTable() {
        final Table detailsTable = new Table();
        detailsTable.addStyleName("sp-table");

        detailsTable.setImmediate(true);
        detailsTable.setSizeFull();

        detailsTable.setId(UIComponentIdProvider.UPLOAD_ARTIFACT_DETAILS_TABLE);
        detailsTable.addStyleName(ValoTheme.TABLE_NO_VERTICAL_LINES);
        detailsTable.addStyleName(ValoTheme.TABLE_SMALL);
        return detailsTable;
    }

    /**
     * will be used by button click listener of action history expand icon.
     */
    private void maxArtifactDetails() {
        final Boolean flag = (Boolean) maxMinButton.getData();
        if (flag == null || Boolean.FALSE.equals(flag)) {
            // Clicked on max Button
            maximizedArtifactDetailsView();
        } else {
            // Clicked on min Button
            minimizeArtifactDetailsView();
        }
    }

    private void minimizeArtifactDetailsView() {
        fullWindowMode = Boolean.FALSE;
        showMaxIcon();
        setTableColumnDetails(artifactDetailsTable);
        createArtifactDetailsMinView();

    }

    private void maximizedArtifactDetailsView() {
        fullWindowMode = Boolean.TRUE;
        showMinIcon();
        setTableColumnDetails(artifactDetailsTable);
        createArtifactDetailsMaxView();

    }

    /**
     * Create Max artifact details Table.
     */
    public void createMaxArtifactDetailsTable() {
        maxArtifactDetailsTable = createArtifactDetailsTable();
        maxArtifactDetailsTable.setId(UIComponentIdProvider.UPLOAD_ARTIFACT_DETAILS_TABLE_MAX);
        maxArtifactDetailsTable.setContainerDataSource(artifactDetailsTable.getContainerDataSource());
        addGeneratedColumn(maxArtifactDetailsTable);
        if (!readOnly) {
            addGeneratedColumnButton(maxArtifactDetailsTable);
        }
        setTableColumnDetails(maxArtifactDetailsTable);
    }

    private void createArtifactDetailsMaxView() {

        artifactDetailsTable.setValue(null);
        artifactDetailsTable.setSelectable(false);
        artifactDetailsTable.setMultiSelect(false);
        artifactDetailsTable.setDragMode(TableDragMode.NONE);
        artifactDetailsTable.setColumnCollapsingAllowed(true);
        artifactUploadState.setArtifactDetailsMaximized(Boolean.TRUE);
        eventBus.publish(this, ArtifactDetailsEvent.MAXIMIZED);
    }

    private void createArtifactDetailsMinView() {
        artifactUploadState.setArtifactDetailsMaximized(Boolean.FALSE);
        artifactDetailsTable.setColumnCollapsingAllowed(false);
        eventBus.publish(this, ArtifactDetailsEvent.MINIMIZED);
    }

    /**
     * Populate artifact details.
     *
     * @param baseSwModuleId
     *            software module id
     * @param swModuleName
     *            software module name
     */
    public void populateArtifactDetails(final Long baseSwModuleId, final String swModuleName) {
        if (!readOnly) {
            if (Strings.isNullOrEmpty(swModuleName)) {
                setTitleOfLayoutHeader();
            } else {
                titleOfArtifactDetails.setValue(HawkbitCommonUtil.getArtifactoryDetailsLabelId(swModuleName));
                titleOfArtifactDetails.setContentMode(ContentMode.HTML);
            }
        }
        final Map<String, Object> queryConfiguration;
        if (baseSwModuleId != null) {
            queryConfiguration = Maps.newHashMapWithExpectedSize(1);
            queryConfiguration.put(SPUIDefinitions.BY_BASE_SOFTWARE_MODULE, baseSwModuleId);
        } else {
            queryConfiguration = Collections.emptyMap();
        }
        final LazyQueryContainer artifactContainer = getArtifactLazyQueryContainer(queryConfiguration);
        artifactDetailsTable.setContainerDataSource(artifactContainer);
        if (fullWindowMode && maxArtifactDetailsTable != null) {
            maxArtifactDetailsTable.setContainerDataSource(artifactContainer);
        }
        setTableColumnDetails(artifactDetailsTable);

    }

    /**
     * Set title of artifact details header layout.
     */
    public void setTitleOfLayoutHeader() {
        titleOfArtifactDetails.setValue(HawkbitCommonUtil.getArtifactoryDetailsLabelId(""));
        titleOfArtifactDetails.setContentMode(ContentMode.HTML);
    }

    /**
     * Close artifact details layout.
     */
    public void closeArtifactDetails() {
        removeAllComponents();
        setVisible(false);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SoftwareModuleEvent softwareModuleEvent) {
        if (BaseEntityEventType.SELECTED_ENTITY == softwareModuleEvent.getEventType()) {
            UI.getCurrent().access(() -> {
                if (softwareModuleEvent.getEntity() != null) {
                    populateArtifactDetails(softwareModuleEvent.getEntity().getId(),
                            HawkbitCommonUtil.getFormattedNameVersion(softwareModuleEvent.getEntity().getName(),
                                    softwareModuleEvent.getEntity().getVersion()));
                } else {
                    populateArtifactDetails(null, null);
                }
            });
        }
        if (softwareModuleEvent.getSoftwareModuleEventType() == SoftwareModuleEventType.ARTIFACTS_CHANGED) {
            UI.getCurrent().access(() -> {
                if (softwareModuleEvent.getEntity() != null) {
                    populateArtifactDetails(softwareModuleEvent.getEntity().getId(),
                            HawkbitCommonUtil.getFormattedNameVersion(softwareModuleEvent.getEntity().getName(),
                                    softwareModuleEvent.getEntity().getVersion()));
                } else {
                    populateArtifactDetails(null, null);
                }
            });
        }
    }

    public Table getArtifactDetailsTable() {
        return artifactDetailsTable;
    }

    public Table getMaxArtifactDetailsTable() {
        return maxArtifactDetailsTable;
    }

    public void setFullWindowMode(final boolean fullWindowMode) {
        this.fullWindowMode = fullWindowMode;
    }

    private void showMinIcon() {
        maxMinButton.togleIcon(FontAwesome.COMPRESS);
        maxMinButton.setData(Boolean.TRUE);
    }

    private void showMaxIcon() {
        maxMinButton.togleIcon(FontAwesome.EXPAND);
        maxMinButton.setData(Boolean.FALSE);
    }

    private boolean isMaximized() {
        return artifactUploadState.isArtifactDetailsMaximized();
    }
}
