/**
 * Copyright (c) 2015 Bosch.IO GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.exception;

import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.Description;
import org.springframework.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@Feature("Unit Tests - Delegating Conditional Error Handler")
@Story("Delegating Conditional Error Handler")
@RunWith(MockitoJUnitRunner.class)
public class DelegatingConditionalErrorHandlerTest {

    @Test(expected = IllegalArgumentException.class)
    @Description("Verifies that with a list of conditional error handlers, the error is delegated to specific handler.")
    public void verifyDelegationHandling(){
        List<ConditionalErrorHandler> handlers = new ArrayList<>();
        handlers.add(new ConditionalErrorHandler1());
        handlers.add(new ConditionalErrorHandler2());
        new DelegatingConditionalErrorHandler(handlers, new DefaultErrorHandler()).handleError(new Throwable(new IllegalArgumentException()));
    }

    @Test(expected = RuntimeException.class)
    @Description("Verifies that with a list of conditional error handlers, undefined error is handled in default way.")
    public void verifyDefaultDelegationHandling(){
        List<ConditionalErrorHandler> handlers = new ArrayList<>();
        handlers.add(new ConditionalErrorHandler1());
        handlers.add(new ConditionalErrorHandler2());
        new DelegatingConditionalErrorHandler(handlers, new DefaultErrorHandler()).handleError(new Throwable(new RuntimeException()));
    }

    // Test class
    public class ConditionalErrorHandler1 implements ConditionalErrorHandler{

        @Override public boolean canHandle(Throwable e) {
            return e.getCause() instanceof IllegalArgumentException;
        }

        @Override public void handleError(Throwable t) {
            throw new IllegalArgumentException(t.getCause().getMessage());
        }
    }

    // Test class
    public class ConditionalErrorHandler2 implements ConditionalErrorHandler{

        @Override
        public boolean canHandle(Throwable e) {
            return e.getCause() instanceof IndexOutOfBoundsException;
        }

        @Override
        public void handleError(Throwable t) {
            throw new IndexOutOfBoundsException(t.getCause().getMessage());
        }
    }

    // Test class
    public class DefaultErrorHandler implements ErrorHandler {

        @Override
        public void handleError(Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
