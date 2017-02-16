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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.hawkbit.repository.FilterParams;
import org.eclipse.hawkbit.repository.TargetFields;
import org.eclipse.hawkbit.repository.TargetManagement;
import org.eclipse.hawkbit.repository.TimestampCalculator;
import org.eclipse.hawkbit.repository.builder.TargetCreate;
import org.eclipse.hawkbit.repository.builder.TargetUpdate;
import org.eclipse.hawkbit.repository.event.remote.TargetDeletedEvent;
import org.eclipse.hawkbit.repository.exception.EntityAlreadyExistsException;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.jpa.builder.JpaTargetCreate;
import org.eclipse.hawkbit.repository.jpa.builder.JpaTargetUpdate;
import org.eclipse.hawkbit.repository.jpa.configuration.Constants;
import org.eclipse.hawkbit.repository.jpa.model.JpaDistributionSet_;
import org.eclipse.hawkbit.repository.jpa.model.JpaTarget;
import org.eclipse.hawkbit.repository.jpa.model.JpaTargetInfo;
import org.eclipse.hawkbit.repository.jpa.model.JpaTargetInfo_;
import org.eclipse.hawkbit.repository.jpa.model.JpaTargetTag;
import org.eclipse.hawkbit.repository.jpa.model.JpaTarget_;
import org.eclipse.hawkbit.repository.jpa.rsql.RSQLUtility;
import org.eclipse.hawkbit.repository.jpa.specifications.SpecificationsBuilder;
import org.eclipse.hawkbit.repository.jpa.specifications.TargetSpecifications;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.RolloutGroup;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetFilterQuery;
import org.eclipse.hawkbit.repository.model.TargetTag;
import org.eclipse.hawkbit.repository.model.TargetTagAssignmentResult;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.repository.rsql.VirtualPropertyReplacer;
import org.eclipse.hawkbit.tenancy.TenantAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.google.common.collect.Lists;

/**
 * JPA implementation of {@link TargetManagement}.
 *
 */
@Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
@Validated
public class JpaTargetManagement implements TargetManagement {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TargetRepository targetRepository;

    @Autowired
    private RolloutGroupRepository rolloutGroupRepository;

    @Autowired
    private DistributionSetRepository distributionSetRepository;

    @Autowired
    private TargetFilterQueryRepository targetFilterQueryRepository;

    @Autowired
    private TargetTagRepository targetTagRepository;

    @Autowired
    private TargetInfoRepository targetInfoRepository;

    @Autowired
    private NoCountPagingRepository criteriaNoCountDao;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TenantAware tenantAware;

    @Autowired
    private VirtualPropertyReplacer virtualPropertyReplacer;

    @Override
    public Optional<Target> findTargetByControllerID(final String controllerId) {
        return targetRepository.findByControllerId(controllerId);
    }

    @Override
    public Optional<Target> findTargetByControllerIDWithDetails(final String controllerId) {
        final Optional<Target> result = targetRepository.findByControllerId(controllerId);
        // load lazy relations
        if (!result.isPresent()) {
            return result;
        }

        result.get().getTargetInfo().getControllerAttributes().size();
        if (result.get().getTargetInfo() != null
                && result.get().getTargetInfo().getInstalledDistributionSet() != null) {
            result.get().getTargetInfo().getInstalledDistributionSet().getName();
            result.get().getTargetInfo().getInstalledDistributionSet().getModules().size();
        }
        if (result.get().getAssignedDistributionSet() != null) {
            result.get().getAssignedDistributionSet().getName();
            result.get().getAssignedDistributionSet().getModules().size();
        }

        return result;
    }

    @Override
    public List<Target> findTargetByControllerID(final Collection<String> controllerIDs) {
        return Collections.unmodifiableList(targetRepository
                .findAll(TargetSpecifications.byControllerIdWithStatusAndAssignedInJoin(controllerIDs)));
    }

    @Override
    public Long countTargetsAll() {
        return targetRepository.count();
    }

    @Override
    public Slice<Target> findTargetsAll(final Pageable pageable) {
        // workarround - no join fetch allowed that is why we need specification
        // instead of query for
        // count() of Pageable
        final Specification<JpaTarget> spec = (root, query, cb) -> {
            if (!query.getResultType().isAssignableFrom(Long.class)) {
                root.fetch(JpaTarget_.targetInfo);
            }
            return cb.conjunction();
        };
        return convertPage(criteriaNoCountDao.findAll(spec, pageable, JpaTarget.class), pageable);
    }

