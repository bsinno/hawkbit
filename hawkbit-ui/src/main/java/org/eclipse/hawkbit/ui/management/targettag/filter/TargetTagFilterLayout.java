/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.targettag.filter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TargetTagManagement;
import org.eclipse.hawkbit.repository.model.TargetTag;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TagToProxyTagMapper;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterLayout;
import org.eclipse.hawkbit.ui.management.ManagementUIState;
import org.eclipse.hawkbit.ui.management.targettag.TargetTagWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.ComponentContainer;

/**
 * Target Tag filter layout.
 */
public class TargetTagFilterLayout extends AbstractFilterLayout {
    private static final long serialVersionUID = 1L;

    private final transient TargetTagManagement targetTagManagement;
    private final transient TagToProxyTagMapper<TargetTag> targetTagMapper;

    private final TargetTagFilterHeader targetTagFilterHeader;
    private final MultipleTargetFilter multipleTargetFilter;

    private final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState;

    private final transient TargetTagFilterLayoutEventListener eventListener;

    /**
     * Constructor
     * 
     * @param i18n
     *            VaadinMessageSource
     * @param managementUIState
     *            ManagementUIState
     * @param permChecker
     *            SpPermissionChecker
     * @param eventBus
     *            UIEventBus
     * @param notification
     *            UINotification
     * @param entityFactory
     *            EntityFactory
     * @param targetFilterQueryManagement
     *            TargetFilterQueryManagement
     * @param targetTagManagement
     *            TargetTagManagement
     */
    public TargetTagFilterLayout(final VaadinMessageSource i18n, final ManagementUIState managementUIState,
            final SpPermissionChecker permChecker, final UIEventBus eventBus, final UINotification notification,
            final EntityFactory entityFactory, final TargetFilterQueryManagement targetFilterQueryManagement,
            final TargetTagManagement targetTagManagement, final TargetManagement targetManagement,
            final TargetTagFilterLayoutUiState targetTagFilterLayoutUiState) {
        this.targetTagManagement = targetTagManagement;
        this.targetTagMapper = new TagToProxyTagMapper<>();
        this.targetTagFilterLayoutUiState = targetTagFilterLayoutUiState;

        final TargetTagWindowBuilder targetTagWindowBuilder = new TargetTagWindowBuilder(i18n, entityFactory, eventBus,
                notification, targetTagManagement);

        this.targetTagFilterHeader = new TargetTagFilterHeader(i18n, managementUIState, permChecker, eventBus,
                targetTagWindowBuilder);
        this.multipleTargetFilter = new MultipleTargetFilter(permChecker, managementUIState, i18n, eventBus,
                notification, targetFilterQueryManagement, targetTagManagement, targetManagement,
                targetTagWindowBuilder);

        this.eventListener = new TargetTagFilterLayoutEventListener(this, eventBus);

        buildLayout();
    }

    @Override
    protected TargetTagFilterHeader getFilterHeader() {
        return targetTagFilterHeader;
    }

    @Override
    protected ComponentContainer getFilterContent() {
        return multipleTargetFilter;
    }

    public void restoreState() {
        final List<Long> lastClickedTagIds = targetTagFilterLayoutUiState.getClickedTargetTagIds();

        if (!CollectionUtils.isEmpty(lastClickedTagIds)) {
            mapIdsToProxyEntities(lastClickedTagIds)
                    .forEach(multipleTargetFilter.getTargetTagFilterButtons()::selectFilter);
        }
    }

    // TODO: extract to parent abstract #mapIdsToProxyEntities?
    private List<ProxyTag> mapIdsToProxyEntities(final Collection<Long> entityIds) {
        return targetTagManagement.get(entityIds).stream().map(targetTagMapper::map).collect(Collectors.toList());
    }

    public void showFilterButtonsEditIcon() {
        multipleTargetFilter.getTargetTagFilterButtons().showEditColumn();
    }

    public void showFilterButtonsDeleteIcon() {
        multipleTargetFilter.getTargetTagFilterButtons().showDeleteColumn();
    }

    public void hideFilterButtonsActionIcons() {
        multipleTargetFilter.getTargetTagFilterButtons().hideActionColumns();
    }

    public void refreshFilterButtons() {
        multipleTargetFilter.getTargetTagFilterButtons().refreshContainer();
    }

    public void unsubscribeListener() {
        eventListener.unsubscribeListeners();
    }
}
