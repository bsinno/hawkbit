/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettable;

import java.util.AbstractMap.SimpleEntry;
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
import org.eclipse.hawkbit.repository.model.TargetMetadata;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyKeyValueDetails;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTargetAttributesDetails;
import org.eclipse.hawkbit.ui.common.detailslayout.AbstractGridDetailsLayout;
import org.eclipse.hawkbit.ui.common.detailslayout.KeyValueDetailsComponent;
import org.eclipse.hawkbit.ui.common.detailslayout.MetadataDetailsGrid;
import org.eclipse.hawkbit.ui.common.detailslayout.MetadataDetailsLayout;
import org.eclipse.hawkbit.ui.common.tagdetails.TargetTagToken;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent;
import org.eclipse.hawkbit.ui.management.state.ManagementUIState;
import org.eclipse.hawkbit.ui.utils.SPDateTimeUtil;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

/**
 * Target details layout which is shown on the Deployment View.
 */
public class TargetDetails extends AbstractGridDetailsLayout<ProxyTarget> {
    private static final long serialVersionUID = 1L;

    private final ManagementUIState managementUIState;

    private final UINotification uiNotification;
    private final transient EntityFactory entityFactory;
    private final transient TargetManagement targetManagement;
    private final transient DeploymentManagement deploymentManagement;

    private final TargetAttributesDetailsComponent attributesLayout;
    private final KeyValueDetailsComponent assignedDsDetails;
    private final KeyValueDetailsComponent installedDsDetails;
    private final TargetTagToken targetTagToken;
    private final MetadataDetailsLayout<ProxyTarget> targetMetadataLayout;

    TargetDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final ManagementUIState managementUIState,
            final UINotification uiNotification, final TargetTagManagement tagManagement,
            final TargetManagement targetManagement, final DeploymentManagement deploymentManagement,
            final EntityFactory entityFactory) {
        super(i18n, permissionChecker, eventBus);

        this.managementUIState = managementUIState;
        this.uiNotification = uiNotification;
        this.targetManagement = targetManagement;
        this.deploymentManagement = deploymentManagement;
        this.entityFactory = entityFactory;

        this.attributesLayout = buildAttributesLayout();

        this.assignedDsDetails = buildAssignedDsDetails();

        this.installedDsDetails = buildInstalledDsDetails();

        this.targetTagToken = new TargetTagToken(permissionChecker, i18n, uiNotification, eventBus, managementUIState,
                tagManagement, targetManagement);
        binder.forField(targetTagToken).bind(target -> target, null);

        this.targetMetadataLayout = new MetadataDetailsLayout<>(i18n, UIComponentIdProvider.TARGET_METADATA_DETAIL_LINK,
                this::showMetadataDetails, this::getTargetMetaData);
        binder.forField(targetMetadataLayout).bind(target -> target, null);

        addDetailsComponents(Arrays.asList(new SimpleEntry<>(i18n.getMessage("caption.tab.details"), entityDetails),
                new SimpleEntry<>(i18n.getMessage("caption.tab.description"), entityDescription),
                new SimpleEntry<>(i18n.getMessage("caption.attributes.tab"), attributesLayout),
                new SimpleEntry<>(i18n.getMessage("header.target.assigned"), assignedDsDetails),
                new SimpleEntry<>(i18n.getMessage("header.target.installed"), installedDsDetails),
                new SimpleEntry<>(i18n.getMessage("caption.tags.tab"), targetTagToken),
                new SimpleEntry<>(i18n.getMessage("caption.logs.tab"), logDetails),
                new SimpleEntry<>(i18n.getMessage("caption.metadata"), targetMetadataLayout)));

        buildDetails();
        restoreState();
    }

    @Override
    protected String getTabSheetId() {
        return UIComponentIdProvider.TARGET_DETAILS_TABSHEET;
    }

    @Override
    protected List<ProxyKeyValueDetails> getEntityDetails(final ProxyTarget entity) {
        return Arrays.asList(
                new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_CONTROLLER_ID, i18n.getMessage("label.target.id"),
                        entity.getControllerId()),
                new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_LAST_QUERY_DT,
                        i18n.getMessage("label.target.lastpolldate"),
                        SPDateTimeUtil.getFormattedDate(entity.getLastTargetQuery())),
                new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_IP_ADDRESS, i18n.getMessage("label.ip"),
                        entity.getAddress() != null ? entity.getAddress().toString() : ""),
                new ProxyKeyValueDetails(UIComponentIdProvider.TARGET_SECURITY_TOKEN,
                        i18n.getMessage("label.target.security.token"), entity.getSecurityToken()));
    }

    @Override
    protected String getDetailsDescriptionId() {
        return UIComponentIdProvider.TARGET_DETAILS_DESCRIPTION_ID;
    }

    private TargetAttributesDetailsComponent buildAttributesLayout() {
        final TargetAttributesDetailsComponent attributesDetails = new TargetAttributesDetailsComponent(i18n,
                targetManagement);

        binder.forField(attributesDetails).bind(target -> {
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

        return attributesDetails;
    }

    private KeyValueDetailsComponent buildAssignedDsDetails() {
        final KeyValueDetailsComponent assignedDsLayout = new KeyValueDetailsComponent();

        binder.forField(assignedDsLayout).bind(target -> {
            final Optional<DistributionSet> targetAssignedDs = deploymentManagement
                    .getAssignedDistributionSet(target.getControllerId());

            return targetAssignedDs.map(this::getDistributionDetails).orElse(null);
        }, null);

        return assignedDsLayout;
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

    private KeyValueDetailsComponent buildInstalledDsDetails() {
        final KeyValueDetailsComponent installedDsLayout = new KeyValueDetailsComponent();

        binder.forField(installedDsLayout).bind(target -> {
            final Optional<DistributionSet> targetInstalledDs = deploymentManagement
                    .getInstalledDistributionSet(target.getControllerId());

            return targetInstalledDs.map(this::getDistributionDetails).orElse(null);
        }, null);

        return installedDsLayout;
    }

    @Override
    protected String getLogLabelIdPrefix() {
        // TODO: fix with constant
        return "target.";
    }

    private void showMetadataDetails(final String metadataKey) {
        // TODO: adapt after popup refactoring
        targetManagement.get(binder.getBean().getId()).ifPresent(target -> {
            final TargetMetadataPopupLayout targetMetadataPopupLayout = new TargetMetadataPopupLayout(i18n,
                    uiNotification, eventBus, targetManagement, entityFactory, permChecker);
            UI.getCurrent().addWindow(targetMetadataPopupLayout.getWindow(target, metadataKey));
        });
    }

    private List<TargetMetadata> getTargetMetaData(final ProxyTarget target) {
        return targetManagement.findMetaDataByControllerId(PageRequest.of(0, MetadataDetailsGrid.MAX_METADATA_QUERY),
                target.getControllerId()).getContent();
    }

    // TODO: should we move it to parent?
    private void restoreState() {
        if (managementUIState.isTargetTableMaximized()) {
            setVisible(false);
        }
    }

    // TODO: implement
    // protected void populateMetaData() {
    // targetMetadataLayout.populateMetadata(entity);
    // }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final TargetTableEvent targetTableEvent) {
        onBaseEntityEvent(targetTableEvent);
    }
}
