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
 * @author Petros Pissias
 *
 */
public class LoadApplicationRequest 
		extends GenericRequestReplyReq<Pair<Boolean, String>, ContainerProcessorRequestType> {

	private final String applicationDescription;
	
	public LoadApplicationRequest(String filename) {
		super(ContainerProcessorRequestType.LOAD_APPLICATION);
		applicationDescription = filename;
	}

	public String getApplicationDescription() {
		return applicationDescription;
	}
	
}
