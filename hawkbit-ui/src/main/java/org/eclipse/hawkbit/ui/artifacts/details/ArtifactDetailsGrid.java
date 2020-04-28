/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.details;

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.builder.GridComponentBuilder;
import org.eclipse.hawkbit.ui.common.data.mappers.ArtifactToProxyArtifactMapper;
import org.eclipse.hawkbit.ui.common.data.providers.ArtifactDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyArtifact;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;

/**
 * Artifact Details grid which is shown on the Upload View.
 */
public class ArtifactDetailsGrid extends AbstractGrid<ProxyArtifact, Long>
        implements MasterEntityAwareComponent<ProxySoftwareModule> {
    private static final long serialVersionUID = 1L;

    private static final String ARTIFACT_NAME_ID = "artifactName";
    private static final String ARTIFACT_SIZE_ID = "artifactSize";
    private static final String ARTIFACT_MODIFIED_DATE_ID = "artifactModifiedDate";
    private static final String ARTIFACT_SHA1_ID = "artifactSha1";
    private static final String ARTIFACT_MD5_ID = "artifactMd5";
    private static final String ARTIFACT_SHA256_ID = "artifactSha256";
    private static final String ARTIFACT_DELETE_BUTTON_ID = "artifactDeleteButton";

    private final transient ArtifactManagement artifactManagement;

    private final ConfigurableFilterDataProvider<ProxyArtifact, Void, Long> artifactDataProvider;
    private final transient ArtifactToProxyArtifactMapper artifactToProxyMapper;
    private final transient DeleteSupport<ProxyArtifact> artifactDeleteSupport;

    private Long masterId;

    public ArtifactDetailsGrid(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final ArtifactManagement artifactManagement) {
        super(i18n, eventBus, permissionChecker);

        this.artifactManagement = artifactManagement;

        this.artifactToProxyMapper = new ArtifactToProxyArtifactMapper();
        this.artifactDataProvider = new ArtifactDataProvider(artifactManagement, artifactToProxyMapper)
                .withConfigurableFilter();

        setResizeSupport(new ArtifactDetailsResizeSupport());

        this.artifactDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("artifact.details.header"),
                ProxyArtifact::getFilename, permissionChecker, notification, this::artifactsDeletionCallback,
                UIComponentIdProvider.ARTIFACT_DELETE_CONFIRMATION_DIALOG);

        init();
    }

    @Override
    protected void init() {
        super.init();

        addStyleName("grid-row-border");
    }

    private void artifactsDeletionCallback(final Collection<ProxyArtifact> artifactsToBeDeleted) {
        final Collection<Long> artifactToBeDeletedIds = artifactsToBeDeleted.stream()
                .map(ProxyIdentifiableEntity::getId).collect(Collectors.toList());
        artifactToBeDeletedIds.forEach(artifactManagement::delete);

        eventBus.publish(EventTopics.ENTITY_MODIFIED, this, new EntityModifiedEventPayload(
                EntityModifiedEventType.ENTITY_UPDATED, ProxySoftwareModule.class, masterId));
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.UPLOAD_ARTIFACT_DETAILS_TABLE;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyArtifact, Void, Long> getFilterDataProvider() {
        return artifactDataProvider;
    }

    @Override
    public void masterEntityChanged(final ProxySoftwareModule masterEntity) {
        if (masterEntity == null && masterId == null) {
            return;
        }

        final Long masterEntityId = masterEntity != null ? masterEntity.getId() : null;
        getFilterDataProvider().setFilter(masterEntityId);
        masterId = masterEntityId;
    }

    public Long getMasterEntityId() {
        return masterId;
    }

    /**
     * Creates the grid content for maximized-state.
     */
    public void createMaximizedContent() {
        getResizeSupport().createMaximizedContent();
        recalculateColumnWidths();
    }

    /**
     * Creates the grid content for normal (minimized) state.
     */
    public void createMinimizedContent() {
        getResizeSupport().createMinimizedContent();
        recalculateColumnWidths();
    }

    @Override
    public void addColumns() {
        addColumn(ProxyArtifact::getFilename).setId(ARTIFACT_NAME_ID)
                .setCaption(i18n.getMessage("artifact.filename.caption")).setMinimumWidth(100d).setExpandRatio(1);

        addColumn(ProxyArtifact::getSize).setId(ARTIFACT_SIZE_ID)
                .setCaption(i18n.getMessage("artifact.filesize.bytes.caption")).setMinimumWidth(50d);

        addColumn(ProxyArtifact::getModifiedDate).setId(ARTIFACT_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("upload.last.modified.date")).setMinimumWidth(100d);

        addActionColumns();

        addColumn(ProxyArtifact::getSha1Hash).setId(ARTIFACT_SHA1_ID).setCaption(i18n.getMessage("upload.sha1"))
                .setHidden(true);

        addColumn(ProxyArtifact::getMd5Hash).setId(ARTIFACT_MD5_ID).setCaption(i18n.getMessage("upload.md5"))
                .setHidden(true);

        addColumn(ProxyArtifact::getSha256Hash).setId(ARTIFACT_SHA256_ID).setCaption(i18n.getMessage("upload.sha256"))
                .setHidden(true);
    }

    private void addActionColumns() {
        addComponentColumn(artifact -> GridComponentBuilder.buildActionButton(i18n,
                clickEvent -> artifactDeleteSupport.openConfirmationWindowDeleteAction(artifact), VaadinIcons.TRASH,
                UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.ARTIFACT_DELET_ICON + "." + artifact.getId(),
                artifactDeleteSupport.hasDeletePermission())).setId(ARTIFACT_DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.delete")).setMinimumWidth(80d);
    }

    /**
     * Adds support to resize the ArtifactDetails grid.
     */
    class ArtifactDetailsResizeSupport implements ResizeSupport {

        private final String[] maxColumnOrder = new String[] { ARTIFACT_NAME_ID, ARTIFACT_SIZE_ID, ARTIFACT_SHA1_ID,
                ARTIFACT_MD5_ID, ARTIFACT_SHA256_ID, ARTIFACT_MODIFIED_DATE_ID, ARTIFACT_DELETE_BUTTON_ID };

        private final String[] minColumnOrder = new String[] { ARTIFACT_NAME_ID, ARTIFACT_SIZE_ID,
                ARTIFACT_MODIFIED_DATE_ID, ARTIFACT_DELETE_BUTTON_ID };

        @Override
        public void setMaximizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(maxColumnOrder);
        }

        @Override
        public void setMaximizedHiddenColumns() {
            getColumn(ARTIFACT_SHA1_ID).setHidden(false);
            getColumn(ARTIFACT_MD5_ID).setHidden(false);
            getColumn(ARTIFACT_SHA256_ID).setHidden(false);

            getColumns().forEach(column -> column.setHidable(true));
        }

        @Override
        public void setMaximizedColumnExpandRatio() {
            getColumns().forEach(column -> column.setExpandRatio(1));

            getColumn(ARTIFACT_NAME_ID).setExpandRatio(2);
        }

        @Override
        public void setMinimizedColumnOrder() {
            clearSortOrder();
            setColumnOrder(minColumnOrder);
        }

        @Override
        public void setMinimizedHiddenColumns() {
            getColumn(ARTIFACT_SHA1_ID).setHidden(true);
            getColumn(ARTIFACT_MD5_ID).setHidden(true);
            getColumn(ARTIFACT_SHA256_ID).setHidden(true);

            getColumns().forEach(column -> column.setHidable(false));
        }

        @Override
        public void setMinimizedColumnExpandRatio() {
            getColumns().forEach(column -> column.setExpandRatio(0));

            getColumn(ARTIFACT_NAME_ID).setExpandRatio(1);
        }
    }
}
