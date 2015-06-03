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

/**
 * This interface is implemented by component connectors.
 * It provides the management interface of connectors that is used
 * during dynamic reconfiguration.
 * 
 * @author Petros Pissias
 *
 */
public interface IConnectorManager {

	/**
	 * sets the target of the connector.
	 * This is typically a component proxy that implements all the interfaces
	 * of the component POJO.
	 * @param target the component proxy implementing the target interface
	 * @param componentProxyHandler the component proxy handler
	 */
	public void setTarget(Object target, IComponentProxy componentProxyHandler);

	/**
	 * method called by the framework during dynamic reconfiguration.
	 * The connector changes its behavior depending on its strategy.
	 * @param reconfiguring if the connector is in reconfiguration mode or not
	 * @param millis the time frame in milliseconds in which we want to achieve a quiescent state
	 */
	public void setReconfiguring(boolean reconfiguring, long millis);
}
