/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.ConstraintDeclarationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.hawkbit.repository.DeploymentManagement;
import org.eclipse.hawkbit.repository.DistributionSetManagement;
import org.eclipse.hawkbit.repository.RolloutFields;
import org.eclipse.hawkbit.repository.RolloutGroupManagement;
import org.eclipse.hawkbit.repository.RolloutManagement;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.builder.GenericRolloutUpdate;
import org.eclipse.hawkbit.repository.builder.RolloutCreate;
import org.eclipse.hawkbit.repository.builder.RolloutGroupCreate;
import org.eclipse.hawkbit.repository.builder.RolloutUpdate;
import org.eclipse.hawkbit.repository.event.remote.entity.RolloutGroupCreatedEvent;
import org.eclipse.hawkbit.repository.exception.ConstraintViolationException;
import org.eclipse.hawkbit.repository.exception.EntityAlreadyExistsException;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.RolloutIllegalStateException;
import org.eclipse.hawkbit.repository.jpa.executor.AfterTransactionCommitExecutor;
import org.eclipse.hawkbit.repository.jpa.model.JpaAction;
import org.eclipse.hawkbit.repository.jpa.model.JpaRollout;
import org.eclipse.hawkbit.repository.jpa.model.JpaRolloutGroup;
import org.eclipse.hawkbit.repository.jpa.model.RolloutTargetGroup;
import org.eclipse.hawkbit.repository.jpa.rollout.condition.RolloutGroupActionEvaluator;
import org.eclipse.hawkbit.repository.jpa.rollout.condition.RolloutGroupConditionEvaluator;
import org.eclipse.hawkbit.repository.jpa.rsql.RSQLUtility;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.Action.Status;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.Rollout;
import org.eclipse.hawkbit.repository.model.Rollout.RolloutStatus;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupErrorCondition;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupStatus;
import org.eclipse.hawkbit.repository.model.RolloutGroup.RolloutGroupSuccessCondition;
import org.eclipse.hawkbit.repository.model.RolloutGroupConditions;
import org.eclipse.hawkbit.repository.model.RolloutGroupsValidation;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TotalTargetCountActionStatus;
import org.eclipse.hawkbit.repository.model.TotalTargetCountStatus;
import org.eclipse.hawkbit.repository.rsql.VirtualPropertyReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.validation.annotation.Validated;

/**
 * JPA implementation of {@link RolloutManagement}.
 */
@Validated
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
public class JpaRolloutManagement implements RolloutManagement {
    private static final Logger LOGGER = LoggerFactory.getLogger(RolloutManagement.class);

    /**
     * Max amount of targets that are handled in one transaction.
     */
    private static final int TRANSACTION_TARGETS = 1000;

    @Autowired
    private RolloutRepository rolloutRepository;

    @Autowired
    private TargetManagement targetManagement;

    @Autowired
    private RolloutGroupRepository rolloutGroupRepository;

    @Autowired
    private DeploymentManagement deploymentManagement;

    @Autowired
    private RolloutTargetGroupRepository rolloutTargetGroupRepository;

    @Autowired
    private RolloutGroupManagement rolloutGroupManagement;

    @Autowired
    private DistributionSetManagement distributionSetManagement;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private NoCountPagingRepository criteriaNoCountDao;

    @Autowired
    private PlatformTransactionManager txManager;

    @Autowired
    private VirtualPropertyReplacer virtualPropertyReplacer;

    @Autowired
    private AfterTransactionCommitExecutor afterCommit;

    @Override
    public Page<Rollout> findAll(final Pageable pageable) {
        return RolloutHelper.convertPage(rolloutRepository.findAll(pageable), pageable);
    }

    @Override
    public Page<Rollout> findAllByPredicate(final String rsqlParam, final Pageable pageable) {
        final Specification<JpaRollout> specification = RSQLUtility.parse(rsqlParam, RolloutFields.class,
                virtualPropertyReplacer);

        final Page<JpaRollout> findAll = rolloutRepository.findAll(specification, pageable);
        return RolloutHelper.convertPage(findAll, pageable);
    }

    @Override
    public Optional<Rollout> findRolloutById(final Long rolloutId) {
        return Optional.ofNullable(rolloutRepository.findOne(rolloutId));
    }

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public Rollout createRollout(final RolloutCreate rollout, final int amountGroup,
            final RolloutGroupConditions conditions) {
        RolloutHelper.verifyRolloutGroupParameter(amountGroup);
        final JpaRollout savedRollout = createRollout((JpaRollout) rollout.build());
        return createRolloutGroups(amountGroup, conditions, savedRollout);
    }

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public Rollout createRollout(final RolloutCreate rollout, final List<RolloutGroupCreate> groups,
            final RolloutGroupConditions conditions) {
        RolloutHelper.verifyRolloutGroupParameter(groups.size());
        final JpaRollout savedRollout = createRollout((JpaRollout) rollout.build());
        return createRolloutGroups(groups, conditions, savedRollout);
    }

