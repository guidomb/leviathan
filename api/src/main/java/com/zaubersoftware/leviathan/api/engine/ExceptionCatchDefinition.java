/**
 * Copyright (c) 2009-2012 Zauber S.A. <http://zauberlabs.com/>
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

/**
 * TODO Descripcion de la clase. Los comentarios van en castellano.
 *
 *
 * @author Martin Silva
 * @since Sep 2, 2011
 */
public interface ExceptionCatchDefinition<T> {

    <E extends Throwable> AfterExceptionCatchDefinition<T> on(Class<E> throwableClass);

}
