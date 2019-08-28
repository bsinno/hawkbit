/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts.details;

import java.util.Optional;

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.event.ArtifactDetailsEvent;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent.SoftwareModuleEventType;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyArtifact;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.grid.AbstractGridComponentLayout;
import org.eclipse.hawkbit.ui.common.grid.DefaultGridHeader;
import org.eclipse.hawkbit.ui.common.grid.DefaultGridHeader.AbstractHeaderMaximizeSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupport;
import org.eclipse.hawkbit.ui.common.grid.support.MasterDetailsSupportIdentifiable;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

/**
 * Display the details of the artifacts for a selected software module.
 */
public class ArtifactDetailsLayout extends AbstractGridComponentLayout<ProxyArtifact> {

    private static final long serialVersionUID = 1L;

    private final ArtifactUploadState artifactUploadState;
    private final String artifactDetailsCaption;

    private final ArtifactDetailsHeader artifactDetailsHeader;
    private final ArtifactDetailsGrid artifactDetailsGrid;

    private final MasterDetailsSupport<ProxySoftwareModule, Long> masterDetailsSupport;

    /**
     * Constructor for ArtifactDetailsLayout
     * 
     * @param i18n
     *            VaadinMessageSource
     * @param eventBus
     *            UIEventBus
     * @param artifactUploadState
     *            ArtifactUploadState
     * @param notification
     *            UINotification
     * @param artifactManagement
     *            ArtifactManagement
     */
    public ArtifactDetailsLayout(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final ArtifactUploadState artifactUploadState, final UINotification notification,
            final ArtifactManagement artifactManagement, final SpPermissionChecker permChecker) {
        super(i18n, eventBus);

        this.artifactUploadState = artifactUploadState;
        this.artifactDetailsCaption = getArtifactDetailsCaption(null);

        this.artifactDetailsHeader = new ArtifactDetailsHeader().init();
        this.artifactDetailsGrid = new ArtifactDetailsGrid(eventBus, i18n, permChecker, notification,
                artifactManagement);

        this.masterDetailsSupport = new MasterDetailsSupportIdentifiable<>(artifactDetailsGrid);

        init();
    }

    private String getArtifactDetailsCaption(final String swModuleNameVersion) {
        final String caption;
        if (StringUtils.hasText(swModuleNameVersion)) {
            caption = getI18n().getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_DETAILS_OF,
                    HawkbitCommonUtil.getBoldHTMLText(swModuleNameVersion));
        } else {
            caption = getI18n().getMessage(UIMessageIdProvider.CAPTION_ARTIFACT_DETAILS);
        }

        return HawkbitCommonUtil.getCaptionText(caption);
    }

    @Override
    public ArtifactDetailsHeader getGridHeader() {
        return artifactDetailsHeader;
    }

    @Override
    public ArtifactDetailsGrid getGrid() {
        return artifactDetailsGrid;
    }

    // TODO: check if it can be removed with registerDetails in
    // UploadArtifactView
    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SoftwareModuleEvent swModuleUIEvent) {
        final Optional<Long> swModuleId = artifactUploadState.getSelectedBaseSwModuleId();

        if (BaseEntityEventType.SELECTED_ENTITY == swModuleUIEvent.getEventType()
                || SoftwareModuleEventType.ARTIFACTS_CHANGED == swModuleUIEvent.getSoftwareModuleEventType()) {
            UI.getCurrent().access(() -> populateArtifactDetails(swModuleUIEvent.getEntity()));
        } else if (BaseEntityEventType.REMOVE_ENTITY == swModuleUIEvent.getEventType() && swModuleId.isPresent()
                && swModuleUIEvent.getEntityIds().contains(swModuleId.get())) {
            UI.getCurrent().access(() -> populateArtifactDetails(null));
        }
    }

    /**
     * Populate artifact details header and grid for the software module.
     *
     * @param swModule
     *            the Software Module
     */
    public void populateArtifactDetails(final ProxySoftwareModule swModule) {
        if (swModule != null) {
            artifactDetailsHeader.updateArtifactDetailsHeader(swModule.getNameAndVersion());
            masterDetailsSupport.masterItemChangedCallback(swModule);
        } else {
            artifactDetailsHeader.updateArtifactDetailsHeader(" ");
            masterDetailsSupport.masterItemChangedCallback(null);
        }
    }

    public MasterDetailsSupport<ProxySoftwareModule, Long> getMasterDetailsSupport() {
        return masterDetailsSupport;
    }

    /**
     * Header for ArtifactDetails with maximize-support.
     */
    class ArtifactDetailsHeader extends DefaultGridHeader {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param managementUIState
         */
        ArtifactDetailsHeader() {
            super(artifactDetailsCaption, getI18n());

            this.setHeaderMaximizeSupport(
                    new ArtifactDetailsHeaderMaxSupport(this, SPUIDefinitions.EXPAND_ARTIFACT_DETAILS));
        }

        /**
         * Initializes the header.
         */
        @Override
        public ArtifactDetailsHeader init() {
            super.init();
            addStyleName("artifact-details-header");
            restorePreviousState();
            return this;
        }

        /**
         * Updates header with target name.
         *
         * @param swModuleNameVersion
         *            name and version of the software module
         */
        public void updateArtifactDetailsHeader(final String swModuleNameVersion) {
            updateTitle(getArtifactDetailsCaption(swModuleNameVersion));
        }

        /**
         * Restores the previous min-max state.
         */
        private void restorePreviousState() {
            if (hasHeaderMaximizeSupport() && artifactUploadState.isArtifactDetailsMaximized()) {
                getHeaderMaximizeSupport().showMinIcon();
            }
        }
    }

    /**
     * Min-max support for header.
     */
    class ArtifactDetailsHeaderMaxSupport extends AbstractHeaderMaximizeSupport {

        private final DefaultGridHeader abstractGridHeader;

        /**
         * Constructor.
         *
         * @param abstractGridHeader
         * @param maximizeButtonId
         */
        protected ArtifactDetailsHeaderMaxSupport(final DefaultGridHeader abstractGridHeader,
                final String maximizeButtonId) {
            abstractGridHeader.super(maximizeButtonId);
            this.abstractGridHeader = abstractGridHeader;
        }

        @Override
        protected void maximize() {
            // TODO: check if it is needed
            // details.populateMasterDataAndRecreateContainer(masterForDetails);
            getEventBus().publish(this, new ArtifactDetailsEvent(BaseEntityEventType.MAXIMIZED));
            artifactUploadState.setArtifactDetailsMaximized(true);
        }

        @Override
        protected void minimize() {
            getEventBus().publish(this, new ArtifactDetailsEvent(BaseEntityEventType.MINIMIZED));
            artifactUploadState.setArtifactDetailsMaximized(false);
        }

        /**
         * Gets the grid header the maximize support is for.
         *
         * @return grid header
         */
        protected DefaultGridHeader getGridHeader() {
            return abstractGridHeader;
        }
    }
}
