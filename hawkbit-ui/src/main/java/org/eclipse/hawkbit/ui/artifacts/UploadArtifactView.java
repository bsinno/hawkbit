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
import org.eclipse.hawkbit.ui.artifacts.event.ArtifactDetailsEvent;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent;
import org.eclipse.hawkbit.ui.artifacts.event.UploadArtifactUIEvent;
import org.eclipse.hawkbit.ui.artifacts.smtable.SoftwareModuleGridLayout;
import org.eclipse.hawkbit.ui.artifacts.smtype.filter.SMTypeFilterLayout;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.artifacts.upload.UploadDropAreaLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.dd.criteria.UploadViewClientCriterion;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Page.BrowserWindowResizeEvent;
import com.vaadin.server.Page.BrowserWindowResizeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * Display artifacts upload view.
 */
@UIScope
@SpringView(name = UploadArtifactView.VIEW_NAME, ui = AbstractHawkbitUI.class)
public class UploadArtifactView extends VerticalLayout implements View, BrowserWindowResizeListener {

    private static final long serialVersionUID = 1L;

    public static final String VIEW_NAME = "spUpload";

    private final transient EventBus.UIEventBus eventBus;

    private final SpPermissionChecker permChecker;

    private final ArtifactUploadState artifactUploadState;

    private final SMTypeFilterLayout filterByTypeLayout;

    private final SoftwareModuleGridLayout smTableLayout;

    private final ArtifactDetailsGridLayout artifactDetailsLayout;

    private final UploadDropAreaLayout dropAreaLayout;

    private VerticalLayout detailAndUploadLayout;

    private GridLayout mainLayout;

    @Autowired
    UploadArtifactView(final UIEventBus eventBus, final SpPermissionChecker permChecker, final VaadinMessageSource i18n,
            final UINotification uiNotification, final ArtifactUploadState artifactUploadState,
            final EntityFactory entityFactory, final SoftwareModuleManagement softwareModuleManagement,
            final SoftwareModuleTypeManagement softwareModuleTypeManagement,
            final UploadViewClientCriterion uploadViewClientCriterion,
            final MultipartConfigElement multipartConfigElement, final ArtifactManagement artifactManagement) {
        this.eventBus = eventBus;
        this.permChecker = permChecker;
        this.artifactUploadState = artifactUploadState;
        this.smTableLayout = new SoftwareModuleGridLayout(i18n, permChecker, artifactUploadState, uiNotification,
                eventBus, softwareModuleManagement, softwareModuleTypeManagement, entityFactory);
        this.artifactDetailsLayout = new ArtifactDetailsGridLayout(i18n, eventBus, artifactUploadState, uiNotification,
                artifactManagement, permChecker);
        this.filterByTypeLayout = new SMTypeFilterLayout(artifactUploadState, i18n, permChecker, eventBus,
                entityFactory, uiNotification, softwareModuleTypeManagement);
        this.dropAreaLayout = new UploadDropAreaLayout(i18n, eventBus, uiNotification, artifactUploadState,
                multipartConfigElement, softwareModuleManagement, artifactManagement);
    }

    @PostConstruct
    void init() {
        buildLayout();
        restoreState();
        eventBus.subscribe(this);
        Page.getCurrent().addBrowserWindowResizeListener(this);
        showOrHideFilterButtons(Page.getCurrent().getBrowserWindowWidth());
    }

