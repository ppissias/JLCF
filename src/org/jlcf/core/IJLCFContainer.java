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
package org.jlcf.core;

import org.jlcf.core.exception.ApplicationInstantiationException;
import org.jlcf.core.exception.ComponentReferenceException;
import org.jlcf.core.util.Pair;

/**
 * Interface with all operations that a JLCF
 * container supports. This is the interface of a user of the framework.
 * 
 * @author Petros Pissias
 *
 */
public interface IJLCFContainer {

	/**
	 * Creates a complete application based on the application input file.
	 * @param applicationFile applicationFile The input file describing the component composition
	 * @throws ApplicationInstantiationException
	 */
	public void loadApplication(String applicationFile) throws ApplicationInstantiationException;
	
	/**
	 * Returns the callback associated with this call. The caller should
	 * use the same thread that initiated the call and not call this method
	 * from a new thread.
	 * 
	 * @return the callback implementation
	 * @throws ComponentReferenceException
	 */
	public <T> T getCallback() throws ComponentReferenceException;
	

	/**
	 * Generic method that returns a component interface to the requestor.
	 * The requestors are: Component POJOs that want to get a callback address and Users of the framework.
	 * They are handled the same way.
	 * 
	 * @param targetPath the target path, for example componentA/interfaceA
	 * @return a component interface implementation that the user can call. This is actually a {@link ReceptacleContextManager} with the target address. On the first call it will resolve the target address to an actual component.
	 * @throws ComponentReferenceException
	 */
	public <T> T getComponentReference(String targetPath) throws ComponentReferenceException;
	
	/**
	 * called by the user when a reconfiguration is needed
	 * @param component the target component
	 * @param millis the timeframe fo the reconfiguration
	 * @return 
	 */
	
	/**
	 * Starts a reconfiguration process.
	 * 
	 * @param component the component name of the component to be replaced
	 * @param replacement the class of the new component that will replace the old comopnent 
	 * @param millis the time-frame that the reconfiguration should be performed. If the time frame elapses and the framework does not manage to replace the component the reconfiguration will fail.
	 * @return pair of boolean indicating of the reconfiguration was successful and a String message
	 * @throws Exception in case: The new component does not implement the formal interfaces of the old component, in case the old component name cannot be found and in case the new component class cannot be found. 
	 */
	public Pair<Boolean, String> singleComponentReconfguration(String component, String replacement, long millis) throws Exception;	
}
