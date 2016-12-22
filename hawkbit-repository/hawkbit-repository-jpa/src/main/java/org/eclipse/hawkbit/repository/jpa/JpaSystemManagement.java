/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.eclipse.hawkbit.cache.TenancyCacheManager;
import org.eclipse.hawkbit.repository.Constants;
import org.eclipse.hawkbit.repository.SystemManagement;
import org.eclipse.hawkbit.repository.TenantStatsManagement;
import org.eclipse.hawkbit.repository.jpa.configuration.MultiTenantJpaTransactionManager;
import org.eclipse.hawkbit.repository.jpa.model.JpaDistributionSetType;
import org.eclipse.hawkbit.repository.jpa.model.JpaSoftwareModuleType;
import org.eclipse.hawkbit.repository.jpa.model.JpaTenantMetaData;
import org.eclipse.hawkbit.repository.model.DistributionSetType;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.repository.model.TenantMetaData;
import org.eclipse.hawkbit.repository.report.model.SystemUsageReport;
import org.eclipse.hawkbit.security.SystemSecurityContext;
import org.eclipse.hawkbit.tenancy.TenantAware;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

/**
 * JPA implementation of {@link SystemManagement}.
 *
 */
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
@Validated
public class JpaSystemManagement implements CurrentTenantCacheKeyGenerator, SystemManagement {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TargetRepository targetRepository;

    @Autowired
    private TargetFilterQueryRepository targetFilterQueryRepository;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private DistributionSetRepository distributionSetRepository;

    @Autowired
    private SoftwareModuleRepository softwareModuleRepository;

    @Autowired
    private TenantMetaDataRepository tenantMetaDataRepository;

    @Autowired
    private DistributionSetTypeRepository distributionSetTypeRepository;

    @Autowired
    private SoftwareModuleTypeRepository softwareModuleTypeRepository;

    @Autowired
    private TargetTagRepository targetTagRepository;

    @Autowired
    private DistributionSetTagRepository distributionSetTagRepository;

    @Autowired
    private LocalArtifactRepository artifactRepository;

    @Autowired
    private TenantConfigurationRepository tenantConfigurationRepository;

    @Autowired
    private RolloutRepository rolloutRepository;

    @Autowired
    private RolloutGroupRepository rolloutGroupRepository;

    @Autowired
    private TenantAware tenantAware;

    @Autowired
    private TenantStatsManagement systemStatsManagement;

    @Autowired
    private TenancyCacheManager cacheManager;

    @Autowired
    private SystemManagementCacheKeyGenerator currentTenantCacheKeyGenerator;

    @Autowired
    private SystemSecurityContext systemSecurityContext;

    @Autowired
    private PlatformTransactionManager txManager;

    @Override
    public SystemUsageReport getSystemUsageStatistics() {

        BigDecimal sumOfArtifacts = (BigDecimal) entityManager
                .createNativeQuery(
                        "select SUM(file_size) from sp_artifact a INNER JOIN sp_base_software_module sm ON a.software_module = sm.id WHERE sm.deleted = 0")
                .getSingleResult();

        if (sumOfArtifacts == null) {
            sumOfArtifacts = new BigDecimal(0);
        }

        // we use native queries to punch through the tenant boundaries. This
        // has to be used with care!
        final Long targets = (Long) entityManager.createNativeQuery("SELECT COUNT(id) FROM sp_target")
                .getSingleResult();

        final Long artifacts = (Long) entityManager
                .createNativeQuery(
                        "SELECT COUNT(a.id) FROM sp_artifact a INNER JOIN sp_base_software_module sm ON a.software_module = sm.id WHERE sm.deleted = 0")
                .getSingleResult();

        final Long actions = (Long) entityManager.createNativeQuery("SELECT COUNT(id) FROM sp_action")
                .getSingleResult();

        final SystemUsageReport result = new SystemUsageReport(targets, artifacts, actions,
                sumOfArtifacts.setScale(0, BigDecimal.ROUND_HALF_UP).longValue());

        usageStatsPerTenant(result);

        return result;
    }

