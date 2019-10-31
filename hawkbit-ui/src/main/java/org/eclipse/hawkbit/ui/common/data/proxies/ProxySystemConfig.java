package org.eclipse.hawkbit.ui.common.data.proxies;

import java.util.Set;

public class ProxySystemConfig {

    private Set<String> distributionSetId;
    private Set<String> repositoryConfigId;
    private Set<String> rolloutConfigId;
    private Set<String> authConfigId;

    public Set<String> getDistributionSetId() {
        return distributionSetId;
    }

    public void setDistributionSetId(Set<String> distributionSetId) {
        this.distributionSetId = distributionSetId;
    }

    public Set<String> getRepositoryConfigId() {
        return repositoryConfigId;
    }

    public void setRepositoryConfigId(Set<String> repositoryConfigId) {
        this.repositoryConfigId = repositoryConfigId;
    }

    public Set<String> getRolloutConfigId() {
        return rolloutConfigId;
    }

    public void setRolloutConfigId(Set<String> rolloutConfigId) {
        this.rolloutConfigId = rolloutConfigId;
    }

    public Set<String> getAuthConfigId() {
        return authConfigId;
    }

    public void setAuthConfigId(Set<String> authConfigId) {
        this.authConfigId = authConfigId;
    }
}
