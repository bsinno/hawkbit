/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.rollout.state;

import java.io.Serializable;
import java.util.Optional;

import org.eclipse.hawkbit.repository.model.RolloutGroup;

import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;

/**
 * Stores rollout management view UI state according to user interactions.
 *
 */
@VaadinSessionScope
@SpringComponent
public class RolloutManagementUIState implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The views that can be displayed
     */
    public enum Layout {
        ROLLOUTS, ROLLOUT_GROUPS, ROLLOUT_GROUP_TARGETS
    }

    private Layout currentLayout;
    private final RolloutLayoutUIState rolloutUIState;
    private final RolloutGroupLayoutUIState rolloutGroupUIState;
    private final RolloutGroupTargetLayoutUIState rolloutGroupTargetUIState;

    /**
     * constructor
     */
    public RolloutManagementUIState() {
        this.rolloutUIState = new RolloutLayoutUIState();
        this.rolloutGroupUIState = new RolloutGroupLayoutUIState();
        this.rolloutGroupTargetUIState = new RolloutGroupTargetLayoutUIState();
    }

    public Optional<Layout> getCurrentLayout() {
        return Optional.ofNullable(currentLayout);
    }

    public void setCurrentLayout(final Layout currentLayout) {
        this.currentLayout = currentLayout;
    }

    public RolloutGroupLayoutUIState getGroupUIState() {
        return rolloutGroupUIState;
    }

    public RolloutGroupTargetLayoutUIState getGroupTargetUIState() {
        return rolloutGroupTargetUIState;
    }

    public RolloutLayoutUIState getRolloutUIState() {
        return rolloutUIState;
    }

    // ---------------------------------------------------

    private String searchText;

    private Long rolloutId;

    private String rolloutName;

    private RolloutGroup rolloutGroup;

    private boolean showRolloutGroups;

    private boolean showRolloutGroupTargets;

    private boolean showRollOuts = true;

    private Long rolloutGroupTargetsTruncated;

    private long rolloutGroupTargetsTotalCount;

    private String rolloutDistributionSet;

    /**
     * @return the searchText
     */
    public Optional<String> getSearchText() {
        return Optional.ofNullable(searchText);
    }

    /**
     * @param searchText
     *            the searchText to set
     */
    public void setSearchText(final String searchText) {
        this.searchText = searchText;
    }

    /**
     * @return the rolloutId
     */
    public Optional<Long> getRolloutId() {
        return Optional.ofNullable(rolloutId);
    }

    /**
     * @param rolloutId
     *            the rolloutId to set
     */
    public void setRolloutId(final long rolloutId) {
        this.rolloutId = rolloutId;
    }

    /**
     * @return the rolloutGroup
     */
    public Optional<RolloutGroup> getRolloutGroup() {
        return Optional.ofNullable(rolloutGroup);
    }

    /**
     * @param rolloutGroup
     *            the rolloutGroup to set
     */
    public void setRolloutGroup(final RolloutGroup rolloutGroup) {
        this.rolloutGroup = rolloutGroup;
    }

    /**
     * @return the showRolloutGroups
     */
    public boolean isShowRolloutGroups() {
        return showRolloutGroups;
    }

    /**
     * @param showRolloutGroups
     *            the showRolloutGroups to set
     */
    public void setShowRolloutGroups(final boolean showRolloutGroups) {
        this.showRolloutGroups = showRolloutGroups;
    }

    /**
     * @return the showRolloutGroupTargets
     */
    public boolean isShowRolloutGroupTargets() {
        return showRolloutGroupTargets;
    }

    /**
     * @param showRolloutGroupTargets
     *            the showRolloutGroupTargets to set
     */
    public void setShowRolloutGroupTargets(final boolean showRolloutGroupTargets) {
        this.showRolloutGroupTargets = showRolloutGroupTargets;
    }

    /**
     * @return the showRollOuts
     */
    public boolean isShowRollOuts() {
        return showRollOuts;
    }

    /**
     * @param showRollOuts
     *            the showRollOuts to set
     */
    public void setShowRollOuts(final boolean showRollOuts) {
        this.showRollOuts = showRollOuts;
    }

    /**
     * @return the rolloutName
     */
    public Optional<String> getRolloutName() {
        return Optional.ofNullable(rolloutName);
    }

    /**
     * @param rolloutName
     *            the rolloutName to set
     */
    public void setRolloutName(final String rolloutName) {
        this.rolloutName = rolloutName;
    }

    /**
     * @return the rolloutGroupTargetsTruncated
     */
    public Long getRolloutGroupTargetsTruncated() {
        return rolloutGroupTargetsTruncated;
    }

    /**
     * @param rolloutGroupTargetsTruncated
     *            the rolloutGroupTargetsTruncated to set
     */
    public void setRolloutGroupTargetsTruncated(final long rolloutGroupTargetsTruncated) {
        this.rolloutGroupTargetsTruncated = rolloutGroupTargetsTruncated;
    }

    /**
     * @return the rolloutGroupTargetsTotalCount
     */
    public long getRolloutGroupTargetsTotalCount() {
        return rolloutGroupTargetsTotalCount;
    }

    /**
     * @param rolloutGroupTargetsTotalCount
     *            the rolloutGroupTargetsTotalCount to set
     */
    public void setRolloutGroupTargetsTotalCount(final long rolloutGroupTargetsTotalCount) {
        this.rolloutGroupTargetsTotalCount = rolloutGroupTargetsTotalCount;
    }

    /**
     * @return rolloutDistributionSet
     */
    public Optional<String> getRolloutDistributionSet() {
        return Optional.ofNullable(rolloutDistributionSet);
    }

    /**
     * 
     * @param rolloutDistributionSet
     *            the distribution set of the rollout
     */
    public void setRolloutDistributionSet(final String rolloutDistributionSet) {
        this.rolloutDistributionSet = rolloutDistributionSet;
    }
}
