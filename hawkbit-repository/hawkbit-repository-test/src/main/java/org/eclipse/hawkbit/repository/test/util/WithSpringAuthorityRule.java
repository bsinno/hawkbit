/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.test.util;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.im.authentication.SpPermission;
import org.eclipse.hawkbit.im.authentication.TenantAwareAuthenticationDetails;
import org.eclipse.hawkbit.im.authentication.UserPrincipal;
import org.eclipse.hawkbit.repository.model.helper.SystemManagementHolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class WithSpringAuthorityRule implements TestRule {

    private static final SpPermission[] EMPTY_PERMISSIONS = new SpPermission[0];
    private static final String DEFAULT_TENANT = "default";

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            // throwable comes from JUnit evaluate signature
            @SuppressWarnings("squid:S00112")
            public void evaluate() throws Throwable {
                final SecurityContext oldContext = before(description);
                try {
                    base.evaluate();
                } finally {
                    after(oldContext);
                }
            }
        };
    }

    private SecurityContext before(final Description description) {
        final SecurityContext oldContext = SecurityContextHolder.getContext();
        WithUser annotation = description.getAnnotation(WithUser.class);
        if (annotation == null) {
            annotation = description.getTestClass().getAnnotation(WithUser.class);
        }
        if (annotation != null) {
            if (annotation.autoCreateTenant()) {
                createTenant(annotation.tenantId());
            }
            setSecurityContext(annotation);
        }
        return oldContext;
    }

    private void setSecurityContext(final WithUser annotation) {
        SecurityContextHolder.setContext(new SecurityContext() {
            private static final long serialVersionUID = 1L;

            @Override
            public void setAuthentication(final Authentication authentication) {
                // nothing todo
            }

            @Override
            public Authentication getAuthentication() {
                final List<String> authorities;
                if (annotation.allSpPermissions()) {
                    authorities = getAllAuthorities(annotation.removeFromAllPermission());
                } else {
                    authorities = permissionsAsStringList(annotation.permissions());
                }
                authorities.addAll(Arrays.asList(annotation.additionalAuthorities()));

                final TestingAuthenticationToken testingAuthenticationToken = new TestingAuthenticationToken(
                        new UserPrincipal(annotation.principal(), annotation.principal(), annotation.principal(),
                                annotation.principal(), null, annotation.tenantId()),
                        annotation.credentials(), authorities.toArray(new String[0]));
                testingAuthenticationToken.setDetails(
                        new TenantAwareAuthenticationDetails(annotation.tenantId(), annotation.controller()));
                return testingAuthenticationToken;
            }

            private List<String> getAllAuthorities(final SpPermission[] notInclude) {
                final List<String> allAuthorities = SpPermission.getAllAuthorities();
                allAuthorities.removeAll(permissionsAsStringList(notInclude));
                return allAuthorities;
            }

            private List<String> permissionsAsStringList(final SpPermission[] permissions) {
                return Arrays.stream(permissions).map(Enum::name).collect(Collectors.toList());
            }
        });
    }

    private void after(final SecurityContext oldContext) {
        SecurityContextHolder.setContext(oldContext);
    }

    /**
     * Clears the current security context.
     */
    public void clear() {
        SecurityContextHolder.clearContext();
    }

    /**
     * @param callable
     * @return the callable result
     * @throws Exception
     */
    public <T> T runAsPrivileged(final Callable<T> callable) throws Exception {
        return runAs(privilegedUser(), callable);
    }

    /**
     * @param withUser
     * @param callable
     * @return callable result
     * @throws Exception
     */
    public <T> T runAs(final WithUser withUser, final Callable<T> callable) throws Exception {
        final SecurityContext oldContext = SecurityContextHolder.getContext();
        setSecurityContext(withUser);
        if (withUser.autoCreateTenant()) {
            createTenant(withUser.tenantId());
        }
        try {
            return callable.call();
        } finally {
            after(oldContext);
        }
    }

    private void createTenant(final String tenantId) {
        final SecurityContext oldContext = SecurityContextHolder.getContext();
        setSecurityContext(privilegedUser());
        try {
            SystemManagementHolder.getInstance().getSystemManagement().getTenantMetadata(tenantId);
        } finally {
            after(oldContext);
        }
    }

    public static WithUser withController(final String principal, final String... authorities) {
        return createWithUser(principal, DEFAULT_TENANT, true, true, true, EMPTY_PERMISSIONS, authorities);
    }

    public static WithUser withUser(final String principal, final SpPermission... permissions) {
        return createWithUser(principal, DEFAULT_TENANT, true, true, false, permissions);
    }

    public static WithUser withUser(final String principal, final boolean allSpPermision,
            final SpPermission... permissions) {
        return createWithUser(principal, DEFAULT_TENANT, true, allSpPermision, false, permissions);
    }

    public static WithUser withUserAndTenant(final String principal, final String tenant) {
        return createWithUser(principal, tenant, true, true, false, EMPTY_PERMISSIONS);
    }

    public static WithUser withUserAndTenant(final String principal, final String tenant,
            final boolean autoCreateTenant, final boolean allSpPermission, final boolean controller,
            final SpPermission[] permissions, final String... additionalAuthorities) {
        return createWithUser(principal, tenant, autoCreateTenant, allSpPermission, controller, permissions,
                additionalAuthorities);
    }

    private static WithUser privilegedUser() {
        return createWithUser("bumlux", DEFAULT_TENANT, true, true, false, EMPTY_PERMISSIONS, "ROLE_CONTROLLER",
                "ROLE_SYSTEM_CODE");
    }

    private static WithUser createWithUser(final String principal, final String tenant, final boolean autoCreateTenant,
            final boolean allSpPermission, final boolean controller, final SpPermission[] permissions,
            final String... additionalAuthorities) {
        return new WithUser() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return WithUser.class;
            }

            @Override
            public String principal() {
                return principal;
            }

            @Override
            public String credentials() {
                return null;
            }

            @Override
            public SpPermission[] permissions() {
                return permissions;
            }

            @Override
            public boolean allSpPermissions() {
                return allSpPermission;
            }

            @Override
            public SpPermission[] removeFromAllPermission() {
                return new SpPermission[0];
            }

            @Override
            public String[] additionalAuthorities() {
                return additionalAuthorities;
            }

            @Override
            public String tenantId() {
                return tenant;
            }

            @Override
            public boolean autoCreateTenant() {
                return autoCreateTenant;
            }

            @Override
            public boolean controller() {
                return controller;
            }
        };
    }
}