    @Override
    public Slice<Target> findTargetsByTargetFilterQuery(final Long targetFilterQueryId, final Pageable pageable) {
        final TargetFilterQuery targetFilterQuery = targetFilterQueryRepository.findById(targetFilterQueryId)
                .orElseThrow(() -> new EntityNotFoundException(TargetFilterQuery.class, targetFilterQueryId));

        return findTargetsBySpec(
                RSQLUtility.parse(targetFilterQuery.getQuery(), TargetFields.class, virtualPropertyReplacer), pageable);
    }

    @Override
    public Page<Target> findTargetsAll(final String targetFilterQuery, final Pageable pageable) {
        return findTargetsBySpec(RSQLUtility.parse(targetFilterQuery, TargetFields.class, virtualPropertyReplacer),
                pageable);
    }

    private Page<Target> findTargetsBySpec(final Specification<JpaTarget> spec, final Pageable pageable) {
        return convertPage(targetRepository.findAll(spec, pageable), pageable);
    }

    @Override
    public List<Target> findTargetsByControllerIDsWithTags(final List<String> controllerIDs) {
        final List<List<String>> partition = Lists.partition(controllerIDs, Constants.MAX_ENTRIES_IN_STATEMENT);
        return partition.stream()
                .map(ids -> targetRepository.findAll(TargetSpecifications.byControllerIdWithStatusAndTagsInJoin(ids)))
                .flatMap(t -> t.stream()).collect(Collectors.toList());
    }

    @Override
    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Target updateTarget(final TargetUpdate u) {
        final JpaTargetUpdate update = (JpaTargetUpdate) u;

        final JpaTarget target = (JpaTarget) targetRepository.findByControllerId(update.getControllerId())
                .orElseThrow(() -> new EntityNotFoundException(Target.class, update.getControllerId()));

        target.setNew(false);

        update.getName().ifPresent(target::setName);
        update.getDescription().ifPresent(target::setDescription);
        update.getAddress().ifPresent(address -> ((JpaTargetInfo) target.getTargetInfo()).setAddress(address));
        update.getSecurityToken().ifPresent(target::setSecurityToken);

        return targetRepository.save(target);
    }

    @Override
    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void deleteTargets(final Collection<Long> targetIDs) {
        final List<JpaTarget> targets = targetRepository.findAll(targetIDs);

        if (targets.size() < targetIDs.size()) {
            throw new EntityNotFoundException(Target.class, targetIDs,
                    targets.stream().map(Target::getId).collect(Collectors.toList()));
        }

        targetRepository.deleteByIdIn(targetIDs);

        targetIDs.forEach(targetId -> eventPublisher.publishEvent(new TargetDeletedEvent(tenantAware.getCurrentTenant(),
                targetId, JpaTarget.class.getName(), applicationContext.getId())));
    }

    @Override
    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void deleteTarget(final String controllerID) {
        final Target target = targetRepository.findByControllerId(controllerID)
                .orElseThrow(() -> new EntityNotFoundException(Target.class, controllerID));

        targetRepository.delete(target.getId());
    }

    @Override
    public Page<Target> findTargetByAssignedDistributionSet(final Long distributionSetID, final Pageable pageReq) {
        throwEntityNotFoundIfDsDoesNotExist(distributionSetID);

        return targetRepository.findByAssignedDistributionSetId(pageReq, distributionSetID);
    }

    @Override
    public Page<Target> findTargetByAssignedDistributionSet(final Long distributionSetID, final String rsqlParam,
            final Pageable pageReq) {
        throwEntityNotFoundIfDsDoesNotExist(distributionSetID);

        final Specification<JpaTarget> spec = RSQLUtility.parse(rsqlParam, TargetFields.class, virtualPropertyReplacer);

        return convertPage(
                targetRepository
                        .findAll((Specification<JpaTarget>) (root, query,
                                cb) -> cb.and(TargetSpecifications.hasAssignedDistributionSet(distributionSetID)
                                        .toPredicate(root, query, cb), spec.toPredicate(root, query, cb)),
                                pageReq),
                pageReq);
    }

