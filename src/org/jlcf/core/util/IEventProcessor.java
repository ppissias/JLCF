/**
 * Copyright 2013 Petros Pissias.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jlcf.core.util;

/**
 * This generic interface is implemented
 * by the abstract queue processor. 
 * 
 * declares the method for performing an active task.
 * 
 * The class that extends the AbstractQueueProcessor will
 * define the data type.
 * 
 * @author Petros Pissias
 *
 * @param <T> the generic input to the method that will do the processing. 
 */
public interface IEventProcessor<T> {
	
	/**
	 * Method called in order to do an active task
	 * @param event the generic input to the method that will do the processing. 
	 * @throws Exception when there is a non nominal error
	 */
	public void processEvent(T event) throws Exception;
	
}
