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

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/**
 * All useful information about a call context.
 * Currently it contains the target method and the callback address if any.
 * The call context is set by the calling component (Receptacle Context Manager) and used
 * by the target component (Interface Context Manager)
 * 
 * @author Petros Pissias
 *
 */
public class CallContextInformation {

	private final Logger logger = Logger.getLogger(getClass());

	//the method of the interface that this call is targeted towards
	private final Method method;
	
	//potential callback information
	private final String callbackAddress;

	/**
	 * Constructor
	 * @param method the target method from the target interface
	 * @param callbackAddress potential callback address. null if no callback is present.
	 */
	public CallContextInformation(Method method, String callbackAddress) {
		this.method = method;
		this.callbackAddress = callbackAddress;
	}

	public Method getMethod() {
		return method;
	}

	public String getCallbackAddress() {
		return callbackAddress;
	}

	@Override
	public String toString() {
		return "ContextInformation [method=" + method + ", callbackAddress="
				+ callbackAddress + "]";
	}

	
	
}
