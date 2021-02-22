/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.amqp;

import org.eclipse.hawkbit.exception.ConditionalErrorHandler;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.exception.InvalidTargetAttributeException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

/**
 * An error handler for all invalid target operations resulting from AMQP.
 */
public class InvalidTargetOperationsExceptionHandler extends ConditionalRejectingErrorHandler implements ConditionalErrorHandler {

    @Override
    public boolean canHandle(Throwable e) {
        Throwable cause = e.getCause();
        return cause instanceof InvalidTargetAttributeException || cause instanceof EntityNotFoundException;
    }

    @Override
    public void handleError(Throwable t) {
        throw new AmqpRejectAndDontRequeueException(t.getCause().getMessage());
    }
}
