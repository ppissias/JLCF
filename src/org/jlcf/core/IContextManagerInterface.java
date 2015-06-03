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
 * This interface is a top level context-aware interface in front of connectors
 * implemented by interface context managers.
 * It supports a generic method for receiving and forwarding
 * calls to component interfaces through component connectors.
 * 
 * @author Petros Pissias
 *
 */
public interface IContextManagerInterface {

	/**
	 * generic method that is called between component calls
	 * @param args the arguments for the actual component call (payload)
	 * @param context the context of the call
	 * @return the return of the actual interface
	 * @throws Exception in case the call cannot be forwarded and in case the target component throws an exception
	 */
	public Object serviceCall(Object[] args, CallContextInformation context) throws Throwable;
}
