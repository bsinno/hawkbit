/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.common.data.providers;

import java.util.Collection;

import org.eclipse.hawkbit.repository.FilterParams;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.ui.common.data.filters.TargetManagementFilterParams;
import org.eclipse.hawkbit.ui.common.data.mappers.TargetToProxyTargetMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.event.EventLayout;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.event.GridSizeExceedanceEventPayload;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Data provider for {@link Target}, which dynamically loads a batch of
 * {@link Target} entities from backend and maps them to corresponding
 * {@link ProxyTarget} entities.
 */
public class TargetManagementStateDataProvider
        extends AbstractProxyDataProvider<ProxyTarget, Target, TargetManagementFilterParams> {

    private static final long serialVersionUID = 1L;

    private final transient TargetManagement targetManagement;
    private final transient UIEventBus eventBus;

    private final long maxEntityCount;
    private final EventView eventView;

    /**
     * Constructor for TargetManagementStateDataProvider
     * 
     * @param targetManagement
     *            TargetManagement
     * @param entityMapper
     *            TargetToProxyTargetMapper
     * @param maxEntityCount
     *            Max number of targets that can be fetched by this Provider
     * @param eventBus
     *            to send grid size exceedance event
     * @param eventView
     *            {@link EventView} the provider is used for
     */
    public TargetManagementStateDataProvider(final TargetManagement targetManagement,
            final TargetToProxyTargetMapper entityMapper, final long maxEntityCount, final UIEventBus eventBus,
            final EventView eventView) {
        super(entityMapper, Sort.by(Direction.DESC, "lastModifiedAt"));

        this.targetManagement = targetManagement;
        this.maxEntityCount = maxEntityCount;
        this.eventBus = eventBus;
        this.eventView = eventView;
    }

    @Override
    protected Slice<Target> loadBackendEntities(final PageRequest pageRequest,
            final TargetManagementFilterParams filter) {
        if (filter == null) {
            return targetManagement.findAll(pageRequest);
        }

        final Long pinnedDistId = filter.getPinnedDistId();
        final String searchText = filter.getSearchText();
        final Collection<TargetUpdateStatus> targetUpdateStatusList = filter.getTargetUpdateStatusList();
        final boolean overdueState = filter.isOverdueState();
        final Long distributionId = filter.getDistributionId();
        final boolean noTagClicked = filter.isNoTagClicked();
        final String[] targetTags = filter.getTargetTags().toArray(new String[0]);
        final Long targetFilterQueryId = filter.getTargetFilterQueryId();

        if (pinnedDistId != null) {
            return targetManagement.findByFilterOrderByLinkedDistributionSet(pageRequest, pinnedDistId,
                    new FilterParams(targetUpdateStatusList, overdueState, searchText, distributionId, noTagClicked,
                            targetTags));
        }

        if (filter.isAnyFilterSelected()) {
            if (targetFilterQueryId != null) {
                return targetManagement.findByTargetFilterQuery(pageRequest, targetFilterQueryId);
            }

            return targetManagement.findByFilters(pageRequest, new FilterParams(targetUpdateStatusList, overdueState,
                    searchText, distributionId, noTagClicked, targetTags));
        }

        return targetManagement.findAll(pageRequest);
    }

    @Override
    protected long sizeInBackEnd(final PageRequest pageRequest, final TargetManagementFilterParams filter) {
        long size;
        if (filter == null) {
            size = targetManagement.count();
        } else {
            final String searchText = filter.getSearchText();
            final Collection<TargetUpdateStatus> targetUpdateStatusList = filter.getTargetUpdateStatusList();
            final boolean overdueState = filter.isOverdueState();
            final Long distributionId = filter.getDistributionId();
            final boolean noTagClicked = filter.isNoTagClicked();
            final String[] targetTags = filter.getTargetTags().toArray(new String[0]);
            final Long targetFilterQueryId = filter.getTargetFilterQueryId();

            if (filter.isAnyFilterSelected()) {
                if (targetFilterQueryId != null) {
                    size = targetManagement.countByTargetFilterQuery(targetFilterQueryId);
                } else {
                    size = targetManagement.countByFilters(targetUpdateStatusList, overdueState, searchText,
                            distributionId, noTagClicked, targetTags);
                }
            } else {
                size = targetManagement.count();
            }
        }

        publishGridSizeExceedanceEvent(size > maxEntityCount);
        return Math.min(size, maxEntityCount);
    }

    private void publishGridSizeExceedanceEvent(final boolean isSizeLimitExceeded) {
        eventBus.publish(EventTopics.GRID_SIZE_EXCEEDANCE_CHANGED, this,
                new GridSizeExceedanceEventPayload(EventLayout.TARGET_LIST, eventView, isSizeLimitExceeded));
    }
}
