/**
 * Copyright (c) 2021 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.amqp;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

@Feature("Unit Tests - Delegating Conditional Error Handler")
@Story("Delegating Conditional Error Handler")
@RunWith(MockitoJUnitRunner.class)
public class DelegatingAmqpErrorHandlerTest {

    @Test
    @Description("Verifies that with a list of conditional error handlers, the error is delegated to specific handler.")
    public void verifyDelegationHandling(){
        List<AmqpErrorHandler> handlers = new ArrayList<>();
        handlers.add(new AmqpErrorHandler1());
        handlers.add(new AmqpErrorHandler2());
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new DelegatingConditionalErrorHandler(handlers, new DefaultErrorHandler())
                        .handleError(new Throwable(new Exception().initCause(new IllegalArgumentException()))),
                "Expected handled exception to be of type IllegalArgumentException");
    }

    @Test
    @Description("Verifies that with a list of conditional error handlers, undefined error is handled in default way.")
    public void verifyDefaultDelegationHandling(){
        List<AmqpErrorHandler> handlers = new ArrayList<>();
        handlers.add(new AmqpErrorHandler1());
        handlers.add(new AmqpErrorHandler2());
        Assertions.assertThrows(RuntimeException.class,
                () -> new DelegatingConditionalErrorHandler(handlers, new DefaultErrorHandler())
                        .handleError(new Throwable(new Exception().initCause(new NullPointerException()))),
                "Expected handled exception to be of type RuntimeException");
    }

    @Test
    @Description("Verifies that when the error does not contain a cause then it ends up in Illegal state")
    public void verifyIllegalStateExceptionWhenErrorContainsNoCause(){
        List<AmqpErrorHandler> handlers = new ArrayList<>();
        handlers.add(new AmqpErrorHandler1());
        handlers.add(new AmqpErrorHandler2());
        Assertions.assertThrows(IllegalStateException.class,
                () -> new DelegatingConditionalErrorHandler(handlers, new DefaultErrorHandler())
                        .handleError(new Throwable(new Exception())),
                "Expected handled exception to be of type RuntimeException");
    }

    // Test class
    public class AmqpErrorHandler1 implements AmqpErrorHandler {

        @Override
        public void doHandle(final Throwable t, final AmqpErrorHandlerChain chain) {
            if (t.getCause() instanceof IllegalArgumentException) {
                throw new IllegalArgumentException(t.getCause().getMessage());
            } else {
                chain.handle(t);
            }
        }
    }

    // Test class
    public class AmqpErrorHandler2 implements AmqpErrorHandler {

        @Override
        public void doHandle(final Throwable t, final AmqpErrorHandlerChain chain) {
            if (t.getCause() instanceof IndexOutOfBoundsException) {
                throw new IndexOutOfBoundsException(t.getCause().getMessage());
            } else {
                chain.handle(t);
            }
        }
    }

    // Test class
    public class DefaultErrorHandler implements ErrorHandler {

        @Override
        public void
        handleError(Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
