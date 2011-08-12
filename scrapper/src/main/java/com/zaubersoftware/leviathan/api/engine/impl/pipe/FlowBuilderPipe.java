/*
 * Copyright (c) 2011 Zauber S.A.  -- All rights reserved
 */
package com.zaubersoftware.leviathan.api.engine.impl.pipe;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaubersoftware.leviathan.api.engine.ExceptionHandler;

/**
 * A {@link Pipe} that builds the flow of pipes and dispatch exception to the registered {@link ExceptionHandler}s
 *
 * @param <I>
 * @param <O>
 * @author Guido Marucci Blas
 * @since Aug 12, 2011
 */
public final class FlowBuilderPipe<I, O> implements Pipe<I, O> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Iterable<Pipe<?, ?>> pipes;

    private ExceptionHandler<Throwable> defaultExceptionHandler = new ExceptionHandler<Throwable>() {
        @Override
        public void handle(final Throwable trowable) {
            logger.error("No one is willing to handle this exception. It will blow up!!!!!", trowable);
            throw new UnhandledException(trowable);
        }
    };

    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends Throwable>, ExceptionHandler> handlers =
        new HashMap<Class<? extends Throwable>, ExceptionHandler>();

    /**
     * Creates the CompositePipe.
     *
     * @param pipes
     */
    @SuppressWarnings("rawtypes")
    public FlowBuilderPipe(
            final Iterable<Pipe<?, ?>> pipes,
            final Map<Class<? extends Throwable>, ExceptionHandler> handlers) {
        Validate.notNull(pipes);
        this.pipes = pipes;
        if (handlers != null) {
            this.handlers.putAll(handlers);
        }
    }

    /**
     * Creates the CompositePipe.
     *
     */
    public FlowBuilderPipe(final Iterable<Pipe<?, ?>> pipes) {
        this(pipes, null);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public O execute(final I input) {
        Object ret = input;
        for (final Pipe pipe : pipes) {
            try {
                ret = pipe.execute(ret);
            } catch(final Throwable e) {
                logger.error("There was an error in the pipe chain execution", e);
                logger.warn("TODO: Handle exception with context stack handlers");
                if (handlers.containsKey(e.getClass())) {
                    handlers.get(e.getClass()).handle(e);
                    logger.info("The pipe's flow has been stopped");
                    break;
                } else {
                    defaultExceptionHandler.handle(e);
                }
            }
        }
        return (O) ret;
    }

    /**
     * Sets the defaultExceptionHandler.
     *
     * @param defaultExceptionHandler <code>ExceptionHandler<Throwable></code> with the defaultExceptionHandler.
     */
    public void setDefaultExceptionHandler(final ExceptionHandler<Throwable> defaultExceptionHandler) {
        Validate.notNull(defaultExceptionHandler, "The default exception handler cannot be null");
        this.defaultExceptionHandler = defaultExceptionHandler;
    }
}
