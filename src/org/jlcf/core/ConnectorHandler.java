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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jlcf.core.dynrec.ConnectorTimingBasedReconfigurationManager;

/**
 * This interecepts and forwards the calls to the component proxy handler.
 * This is the 2nd level wrapper, on top of the component POJO and the component Proxy Handler.
 * There is one connector created per formal component interface.
 * 
 * The main purpose of a connector is to selectively block calls directed at the
 * component during dynamic reconfiguration.
 * 
 * @author Petros Pissias
 *
 */
public class ConnectorHandler implements InvocationHandler, IConnectorManager {
	
	//the component proxy instance.
	//called by the component framework and user threads so needs to be volatile.
	private volatile Object componentProxy;
	//the proxy handler of the component
	private volatile IComponentProxy componentProxyHandler;

	//the connector reconfiguration manager
	//TODO change actual implementation based on a configuration file.
	private final ConnectorTimingBasedReconfigurationManager connectorReconfigurationManager;
	
	private final Logger logger = Logger.getLogger(getClass());

	//name of this connector, primarily for logging
	private final String name;
	
	public ConnectorHandler(Object componentProxyInstance, IComponentProxy compProxyHandler, String name) {
		componentProxy = componentProxyInstance;
		this.name = name;
		this.componentProxyHandler = compProxyHandler;
		connectorReconfigurationManager = new ConnectorTimingBasedReconfigurationManager();
		connectorReconfigurationManager.setComponentProxy(componentProxyHandler);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method m, Object[] arguments)
			throws Throwable {
		
		//logger.debug(name+" invoking connector reconf manager start method");
		connectorReconfigurationManager.startCall(m, arguments);
		//logger.debug(name+" invoking target component proxy");
		Object ret; //the return
		try {
			ret = m.invoke(componentProxy, arguments);
		}catch (InvocationTargetException ex) {
			if (ex.getCause() == null) {
				throw (ex);
			} else {
				throw (ex.getCause()); //throw the actual exception of the target interface
			}		}			
		//logger.debug(name+" invoking connector reconf manager finish method");
		connectorReconfigurationManager.finishCall(m, arguments);
		return ret;
	}

	@Override
	//called by the framework at startup and after a reconfiguration to set the component proxy target.
	public void setTarget(Object target, IComponentProxy componentProxyHandler) {
		//logger.debug(name+" setting target");
		this.componentProxy = target;
		this.componentProxyHandler = componentProxyHandler;
		connectorReconfigurationManager.setComponentProxy(componentProxyHandler);
	}

	@Override
	//called by the framework reconfiguration manager to declare the start of a reconfiguration.
	public void setReconfiguring(boolean reconfiguring, long millis) {
		connectorReconfigurationManager.setReconfiguring(reconfiguring, millis);
	}

}
