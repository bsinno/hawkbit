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
import org.eclipse.hawkbit.exception.ErrorHandlerChain;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.support.converter.MessageConversionException;

/**
 * An error handler for message conversion exception resulting from AMQP.
 */
public class MessageConversionExceptionHandler implements ConditionalErrorHandler<Throwable> {

    @Override
    public void doHandle(Throwable t, ErrorHandlerChain<Throwable> chain) {
        if (t.getCause() instanceof MessageConversionException) {
            throw new AmqpRejectAndDontRequeueException("The message could not be parsed", t.getCause());
        } else {
            chain.handle(t);
        }
    }
}

