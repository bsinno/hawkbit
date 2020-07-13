/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.eclipse.hawkbit.repository.DirectoryGroupManagement;
import org.eclipse.hawkbit.repository.exception.InvalidDirectoryGroupAssignmentException;
import org.eclipse.hawkbit.repository.jpa.builder.JpaDirectoryGroupBuilder;
import org.eclipse.hawkbit.repository.jpa.model.DirectoryTreeId;
import org.eclipse.hawkbit.repository.model.DirectoryGroup;
import org.junit.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

/**
 * {@link DirectoryGroupManagement} tests.
 */
@Feature("Component Tests - Repository")
@Story("DirectoryGroup Management")
public class DirectoryGroupManagementTest extends AbstractJpaIntegrationTest {

    @Test
    @Description("A closure self reference is created after adding a new directory group")
    public void creatingGroupTriggersClosureSelfReference() {
        // Create directory group
        final DirectoryGroup testGroup = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroup"));

        /* Expected closures:
         *   - testGroup <-> testGroup | depth: 0
         */
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroup, testGroup, 0)).isTrue();
    }

    @Test
    @Description("Setting a parent for a group creates a closure entry between them")
    public void settingGroupParentTriggersClosure() {
        // Create directory groups
        final DirectoryGroup testGroupParent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupParent"));
        final DirectoryGroup testGroupChild = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupChild"));

        // Set parent of child group
        directoryGroupManagement.assignDirectoryParent(testGroupChild.getId(), testGroupParent.getId());

        /* Expected closures:
         *   - parent <-> parent | depth: 0
         *   - child <-> child   | depth: 0
         *   - parent <-> child  | depth: 1
         */
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupParent, 0)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupChild, testGroupChild, 0)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupChild, 1)).isTrue();
    }

    @Test
    @Description("Setting multiple levels of group parents creates a closure hierarchy")
    public void multipleGroupParentLevelsTriggerClosureHierarchy() {
        // Create directory groups
        final DirectoryGroup testGroupGrandparent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupGrandparent"));
        final DirectoryGroup testGroupParent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupParent"));
        final DirectoryGroup testGroupChild = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupChild"));

        // Set parent/child relationships
        directoryGroupManagement.assignDirectoryParent(testGroupParent.getId(), testGroupGrandparent.getId());
        directoryGroupManagement.assignDirectoryParent(testGroupChild.getId(), testGroupParent.getId());

        /* Expected closures (without self reference, total 6):
         *   - grandparent <-> parent | depth: 1
         *   - parent <-> child      | depth: 1
         *   - grandparent <-> child  | depth: 2
         */
        assertThat(directoryTreeRepository.count()).isEqualTo(6);
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent, testGroupParent, 1)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupChild, 1)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent, testGroupChild, 2)).isTrue();
    }

    @Test
    @Description("Moving a group to another parent results in the correct closure hierarchy")
    public void movingGroupLeadsToCorrectClosureHierarchy() {
        // Create directory groups
        final DirectoryGroup testGroupGrandparent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupGrandparent"));
        final DirectoryGroup testGroupParent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupParent"));
        final DirectoryGroup testGroupChild = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupChild"));

        // Set parent/child relationships
        directoryGroupManagement.assignDirectoryParent(testGroupParent.getId(), testGroupGrandparent.getId());
        directoryGroupManagement.assignDirectoryParent(testGroupChild.getId(), testGroupParent.getId());

        /* Expected closures (without self reference, total 6):
         *   - grandparent <-> parent | depth: 1
         *   - parent <-> child       | depth: 1
         *   - grandparent <-> child  | depth: 2
         */
        assertThat(directoryTreeRepository.count()).isEqualTo(6);
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent, testGroupParent, 1)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupChild, 1)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent, testGroupChild, 2)).isTrue();

        // Create second grandparent and move parent to it
        final DirectoryGroup testGroupSecondGrandparent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupSecondGrandparent"));
        directoryGroupManagement.assignDirectoryParent(testGroupParent.getId(), testGroupSecondGrandparent.getId());

        /* Ensure old closures are gone and new exists
         * Expected closures (without self reference, total 7):
         *   - grandparent <-> parent | depth: 1
         *   - parent <-> child       | depth: 1
         *   - grandparent <-> child  | depth: 2
         */
        assertThat(directoryTreeRepository.count()).isEqualTo(7);
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupSecondGrandparent, testGroupParent, 1)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupChild, 1)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupSecondGrandparent, testGroupChild, 2)).isTrue();
    }

    @Test
    @Description("Deleting leaf groups clears closure entries")
    public void deletingLeafGroupsClearsClosure() {
        // Create directory groups
        final DirectoryGroup testGroupGrandparent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupGrandParent"));
        final DirectoryGroup testGroupParent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupParent"));
        final DirectoryGroup testGroupChild = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupChild"));

        // 1. Set initial parent/child relationships
        directoryGroupManagement.assignDirectoryParent(testGroupParent.getId(), testGroupGrandparent.getId());
        directoryGroupManagement.assignDirectoryParent(testGroupChild.getId(), testGroupParent.getId());

        /* Expected closures (without self reference, total 7):
         *   - grandparent <-> parent | depth: 1
         *   - parent <-> child       | depth: 1
         *   - grandparent <-> child  | depth: 2
         */
        assertThat(directoryTreeRepository.count()).isEqualTo(6);
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent, testGroupParent, 1)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupChild, 1)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent, testGroupChild, 2)).isTrue();

        // 2. Delete relationships and ensure closures are removed as well (depth does not need to be considered)
        final long childId = testGroupChild.getId();
        directoryGroupManagement.deleteById(testGroupChild.getId());
        assertThat(directoryTreeRepository.existsById(new DirectoryTreeId(testGroupGrandparent.getId(), childId))).isFalse();
        assertThat(directoryTreeRepository.existsById(new DirectoryTreeId(testGroupParent.getId(), childId))).isFalse();

        final long parentId = testGroupParent.getId();
        directoryGroupManagement.deleteById(parentId);
        assertThat(directoryTreeRepository.existsById(new DirectoryTreeId(testGroupGrandparent.getId(), parentId))).isFalse();

        // Only grandparent self reference is left
        assertThat(directoryTreeRepository.count()).isEqualTo(1);
    }

    @Test
    @Description("Deleting an intermediate parent deletes sub groups and clears closures")
    public void deletingIntermediateParentClearsClosure() {
        // Create directory groups
        final DirectoryGroup testGroupGrandparent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupGrandparent"));
        final DirectoryGroup testGroupParent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupParent"));
        final DirectoryGroup testGroupChild = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupChild"));

        // Set parent/child relationships
        directoryGroupManagement.assignDirectoryParent(testGroupParent.getId(), testGroupGrandparent.getId());
        directoryGroupManagement.assignDirectoryParent(testGroupChild.getId(), testGroupParent.getId());

        // deleting intermediate parent only
        directoryGroupManagement.deleteById(testGroupParent.getId());

        // ensure directory groups (parent and child) are gone
        assertThat(directoryGroupRepository.existsById(testGroupParent.getId())).isFalse();
        assertThat(directoryGroupRepository.existsById(testGroupChild.getId())).isFalse();

        // ensure closures are gone too, also between grandparent and child
        assertThat(directoryTreeRepository.count()).isEqualTo(1);
        assertThat(directoryTreeRepository.existsById(new DirectoryTreeId(testGroupParent.getId(), testGroupChild.getId()))).isFalse();
        assertThat(directoryTreeRepository.existsById(new DirectoryTreeId(testGroupGrandparent.getId(), testGroupChild.getId()))).isFalse();
    }

    @Test
    @Description("Deleting a hierarchy parent also deletes all sub groups")
    public void deletingParentGroupCascades() {
        // Create directory groups
        final DirectoryGroup testGroupGrandparent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupGrandparent"));
        final DirectoryGroup testGroupParent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupParent"));
        final DirectoryGroup testGroupChild = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupChild"));

        // Set parent/child relationships
        directoryGroupManagement.assignDirectoryParent(testGroupParent.getId(), testGroupGrandparent.getId());
        directoryGroupManagement.assignDirectoryParent(testGroupChild.getId(), testGroupParent.getId());

        // deleting grandparent only
        directoryGroupManagement.deleteById(testGroupGrandparent.getId());

        // ensure directory groups are gone
        assertThat(directoryGroupRepository.existsById(testGroupGrandparent.getId())).isFalse();
        assertThat(directoryGroupRepository.existsById(testGroupParent.getId())).isFalse();
        assertThat(directoryGroupRepository.existsById(testGroupChild.getId())).isFalse();

        // ensure closures are gone too, no closures left
        assertThat(directoryTreeRepository.count()).isEqualTo(0);
        assertThat(directoryTreeRepository.existsById(new DirectoryTreeId(testGroupGrandparent.getId(), testGroupParent.getId()))).isFalse();
        assertThat(directoryTreeRepository.existsById(new DirectoryTreeId(testGroupParent.getId(), testGroupChild.getId()))).isFalse();
    }

    @Test
    @Description("Deleting all groups of a tenant is not blocked by a database restrictions")
    public void deletingAllGroupsOfATenantIsNotBlocked() {
        // Create directory groups
        final DirectoryGroup testGroupGrandparent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupGrandparent"));
        final DirectoryGroup testGroupParent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupParent"));
        final DirectoryGroup testGroupChild = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupChild"));

        // Set parent/child relationships
        directoryGroupManagement.assignDirectoryParent(testGroupParent.getId(), testGroupGrandparent.getId());
        directoryGroupManagement.assignDirectoryParent(testGroupChild.getId(), testGroupParent.getId());

        // Delete all groups of single tenant
        directoryGroupRepository.deleteByTenant("DEFAULT");

        // ensure all groups are removed
        assertThat(directoryGroupRepository.existsById(testGroupGrandparent.getId())).isFalse();
        assertThat(directoryGroupRepository.existsById(testGroupParent.getId())).isFalse();
        assertThat(directoryGroupRepository.existsById(testGroupChild.getId())).isFalse();
        // ensure closures are removed
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent, testGroupGrandparent, 0)).isFalse();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupParent, 0)).isFalse();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupChild, testGroupChild, 0)).isFalse();
    }

    @Test
    @Description("Setting a parent group that causes a looped reference is not possible")
    public void preventSettingGroupParentCausingLoop() {
        // Create directory groups
        final DirectoryGroup testGroupParent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupParent"));
        final DirectoryGroup testGroupChild = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupChild"));

        // Set parent of child group
        directoryGroupManagement.assignDirectoryParent(testGroupChild.getId(), testGroupParent.getId());

        // Try to set parents group to child causing a looped reference leading to an exception
        verifyExceptionIsThrown(() -> directoryGroupManagement.assignDirectoryParent(testGroupParent.getId(), testGroupChild.getId()), InvalidDirectoryGroupAssignmentException.class);
    }

    @Test
    @Description("Setting parent group to self is not possible")
    public void preventSettingGroupParentToSelf() {
        // Create directory group
        final DirectoryGroup testGroup = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroup"));

        // Try to set parents group to child causing a looped reference leading to an exception
        verifyExceptionIsThrown(() -> directoryGroupManagement.assignDirectoryParent(testGroup.getId(), testGroup.getId()), InvalidDirectoryGroupAssignmentException.class);
    }

    private static <T extends Throwable> void verifyExceptionIsThrown(final ThrowableAssert.ThrowingCallable tc, final Class<? extends T> exceptionType) {
        Assertions.assertThatExceptionOfType(exceptionType).isThrownBy(tc);
    }
}