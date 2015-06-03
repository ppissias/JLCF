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

import org.jlcf.core.dynrec.ComponentState;
import org.jlcf.core.dynrec.IComponentStateReceiver;

/**
 * This interface Implemented by dynamic proxies that are used for components (Component proxy handler)
 * and is used to perform management actions on component POJOs,
 * such as calling a method the developer has marked as an initializer method
 * and also to manage the state of the component during dynamic reconfiguration.
 * 
 * @author Petros Pissias
 *
 */
public interface IComponentProxy {

	/**
	 * The component proxy calls the init method of the component
	 * (if any init method has been specified)
	 */
	public void callInitMethod();
	
	/**
	 * called by the framework when the component must reach a quiscent state.
	 * The component must then report state transitions to the state receiver.
	 * @param observer
	 */
	public void reachQuiescentState(IComponentStateReceiver observer);
	
	/**
	 * called by the framework when it wants to instruct the component to
	 * proceed its normal operation. This is typically called when a reconfiguration
	 * is aborted or not reached within a required time interval.
	 */
	public void proceed();
	
	/**
	 * called by the framework when the component has reached a quiescent state
	 * @return the internal state of the component
	 */
	public Object getInternalState() ;

	/**
	 * called by the framework when the component has reached a quiescent state
	 * @return the internal state of the component
	 */
	public void setInternalState(Object internalState) ;
	
	/**
	 * called by connectors in order to get the external state of the component
	 * @return The component external state
	 */
	public ComponentState getExternalState();
	
	//obtain the lock, used for synchronization between the reconfiguration manager , connectors and the component proxy
	public void lock();
	
	//release the lock, used for synchronization between the reconfiguration manager , connectors and the component proxy
	public void unlock();
	
}
