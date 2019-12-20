package org.eclipse.hawkbit.ui.tenantconfiguration.window;

import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.model.TenantMetaData;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetTypeDataProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;

public class SystemConfigWindowDependencies {

    private final SystemManagement systemManagement;
    private final VaadinMessageSource i18n;
    private final SpPermissionChecker permissionChecker;
    private final DistributionSetTypeManagement distributionSetTypeManagement;
    private final DistributionSetTypeDataProvider distributionSetTypeDataProvider;
    private final TenantMetaData tenantMetaData;

    public SystemConfigWindowDependencies(final SystemManagement systemManagement, final VaadinMessageSource i18n,
            final SpPermissionChecker permissionChecker,
            final DistributionSetTypeManagement distributionSetTypeManagement,
            final DistributionSetTypeDataProvider distributionSetTypeDataProvider,
            final TenantMetaData tenantMetaData) {
        this.systemManagement = systemManagement;
        this.i18n = i18n;
        this.permissionChecker = permissionChecker;
        this.distributionSetTypeManagement = distributionSetTypeManagement;
        this.distributionSetTypeDataProvider = distributionSetTypeDataProvider;
        this.tenantMetaData = tenantMetaData;
    }

    public SystemManagement getSystemManagement() {
        return systemManagement;
    }

    public VaadinMessageSource getI18n() {
        return i18n;
    }

    public SpPermissionChecker getPermissionChecker() {
        return permissionChecker;
    }

    public DistributionSetTypeManagement getDistributionSetTypeManagement() {
        return distributionSetTypeManagement;
    }

    public DistributionSetTypeDataProvider getDistributionSetTypeDataProvider() {
        return distributionSetTypeDataProvider;
    }

    public TenantMetaData getTenantMetaData() {
        return tenantMetaData;
    }
}
