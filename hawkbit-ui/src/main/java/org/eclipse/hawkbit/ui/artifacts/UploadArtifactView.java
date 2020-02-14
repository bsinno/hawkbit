/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.artifacts;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.MultipartConfigElement;

import org.eclipse.hawkbit.repository.ArtifactManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.ui.AbstractHawkbitUI;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.artifacts.details.ArtifactDetailsGridLayout;
import org.eclipse.hawkbit.ui.artifacts.smtable.SoftwareModuleGridLayout;
import org.eclipse.hawkbit.ui.artifacts.smtype.filter.SMTypeFilterLayout;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.navigator.View;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Display artifacts upload view.
 */
@UIScope
@SpringView(name = UploadArtifactView.VIEW_NAME, ui = AbstractHawkbitUI.class)
public class UploadArtifactView extends VerticalLayout implements View, BrowserWindowResizeListener {
    private static final long serialVersionUID = 1L;

    public static final String VIEW_NAME = "spUpload";

    private final SpPermissionChecker permChecker;
    private final ArtifactUploadState artifactUploadState;

    private final SMTypeFilterLayout smTypeFilterLayout;
    private final SoftwareModuleGridLayout smGridLayout;
    private final ArtifactDetailsGridLayout artifactDetailsGridLayout;

    private HorizontalLayout mainLayout;

    private final transient UploadArtifactViewEventListener eventListener;

    @Autowired
    UploadArtifactView(final UIEventBus eventBus, final SpPermissionChecker permChecker, final VaadinMessageSource i18n,
            final UINotification uiNotification, final ArtifactUploadState artifactUploadState,
            final EntityFactory entityFactory, final SoftwareModuleManagement softwareModuleManagement,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final MultipartConfigElement multipartConfigElement, final ArtifactManagement artifactManagement) {
        this.permChecker = permChecker;
        this.artifactUploadState = artifactUploadState;

        if (permChecker.hasReadRepositoryPermission()) {
            this.smTypeFilterLayout = new SMTypeFilterLayout(i18n, permChecker, eventBus, entityFactory, uiNotification,
                    softwareModuleTypeManagement, artifactUploadState.getSmTypeFilterLayoutUiState());
            this.smGridLayout = new SoftwareModuleGridLayout(i18n, permChecker, uiNotification, eventBus,
                    softwareModuleManagement, softwareModuleTypeManagement, entityFactory, artifactUploadState,
                    artifactUploadState.getSmTypeFilterLayoutUiState(), artifactUploadState.getSmGridLayoutUiState());
            this.artifactDetailsGridLayout = new ArtifactDetailsGridLayout(i18n, eventBus, permChecker, uiNotification,
                    artifactUploadState, artifactUploadState.getArtifactDetailsGridLayoutUiState(), artifactManagement,
                    softwareModuleManagement, multipartConfigElement);
            this.eventListener = new UploadArtifactViewEventListener(this, eventBus);
        } else {
            this.smTypeFilterLayout = null;
            this.smGridLayout = null;
            this.artifactDetailsGridLayout = null;
            this.eventListener = null;
        }
    }

    @PostConstruct
    void init() {
        if (permChecker.hasReadRepositoryPermission()) {
            buildLayout();
            restoreState();
            Page.getCurrent().addBrowserWindowResizeListener(this);
        }
    }

    private void buildLayout() {
        setMargin(false);
        setSpacing(false);
        setSizeFull();

        createMainLayout();

        addComponent(mainLayout);
        setExpandRatio(mainLayout, 1.0F);
    }

    private void createMainLayout() {
        mainLayout = new HorizontalLayout();
        mainLayout.setSizeFull();
        mainLayout.setMargin(false);
        mainLayout.setSpacing(true);

        mainLayout.addComponent(smTypeFilterLayout);
        mainLayout.addComponent(smGridLayout);
        mainLayout.addComponent(artifactDetailsGridLayout);

        mainLayout.setExpandRatio(smTypeFilterLayout, 0F);
        mainLayout.setExpandRatio(smGridLayout, 0.5F);
        mainLayout.setExpandRatio(artifactDetailsGridLayout, 0.5F);
    }