    private void throwEntityNotFoundIfDsDoesNotExist(final Long distributionSetID) {
        if (!distributionSetRepository.exists(distributionSetID)) {
            throw new EntityNotFoundException(DistributionSet.class, distributionSetID);
        }
    }

    private static Page<Target> convertPage(final Page<JpaTarget> findAll, final Pageable pageable) {
        return new PageImpl<>(Collections.unmodifiableList(findAll.getContent()), pageable, findAll.getTotalElements());
    }

    private static Slice<Target> convertPage(final Slice<JpaTarget> findAll, final Pageable pageable) {
        return new PageImpl<>(Collections.unmodifiableList(findAll.getContent()), pageable, 0);
    }

    @Override
    public Page<Target> findTargetByInstalledDistributionSet(final Long distributionSetID, final Pageable pageReq) {
        throwEntityNotFoundIfDsDoesNotExist(distributionSetID);
        return targetRepository.findByTargetInfoInstalledDistributionSetId(pageReq, distributionSetID);
    }

    @Override
    public Page<Target> findTargetByInstalledDistributionSet(final Long distributionSetId, final String rsqlParam,
            final Pageable pageable) {
        throwEntityNotFoundIfDsDoesNotExist(distributionSetId);

        final Specification<JpaTarget> spec = RSQLUtility.parse(rsqlParam, TargetFields.class, virtualPropertyReplacer);

        return convertPage(
                targetRepository
                        .findAll((Specification<JpaTarget>) (root, query,
                                cb) -> cb.and(TargetSpecifications.hasInstalledDistributionSet(distributionSetId)
                                        .toPredicate(root, query, cb), spec.toPredicate(root, query, cb)),
                                pageable),
                pageable);
    }

    @Override
    public Page<Target> findTargetByUpdateStatus(final Pageable pageable, final TargetUpdateStatus status) {
        return targetRepository.findByTargetInfoUpdateStatus(pageable, status);
    }

    @Override
    public Slice<Target> findTargetByFilters(final Pageable pageable, final Collection<TargetUpdateStatus> status,
            final Boolean overdueState, final String searchText, final Long installedOrAssignedDistributionSetId,
            final Boolean selectTargetWithNoTag, final String... tagNames) {
        final List<Specification<JpaTarget>> specList = buildSpecificationList(
                new FilterParams(installedOrAssignedDistributionSetId, status, overdueState, searchText,
                        selectTargetWithNoTag, tagNames),
                true);
        return findByCriteriaAPI(pageable, specList);
    }

    @Override
    public Long countTargetByFilters(final Collection<TargetUpdateStatus> status, final Boolean overdueState,
            final String searchText, final Long installedOrAssignedDistributionSetId,
            final Boolean selectTargetWithNoTag, final String... tagNames) {
        final List<Specification<JpaTarget>> specList = buildSpecificationList(
                new FilterParams(installedOrAssignedDistributionSetId, status, overdueState, searchText,
                        selectTargetWithNoTag, tagNames),
                true);
        return countByCriteriaAPI(specList);
    }

    private List<Specification<JpaTarget>> buildSpecificationList(final FilterParams filterParams,
            final boolean fetch) {
        final List<Specification<JpaTarget>> specList = new ArrayList<>();
        if (filterParams.getFilterByStatus() != null && !filterParams.getFilterByStatus().isEmpty()) {
            specList.add(TargetSpecifications.hasTargetUpdateStatus(filterParams.getFilterByStatus(), fetch));
        }
        if (filterParams.getOverdueState() != null) {
            specList.add(TargetSpecifications.isOverdue(TimestampCalculator.calculateOverdueTimestamp()));
        }
        if (filterParams.getFilterByDistributionId() != null) {
            throwEntityNotFoundIfDsDoesNotExist(filterParams.getFilterByDistributionId());

            specList.add(TargetSpecifications
                    .hasInstalledOrAssignedDistributionSet(filterParams.getFilterByDistributionId()));
        }
        if (StringUtils.isNotEmpty(filterParams.getFilterBySearchText())) {
            specList.add(TargetSpecifications.likeIdOrNameOrDescription(filterParams.getFilterBySearchText()));
        }
        if (isHasTagsFilterActive(filterParams)) {
            specList.add(TargetSpecifications.hasTags(filterParams.getFilterByTagNames(),
                    filterParams.getSelectTargetWithNoTag()));
        }
        return specList;
    }

