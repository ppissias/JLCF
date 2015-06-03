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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jlcf.core.annotation.InitMethod;
import org.jlcf.core.dynrec.ComponentProxyReconfigurationManager;
import org.jlcf.core.dynrec.ComponentState;
import org.jlcf.core.dynrec.ComponentStateEvent;
import org.jlcf.core.dynrec.IComponentStateReceiver;
import org.jlcf.core.dynrec.IReconfigurableComponent;

/**
 * This is the first level wrapper on top of the POJO component.
 * This intercepts the calls to the component POJO and calls the actual object.
 * It is also used in order to call the Init methods of components
 * and to manage dynamic reconfiguration.
 * 
 * It implements all formal interfaces of the component and is instantiated
 * as a proxy object during the component instantiation by the framework.
 * 
 * @author Petros Pissias
 *
 */
public class ComponentProxyHandler implements InvocationHandler, IComponentProxy {
	
	//the component instance
	private final Object componentPojo;
	//init method
	private final Method initMethod;
	//name of the component, primarily for logging.
	private final String name;
	
	private final Logger logger = Logger.getLogger(getClass());
		
	//state manager of the component external state
	private final ComponentProxyReconfigurationManager componentExternalStateManager;
	
	//lock that is used for synchronization between connectors, this object and the reconfiguration manager of the framework
	private final ReentrantLock lock;
	/**
	 * creates a new instance of a component proxy handler.
	 * @param componentPojoInstance
	 * @param name
	 */
	public ComponentProxyHandler(Object componentPojoInstance, String name) {
		componentPojo = componentPojoInstance;
		Method[] methods = componentPojo.getClass().getMethods();
		Method fInitMethod = null;
		
		//search for the init method
		boolean found = false;
		for (Method method : methods) {
			InitMethod mthd = method.getAnnotation(InitMethod.class);
			if (mthd != null) {
				found = true;
				fInitMethod = method;
				break;
			} 
		}
		if (found) {
			initMethod = fInitMethod;
		} else {
			initMethod = null; //this component does not declare an init method
		}
		
		this.name = name;
		
		//create the state manager
		//TODO do via property file
		componentExternalStateManager = new ComponentProxyReconfigurationManager();
		
		//initialize the lock
		lock = new ReentrantLock();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method m, Object[] arguments)
			throws Throwable {

		//logger.debug(name+" sending CALLING msg to state manager");
		componentExternalStateManager.receiveEvent(ComponentStateEvent.CALLING, m, arguments);
		
		//has been previously locked by the same thread on the connector
		//logger.debug(name+" thread:"+Thread.currentThread().getName()+" releasing lock");
		lock.unlock();
		
		//logger.debug(name+" invoking pojo");
		Object ret; //the return
		try {
			ret = m.invoke(componentPojo, arguments);
		}catch (InvocationTargetException ex) {
			if (ex.getCause() == null) {
				throw (ex);
			} else {
				throw (ex.getCause()); //throw the actual exception of the target interface
			}		
		}
		
		//logger.debug(name+" sending FINISHED_CALLING msg to state manager after obtaining lock");
		lock.lock();
		componentExternalStateManager.receiveEvent(ComponentStateEvent.FINISHED_CALLING, m, arguments);
		lock.unlock();
		
		return ret;
	}

	@Override
	public void callInitMethod() {
		//logger.debug(name+" Calling init method on component pojo if exists");
		if (initMethod != null) {
			try {
				initMethod.invoke(componentPojo, new Object[]{});
			} catch (Exception e) {
				//logger.log(Level.ERROR, "Cannot call init method on component of class:"+componentPojo.getClass().getName()+" "+e.getMessage(), e);
			}
		}else {
			//logger.debug(name+" No init method for component of class:"+componentPojo.getClass().getName());
		}
		
	}

	@Override
	public void reachQuiescentState(IComponentStateReceiver observer) {
		//logger.debug(name+" reaching a quiescent stste. Will inform user on component state changes");
		componentExternalStateManager.observeState(observer);
		
		//inform the component to stop its internal threads that may initiate calls through the component receptcles
		//this is needed if the component is an "alive" component. That is if it has threads that are able to
		//make calls through its receptacles.
		try {
			//logger.debug(name+" trying to call stopAliveThreads on pojo");
			((IReconfigurableComponent)componentPojo).stopAliveThreads();
		}catch (ClassCastException ex) {
			//logger.debug("could not cast component to IReconfigurableComponent. Assuming component does not implement stopAliveThreads method");
		}
		//logger.debug(name+" sending RECONFIGURATION_START event to manager");
		componentExternalStateManager.receiveEvent(ComponentStateEvent.RECONFIGURATION_START, null, null);

	}

	@Override
	public void proceed() {
		try {
			//logger.debug(name+" trying to call proceed on pojo");
			((IReconfigurableComponent)componentPojo).proceed();
		}catch (ClassCastException ex) {
			//logger.debug("could not cast component to IReconfigurableComponent. Assuming component dose not implement proceed method");

		}	
		//logger.debug(name+" sending RECONFIGURATION_END event to manager");

		componentExternalStateManager.receiveEvent(ComponentStateEvent.RECONFIGURATION_END, null, null);

	}

	@Override
	public Object getInternalState() {
		try {
			//logger.debug(name+" trying to call extractState on pojo");
			return ((IReconfigurableComponent)componentPojo).extractState();
		}catch (ClassCastException ex) {
			//logger.debug("could not cast component to IReconfigurableComponent");
		}	
		return null;
	}

	@Override
	public void setInternalState(Object internalState) {
		try {
			//logger.debug(name+" trying to call insertState on pojo");
			((IReconfigurableComponent)componentPojo).insertState(internalState);
		}catch (ClassCastException ex) {
			//logger.debug("could not cast component to IReconfigurableComponent");
		}	
	}
	
	@Override
	//called by coonnectors after obtaining the lock in order to determine the component state
	public ComponentState getExternalState() {
		return this.componentExternalStateManager.getState();
	}
	
	@Override
	public void lock() {
		//logger.debug("Thread:"+Thread.currentThread().getName()+" obtaining lock, my object is of class:"+componentPojo.getClass().getName());
		lock.lock();
		//logger.debug("Thread:"+Thread.currentThread().getName()+" obtainined lock, my object is of class:"+componentPojo.getClass().getName());

	}
	
	@Override
	public void unlock() {
		//logger.debug("Thread:"+Thread.currentThread().getName()+" releasing lock, my object is of class:"+componentPojo.getClass().getName());
		lock.unlock();
		//logger.debug("Thread:"+Thread.currentThread().getName()+" released lock, my object is of class:"+componentPojo.getClass().getName());
	}

}
