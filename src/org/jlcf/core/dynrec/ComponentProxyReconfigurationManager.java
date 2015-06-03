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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Component reconfiguration manager used in coordination with the
 * connector reconfiguration managers.
 * All methods are accessed after obtaining a reentrant lock so no synchronization is necessary
 * @author Petros Pissias
 *
 */
public class ComponentProxyReconfigurationManager {

	private final Logger logger = Logger.getLogger(getClass());
	
	//list of state observers. only used during reconfiguration
	private final List<IComponentStateReceiver> observers;
	
	//component state
	private ComponentState componentState;
	
	//number of pending calls (calls currently being served by the component).
	private volatile int pendingCalls;
	
	public ComponentProxyReconfigurationManager() {
		observers = new ArrayList<IComponentStateReceiver>();
		componentState = ComponentState.IDLE;
		pendingCalls = 0;
	}
	
	/**
	 * receives an event related to the component and transits the component state,
	 * informing any observers if the state changes
	 * @param event the event
	 * @param m the related method
	 * @param args the method arguments
	 */
	public void receiveEvent(ComponentStateEvent event, Method m, Object[] args) {
		//logger.debug("received event:"+event+" current state:"+componentState);
		ComponentState initialState = componentState;
		switch (event) {
			case CALLING : {
				pendingCalls++;

				switch (componentState) {
					
					case IDLE : {
						componentState = ComponentState.WORKING;
						break;
					}
					case QUIESCENT : {
						//logger.log(Level.FATAL,"received "+event+" message while in "+componentState+" state", new Throwable());
						break;
					}				
					case WAITING : {
						break;
					}
					case WORKING : {
						break;
					}
				}
				break;
			}
			case FINISHED_CALLING : {
				pendingCalls--;
				switch (componentState) {
				
					case IDLE : {
						//logger.log(Level.FATAL,"received "+event+" message while in "+componentState+" state", new Throwable());
						break;
					}
					case QUIESCENT : {
						//logger.log(Level.FATAL,"received "+event+" message while in "+componentState+" state", new Throwable());
						break;
					}				
					case WAITING : {
						if (pendingCalls == 0) {
							//reached quiscent state
							componentState = ComponentState.QUIESCENT;
						}
						break;
					}
					case WORKING : {
						if (pendingCalls == 0) {
							//reached quiscent state
							componentState = ComponentState.IDLE;
						}
						break;
					}
				}					
				break;
			}
			
			case RECONFIGURATION_START : {
				switch (componentState) {
				
					case IDLE : {
						componentState = ComponentState.QUIESCENT;
						break;
					}
					case QUIESCENT : {
						//logger.log(Level.FATAL,"received "+event+" message while in "+componentState+" state", new Throwable());
						break;
					}				
					case WAITING : {
						//logger.log(Level.FATAL,"received "+event+" message while in "+componentState+" state", new Throwable());
						break;
					}
					case WORKING : {
						componentState = ComponentState.WAITING;
						break;
					}
				}				
				break;
			}
			
			case RECONFIGURATION_END : {
				//clear the observers queue
				//logger.debug("clearing observers list. currently contains:"+observers.toString());

				observers.clear();
				switch (componentState) {
				
					case IDLE : {
						//logger.log(Level.FATAL,"received "+event+" message while in "+componentState+" state", new Throwable());
						break;
					}
					case QUIESCENT : {
						componentState = ComponentState.IDLE;
						break;
					}				
					case WAITING : {
						componentState = ComponentState.WORKING;
						break;
					}
					case WORKING : {
						//logger.log(Level.FATAL,"received "+event+" message while in "+componentState+" state", new Throwable());

						break;
					}
				}				
				break;
			}			
		}
		
		//inform observers (if any) of the new component state
		if (initialState != componentState) {
			for (IComponentStateReceiver manager : observers) {
				//logger.debug("informing observer about state change :"+componentState+" observer:"+manager);
				manager.receiveComponentExternalState(componentState);
			}
		}
		
	}


	/**
	 * Registers an observer with the component state
	 * @param stateReceiver the callback interface that the observer will be notified
	 */
	public void observeState(IComponentStateReceiver stateReceiver) {
		//logger.debug("registering observer :"+stateReceiver);
		observers.add(stateReceiver);
	}

	/**
	 * returns the component state. typically called by connectors
	 * @return the componetn state
	 */
	public ComponentState getState() {
		//logger.debug("returning external state of the component:"+componentState);
		return componentState;
	}
}
