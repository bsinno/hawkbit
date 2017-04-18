/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.test.util;

import java.util.List;

import org.eclipse.hawkbit.cache.TenantAwareCacheManager;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaTestRepositoryManagement implements TestRepositoryManagement {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaTestRepositoryManagement.class);

    private final TenantAwareCacheManager cacheManager;

    private final SystemSecurityContext systemSecurityContext;

    private final SystemManagement systemManagement;

    /**
     * Constructor.
     * 
     * @param cacheManager
     *            the cachemanager
     * @param systemSecurityContext
     *            the systemSecurityContext
     * @param systemManagement
     *            the systemManagement
     */
    public JpaTestRepositoryManagement(final TenantAwareCacheManager cacheManager,
            final SystemSecurityContext systemSecurityContext, final SystemManagement systemManagement) {
        this.cacheManager = cacheManager;
        this.systemSecurityContext = systemSecurityContext;
        this.systemManagement = systemManagement;
    }

    @Override
    public void clearTestRepository() {
        deleteAllRepos();
        cacheManager.getDirectCacheNames().forEach(name -> cacheManager.getDirectCache(name).clear());
    }

    public void deleteAllRepos() {
        final List<String> tenants = systemSecurityContext.runAsSystem(() -> systemManagement.findTenants());
        tenants.forEach(tenant -> {
            try {
                systemSecurityContext.runAsSystem(() -> {
                    systemManagement.deleteTenant(tenant);
                    return null;
                });
            } catch (final Exception e) {
                LOGGER.error("Error hile delete tenant", e);
            }
        });
    }
}
