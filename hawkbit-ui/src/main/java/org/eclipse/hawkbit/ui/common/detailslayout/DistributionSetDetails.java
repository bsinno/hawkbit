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
import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.SoftwareModuleManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
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
 * Code for Distribution Set Details
 *
 */
public class DistributionSetDetails extends AbstractGridDetailsLayout<ProxyDistributionSet> {
    private static final long serialVersionUID = 1L;

    private final MetadataDetailsGrid<Long> dsMetadataGrid;
    private final SoftwareModuleDetailsGrid smDetailsGrid;
    private TargetFilterQueryDetailsGrid tfqDetailsGrid;

    private final transient DsMetaDataWindowBuilder dsMetaDataWindowBuilder;

    private final transient TenantConfigurationManagement tenantConfigurationManagement;
    private final transient SystemSecurityContext systemSecurityContext;

    private final transient DistributionTagToken distributionTagToken;

    public DistributionSetDetails(final VaadinMessageSource i18n, final UIEventBus eventBus,
            final SpPermissionChecker permissionChecker, final UINotification uiNotification,
            final DistributionSetManagement dsManagement, final SoftwareModuleManagement smManagement,
            final DistributionSetTypeManagement dsTypeManagement, final DistributionSetTagManagement dsTagManagement,
            final TenantConfigurationManagement tenantConfigurationManagement,
            final SystemSecurityContext systemSecurityContext, final DsMetaDataWindowBuilder dsMetaDataWindowBuilder) {
        super(i18n);

        this.tenantConfigurationManagement = tenantConfigurationManagement;
        this.systemSecurityContext = systemSecurityContext;
        this.dsMetaDataWindowBuilder = dsMetaDataWindowBuilder;

        this.smDetailsGrid = new SoftwareModuleDetailsGrid(i18n, eventBus, uiNotification, permissionChecker,
                dsManagement, smManagement, dsTypeManagement);

        this.distributionTagToken = new DistributionTagToken(permissionChecker, i18n, uiNotification, eventBus,
                dsTagManagement, dsManagement);

        this.dsMetadataGrid = new MetadataDetailsGrid<>(i18n, eventBus, UIComponentIdProvider.DS_TYPE_PREFIX,
                this::showMetadataDetails, new DsMetaDataDataProvider(dsManagement));

        addDetailsComponents(Arrays.asList(new SimpleEntry<>(i18n.getMessage("caption.tab.details"), entityDetails),
                new SimpleEntry<>(i18n.getMessage("caption.tab.description"), entityDescription),
                new SimpleEntry<>(i18n.getMessage("caption.softwares.distdetail.tab"), smDetailsGrid),
                new SimpleEntry<>(i18n.getMessage("caption.tags.tab"), distributionTagToken.getTagPanel()),
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

    @Override
    public void masterEntityChanged(final ProxyDistributionSet entity) {
        super.masterEntityChanged(entity);

        // TODO: consider populating the grid only when metadata tab is/becomes
        // active (lazy loading)
        dsMetadataGrid.masterEntityChanged(entity != null ? entity.getId() : null);
        smDetailsGrid.masterEntityChanged(entity);
        distributionTagToken.masterEntityChanged(entity);
        if (tfqDetailsGrid != null) {
            tfqDetailsGrid.masterEntityChanged(entity != null ? entity.getId() : null);
        }
    }

    public void setUnassignSmAllowed(final boolean isUnassignSmAllowed) {
        smDetailsGrid.setUnassignSmAllowed(isUnassignSmAllowed);
    }

    public void addTfqDetailsGrid(final TargetFilterQueryManagement targetFilterQueryManagement) {
        if (tfqDetailsGrid == null) {
            tfqDetailsGrid = new TargetFilterQueryDetailsGrid(i18n, targetFilterQueryManagement);

            addDetailsComponents(Collections
                    .singletonList(new SimpleEntry<>(i18n.getMessage("caption.auto.assignment.ds"), tfqDetailsGrid)));
        }
    }

    public DistributionTagToken getDistributionTagToken() {
        return distributionTagToken;
    }
}
