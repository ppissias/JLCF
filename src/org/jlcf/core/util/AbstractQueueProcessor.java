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

import org.apache.log4j.Logger;

/**
 * This abstract generic class 
 * provides a single thread that processes generic 
 * requests and a blocking queue for receiving messages.
 * 
 * It shall be inherited by implementation classes
 * that follow this pattern.
 * The caller of the implementation classes must call initialize
 * following the creation of an instance.
 * @param <T> the type of the items that the processor can process
 * @author Petros Pissias
 *
 */

public abstract class AbstractQueueProcessor<T> 
						implements IEventProcessor<T>{

	//the input queue
	private final BlockingQueue<T> inputQueue;
	
	//the processor thread
	private final AbstractQueueProcessorThread<T> processorThread;
	
	private final Logger logger;
	
	private final String processorName;
	
	
	/**
	 * parent constructor.
	 * Initializes processor thread and the input queue.
	 * 
	 * the this reference passed to the processor thread is safe
	 * if the initialize method will be called by the same
	 * thread that instantiated this object.
	 * 
	 * TODO always make sure that the same thread creates the 
	 * instance and calls initialize()
	 * @param processorName the name of this processor
	 */
	public AbstractQueueProcessor(String processorName) {
		
		logger = Logger.getLogger(getClass());
		inputQueue = new LinkedBlockingQueue<T>();
		this.processorName = processorName;
		processorThread = new AbstractQueueProcessorThread<T>(this, inputQueue,
				processorName);
		
	}
	
	/**
	 * Method called in order to start the processing
	 */
	public void initialize() {
		//start the processor thread
		//logger.debug(processorName+" starting processor thread");
		processorThread.start();
		
		//logger.debug(processorName+" initialized");
	}
	
	/**
	 * Method called in order to stop the processing.
	 */
	public void stop() {
		//start the processor thread
		//logger.debug(processorName+" stopping processor thread");
		processorThread.interrupt();
		
		//logger.debug(processorName+" stopped");
	}
	
	/**
	 * returns the processor queue
	 * @return
	 */
	public BlockingQueue<T> getInputQueue() {
		return inputQueue;
	}

	/**
	 * adds a request of type <T> to the processor.
	 */
	public void  addRequest(T request) {
		inputQueue.add(request);
	}

	public Logger getLogger() {
		return logger;
	}

	
}