    private JpaRollout createRollout(final JpaRollout rollout) {
        final Optional<Rollout> existingRollout = rolloutRepository.findByName(rollout.getName());
        if (existingRollout.isPresent()) {
            throw new EntityAlreadyExistsException(existingRollout.get().getName());
        }

        final Long totalTargets = targetManagement.countTargetByTargetFilterQuery(rollout.getTargetFilterQuery());
        if (totalTargets == 0) {
            throw new ConstraintViolationException("Rollout does not match any existing targets");
        }
        rollout.setTotalTargets(totalTargets);
        return rolloutRepository.save(rollout);
    }

    private Rollout createRolloutGroups(final int amountOfGroups, final RolloutGroupConditions conditions,
            final Rollout rollout) {
        RolloutHelper.verifyRolloutInStatus(rollout, RolloutStatus.CREATING);
        RolloutHelper.verifyRolloutGroupConditions(conditions);

        final JpaRollout savedRollout = (JpaRollout) rollout;

        RolloutGroup lastSavedGroup = null;
        for (int i = 0; i < amountOfGroups; i++) {
            final String nameAndDesc = "group-" + (i + 1);
            final JpaRolloutGroup group = new JpaRolloutGroup();
            group.setName(nameAndDesc);
            group.setDescription(nameAndDesc);
            group.setRollout(savedRollout);
            group.setParent(lastSavedGroup);
            group.setStatus(RolloutGroupStatus.CREATING);

            group.setSuccessCondition(conditions.getSuccessCondition());
            group.setSuccessConditionExp(conditions.getSuccessConditionExp());

            group.setSuccessAction(conditions.getSuccessAction());
            group.setSuccessActionExp(conditions.getSuccessActionExp());

            group.setErrorCondition(conditions.getErrorCondition());
            group.setErrorConditionExp(conditions.getErrorConditionExp());

            group.setErrorAction(conditions.getErrorAction());
            group.setErrorActionExp(conditions.getErrorActionExp());

            group.setTargetPercentage(1.0F / (amountOfGroups - i) * 100);

            lastSavedGroup = rolloutGroupRepository.save(group);
            publishRolloutGroupCreatedEventAfterCommit(lastSavedGroup);

        }

        savedRollout.setRolloutGroupsCreated(amountOfGroups);
        return rolloutRepository.save(savedRollout);
    }

    private Rollout createRolloutGroups(final List<RolloutGroupCreate> groupList,
            final RolloutGroupConditions conditions, final Rollout rollout) {
        RolloutHelper.verifyRolloutInStatus(rollout, RolloutStatus.CREATING);
        final JpaRollout savedRollout = (JpaRollout) rollout;

        // Preparing the groups
        final List<RolloutGroup> groups = groupList.stream()
                .map(group -> RolloutHelper.prepareRolloutGroupWithDefaultConditions(group, conditions))
                .collect(Collectors.toList());
        groups.forEach(RolloutHelper::verifyRolloutGroupHasConditions);

        RolloutHelper.verifyRemainingTargets(
                calculateRemainingTargets(groups, savedRollout.getTargetFilterQuery(), savedRollout.getCreatedAt()));

        // Persisting the groups
        RolloutGroup lastSavedGroup = null;
        for (final RolloutGroup srcGroup : groups) {
            final JpaRolloutGroup group = new JpaRolloutGroup();
            group.setName(srcGroup.getName());
            group.setDescription(srcGroup.getDescription());
            group.setRollout(savedRollout);
            group.setParent(lastSavedGroup);
            group.setStatus(RolloutGroupStatus.CREATING);

            group.setTargetPercentage(srcGroup.getTargetPercentage());
            if (srcGroup.getTargetFilterQuery() != null) {
                group.setTargetFilterQuery(srcGroup.getTargetFilterQuery());
            } else {
                group.setTargetFilterQuery("");
            }

            group.setSuccessCondition(srcGroup.getSuccessCondition());
            group.setSuccessConditionExp(srcGroup.getSuccessConditionExp());

            group.setSuccessAction(srcGroup.getSuccessAction());
            group.setSuccessActionExp(srcGroup.getSuccessActionExp());

            group.setErrorCondition(srcGroup.getErrorCondition());
            group.setErrorConditionExp(srcGroup.getErrorConditionExp());

            group.setErrorAction(srcGroup.getErrorAction());
            group.setErrorActionExp(srcGroup.getErrorActionExp());

            lastSavedGroup = rolloutGroupRepository.save(group);
            publishRolloutGroupCreatedEventAfterCommit(lastSavedGroup);
        }

        savedRollout.setRolloutGroupsCreated(groups.size());
        return rolloutRepository.save(savedRollout);
    }

