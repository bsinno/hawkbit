/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 * <p>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.im.authentication;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;

/**
 * <p>
 * Software provisioning permissions that are technically available as
 * {@link GrantedAuthority} based on the authenticated users identity context.
 * </p>
 *
 * <p>
 * The Permissions cover CRUD for two data areas:
 * <p>
 * XX_Target_CRUD which covers the following entities: {@link Target} entities
 * including metadata, {@link TargetTag}s, {@link TargetRegistrationRule}s
 * XX_Repository CRUD which covers: {@link DistributionSet}s,
 * {@link SoftwareModule}s, DS Tags
 * </p>
 */
public enum SpPermission implements GrantedAuthority {

    /**
     * Permission to read the targets from the {@link ProvisioningTargetRepository}
     * including their meta information, {@link ProvisioningTargetFilter}s and
     * target changing entities ( {@link DistributionSetApplier} and
     * {@link TargetRegistrationRule}). That corresponds in REST API to GET.
     */
    READ_TARGET(Authority.READ_TARGET),

    /**
     * Permission to read the target security token. The security token is security
     * concerned and should be protected. So the combination {@link #READ_TARGET}
     * and {@link #READ_TARGET_SEC_TOKEN} is necessary to able to read the security
     * token of an target.
     */
    READ_TARGET_SEC_TOKEN(Authority.READ_TARGET_SEC_TOKEN),
    /**
     * Permission to change/edit/update targets in the
     * {@link ProvisioningTargetRepository} including their meta information and
     * or/relations or {@link DistributionSet} assignment,
     * {@link ProvisioningTargetFilter}s and target changing entities (
     * {@link DistributionSetApplier} and {@link TargetRegistrationRule}). That
     * corresponds in REST API to POST.
     */
    UPDATE_TARGET(Authority.UPDATE_TARGET),

    /**
     * Permission to add new targets to the {@link ProvisioningTargetRepository}
     * including their meta information and or/relations or {@link DistributionSet}
     * assignment.That corresponds in REST API to PUT.
     */
    CREATE_TARGET(Authority.CREATE_TARGET),

    /**
     * Permission to delete targets in the {@link ProvisioningTargetRepository},
     * {@link ProvisioningTargetFilter}s and target changing entities (
     * {@link DistributionSetApplier} and {@link TargetRegistrationRule}). That
     * corresponds in REST API to DELETE.
     */
    DELETE_TARGET(Authority.DELETE_TARGET),

    /**
     * Permission to read {@link DistributionSet}s and/or {@link OsPackage}s. That
     * corresponds in REST API to GET.
     */
    READ_REPOSITORY(Authority.READ_REPOSITORY),

    /**
     * Permission to edit/update {@link DistributionSet}s including their
     * {@link OsPackage} assignment and/or {@link OsPackage}s. That corresponds in
     * REST API to POST.
     */
    UPDATE_REPOSITORY(Authority.UPDATE_REPOSITORY),

    /**
     * Permission to add {@link DistributionSet}s and/or {@link OsPackage}s to the
     * repository. That corresponds in REST API to PUT.
     */
    CREATE_REPOSITORY(Authority.CREATE_REPOSITORY),

    /**
     * Permission to delete {@link DistributionSet}s and/or {@link OsPackage}s from
     * the repository. That corresponds in REST API to DELETE.
     */
    DELETE_REPOSITORY(Authority.DELETE_REPOSITORY),

    /**
     * Permission to administrate the system on a global, i.e. tenant independent
     * scale. That includes the deletion of tenants.
     */
    SYSTEM_ADMIN(Authority.SYSTEM_ADMIN),

    /**
     * Permission to download repository artifact of an software module.
     */
    DOWNLOAD_REPOSITORY_ARTIFACT(Authority.DOWNLOAD_REPOSITORY_ARTIFACT),

    /**
     * Permission to administrate the tenant settings.
     */
    TENANT_CONFIGURATION(Authority.TENANT_CONFIGURATION),

