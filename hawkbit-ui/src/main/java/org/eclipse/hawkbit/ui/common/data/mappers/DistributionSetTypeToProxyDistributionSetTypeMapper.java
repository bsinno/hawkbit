package org.eclipse.hawkbit.ui.common.data.mappers;

import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSetType;

public class DistributionSetTypeToProxyDistributionSetTypeMapper
        extends AbstractNamedEntityToProxyNamedEntityMapper<ProxyDistributionSetType, DistributionSetType>{
    @Override
    public ProxyDistributionSetType map(final DistributionSetType distributionSetType) {
        ProxyDistributionSetType proxyDistributionSetType = new ProxyDistributionSetType();
        mapNamedEntityAttributes(distributionSetType, proxyDistributionSetType);

        proxyDistributionSetType.setDistSetTypeId(distributionSetType.getId());
        proxyDistributionSetType.setName(distributionSetType.getName());
        proxyDistributionSetType.setDescription(distributionSetType.getDescription());

        proxyDistributionSetType.setMandatoryModuleTypes(distributionSetType.getMandatoryModuleTypes());
        proxyDistributionSetType.setOptionalModuleTypes(distributionSetType.getOptionalModuleTypes());

        return proxyDistributionSetType;
    }
}
