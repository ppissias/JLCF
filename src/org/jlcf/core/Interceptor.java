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
 * This is the base class that all interceptors must extend.
 * The interceptor developer shall extend this object and call the getTarget
 * method in order to invoke the target method.
 * 
 * @author Petros Pissias
 *
 */
public abstract class Interceptor {

	//the target. needs to be volatile because it is potentially
	//accessed by other threads in the lifetime of the framework.
	//interceptors can be added/removed bynamically.
	private volatile Object target;


	/**
	 * Method that returns the target interface to be used by interceptor
	 * implementations. The interceptor developer will typically perform any intercepting
	 * actions on the data of the call and then call getTarget in order to 
	 * obtain a reference to the target interface.
	 * 
	 * @return
	 */
	public <T> T getTarget() {
		return (T)target;
	}

	/**
	 * Method called by the framework in order to set the target of the interceptor
	 * during the component instantiation.
	 * 
	 * @param target the target of the interceptor.
	 */
	protected void setTarget(Object target) {
		this.target = target;
	}
}
