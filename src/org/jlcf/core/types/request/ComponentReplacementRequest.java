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
package org.jlcf.core.types.request;

import org.jlcf.core.util.GenericRequestReplyReq;
import org.jlcf.core.util.Pair;


/**
 * Request to get a component interface reference from the processor.
 * @author Petros Pissias
 *
 */
public class ComponentReplacementRequest extends GenericRequestReplyReq< Pair<Boolean, String>, ContainerProcessorRequestType> {

	//target component
	private final String targetComponent;

	//reconfiguration time frame
	private final long millis;
	
	//class of the new component
	private final String newComponent;
	/**
	 * Creates a new instance of a request to get a 
	 * replace a component  
	 * @param targetComponent the target component to be replaced. 
	 * @param millis reconfiguration time window
	 */
	public ComponentReplacementRequest(String targetComponent, String newComponent, long millis) {
		super(ContainerProcessorRequestType.QUISCE_SINGLE);
		this.targetComponent = targetComponent;
		this.newComponent = newComponent;
		this.millis = millis;
	}

	public long getMillis() {
		return millis;
	}


	public String getTargetComponent() {
		return targetComponent;
	}

	public String getNewComponent() {
		return newComponent;
	}


	
	
}
