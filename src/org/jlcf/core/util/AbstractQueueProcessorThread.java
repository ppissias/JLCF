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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The main thread of the abstract queue processor
 * it picks up an item from the queue
 * and calls the process method of the event
 * processor.
 * 
 * @author Petros Pissias
 *
 * @param <T>
 */
public class AbstractQueueProcessorThread<T> extends Thread {

	//the event processor
	private final IEventProcessor<T> eventProcessor;
	//the logger
	private final Logger logger;
	//the input queue
	private final BlockingQueue<T> inputQueue;
	
	private final String processorName;
	
	/**
	 * @param eventProcessor the event processor used for processing requests
	 * @param inputQueue the input queue where the requests originate
	 * @param processorName the name of this processor thread
	 */
	public AbstractQueueProcessorThread(IEventProcessor<T> eventProcessor,
			BlockingQueue<T> inputQueue,
			String processorName) {
		
		
		logger =Logger.getLogger(getClass());
		this.eventProcessor = eventProcessor;			
		this.inputQueue = inputQueue;		
		this.processorName = processorName;
		
		setName("AbstractQueueProcessorThread :"+processorName);
		
		//logger.debug(getClass().getName()+" instance created");
	}

	/**
	 * main thread loop.
	 * This runs forever until it will be interrupted.
	 * It fetches an event from the queue and forwards
	 * it to the event processor. 
	 * The event processor it the implementing object
	 * of the AbstractQueueProcessor 
	 * 
	 */
	public void run() {
		
		//logger.info(getClass().getName()+" thread started.");
		while (!interrupted()) {
			T inputItem = null;
			try {
				//fetch an item from the queue
				inputItem = inputQueue.take();
				eventProcessor.processEvent(inputItem);

			} catch (InterruptedException e) {
				//logger.info(getClass().getName()+" thread stopping.");
				//logger.log(Level.DEBUG,processorName+ ":InterruptedException while waiting for input. Exiting...", e);
				//EXIT
				return;
			} catch (Throwable e) {
				//logger.log(Level.ERROR,processorName+": caught an exception while processing input event of type:"+inputItem.getClass().toString(), e);
			}
		}
	}

}
