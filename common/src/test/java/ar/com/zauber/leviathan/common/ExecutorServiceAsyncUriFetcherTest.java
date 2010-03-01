/**
 * Copyright (c) 2009-2010 Zauber S.A. <http://www.zaubersoftware.com/>
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
package ar.com.zauber.leviathan.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import ar.com.zauber.commons.dao.Closure;
import ar.com.zauber.leviathan.api.AsyncUriFetcher;
import ar.com.zauber.leviathan.api.URIFetcher;
import ar.com.zauber.leviathan.api.URIFetcherResponse;
import ar.com.zauber.leviathan.api.URIFetcherResponse.URIAndCtx;
import ar.com.zauber.leviathan.common.async.FetchQueueAsyncUriFetcher;
import ar.com.zauber.leviathan.common.mock.FixedURIFetcher;
import ar.com.zauber.leviathan.common.utils.DirectExecutorService;



/**
 * tests {@link ExecutorServiceAsyncUriFetcher} 
 * 
 * 
 * @author Juan F. Codagnone
 * @since Jan 21, 2010
 */
public class ExecutorServiceAsyncUriFetcherTest {

    /** 
     * Arma un {@link ExecutorServiceAsyncUriFetcherTest} con un 
     * {@link ExecutorService} de 2 threads con cola infinita.
     * 
     * Usa URIFetcher que gracias a un semaforo no hace nada hasta que se diga
     * (queda trabado; simulando una descarga lenta). De esta forma todos los
     * trabajos  se encolan. En un momento del test, se libera el semaforo, y 
     * todas las descargas deberian ocurrir
     */
    @Test
    public final void foo() throws Exception {
        final URI foo = new URI("http://foo");
        final URI foo1 = new URI("http://foo1");
        final URI foo2 = new URI("http://foo2");
        final URI foo3 = new URI("http://foo3");

        final Map<URI, String> map = new HashMap<URI, String>();
        final String resource =
            "ar/com/zauber/leviathan/impl/mock/noexiste.txt";
        map.put(foo, resource);
        map.put(foo2, resource);
        final URIFetcher fixedUriFetcher = new FixedURIFetcher(map);

        final CountDownLatch available = new CountDownLatch(1);
        final Random random = new Random();
        final AsyncUriFetcher fetcher = new ExecutorServiceAsyncUriFetcher(
            Executors.newScheduledThreadPool(2), 
            new AbstractURIFetcher() {
                public URIFetcherResponse fetch(final URIAndCtx uri) {
                    try {
                        available.await();
                        Thread.sleep(random.nextInt(500));
                        return fixedUriFetcher.fetch(uri);
                    } catch (final InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        final List<URIFetcherResponse> responses = 
            new CopyOnWriteArrayList<URIFetcherResponse>();
        
        final CountDownLatch done = new CountDownLatch(4);
        final Closure<URIFetcherResponse> closure = 
            new Closure<URIFetcherResponse>() {
            public void execute(final URIFetcherResponse t) {
                responses.add(t);
                done.countDown();
            }
        };
        
        fetcher.fetch(foo, closure);
        fetcher.fetch(foo1, closure);
        fetcher.fetch(foo2, closure);
        fetcher.fetch(foo3, closure);
        
        available.countDown();
        done.await(2, TimeUnit.SECONDS);
        Assert.assertEquals(4, responses.size());
    }
    
    /**
     * Prueba que funcione el {@link FetchQueueAsyncUriFetcher#awaitIdleness()}  
     */
    @Test(timeout = 2000)
    public final void waitIdlenesss() 
        throws URISyntaxException, InterruptedException {
        final AsyncUriFetcher fetcher = new ExecutorServiceAsyncUriFetcher(
                new DirectExecutorService(), 
                new FixedURIFetcher(new HashMap<URI, String>()));
        final URI uri = new URI("http://foo");
        
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger i = new AtomicInteger(0);
        
        final int n = 10000; 
        
        fetcher.fetch(uri, new Closure<URIFetcherResponse>() {
            public void execute(final URIFetcherResponse t) {
                for(int j = 0; j < n; j++) {
                    fetcher.fetch(uri, new Closure<URIFetcherResponse>() {
                        /** @see Closure#execute(Object) */
                        public void execute(final URIFetcherResponse t) {
                            i.incrementAndGet();
                        }
                    });
                }
                latch.countDown();
            }
        });
        latch.await();
        fetcher.awaitIdleness();
        Assert.assertEquals(n, i.get());
        fetcher.shutdown();
    }
    
    
    /**
     * Prueba que funcione el {@link FetchQueueAsyncUriFetcher#awaitIdleness()}
     * con execpciones  
     */
    @Test(timeout = 2000)
    public final void waitIdlenesssWithExceptions() 
        throws URISyntaxException, InterruptedException {
        final AsyncUriFetcher fetcher = new ExecutorServiceAsyncUriFetcher(
                new DirectExecutorService(), 
                new FixedURIFetcher(new HashMap<URI, String>()));
        final URI uri = new URI("http://foo");
        
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicInteger i = new AtomicInteger(0);
        
        final int n = 10000; 
        
        Logger.getLogger(ExecutorServiceAsyncUriFetcher.class).setLevel(Level.FATAL);
        
        fetcher.fetch(uri, new Closure<URIFetcherResponse>() {
            public void execute(final URIFetcherResponse t) {
                for(int j = 0; j < n; j++) {
                    fetcher.fetch(uri, new Closure<URIFetcherResponse>() {
                        /** @see Closure#execute(Object) */
                        public void execute(final URIFetcherResponse t) {
                            i.incrementAndGet();
                            throw new IllegalStateException(
                                    "solo para probar que funcione");
                        }
                    });
                }
                latch.countDown();
            }
        });
        latch.await();
        fetcher.awaitIdleness();
        Assert.assertEquals(n, i.get());
        fetcher.shutdown();
    }

}