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
 * This is a context-aware proxy handler that enriches
 * calls to component interfaces with context related information.
 * 
 * It intercepts calls forwarded to a user interface and redirects
 * them through the generic interface on the component interface side
 * that is also context-aware.
 * 
 * This is used as the last object intercepting the call from the receptacle side,
 * called by the last interceptor and in the cases below.
 * 
 * When a user asks the runtime to get a reference to a component interface
 * using the getComponentReference method an instance of this class is returned.
 * 
 * When a component POJO wants to get a reference to a callback also an instance
 * of this object it returned. 
 * 
 * @author Petros Pissias
 *
 */
public class ReceptacleContextManager implements
		IContextManagerReceptacle, InvocationHandler {

	private final Logger logger = Logger.getLogger(getClass());
	
	//the target context-aware interface
	private volatile IContextManagerInterface target = null;
	
	//the target path
	private volatile String targetPath;
	
	//the callback path of the caller component
	private volatile String callbackPath; 
	
	//ref to the container
	private final JLCFContainer container;

	/**
	 * Constructs a new invocation handler. The handler will resolve the target 
	 * component at runtime prior the call.
	 * @param targetPath the path of the target component
	 * @param callbackPath the path of the caller component. "" if the component does not specify a callback.
	 */
	public ReceptacleContextManager(String targetPath, String callbackPath, JLCFContainer container) {
		//logger.debug("creating isntance of "+getClass().getName()+" with targetpath:"+targetPath+" cb:"+callbackPath);
		this.targetPath = targetPath;
		this.callbackPath = callbackPath;
		this.container = container;
	}
	


	@Override
	public Object invoke(Object proxy, Method method, Object[] args) 
			throws Throwable {
		
		//logger.debug("invoking target context manager");

		if (target == null) {
			//logger.debug("fetching target"+targetPath+" from runtime");

			//get target reference 
			target = container.getTargetReference(targetPath);
		}
		
		Object ret = null;
		
		//redirect the call to the context-aware "server"
		try {
			ret = target.serviceCall(args, new CallContextInformation(method, callbackPath));
			return ret;
		}catch (InvocationTargetException ex) {
			if (ex.getCause() == null) {
				throw (ex);
			} else {
				throw (ex.getCause()); //throw the actual exception of the target interface
			}		
		}
		
	}

	/* (non-Javadoc)
	 * @see org.jlcf.core.IReceptacleContextInterceptor#setTarget(java.lang.String)
	 */
	@Override
	public void setTarget(IContextManagerInterface targetComponent, String targetPath) {
		//logger.debug("setting target to:"+targetPath);
		this.targetPath = targetPath;
		
		this.target = targetComponent;
	}
	
	@Override
	public void setCallback(String cbPath) {
		this.callbackPath = cbPath;
	}

}
