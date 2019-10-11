/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.detailslayout;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetMetadata;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyKeyValueDetails;
import org.eclipse.hawkbit.ui.common.tagdetails.DistributionTagToken;
import org.eclipse.hawkbit.ui.distributions.dstable.DsMetadataPopupLayout;
import org.eclipse.hawkbit.ui.management.event.DistributionTableEvent;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.data.domain.PageRequest;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.ui.UI;

/**
 * Abstract class which contains common code for Distribution Set Details
 *
 */
public abstract class AbstractDistributionSetDetails extends AbstractGridDetailsLayout<ProxyDistributionSet> {
    private static final long serialVersionUID = 1L;

    protected final UINotification uiNotification;
    protected final transient DistributionSetManagement distributionSetManagement;

    private final transient EntityFactory entityFactory;
    private final transient TenantConfigurationManagement tenantConfigurationManagement;
    private final transient SystemSecurityContext systemSecurityContext;

    private final DistributionTagToken distributionTagToken;
    private final MetadataDetailsLayout<ProxyDistributionSet> dsMetadataLayout;

    protected AbstractDistributionSetDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final DistributionSetManagement distributionSetManagement,
            final UINotification uiNotification, final DistributionSetTagManagement distributionSetTagManagement,
            final TenantConfigurationManagement tenantConfigurationManagement,
            final SystemSecurityContext systemSecurityContext, final EntityFactory entityFactory) {
        super(i18n, permissionChecker, eventBus);

        this.uiNotification = uiNotification;
        this.entityFactory = entityFactory;
        this.distributionSetManagement = distributionSetManagement;
        this.tenantConfigurationManagement = tenantConfigurationManagement;
        this.systemSecurityContext = systemSecurityContext;

        this.distributionTagToken = new DistributionTagToken(permissionChecker, i18n, uiNotification, eventBus,
                distributionSetTagManagement, distributionSetManagement);
        binder.forField(distributionTagToken).bind(ds -> ds, null);

        this.dsMetadataLayout = new MetadataDetailsLayout<>(i18n, UIComponentIdProvider.DS_METADATA_DETAIL_LINK,
                this::showMetadataDetails, this::getDsMetaData);
        binder.forField(dsMetadataLayout).bind(ds -> ds, null);

        addDetailsComponents(Arrays.asList(new SimpleEntry<>(i18n.getMessage("caption.tab.details"), entityDetails),
                new SimpleEntry<>(i18n.getMessage("caption.tab.description"), entityDescription),
                new SimpleEntry<>(i18n.getMessage("caption.softwares.distdetail.tab"), getSoftwareModuleDetailsGrid()),
                new SimpleEntry<>(i18n.getMessage("caption.tags.tab"), distributionTagToken),
                new SimpleEntry<>(i18n.getMessage("caption.logs.tab"), logDetails),
                new SimpleEntry<>(i18n.getMessage("caption.metadata"), dsMetadataLayout)));
    }

    @Override
    protected String getTabSheetId() {
        return UIComponentIdProvider.DISTRIBUTIONSET_DETAILS_TABSHEET_ID;
    }

    @Override
    protected List<ProxyKeyValueDetails> getEntityDetails(final ProxyDistributionSet entity) {
        final ProxyKeyValueDetails typeLabel = new ProxyKeyValueDetails(UIComponentIdProvider.DETAILS_TYPE_LABEL_ID,
                i18n.getMessage("label.dist.details.type"), entity.getType().getName());

        if (isMultiAssignmentEnabled()) {
            return Collections.singletonList(typeLabel);
        } else {
            return Arrays.asList(typeLabel,
                    new ProxyKeyValueDetails(UIComponentIdProvider.DETAILS_REQUIRED_MIGRATION_STEP_LABEL_ID,
                            i18n.getMessage("checkbox.dist.migration.required"),
                            getMigrationRequiredValue(entity.isRequiredMigrationStep())));
        }
    }

    private boolean isMultiAssignmentEnabled() {
        return systemSecurityContext.runAsSystem(() -> tenantConfigurationManagement
                .getConfigurationValue(TenantConfigurationKey.MULTI_ASSIGNMENTS_ENABLED, Boolean.class).getValue());
    }

    private String getMigrationRequiredValue(final Boolean isMigrationRequired) {
        if (isMigrationRequired == null) {
            return "";
        }
        return isMigrationRequired.equals(Boolean.TRUE) ? i18n.getMessage("label.yes") : i18n.getMessage("label.no");
    }

    @Override
    protected String getDetailsDescriptionId() {
        return UIComponentIdProvider.DS_DETAILS_DESCRIPTION_ID;
    }

    @Override
    protected String getLogLabelIdPrefix() {
        // TODO: fix with constant
        return "ds.";
    }

    private void showMetadataDetails(final String metadataKey) {
        // TODO: adapt after popup refactoring
        distributionSetManagement.get(binder.getBean().getId()).ifPresent(ds -> {
            final DsMetadataPopupLayout dsMetadataPopupLayout = new DsMetadataPopupLayout(i18n, uiNotification,
                    eventBus, distributionSetManagement, entityFactory, permChecker);
            UI.getCurrent().addWindow(dsMetadataPopupLayout.getWindow(ds, metadataKey));
        });
    }

    private List<DistributionSetMetadata> getDsMetaData(final ProxyDistributionSet ds) {
        return distributionSetManagement
                .findMetaDataByDistributionSetId(PageRequest.of(0, MetadataDetailsGrid.MAX_METADATA_QUERY), ds.getId())
                .getContent();
    }

    protected abstract SoftwareModuleDetailsGrid getSoftwareModuleDetailsGrid();

    // TODO: implement
    // protected void populateSmDetails() {
    // softwareModuleDetailsGrid.populateGrid(getSelectedBaseEntity());
    // }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final DistributionTableEvent distributionTableEvent) {
        onBaseEntityEvent(distributionTableEvent);
    }
}
