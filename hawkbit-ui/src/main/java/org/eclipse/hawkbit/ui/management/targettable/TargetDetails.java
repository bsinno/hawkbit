/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.UserDetailsFormatter;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyKeyValueDetails;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetAttributesDetails;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractTableDetailsLayout;
import org.eclipse.hawkbit.ui.common.detailslayout.KeyValueDetailsComponent;
import org.eclipse.hawkbit.ui.common.detailslayout.TargetMetadataDetailsLayout;
import org.eclipse.hawkbit.ui.common.tagdetails.TargetTagToken;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.Binder;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Target details layout which is shown on the Deployment View.
 */
public class TargetDetails extends AbstractTableDetailsLayout<ProxyTarget> {
    private static final long serialVersionUID = 1L;

    private final ManagementUIState managementUIState;

    private final TargetTagToken targetTagToken;

    private final TargetMetadataDetailsLayout targetMetadataLayout;

    private final TargetAddUpdateWindowLayout targetAddUpdateWindowLayout;

    private final transient TargetManagement targetManagement;

    private final TargetMetadataPopupLayout targetMetadataPopupLayout;

    private final UINotification uiNotification;

    private final transient DeploymentManagement deploymentManagement;

    private final KeyValueDetailsComponent entityDetails;
    private final TextArea entityDescription;
    private final TargetAttributesDetailsComponent attributesLayout;
    private final KeyValueDetailsComponent assignedDsDetails;
    private final KeyValueDetailsComponent installedDsDetails;
    private final KeyValueDetailsComponent logDetails;
    private final Binder<ProxyTarget> binder;

    TargetDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final ManagementUIState managementUIState,
            final UINotification uiNotification, final TargetTagManagement tagManagement,
            final TargetManagement targetManagement, final DeploymentManagement deploymentManagement,
            final EntityFactory entityFactory, final TargetAddUpdateWindowLayout targetAddUpdateWindowLayout) {
        super(i18n, eventBus, permissionChecker);

        this.binder = new Binder<>();

        this.entityDetails = new KeyValueDetailsComponent();
        binder.forField(entityDetails)
                .bind(target -> Arrays.asList(
                        new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_CONTROLLER_ID,
                                i18n.getMessage("label.target.id"), target.getControllerId()),
                        new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_LAST_QUERY_DT,
                                i18n.getMessage("label.target.lastpolldate"),
                                SPDateTimeUtil.getFormattedDate(target.getLastTargetQuery())),
                        new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_IP_ADDRESS, i18n.getMessage("label.ip"),
                                target.getAddress() != null ? target.getAddress().toString() : ""),
                        new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_SECURITY_TOKEN,
                                i18n.getMessage("label.target.security.token"), target.getSecurityToken())),
                        null);

        this.entityDescription = new TextArea();
        entityDescription.setId(UIComponentIdProvider.TARGET_DETAILS_DESCRIPTION_ID);
        entityDescription.setReadOnly(true);
        entityDescription.setWordWrap(true);
        entityDescription.setSizeFull();
        entityDescription.setStyleName(ValoTheme.TEXTAREA_BORDERLESS);
        entityDescription.addStyleName(ValoTheme.TEXTAREA_SMALL);
        entityDescription.addStyleName("details-description");
        binder.forField(entityDescription).bind(ProxyTarget::getDescription, null);

        this.attributesLayout = new TargetAttributesDetailsComponent(i18n, targetManagement);
        binder.forField(attributesLayout).bind(target -> {
            final String controllerId = target.getControllerId();
            final boolean isRequestAttributes = target.isRequestAttributes();

            final List<Map.Entry<String, String>> targetAttributes = targetManagement
                    .getControllerAttributes(controllerId).entrySet().stream().collect(Collectors.toList());

            final List<ProxyKeyValueDetails> attributes = IntStream.range(0, targetAttributes.size())
                    .mapToObj(i -> new ProxyKeyValueDetails("target.attributes.label" + i,
                            targetAttributes.get(i).getKey(), targetAttributes.get(i).getValue()))
                    .collect(Collectors.toList());

            return new ProxyTargetAttributesDetails(controllerId, isRequestAttributes, attributes);
        }, null);

        this.assignedDsDetails = new KeyValueDetailsComponent();
        binder.forField(assignedDsDetails).bind(target -> {
            final Optional<DistributionSet> targetAssignedDs = deploymentManagement
                    .getAssignedDistributionSet(target.getControllerId());

            return targetAssignedDs.map(this::getDistributionDetails).orElse(null);
        }, null);

        this.installedDsDetails = new KeyValueDetailsComponent();
        binder.forField(installedDsDetails).bind(target -> {
            final Optional<DistributionSet> targetInstalledDs = deploymentManagement
                    .getInstalledDistributionSet(target.getControllerId());

            return targetInstalledDs.map(this::getDistributionDetails).orElse(null);
        }, null);

        this.logDetails = new KeyValueDetailsComponent();
        binder.forField(logDetails).bind(target -> Arrays.asList(
                new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_CREATEDAT_ID, i18n.getMessage("label.created.at"),
                        SPDateTimeUtil.getFormattedDate(target.getCreatedAt())),
                new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_CREATEDBY_ID, i18n.getMessage("label.created.by"),
                        target.getCreatedBy() != null
                                ? UserDetailsFormatter.loadAndFormatUsername(target.getCreatedBy())
                                : ""),
                new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_MODIFIEDAT_ID,
                        i18n.getMessage("label.modified.date"),
                        SPDateTimeUtil.getFormattedDate(target.getLastModifiedAt())),
                new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_MODIFIEDBY_ID,
                        i18n.getMessage("label.modified.by"),
                        target.getCreatedBy() != null
                                ? UserDetailsFormatter.loadAndFormatUsername(target.getLastModifiedBy())
                                : "")),
                null);

        // ------------------------------------------
        this.managementUIState = managementUIState;

        this.targetTagToken = new TargetTagToken(permissionChecker, i18n, uiNotification, eventBus, managementUIState,
                tagManagement, targetManagement);
        this.targetAddUpdateWindowLayout = targetAddUpdateWindowLayout;
        this.uiNotification = uiNotification;
        this.targetManagement = targetManagement;
        this.deploymentManagement = deploymentManagement;
        this.targetMetadataPopupLayout = new TargetMetadataPopupLayout(i18n, uiNotification, eventBus, targetManagement,
                entityFactory, permissionChecker);
        this.targetMetadataLayout = new TargetMetadataDetailsLayout(i18n, targetManagement, targetMetadataPopupLayout);
        addDetailsTab();
        restoreState();
    }

    private List<ProxyKeyValueDetails> getDistributionDetails(final DistributionSet ds) {
        final List<ProxyKeyValueDetails> dsDetails = Arrays.asList(
                new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_ASSIGNED_DS_NAME_ID,
                        i18n.getMessage("label.dist.details.name"), ds.getName()),
                new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_ASSIGNED_DS_VERSION_ID,
                        i18n.getMessage("label.dist.details.version"), ds.getVersion()));

        final List<ProxyKeyValueDetails> dsSmDetails = ds.getModules().stream()
                .map(swModule -> new ProxyKeyValueDetails("target.assigned.ds.sm.id." + swModule.getId(),
                        swModule.getType().getName(), swModule.getName() + ":" + swModule.getVersion()))
                .collect(Collectors.toList());
        dsDetails.addAll(dsSmDetails);

        return dsDetails;
    }

    @Override
    protected String getDefaultCaption() {
        return i18n.getMessage("target.details.header");
    }

    private final void addDetailsTab() {
        getDetailsTab().addTab(buildTabWrapperDetailsLayout(entityDetails), i18n.getMessage("caption.tab.details"),
                null);
        getDetailsTab().addTab(buildTabWrapperDetailsLayout(entityDescription),
                i18n.getMessage("caption.tab.description"), null);
        getDetailsTab().addTab(buildTabWrapperDetailsLayout(attributesLayout),
                i18n.getMessage("caption.attributes.tab"), null);
        getDetailsTab().addTab(buildTabWrapperDetailsLayout(assignedDsDetails),
                i18n.getMessage("header.target.assigned"), null);
        getDetailsTab().addTab(buildTabWrapperDetailsLayout(installedDsDetails),
                i18n.getMessage("header.target.installed"), null);
        getDetailsTab().addTab(getTagsLayout(), i18n.getMessage("caption.tags.tab"), null);
        getDetailsTab().addTab(buildTabWrapperDetailsLayout(logDetails), i18n.getMessage("caption.logs.tab"), null);
        getDetailsTab().addTab(targetMetadataLayout, i18n.getMessage("caption.metadata"), null);
    }

    private VerticalLayout buildTabWrapperDetailsLayout(final Component detailsComponent) {
        final VerticalLayout tabWrapperDetailsLayout = new VerticalLayout();
        tabWrapperDetailsLayout.setSpacing(false);
        tabWrapperDetailsLayout.setMargin(false);
        tabWrapperDetailsLayout.setStyleName("details-layout");

        tabWrapperDetailsLayout.addComponent(detailsComponent);

        return tabWrapperDetailsLayout;
    }

    @Override
    protected void onEdit(final ClickEvent event) {
        if (getSelectedBaseEntity() == null) {
            return;
        }
        openWindow();
    }

    private void openWindow() {
        final Window targetWindow = targetAddUpdateWindowLayout.getWindow(getSelectedBaseEntity().getControllerId());
        if (targetWindow == null) {
            return;
        }
        targetWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.target")));
        UI.getCurrent().addWindow(targetWindow);
        targetWindow.setVisible(Boolean.TRUE);
    }

    @Override
    protected String getEditButtonId() {
        return UIComponentIdProvider.TARGET_EDIT_ICON;
    }

    @Override
    protected boolean onLoadIsTableMaximized() {
        return managementUIState.isTargetTableMaximized();
    }

    @Override
    protected void populateDetailsWidget() {
        binder.setBean(getSelectedBaseEntity());

        populateTags(targetTagToken);
        populateMetadataDetails();
    }

    @Override
    protected boolean hasEditPermission() {
        return permissionChecker.hasUpdateTargetPermission();
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final TargetTableEvent targetTableEvent) {
        onBaseEntityEvent(targetTableEvent);
    }

    @Override
    protected String getTabSheetId() {
        return UIComponentIdProvider.TARGET_DETAILS_TABSHEET;
    }

    @Override
    protected String getDetailsHeaderCaptionId() {
        return UIComponentIdProvider.TARGET_DETAILS_HEADER_LABEL_ID;
    }

    @Override
    protected void showMetadata(final ClickEvent event) {
        final Optional<Target> target = targetManagement.get(getSelectedBaseEntityId());
        if (!target.isPresent()) {
            uiNotification.displayWarning(i18n.getMessage("targets.not.exists"));
            return;
        }
        UI.getCurrent().addWindow(targetMetadataPopupLayout.getWindow(target.get(), null));
    }

    @Override
    protected void populateMetadataDetails() {
        targetMetadataLayout.populateMetadata(getSelectedBaseEntity());
    }

    @Override
    protected String getMetadataButtonId() {
        return UIComponentIdProvider.TARGET_METADATA_BUTTON;
    }

}
