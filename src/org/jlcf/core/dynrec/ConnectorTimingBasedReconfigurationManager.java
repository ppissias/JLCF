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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jlcf.core.IComponentProxy;

/**
 * connector that implements a timing based algorithm in 
 * order to selectively block calls.
 * @author Petros Pissias
 *
 */
public class ConnectorTimingBasedReconfigurationManager {

	private final Logger logger = Logger.getLogger(getClass());
	
	//thread local data that hold accounting information about call times
	private final ThreadLocal<Long> callInitTime;
	
	//the average time for each method
	private final Map <Method, Long> methodTimes;
	
	//reconfiguration flag and timeframe and system time when the reconfiguration started
	private volatile boolean reconfiguring;
	private volatile long millis, reconfigurationStartTime;
	
	//the component proxy handler. provides the components state
	private volatile IComponentProxy componentProxyHandler;
	
	//lock used to selectively block calls.
	//this is locked as soon as the reconfiguration begins and released when it ends.
	private final ReentrantLock callBlocker;
	
	public ConnectorTimingBasedReconfigurationManager() {
		callInitTime = new ThreadLocal<Long>() {
			@Override
			protected Long initialValue() {return Long.valueOf(0);};
		};
		//create map that maps average method times
		methodTimes = Collections.synchronizedMap(new HashMap<Method, Long>());
		callBlocker = new ReentrantLock();
	}
	
	/**
	 * called before a call starts by the connector
	 * @param m the method
	 * @param args the method arguments
	 */
	public void startCall(Method m, Object[] args) {
		//logger.debug("connector reconfiguration manager procedding call to method:"+m.getName()+" previous statistics time:"+(methodTimes.get(m)==null?"no data":methodTimes.get(m)));
		//get the lock. This will be released on the component proxy.
		//the only other lock holder is the framework reconfiguration manager
		//this is needed in order to register the call at the component proxy
		//before the framework starts a reconfiguration, otherwise we might call a quiescent component
		//logger.debug(" thread:"+Thread.currentThread().getName()+" obtaining lock");
		componentProxyHandler.lock();
		
		if (reconfiguring) {
			//logger.debug("component reconfiguring");
			//reconf algorithm. electively block calls based on the time it will take to complete the call
			//get component state, we already have the component proxy lock
			ComponentState externalState = componentProxyHandler.getExternalState();
			switch (externalState) {
				case IDLE : {
					//should never happen as component is reconfiguring
					//logger.log(Level.FATAL, "component state  :"+externalState+" while reconfiguring");
					break;
				}
				case QUIESCENT : {
					//logger.debug("component in quiescent state, blocking call");
					//block the call
					//first release the lock to the component proxy
					//logger.debug(" thread:"+Thread.currentThread().getName()+" releasing lock");
					componentProxyHandler.unlock();
					callBlocker.lock();
					//By now the reconfiguration has ended. release the call and proceed normally.
					callBlocker.unlock();
					//go back to the beginning in order to get the lock and the component state
					startCall( m, args);
					return;
				}
				case WAITING : {
					//judge if we will release the lock
					Long time = methodTimes.get(m);
					if (time != null) {
						//we have data
						//logger.debug("method statistic time:"+time+" time left:"+(millis - (System.currentTimeMillis() - reconfigurationStartTime)));
						if (time > (millis - (System.currentTimeMillis() - reconfigurationStartTime)) ) { //call does not have time to finish
							//logger.debug("component in "+externalState.toString()+" state, blocking because there is no time to complete the call");

							//greater the time we have left
							//block the call
							//first release the lock to the component proxy
							//logger.debug(" thread:"+Thread.currentThread().getName()+" releasing lock");
							componentProxyHandler.unlock();
							callBlocker.lock();
							//By now the reconfiguration has ended. release the call and proceed normally.
							callBlocker.unlock();
							//go back to the beginning in order to get the lock and the component state
							startCall( m, args);
							return;
						}else {
							//logger.debug("component in "+externalState.toString()+" state, alowing call because there is time to complete the call");
						}
					}
					break;
				}
				case WORKING : {
					//should never happen as component is reconfiguring
					//logger.log(Level.FATAL, "component state  :"+externalState+" while reconfiguring");					
					break;
				}				
			}
		}
		callInitTime.set(System.currentTimeMillis());
	}

	/**
	 * called after a call has finished by the connector
	 * @param m the method
	 * @param args the method arguments
	 */
	public void finishCall(Method m, Object[] args) {
		long currTime = System.currentTimeMillis();
		long startTime = callInitTime.get();
		long callTime = currTime - startTime;
		methodTimes.put(m, callTime);
	}

	/**
	 * called by the framework in order to set the reconfiguration flag of this connector.
	 * The caller of this method must always use the same thread to set to true and false the reconfiruation flag.
	 * @param reconfiguring 
	 * @param millis 
	 */
	public void setReconfiguring(boolean reconfiguring, long millis) {
		//logger.info("connector received reconfiguration message. reconfiguring:"+reconfiguring);
		this.reconfiguring = reconfiguring;
		this.millis = millis;
			
		if (reconfiguring) { //reconfiguration starts. lock the lock so that we can block methods
			//set the time when reconfiguration started
			this.reconfigurationStartTime = System.currentTimeMillis();

			//logger.debug("obtaining callblocker lock");
			callBlocker.lock();
			//logger.debug("obtained callblocker lock");

		} else { //unlock the lock so that method calls can proceed
			//logger.debug("releasing callblocker lock");
			callBlocker.unlock();
			//logger.debug("released callblocker lock");

		}
	}

	/**
	 * called after initilization and after reconfiguration in order to set the reference 
	 * to the component proxy
	 * @param componentProxy the component proxy handler
	 */
	public void setComponentProxy(IComponentProxy componentProxy) {
		this.componentProxyHandler = componentProxy;
	}
}
