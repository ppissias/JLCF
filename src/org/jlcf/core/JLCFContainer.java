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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jlcf.core.exception.ApplicationInstantiationException;
import org.jlcf.core.exception.ComponentReferenceException;
import org.jlcf.core.types.request.ComponentReplacementRequest;
import org.jlcf.core.types.request.ComponentTargetRequest;
import org.jlcf.core.types.request.LoadApplicationRequest;
import org.jlcf.core.util.Pair;


/**
 * This class is the framework container.
 * It is responsible of:
 * - reading the application description and instantiating a component composition
 * - Holding all components
 * - Connecting components
 * - Inserting / removing interceptors
 * - creating and connecting components
 * - managing dynamic reconfiguration
 * 
 * This is the main class that a user of the framework interacts with.
 * 
 * It typically receives requests and forwards them to the container processor
 * which performs the requested actions.
 * 
 * @author Petros Pissias
 *
 */
public class JLCFContainer implements IJLCFContainer {

	//logger
	private static final Logger logger = Logger.getLogger(JLCFContainer.class);

	//holds local data. it stores information related to the call context (currently callbacks)
	private final ThreadLocal<CallContextInformation> contextInformation;
	
	//the container processor. derived from the abstract queue processor
	private final JLCFContainerProcessor processor;

	/**
	 * Returns an instance of the framework
	 * @return container single instance
	 */
	public static IJLCFContainer getInstance() {
		JLCFContainer container = new JLCFContainer();
		container.processor.initialize();
		//logger.info("JLCF container instance initialized");
		return container;
	}

	/**
	 * default constructor
	 */
	private JLCFContainer() {
		//initialize thread local data 
		contextInformation = new ThreadLocal<CallContextInformation>() {
            @Override protected CallContextInformation initialValue() {
                return null;
            }
		};
		
		//create a new processror
		processor = new JLCFContainerProcessor(this);
		//logger.debug("JLCF container instance created");
	}
	

	/**
	 * {@link IJLCFContainer}
	 */
	@Override 
	public void loadApplication(String applicationFile) throws ApplicationInstantiationException{
		//make a request to the processor
		LoadApplicationRequest req = new LoadApplicationRequest(applicationFile);
		processor.addRequest(req);
		try {
			//get the reply form the processor
			Pair<Boolean, String> reply = req.getResponse();
			if (reply.getLeft().booleanValue() == false) {
				//report error
				throw new ApplicationInstantiationException(reply.getRight());
			} 
		} catch (InterruptedException e) {
			//logger.log(Level.ERROR, e.getMessage(), e);
			throw new ApplicationInstantiationException(e.getMessage());
		}
	}


	/**
	 * {@link IJLCFContainer}
	 */
	@Override
	public <T> T getCallback() throws ComponentReferenceException{
		CallContextInformation cxtInfo = contextInformation.get();
		if (cxtInfo == null) {
			//logger.debug("No context available");
			return null;
		} else {
			//logger.debug("fetched context:"+cxtInfo.toString());
			if (cxtInfo.getCallbackAddress() != null){
				//T cd = getComponentReference(cxtInfo.getCallbackAddress()); //change as below to satisfy bug#6302954 : Inference fails for type variable return constraint
				T cb = this.<T>getComponentReference(cxtInfo.getCallbackAddress());
				return cb;
			} else {
				return null;
			}			
		}

	}

	/**
 	 * called by the framework internally when a call arrives at the context manager of the interface.
	 * @param callContext the context of the call
	 */
	protected void setCallContext(CallContextInformation callContext) {
		contextInformation.set(callContext);
	}
	
	/**
	 * {@link IJLCFContainer}
	 */
	@Override
	public <T> T getComponentReference(String targetPath) throws ComponentReferenceException{
		//this request should not be serviced by the processor because of concurrency issues.
		//When a component calls another component providing a callback then this causes a deadlock.
		//it is served directly here.
		try {
			//get the callback type form the application descrpition
			String targetType = JLCFFrameworkUtilities.getReceptacleType(targetPath, 
								processor.getComponentApplication());
			
			Pair<Object, IContextManagerReceptacle> compRef = 
					JLCFFrameworkUtilities.getReceptacleContextInterceptor(targetPath, null, targetType, this);
			
			return (T)compRef.getLeft();
		}catch (Exception e ) {
			//logger.log(Level.ERROR, "Exception while trying to get ReceptacleContextInterceptor", e);
			throw new ComponentReferenceException("cannot find interface on path "+targetPath+" or processing error. Exception:"+e.getMessage());
			
		}


	}
	

	/**
	 * gets a reference to a target components context-aware interface.
	 * Requestors are only the internal framework receptacle context managers when trying to resolve 
	 * references.
	 * @param path the path of the target component in the form of component/interface
	 * @return returns a pair that contains the target context-aware interface and a potential callback path, if the receptacle expects a callback
	 * @throws ComponentReferenceException if the target path cannot be resolved
	 */
	/**
	 */
	protected IContextManagerInterface getTargetReference(String path) throws ComponentReferenceException {
		//logger.debug("getting target reference for path:"+path);
		//create and send request to the processor
		ComponentTargetRequest req = new ComponentTargetRequest(path);
		processor.addRequest(req);
		Pair<Boolean, IContextManagerInterface> target = null;
		try {
			target = req.getResponse();
		} catch (InterruptedException e) {
			//logger.log(Level.ERROR, e.getMessage(), e);
			throw new ComponentReferenceException("error while processing request. Interrupted.:"+e.getMessage());
		}
		if (target.getLeft() == false) { //cannot find this component
			throw new ComponentReferenceException("cannot find interface on path "+path+" or processing error");
		} else {
			//component found
			return target.getRight();
		}

	}
	
	
	/**
	 * {@link IJLCFContainer}
	 */
	@Override
	public Pair<Boolean, String> singleComponentReconfguration(String component, String replacement, long millis) 
			throws Exception{
		//logger.debug("received reconfiguration request");
		ComponentReplacementRequest req = new ComponentReplacementRequest(component, replacement, millis);
		processor.addRequest(req);
		Pair<Boolean, String> reply = null;
		try {
			reply = req.getResponse();
		} catch (InterruptedException e) {
			//logger.log(Level.ERROR, e.getMessage(), e);
			throw new Exception("error while processing request. Interrupted.:"+e.getMessage());
		}
		return reply;
	}
	
}
