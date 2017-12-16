/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository;

import org.eclipse.hawkbit.repository.model.ActionStatus;
import org.eclipse.hawkbit.repository.model.DistributionSetType;

/**
 * Repository constants.
 *
 */
public final class RepositoryConstants {

    /**
     * Prefix that the server puts in front of
     * {@link ActionStatus#getMessages()} if the message is generated by the
     * server.
     */
    public static final String SERVER_MESSAGE_PREFIX = "Update Server: ";

    /**
     * Number of {@link DistributionSetType}s that are generated as part of
     * default tenant setup.
     */
    public static final int DEFAULT_DS_TYPES_IN_TENANT = 3;

    /**
     * Maximum number of messages that can be retrieved by a controller for an
     * {@link Action}.
     */
    public static final int MAX_ACTION_HISTORY_MSG_COUNT = 100;

    private RepositoryConstants() {
        // Utility class.
    }

}
