/**
 * Copyright (c) 2009-2011 Zauber S.A. <http://www.zaubersoftware.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zaubersoftware.leviathan.api.engine;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import ar.com.zauber.leviathan.api.AsyncUriFetcher;
import ar.com.zauber.leviathan.api.URIFetcherResponse.URIAndCtx;


/**
 * TODO: Description of the class, Comments in english by default  
 * 
 * 
 * @author Juan F. Codagnone
 * @since Dec 28, 2011
 */
public interface FetchingEngine extends Engine {

    /**
     * Performs the GET request for the given {@link URI}.
     *
     * @param uri The {@link URI} to be fetched.
     * @return The {@link Engine}
     */
    FetchingEngine doGet(URI uri, ProcessingFlow flow);

    /** @see #doGet(URI) */
    FetchingEngine doGet(URIAndCtx uriAndCtx, ProcessingFlow flow);

    /**
     * Blocks until all tasks have completed execution
     *
     * @return The {@link Engine}
     */
    FetchingEngine awaitIdleness() throws InterruptedException;

    /**
     * Blocks until all tasks have completed execution,
     * or the timeout occurs, or the current thread is interrupted, whichever
     * happens first.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return <tt>true</tt> if this executor terminated and <tt>false</tt> if
     *         the timeout elapsed before termination
     */
    boolean awaitIdleness(long timeout, TimeUnit unit) throws InterruptedException;

    /** @see  AsyncUriFetcher#shutdown() */
    void shutdown();
    
    /** @see  AsyncUriFetcher#shutdownNow()() */
    void shutdownNow();
}