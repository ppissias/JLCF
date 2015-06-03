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

import org.jlcf.core.IContextManagerInterface;
import org.jlcf.core.util.GenericRequestReplyReq;
import org.jlcf.core.util.Pair;


/**
 * Request to get a component interface reference from the processor.
 * @author Petros Pissias
 *
 */
public class ComponentTargetRequest extends GenericRequestReplyReq< Pair<Boolean,IContextManagerInterface>, ContainerProcessorRequestType> {

	//target component
	private final String path;
	
	/**
	 * Creates a new isntance of a request to get a 
	 * componetnt target instrface
	 * @param path of the target interface
	 */
	public ComponentTargetRequest(String path) {
		super(ContainerProcessorRequestType.GET_TARGET_REFERENCE);
		this.path = path;		
	}

	public String getPath() {
		return path;
	}

	
}
