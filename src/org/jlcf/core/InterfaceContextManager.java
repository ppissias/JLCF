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

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

/**
 * This class receives potentially "context-aware"
 * calls from a user of the component interface and forwards it
 * to the connector of the component, stripping out the context
 * information.
 * 
 * The context information may contain a callback, in which case it is added
 * in ThreadLocal data in order to be used by the target component POJO if needed.
 * 
 * This class is the first receiver of a call to a component interface.
 * 
 * @author Petros Pissias
 *
 */
public class InterfaceContextManager implements IContextManagerInterface {

	private final Logger logger = Logger.getLogger(getClass());
	
	//the target object implementing the actual interface
	private final Object targetConnector;
	
	//the framework container
	private final JLCFContainer container;
	
	/**
	 * constructor. 
	 * @param target The connector implementing the target interface
	 */
	public InterfaceContextManager(Object target, JLCFContainer container) {
		this.targetConnector = target;
		this.container = container;
	}

	@Override
	public Object serviceCall(Object[] args, CallContextInformation context) throws Throwable{
		
		//logger.debug("received call for method:"+context.getMethod().getName()+" callback:"+context.getCallbackAddress());
		
		if (context.getCallbackAddress() != null) {
			//handle callback information
			//logger.debug("found callback information '"+context.getCallbackAddress()+"' adding to threadlocal data");
			container.setCallContext(context);
		}
		
		//forward the call to the target connector.	
		Object ret;
		try {
			ret = context.getMethod().invoke(targetConnector, args);
			return ret;
		}catch (InvocationTargetException ex) {
			if (ex.getCause() == null) {
				throw (ex);
			} else {
				throw (ex.getCause()); //throw the actual exception of the target interface
			}
		}

	}
	
}
