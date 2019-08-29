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
import org.eclipse.hawkbit.ui.artifacts.event.ArtifactDetailsEvent;
import org.eclipse.hawkbit.ui.common.data.mappers.ArtifactToProxyArtifactMapper;
import org.eclipse.hawkbit.ui.common.data.providers.ArtifactDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyArtifact;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.grid.AbstractGrid;
import org.eclipse.hawkbit.ui.common.grid.support.DeleteSupport;
import org.eclipse.hawkbit.ui.common.grid.support.ResizeSupport;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.UI;

/**
 * Artifact Details grid which is shown on the Upload View.
 */
public class ArtifactDetailsGrid extends AbstractGrid<ProxyArtifact, Long> {
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
    private final ArtifactToProxyArtifactMapper artifactToProxyMapper;
    private final DeleteSupport<ProxyArtifact> artifactDeleteSupport;

    public ArtifactDetailsGrid(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker, final UINotification notification,
            final ArtifactManagement artifactManagement) {
        super(i18n, eventBus, permissionChecker);

        this.artifactManagement = artifactManagement;

        this.artifactToProxyMapper = new ArtifactToProxyArtifactMapper();
        this.artifactDataProvider = new ArtifactDataProvider(artifactManagement, artifactToProxyMapper)
                .withConfigurableFilter();

        setResizeSupport(new ArtifactDetailsResizeSupport());

        // TODO: consider moving to AbstractGrid as default
        setSelectionMode(SelectionMode.NONE);

        this.artifactDeleteSupport = new DeleteSupport<>(this, i18n, i18n.getMessage("artifact.details.header"),
                permissionChecker, notification, this::artifactsDeletionCallback);

        init();
    }

    private void artifactsDeletionCallback(final Collection<ProxyArtifact> artifactsToBeDeleted) {
        final Collection<Long> artifactToBeDeletedIds = artifactsToBeDeleted.stream()
                .map(ProxyIdentifiableEntity::getId).collect(Collectors.toList());
        artifactToBeDeletedIds.forEach(artifactManagement::delete);

        // TODO: should we really pass the artifactsToBeDeleted? We call
        // dataprovider refreshAll anyway after receiving the event
        eventBus.publish(this, new ArtifactDetailsEvent(BaseEntityEventType.REMOVE_ENTITY, artifactToBeDeletedIds));
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.UPLOAD_ARTIFACT_DETAILS_TABLE;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyArtifact, Void, Long> getFilterDataProvider() {
        return artifactDataProvider;
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ArtifactDetailsEvent event) {
        if (BaseEntityEventType.MINIMIZED == event.getEventType()) {
            UI.getCurrent().access(this::createMinimizedContent);
        } else if (BaseEntityEventType.MAXIMIZED == event.getEventType()) {
            UI.getCurrent().access(this::createMaximizedContent);
        } else if (BaseEntityEventType.ADD_ENTITY == event.getEventType()
                || BaseEntityEventType.REMOVE_ENTITY == event.getEventType()) {
            UI.getCurrent().access(this::refreshContainer);
        }
    }

    /**
     * Creates the grid content for maximized-state.
     */
    private void createMaximizedContent() {
        getResizeSupport().createMaximizedContent();
        recalculateColumnWidths();
    }

    /**
     * Creates the grid content for normal (minimized) state.
     */
    private void createMinimizedContent() {
        getResizeSupport().createMinimizedContent();
        recalculateColumnWidths();
    }

    @Override
    public void addColumns() {
        // TODO: check width
        addColumn(ProxyArtifact::getFilename).setId(ARTIFACT_NAME_ID)
                .setCaption(i18n.getMessage("artifact.filename.caption")).setMinimumWidth(100d).setMaximumWidth(150d)
                .setHidable(false).setHidden(false);

        addColumn(ProxyArtifact::getSize).setId(ARTIFACT_SIZE_ID)
                .setCaption(i18n.getMessage("artifact.filesize.bytes.caption")).setMinimumWidth(50d)
                .setMaximumWidth(100d).setHidable(false).setHidden(false);

        addColumn(ProxyArtifact::getModifiedDate).setId(ARTIFACT_MODIFIED_DATE_ID)
                .setCaption(i18n.getMessage("upload.last.modified.date")).setHidable(false).setHidden(false);

        addActionColumns();

        addColumn(ProxyArtifact::getSha1Hash).setId(ARTIFACT_SHA1_ID).setCaption(i18n.getMessage("upload.sha1"))
                .setHidable(true).setHidden(true);

        addColumn(ProxyArtifact::getMd5Hash).setId(ARTIFACT_MD5_ID).setCaption(i18n.getMessage("upload.md5"))
                .setHidable(true).setHidden(true);

        addColumn(ProxyArtifact::getSha256Hash).setId(ARTIFACT_SHA256_ID).setCaption(i18n.getMessage("upload.sha256"))
                .setHidable(true).setHidden(true);
    }

    private void addActionColumns() {
        addComponentColumn(artifact -> buildActionButton(
                clickEvent -> artifactDeleteSupport.openConfirmationWindowDeleteAction(artifact,
                        artifact.getFilename()),
                VaadinIcons.TRASH, UIMessageIdProvider.TOOLTIP_DELETE, SPUIStyleDefinitions.STATUS_ICON_NEUTRAL,
                UIComponentIdProvider.ARTIFACT_DELET_ICON + "." + artifact.getId(),
                artifactDeleteSupport.hasDeletePermission())).setId(ARTIFACT_DELETE_BUTTON_ID)
                        .setCaption(i18n.getMessage("header.action.delete")).setMinimumWidth(50d).setMaximumWidth(50d)
                        .setHidable(false).setHidden(false);
    }

    private Button buildActionButton(final ClickListener clickListener, final VaadinIcons icon,
            final String descriptionProperty, final String style, final String buttonId, final boolean enabled) {
        final Button actionButton = new Button();

        actionButton.addClickListener(clickListener);
        actionButton.setIcon(icon);
        actionButton.setDescription(i18n.getMessage(descriptionProperty));
        actionButton.setEnabled(enabled);
        actionButton.setId(buttonId);
        actionButton.addStyleName("tiny");
        actionButton.addStyleName("borderless");
        actionButton.addStyleName("button-no-border");
        actionButton.addStyleName("action-type-padding");
        actionButton.addStyleName(style);

        return actionButton;
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
        }

        @Override
        public void setMaximizedColumnExpandRatio() {
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
        }

        @Override
        public void setMinimizedColumnExpandRatio() {
        }
    }
}
