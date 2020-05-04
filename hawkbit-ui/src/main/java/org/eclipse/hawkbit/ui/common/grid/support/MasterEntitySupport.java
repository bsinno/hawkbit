package org.eclipse.hawkbit.ui.common.grid.support;

import java.util.function.Function;

import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.event.FilterType;
import org.eclipse.hawkbit.ui.common.layout.MasterEntityAwareComponent;

public class MasterEntitySupport<M extends ProxyIdentifiableEntity> implements MasterEntityAwareComponent<M> {
    private final FilterSupport<?, ?> filterSupport;
    private final Function<M, ?> masterEntityToFilterMapper;

    private Long masterId;

    public MasterEntitySupport(final FilterSupport<?, ?> filterSupport) {
        this(filterSupport, null);
    }

    public MasterEntitySupport(final FilterSupport<?, ?> filterSupport,
            final Function<M, ?> masterEntityToFilterMapper) {
        this.filterSupport = filterSupport;
        this.masterEntityToFilterMapper = masterEntityToFilterMapper;
    }

    @Override
    public void masterEntityChanged(final M masterEntity) {
        if ((masterEntity == null && masterId == null) || !filterSupport.isFilterTypeSupported(FilterType.MASTER)) {
            return;
        }

        final Long masterEntityId = masterEntity != null ? masterEntity.getId() : null;
        masterId = masterEntityId;

        if (masterEntity != null) {
            filterSupport.updateFilter(FilterType.MASTER,
                    masterEntityToFilterMapper != null ? masterEntityToFilterMapper.apply(masterEntity)
                            : masterEntityId);
        } else {
            filterSupport.updateFilter(FilterType.MASTER, null);
        }
    }

    public Long getMasterId() {
        return masterId;
    }
}
