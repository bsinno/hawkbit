/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.exception;

import com.google.common.base.Throwables;
import org.springframework.util.ErrorHandler;

import java.util.List;

/**
 * An error handler which performs delegation based on the type of {@link ConditionalErrorHandler}
 */
public class DelegatingConditionalErrorHandler implements ErrorHandler {

    private final List<ConditionalErrorHandler> handlers;

    public DelegatingConditionalErrorHandler(final List<ConditionalErrorHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handleError(final Throwable t) {
        for (ConditionalErrorHandler handler : handlers) {
            if (handler.canHandle(t)) {
                handler.handleError(t);
                return;
            }
        }
        Throwables.throwIfUnchecked(t);
    }
}
