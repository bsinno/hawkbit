/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.amqp;

import org.springframework.util.ErrorHandler;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * An error handler which performs delegation based on the type of {@link AmqpErrorHandler}
 */
public class DelegatingConditionalErrorHandler implements ErrorHandler {
    private final List<AmqpErrorHandler> handlers;
    private final ErrorHandler defaultHandler;

    /**
     * Constructor
     *
     * @param handlers
     *                 {@link List} of error handlers
     * @param defaultHandler
     *                  the default error handler
     */
    public DelegatingConditionalErrorHandler(final List<AmqpErrorHandler> handlers, @NotNull final ErrorHandler defaultHandler) {
        this.handlers = handlers;
        this.defaultHandler = defaultHandler;
    }

    @Override
    public void handleError(final Throwable t) {
        AmqpErrorHandlerChain.getHandler(handlers, defaultHandler).handle(t);
    }
}