    /**
     * Permission to read a rollout.
     */
    READ_ROLLOUT(Authority.READ_ROLLOUT),

    /**
     * Permission to create a rollout.
     */
    CREATE_ROLLOUT(Authority.CREATE_ROLLOUT),

    /**
     * Permission to update a rollout.
     */
    UPDATE_ROLLOUT(Authority.UPDATE_ROLLOUT),

    /**
     * Permission to delete a rollout.
     */
    DELETE_ROLLOUT(Authority.DELETE_ROLLOUT),

    /**
     * Permission to start/stop/resume a rollout.
     */
    HANDLE_ROLLOUT(Authority.HANDLE_ROLLOUT),

    /**
     * Permission to approve or deny a rollout prior to starting.
     */
    APPROVE_ROLLOUT(Authority.APPROVE_ROLLOUT);

    private final String authority;

    SpPermission(final String authority) {
        if (!authority.equals(this.name())) {
            throw new IllegalArgumentException();
        }
        this.authority = authority;
    }

    /**
     * Get the authority of a single permission
     * 
     * @return authority of permission
     */
    @Override
    public String getAuthority() {
        return this.authority;
    }

    /**
     * Return all permission.
     *
     * @return all permissions
     */
    public static List<String> getAllAuthorities() {
        return Stream.of(SpPermission.values()).map(SpPermission::getAuthority).collect(Collectors.toList());
    }

    public static final class Authority {
        public static final String READ_TARGET = "READ_TARGET";

        public static final String READ_TARGET_SEC_TOKEN = "READ_TARGET_SEC_TOKEN";
        public static final String UPDATE_TARGET = "UPDATE_TARGET";
        public static final String CREATE_TARGET = "CREATE_TARGET";
        public static final String DELETE_TARGET = "DELETE_TARGET";

        public static final String READ_REPOSITORY = "READ_REPOSITORY";
        public static final String UPDATE_REPOSITORY = "UPDATE_REPOSITORY";
        public static final String CREATE_REPOSITORY = "CREATE_REPOSITORY";
        public static final String DELETE_REPOSITORY = "DELETE_REPOSITORY";

        public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";

        public static final String DOWNLOAD_REPOSITORY_ARTIFACT = "DOWNLOAD_REPOSITORY_ARTIFACT";

        public static final String TENANT_CONFIGURATION = "TENANT_CONFIGURATION";

        public static final String READ_ROLLOUT = "READ_ROLLOUT";
        public static final String CREATE_ROLLOUT = "CREATE_ROLLOUT";
        public static final String UPDATE_ROLLOUT = "UPDATE_ROLLOUT";
        public static final String DELETE_ROLLOUT = "DELETE_ROLLOUT";
        public static final String HANDLE_ROLLOUT = "HANDLE_ROLLOUT";
        public static final String APPROVE_ROLLOUT = "APPROVE_ROLLOUT";

        private Authority() {
            // Constant class
        }
    }

    /**
     * <p>
     * Contains all the spring security evaluation expressions for the
     * {@link PreAuthorize} annotation for method security.
     * </p>
     *
     * <p>
     * Examples:
     * <p>
     * {@code
     * hasRole([role])   Returns true if the current principal has the specified role.
     * hasAnyRole([role1,role2])  Returns true if the current principal has any of the supplied roles (given as a comma-separated list of strings)
     * principal   Allows direct access to the principal object representing the current user
     * authentication Allows direct access to the current Authentication object obtained from the SecurityContext
     * permitAll   Always evaluates to true
     * denyAll  Always evaluates to false
     * isAnonymous()  Returns true if the current principal is an anonymous user
     * isRememberMe() Returns true if the current principal is a remember-me user
     * isAuthenticated() Returns true if the user is not anonymous
     * isFullyAuthenticated()  Returns true if the user is not an anonymous or a remember-me user
     * }
     * </p>
     */
    public static final class SpringEvalExpressions {
        /*
         * Spring security eval expressions.
         */
        public static final String BRACKET_OPEN = "(";
        public static final String BRACKET_CLOSE = ")";
        public static final String HAS_AUTH_PREFIX = "hasAuthority" + BRACKET_OPEN + "'";
        public static final String HAS_AUTH_SUFFIX = "'" + BRACKET_CLOSE;
        public static final String HAS_AUTH_AND = " and ";