    private void publishRolloutGroupCreatedEventAfterCommit(final RolloutGroup group) {
        afterCommit
                .afterCommit(() -> eventPublisher.publishEvent(new RolloutGroupCreatedEvent(group, context.getId())));
    }

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public void fillRolloutGroupsWithTargets(final Long rolloutId) {
        final JpaRollout rollout = getRolloutAndThrowExceptionIfNotFound(rolloutId);

        RolloutHelper.verifyRolloutInStatus(rollout, RolloutStatus.CREATING);

        final List<RolloutGroup> rolloutGroups = RolloutHelper.getOrderedGroups(rollout);
        int readyGroups = 0;
        int totalTargets = 0;
        for (final RolloutGroup group : rolloutGroups) {
            if (group.getStatus() != RolloutGroupStatus.CREATING) {
                readyGroups++;
                totalTargets += group.getTotalTargets();
                continue;
            }

            final RolloutGroup filledGroup = fillRolloutGroupWithTargets(rollout, group);
            if (filledGroup.getStatus() == RolloutGroupStatus.READY) {
                readyGroups++;
                totalTargets += filledGroup.getTotalTargets();
            }
        }

        // When all groups are ready the rollout status can be changed to be
        // ready, too.
        if (readyGroups == rolloutGroups.size()) {
            rollout.setStatus(RolloutStatus.READY);
            rollout.setLastCheck(0);
            rollout.setTotalTargets(totalTargets);
            rolloutRepository.save(rollout);
        }
    }

    private RolloutGroup fillRolloutGroupWithTargets(final Rollout rollout, final RolloutGroup group1) {
        RolloutHelper.verifyRolloutInStatus(rollout, RolloutStatus.CREATING);

        final JpaRolloutGroup group = (JpaRolloutGroup) group1;

        final String baseFilter = RolloutHelper.getTargetFilterQuery(rollout);
        final String groupTargetFilter;
        if (StringUtils.isEmpty(group.getTargetFilterQuery())) {
            groupTargetFilter = baseFilter;
        } else {
            groupTargetFilter = baseFilter + ";" + group.getTargetFilterQuery();
        }

        final List<Long> readyGroups = RolloutHelper.getGroupsByStatusIncludingGroup(rollout, RolloutGroupStatus.READY,
                group);

        final long targetsInGroupFilter = targetManagement
                .countAllTargetsByTargetFilterQueryAndNotInRolloutGroups(readyGroups, groupTargetFilter);
        final long expectedInGroup = Math.round(group.getTargetPercentage() / 100 * (double) targetsInGroupFilter);
        final long currentlyInGroup = rolloutTargetGroupRepository.countByRolloutGroup(group);

        // Switch the Group status to READY, when there are enough Targets in
        // the Group
        if (currentlyInGroup >= expectedInGroup) {
            group.setStatus(RolloutGroupStatus.READY);
            return rolloutGroupRepository.save(group);
        }

        long targetsLeftToAdd = expectedInGroup - currentlyInGroup;

        try {
            do {
                // Add up to TRANSACTION_TARGETS of the left targets
                // In case a TransactionException is thrown this loop aborts
                targetsLeftToAdd -= assignTargetsToGroupInNewTransaction(rollout, group, groupTargetFilter,
                        Math.min(TRANSACTION_TARGETS, targetsLeftToAdd));
            } while (targetsLeftToAdd > 0);

            group.setStatus(RolloutGroupStatus.READY);
            group.setTotalTargets(rolloutTargetGroupRepository.countByRolloutGroup(group).intValue());
            return rolloutGroupRepository.save(group);

        } catch (final TransactionException e) {
            LOGGER.warn("Transaction assigning Targets to RolloutGroup failed", e);
            return group;
        }
    }

    private int runInNewCountingTransaction(final String transactionName, final TransactionCallback<Integer> action) {
        final DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName(transactionName);
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return new TransactionTemplate(txManager, def).execute(action);
    }

    private Integer assignTargetsToGroupInNewTransaction(final Rollout rollout, final RolloutGroup group,
            final String targetFilter, final long limit) {

        return runInNewCountingTransaction("assignTargetsToRolloutGroup", status -> {
            final PageRequest pageRequest = new PageRequest(0, Math.toIntExact(limit));
            final List<Long> readyGroups = RolloutHelper.getGroupsByStatusIncludingGroup(rollout,
                    RolloutGroupStatus.READY, group);
            final Page<Target> targets = targetManagement
                    .findAllTargetsByTargetFilterQueryAndNotInRolloutGroups(pageRequest, readyGroups, targetFilter);

            createAssignmentOfTargetsToGroup(targets, group);

            return targets.getNumberOfElements();
        });
    }

    private void createAssignmentOfTargetsToGroup(final Page<Target> targets, final RolloutGroup group) {
        targets.forEach(target -> rolloutTargetGroupRepository.save(new RolloutTargetGroup(group, target)));
    }

    private long calculateRemainingTargets(final List<RolloutGroup> groups, final String targetFilter,
            final Long createdAt) {
        final String baseFilter = RolloutHelper.getTargetFilterQuery(targetFilter, createdAt);
        final long totalTargets = targetManagement.countTargetByTargetFilterQuery(baseFilter);
        if (totalTargets == 0) {
            throw new ConstraintDeclarationException("Rollout target filter does not match any targets");
        }

        final RolloutGroupsValidation validation = validateTargetsInGroups(groups, baseFilter, totalTargets);

        return totalTargets - validation.getTargetsInGroups();
    }

