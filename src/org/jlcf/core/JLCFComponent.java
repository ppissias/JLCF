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

import java.util.Map;

import org.apache.log4j.Logger;
import org.jlcf.core.util.Pair;

/**
 * This class represents a component instantiation and holds all information that is related to a component
 * that the framework need to access at runtime. It contains references to
 * - The Component Proxy
 * - For each receptacle, the IReceptacleManager interface and the IContextManagerReceptacle of the receptacle context manager.
 * - For each interface, the IConnectorManager interface and the IContextManagerInterface of the interface context manager.
 * 
 * This is only accessed by the container processor thread
 * so no synchronization is necessary.
 * 
 * @author Petros Pissias
 *
 */
public class JLCFComponent {

	private final Logger logger = Logger.getLogger(getClass());
	
	//the component proxy handler
	private IComponentProxy componentProxy;
	
	//list of receptacles with references to the receptacle context interceptors.
	private Map<String , Pair<IReceptacle, IContextManagerReceptacle>> receptacles;
	
	//list of interfaces with references to the connector manager and the context aware proxy 
	private Map<String, Pair<IConnectorManager, IContextManagerInterface>> connectors;
	
	/**
	 * constructs a new component holder object  
	 * @param component the component proxy
	 * @param rec the list of receptacles and their associated objects
	 * @param intf the list of interfaces and their associated objects
	 */
	public JLCFComponent(IComponentProxy component, Map<String , Pair<IReceptacle, IContextManagerReceptacle>> rec,
			Map<String, Pair<IConnectorManager, IContextManagerInterface>> intf ) {
		componentProxy = component;
		receptacles = rec;
		connectors = intf;
		//logger.debug("creating instance of component holder:"+toString());
		
	}

	//getters and setters
	
	public IComponentProxy getComponentProxy() {
		return componentProxy;
	}

	public Map<String, Pair<IReceptacle, IContextManagerReceptacle>> getReceptacles() {
		return receptacles;
	}

	public Map<String, Pair<IConnectorManager, IContextManagerInterface>> getConnectors() {
		return connectors;
	}

	public void setReceptacles(
			Map<String, Pair<IReceptacle, IContextManagerReceptacle>> receptacles) {
		this.receptacles = receptacles;
	}

	public void setComponentProxy(IComponentProxy componentProxy) {
		this.componentProxy = componentProxy;
	}

	public void setConnectors(
			Map<String, Pair<IConnectorManager, IContextManagerInterface>> connectors) {
		this.connectors = connectors;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("receptacles:");
		for (String key : receptacles.keySet()) {
			sb.append(key+",");
		}
		if (connectors != null) {
			sb.append(" interfaces:");
			for (String key : connectors.keySet()) {
				sb.append(key+",");
			}
		}
		return sb.toString();
	}
}
