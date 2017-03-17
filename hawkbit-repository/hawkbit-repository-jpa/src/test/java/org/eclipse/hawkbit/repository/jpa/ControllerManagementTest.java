/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.hawkbit.im.authentication.SpPermission.SpringEvalExpressions.CONTROLLER_ROLE_ANONYMOUS;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.RandomUtils;
import org.eclipse.hawkbit.repository.RepositoryProperties;
import org.eclipse.hawkbit.repository.event.remote.TargetAssignDistributionSetEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.ActionCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.ActionUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.CancelTargetAssignmentEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.SoftwareModuleCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.SoftwareModuleUpdatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetCreatedEvent;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetUpdatedEvent;
import org.eclipse.hawkbit.repository.exception.CancelActionNotAllowedException;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.repository.model.Artifact;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.SoftwareModule;
import org.eclipse.hawkbit.repository.model.Target;
import org.eclipse.hawkbit.repository.model.TargetUpdateStatus;
import org.eclipse.hawkbit.repository.test.matcher.Expect;
import org.eclipse.hawkbit.repository.test.matcher.ExpectEvents;
import org.eclipse.hawkbit.repository.test.util.TestdataFactory;
import org.eclipse.hawkbit.repository.test.util.WithSpringAuthorityRule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps;

import ru.yandex.qatools.allure.annotations.Description;
import ru.yandex.qatools.allure.annotations.Features;
import ru.yandex.qatools.allure.annotations.Step;
import ru.yandex.qatools.allure.annotations.Stories;

@Features("Component Tests - Repository")
@Stories("Controller Management")
public class ControllerManagementTest extends AbstractJpaIntegrationTest {

    @Autowired
    private RepositoryProperties repositoryProperties;