    private void usageStatsPerTenant(final SystemUsageReport report) {
        final List<String> tenants = findTenants();

        tenants.forEach(tenant -> tenantAware.runAsTenant(tenant, () -> {
            report.addTenantData(systemStatsManagement.getStatsOfTenant());
            return null;
        }));
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public KeyGenerator currentTenantKeyGenerator() {
        return currentTenantCacheKeyGenerator.currentTenantKeyGenerator();
    }

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public TenantMetaData getTenantMetadata(final String tenant) {
        final TenantMetaData result = tenantMetaDataRepository.findByTenantIgnoreCase(tenant);
        // Create if it does not exist
        if (result == null) {
            try {
                currentTenantCacheKeyGenerator.getCreateInitialTenant().set(tenant);
                return createInitialTenantMetaData(tenant);

            } finally {
                currentTenantCacheKeyGenerator.getCreateInitialTenant().remove();
            }
        }
        return result;
    }

    /**
     * Creating the initial tenant meta-data in a new transaction. Due the
     * {@link MultiTenantJpaTransactionManager} is using the current tenant to
     * set the necessary tenant discriminator to the query. This is not working
     * if we don't have a current tenant set. Due the
     * {@link #getTenantMetadata(String)} is maybe called without having a
     * current tenant we need to re-open a new transaction so the
     * {@link MultiTenantJpaTransactionManager} is called again and set the
     * tenant for this transaction.
     * 
     * @param tenant
     *            the tenant to be created
     * @return the initial created {@link TenantMetaData}
     */
    private TenantMetaData createInitialTenantMetaData(final String tenant) {
        final DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("initial-tenant-creation");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return systemSecurityContext.runAsSystemAsTenant(
                () -> new TransactionTemplate(txManager, def).execute(status -> tenantMetaDataRepository
                        .save(new JpaTenantMetaData(createStandardSoftwareDataSetup(), tenant))),
                tenant);
    }

    @Override
    public List<String> findTenants() {
        return tenantMetaDataRepository.findAll().stream().map(TenantMetaData::getTenant).collect(Collectors.toList());
    }

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public void deleteTenant(final String tenant) {
        cacheManager.evictCaches(tenant);
        tenantAware.runAsTenant(tenant, () -> {
            entityManager.setProperty(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, tenant.toUpperCase());
            tenantMetaDataRepository.deleteByTenantIgnoreCase(tenant);
            tenantConfigurationRepository.deleteByTenantIgnoreCase(tenant);
            targetRepository.deleteByTenantIgnoreCase(tenant);
            targetFilterQueryRepository.deleteByTenantIgnoreCase(tenant);
            actionRepository.deleteByTenantIgnoreCase(tenant);
            rolloutGroupRepository.deleteByTenantIgnoreCase(tenant);
            rolloutRepository.deleteByTenantIgnoreCase(tenant);
            artifactRepository.deleteByTenantIgnoreCase(tenant);
            targetTagRepository.deleteByTenantIgnoreCase(tenant);
            distributionSetTagRepository.deleteByTenantIgnoreCase(tenant);
            distributionSetRepository.deleteByTenantIgnoreCase(tenant);
            distributionSetTypeRepository.deleteByTenantIgnoreCase(tenant);
            softwareModuleRepository.deleteByTenantIgnoreCase(tenant);
            softwareModuleTypeRepository.deleteByTenantIgnoreCase(tenant);
            return null;
        });
    }

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public TenantMetaData getTenantMetadata() {
        if (tenantAware.getCurrentTenant() == null) {
            throw new IllegalStateException("Tenant not set");
        }

        return getTenantMetadata(tenantAware.getCurrentTenant());
    }

    @Override
    @Cacheable(value = "currentTenant", keyGenerator = "currentTenantKeyGenerator", cacheManager = "directCacheManager", unless = "#result == null")
    // set transaction to not supported, due we call this in
    // BaseEntity#prePersist methods
    // and it seems that JPA committing the transaction when executing this
    // transactional method,
    // which then leads that the BaseEntity#prePersist is called again to
    // persist the un-persisted
    // entity and we landing again in the #currentTenant() method
    // suspend the transaction here to do a read-request against the medata
    // table, when the current
    // tenant is not cached anyway already.
    @Transactional(propagation = Propagation.NOT_SUPPORTED, isolation = Isolation.READ_UNCOMMITTED)
    public String currentTenant() {
        final String initialTenantCreation = currentTenantCacheKeyGenerator.getCreateInitialTenant().get();
        if (initialTenantCreation == null) {
            final TenantMetaData findByTenant = tenantMetaDataRepository
                    .findByTenantIgnoreCase(tenantAware.getCurrentTenant());
            return findByTenant != null ? findByTenant.getTenant() : null;
        }
        return initialTenantCreation;
    }

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public TenantMetaData updateTenantMetadata(final Long defaultDsType) {
        final JpaTenantMetaData data = (JpaTenantMetaData) getTenantMetadata();

        data.setDefaultDsType(distributionSetTypeRepository.findOne(defaultDsType));

        return tenantMetaDataRepository.save(data);
    }

    private DistributionSetType createStandardSoftwareDataSetup() {
        final SoftwareModuleType app = softwareModuleTypeRepository
                .save(new JpaSoftwareModuleType(Constants.SMT_DEFAULT_APP_KEY, Constants.SMT_DEFAULT_APP_NAME,
                        "Application Addons", Integer.MAX_VALUE));
        final SoftwareModuleType os = softwareModuleTypeRepository.save(new JpaSoftwareModuleType(
                Constants.SMT_DEFAULT_OS_KEY, Constants.SMT_DEFAULT_OS_NAME, "Core firmware or operationg system", 1));

        // make sure the module types get their IDs
        entityManager.flush();

        distributionSetTypeRepository.save(new JpaDistributionSetType(Constants.DST_DEFAULT_OS_ONLY_KEY,
                Constants.DST_DEFAULT_OS_ONLY_NAME, "Default type with Firmware/OS only.").addMandatoryModuleType(os));

        return distributionSetTypeRepository.save(new JpaDistributionSetType(Constants.DST_DEFAULT_OS_WITH_APPS_KEY,
                Constants.DST_DEFAULT_OS_WITH_APPS_NAME, "Default type with Firmware/OS and optional app(s).")
                        .addMandatoryModuleType(os).addOptionalModuleType(app));
    }

    @Override
    public TenantMetaData getTenantMetadata(final Long tenantId) {
        return tenantMetaDataRepository.findOne(tenantId);
    }
}
