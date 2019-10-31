package org.eclipse.hawkbit.ui.common.data.proxies;

import java.io.Serializable;

public class ProxySystemConfigWindow implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private Long distributionSetTypeId;
    private Long repositoryConfigId;
    private Long rolloutConfigId;
    private Long authConfigId;
    private Long pollingConfigId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getDistributionSetTypeId() {
        return distributionSetTypeId;
    }

    public void setDistributionSetTypeId(Long distributionSetTypeId) {
        this.distributionSetTypeId = distributionSetTypeId;
    }

    public Long getRepositoryConfigId() {
        return repositoryConfigId;
    }

    public void setRepositoryConfigId(Long repositoryConfigId) {
        this.repositoryConfigId = repositoryConfigId;
    }

    public Long getRolloutConfigId() {
        return rolloutConfigId;
    }

    public void setRolloutConfigId(Long rolloutConfigId) {
        this.rolloutConfigId = rolloutConfigId;
    }

    public Long getAuthConfigId() {
        return authConfigId;
    }

    public void setAuthConfigId(Long authConfigId) {
        this.authConfigId = authConfigId;
    }

    public Long getPollingConfigId() {
        return pollingConfigId;
    }

    public void setPollingConfigId(Long pollingConfigId) {
        this.pollingConfigId = pollingConfigId;
    }
}
