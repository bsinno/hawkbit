/**
 * Copyright (c) 2020 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.jpa.model;

import java.util.List;

import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.platform.database.H2Platform;
import org.eclipse.persistence.platform.database.PostgreSQLPlatform;
import org.eclipse.persistence.queries.StoredProcedureCall;

/**
 * Interface which can be implemented by entities to be called when the entity
 * should call a procedure because the entity has been created or changed.
 */
public interface ProcedureAwareEntity {

    /**
     * Fired for the Entity creation.
     *
     * @param descriptorEvent
     *            event element
     */
    void fireCreateProcedure(DescriptorEvent descriptorEvent);

    /**
     * Fired for the Entity update.
     *
     * @param descriptorEvent
     *            event element
     */
    void fireUpdateProcedure(DescriptorEvent descriptorEvent);

    /**
     * Helper to pick the correct call for the DB platform
     * 
     * TODO: Due to a bug H2 needs a manual call, shall be changed to eclipse
     * persistence once fixed. See:
     * https://github.com/eclipse-ee4j/eclipselink/pull/840
     * 
     * It is necessary as the current EclipseLink implementation (2.7.x) does not
     * correctly handle Postgres and H2 calls without a return value
     *
     * @param descriptorEvent
     *            event element
     * @param storedProcedureCall
     *            that shall be executed
     */
    default void executeCall(final DescriptorEvent descriptorEvent, final StoredProcedureCall storedProcedureCall) {
        if (descriptorEvent.getSession().getPlatform() instanceof PostgreSQLPlatform) {
            descriptorEvent.getSession().executeSelectingCall(storedProcedureCall);
        } else if (descriptorEvent.getSession().getPlatform() instanceof H2Platform) {
            final List<Object> parameters = storedProcedureCall.getParameters();
            descriptorEvent.getSession().executeNonSelectingSQL("CALL " + storedProcedureCall.getProcedureName() + "("
                    + parameters.get(0) + ", " + parameters.get(1) + ")");
        } else {
            descriptorEvent.getSession().executeNonSelectingCall(storedProcedureCall);
        }
    }
}