    @PreDestroy
    void destroy() {
        eventBus.unsubscribe(this);
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final SoftwareModuleEvent event) {
        if (BaseEntityEventType.MINIMIZED == event.getEventType()) {
            minimizeSwTable();
        } else if (BaseEntityEventType.MAXIMIZED == event.getEventType()) {
            maximizeSwTable();
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final ArtifactDetailsEvent event) {
        if (BaseEntityEventType.MINIMIZED == event.getEventType()) {
            minimizeArtifactoryDetails();
        } else if (BaseEntityEventType.MAXIMIZED == event.getEventType()) {
            maximizeArtifactoryDetails();
        }
    }

    private void restoreState() {
        if (artifactUploadState.isSwModuleTableMaximized()) {
            maximizeSwTable();
        }
        if (artifactUploadState.isArtifactDetailsMaximized()) {
            maximizeArtifactoryDetails();
        }
    }

    private void buildLayout() {
        if (permChecker.hasReadRepositoryPermission() || permChecker.hasCreateRepositoryPermission()) {
            setSizeFull();
            createMainLayout();
            addComponents(mainLayout);
            setExpandRatio(mainLayout, 1);
        }
    }

    private void createDetailsAndUploadLayout() {
        detailAndUploadLayout = new VerticalLayout();
        detailAndUploadLayout.setId(UIComponentIdProvider.UPLOAD_ARTIFACT_DETAILS_AND_UPLOAD_LAYOUT);
        detailAndUploadLayout.addComponent(artifactDetailsLayout);
        detailAndUploadLayout.setComponentAlignment(artifactDetailsLayout, Alignment.TOP_CENTER);
        detailAndUploadLayout.setExpandRatio(artifactDetailsLayout, 1.0F);

        if (permChecker.hasCreateRepositoryPermission()) {
            detailAndUploadLayout.addComponent(dropAreaLayout.getDropAreaLayout());
        }

        detailAndUploadLayout.setSizeFull();
        detailAndUploadLayout.addStyleName("group");
        detailAndUploadLayout.addStyleName("detail-and-upload-layout");
        detailAndUploadLayout.setSpacing(true);
        detailAndUploadLayout.setMargin(false);
    }

    private GridLayout createMainLayout() {
        mainLayout = new GridLayout(3, 1);
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setStyleName("fullSize");
        mainLayout.addComponent(filterByTypeLayout, 0, 0);
        mainLayout.addComponent(smTableLayout, 1, 0);
        createDetailsAndUploadLayout();
        mainLayout.addComponent(detailAndUploadLayout, 2, 0);

        mainLayout.setRowExpandRatio(0, 1.0F);
        mainLayout.setColumnExpandRatio(1, 0.5F);
        mainLayout.setColumnExpandRatio(2, 0.5F);

        return mainLayout;
    }

    private void minimizeSwTable() {
        mainLayout.addComponent(detailAndUploadLayout, 2, 0);
        addOtherComponents();
    }

    private void maximizeSwTable() {
        mainLayout.removeComponent(detailAndUploadLayout);
        mainLayout.setColumnExpandRatio(1, 1F);
        mainLayout.setColumnExpandRatio(2, 0F);
    }

    private void minimizeArtifactoryDetails() {
        mainLayout.setSpacing(true);
        detailAndUploadLayout.addComponent(dropAreaLayout.getDropAreaLayout());
        mainLayout.addComponent(filterByTypeLayout, 0, 0);
        mainLayout.addComponent(smTableLayout, 1, 0);
        addOtherComponents();
    }

    private void maximizeArtifactoryDetails() {
        mainLayout.setSpacing(false);
        mainLayout.removeComponent(filterByTypeLayout);
        mainLayout.removeComponent(smTableLayout);
        detailAndUploadLayout.removeComponent(dropAreaLayout.getDropAreaLayout());
        mainLayout.setColumnExpandRatio(1, 0F);
        mainLayout.setColumnExpandRatio(2, 1F);
    }

    private void addOtherComponents() {
        mainLayout.setColumnExpandRatio(1, 0.5F);
        mainLayout.setColumnExpandRatio(2, 0.5F);
    }

    @Override
    public void browserWindowResized(final BrowserWindowResizeEvent event) {
        showOrHideFilterButtons(event.getWidth());
    }

    private void showOrHideFilterButtons(final int browserWidth) {
        if (browserWidth < SPUIDefinitions.REQ_MIN_BROWSER_WIDTH) {
            eventBus.publish(this, UploadArtifactUIEvent.HIDE_FILTER_BY_TYPE);
        } else if (!artifactUploadState.isSwTypeFilterClosed()) {
            eventBus.publish(this, UploadArtifactUIEvent.SHOW_FILTER_BY_TYPE);
        }
    }

    @Override
    public void enter(final ViewChangeEvent event) {
        if (permChecker.hasReadRepositoryPermission()) {
            artifactUploadState.getSelectedBaseSwModuleId().ifPresent(lastSeletedSmId -> {
                final ProxySoftwareModule smToSelect = new ProxySoftwareModule();
                smToSelect.setId(lastSeletedSmId);

                smTableLayout.getSoftwareModuleGrid().select(smToSelect);
            });
        }
        dropAreaLayout.getUploadButtonLayout().restoreState();
    }

}
