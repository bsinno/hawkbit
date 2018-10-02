/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.autoconfigure.mgmt.ui;

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.hawkbit.DistributedResourceBundleMessageSource;
import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.ui.MgmtUiConfiguration;
import org.eclipse.hawkbit.ui.UiProperties;
import org.eclipse.hawkbit.ui.push.DelayedEventBusPushStrategy;
import org.eclipse.hawkbit.ui.push.EventPushStrategy;
import org.eclipse.hawkbit.ui.push.HawkbitEventProvider;
import org.eclipse.hawkbit.ui.push.UIEventProvider;
import org.eclipse.hawkbit.ui.rollout.rollout.RolloutDataProvider;
import org.eclipse.hawkbit.ui.rollout.rolloutgroup.RolloutGroupDataProvider;
import org.eclipse.hawkbit.ui.rollout.rolloutgrouptargets.RolloutGroupTargetsDataProvider;
import org.eclipse.hawkbit.ui.rollout.state.RolloutUIState;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.vaadin.spring.annotation.EnableVaadinExtensions;
import org.vaadin.spring.events.EventBus.UIEventBus;
import org.vaadin.spring.events.annotation.EnableEventBus;
import org.vaadin.spring.security.annotation.EnableVaadinSharedSecurity;

import com.vaadin.spring.annotation.UIScope;

/**
 * The Management UI auto configuration.
 */
@Configuration
@EnableVaadinSharedSecurity
@EnableVaadinExtensions
@EnableEventBus
@ConditionalOnClass(MgmtUiConfiguration.class)
@Import(MgmtUiConfiguration.class)
public class MgmtUiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    RedirectController uiRedirectController() {
        return new RedirectController();
    }

    /**
     * A message source bean to add distributed message sources.
     * 
     * @return the message bean.
     */
    @Bean(name = "messageSource")
    DistributedResourceBundleMessageSource messageSource() {
        return new DistributedResourceBundleMessageSource();
    }

    /**
     * A event provider bean which hold the supported events for the UI.
     * 
     * @return the provider bean
     */
    @Bean
    @ConditionalOnMissingBean
    UIEventProvider eventProvider() {
        return new HawkbitEventProvider();
    }

    /**
     * The UI scoped event push strategy. Session scope is necessary, that every
     * UI has an own strategy.
     * 
     * @param applicationContext
     *            the context to add the listener to
     * @param executorService
     *            the general scheduler service
     * @param eventBus
     *            the ui event bus
     * @param eventProvider
     *            the event provider
     * @param uiProperties
     *            the ui properties
     * @return the push strategy bean
     */
    @Bean
    @ConditionalOnMissingBean
    @UIScope
    EventPushStrategy eventPushStrategy(final ConfigurableApplicationContext applicationContext,
            final ScheduledExecutorService executorService, final UIEventBus eventBus,
            final UIEventProvider eventProvider, final UiProperties uiProperties) {
        final DelayedEventBusPushStrategy delayedEventBusPushStrategy = new DelayedEventBusPushStrategy(executorService,
                eventBus, eventProvider, uiProperties.getEvent().getPush().getDelay());
        applicationContext.addApplicationListener(delayedEventBusPushStrategy);
        return delayedEventBusPushStrategy;
    }

    @Bean
    @ConditionalOnMissingBean
    @UIScope
    RolloutDataProvider rolloutDataProvider(final RolloutManagement rolloutManagement,
            final RolloutUIState rolloutUIState) {
        return new RolloutDataProvider(rolloutManagement, rolloutUIState);
    }

    @Bean
    @ConditionalOnMissingBean
    @UIScope
    RolloutGroupDataProvider rolloutGroupDataProvider(final RolloutManagement rolloutManagement,
            final RolloutGroupManagement rolloutGroupManagement, final RolloutUIState rolloutUIState) {
        return new RolloutGroupDataProvider(rolloutManagement, rolloutGroupManagement, rolloutUIState);
    }

    @Bean
    @ConditionalOnMissingBean
    @UIScope
    RolloutGroupTargetsDataProvider rolloutGroupTargetsDataProvider(final RolloutManagement rolloutManagement,
            final RolloutGroupManagement rolloutGroupManagement, final RolloutUIState rolloutUIState) {
        return new RolloutGroupTargetsDataProvider(rolloutManagement, rolloutGroupManagement, rolloutUIState);
    }

}
