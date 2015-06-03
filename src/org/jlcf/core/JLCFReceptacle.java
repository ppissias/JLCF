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

import org.apache.log4j.Logger;

/**
 * The receptacle is the invocation handler of the proxy object that is created for every receptacle.
 * Using the java proxy mechanism, it implements the target component interface.
 * It intercepts calls and forwards them to a chain of (optional) interceptors.
 * The last interceptor of the chain forwards the call to the Receptacle Context Manager,
 * which enriches the call with some context information and forwards the call to
 * another component.
 * 
 * This object also implements the methods for adding  / removing interceptors at runtime.
 * 
 * @author Petros Pissias
 *
 */
public class JLCFReceptacle implements IReceptacle, InvocationHandler{

	//logger
	private final Logger logger = Logger.getLogger(getClass());
	
	//target interface. This is the context-aware receptacle context manager
	private final Object target;
	
	//list of interceptors 
	//TODO change to non final synchronized list when feature to add / remove interceptors dynamically is added.
	private final Interceptor[] interceptors;
	
	//name of this handler (for logging)
	private final String name;
	
	//flag that indicates if this receptacle has interceptors
	private final boolean hasInterceptors;
	
	/**
	 * Creates a new instance of this invocation handler.
	 * @param interceptors the list of initial interceptors
	 * @param name the name of this receptacle 
	 */
	public JLCFReceptacle(Interceptor[] interceptors, String name, Object target) {
		this.interceptors = interceptors;
		this.hasInterceptors = (interceptors != null && interceptors.length > 0);		
		this.name = name;
		this.target = target;
		setupInterceptors() ;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method m, Object[] arguments)
			throws Throwable {
		
		Object targObject;
		if (hasInterceptors) { 
			//invoke the first interceptor
			//logger.debug(name+" invoking interceptor");
			targObject = interceptors[0];		
		} else { //no interceptors
			//logger.debug(name+" invoking target");
			targObject = target;
		}
		Object ret;
		try {
			ret = m.invoke(targObject, arguments);
			return ret;
		}catch (InvocationTargetException ex) {
			if (ex.getCause() == null) {
				throw (ex);
			} else {
				throw (ex.getCause()); //throw the actual exception of the target interface
			}		
		}
	}


	/**
	 * Sets up the interceptor chain.
	 * Each interceptor is setup to forward the call to the next one.
	 * The last interceptor forwards the call to the Reecptacle Context Manager.
	 */
	private void setupInterceptors() {
		if (!hasInterceptors) {
			return;
		}
		int interceptorsSize = interceptors.length;
		for (int i=0 ; i<interceptorsSize ; i++) {
			if (i + 1 < interceptorsSize) {
				//there is a next one, so fix the previous interceptor to point to the next one
				interceptors[i].setTarget(interceptors[i+1]);
			}
		}
		//set last interceptor to point to the actual object
		interceptors[interceptorsSize-1].setTarget(target);
	}

	
	
	@Override
	public void addInterceptor(Interceptor interceptor) {
		// TODO Implement
		//setupInterceptors();
	}

	@Override
	public void removeInterceptor(Interceptor interceptor) {
		// TODO Implement
		//setupInterceptors();
	}

}
