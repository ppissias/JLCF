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
package org.jlcf.core.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is an abstract class that is to be extended by
 * requests that need a reply but do not extend the GenericProcessorRequest.
 * 
 * As the AbstractQueueProcessor processes requests asynchronously, the 
 * request itself has a BlockingQueue that the caller must listen for a reply.
 *  
 * Typically the user of a request that extends this class,
 * will dispatch the request to a queue processor and wait on the 
 * queue for a reply. 
 *   
 * 
 * @param <V> the reply type
 * @author Petros Pissias
 *
 */

public abstract class SpecificRequestReplyReq <V> {

	//the reply queue
	private final BlockingQueue<V> replyQueue;
	
	//instantiate the reply queue
	public SpecificRequestReplyReq () {	
		replyQueue = new LinkedBlockingQueue<V>();
	}
	
	public void insertResponse(V response) {
		replyQueue.add(response);
	}
	//getter for the queue
	public V getResponse() throws InterruptedException {
		return replyQueue.take();
	}
}
