/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag.filter;

import java.util.Collections;
import java.util.Map;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetTag;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TagToProxyTagMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetTagDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyDistributionSet;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyIdentifiableEntity;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.EventView;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractTagFilterButtons;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.DistributionSetsToTagAssignmentSupport;
import org.eclipse.hawkbit.ui.management.dstag.DsTagWindowBuilder;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.ui.Window;

/**
 * Class for defining the tag buttons of the distribution sets on the Deployment
 * View.
 */
public class DistributionTagButtons extends AbstractTagFilterButtons {
    private static final long serialVersionUID = 1L;

    private final DistributionTagLayoutUiState distributionTagLayoutUiState;

    private final transient DistributionSetTagManagement distributionSetTagManagement;
    private final transient DsTagWindowBuilder dsTagWindowBuilder;

    private final ConfigurableFilterDataProvider<ProxyTag, Void, Void> dsTagDataProvider;
    private final transient TagToProxyTagMapper<DistributionSetTag> dsTagMapper;

    private final transient DragAndDropSupport<ProxyTag> dragAndDropSupport;

    public DistributionTagButtons(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final UINotification uiNotification, final SpPermissionChecker permChecker,
            final DistributionSetTagManagement distributionSetTagManagement,
            final DistributionSetManagement distributionSetManagement, final DsTagWindowBuilder dsTagWindowBuilder,
            final DistributionTagLayoutUiState distributionTagLayoutUiState) {
        super(eventBus, i18n, uiNotification, permChecker);

        this.distributionTagLayoutUiState = distributionTagLayoutUiState;
        this.distributionSetTagManagement = distributionSetTagManagement;
        this.dsTagWindowBuilder = dsTagWindowBuilder;

        this.dsTagMapper = new TagToProxyTagMapper<>();
        this.dsTagDataProvider = new DistributionSetTagDataProvider(distributionSetTagManagement, dsTagMapper)
                .withConfigurableFilter();

        final DistributionSetsToTagAssignmentSupport distributionSetsToTagAssignment = new DistributionSetsToTagAssignmentSupport(
                uiNotification, i18n, distributionSetManagement, eventBus, permChecker, distributionTagLayoutUiState);

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, uiNotification,
                Collections.singletonMap(UIComponentIdProvider.DIST_TABLE_ID, distributionSetsToTagAssignment),
                eventBus);
        this.dragAndDropSupport.ignoreSelection(true);
        this.dragAndDropSupport.addDragAndDrop();

        init();
    }

    @Override
    public String getGridId() {
        return UIComponentIdProvider.DISTRIBUTION_TAG_TABLE_ID;
    }

    @Override
    public ConfigurableFilterDataProvider<ProxyTag, Void, Void> getFilterDataProvider() {
        return dsTagDataProvider;
    }

    @Override
    protected String getFilterButtonsType() {
        return i18n.getMessage(UIMessageIdProvider.CAPTION_DISTRIBUTION_TAG);
    }

    @Override
    protected String getFilterButtonIdPrefix() {
        return UIComponentIdProvider.DISTRIBUTION_TAG_ID_PREFIXS;
    }

    @Override
    protected Class<? extends ProxyIdentifiableEntity> getFilterMasterEntityType() {
        return ProxyDistributionSet.class;
    }

    @Override
    protected EventView getView() {
        return EventView.DEPLOYMENT;
    }

    @Override
    protected void updateClickedTagsUiState(final Map<Long, String> activeTagIdsWithName) {
        distributionTagLayoutUiState.setClickedDsTagIdsWithName(activeTagIdsWithName);
    }

    @Override
    protected void updateClickedNoTagUiState(final boolean isNoTagActivated) {
        distributionTagLayoutUiState.setNoTagClicked(isNoTagActivated);
    }

    @Override
    protected Map<Long, String> getClickedTagIdsWithNameFromUiState() {
        return distributionTagLayoutUiState.getClickedDsTagIdsWithName();
    }

    @Override
    protected boolean getClickedNoTagFromUiState() {
        return distributionTagLayoutUiState.isNoTagClicked();
    }

    @Override
    protected void deleteTag(final ProxyTag tagToDelete) {
        distributionSetTagManagement.delete(tagToDelete.getName());
    }

    @Override
    protected boolean isDeletionAllowed() {
        return permissionChecker.hasDeleteRepositoryPermission();
    }

    @Override
    protected Window getUpdateWindow(final ProxyTag clickedFilter) {
        return dsTagWindowBuilder.getWindowForUpdateDsTag(clickedFilter);
    }
}
