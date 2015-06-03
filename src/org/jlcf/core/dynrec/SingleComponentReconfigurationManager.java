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
package org.jlcf.core.dynrec;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jlcf.core.IComponentProxy;
import org.jlcf.core.IConnectorManager;
import org.jlcf.core.util.Pair;

/**
 * This class is the 
 * handler of a reconfiguration (replacement) of a single component.
 * It is used by the runtime when such a user request arrives.
 * 
 * It locks the component proxy and then informs all connectors about the reconfiguration.
 * It then starts a timer that will expire after a timeout if the reconfiguration has not been done.
 * It then informs the component proxy and registers for receiving the state of the component.
 * It blocks on a queue waiting to receive either a confirmation or a negative response
 * about the reconfiguration.
 * 
 * @author Petros Pissias
 *
 */
public class SingleComponentReconfigurationManager extends Thread implements IComponentStateReceiver {

	private final Logger logger = Logger.getLogger(getClass());
	
	//the comp proxy
	private final IComponentProxy compProxy;
	
	//the connectors
	private final List<IConnectorManager> connectorList;
	
	//the queue that the framework will wait for a reply
	private final BlockingQueue<Pair<Boolean, String>> reconfigurationResponseQueue;
	
	private final long millis;
	
	//the queue that this thread waits for a reply from the component or the watchdog thread
	private final BlockingQueue<Pair<Boolean, String>> reconfigurationThreadQueue;
	
	//watchdog timer for the reconfiguration duration
	private final Timer t;
	
	public SingleComponentReconfigurationManager(IComponentProxy compProxy,
			List<IConnectorManager> connectorList,
			BlockingQueue<Pair<Boolean, String>> reconfigurationResponseQueue, long millis) {

		this.compProxy = compProxy;
		this.reconfigurationResponseQueue =reconfigurationResponseQueue;
		this.millis = millis;
		this.connectorList = connectorList;
		this.t = new Timer();
		this.reconfigurationThreadQueue = new LinkedBlockingQueue<Pair<Boolean, String>>();
		setName("single component reconfiguration thread");
		//logger.debug("created SingleComponentReconfigurationManager instance");
	}

	@Override
	public void receiveComponentExternalState(ComponentState s) {
		//here we receive notifications from the component proxy state manager 
		//logger.debug("received state update form component proxy state manager:"+s);
		if (s == ComponentState.QUIESCENT) {
			//component has reached a quiescent state
			//logger.info("component reached quiescent state");
			
			//stop timer
			t.cancel();
			
			//return the success of reaching a quiescent state to the JLCF processor thread
			reconfigurationThreadQueue.add(new Pair<Boolean, String>(true, "component reached quiescent state"));
		}
	}

	@Override
	public void start() {
		//main processing
		//logger.debug(getName()+" started");
		
		//get lock on component Proxy
		//logger.debug("obtaining lock");
		compProxy.lock();
		
		//inform all connectors
		for (IConnectorManager connector : connectorList) {
			connector.setReconfiguring(true, millis);
		}

		//start watchdog timer, will insert negative reply after the specified milliseconds
		t.schedule(new TimerTask() {

			@Override
			public void run() {
				//reconfiguration time elapsed.
				//logger.info("reconfiguration time elapsed qithout achieving a quiescent state");
				reconfigurationThreadQueue.add(new Pair<Boolean, String>(false, "reconfiguration time elapsed"));
			}
			
		}, millis);
		
		//inform component proxy
		compProxy.reachQuiescentState(this);
		
		//release lock
		//logger.debug("releasing lock");
		compProxy.unlock();
		
		//wait for a reply from the component
		try {
			Pair<Boolean, String> reply = reconfigurationThreadQueue.take();
			//logger.info("got reply for the outcome of the reconfiguration:"+reply.getLeft()+" "+reply.getRight());
			//send reply to the JLCF framework processor thread
			reconfigurationResponseQueue.add(reply);
		} catch (InterruptedException e) { //interrupted. should not happen
			//logger.log(Level.FATAL, "interrupted while waiting for a reply", new Throwable());
			//send reply to the JLCF framework processor thread
			reconfigurationResponseQueue.add(new Pair<Boolean, String>(false,"processing error. interrupted while waiting for a reply for the reconfiguration"));
		}
		

		
	}
	
}