        /**
         * The role which contains in the spring security context in case an controller
         * is authenticated.
         */
        public static final String CONTROLLER_ROLE = "ROLE_CONTROLLER";

        /**
         * The role which contains in the spring security context in case an controller
         * is authenticated but only as anonymous.
         */
        public static final String CONTROLLER_ROLE_ANONYMOUS = "ROLE_CONTROLLER_ANONYMOUS";

        /**
         * The role which contains the spring security context in case the system is
         * executing code which is necessary to be privileged.
         */
        public static final String SYSTEM_ROLE = "ROLE_SYSTEM_CODE";

        /**
         * The spring security eval expression operator {@code or}.
         */
        public static final String HAS_AUTH_OR = " or ";

        /**
         * Spring security eval hasAnyRole expression to check if the spring context
         * contains system code role {@link SpringEvalExpressions#SYSTEM_ROLE}.
         */
        public static final String IS_SYSTEM_CODE = HAS_AUTH_PREFIX + SYSTEM_ROLE + HAS_AUTH_SUFFIX;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#UPDATE_TARGET} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_UPDATE_TARGET = HAS_AUTH_PREFIX + Authority.UPDATE_TARGET + HAS_AUTH_SUFFIX
                + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#SYSTEM_ADMIN} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_SYSTEM_ADMIN = HAS_AUTH_PREFIX + Authority.SYSTEM_ADMIN + HAS_AUTH_SUFFIX
                + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#READ_TARGET} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_READ_TARGET = HAS_AUTH_PREFIX + Authority.READ_TARGET + HAS_AUTH_SUFFIX
                + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#READ_TARGET_SEC_TOKEN} or
         * {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_READ_TARGET_SEC_TOKEN = HAS_AUTH_PREFIX + Authority.READ_TARGET_SEC_TOKEN
                + HAS_AUTH_SUFFIX + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#CREATE_TARGET} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_CREATE_TARGET = HAS_AUTH_PREFIX + Authority.CREATE_TARGET + HAS_AUTH_SUFFIX
                + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#DELETE_TARGET} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_DELETE_TARGET = HAS_AUTH_PREFIX + Authority.DELETE_TARGET + HAS_AUTH_SUFFIX
                + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#READ_REPOSITORY} and
         * {@link SpPermission#UPDATE_TARGET} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_READ_REPOSITORY_AND_UPDATE_TARGET = BRACKET_OPEN + HAS_AUTH_PREFIX
                + Authority.READ_REPOSITORY + HAS_AUTH_SUFFIX + HAS_AUTH_AND + HAS_AUTH_PREFIX + Authority.UPDATE_TARGET
                + HAS_AUTH_SUFFIX + BRACKET_CLOSE + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#CREATE_REPOSITORY} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_CREATE_REPOSITORY = HAS_AUTH_PREFIX + Authority.CREATE_REPOSITORY
                + HAS_AUTH_SUFFIX + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#DELETE_REPOSITORY} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_DELETE_REPOSITORY = HAS_AUTH_PREFIX + Authority.DELETE_REPOSITORY
                + HAS_AUTH_SUFFIX + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#READ_REPOSITORY} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_READ_REPOSITORY = HAS_AUTH_PREFIX + Authority.READ_REPOSITORY
                + HAS_AUTH_SUFFIX + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#UPDATE_REPOSITORY} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_UPDATE_REPOSITORY = HAS_AUTH_PREFIX + Authority.UPDATE_REPOSITORY
                + HAS_AUTH_SUFFIX + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#READ_REPOSITORY} and
         * {@link SpPermission#READ_TARGET} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_READ_REPOSITORY_AND_READ_TARGET = BRACKET_OPEN + HAS_AUTH_PREFIX
                + Authority.READ_REPOSITORY + HAS_AUTH_SUFFIX + HAS_AUTH_AND + HAS_AUTH_PREFIX + Authority.READ_TARGET
                + HAS_AUTH_SUFFIX + BRACKET_CLOSE + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#DOWNLOAD_REPOSITORY_ARTIFACT} or
         * {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_DOWNLOAD_ARTIFACT = HAS_AUTH_PREFIX + Authority.DOWNLOAD_REPOSITORY_ARTIFACT
                + HAS_AUTH_SUFFIX + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAnyRole expression to check if the spring context
         * contains the anoynmous role or the controller specific role
         * {@link SpringEvalExpressions#CONTROLLER_ROLE}.
         */
        public static final String IS_CONTROLLER = "hasAnyRole('" + CONTROLLER_ROLE_ANONYMOUS + "', '" + CONTROLLER_ROLE
                + "')";

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#CREATE_REPOSITORY} and
         * {@link SpPermission#CREATE_TARGET} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_CREATE_REPOSITORY_AND_CREATE_TARGET = BRACKET_OPEN + HAS_AUTH_PREFIX
                + Authority.CREATE_REPOSITORY + HAS_AUTH_SUFFIX + HAS_AUTH_AND + HAS_AUTH_PREFIX
                + Authority.CREATE_TARGET + HAS_AUTH_SUFFIX + BRACKET_CLOSE + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#READ_ROLLOUT} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_ROLLOUT_MANAGEMENT_READ = HAS_AUTH_PREFIX + Authority.READ_ROLLOUT
                + HAS_AUTH_SUFFIX + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#READ_ROLLOUT} and
         * {@link SpPermission#READ_TARGET} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_ROLLOUT_MANAGEMENT_READ_AND_TARGET_READ = BRACKET_OPEN + HAS_AUTH_PREFIX
                + Authority.READ_ROLLOUT + HAS_AUTH_SUFFIX + HAS_AUTH_AND + HAS_AUTH_PREFIX + Authority.READ_TARGET
                + HAS_AUTH_SUFFIX + BRACKET_CLOSE + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#CREATE_ROLLOUT} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_ROLLOUT_MANAGEMENT_CREATE = HAS_AUTH_PREFIX + Authority.CREATE_ROLLOUT
                + HAS_AUTH_SUFFIX + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#HANDLE_ROLLOUT} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_ROLLOUT_MANAGEMENT_HANDLE = HAS_AUTH_PREFIX + Authority.HANDLE_ROLLOUT
                + HAS_AUTH_SUFFIX + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#APPROVE_ROLLOUT} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_ROLLOUT_MANAGEMENT_APPROVE = HAS_AUTH_PREFIX + Authority.APPROVE_ROLLOUT
                + HAS_AUTH_SUFFIX + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#UPDATE_ROLLOUT} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_ROLLOUT_MANAGEMENT_UPDATE = HAS_AUTH_PREFIX + Authority.UPDATE_ROLLOUT
                + HAS_AUTH_SUFFIX + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#DELETE_ROLLOUT} or {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_ROLLOUT_MANAGEMENT_DELETE = HAS_AUTH_PREFIX + Authority.DELETE_ROLLOUT
                + HAS_AUTH_SUFFIX + HAS_AUTH_OR + IS_SYSTEM_CODE;

        /**
         * Spring security eval hasAuthority expression to check if spring context
         * contains {@link SpPermission#TENANT_CONFIGURATION} or
         * {@link #IS_SYSTEM_CODE}.
         */
        public static final String HAS_AUTH_TENANT_CONFIGURATION = HAS_AUTH_PREFIX + Authority.TENANT_CONFIGURATION
                + HAS_AUTH_SUFFIX + HAS_AUTH_OR + IS_SYSTEM_CODE;

        private SpringEvalExpressions() {
            // utility class
        }
    }
}
