/*
 * Copyright (c) 2010 Zauber S.A.  -- All rights reserved
 */
package ar.com.zauber.leviathan.common.async.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.Validate;

import ar.com.zauber.leviathan.common.async.FetchJob;
import ar.com.zauber.leviathan.common.async.FetchQueue;

/**
 * {@link FetchQueue} que delega su funcionamiento en un {@link BlockingQueue}.
 * 
 * @author Juan F. Codagnone
 * @since Feb 16, 2010
 */
public class BlockingQueueFetchQueue implements FetchQueue {
    private final BlockingQueue<FetchJob> target;
    private AtomicBoolean shutdownFlag = new AtomicBoolean(false);
    private final long timeout;
    
    /** @see BlockingQueueFetchQueue */
    public BlockingQueueFetchQueue(final BlockingQueue<FetchJob> target) {
        this(target, 500);
    }
    
    /**
     * @param target   Queue que se "wrappea". Es quien en realidad tiene 
     *                 implementada las operaciones
     * @param timeout  Como las {@link BlockingQueue} retornan null si no hay 
     *                 elementos, se le debe indicar un timeout de espera para
     *                 volver a ver si hay elementos (y de esta forma "blockear").
     *                 Est� dicho en milisegundos.  
     */
    public BlockingQueueFetchQueue(final BlockingQueue<FetchJob> target,
            final long timeout) {
        Validate.notNull(target, "target is null");
        Validate.isTrue(timeout > 0, "timeout must be positive");
        
        this.target = target;
        this.timeout = timeout;
    }
    
    
    /** @see FetchQueue#add(FetchJob) */
    public final void add(final  FetchJob fetchJob) {
        Validate.notNull(fetchJob, "null jobs are not accepted");
        if(shutdownFlag.get()) {
            throw new RejectedExecutionException(
                    "We do not accept jobs, while shutting down");
        } else {
            try {
                target.put(fetchJob);
            } catch (final InterruptedException e) {
                throw new UnhandledException(e);
            }
        }
    }

    /** @see FetchQueue#isEmpty() */
    public final boolean isEmpty() {
        return target.isEmpty();
    }

    /** template method usado para notificar que se llam� al pool. es interesante
     *  para armar los test. luego de que alguien hace pool, hacer un add. etc */
    public void onPoll() {
        // void
    }
    
    /** @see FetchQueue#poll() */
    public final FetchJob poll() throws InterruptedException {
        FetchJob job;
        onPoll();
        do {
            job = target.poll(timeout, TimeUnit.MILLISECONDS);
            
            // no hay nada en la cola, y notificaron para el shutdown.
            if(job == null && shutdownFlag.get() && target.isEmpty()) {
                throw new InterruptedException("Shutting down");
            }
        } while(job == null);
        
        return job;
    }
    
    /** @see FetchQueue#shutdown() */
    public final void shutdown() {
        shutdownFlag.set(true);
    }
    
    /** @see FetchQueue#isShutdown() */
    public final boolean isShutdown() {
        return shutdownFlag.get();
    }
}
