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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.hawkbit.im.authentication.SpPermission;
import org.eclipse.hawkbit.im.authentication.TenantAwareAuthenticationDetails;
import org.eclipse.hawkbit.im.authentication.UserPrincipal;
import org.eclipse.hawkbit.repository.model.helper.SystemManagementHolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class WithSpringAuthorityRule implements TestRule {
    private static final Logger LOGGER = LoggerFactory.getLogger(WithSpringAuthorityRule.class);

    private final ThreadLocal<WithUser> testSecurityContext = new InheritableThreadLocal<>();
    private final Map<String, WithUser> securityContexts = new ConcurrentHashMap<>(1000);
    private static final WithSpringAuthorityRule INSTANCE = new WithSpringAuthorityRule();

    public static WithSpringAuthorityRule instance() {
        return INSTANCE;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            // throwable comes from jnuit evaluate signature
            @SuppressWarnings("squid:S00112")
            public void evaluate() throws Throwable {
                final WithUser oldWithUser = testSecurityContext.get();
                final SecurityContext oldContext = before(description);
                try {
                    base.evaluate();
                } finally {
                    after(oldContext, oldWithUser);
                    testSecurityContext.remove();
                }
            }
        };
    }

    private SecurityContext before(final Description description) {
        final SecurityContext oldContext = SecurityContextHolder.getContext();
        WithUser annotation = getWithUserAnnotation(description);
        if (annotation != null) {
            if (annotation.autoCreateTenant()) {
                createTenant(annotation.tenantId());
            }
            LOGGER.info("Setting security context for tenant {}", annotation.tenantId());
            setSecurityContext(annotation);
        }
        return oldContext;
    }

    private WithUser getWithUserAnnotation(final Description description) {
        return securityContexts.computeIfAbsent(description.getClassName() + description.getMethodName(),
                s -> getMethodAnnotation(description).map(WithSpringAuthorityRule::createWithUser)
                        .orElseGet(() -> getTestClassAnnotation(description)));
    }

    private static Optional<WithUser> getMethodAnnotation(final Description description) {
        final WithUser methodAnnotation = description.getAnnotation(WithUser.class);
        return Optional.ofNullable(methodAnnotation);
    }

    private WithUser getTestClassAnnotation(final Description description) {
        final WithUser withUser = securityContexts.computeIfAbsent(description.getClassName(),
                className -> description.getTestClass().getAnnotation(WithUser.class));

        return withUser.autoCreateTenant() ? createWithUser(withUser) : withUser;
    }

    private void setSecurityContext(final WithUser annotation) {
        testSecurityContext.set(annotation);
        SecurityContextHolder.setContext(new SecurityContext() {

            @Override
            public void setAuthentication(final Authentication authentication) {
                // do nothing
            }

            @Override
            public Authentication getAuthentication() {
                final String[] authorities = annotation.allSpPermissions() ?
                        getAllAuthorities(annotation.authorities(), annotation.removeFromAllPermission()) :
                        annotation.authorities();

                final TestingAuthenticationToken testingAuthenticationToken = new TestingAuthenticationToken(
                        new UserPrincipal(annotation.principal(), annotation.principal(), annotation.principal(),
                                annotation.principal(), null, annotation.tenantId()),
                        annotation.credentials(), authorities);
                testingAuthenticationToken.setDetails(
                        new TenantAwareAuthenticationDetails(annotation.tenantId(), annotation.controller()));
                return testingAuthenticationToken;
            }

            private String[] getAllAuthorities(final String[] additionalAuthorities, final String[] notInclude) {
                final List<String> allPermissions = new ArrayList<>();
                final Field[] declaredFields = SpPermission.class.getDeclaredFields();
                for (final Field field : declaredFields) {
                    if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                        field.setAccessible(true);
                        try {
                            boolean addPermission = true;
                            final String permissionName = (String) field.get(null);
                            if (notInclude != null) {
                                for (final String notInlcudePerm : notInclude) {
                                    if (permissionName.equals(notInlcudePerm)) {
                                        addPermission = false;
                                        break;
                                    }
                                }
                            }
                            if (addPermission) {
                                allPermissions.add(permissionName);
                            }
                            // don't want to log this exceptions.
                        } catch (@SuppressWarnings("squid:S1166") IllegalArgumentException | IllegalAccessException e) {
                            // nope
                        }
                    }
                }
                allPermissions.addAll(Arrays.asList(additionalAuthorities));
                return allPermissions.toArray(new String[0]);
            }
        });
    }

    private void after(final SecurityContext oldContext, final WithUser oldWithUser) {
        SecurityContextHolder.setContext(oldContext);
        testSecurityContext.set(oldWithUser);
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
        if(testSecurityContext.get() == null || !testSecurityContext.get().autoCreateTenant()){
            callable.call();
        }
        final WithUser withUser = privilegedUser();
        return runAs(withUser, callable);
    }

    /**
     *
     * @param withUser
     * @param callable
     * @return callable result
     * @throws Exception
     */
    public <T> T runAs(final WithUser withUser, final Callable<T> callable) throws Exception {
        final SecurityContext oldContext = SecurityContextHolder.getContext();
        final WithUser oldWithUser = testSecurityContext.get();
        setSecurityContext(withUser);
        if (withUser.autoCreateTenant()) {
            createTenant(withUser.tenantId());
        }
        try {
            return callable.call();
        } finally {
            after(oldContext, oldWithUser);
        }
    }

    private void createTenant(final String tenantId) {
        final SecurityContext oldContext = SecurityContextHolder.getContext();
        final WithUser oldWithUser = testSecurityContext.get();
        setSecurityContext(privilegedUser());
        try {
            SystemManagementHolder.getInstance().getSystemManagement().getTenantMetadata(tenantId);
        } finally {
            after(oldContext, oldWithUser);
        }
    }

    public static WithUser withController(final String principal, final String... authorities) {
        final String tenantId = INSTANCE.testSecurityContext.get() != null ? INSTANCE.testSecurityContext.get().tenantId() : "";
        return withUserAndTenant(principal, tenantId, true, true, true, authorities);
    }

    public static WithUser withUser(final String principal, final String... authorities) {
        final String tenantId = INSTANCE.testSecurityContext.get() != null ? INSTANCE.testSecurityContext.get().tenantId() : "";
        return withUserAndTenant(principal, tenantId, true, true, false, authorities);
    }

    public static WithUser withUser(final String principal, final boolean allSpPermision, final String... authorities) {
        final String tenantId = INSTANCE.testSecurityContext.get() != null ? INSTANCE.testSecurityContext.get().tenantId() : "";
        return withUserAndTenant(principal, tenantId, true, allSpPermision, false, authorities);
    }

    public  WithUser withUser(final boolean autoCreateTenant) {
        final String tenantId = testSecurityContext.get() != null ? testSecurityContext.get().tenantId() : "";
        return withUserAndTenant("", tenantId, autoCreateTenant, true, false);
    }

    public static WithUser withUserAndTenant(final String principal, final String tenant, final String... authorities) {
        return withUserAndTenant(principal, tenant, true, true, false, authorities);
    }

    public static WithUser withUserAndTenant(final String principal, final String tenant,
            final boolean autoCreateTenant, final boolean allSpPermission) {
        return withUserAndTenant(principal, tenant, autoCreateTenant, allSpPermission, false);
    }

    public static WithUser withUserAndTenant(final String principal, final String tenant,
            final boolean autoCreateTenant, final boolean allSpPermission, final boolean controller,
            final String... authorities) {
        return createWithUser(principal, tenant, autoCreateTenant, allSpPermission, controller, authorities);
    }

    private WithUser privilegedUser() {
        final String tenant = testSecurityContext.get() == null ? "" : testSecurityContext.get().tenantId();
        return createWithUser("", tenant, true, true, false, new String[] { "ROLE_CONTROLLER", "ROLE_SYSTEM_CODE" });
    }

    private static WithUser createWithUser(final WithUser annotation) {
        return createWithUser(annotation.principal(), annotation.tenantId(), annotation.autoCreateTenant(),
                annotation.allSpPermissions(), annotation.controller(),
                Arrays.asList(annotation.removeFromAllPermission()), annotation.authorities());
    }

    private static WithUser createWithUser(final String principal, final String tenant, final boolean autoCreateTenant,
            final boolean allSpPermission, final boolean controller, final String[] authorities) {
        return createWithUser(principal, tenant, autoCreateTenant, allSpPermission, controller, Collections.emptyList(),
                authorities);
    }
    private static WithUser createWithUser(final String principal, final String tenant, final boolean autoCreateTenant,
            final boolean allSpPermission, final boolean controller, final List<String> removeFromAllPermission,
            final String[] authorities) {
        return new WithUser() {

            private final String thePrincipal = principal.isEmpty() ? UUID.randomUUID().toString() : principal;
            private final String theTenant = (tenant.isEmpty() ? UUID.randomUUID().toString() : tenant).toUpperCase();

            @Override
            public Class<? extends Annotation> annotationType() {
                return WithUser.class;
            }

            @Override
            public String principal() {
                return thePrincipal;
            }

            @Override
            public String credentials() {
                return null;
            }

            @Override
            public String[] authorities() {
                return authorities;
            }

            @Override
            public boolean allSpPermissions() {
                return allSpPermission;
            }

            @Override
            public String[] removeFromAllPermission() {
                return removeFromAllPermission.toArray(new String[0]);
            }

            @Override
            public String tenantId() {
                return theTenant;
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
