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
import io.qameta.allure.Step;
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
        final DirectoryGroup testGroup = createAndCheckBasicGroup();

        /* Check expected closures:
         *   - testGroup <-> testGroup | depth: 0
         * Total: 1
         */
        assertThat(directoryTreeRepository.count()).isEqualTo(1);
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroup, testGroup, 0)).isTrue();
    }

    @Test
    @Description("Setting a parent for a group creates a closure entry between them")
    public void settingGroupParentTriggersClosure() {
        // Create and check base group
        final DirectoryGroup testGroupChild = createAndCheckBasicGroup();
        // Create and assign parent with checked closures
        final DirectoryGroup testGroupParent = createAndCheckParentGroup(testGroupChild);

        /* Check complete final hierarchy:
         *   - parent <-> parent | depth: 0
         *   - child <-> child   | depth: 0
         *   - parent <-> child  | depth: 1
         * Total: 3
         */
        assertThat(directoryTreeRepository.count()).isEqualTo(3);
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupParent, 0))
                .isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupChild, testGroupChild, 0))
                .isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupChild, 1))
                .isTrue();
    }

    @Test
    @Description("Setting multiple levels of group parents creates a closure hierarchy")
    public void multipleGroupParentLevelsTriggerClosureHierarchy() {
        // Create and check base group
        final DirectoryGroup testGroupChild = createAndCheckBasicGroup();
        // Create and assign parent with checked closures
        final DirectoryGroup testGroupParent = createAndCheckParentGroup(testGroupChild);
        // Create and assign grandparent with checked closures
        final DirectoryGroup testGroupGrandparent = createAndCheckParentGroup(testGroupParent);

        /* Check complete final hierarchy:
         *   - grandparent <-> grandparent | depth: 0
         *   - parent <-> parent           | depth: 0
         *   - child <-> child             | depth: 0
         *   - grandparent <-> parent      | depth: 1
         *   - parent <-> child            | depth: 1
         *   - grandparent <-> child       | depth: 2
         * Total: 6
         */
        assertThat(directoryTreeRepository.count()).isEqualTo(6);
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent,
                testGroupGrandparent, 0)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupParent, 0))
                .isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupChild, testGroupChild, 0))
                .isTrue();
        assertThat(
                directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent, testGroupParent, 1))
                        .isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupChild, 1))
                .isTrue();
        assertThat(
                directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent, testGroupChild, 2))
                        .isTrue();
    }

    @Test
    @Description("Moving a group to another parent results in the correct closure hierarchy")
    public void movingGroupLeadsToCorrectClosureHierarchy() {
        // Create and check basic group hierarchy
        final DirectoryGroup testGroupChild = createAndCheckBasicGroup();
        final DirectoryGroup testGroupParent = createAndCheckParentGroup(testGroupChild);
        final DirectoryGroup testGroupGrandparent = createAndCheckParentGroup(testGroupParent);

        // Create another grandparent and move parent to it
        final DirectoryGroup testGroupSecondGrandparent = directoryGroupManagement
                .create(new JpaDirectoryGroupBuilder().create().name("testGroupSecondGrandparent"));
        directoryGroupManagement.assignDirectoryParent(testGroupParent.getId(), testGroupSecondGrandparent.getId());

        /* 3. Ensure old closures are gone and new exists
         * Expected closures (without self reference):
         *   - grandparent <-> parent | depth: 1
         *   - parent <-> child       | depth: 1
         *   - grandparent <-> child  | depth: 2
         * Total: 7 (incl. 4 self references)
         */
        assertThat(directoryTreeRepository.count()).isEqualTo(7);
        // ensure expected closures are there
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupSecondGrandparent,
                testGroupParent, 1)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupChild, 1))
                .isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupSecondGrandparent,
                testGroupChild, 2)).isTrue();
        // ensure closures with old grandparent are gone
        assertThat(directoryTreeRepository.existsById(new DirectoryTreeId(testGroupGrandparent, testGroupParent)))
                .isFalse();
        assertThat(directoryTreeRepository.existsById(new DirectoryTreeId(testGroupGrandparent, testGroupChild)))
                .isFalse();
    }

    @Test
    @Description("Deleting leaf groups clears closure entries")
    public void deletingLeafGroupsClearsClosure() {
        // Create and check basic group hierarchy
        final DirectoryGroup testGroupChild = createAndCheckBasicGroup();
        final DirectoryGroup testGroupParent = createAndCheckParentGroup(testGroupChild);
        final DirectoryGroup testGroupGrandparent = createAndCheckParentGroup(testGroupParent);

        // Delete leaf child group and ensure closures are removed as well (depth does
        // not need to be considered)
        final long childId = testGroupChild.getId();
        directoryGroupManagement.deleteById(childId);
        assertThat(directoryTreeRepository.existsById(new DirectoryTreeId(testGroupParent.getId(), childId))).isFalse();
        assertThat(directoryTreeRepository.existsById(new DirectoryTreeId(testGroupParent.getId(), childId))).isFalse();

        // Delete leaf parent group and ensure closures are removed as well (depth does
        // not need to be considered)
        final long parentId = testGroupParent.getId();
        directoryGroupManagement.deleteById(parentId);
        assertThat(directoryTreeRepository.existsById(new DirectoryTreeId(testGroupParent.getId(), parentId)))
                .isFalse();

        // Ensure only grandparent self reference is left
        assertThat(directoryTreeRepository.count()).isEqualTo(1);
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent,
                testGroupGrandparent, 0)).isTrue();
    }

    @Test
    @Description("Deleting an intermediate parent deletes sub groups and clears closures")
    public void deletingIntermediateParentClearsClosure() {
        // Create and check basic group hierarchy
        final DirectoryGroup testGroupChild = createAndCheckBasicGroup();
        final DirectoryGroup testGroupParent = createAndCheckParentGroup(testGroupChild);
        final DirectoryGroup testGroupGrandparent = createAndCheckParentGroup(testGroupParent);

        // Deleting intermediate parent only
        directoryGroupManagement.deleteById(testGroupParent.getId());

        // Ensure directory groups (parent and child) are gone
        assertThat(directoryGroupRepository.existsById(testGroupParent.getId())).isFalse();
        assertThat(directoryGroupRepository.existsById(testGroupChild.getId())).isFalse();

        // Ensure closures are gone too, also between grandparent and child (only
        // grandparent self reference is left)
        assertThat(directoryTreeRepository.count()).isEqualTo(1);
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent,
                testGroupGrandparent, 0)).isTrue();
    }

    @Test
    @Description("Deleting a hierarchy parent also deletes all sub groups")
    public void deletingParentGroupCascades() {
        // Create and check basic group hierarchy
        final DirectoryGroup testGroupChild = createAndCheckBasicGroup();
        final DirectoryGroup testGroupParent = createAndCheckParentGroup(testGroupChild);
        final DirectoryGroup testGroupGrandparent = createAndCheckParentGroup(testGroupParent);

        // Deleting grandparent only
        directoryGroupManagement.deleteById(testGroupGrandparent.getId());

        // Ensure directory groups are gone
        assertThat(directoryGroupRepository.count()).isEqualTo(0);

        // Ensure closures are gone too, no closures left
        assertThat(directoryTreeRepository.count()).isEqualTo(0);
    }

    @Test
    @Description("Deleting all groups of a tenant is not blocked by a database restrictions")
    public void deletingAllGroupsOfATenantIsNotBlocked() {
        // Create and check basic group hierarchy
        final DirectoryGroup testGroupChild = createAndCheckBasicGroup();
        final DirectoryGroup testGroupParent = createAndCheckParentGroup(testGroupChild);
        final DirectoryGroup testGroupGrandparent = createAndCheckParentGroup(testGroupParent);

        // Delete all groups of single tenant
        directoryGroupRepository.deleteByTenant("DEFAULT");

        // Ensure all groups are removed
        assertThat(directoryGroupRepository.count()).isEqualTo(0);
        // Ensure closures are removed
        assertThat(directoryTreeRepository.count()).isEqualTo(0);
    }

    @Test
    @Description("Setting a parent group that causes a looped reference is not possible")
    public void preventSettingGroupParentCausingLoop() {
        // Create and check basic group hierarchy
        final DirectoryGroup testGroupChild = createAndCheckBasicGroup();
        final DirectoryGroup testGroupParent = createAndCheckParentGroup(testGroupChild);

        // Try to set parents group to child causing a looped reference leading to an
        // exception
        verifyExceptionIsThrown(
                () -> directoryGroupManagement.assignDirectoryParent(testGroupParent.getId(), testGroupChild.getId()),
                InvalidDirectoryGroupAssignmentException.class);
    }

    @Test
    @Description("Setting parent group to self is not possible")
    public void preventSettingGroupParentToSelf() {
        // Create directory group
        final DirectoryGroup testGroup = createAndCheckBasicGroup();

        // Try to set parents group to child causing a looped reference leading to an
        // exception
        verifyExceptionIsThrown(
                () -> directoryGroupManagement.assignDirectoryParent(testGroup.getId(), testGroup.getId()),
                InvalidDirectoryGroupAssignmentException.class);
    }

    /*
     * Reusable create and check steps
     */
    @Step("Create a single group and check closure")
    private DirectoryGroup createAndCheckBasicGroup() {
        // Create directory group
        final DirectoryGroup testGroup = directoryGroupManagement
                .create(new JpaDirectoryGroupBuilder().create().name("testGroup"));

        /* Expected closure only self reference:
         *   - group <-> group | depth: 0
         */
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroup, testGroup, 0)).isTrue();

        return testGroup;
    }

    @Step("Create a parent group, assign it to a child and check closure (also for possible grandchildren)")
    private DirectoryGroup createAndCheckParentGroup(DirectoryGroup childGroup) {
        // Create parent directory group
        DirectoryGroup parentGroup = directoryGroupManagement
                .create(new JpaDirectoryGroupBuilder().create().name("testGroup"));

        // assign parent to child
        childGroup = directoryGroupManagement.assignDirectoryParent(childGroup.getId(), parentGroup.getId());

        /* Expected closure self reference and child relationship:
         *   - parent <-> parent            | depth: 0
         *   - parent <-> child             | depth: 1
         *  (- parent <-> children of child | depth: 2) Optional
         */
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(parentGroup, parentGroup, 0)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(parentGroup, childGroup, 1)).isTrue();
        // if child also has children, also check second generation closure (depth 2)
        for (DirectoryGroup grandchildGroup : childGroup.getDirectoryChildren()) {
            assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(parentGroup, grandchildGroup, 2))
                    .isTrue();
        }

        return parentGroup;
    }

    private static <T extends Throwable> void verifyExceptionIsThrown(final ThrowableAssert.ThrowingCallable tc,
            final Class<? extends T> exceptionType) {
        Assertions.assertThatExceptionOfType(exceptionType).isThrownBy(tc);
    }
}