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
import org.eclipse.hawkbit.ui.artifacts.details.ArtifactDetailsLayout;
import org.eclipse.hawkbit.ui.artifacts.event.ArtifactDetailsEvent;
import org.eclipse.hawkbit.ui.artifacts.event.SoftwareModuleEvent;
import org.eclipse.hawkbit.ui.artifacts.smtable.SoftwareModuleTableLayout;
import org.eclipse.hawkbit.ui.artifacts.smtype.filter.SMTypeFilterButtons;
import org.eclipse.hawkbit.ui.artifacts.smtype.filter.SMTypeFilterLayout;
import org.eclipse.hawkbit.ui.artifacts.state.ArtifactUploadState;
import org.eclipse.hawkbit.ui.artifacts.upload.UploadDropAreaLayout;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxySoftwareModule;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.dd.criteria.UploadViewClientCriterion;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
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
import com.vaadin.v7.ui.VerticalLayout;

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

    private final VaadinMessageSource i18n;

    private final UINotification uiNotification;

    private final ArtifactUploadState artifactUploadState;

    private final SMTypeFilterLayout filterByTypeLayout;

    private final SoftwareModuleTableLayout smTableLayout;

    private final ArtifactDetailsLayout artifactDetailsLayout;

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
        this.i18n = i18n;
        this.uiNotification = uiNotification;
        this.artifactUploadState = artifactUploadState;
        this.smTableLayout = new SoftwareModuleTableLayout(i18n, permChecker, artifactUploadState, uiNotification,
                eventBus, softwareModuleManagement, softwareModuleTypeManagement, entityFactory,
                uploadViewClientCriterion);
        this.artifactDetailsLayout = new ArtifactDetailsLayout(i18n, eventBus, artifactUploadState, uiNotification,
                artifactManagement, permChecker);
        final SMTypeFilterButtons smTypeFilterButtons = new SMTypeFilterButtons(eventBus, artifactUploadState,
                uploadViewClientCriterion, softwareModuleTypeManagement, i18n, entityFactory, permChecker,
                uiNotification);
        this.filterByTypeLayout = new SMTypeFilterLayout(artifactUploadState, i18n, permChecker, eventBus,
                entityFactory, uiNotification, softwareModuleTypeManagement, smTypeFilterButtons);
        this.dropAreaLayout = new UploadDropAreaLayout(i18n, eventBus, uiNotification, artifactUploadState,
                multipartConfigElement, softwareModuleManagement, artifactManagement);

        // TODO: when smTableLayout extends AbstractGridComponentLayout
        // smTableLayout.registerDetails(artifactDetailsLayout.getMasterDetailsSupport());
    }

    @PostConstruct
    void init() {
        buildLayout();
        restoreState();
        checkNoDataAvaialble();
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

    private VerticalLayout createDetailsAndUploadLayout() {
        detailAndUploadLayout = new VerticalLayout();
        detailAndUploadLayout.addComponent(artifactDetailsLayout);
        detailAndUploadLayout.setComponentAlignment(artifactDetailsLayout, Alignment.TOP_CENTER);
        detailAndUploadLayout.setExpandRatio(artifactDetailsLayout, 1.0F);

        if (permChecker.hasCreateRepositoryPermission()) {
            detailAndUploadLayout.addComponent(dropAreaLayout.getDropAreaWrapper());
        }

        detailAndUploadLayout.setSizeFull();
        detailAndUploadLayout.addStyleName("group");
        detailAndUploadLayout.setSpacing(true);
        return detailAndUploadLayout;
    }

    private GridLayout createMainLayout() {
        createDetailsAndUploadLayout();
        mainLayout = new GridLayout(3, 1);
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setStyleName("fullSize");
        mainLayout.addComponent(filterByTypeLayout, 0, 0);
        mainLayout.addComponent(smTableLayout, 1, 0);
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
        detailAndUploadLayout.addComponent(dropAreaLayout.getDropAreaWrapper());
        mainLayout.addComponent(filterByTypeLayout, 0, 0);
        mainLayout.addComponent(smTableLayout, 1, 0);
        addOtherComponents();
    }

    private void maximizeArtifactoryDetails() {
        mainLayout.setSpacing(false);
        mainLayout.removeComponent(filterByTypeLayout);
        mainLayout.removeComponent(smTableLayout);
        detailAndUploadLayout.removeComponent(dropAreaLayout.getDropAreaWrapper());
        mainLayout.setColumnExpandRatio(1, 0F);
        mainLayout.setColumnExpandRatio(2, 1F);
    }

    private void addOtherComponents() {
        mainLayout.setColumnExpandRatio(1, 0.5F);
        mainLayout.setColumnExpandRatio(2, 0.5F);
    }

    private void checkNoDataAvaialble() {
        if (artifactUploadState.isNoDataAvilableSoftwareModule()) {
            uiNotification.displayValidationError(i18n.getMessage("message.no.data"));
        }
    }

    @Override
    public void browserWindowResized(final BrowserWindowResizeEvent event) {
        showOrHideFilterButtons(event.getWidth());
    }

    private void showOrHideFilterButtons(final int browserWidth) {
        if (browserWidth < SPUIDefinitions.REQ_MIN_BROWSER_WIDTH) {
            filterByTypeLayout.setVisible(false);
            smTableLayout.setShowFilterButtonVisible(true);
        } else if (!artifactUploadState.isSwTypeFilterClosed()) {
            filterByTypeLayout.setVisible(true);
            smTableLayout.setShowFilterButtonVisible(false);
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
