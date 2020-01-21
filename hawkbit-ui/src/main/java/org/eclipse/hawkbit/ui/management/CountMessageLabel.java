/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management;

import java.util.Collection;

import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.ui.common.data.filters.TargetManagementFilterParams;
import org.eclipse.hawkbit.ui.common.data.proxies.ProxyTarget;
import org.eclipse.hawkbit.ui.common.grid.AbstractFooterSupport;
import org.eclipse.hawkbit.ui.management.event.ManagementUIEvent;
import org.eclipse.hawkbit.ui.management.event.PinUnpinEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent;
import org.eclipse.hawkbit.ui.management.event.TargetTableEvent.TargetComponentEvent;
import org.eclipse.hawkbit.ui.management.targettable.TargetGridLayoutUiState;
import org.eclipse.hawkbit.ui.utils.HawkbitCommonUtil;
import org.eclipse.hawkbit.ui.utils.SPUIStyleDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.springframework.util.StringUtils;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.EventScope;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.vaadin.data.provider.DataCommunicator;
import com.vaadin.data.provider.Query;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;

/**
 * Count message label which display current filter details and details on
 * pinning.
 */
// TODO: refactor
public class CountMessageLabel extends AbstractFooterSupport {
    private final VaadinMessageSource i18n;

    private final TargetGridLayoutUiState targetGridLayoutUiState;

    private final TargetManagement targetManagement;

    private final DataCommunicator<ProxyTarget> targetGridDataCommunicator;

    private final Label targetCountLabel;

    /**
     * Constructor
     * 
     * @param eventBus
     *            UIEventBus
     * @param targetManagement
     *            TargetManagement
     * @param i18n
     *            I18N
     * @param managementUIState
     *            ManagementUIState
     * @param targetGridDataCommunicator
     *            TargetGrid data communicator
     */
    public CountMessageLabel(final UIEventBus eventBus, final TargetManagement targetManagement,
            final VaadinMessageSource i18n, final TargetGridLayoutUiState targetGridLayoutUiState,
            final DataCommunicator<ProxyTarget> targetGridDataCommunicator) {
        this.targetManagement = targetManagement;
        this.i18n = i18n;
        this.targetGridLayoutUiState = targetGridLayoutUiState;
        this.targetGridDataCommunicator = targetGridDataCommunicator;
        this.targetCountLabel = new Label();

        init();
        eventBus.subscribe(this);
    }

    private void init() {
        targetCountLabel.setId(UIComponentIdProvider.COUNT_LABEL);
        targetCountLabel.addStyleName(SPUIStyleDefinitions.SP_LABEL_MESSAGE_STYLE);
        targetCountLabel.setContentMode(ContentMode.HTML);
    }

    /**
     * TenantAwareEvent Listener to show the message count.
     *
     * @param event
     *            ManagementUIEvent which describes the action to execute
     */
    @EventBusListenerMethod(scope = EventScope.UI)
    public void onEvent(final ManagementUIEvent event) {
        if (event == ManagementUIEvent.TARGET_TABLE_FILTER || event == ManagementUIEvent.SHOW_COUNT_MESSAGE) {
            displayTargetCountStatus();
        }
    }

    @EventBusListenerMethod(scope = EventScope.UI)
    void onEvent(final TargetTableEvent event) {
        if (TargetTableEvent.TargetComponentEvent.SELECT_ALL == event.getTargetComponentEvent()
                || TargetComponentEvent.REFRESH_TARGETS == event.getTargetComponentEvent()) {
            displayTargetCountStatus();
        }
    }

    /**
     * TenantAwareEvent Listener for Pinning Distribution.
     *
     * @param event
     */
    @EventBusListenerMethod(scope = EventScope.UI)
    public void onEvent(final PinUnpinEvent event) {
        final Long pinnedDistId = targetGridLayoutUiState.getTargetManagementFilterParams().getPinnedDistId();

        if (event == PinUnpinEvent.PIN_DISTRIBUTION && pinnedDistId != null) {
            displayCountLabel(pinnedDistId);
        } else {
            targetCountLabel.setValue("");
            displayTargetCountStatus();
        }
    }

