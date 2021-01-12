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
import java.net.URISyntaxException;
import java.util.UUID;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.util.StringUtils;

import com.google.common.base.Throwables;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.UserPermissions;

/**
 * Creates and deletes a new virtual host if the rabbit mq management api is
 * available.
 * 
 */
// exception squid:S2068 - Test instance passwd
@SuppressWarnings("squid:S2068")
public class RabbitMqSetupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqSetupService.class);
    private static final String GUEST = "guest";
    private static final String DEFAULT_USER = GUEST;
    private static final String DEFAULT_PASSWORD = GUEST;
    private static final RabbitMqSetupService INSTANCE = new RabbitMqSetupService();

    @Autowired
    private RabbitProperties properties;

    private Client rabbitmqHttpClient;

    private static final String VIRTUAL_HOST = UUID.randomUUID().toString();

    private RabbitMqSetupService() {
    }

    public String getUsername() {
        return StringUtils.isEmpty(properties.getUsername()) ? DEFAULT_USER : properties.getUsername();
    }

    public String getPassword() {
        return StringUtils.isEmpty(properties.getPassword()) ? DEFAULT_PASSWORD : properties.getPassword();
    }

    public static RabbitMqSetupService instance() {
        return INSTANCE;
    }

    private synchronized Client getRabbitmqHttpClient() {
        if (rabbitmqHttpClient == null) {
            try {
                rabbitmqHttpClient = new Client(getHttpApiUrl(), getUsername(), getPassword());
            } catch (MalformedURLException | URISyntaxException e) {
                throw Throwables.propagate(e);
            }
        }
        return rabbitmqHttpClient;
    }

    private String getHttpApiUrl() {
        return "http://" + properties.getHost() + ":15672/api/";
    }

    @SuppressWarnings("squid:S1162")
    public String createVirtualHost() {
        final Client client = getRabbitmqHttpClient();
        if (!client.alivenessTest("/")) {
            throw new AlivenessException(properties.getHost());
        }
        LOGGER.info("\n\n\nCreating virtual host {}\n\n", VIRTUAL_HOST);
        client.createVhost(VIRTUAL_HOST);
        client.updatePermissions(VIRTUAL_HOST, getUsername(), createUserPermissionsFullAccess());
        return VIRTUAL_HOST;
    }

    @PreDestroy
    public void deleteVirtualHost() {
        LOGGER.info("\n\n\nDeleting virtual host {}\n\n", VIRTUAL_HOST);
        getRabbitmqHttpClient().deleteVhost(VIRTUAL_HOST);
    }

    private UserPermissions createUserPermissionsFullAccess() {
        final UserPermissions permissions = new UserPermissions();
        permissions.setVhost(VIRTUAL_HOST);
        permissions.setRead(".*");
        permissions.setConfigure(".*");
        permissions.setWrite(".*");
        return permissions;
    }

    static class AlivenessException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public AlivenessException(final String hostname) {
            super("Aliveness test failed for " + hostname
                    + ":15672 guest/quest; rabbit mq management api not available");
        }
    }

}
