/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.model.DistributionSetTag;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.common.data.mappers.TagToProxyTagMapper;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetTagDataProvider;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTag;
import org.eclipse.hawkbit.ui.common.event.DsTagModifiedEventPayload;
import org.eclipse.hawkbit.ui.common.event.EntityModifiedEventPayload.EntityModifiedEventType;
import org.eclipse.hawkbit.ui.common.event.EventTopics;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtonClickBehaviour.ClickBehaviourType;
import org.eclipse.hawkbit.ui.common.filterlayout.AbstractFilterButtons;
import org.eclipse.hawkbit.ui.common.grid.support.DragAndDropSupport;
import org.eclipse.hawkbit.ui.common.grid.support.assignment.DistributionSetsToTagAssignmentSupport;
import org.eclipse.hawkbit.ui.management.dstag.DsTagWindowBuilder;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.UIMessageIdProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.CollectionUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * Class for defining the tag buttons of the distribution sets on the Deployment
 * View.
 */
public class DistributionTagButtons extends AbstractFilterButtons<ProxyTag, Void> {

    private static final long serialVersionUID = 1L;

    private final DistributionTagLayoutUiState distributionTagLayoutUiState;
    private final UINotification uiNotification;

    private final transient DistributionSetTagManagement distributionSetTagManagement;
    private final transient DistributionTagButtonClick distributionTagButtonClickBehaviour;
    private final transient DsTagWindowBuilder dsTagWindowBuilder;

    private final ConfigurableFilterDataProvider<ProxyTag, Void, Void> dsTagDataProvider;

    private final transient DragAndDropSupport<ProxyTag> dragAndDropSupport;

    public DistributionTagButtons(final UIEventBus eventBus, final VaadinMessageSource i18n,
            final UINotification uiNotification, final SpPermissionChecker permChecker,
            final DistributionSetTagManagement distributionSetTagManagement,
            final DistributionSetManagement distributionSetManagement, final DsTagWindowBuilder dsTagWindowBuilder,
            final DistributionTagLayoutUiState distributionTagLayoutUiState) {
        super(eventBus, i18n, uiNotification, permChecker);

        this.distributionTagLayoutUiState = distributionTagLayoutUiState;
        this.uiNotification = uiNotification;
        this.distributionSetTagManagement = distributionSetTagManagement;
        this.dsTagWindowBuilder = dsTagWindowBuilder;

        this.distributionTagButtonClickBehaviour = new DistributionTagButtonClick(this::publishFilterChangedEvent,
                this::publishNoTagChangedEvent);
        this.dsTagDataProvider = new DistributionSetTagDataProvider(distributionSetTagManagement,
                new TagToProxyTagMapper<DistributionSetTag>()).withConfigurableFilter();

        final DistributionSetsToTagAssignmentSupport distributionSetsToTagAssignment = new DistributionSetsToTagAssignmentSupport(
                uiNotification, i18n, distributionSetManagement, eventBus, permChecker, distributionTagLayoutUiState);

        this.dragAndDropSupport = new DragAndDropSupport<>(this, i18n, uiNotification,
                Collections.singletonMap(UIComponentIdProvider.DIST_TABLE_ID, distributionSetsToTagAssignment));
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
    protected AbstractFilterButtonClickBehaviour<ProxyTag> getFilterButtonClickBehaviour() {
        return distributionTagButtonClickBehaviour;
    }

    private void publishFilterChangedEvent(final Map<Long, String> activeTagIdsWithName) {
        // TODO: somehow move it to abstract class/TypeFilterButtonClick
        // needed to trigger style generator
        getDataCommunicator().reset();

        eventBus.publish(EventTopics.TAG_FILTER_CHANGED, this, activeTagIdsWithName.values());

        distributionTagLayoutUiState.setClickedDsTagIds(activeTagIdsWithName.keySet());
    }

    private void publishNoTagChangedEvent(final ClickBehaviourType clickType) {
        // TODO: add gray styling to NO_TAG Button

        eventBus.publish(EventTopics.NO_TAG_FILTER_CHANGED, this, ClickBehaviourType.CLICKED == clickType);

        distributionTagLayoutUiState.setNoTagClicked(ClickBehaviourType.CLICKED == clickType);
    }

    @Override
    protected void deleteFilterButtons(final Collection<ProxyTag> filterButtonsToDelete) {
        // TODO: we do not allow multiple deletion yet
        final ProxyTag dsTagToDelete = filterButtonsToDelete.iterator().next();
        final String dsTagToDeleteName = dsTagToDelete.getName();
        final Long dsTagToDeleteId = dsTagToDelete.getId();

        final Set<Long> clickedDsTagIds = distributionTagLayoutUiState.getClickedDsTagIds();

        if (!CollectionUtils.isEmpty(clickedDsTagIds) && clickedDsTagIds.contains(dsTagToDeleteId)) {
            uiNotification.displayValidationError(i18n.getMessage("message.tag.delete", dsTagToDeleteName));
        } else {
            distributionSetTagManagement.delete(dsTagToDeleteName);

            eventBus.publish(EventTopics.ENTITY_MODIFIED, this,
                    new DsTagModifiedEventPayload(EntityModifiedEventType.ENTITY_REMOVED, dsTagToDeleteId));
        }
    }

    @Override
    protected void editButtonClickListener(final ProxyTag clickedFilter) {
        final Window updateWindow = dsTagWindowBuilder.getWindowForUpdateDsTag(clickedFilter);

        updateWindow.setCaption(i18n.getMessage("caption.update", i18n.getMessage("caption.tag")));
        UI.getCurrent().addWindow(updateWindow);
        updateWindow.setVisible(Boolean.TRUE);
    }

    @Override
    protected String getFilterButtonIdPrefix() {
        return SPUIDefinitions.DISTRIBUTION_TAG_ID_PREFIXS;
    }

    public void clearDsTagFilters() {
        if (distributionTagButtonClickBehaviour.getPreviouslyClickedFiltersSize() > 0) {
            distributionTagButtonClickBehaviour.clearPreviouslyClickedFilters();

            // TODO: should we reset data communicator here for styling update

            eventBus.publish(EventTopics.TAG_FILTER_CHANGED, this, Collections.emptyList());

            distributionTagLayoutUiState.setClickedDsTagIds(Collections.emptySet());

            if (distributionTagLayoutUiState.isNoTagClicked()) {
                eventBus.publish(EventTopics.NO_TAG_FILTER_CHANGED, this, false);

                distributionTagLayoutUiState.setNoTagClicked(false);
            }
        }
    }
}
