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

package org.jlcf.core.dynrec;

/**
 * Interface that may be implemented by Components that support dynamic reconfiguration.
 * 
 * @author Petros Pissias
 *
 */
public interface IReconfigurableComponent {

	/**
	 * Method called by the framework in order to inform a component to stop its internal processing
	 * that may disrupt its quiscent state.
	 * implementer should stop all threads that are able to make calls through receptacles.
	 */
	public void stopAliveThreads();
	
	/**
	 * Method called by the runtime in order to inform the component to proceed
	 * its normal processing. This done when a reconfiguration cannot be reached within the desired timeframe
	 */
	public void proceed();
	
	/**
	 * Method called by the framework when a quiescent state has been reached.
	 * The components state is extracted in order to be passed to the new component.
	 * @return the state of the component that should be passed on
	 */
	public Object extractState();
	

	/**
	 * Method called by the framework when a component is being replaced.
	 * The old components state is inserted into the new component.
	 * @param state the internal state 
	 */
	public void insertState(Object state);	
}
