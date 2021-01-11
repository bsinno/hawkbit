/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.rabbitmq.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.eclipse.hawkbit.HawkbitServerProperties;
import org.eclipse.hawkbit.api.HostnameResolver;
import org.eclipse.hawkbit.rabbitmq.test.RabbitMqSetupService.AlivenessException;
import org.eclipse.hawkbit.repository.jpa.model.helper.SystemSecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.junit.BrokerRunning;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import com.google.common.base.Throwables;

/**
 *
 */
@Configuration
public class AmqpTestConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(AmqpTestConfiguration.class);

    @Bean
    SystemSecurityContextHolder systemSecurityContextHolder() {
        return SystemSecurityContextHolder.getInstance();
    }

    @Bean
    Executor asyncExecutor() {
        return new DelegatingSecurityContextExecutorService(Executors.newSingleThreadExecutor());
    }

    @Bean
    TaskExecutor taskExecutor() {
        return new ConcurrentTaskExecutor(asyncExecutor());
    }

    @Bean
    ScheduledExecutorService scheduledExecutorService() {
        return threadPoolTaskScheduler().getScheduledExecutor();
    }

    @Bean
    ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Bean
    HostnameResolver hostnameResolver(final HawkbitServerProperties serverProperties) {
        return () -> {
            try {
                return new URL(serverProperties.getUrl());
            } catch (final MalformedURLException e) {
                throw Throwables.propagate(e);
            }
        };
    }

    @Bean
    RabbitMqSetupService rabbitMqSetupService(final RabbitProperties properties) {
        return new RabbitMqSetupService(properties);
    }

    @Bean(destroyMethod = "deleteVhost")
    ConnectionFactory rabbitConnectionFactory(final RabbitProperties properties, final RabbitMqSetupService setupService) {
        final CachingConnectionFactory factory = new CachingConnectionFactory(){
            @PreDestroy
            public void deleteVhost() {
                destroy();
                setupService.deleteVirtualHost();
            }
        };
        factory.setHost(properties.getHost());
        factory.setPort(5672);
        factory.setUsername(properties.getUsername());
        factory.setPassword(properties.getPassword());
        try {
            factory.setVirtualHost(setupService.createVirtualHost());
            // All exception are caught. The BrokerRunning decide if the
            // test should break or not
        } catch (@SuppressWarnings("squid:S2221") final Exception e) {
            Throwables.propagateIfInstanceOf(e, AlivenessException.class);
            LOG.error("Cannot create virtual host.", e);
        }
        return factory;
    }

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplateForTest(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.setReplyTimeout(TimeUnit.SECONDS.toMillis(3));
        rabbitTemplate.setReceiveTimeout(TimeUnit.SECONDS.toMillis(3));
        return rabbitTemplate;
    }

    @Bean
    BrokerRunning brokerRunning(final RabbitProperties properties) {
        final BrokerRunning brokerRunning = BrokerRunning.isRunning();
        brokerRunning.setHostName(properties.getHost());
        brokerRunning.getConnectionFactory().setUsername(properties.getUsername());
        brokerRunning.getConnectionFactory().setPassword(properties.getPassword());
        return brokerRunning;
    }

}
