/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.amqp;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.support.converter.MessageConversionException;

/**
 * An error handler for message conversion exception resulting from AMQP.
 */
public class MessageConversionExceptionHandler implements AmqpErrorHandler {

    @Override
    public void doHandle(final Throwable t, final AmqpErrorHandlerChain chain) {
        // retrieving the cause of throwable as it contains the details of invalid message
        // structure which caused MessageConversionException
        if (t instanceof MessageConversionException) {
            throw new AmqpRejectAndDontRequeueException(t.getCause().getMessage());
        } else {
            chain.handle(t);
        }
    }
}

