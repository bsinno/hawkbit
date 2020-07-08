package org.eclipse.hawkbit.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.eclipse.hawkbit.repository.DirectoryGroupManagement;
import org.eclipse.hawkbit.repository.exception.EntityAlreadyExistsException;
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

        /* Expected closures:
         *   - grandparent <-> parent | depth: 1
         *   - parent <-> parent      | depth: 1
         *   - grandparent <-> child  | depth: 2
         */
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent, testGroupParent, 1)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupChild, 1)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent, testGroupChild, 2)).isTrue();
    }

    @Test
    @Description("Deleting group clears closure entries")
    public void deletingGroupClearsClosure() {
        // Create directory groups
        final DirectoryGroup testGroupGrandparent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupGrandParent"));
        final DirectoryGroup testGroupParent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupParent"));
        final DirectoryGroup testGroupChild = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupChild"));

        // 1. Set initial parent/child relationships
        directoryGroupManagement.assignDirectoryParent(testGroupParent.getId(), testGroupGrandparent.getId());
        directoryGroupManagement.assignDirectoryParent(testGroupChild.getId(), testGroupParent.getId());

        /* Expected closures:
         *   - grandparent <-> parent | depth: 1
         *   - parent <-> parent      | depth: 1
         *   - grandparent <-> child  | depth: 2
         */
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
    }


    @Test
    @Description("Deleting parent group is blocked by a database restriction")
    public void deletingParentGroupIsBlocked() {
        // Create directory groups
        final DirectoryGroup testGroupGrandparent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupGrandparent"));
        final DirectoryGroup testGroupParent = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupParent"));
        final DirectoryGroup testGroupChild = directoryGroupManagement.create(new JpaDirectoryGroupBuilder().create().name("testGroupChild"));

        // Set parent/child relationships
        directoryGroupManagement.assignDirectoryParent(testGroupParent.getId(), testGroupGrandparent.getId());
        directoryGroupManagement.assignDirectoryParent(testGroupChild.getId(), testGroupParent.getId());

        // deleting parent only is not possible
        verifyExceptionIsThrown(() -> directoryGroupManagement.deleteById(testGroupGrandparent.getId()), EntityAlreadyExistsException.class);

        // ensure closures are still valid
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupParent, testGroupChild, 1)).isTrue();
        assertThat(directoryTreeRepository.existsByAncestorAndDescendantAndDepth(testGroupGrandparent, testGroupParent, 1)).isTrue();
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