    private void displayTargetCountStatus() {
        final TargetManagementFilterParams targetFilterParams = targetGridLayoutUiState
                .getTargetManagementFilterParams();
        final StringBuilder message = getTotalTargetMessage();

        if (targetFilterParams.isAnyFilterSelected()) {
            message.append(HawkbitCommonUtil.SP_STRING_PIPE);
            message.append(i18n.getMessage("label.filter.targets"));
            message.append(targetGridDataCommunicator.getDataProviderSize());
            message.append(HawkbitCommonUtil.SP_STRING_PIPE);
            final String status = i18n.getMessage("label.filter.status");
            final String overdue = i18n.getMessage("label.filter.overdue");
            final String tags = i18n.getMessage("label.filter.tags");
            final String text = i18n.getMessage("label.filter.text");
            final String dists = i18n.getMessage("label.filter.dist");
            final String custom = i18n.getMessage("label.filter.custom");
            final StringBuilder filterMesgBuf = new StringBuilder(i18n.getMessage("label.filter"));
            filterMesgBuf.append(" ");
            filterMesgBuf.append(getStatusMsg(targetFilterParams.getTargetUpdateStatusList(), status));
            filterMesgBuf.append(getOverdueStateMsg(targetFilterParams.getOverdueState(), overdue));
            filterMesgBuf
                    .append(getTagsMsg(targetFilterParams.getNoTagClicked(), targetFilterParams.getTargetTags(), tags));
            filterMesgBuf.append(!StringUtils.isEmpty(targetFilterParams.getSearchText()) ? text : " ");
            filterMesgBuf.append(targetFilterParams.getDistributionId() != null ? dists : " ");
            filterMesgBuf.append(targetFilterParams.getTargetFilterQueryId() != null ? custom : " ");
            final String filterMesageChk = filterMesgBuf.toString().trim();
            String filterMesage = filterMesageChk;
            if (filterMesage.endsWith(",")) {
                filterMesage = filterMesageChk.substring(0, filterMesageChk.length() - 1);
            }
            message.append(filterMesage);
        }

        targetCountLabel.setCaption(message.toString());
    }

    private StringBuilder getTotalTargetMessage() {
        targetCountLabel.setIcon(null);
        targetCountLabel.setDescription(null);

        final StringBuilder message = new StringBuilder(i18n.getMessage("label.target.filter.count"));
        message.append(targetGridDataCommunicator.getDataProvider().size(new Query<>()));

        return message;
    }

    private void displayCountLabel(final Long distId) {
        final Long targetsWithAssigedDsCount = targetManagement.countByAssignedDistributionSet(distId);
        final Long targetsWithInstalledDsCount = targetManagement.countByInstalledDistributionSet(distId);
        final StringBuilder message = new StringBuilder(i18n.getMessage("label.target.count"));
        message.append("<span class=\"assigned-count-message\">");
        message.append(i18n.getMessage("label.assigned.count", targetsWithAssigedDsCount));
        message.append("</span>, <span class=\"installed-count-message\"> ");
        message.append(i18n.getMessage("label.installed.count", targetsWithInstalledDsCount));
        message.append("</span>");
        targetCountLabel.setValue(message.toString());
    }

    private static String getStatusMsg(final Collection<TargetUpdateStatus> status, final String param) {
        return status.isEmpty() ? " " : param;
    }

    private static String getOverdueStateMsg(final boolean overdueState, final String param) {
        return !overdueState ? " " : param;
    }

    private static String getTagsMsg(final Boolean noTargetTagSelected, final String[] tags, final String param) {
        return (tags == null || tags.length == 0)
                && (noTargetTagSelected == null || !noTargetTagSelected.booleanValue()) ? " " : param;
    }

    @Override
    protected Label getFooterMessageLabel() {
        return targetCountLabel;
    }
}
