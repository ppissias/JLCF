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
 * Interface implemented by Receptacle Context Managers.
 * Provides the basic functionality for setting an interface context manager
 * and the path of the target.
 * 
 * @author Petros Pissias
 *
 */
public interface IContextManagerReceptacle {



	/**
	 * Sets the target path and interface context manager. 
	 * This is used by the runtime when it connects two components.
	 * @param targeComponent the target component IContextManagerInterface
	 * @param path the path of the target component
	 */
	public void setTarget(IContextManagerInterface targeComponent, String path);
	
	/**
	 * sets the callback path. This path must point to a component interface, for example "ComponentA/InterfaceA"
	 * Called by the runtime when we connect two components.
	 * @param cbPath the path of the callback
	 */
	public void setCallback(String cbPath);
}
