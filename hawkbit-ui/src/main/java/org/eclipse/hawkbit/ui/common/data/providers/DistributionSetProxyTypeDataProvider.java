package org.eclipse.hawkbit.ui.common.data.providers;

import java.util.Optional;

import org.eclipse.hawkbit.repository.DistributionSetTypeManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.ui.common.data.mappers.DistributionSetTypeToProxyDistributionSetTypeMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSetType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

public class DistributionSetProxyTypeDataProvider extends ProxyDataProvider<ProxyDistributionSetType, DistributionSetType, String> {
    private static final long serialVersionUID = 1L;

    private final transient DistributionSetTypeManagement distributionSetTypeManagement;

    public DistributionSetProxyTypeDataProvider(final DistributionSetTypeManagement distributionSetTypeManagement,
            final DistributionSetTypeToProxyDistributionSetTypeMapper mapper) {
        super(mapper, new Sort(Sort.Direction.ASC, "name"));
        this.distributionSetTypeManagement = distributionSetTypeManagement;

    }

    @Override
    protected Optional<Slice<DistributionSetType>> loadBackendEntities(PageRequest pageRequest,
            Optional<String> filter) {
        return Optional.of(distributionSetTypeManagement.findAll(pageRequest));
    }

    @Override
    protected long sizeInBackEnd(PageRequest pageRequest, Optional<String> filter) {
        return distributionSetTypeManagement.count();
    }
}
