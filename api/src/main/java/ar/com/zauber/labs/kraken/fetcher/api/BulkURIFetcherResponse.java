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
package ar.com.zauber.labs.kraken.fetcher.api;

import java.net.URI;
import java.util.Collection;
import java.util.Map;


/**
 * {@link BulkURIFetcher#fetch(java.util.Collection)} result
 * 
 * @author Juan F. Codagnone
 * @since Oct 12, 2009
 */
public interface BulkURIFetcherResponse {

    /** the details of each retrieval */
    Map<URI, URIFetcherResponse> getDetails();
    
    /** @return the successful uris */
    Collection<URIFetcherResponse> getSuccessfulURIs();
    
    /** @return the successful uris */
    Collection<URIFetcherResponse> getFailedURIs();
}
