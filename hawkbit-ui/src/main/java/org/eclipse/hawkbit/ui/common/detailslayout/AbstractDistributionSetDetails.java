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
import org.eclipse.hawkbit.repository.TenantConfigurationManagement;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.tenancy.configuration.TenantConfigurationProperties.TenantConfigurationKey;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.providers.DsMetaDataDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyKeyValueDetails;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyMetaData;
import org.eclipse.hawkbit.ui.common.tagdetails.DistributionTagToken;
import org.eclipse.hawkbit.ui.distributions.dstable.DsMetaDataWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Abstract class which contains common code for Distribution Set Details
 *
 */
public abstract class AbstractDistributionSetDetails extends AbstractGridDetailsLayout<ProxyDistributionSet> {
    private static final long serialVersionUID = 1L;

    private final MetadataDetailsGrid<Long> dsMetadataGrid;

    private final transient DsMetaDataWindowBuilder dsMetaDataWindowBuilder;

    protected final transient UIEventBus eventBus;
    protected final SpPermissionChecker permissionChecker;
    protected final UINotification uiNotification;
    protected final transient DistributionSetManagement distributionSetManagement;

    private final transient TenantConfigurationManagement tenantConfigurationManagement;
    private final transient SystemSecurityContext systemSecurityContext;

    private final DistributionTagToken distributionTagToken;

    protected AbstractDistributionSetDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final DistributionSetManagement distributionSetManagement,
            final UINotification uiNotification, final DistributionSetTagManagement distributionSetTagManagement,
            final TenantConfigurationManagement tenantConfigurationManagement,
            final SystemSecurityContext systemSecurityContext, final DsMetaDataWindowBuilder dsMetaDataWindowBuilder) {
        super(i18n);

        this.eventBus = eventBus;
        this.permissionChecker = permissionChecker;
        this.uiNotification = uiNotification;
        this.distributionSetManagement = distributionSetManagement;
        this.tenantConfigurationManagement = tenantConfigurationManagement;
        this.systemSecurityContext = systemSecurityContext;
        this.dsMetaDataWindowBuilder = dsMetaDataWindowBuilder;

        this.distributionTagToken = new DistributionTagToken(permissionChecker, i18n, uiNotification, eventBus,
                distributionSetTagManagement, distributionSetManagement);
        binder.forField(distributionTagToken).bind(ds -> ds, null);

        this.dsMetadataGrid = new MetadataDetailsGrid<>(i18n, eventBus, UIComponentIdProvider.DS_METADATA_DETAIL_LINK,
                this::showMetadataDetails, new DsMetaDataDataProvider(distributionSetManagement));
        this.dsMetadataGrid.setVisible(false);

        addDetailsComponents(Arrays.asList(new SimpleEntry<>(i18n.getMessage("caption.tab.details"), entityDetails),
                new SimpleEntry<>(i18n.getMessage("caption.tab.description"), entityDescription),
                new SimpleEntry<>(i18n.getMessage("caption.softwares.distdetail.tab"), getSoftwareModuleDetailsGrid()),
                new SimpleEntry<>(i18n.getMessage("caption.tags.tab"), distributionTagToken),
                new SimpleEntry<>(i18n.getMessage("caption.logs.tab"), logDetails),
                new SimpleEntry<>(i18n.getMessage("caption.metadata"), dsMetadataGrid)));
    }

    @Override
    protected String getTabSheetId() {
        return UIComponentIdProvider.DISTRIBUTIONSET_DETAILS_TABSHEET_ID;
    }

    @Override
    protected List<ProxyKeyValueDetails> getEntityDetails(final ProxyDistributionSet entity) {
        final ProxyKeyValueDetails typeLabel = new ProxyKeyValueDetails(UIComponentIdProvider.DETAILS_TYPE_LABEL_ID,
                i18n.getMessage("label.dist.details.type"), entity.getProxyType().getName());

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

    private void showMetadataDetails(final ProxyMetaData metadata) {
        if (binder.getBean() == null) {
            return;
        }

        final Window metaDataWindow = dsMetaDataWindowBuilder.getWindowForShowDsMetaData(binder.getBean().getId(),
                metadata);

        metaDataWindow.setCaption(i18n.getMessage("caption.metadata.popup") + binder.getBean().getNameVersion());
        UI.getCurrent().addWindow(metaDataWindow);
        metaDataWindow.setVisible(Boolean.TRUE);
    }

    protected abstract SoftwareModuleDetailsGrid getSoftwareModuleDetailsGrid();

    // TODO: implement
    // protected void populateSmDetails() {
    // softwareModuleDetailsGrid.populateGrid(getSelectedBaseEntity());
    // }

    @Override
    public void masterEntityChanged(final ProxyDistributionSet entity) {
        super.masterEntityChanged(entity);

        // TODO: consider populating the grid only when metadata tab is/becomes
        // active (lazy loading)
        if (entity == null) {
            dsMetadataGrid.updateMasterEntityFilter(null);
            dsMetadataGrid.setVisible(false);
        } else {
            dsMetadataGrid.updateMasterEntityFilter(entity.getId());
            dsMetadataGrid.setVisible(true);
        }
    }
}