    private static boolean isHasTagsFilterActive(final FilterParams filterParams) {
        return filterParams.getSelectTargetWithNoTag() != null && (filterParams.getSelectTargetWithNoTag()
                || (filterParams.getFilterByTagNames() != null && filterParams.getFilterByTagNames().length > 0));
    }

    private Slice<Target> findByCriteriaAPI(final Pageable pageable, final List<Specification<JpaTarget>> specList) {
        if (specList == null || specList.isEmpty()) {
            return convertPage(criteriaNoCountDao.findAll(pageable, JpaTarget.class), pageable);
        }
        return convertPage(
                criteriaNoCountDao.findAll(SpecificationsBuilder.combineWithAnd(specList), pageable, JpaTarget.class),
                pageable);
    }

    private Long countByCriteriaAPI(final List<Specification<JpaTarget>> specList) {
        if (specList == null || specList.isEmpty()) {
            return targetRepository.count();
        }

        return targetRepository.count(SpecificationsBuilder.combineWithAnd(specList));
    }

    @Override
    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public TargetTagAssignmentResult toggleTagAssignment(final Collection<String> targetIds, final String tagName) {
        final TargetTag tag = targetTagRepository.findByNameEquals(tagName)
                .orElseThrow(() -> new EntityNotFoundException(TargetTag.class, tagName));
        final List<JpaTarget> alreadyAssignedTargets = targetRepository.findByTagNameAndControllerIdIn(tagName,
                targetIds);
        final List<JpaTarget> allTargets = targetRepository
                .findAll(TargetSpecifications.byControllerIdWithStatusAndTagsInJoin(targetIds));

        // all are already assigned -> unassign
        if (alreadyAssignedTargets.size() == allTargets.size()) {
            alreadyAssignedTargets.forEach(target -> target.removeTag(tag));
            final TargetTagAssignmentResult result = new TargetTagAssignmentResult(0, 0, alreadyAssignedTargets.size(),
                    Collections.emptyList(), Collections.unmodifiableList(alreadyAssignedTargets), tag);

            return result;
        }

        allTargets.removeAll(alreadyAssignedTargets);
        // some or none are assigned -> assign
        allTargets.forEach(target -> target.addTag(tag));
        final TargetTagAssignmentResult result = new TargetTagAssignmentResult(alreadyAssignedTargets.size(),
                allTargets.size(), 0,
                Collections
                        .unmodifiableList(allTargets.stream().map(targetRepository::save).collect(Collectors.toList())),
                Collections.emptyList(), tag);

        // no reason to persist the tag
        entityManager.detach(tag);
        return result;
    }

    @Override
    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public List<Target> assignTag(final Collection<String> controllerIds, final Long tagId) {
        final List<JpaTarget> allTargets = targetRepository
                .findAll(TargetSpecifications.byControllerIdWithStatusAndTagsInJoin(controllerIds));

        final JpaTargetTag tag = targetTagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(TargetTag.class, tagId));