    @Override
    @Async
    public ListenableFuture<RolloutGroupsValidation> validateTargetsInGroups(final List<RolloutGroupCreate> groups,
            final String targetFilter, final Long createdAt) {

        final String baseFilter = RolloutHelper.getTargetFilterQuery(targetFilter, createdAt);
        final long totalTargets = targetManagement.countTargetByTargetFilterQuery(baseFilter);
        if (totalTargets == 0) {
            throw new ConstraintDeclarationException("Rollout target filter does not match any targets");
        }

        return new AsyncResult<>(validateTargetsInGroups(
                groups.stream().map(RolloutGroupCreate::build).collect(Collectors.toList()), baseFilter, totalTargets));
    }

    private RolloutGroupsValidation validateTargetsInGroups(final List<RolloutGroup> groups, final String baseFilter,
            final long totalTargets) {
        final List<Long> groupTargetCounts = new ArrayList<>(groups.size());
        final Map<String, Long> targetFilterCounts = groups.stream()
                .map(group -> RolloutHelper.getGroupTargetFilter(baseFilter, group)).distinct()
                .collect(Collectors.toMap(Function.identity(), targetManagement::countTargetByTargetFilterQuery));

        long unusedTargetsCount = 0;

        for (int i = 0; i < groups.size(); i++) {
            final RolloutGroup group = groups.get(i);
            final String groupTargetFilter = RolloutHelper.getGroupTargetFilter(baseFilter, group);
            RolloutHelper.verifyRolloutGroupTargetPercentage(group.getTargetPercentage());

            final long targetsInGroupFilter = targetFilterCounts.get(groupTargetFilter);
            final long overlappingTargets = countOverlappingTargetsWithPreviousGroups(baseFilter, groups, group, i,
                    targetFilterCounts);

            final long realTargetsInGroup;
            // Assume that targets which were not used in the previous groups
            // are used in this group
            if (overlappingTargets > 0 && unusedTargetsCount > 0) {
                realTargetsInGroup = targetsInGroupFilter - overlappingTargets + unusedTargetsCount;
                unusedTargetsCount = 0;
            } else {
                realTargetsInGroup = targetsInGroupFilter - overlappingTargets;
            }

            final long reducedTargetsInGroup = Math
                    .round(group.getTargetPercentage() / 100 * (double) realTargetsInGroup);
            groupTargetCounts.add(reducedTargetsInGroup);
            unusedTargetsCount += realTargetsInGroup - reducedTargetsInGroup;

        }

        return new RolloutGroupsValidation(totalTargets, groupTargetCounts);
    }