    private void restoreState() {
        if (artifactUploadState.getSmTypeFilterLayoutUiState().isHidden()) {
            hideSmTypeLayout();
        } else {
            showSmTypeLayout();
        }
        smTypeFilterLayout.restoreState();

        if (artifactUploadState.getSmGridLayoutUiState().isMaximized()) {
            maximizeSmGridLayout();
        }
        smGridLayout.restoreState();

        if (artifactUploadState.getArtifactDetailsGridLayoutUiState().isMaximized()) {
            maximizeArtifactGridLayout();
        }
        artifactDetailsGridLayout.restoreState();
    }

    void hideSmTypeLayout() {
        smTypeFilterLayout.setVisible(false);
        smGridLayout.showSmTypeHeaderIcon();
    }

    void showSmTypeLayout() {
        smTypeFilterLayout.setVisible(true);
        smGridLayout.hideSmTypeHeaderIcon();
    }

    void maximizeSmGridLayout() {
        artifactDetailsGridLayout.setVisible(false);

        mainLayout.setExpandRatio(smTypeFilterLayout, 0F);
        mainLayout.setExpandRatio(smGridLayout, 1.0F);
        mainLayout.setExpandRatio(artifactDetailsGridLayout, 0F);

        smGridLayout.maximize();
    }

    void maximizeArtifactGridLayout() {
        smTypeFilterLayout.setVisible(false);
        smGridLayout.setVisible(false);

        mainLayout.setExpandRatio(smTypeFilterLayout, 0F);
        mainLayout.setExpandRatio(smGridLayout, 0F);
        mainLayout.setExpandRatio(artifactDetailsGridLayout, 1.0F);

        artifactDetailsGridLayout.maximize();
    }

    @Override
    public void browserWindowResized(final BrowserWindowResizeEvent event) {
        showOrHideFilterButtons(event.getWidth());
    }

    private void showOrHideFilterButtons(final int browserWidth) {
        if (browserWidth < SPUIDefinitions.REQ_MIN_BROWSER_WIDTH) {
            if (!artifactUploadState.getSmTypeFilterLayoutUiState().isHidden()) {
                hideSmTypeLayout();
            }
        } else {
            if (artifactUploadState.getSmTypeFilterLayoutUiState().isHidden()) {
                showSmTypeLayout();
            }
        }
    }

    void minimizeSmGridLayout() {
        artifactDetailsGridLayout.setVisible(true);

        mainLayout.setExpandRatio(smTypeFilterLayout, 0F);
        mainLayout.setExpandRatio(smGridLayout, 0.5F);
        mainLayout.setExpandRatio(artifactDetailsGridLayout, 0.5F);

        smGridLayout.minimize();
    }

    void minimizeArtifactGridLayout() {
        if (!artifactUploadState.getSmTypeFilterLayoutUiState().isHidden()) {
            smTypeFilterLayout.setVisible(true);
        }
        smGridLayout.setVisible(true);

        mainLayout.setExpandRatio(smTypeFilterLayout, 0F);
        mainLayout.setExpandRatio(smGridLayout, 0.5F);
        mainLayout.setExpandRatio(artifactDetailsGridLayout, 0.5F);

        artifactDetailsGridLayout.minimize();
    }

    @PreDestroy
    void destroy() {
        if (smTypeFilterLayout != null && smGridLayout != null && artifactDetailsGridLayout != null
                && eventListener != null) {
            smTypeFilterLayout.unsubscribeListener();
            smGridLayout.unsubscribeListener();
            artifactDetailsGridLayout.unsubscribeListener();
            eventListener.unsubscribeListeners();
        }
    }
}