    @Test
    @Description("Verifies that management queries react as specfied on calls for non existing entities.")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = SoftwareModuleCreatedEvent.class, count = 1) })
    public void nonExistingEntityQueries() throws URISyntaxException {
        final Target target = testdataFactory.createTarget();
        final SoftwareModule module = testdataFactory.createSoftwareModuleOs();

        assertThatThrownBy(() -> controllerManagement
                .addCancelActionStatus(entityFactory.actionStatus().create(1234L).status(Action.Status.FINISHED)))
                        .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("1234")
                        .hasMessageContaining("Action");

        assertThatThrownBy(() -> controllerManagement
                .addInformationalActionStatus(entityFactory.actionStatus().create(1234L).status(Action.Status.RUNNING)))
                        .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("1234")
                        .hasMessageContaining("Action");

        assertThatThrownBy(() -> controllerManagement
                .addUpdateActionStatus(entityFactory.actionStatus().create(1234L).status(Action.Status.FINISHED)))
                        .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("1234")
                        .hasMessageContaining("Action");

        assertThat(controllerManagement.findActionWithDetails(1234L)).isNotPresent();
        assertThat(controllerManagement.findByControllerId("1234")).isNotPresent();
        assertThat(controllerManagement.findByTargetId(1234L)).isNotPresent();

        assertThatThrownBy(() -> controllerManagement.findOldestActiveActionByTarget("1234"))
                .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("1234")
                .hasMessageContaining("Target");

        assertThatThrownBy(() -> controllerManagement
                .getActionForDownloadByTargetAndSoftwareModule(target.getControllerId(), 1234L))
                        .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("1234")
                        .hasMessageContaining("SoftwareModule");

        assertThatThrownBy(
                () -> controllerManagement.getActionForDownloadByTargetAndSoftwareModule("1234", module.getId()))
                        .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("1234")
                        .hasMessageContaining("Target");

        assertThat(controllerManagement
                .getActionForDownloadByTargetAndSoftwareModule(target.getControllerId(), module.getId()).isPresent())
                        .isFalse();

        assertThatThrownBy(() -> controllerManagement.hasTargetArtifactAssigned(1234L, "XXX"))
                .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("1234")
                .hasMessageContaining("Target");

        assertThatThrownBy(() -> controllerManagement.hasTargetArtifactAssigned("1234", "XXX"))
                .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("1234")
                .hasMessageContaining("Target");

        assertThat(controllerManagement.hasTargetArtifactAssigned(target.getControllerId(), "XXX")).isFalse();
        assertThat(controllerManagement.hasTargetArtifactAssigned(target.getId(), "XXX")).isFalse();

        assertThatThrownBy(() -> controllerManagement.registerRetrieved(1234L, "test message"))
                .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("1234")
                .hasMessageContaining("Action");

        assertThatThrownBy(() -> controllerManagement.updateControllerAttributes("1234", Maps.newHashMap()))
                .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("1234")
                .hasMessageContaining("Target");

        assertThatThrownBy(() -> controllerManagement.updateLastTargetQuery("1234", new URI("http://test.com")))
                .isInstanceOf(EntityNotFoundException.class).hasMessageContaining("1234")
                .hasMessageContaining("Target");
    }

    @Test
    @Description("Controller confirms successfull update with FINISHED status.")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = DistributionSetCreatedEvent.class, count = 1),
            @Expect(type = ActionCreatedEvent.class, count = 1), @Expect(type = ActionUpdatedEvent.class, count = 1),
            @Expect(type = TargetUpdatedEvent.class, count = 2),
            @Expect(type = TargetAssignDistributionSetEvent.class, count = 1),
            @Expect(type = SoftwareModuleCreatedEvent.class, count = 3) })
    public void controllerConfirmsUpdateWithFinished() {
        final Long actionId = createTargetAndAssignDs();

        simulateIntermediateStatusOnUpdate(actionId);

        controllerManagement
                .addUpdateActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.FINISHED));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.IN_SYNC,
                Action.Status.FINISHED, Action.Status.FINISHED, false);

        assertThat(actionStatusRepository.count()).isEqualTo(6);
        assertThat(deploymentManagement.findActionStatusByAction(pageReq, actionId).getNumberOfElements()).isEqualTo(6);
    }

    @Test
    @Description("Controller confirms successfull update with FINISHED status on a action that is on canceling. "
            + "Reason: The decission to ignore the cancellation is in fact up to the controller.")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = DistributionSetCreatedEvent.class, count = 1),
            @Expect(type = ActionCreatedEvent.class, count = 1), @Expect(type = ActionUpdatedEvent.class, count = 2),
            @Expect(type = CancelTargetAssignmentEvent.class, count = 1),
            @Expect(type = TargetUpdatedEvent.class, count = 2),
            @Expect(type = TargetAssignDistributionSetEvent.class, count = 1),
            @Expect(type = SoftwareModuleCreatedEvent.class, count = 3) })
    public void controllerConfirmsUpdateWithFinishedAndIgnorsCancellationWithThat() {
        final Long actionId = createTargetAndAssignDs();
        deploymentManagement.cancelAction(actionId);

        controllerManagement
                .addUpdateActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.FINISHED));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.IN_SYNC,
                Action.Status.FINISHED, Action.Status.FINISHED, false);

        assertThat(actionStatusRepository.count()).isEqualTo(3);
        assertThat(deploymentManagement.findActionStatusByAction(pageReq, actionId).getNumberOfElements()).isEqualTo(3);
    }

    @Test
    @Description("Update server rejects cancelation feedback if action is not in CANCELING state.")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = DistributionSetCreatedEvent.class, count = 1),
            @Expect(type = ActionCreatedEvent.class, count = 1), @Expect(type = TargetUpdatedEvent.class, count = 1),
            @Expect(type = TargetAssignDistributionSetEvent.class, count = 1),
            @Expect(type = SoftwareModuleCreatedEvent.class, count = 3) })
    public void cancellationFeedbackRejectedIfActionIsNotInCanceling() {
        final Long actionId = createTargetAndAssignDs();

        try {
            controllerManagement.addCancelActionStatus(
                    entityFactory.actionStatus().create(actionId).status(Action.Status.FINISHED));
            fail("Expected " + CancelActionNotAllowedException.class.getName());
        } catch (final CancelActionNotAllowedException e) {
            // expected
        }

        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.RUNNING, Action.Status.RUNNING, true);

        assertThat(actionStatusRepository.count()).isEqualTo(1);
        assertThat(deploymentManagement.findActionStatusByAction(pageReq, actionId).getNumberOfElements()).isEqualTo(1);

    }

    @Test
    @Description("Controller confirms action cancelation with FINISHED status.")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = DistributionSetCreatedEvent.class, count = 1),
            @Expect(type = ActionCreatedEvent.class, count = 1), @Expect(type = ActionUpdatedEvent.class, count = 2),
            @Expect(type = TargetUpdatedEvent.class, count = 2),
            @Expect(type = CancelTargetAssignmentEvent.class, count = 1),
            @Expect(type = TargetAssignDistributionSetEvent.class, count = 1),
            @Expect(type = SoftwareModuleCreatedEvent.class, count = 3) })
    public void controllerConfirmsActionCancelationWithFinished() {
        final Long actionId = createTargetAndAssignDs();

        deploymentManagement.cancelAction(actionId);
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.CANCELING, Action.Status.CANCELING, true);

        simulateIntermediateStatusOnCancellation(actionId);

        controllerManagement
                .addCancelActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.FINISHED));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.IN_SYNC,
                Action.Status.CANCELED, Action.Status.FINISHED, false);

        assertThat(actionStatusRepository.count()).isEqualTo(7);
        assertThat(deploymentManagement.findActionStatusByAction(pageReq, actionId).getNumberOfElements()).isEqualTo(7);
    }

    @Test
    @Description("Controller confirms action cancelation with FINISHED status.")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = DistributionSetCreatedEvent.class, count = 1),
            @Expect(type = ActionCreatedEvent.class, count = 1), @Expect(type = ActionUpdatedEvent.class, count = 2),
            @Expect(type = TargetUpdatedEvent.class, count = 2),
            @Expect(type = CancelTargetAssignmentEvent.class, count = 1),
            @Expect(type = TargetAssignDistributionSetEvent.class, count = 1),
            @Expect(type = SoftwareModuleCreatedEvent.class, count = 3) })
    public void controllerConfirmsActionCancelationWithCanceled() {
        final Long actionId = createTargetAndAssignDs();

        deploymentManagement.cancelAction(actionId);
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.CANCELING, Action.Status.CANCELING, true);

        simulateIntermediateStatusOnCancellation(actionId);

        controllerManagement
                .addCancelActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.CANCELED));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.IN_SYNC,
                Action.Status.CANCELED, Action.Status.CANCELED, false);

        assertThat(actionStatusRepository.count()).isEqualTo(7);
        assertThat(deploymentManagement.findActionStatusByAction(pageReq, actionId).getNumberOfElements()).isEqualTo(7);
    }

    @Test
    @Description("Controller rejects action cancelation with CANCEL_REJECTED status. Action goes back to RUNNING status as it expects "
            + "that the controller will continue the original update.")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = DistributionSetCreatedEvent.class, count = 1),
            @Expect(type = ActionCreatedEvent.class, count = 1), @Expect(type = ActionUpdatedEvent.class, count = 2),
            @Expect(type = TargetUpdatedEvent.class, count = 1),
            @Expect(type = CancelTargetAssignmentEvent.class, count = 1),
            @Expect(type = TargetAssignDistributionSetEvent.class, count = 1),
            @Expect(type = SoftwareModuleCreatedEvent.class, count = 3) })
    public void controllerRejectsActionCancelationWithReject() {
        final Long actionId = createTargetAndAssignDs();

        deploymentManagement.cancelAction(actionId);
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.CANCELING, Action.Status.CANCELING, true);

        simulateIntermediateStatusOnCancellation(actionId);

        controllerManagement.addCancelActionStatus(
                entityFactory.actionStatus().create(actionId).status(Action.Status.CANCEL_REJECTED));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.RUNNING, Action.Status.CANCEL_REJECTED, true);

        assertThat(actionStatusRepository.count()).isEqualTo(7);
        assertThat(deploymentManagement.findActionStatusByAction(pageReq, actionId).getNumberOfElements()).isEqualTo(7);
    }

    @Test
    @Description("Controller rejects action cancelation with ERROR status. Action goes back to RUNNING status as it expects "
            + "that the controller will continue the original update.")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = DistributionSetCreatedEvent.class, count = 1),
            @Expect(type = ActionCreatedEvent.class, count = 1), @Expect(type = ActionUpdatedEvent.class, count = 2),
            @Expect(type = TargetUpdatedEvent.class, count = 1),
            @Expect(type = CancelTargetAssignmentEvent.class, count = 1),
            @Expect(type = TargetAssignDistributionSetEvent.class, count = 1),
            @Expect(type = SoftwareModuleCreatedEvent.class, count = 3) })
    public void controllerRejectsActionCancelationWithError() {
        final Long actionId = createTargetAndAssignDs();

        deploymentManagement.cancelAction(actionId);
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.CANCELING, Action.Status.CANCELING, true);

        simulateIntermediateStatusOnCancellation(actionId);

        controllerManagement
                .addCancelActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.ERROR));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.RUNNING, Action.Status.ERROR, true);

        assertThat(actionStatusRepository.count()).isEqualTo(7);
        assertThat(deploymentManagement.findActionStatusByAction(pageReq, actionId).getNumberOfElements()).isEqualTo(7);
    }

    @Step
    private Long createTargetAndAssignDs() {
        final Long dsId = testdataFactory.createDistributionSet().getId();
        testdataFactory.createTarget();
        assignDistributionSet(dsId, TestdataFactory.DEFAULT_CONTROLLER_ID);
        assertThat(targetManagement.findTargetByControllerID(TestdataFactory.DEFAULT_CONTROLLER_ID).get()
                .getUpdateStatus()).isEqualTo(TargetUpdateStatus.PENDING);

        return deploymentManagement.findActiveActionsByTarget(TestdataFactory.DEFAULT_CONTROLLER_ID).get(0).getId();
    }

    @Step
    private void simulateIntermediateStatusOnCancellation(final Long actionId) {
        controllerManagement
                .addCancelActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.RUNNING));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.CANCELING, Action.Status.RUNNING, true);

        controllerManagement
                .addCancelActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.DOWNLOAD));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.CANCELING, Action.Status.DOWNLOAD, true);

        controllerManagement
                .addCancelActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.RETRIEVED));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.CANCELING, Action.Status.RETRIEVED, true);

        controllerManagement
                .addCancelActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.WARNING));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.CANCELING, Action.Status.WARNING, true);
    }

    @Step
    private void simulateIntermediateStatusOnUpdate(final Long actionId) {
        controllerManagement
                .addUpdateActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.RUNNING));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.RUNNING, Action.Status.RUNNING, true);

        controllerManagement
                .addUpdateActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.DOWNLOAD));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.RUNNING, Action.Status.DOWNLOAD, true);

        controllerManagement
                .addUpdateActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.RETRIEVED));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.RUNNING, Action.Status.RETRIEVED, true);

        controllerManagement
                .addUpdateActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.WARNING));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.RUNNING, Action.Status.WARNING, true);
    }

    private void assertActionStatus(final Long actionId, final String controllerId,
            final TargetUpdateStatus expectedTargetUpdateStatus, final Action.Status expectedActionActionStatus,
            final Action.Status expectedActionStatus, final boolean actionActive) {
        final TargetUpdateStatus targetStatus = targetManagement.findTargetByControllerID(controllerId).get()
                .getUpdateStatus();
        assertThat(targetStatus).isEqualTo(expectedTargetUpdateStatus);
        final Action action = deploymentManagement.findAction(actionId).get();
        assertThat(action.getStatus()).isEqualTo(expectedActionActionStatus);
        assertThat(action.isActive()).isEqualTo(actionActive);
        final List<ActionStatus> actionStatusList = deploymentManagement.findActionStatusByAction(pageReq, actionId)
                .getContent();
        assertThat(actionStatusList.get(actionStatusList.size() - 1).getStatus()).isEqualTo(expectedActionStatus);
        if (actionActive) {
            assertThat(controllerManagement.findOldestActiveActionByTarget(controllerId).get().getId())
                    .isEqualTo(actionId);
        }
    }

    @Test
    @Description("Verifies that assignement verification works based on SHA1 hash. By design it is not important which artifact "
            + "is actually used for the check as long as they have an identical binary, i.e. same SHA1 hash. ")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = DistributionSetCreatedEvent.class, count = 2),
            @Expect(type = ActionCreatedEvent.class, count = 1), @Expect(type = TargetUpdatedEvent.class, count = 1),
            @Expect(type = TargetAssignDistributionSetEvent.class, count = 1),
            @Expect(type = SoftwareModuleCreatedEvent.class, count = 6),
            @Expect(type = SoftwareModuleUpdatedEvent.class, count = 2) })
    public void hasTargetArtifactAssignedIsTrueWithMultipleArtifacts() {
        final byte[] random = RandomUtils.nextBytes(5 * 1024);

        final DistributionSet ds = testdataFactory.createDistributionSet("");
        final DistributionSet ds2 = testdataFactory.createDistributionSet("2");
        Target savedTarget = testdataFactory.createTarget();

        // create two artifacts with identical SHA1 hash
        final Artifact artifact = artifactManagement.createArtifact(new ByteArrayInputStream(random),
                ds.findFirstModuleByType(osType).get().getId(), "file1", false);
        final Artifact artifact2 = artifactManagement.createArtifact(new ByteArrayInputStream(random),
                ds2.findFirstModuleByType(osType).get().getId(), "file1", false);
        assertThat(artifact.getSha1Hash()).isEqualTo(artifact2.getSha1Hash());

        assertThat(
                controllerManagement.hasTargetArtifactAssigned(savedTarget.getControllerId(), artifact.getSha1Hash()))
                        .isFalse();
        savedTarget = assignDistributionSet(ds.getId(), savedTarget.getControllerId()).getAssignedEntity().iterator()
                .next();
        assertThat(
                controllerManagement.hasTargetArtifactAssigned(savedTarget.getControllerId(), artifact.getSha1Hash()))
                        .isTrue();
        assertThat(
                controllerManagement.hasTargetArtifactAssigned(savedTarget.getControllerId(), artifact2.getSha1Hash()))
                        .isTrue();
    }

    @Test
    @Description("Register a controller which does not exist")
    public void findOrRegisterTargetIfItDoesNotexist() {
        final Target target = controllerManagement.findOrRegisterTargetIfItDoesNotexist("AA", null);
        assertThat(target).as("target should not be null").isNotNull();

        final Target sameTarget = controllerManagement.findOrRegisterTargetIfItDoesNotexist("AA", null);
        assertThat(target.getId()).as("Target should be the equals").isEqualTo(sameTarget.getId());
        assertThat(targetRepository.count()).as("Only 1 target should be registred").isEqualTo(1L);

        // throws exception
        try {
            controllerManagement.findOrRegisterTargetIfItDoesNotexist("", null);
            fail("should fail as target does not exist");
        } catch (final ConstraintViolationException e) {

        }
    }

    @Test
    @Description("Controller trys to finish an update process after it has been finished by an error action status.")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = DistributionSetCreatedEvent.class, count = 1),
            @Expect(type = ActionCreatedEvent.class, count = 1), @Expect(type = ActionUpdatedEvent.class, count = 1),
            @Expect(type = TargetUpdatedEvent.class, count = 2),
            @Expect(type = TargetAssignDistributionSetEvent.class, count = 1),
            @Expect(type = SoftwareModuleCreatedEvent.class, count = 3) })
    public void tryToFinishWithErrorUpdateProcessMoreThanOnce() {
        final Long actionId = createTargetAndAssignDs();

        // test and verify
        controllerManagement
                .addUpdateActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.RUNNING));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.PENDING,
                Action.Status.RUNNING, Action.Status.RUNNING, true);

        controllerManagement
                .addUpdateActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.ERROR));
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.ERROR,
                Action.Status.ERROR, Action.Status.ERROR, false);

        // try with disabled late feedback
        repositoryProperties.setRejectActionStatusForClosedAction(true);
        controllerManagement
                .addUpdateActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.FINISHED));

        // test
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.ERROR,
                Action.Status.ERROR, Action.Status.ERROR, false);

        // try with enabled late feedback - should not make a difference as it
        // only allows intermediate feedbacks and not multiple close
        repositoryProperties.setRejectActionStatusForClosedAction(false);
        controllerManagement
                .addUpdateActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.FINISHED));

        // test
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.ERROR,
                Action.Status.ERROR, Action.Status.ERROR, false);

        assertThat(actionStatusRepository.count()).isEqualTo(3);
        assertThat(deploymentManagement.findActionStatusByAction(pageReq, actionId).getNumberOfElements()).isEqualTo(3);

    }

    @Test
    @Description("Controller trys to finish an update process after it has been finished by an FINISHED action status.")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = DistributionSetCreatedEvent.class, count = 1),
            @Expect(type = ActionCreatedEvent.class, count = 1), @Expect(type = ActionUpdatedEvent.class, count = 1),
            @Expect(type = TargetUpdatedEvent.class, count = 2),
            @Expect(type = TargetAssignDistributionSetEvent.class, count = 1),
            @Expect(type = SoftwareModuleCreatedEvent.class, count = 3) })
    public void tryToFinishUpdateProcessMoreThanOnce() {
        final Long actionId = prepareFinishedUpdate().getId();

        // try with disabled late feedback
        repositoryProperties.setRejectActionStatusForClosedAction(true);
        controllerManagement
                .addUpdateActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.FINISHED));

        // test
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.IN_SYNC,
                Action.Status.FINISHED, Action.Status.FINISHED, false);

        // try with enabled late feedback - should not make a difference as it
        // only allows intermediate feedbacks and not multiple close
        repositoryProperties.setRejectActionStatusForClosedAction(false);
        controllerManagement
                .addUpdateActionStatus(entityFactory.actionStatus().create(actionId).status(Action.Status.FINISHED));

        // test
        assertActionStatus(actionId, TestdataFactory.DEFAULT_CONTROLLER_ID, TargetUpdateStatus.IN_SYNC,
                Action.Status.FINISHED, Action.Status.FINISHED, false);

        assertThat(actionStatusRepository.count()).isEqualTo(3);
        assertThat(deploymentManagement.findActionStatusByAction(pageReq, actionId).getNumberOfElements()).isEqualTo(3);

    }

    @Test
    @Description("Controller trys to send an update feedback after it has been finished which is reject as the repository is "
            + "configured to reject that.")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = DistributionSetCreatedEvent.class, count = 1),
            @Expect(type = ActionCreatedEvent.class, count = 1), @Expect(type = ActionUpdatedEvent.class, count = 1),
            @Expect(type = TargetUpdatedEvent.class, count = 2),
            @Expect(type = TargetAssignDistributionSetEvent.class, count = 1),
            @Expect(type = SoftwareModuleCreatedEvent.class, count = 3) })
    public void sendUpdatesForFinishUpdateProcessDropedIfDisabled() {
        repositoryProperties.setRejectActionStatusForClosedAction(true);

        final Action action = prepareFinishedUpdate();

        controllerManagement.addUpdateActionStatus(
                entityFactory.actionStatus().create(action.getId()).status(Action.Status.RUNNING));

        // nothing changed as "feedback after close" is disabled
        assertThat(targetManagement.findTargetByControllerID(TestdataFactory.DEFAULT_CONTROLLER_ID).get()
                .getUpdateStatus()).isEqualTo(TargetUpdateStatus.IN_SYNC);

        assertThat(actionStatusRepository.count()).isEqualTo(3);
        assertThat(deploymentManagement.findActionStatusByAction(pageReq, action.getId()).getNumberOfElements())
                .isEqualTo(3);
    }

    @Test
    @Description("Controller trys to send an update feedback after it has been finished which is accepted as the repository is "
            + "configured to accept them.")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = DistributionSetCreatedEvent.class, count = 1),
            @Expect(type = ActionCreatedEvent.class, count = 1), @Expect(type = ActionUpdatedEvent.class, count = 1),
            @Expect(type = TargetUpdatedEvent.class, count = 2),
            @Expect(type = TargetAssignDistributionSetEvent.class, count = 1),
            @Expect(type = SoftwareModuleCreatedEvent.class, count = 3) })
    public void sendUpdatesForFinishUpdateProcessAcceptedIfEnabled() {
        repositoryProperties.setRejectActionStatusForClosedAction(false);

        Action action = prepareFinishedUpdate();
        action = controllerManagement.addUpdateActionStatus(
                entityFactory.actionStatus().create(action.getId()).status(Action.Status.RUNNING));

        // nothing changed as "feedback after close" is disabled
        assertThat(targetManagement.findTargetByControllerID(TestdataFactory.DEFAULT_CONTROLLER_ID).get()
                .getUpdateStatus()).isEqualTo(TargetUpdateStatus.IN_SYNC);

        // however, additional action status has been stored
        assertThat(actionStatusRepository.findAll(pageReq).getNumberOfElements()).isEqualTo(4);
        assertThat(deploymentManagement.findActionStatusByAction(pageReq, action.getId()).getNumberOfElements())
                .isEqualTo(4);
    }

    @Test
    @Description("Ensures that target attribute update is reflected by the repository.")
    @ExpectEvents({ @Expect(type = TargetCreatedEvent.class, count = 1),
            @Expect(type = TargetUpdatedEvent.class, count = 3) })
    public void updateTargetAttributes() throws Exception {
        final String controllerId = "test123";
        final Target target = testdataFactory.createTarget(controllerId);

        securityRule.runAs(WithSpringAuthorityRule.withController("controller", CONTROLLER_ROLE_ANONYMOUS), () -> {
            addAttributeAndVerify(controllerId);
            addSecondAttributeAndVerify(controllerId);
            updateAttributeAndVerify(controllerId);
            return null;
        });

        // verify that audit information has not changed
        final Target targetVerify = targetManagement.findTargetByControllerID(controllerId).get();
        assertThat(targetVerify.getCreatedBy()).isEqualTo(target.getCreatedBy());
        assertThat(targetVerify.getCreatedAt()).isEqualTo(target.getCreatedAt());
        assertThat(targetVerify.getLastModifiedBy()).isEqualTo(target.getLastModifiedBy());
        assertThat(targetVerify.getLastModifiedAt()).isEqualTo(target.getLastModifiedAt());
    }

    @Step
    private void addAttributeAndVerify(final String controllerId) {
        final Map<String, String> testData = Maps.newHashMapWithExpectedSize(1);
        testData.put("test1", "testdata1");
        controllerManagement.updateControllerAttributes(controllerId, testData);

        assertThat(targetManagement.getControllerAttributes(controllerId)).as("Controller Attributes are wrong")
                .isEqualTo(testData);
    }

    @Step
    private void addSecondAttributeAndVerify(final String controllerId) {
        final Map<String, String> testData = Maps.newHashMapWithExpectedSize(2);
        testData.put("test2", "testdata20");
        controllerManagement.updateControllerAttributes(controllerId, testData);

        testData.put("test1", "testdata1");
        assertThat(targetManagement.getControllerAttributes(controllerId)).as("Controller Attributes are wrong")
                .isEqualTo(testData);
    }

    @Step
    private void updateAttributeAndVerify(final String controllerId) {
        final Map<String, String> testData = Maps.newHashMapWithExpectedSize(2);
        testData.put("test1", "testdata12");

        controllerManagement.updateControllerAttributes(controllerId, testData);

        testData.put("test2", "testdata20");
        assertThat(targetManagement.getControllerAttributes(controllerId)).as("Controller Attributes are wrong")
                .isEqualTo(testData);
    }
}
