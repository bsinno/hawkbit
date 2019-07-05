/**
 * Copyright (c) 2019 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.window;

import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.QuotaManagement;
import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TargetFilterQueryManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.common.data.providers.DistributionSetStatelessDataProvider;
import org.eclipse.hawkbit.ui.common.data.providers.TargetFilterQueryDataProvider;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

/**
 * Rollout window dependencies holder.
 */
public final class RolloutWindowDependencies {

    private final RolloutManagement rolloutManagement;
    private final RolloutGroupManagement rolloutGroupManagement;
    private final QuotaManagement quotaManagement;
    private final TargetManagement targetManagement;
    private final TargetFilterQueryManagement targetFilterQueryManagement;
    private final UINotification uiNotification;
    private final EntityFactory entityFactory;
    private final VaadinMessageSource i18n;
    private final UIEventBus eventBus;
    private final UiProperties uiProperties;
    private final DistributionSetStatelessDataProvider distributionSetDataProvider;
    private final TargetFilterQueryDataProvider targetFilterQueryDataProvider;

    public RolloutWindowDependencies(final RolloutManagement rolloutManagement, final TargetManagement targetManagement,
            final UINotification uiNotification, final EntityFactory entityFactory, final VaadinMessageSource i18n,
            final UiProperties uiProperties, final UIEventBus eventBus,
            final TargetFilterQueryManagement targetFilterQueryManagement,
            final RolloutGroupManagement rolloutGroupManagement, final QuotaManagement quotaManagement,
            final DistributionSetStatelessDataProvider distributionSetDataProvider,
            final TargetFilterQueryDataProvider targetFilterQueryDataProvider) {
        this.rolloutManagement = rolloutManagement;
        this.rolloutGroupManagement = rolloutGroupManagement;
        this.quotaManagement = quotaManagement;
        this.targetManagement = targetManagement;
        this.uiNotification = uiNotification;
        this.uiProperties = uiProperties;
        this.entityFactory = entityFactory;
        this.i18n = i18n;
        this.eventBus = eventBus;
        this.targetFilterQueryManagement = targetFilterQueryManagement;
        this.distributionSetDataProvider = distributionSetDataProvider;
        this.targetFilterQueryDataProvider = targetFilterQueryDataProvider;
    }

    public RolloutManagement getRolloutManagement() {
        return rolloutManagement;
    }

    public RolloutGroupManagement getRolloutGroupManagement() {
        return rolloutGroupManagement;
    }

    public QuotaManagement getQuotaManagement() {
        return quotaManagement;
    }

    public TargetManagement getTargetManagement() {
        return targetManagement;
    }

    public TargetFilterQueryManagement getTargetFilterQueryManagement() {
        return targetFilterQueryManagement;
    }

    public UINotification getUiNotification() {
        return uiNotification;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public VaadinMessageSource getI18n() {
        return i18n;
    }

    public UiProperties getUiProperties() {
        return uiProperties;
    }

    public UIEventBus getEventBus() {
        return eventBus;
    }

    public DistributionSetStatelessDataProvider getDistributionSetDataProvider() {
        return distributionSetDataProvider;
    }

    public TargetFilterQueryDataProvider getTargetFilterQueryDataProvider() {
        return targetFilterQueryDataProvider;
    }
}