    private long countOverlappingTargetsWithPreviousGroups(final String baseFilter, final List<RolloutGroup> groups,
            final RolloutGroup group, final int groupIndex, final Map<String, Long> targetFilterCounts) {
        // there can't be overlapping targets in the first group
        if (groupIndex == 0) {
            return 0;
        }
        final List<RolloutGroup> previousGroups = groups.subList(0, groupIndex);
        final String overlappingTargetsFilter = RolloutHelper.getOverlappingWithGroupsTargetFilter(baseFilter,
                previousGroups, group);

        if (targetFilterCounts.containsKey(overlappingTargetsFilter)) {
            return targetFilterCounts.get(overlappingTargetsFilter);
        } else {
            final long overlappingTargets = targetManagement.countTargetByTargetFilterQuery(overlappingTargetsFilter);
            targetFilterCounts.put(overlappingTargetsFilter, overlappingTargets);
            return overlappingTargets;
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public Rollout startRollout(final Long rolloutId) {
        final JpaRollout rollout = getRolloutAndThrowExceptionIfNotFound(rolloutId);
        RolloutHelper.checkIfRolloutCanStarted(rollout, rollout);
        rollout.setStatus(RolloutStatus.STARTING);
        rollout.setLastCheck(0);
        return rolloutRepository.save(rollout);
    }

    private void startFirstRolloutGroup(final Rollout rollout) {
        RolloutHelper.verifyRolloutInStatus(rollout, RolloutStatus.STARTING);
        final JpaRollout jpaRollout = (JpaRollout) rollout;

        final List<JpaRolloutGroup> rolloutGroups = rolloutGroupRepository.findByRolloutOrderByIdAsc(jpaRollout);
        final JpaRolloutGroup rolloutGroup = rolloutGroups.get(0);
        if (rolloutGroup.getParent() != null) {
            throw new RolloutIllegalStateException("First Group is not the first group.");
        }

        deploymentManagement.startScheduledActionsByRolloutGroupParent(rollout, null);

        rolloutGroup.setStatus(RolloutGroupStatus.RUNNING);
        rolloutGroupRepository.save(rolloutGroup);

        jpaRollout.setStatus(RolloutStatus.RUNNING);
        jpaRollout.setLastCheck(0);
        rolloutRepository.save(jpaRollout);
    }

    private boolean ensureAllGroupsAreScheduled(final Rollout rollout) {
        RolloutHelper.verifyRolloutInStatus(rollout, RolloutStatus.STARTING);
        final JpaRollout jpaRollout = (JpaRollout) rollout;

        final List<JpaRolloutGroup> groupsToBeScheduled = rolloutGroupRepository.findByRolloutAndStatus(rollout,
                RolloutGroupStatus.READY);
        final long scheduledGroups = groupsToBeScheduled.stream()
                .filter(group -> scheduleRolloutGroup(jpaRollout, group)).count();

        return scheduledGroups == groupsToBeScheduled.size();
    }

    /**
     * Schedules a group of the rollout. Scheduled Actions are created to
     * achieve this. The creation of those Actions is allowed to fail.
     */
    private boolean scheduleRolloutGroup(final JpaRollout rollout, final JpaRolloutGroup group) {
        final long targetsInGroup = rolloutTargetGroupRepository.countByRolloutGroup(group);
        final long countOfActions = actionRepository.countByRolloutAndRolloutGroup(rollout, group);

        long actionsLeft = targetsInGroup - countOfActions;
        if (actionsLeft > 0) {
            actionsLeft -= createActionsForRolloutGroup(rollout, group);
        }

        if (actionsLeft <= 0) {
            group.setStatus(RolloutGroupStatus.SCHEDULED);
            rolloutGroupRepository.save(group);
            return true;
        }
        return false;
    }

    private long createActionsForRolloutGroup(final Rollout rollout, final RolloutGroup group) {
        long totalActionsCreated = 0;
        try {
            long actionsCreated;
            do {
                actionsCreated = createActionsForTargetsInNewTransaction(rollout.getId(), group.getId(),
                        TRANSACTION_TARGETS);
                totalActionsCreated += actionsCreated;
            } while (actionsCreated > 0);

        } catch (final TransactionException e) {
            LOGGER.warn("Transaction assigning Targets to RolloutGroup failed", e);
            return 0;
        }
        return totalActionsCreated;
    }

    private Integer createActionsForTargetsInNewTransaction(final long rolloutId, final long groupId, final int limit) {
        return runInNewCountingTransaction("createActionsForTargets", status -> {
            final PageRequest pageRequest = new PageRequest(0, limit);
            final Rollout rollout = rolloutRepository.findOne(rolloutId);
            final RolloutGroup group = rolloutGroupRepository.findOne(groupId);

            final DistributionSet distributionSet = rollout.getDistributionSet();
            final ActionType actionType = rollout.getActionType();
            final long forceTime = rollout.getForcedTime();

            final Page<Target> targets = targetManagement.findAllTargetsInRolloutGroupWithoutAction(pageRequest,
                    groupId);
            if (targets.getTotalElements() > 0) {
                createScheduledAction(targets.getContent(), distributionSet, actionType, forceTime, rollout, group);
            }

            return targets.getNumberOfElements();
        });
    }

    /**
     * Creates an action entry into the action repository. In case of existing
     * scheduled actions the scheduled actions gets canceled. A scheduled action
     * is created in-active.
     */
    private void createScheduledAction(final Collection<Target> targets, final DistributionSet distributionSet,
            final ActionType actionType, final Long forcedTime, final Rollout rollout,
            final RolloutGroup rolloutGroup) {
        // cancel all current scheduled actions for this target. E.g. an action
        // is already scheduled and a next action is created then cancel the
        // current scheduled action to cancel. E.g. a new scheduled action is
        // created.
        final List<Long> targetIds = targets.stream().map(Target::getId).collect(Collectors.toList());
        actionRepository.switchStatus(Action.Status.CANCELED, targetIds, false, Action.Status.SCHEDULED);
        targets.forEach(target -> {
            final JpaAction action = new JpaAction();
            action.setTarget(target);
            action.setActive(false);
            action.setDistributionSet(distributionSet);
            action.setActionType(actionType);
            action.setForcedTime(forcedTime);
            action.setStatus(Status.SCHEDULED);
            action.setRollout(rollout);
            action.setRolloutGroup(rolloutGroup);
            actionRepository.save(action);
        });
    }

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public void pauseRollout(final Long rolloutId) {
        final JpaRollout rollout = getRolloutAndThrowExceptionIfNotFound(rolloutId);
        if (rollout.getStatus() != RolloutStatus.RUNNING) {
            throw new RolloutIllegalStateException("Rollout can only be paused in state running but current state is "
                    + rollout.getStatus().name().toLowerCase());
        }
        // setting the complete rollout only in paused state. This is sufficient
        // due the currently running groups will be completed and new groups are
        // not started until rollout goes back to running state again. The
        // periodically check for running rollouts will skip rollouts in pause
        // state.
        rollout.setStatus(RolloutStatus.PAUSED);
        rolloutRepository.save(rollout);
    }

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public void resumeRollout(final Long rolloutId) {
        final JpaRollout rollout = getRolloutAndThrowExceptionIfNotFound(rolloutId);
        if (!(RolloutStatus.PAUSED.equals(rollout.getStatus()))) {
            throw new RolloutIllegalStateException("Rollout can only be resumed in state paused but current state is "
                    + rollout.getStatus().name().toLowerCase());
        }
        rollout.setStatus(RolloutStatus.RUNNING);
        rolloutRepository.save(rollout);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public void checkRunningRollouts(final long delayBetweenChecks) {
        final List<JpaRollout> rolloutsToCheck = getRolloutsToCheckForStatus(delayBetweenChecks, RolloutStatus.RUNNING);
        if (rolloutsToCheck.isEmpty()) {
            return;
        }

        LOGGER.info("Found {} running rollouts to check", rolloutsToCheck.size());

        for (final JpaRollout rollout : rolloutsToCheck) {
            LOGGER.debug("Checking rollout {}", rollout);

            final List<JpaRolloutGroup> rolloutGroupsRunning = rolloutGroupRepository.findByRolloutAndStatus(rollout,
                    RolloutGroupStatus.RUNNING);

            if (rolloutGroupsRunning.isEmpty()) {
                // no running rollouts, probably there was an error
                // somewhere at the latest group. And the latest group has
                // been switched from running into error state. So we need
                // to find the latest group which
                executeLatestRolloutGroup(rollout);
            } else {
                LOGGER.debug("Rollout {} has {} running groups", rollout.getId(), rolloutGroupsRunning.size());
                executeRolloutGroups(rollout, rolloutGroupsRunning);
            }

            if (isRolloutComplete(rollout)) {
                LOGGER.info("Rollout {} is finished, setting finished status", rollout);
                rollout.setStatus(RolloutStatus.FINISHED);
                rolloutRepository.save(rollout);
            }
        }
    }

    private void executeRolloutGroups(final JpaRollout rollout, final List<JpaRolloutGroup> rolloutGroups) {
        for (final JpaRolloutGroup rolloutGroup : rolloutGroups) {

            final long targetCount = countTargetsFrom(rolloutGroup);
            if (rolloutGroup.getTotalTargets() != targetCount) {
                updateTotalTargetCount(rolloutGroup, targetCount);
            }

            // error state check, do we need to stop the whole
            // rollout because of error?
            final boolean isError = checkErrorState(rollout, rolloutGroup);
            if (isError) {
                LOGGER.info("Rollout {} {} has error, calling error action", rollout.getName(), rollout.getId());
                callErrorAction(rollout, rolloutGroup);
            } else {
                // not in error so check finished state, do we need to
                // start the next group?
                final RolloutGroupSuccessCondition finishedCondition = rolloutGroup.getSuccessCondition();
                checkFinishCondition(rollout, rolloutGroup, finishedCondition);
                if (isRolloutGroupComplete(rollout, rolloutGroup)) {
                    rolloutGroup.setStatus(RolloutGroupStatus.FINISHED);
                    rolloutGroupRepository.save(rolloutGroup);
                }
            }
        }
    }

    private void updateTotalTargetCount(final JpaRolloutGroup rolloutGroup, final long countTargetsOfRolloutGroup) {
        final JpaRollout jpaRollout = (JpaRollout) rolloutGroup.getRollout();
        final long updatedTargetCount = jpaRollout.getTotalTargets()
                - (rolloutGroup.getTotalTargets() - countTargetsOfRolloutGroup);
        jpaRollout.setTotalTargets(updatedTargetCount);
        rolloutGroup.setTotalTargets((int) countTargetsOfRolloutGroup);
        rolloutRepository.save(jpaRollout);
        rolloutGroupRepository.save(rolloutGroup);
    }

    private long countTargetsFrom(final JpaRolloutGroup rolloutGroup) {
        return rolloutGroupManagement.countTargetsOfRolloutsGroup(rolloutGroup.getId());
    }

    private void executeLatestRolloutGroup(final JpaRollout rollout) {
        final List<JpaRolloutGroup> latestRolloutGroup = rolloutGroupRepository
                .findByRolloutAndStatusNotOrderByIdDesc(rollout, RolloutGroupStatus.SCHEDULED);
        if (latestRolloutGroup.isEmpty()) {
            return;
        }
        executeRolloutGroupSuccessAction(rollout, latestRolloutGroup.get(0));
    }

    private void callErrorAction(final Rollout rollout, final RolloutGroup rolloutGroup) {
        try {
            context.getBean(rolloutGroup.getErrorAction().getBeanName(), RolloutGroupActionEvaluator.class)
                    .eval(rollout, rolloutGroup, rolloutGroup.getErrorActionExp());
        } catch (final BeansException e) {
            LOGGER.error("Something bad happend when accessing the error action bean {}",
                    rolloutGroup.getErrorAction().getBeanName(), e);
        }
    }

    private boolean isRolloutComplete(final JpaRollout rollout) {
        final Long groupsActiveLeft = rolloutGroupRepository.countByRolloutAndStatusOrStatus(rollout,
                RolloutGroupStatus.RUNNING, RolloutGroupStatus.SCHEDULED);
        return groupsActiveLeft == 0;
    }

    private boolean isRolloutGroupComplete(final JpaRollout rollout, final JpaRolloutGroup rolloutGroup) {
        final Long actionsLeftForRollout = actionRepository
                .countByRolloutAndRolloutGroupAndStatusNotAndStatusNotAndStatusNot(rollout, rolloutGroup,
                        Action.Status.ERROR, Action.Status.FINISHED, Action.Status.CANCELED);
        return actionsLeftForRollout == 0;
    }

    private boolean checkErrorState(final Rollout rollout, final RolloutGroup rolloutGroup) {

        final RolloutGroupErrorCondition errorCondition = rolloutGroup.getErrorCondition();

        if (errorCondition == null) {
            // there is no error condition, so return false, don't have error.
            return false;
        }
        try {
            return context.getBean(errorCondition.getBeanName(), RolloutGroupConditionEvaluator.class).eval(rollout,
                    rolloutGroup, rolloutGroup.getErrorConditionExp());
        } catch (final BeansException e) {
            LOGGER.error("Something bad happend when accessing the error condition bean {}",
                    errorCondition.getBeanName(), e);
            return false;
        }
    }

    private boolean checkFinishCondition(final Rollout rollout, final RolloutGroup rolloutGroup,
            final RolloutGroupSuccessCondition finishCondition) {
        LOGGER.trace("Checking finish condition {} on rolloutgroup {}", finishCondition, rolloutGroup);
        try {
            final boolean isFinished = context
                    .getBean(finishCondition.getBeanName(), RolloutGroupConditionEvaluator.class)
                    .eval(rollout, rolloutGroup, rolloutGroup.getSuccessConditionExp());
            if (isFinished) {
                LOGGER.info("Rolloutgroup {} is finished, starting next group", rolloutGroup);
                executeRolloutGroupSuccessAction(rollout, rolloutGroup);
            } else {
                LOGGER.debug("Rolloutgroup {} is still running", rolloutGroup);
            }
            return isFinished;
        } catch (final BeansException e) {
            LOGGER.error("Something bad happend when accessing the finish condition bean {}",
                    finishCondition.getBeanName(), e);
            return false;
        }
    }

    private void executeRolloutGroupSuccessAction(final Rollout rollout, final RolloutGroup rolloutGroup) {
        context.getBean(rolloutGroup.getSuccessAction().getBeanName(), RolloutGroupActionEvaluator.class).eval(rollout,
                rolloutGroup, rolloutGroup.getSuccessActionExp());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public void checkCreatingRollouts(final long delayBetweenChecks) {
        final List<Long> rolloutsToCheck = getRolloutsToCheckForStatus(delayBetweenChecks, RolloutStatus.CREATING)
                .stream().map(Rollout::getId).collect(Collectors.toList());
        if (rolloutsToCheck.isEmpty()) {
            return;
        }

        LOGGER.info("Found {} creating rollouts to check", rolloutsToCheck.size());

        rolloutsToCheck.forEach(this::fillRolloutGroupsWithTargets);

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public void checkStartingRollouts(final long delayBetweenChecks) {
        final List<JpaRollout> rolloutsToCheck = getRolloutsToCheckForStatus(delayBetweenChecks,
                RolloutStatus.STARTING);
        if (rolloutsToCheck.isEmpty()) {
            return;
        }

        LOGGER.info("Found {} starting rollouts to check", rolloutsToCheck.size());

        rolloutsToCheck.forEach(rollout -> {
            if (ensureAllGroupsAreScheduled(rollout)) {
                startFirstRolloutGroup(rollout);
            }
        });
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public void checkReadyRollouts(final long delayBetweenChecks) {
        final List<JpaRollout> rolloutsToCheck = getRolloutsToCheckForStatus(delayBetweenChecks, RolloutStatus.READY);
        if (rolloutsToCheck.isEmpty()) {
            return;
        }

        LOGGER.info("Found {} ready rollouts to check", rolloutsToCheck.size());

        final long now = System.currentTimeMillis();

        rolloutsToCheck.forEach(rollout -> {
            if (rollout.getStartAt() != null && rollout.getStartAt() <= now) {
                startRollout(rollout.getId());
            }
        });
    }

    private List<JpaRollout> getRolloutsToCheckForStatus(final long delayBetweenChecks, final RolloutStatus status) {
        final long lastCheck = System.currentTimeMillis();
        final int updated = rolloutRepository.updateLastCheck(lastCheck, delayBetweenChecks, status);
        if (updated == 0) {
            // nothing to check, maybe another instance already checked in
            // between
            LOGGER.debug("No rollouts starting check necessary for current scheduled check {}, next check at {}",
                    lastCheck, lastCheck + delayBetweenChecks);
            return Collections.emptyList();
        }

        return rolloutRepository.findByLastCheckAndStatus(lastCheck, status);
    }

    @Override
    public Long countRolloutsAll() {
        return rolloutRepository.count();
    }

    @Override
    public Long countRolloutsAllByFilters(final String searchText) {
        return rolloutRepository.count(RolloutHelper.likeNameOrDescription(searchText));
    }

    @Override
    public Slice<Rollout> findRolloutWithDetailedStatusByFilters(final Pageable pageable, final String searchText) {
        final Specification<JpaRollout> specs = RolloutHelper.likeNameOrDescription(searchText);
        final Slice<JpaRollout> findAll = criteriaNoCountDao.findAll(specs, pageable, JpaRollout.class);
        setRolloutStatusDetails(findAll);
        return RolloutHelper.convertPage(findAll, pageable);
    }

    @Override
    public Optional<Rollout> findRolloutByName(final String rolloutName) {
        return rolloutRepository.findByName(rolloutName);
    }

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    @Modifying
    public Rollout updateRollout(final RolloutUpdate u) {
        final GenericRolloutUpdate update = (GenericRolloutUpdate) u;
        final JpaRollout rollout = getRolloutAndThrowExceptionIfNotFound(update.getId());

        update.getName().ifPresent(rollout::setName);
        update.getDescription().ifPresent(rollout::setDescription);
        update.getActionType().ifPresent(rollout::setActionType);
        update.getForcedTime().ifPresent(rollout::setForcedTime);
        update.getStartAt().ifPresent(rollout::setStartAt);
        update.getSet().ifPresent(setId -> {
            final DistributionSet set = distributionSetManagement.findDistributionSetById(setId)
                    .orElseThrow(() -> new EntityNotFoundException(DistributionSet.class, setId));

            rollout.setDistributionSet(set);
        });

        return rolloutRepository.save(rollout);
    }

    private JpaRollout getRolloutAndThrowExceptionIfNotFound(final Long rolloutId) {
        return rolloutRepository.findById(rolloutId)
                .orElseThrow(() -> new EntityNotFoundException(Rollout.class, rolloutId));
    }

    @Override
    public Page<Rollout> findAllRolloutsWithDetailedStatus(final Pageable pageable) {
        final Page<JpaRollout> rollouts = rolloutRepository.findAll(pageable);
        setRolloutStatusDetails(rollouts);
        return RolloutHelper.convertPage(rollouts, pageable);
    }

    @Override
    public Optional<Rollout> findRolloutWithDetailedStatus(final Long rolloutId) {
        final Optional<Rollout> rollout = findRolloutById(rolloutId);

        if (!rollout.isPresent()) {
            return rollout;
        }

        final List<TotalTargetCountActionStatus> rolloutStatusCountItems = actionRepository
                .getStatusCountByRolloutId(rolloutId);
        final TotalTargetCountStatus totalTargetCountStatus = new TotalTargetCountStatus(rolloutStatusCountItems,
                rollout.get().getTotalTargets());
        ((JpaRollout) rollout.get()).setTotalTargetCountStatus(totalTargetCountStatus);
        return rollout;
    }

    private Map<Long, List<TotalTargetCountActionStatus>> getStatusCountItemForRollout(final List<Long> rolloutIds) {
        final List<TotalTargetCountActionStatus> resultList = actionRepository.getStatusCountByRolloutId(rolloutIds);
        return resultList.stream().collect(Collectors.groupingBy(TotalTargetCountActionStatus::getId));
    }

    private void setRolloutStatusDetails(final Slice<JpaRollout> rollouts) {
        final List<Long> rolloutIds = rollouts.getContent().stream().map(Rollout::getId).collect(Collectors.toList());
        final Map<Long, List<TotalTargetCountActionStatus>> allStatesForRollout = getStatusCountItemForRollout(
                rolloutIds);

        for (final Rollout rollout : rollouts) {
            final TotalTargetCountStatus totalTargetCountStatus = new TotalTargetCountStatus(
                    allStatesForRollout.get(rollout.getId()), rollout.getTotalTargets());
            ((JpaRollout) rollout).setTotalTargetCountStatus(totalTargetCountStatus);
        }
    }

    @Override
    public float getFinishedPercentForRunningGroup(final Long rolloutId, final Long rolloutGroupId) {
        final RolloutGroup rolloutGroup = rolloutGroupRepository.findById(rolloutGroupId)
                .orElseThrow(() -> new EntityNotFoundException(RolloutGroup.class, rolloutGroupId));

        final long totalGroup = rolloutGroup.getTotalTargets();
        if (totalGroup == 0) {
            // in case e.g. targets has been deleted we don't have any actions
            // left for this group, so the group is finished
            return 100;
        }

        final Long finished = actionRepository.countByRolloutIdAndRolloutGroupIdAndStatus(rolloutId,
                rolloutGroup.getId(), Action.Status.FINISHED);
        // calculate threshold
        return ((float) finished / (float) totalGroup) * 100;
    }

    @Override
    public boolean exists(final Long rolloutId) {
        return rolloutRepository.exists(rolloutId);
    }
}