        allTargets.forEach(target -> target.addTag(tag));
        return Collections
                .unmodifiableList(allTargets.stream().map(targetRepository::save).collect(Collectors.toList()));
    }

    private List<Target> unAssignTag(final Collection<Target> targets, final TargetTag tag) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Collection<JpaTarget> toUnassign = (Collection) targets;

        toUnassign.forEach(target -> target.removeTag(tag));

        return Collections
                .unmodifiableList(toUnassign.stream().map(targetRepository::save).collect(Collectors.toList()));
    }

    @Override
    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public List<Target> unAssignAllTargetsByTag(final Long targetTagId) {

        final TargetTag tag = targetTagRepository.findById(targetTagId)
                .orElseThrow(() -> new EntityNotFoundException(TargetTag.class, targetTagId));

        if (tag.getAssignedToTargets().isEmpty()) {
            return Collections.emptyList();
        }

        return unAssignTag(tag.getAssignedToTargets(), tag);
    }

    @Override
    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Target unAssignTag(final String controllerID, final Long targetTagId) {
        final List<Target> allTargets = Collections.unmodifiableList(targetRepository
                .findAll(TargetSpecifications.byControllerIdWithStatusAndTagsInJoin(Arrays.asList(controllerID))));

        final TargetTag tag = targetTagRepository.findById(targetTagId)
                .orElseThrow(() -> new EntityNotFoundException(TargetTag.class, targetTagId));

        final List<Target> unAssignTag = unAssignTag(allTargets, tag);
        return unAssignTag.isEmpty() ? null : unAssignTag.get(0);
    }

    @Override
    public Slice<Target> findTargetsAllOrderByLinkedDistributionSet(final Pageable pageable,
            final Long orderByDistributionId, final FilterParams filterParams) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<JpaTarget> query = cb.createQuery(JpaTarget.class);
        final Root<JpaTarget> targetRoot = query.from(JpaTarget.class);

        // necessary joins for the select
        final Join<JpaTarget, JpaTargetInfo> targetInfo = (Join<JpaTarget, JpaTargetInfo>) targetRoot
                .fetch(JpaTarget_.targetInfo, JoinType.LEFT);

        // select case expression to retrieve the case value as a column to be
        // able to order based on
        // this column, installed first,...
        final Expression<Object> selectCase = cb.selectCase()
                .when(cb.equal(targetInfo.get(JpaTargetInfo_.installedDistributionSet).get(JpaDistributionSet_.id),
                        orderByDistributionId), 1)
                .when(cb.equal(targetRoot.get(JpaTarget_.assignedDistributionSet).get(JpaDistributionSet_.id),
                        orderByDistributionId), 2)
                .otherwise(100);
        // multiselect statement order by the select case and controllerId
        query.distinct(true);
        // build the specifications and then to predicates necessary by the
        // given filters
        final Predicate[] specificationsForMultiSelect = specificationsToPredicate(
                buildSpecificationList(filterParams, true), targetRoot, query, cb);

        // if we have some predicates then add it to the where clause of the
        // multiselect
        if (specificationsForMultiSelect.length > 0) {
            query.where(specificationsForMultiSelect);
        }
        // add the order to the multi select first based on the selectCase
        query.orderBy(cb.asc(selectCase), cb.desc(targetRoot.get(JpaTarget_.id)));
        // the result is a Object[] due the fact that the selectCase is an extra
        // column, so it cannot
        // be mapped directly to a Target entity because the selectCase is not a
        // attribute of the
        // Target entity, the the Object array contains the Target on the first
        // index (case of the
        // multiselect order) of the array and
        // the 2nd contains the selectCase int value.
        final int pageSize = pageable.getPageSize();
        final List<JpaTarget> resultList = entityManager.createQuery(query).setFirstResult(pageable.getOffset())
                .setMaxResults(pageSize + 1).getResultList();
        final boolean hasNext = resultList.size() > pageSize;
        return new SliceImpl<>(Collections.unmodifiableList(resultList), pageable, hasNext);
    }

    private static Predicate[] specificationsToPredicate(final List<Specification<JpaTarget>> specifications,
            final Root<JpaTarget> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
        final Predicate[] predicates = new Predicate[specifications.size()];
        for (int index = 0; index < predicates.length; index++) {
            predicates[index] = specifications.get(index).toPredicate(root, query, cb);
        }
        return predicates;
    }

    @Override
    public Long countTargetByAssignedDistributionSet(final Long distId) {
        throwEntityNotFoundIfDsDoesNotExist(distId);

        return targetRepository.countByAssignedDistributionSetId(distId);
    }

    @Override
    public Long countTargetByInstalledDistributionSet(final Long distId) {
        throwEntityNotFoundIfDsDoesNotExist(distId);

        return targetRepository.countByTargetInfoInstalledDistributionSetId(distId);
    }

    @Override
    public Page<Target> findAllTargetsByTargetFilterQueryAndNonDS(final Pageable pageRequest,
            final Long distributionSetId, final String targetFilterQuery) {
        throwEntityNotFoundIfDsDoesNotExist(distributionSetId);

        final Specification<JpaTarget> spec = RSQLUtility.parse(targetFilterQuery, TargetFields.class,
                virtualPropertyReplacer);

        return findTargetsBySpec(
                (root, cq,
                        cb) -> cb.and(spec.toPredicate(root, cq, cb), TargetSpecifications
                                .hasNotDistributionSetInActions(distributionSetId).toPredicate(root, cq, cb)),
                pageRequest);

    }

    @Override
    public Page<Target> findAllTargetsByTargetFilterQueryAndNotInRolloutGroups(final Pageable pageRequest,
            final Collection<Long> groups, final String targetFilterQuery) {

        final Specification<JpaTarget> spec = RSQLUtility.parse(targetFilterQuery, TargetFields.class,
                virtualPropertyReplacer);

        return findTargetsBySpec((root, cq, cb) -> cb.and(spec.toPredicate(root, cq, cb),
                TargetSpecifications.isNotInRolloutGroups(groups).toPredicate(root, cq, cb)), pageRequest);

    }

    @Override
    public Page<Target> findAllTargetsInRolloutGroupWithoutAction(@NotNull final Pageable pageRequest,
            @NotNull final Long group) {
        if (!rolloutGroupRepository.exists(group)) {
            throw new EntityNotFoundException(RolloutGroup.class, group);
        }

        return findTargetsBySpec(
                (root, cq, cb) -> TargetSpecifications.hasNoActionInRolloutGroup(group).toPredicate(root, cq, cb),
                pageRequest);
    }

    @Override
    public Long countAllTargetsByTargetFilterQueryAndNotInRolloutGroups(final Collection<Long> groups,
            final String targetFilterQuery) {
        final Specification<JpaTarget> spec = RSQLUtility.parse(targetFilterQuery, TargetFields.class,
                virtualPropertyReplacer);
        final List<Specification<JpaTarget>> specList = Lists.newArrayList(spec,
                TargetSpecifications.isNotInRolloutGroups(groups));

        return countByCriteriaAPI(specList);
    }

    @Override
    public Long countTargetsByTargetFilterQueryAndNonDS(final Long distributionSetId, final String targetFilterQuery) {
        throwEntityNotFoundIfDsDoesNotExist(distributionSetId);

        final Specification<JpaTarget> spec = RSQLUtility.parse(targetFilterQuery, TargetFields.class,
                virtualPropertyReplacer);
        final List<Specification<JpaTarget>> specList = new ArrayList<>(2);
        specList.add(spec);
        specList.add(TargetSpecifications.hasNotDistributionSetInActions(distributionSetId));

        return countByCriteriaAPI(specList);
    }

    @Override
    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public Target createTarget(final TargetCreate c) {
        final JpaTargetCreate create = (JpaTargetCreate) c;

        final JpaTarget target = create.build();

        if (targetRepository.findByControllerId(target.getControllerId()).isPresent()) {
            throw new EntityAlreadyExistsException();
        }

        target.setNew(true);
        final JpaTarget savedTarget = targetRepository.save(target);
        final JpaTargetInfo targetInfo = (JpaTargetInfo) savedTarget.getTargetInfo();

        targetInfo.setNew(true);
        final Target targetToReturn = targetInfoRepository.save(targetInfo).getTarget();
        targetInfo.setNew(false);
        return targetToReturn;
    }

    @Override
    @Modifying
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public List<Target> createTargets(final Collection<TargetCreate> targets) {
        return targets.stream().map(this::createTarget).collect(Collectors.toList());
    }

    @Override
    public List<Target> findTargetsByTag(final String tagName) {
        final Optional<TargetTag> tag = targetTagRepository.findByNameEquals(tagName);
        if (!tag.isPresent()) {
            return Collections.emptyList();
        }

        return Collections.unmodifiableList(targetRepository.findByTag(tag.get().getId()));
    }

    @Override
    public Long countTargetByTargetFilterQuery(final Long targetFilterQueryId) {
        final TargetFilterQuery targetFilterQuery = targetFilterQueryRepository.findById(targetFilterQueryId)
                .orElseThrow(() -> new EntityNotFoundException(TargetFilterQuery.class, targetFilterQueryId));

        final Specification<JpaTarget> specs = RSQLUtility.parse(targetFilterQuery.getQuery(), TargetFields.class,
                virtualPropertyReplacer);
        return targetRepository.count(specs);
    }

    @Override
    public Long countTargetByTargetFilterQuery(final String targetFilterQuery) {
        final Specification<JpaTarget> specs = RSQLUtility.parse(targetFilterQuery, TargetFields.class,
                virtualPropertyReplacer);
        return targetRepository.count((root, query, cb) -> {
            query.distinct(true);
            return specs.toPredicate(root, query, cb);
        });
    }

    @Override
    public Optional<Target> findTargetById(final Long id) {
        return Optional.ofNullable(targetRepository.findOne(id));
    }

    @Override
    public List<Target> findTargetAllById(final Collection<Long> ids) {
        return Collections.unmodifiableList(targetRepository.findAll(ids));
    }

}
