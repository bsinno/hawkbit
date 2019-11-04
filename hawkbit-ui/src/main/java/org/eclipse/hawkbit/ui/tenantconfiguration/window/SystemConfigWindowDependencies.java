package org.eclipse.hawkbit.ui.tenantconfiguration.window;

import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.model.TenantMetaData;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetProxyTypeDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyType;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus;

import com.vaadin.data.provider.DataProvider;

public class SystemConfigWindowDependencies {

    private final SystemManagement systemManagement;
    private final VaadinMessageSource i18n;
    private final SpPermissionChecker permissionChecker;
    private final DistributionSetTypeManagement distributionSetTypeManagement;
    private final DistributionSetProxyTypeDataProvider distributionSetTypeDataProvider;
    private TenantMetaData tenantMetaData;

    public SystemConfigWindowDependencies(SystemManagement systemManagement, VaadinMessageSource i18n,
            SpPermissionChecker permissionChecker, DistributionSetTypeManagement distributionSetTypeManagement,
            DistributionSetProxyTypeDataProvider distributionSetTypeDataProvider, TenantMetaData tenantMetaData) {
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

    public DistributionSetProxyTypeDataProvider getDistributionSetTypeDataProvider() {
       return distributionSetTypeDataProvider;
    }
    public TenantMetaData getTenantMetaData() {
        return tenantMetaData;
    }
}
