package org.eclipse.hawkbit.ui.common.data.proxies;

import java.io.Serializable;
import java.time.Duration;

import org.eclipse.hawkbit.ui.tenantconfiguration.repository.ActionAutoCleanupConfigurationItem.ActionStatusOption;

public class ProxySystemConfigWindow implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private Long distributionSetTypeId;
    private Long repositoryConfigId;
    private Long rolloutConfigId;
    private Long caRootAuthorityId;
    private String caRootAuthority;
    private String gatewaySecurityToken;
    private ActionStatusOption actionCleanupStatus;
    private boolean pollingOverdue;
    private Duration pollingOverdueDuration;
    private boolean pollingTime;
    private Duration pollingTimeDuration;
    private String actionExpiryDays;
    private boolean rolloutApproval;
    private boolean actionAutoclose;
    private boolean actionAutocleanup;
    private boolean multiAssignments;
    private boolean certificateAuth;
    private boolean targetSecToken;
    private boolean gatewaySecToken;
    private boolean downloadAnonymous;
    private Long authConfigId;
    private Long pollingConfigId;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCaRootAuthority() {
        return caRootAuthority;
    }

    public void setCaRootAuthority(final String caRootAuthority) {
        this.caRootAuthority = caRootAuthority;
    }

    public String getGatewaySecurityToken() {
        return gatewaySecurityToken;
    }

    public void setGatewaySecurityToken(final String gatewaySecurityToken) {
        this.gatewaySecurityToken = gatewaySecurityToken;
    }

    public boolean isPollingTime() {
        return pollingTime;
    }

    public void setPollingTime(final boolean pollingTime) {
        this.pollingTime = pollingTime;
    }

    public Duration getPollingTimeDuration() {
        return pollingTimeDuration;
    }

    public void setPollingTimeDuration(final Duration pollingTimeDuration) {
        this.pollingTimeDuration = pollingTimeDuration;
    }

    public boolean isPollingOverdue() {
        return pollingOverdue;
    }

    public void setPollingOverdue(final boolean pollingOverdue) {
        this.pollingOverdue = pollingOverdue;
    }

    public Duration getPollingOverdueDuration() {
        return pollingOverdueDuration;
    }

    public void setPollingOverdueDuration(final Duration pollingOverdueDuration) {
        this.pollingOverdueDuration = pollingOverdueDuration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Long getDistributionSetTypeId() {
        return distributionSetTypeId;
    }

    public String getActionExpiryDays() {
        return actionExpiryDays;
    }

    public void setActionExpiryDays(final String actionExpiryDays) {
        this.actionExpiryDays = actionExpiryDays;
    }

    public ActionStatusOption getActionCleanupStatus() {
        return actionCleanupStatus;
    }

    public void setActionCleanupStatus(final ActionStatusOption actionCleanupStatus) {
        this.actionCleanupStatus = actionCleanupStatus;
    }

    public Long getCaRootAuthorityId() {
        return caRootAuthorityId;
    }

    public void setCaRootAuthorityId(final Long caRootAuthorityId) {
        this.caRootAuthorityId = caRootAuthorityId;
    }

    public boolean isCertificateAuth() {
        return certificateAuth;
    }

    public void setCertificateAuth(final boolean certificateAuth) {
        this.certificateAuth = certificateAuth;
    }

    public boolean isTargetSecToken() {
        return targetSecToken;
    }

    public void setTargetSecToken(final boolean targetSecToken) {
        this.targetSecToken = targetSecToken;
    }

    public boolean isGatewaySecToken() {
        return gatewaySecToken;
    }

    public void setGatewaySecToken(final boolean gatewaySecToken) {
        this.gatewaySecToken = gatewaySecToken;
    }

    public boolean isDownloadAnonymous() {
        return downloadAnonymous;
    }

    public void setDownloadAnonymous(final boolean downloadAnonymous) {
        this.downloadAnonymous = downloadAnonymous;
    }

    public boolean isActionAutocleanup() {
        return actionAutocleanup;
    }

    public void setActionAutocleanup(final boolean actionAutocleanup) {
        this.actionAutocleanup = actionAutocleanup;
    }

    public boolean isMultiAssignments() {
        return multiAssignments;
    }

    public void setMultiAssignments(final boolean multiAssignments) {
        this.multiAssignments = multiAssignments;
    }

    public boolean isActionAutoclose() {
        return actionAutoclose;
    }

    public void setActionAutoclose(final boolean actionAutoclose) {
        this.actionAutoclose = actionAutoclose;
    }

    public boolean isRolloutApproval() {
        return rolloutApproval;
    }

    public void setRolloutApproval(final boolean rolloutApproval) {
        this.rolloutApproval = rolloutApproval;
    }

    public void setDistributionSetTypeId(final Long distributionSetTypeId) {
        this.distributionSetTypeId = distributionSetTypeId;
    }

    public Long getRepositoryConfigId() {
        return repositoryConfigId;
    }

    public void setRepositoryConfigId(final Long repositoryConfigId) {
        this.repositoryConfigId = repositoryConfigId;
    }

    public Long getRolloutConfigId() {
        return rolloutConfigId;
    }

    public void setRolloutConfigId(final Long rolloutConfigId) {
        this.rolloutConfigId = rolloutConfigId;
    }

    public Long getAuthConfigId() {
        return authConfigId;
    }

    public void setAuthConfigId(final Long authConfigId) {
        this.authConfigId = authConfigId;
    }

    public Long getPollingConfigId() {
        return pollingConfigId;
    }

    public void setPollingConfigId(final Long pollingConfigId) {
        this.pollingConfigId = pollingConfigId;
    }
}
