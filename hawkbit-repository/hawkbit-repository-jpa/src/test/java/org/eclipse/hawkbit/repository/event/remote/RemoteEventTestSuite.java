package org.eclipse.hawkbit.repository.event.remote;

import org.eclipse.hawkbit.repository.event.remote.entity.ActionEventTest;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetCreatedEventTest;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetTagEventTest;
import org.eclipse.hawkbit.repository.event.remote.entity.DistributionSetUpdatedEventTest;
import org.eclipse.hawkbit.repository.event.remote.entity.RolloutEventTest;
import org.eclipse.hawkbit.repository.event.remote.entity.RolloutGroupEventTest;
import org.eclipse.hawkbit.repository.event.remote.entity.SoftwareModuleEventTest;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetEventTest;
import org.eclipse.hawkbit.repository.event.remote.entity.TargetTagEventTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ RemoteIdEventTest.class, RemoteTenantAwareEventTest.class, ActionEventTest.class, //
    DistributionSetCreatedEventTest.class, DistributionSetTagEventTest.class, DistributionSetUpdatedEventTest.class, //
    RolloutEventTest.class, RolloutGroupEventTest.class, SoftwareModuleEventTest.class, TargetEventTest.class, //
    TargetTagEventTest.class //
})
public class RemoteEventTestSuite {
}
