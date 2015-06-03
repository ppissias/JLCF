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

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jlcf.core.dynrec.SingleComponentReconfigurationManager;
import org.jlcf.core.exception.ApplicationInstantiationException;
import org.jlcf.core.exception.ApplicationReconfigurationException;
import org.jlcf.core.types.request.ComponentReplacementRequest;
import org.jlcf.core.types.request.ComponentTargetRequest;
import org.jlcf.core.types.request.ContainerProcessorRequestType;
import org.jlcf.core.types.request.LoadApplicationRequest;
import org.jlcf.core.types.xml.Application;
import org.jlcf.core.util.AbstractQueueProcessor;
import org.jlcf.core.util.GenericProcessorRequest;
import org.jlcf.core.util.Pair;

/**
 * This is the processor of the JLCF framework.
 * It processes messages of type ContainerProcessorRequest.
 * 
 * This is where most requests to the framework are handled.
 * @author Petros Pissias
 *
 */
public class JLCFContainerProcessor extends AbstractQueueProcessor<GenericProcessorRequest<ContainerProcessorRequestType>> {

	private final Logger logger = Logger.getLogger(this.getClass());
	
	// the description of the application. volatile as it is accessed by other threads through the JLCFContainer that contains this processor
	private volatile Application componentApplication;
	
	//holds component name -> useful data needed for each component
	private final Map<String, JLCFComponent> components;
	
	//the framework container
	private final JLCFContainer container;
	
	public JLCFContainerProcessor(JLCFContainer container) {
		super("JLCFContainerProcessor");
		
		//initialize the components map
		components = new HashMap<String, JLCFComponent>();
	
		//set the framework container
		this.container = container;
	}

	@Override
	public void processEvent(GenericProcessorRequest<ContainerProcessorRequestType> event) throws Exception {
		//logger.debug("received request:"+event.getRequestType());
		//switch depending on the request type
		switch (event.getRequestType()) {
		case LOAD_APPLICATION :{ 
			//request to load an application based on a description file
			LoadApplicationRequest req = (LoadApplicationRequest)event;
			try {
				handleLoadAppRequest(req.getApplicationDescription());
				req.insertResponse(new Pair<Boolean, String>(true,""));
			}catch (Exception e) {
				req.insertResponse(new Pair<Boolean, String>(false,e.getMessage()));
				throw (e);
			}
			break;
		}
		
	
		case GET_TARGET_REFERENCE : {
			//request by the framework itself, when a receptacle context manager tries to resolve a reference to a component itnerface
			ComponentTargetRequest req = (ComponentTargetRequest) event;
			try {
				IContextManagerInterface response = handleComponentTargetRequest(req.getPath());
				req.insertResponse(new Pair<Boolean, IContextManagerInterface>(true,response));
			} catch (Exception e) {
				req.insertResponse(new Pair<Boolean, IContextManagerInterface>(false,null));
				throw (e);
			}
			break;

		}	
		
		case QUISCE_SINGLE : {
			//request by the user to replace a component at runtime 
			ComponentReplacementRequest req = (ComponentReplacementRequest) event;
			try {
				Pair<Boolean, String> reply = handleComponentReplacementRequest(req.getTargetComponent(), req.getNewComponent(), req.getMillis());
				req.insertResponse(reply);
			} catch (Exception e) {
				req.insertResponse(new Pair<Boolean, String>(false, "proessing error"+e.getMessage()));
				throw (e);
			}			
			break;
		}
		default : {
			//logger.info("ignoring request:"+event.getRequestType());
		}
		}
	}
	
	/**
	 * handles a component replacement request
	 * @param targetComponent the target component
	 * @param millis the milliseconds that we want the reconfiguration to be reached
	 * @return true of the component was replaced false otherwise
	 * @throws Exception in case the target component cannot be found
	 */
	private Pair<Boolean, String> handleComponentReplacementRequest(String targetComponent,
			String newImplementation, long millis) throws Exception {

		//logger.debug("processing reconfiguration request for component:"+targetComponent);
		//find target component in component holder
		JLCFComponent component = components.get(targetComponent);
		if (component == null) {
			throw new Exception("cannot find target component");
		}
		
		//get component proxy and all connectors 
		IComponentProxy compProxy = component.getComponentProxy();
		List<IConnectorManager> connectorList = new ArrayList<IConnectorManager>();

		//get all connectors
		Map<String, Pair<IConnectorManager, IContextManagerInterface>>  connectors = component.getConnectors();
		for (String connector : connectors.keySet()) {
			connectorList.add(connectors.get(connector).getLeft());
		}
		//logger.debug("obtained proxy and connectors rfor component:"+targetComponent);

		//create the reconfiguration manager for this reconfiguration
		BlockingQueue<Pair<Boolean, String>> reconfResponseQueue = new LinkedBlockingQueue<Pair<Boolean, String>>();
		SingleComponentReconfigurationManager reconfManager 
					= new SingleComponentReconfigurationManager(compProxy, connectorList, reconfResponseQueue, millis);
		reconfManager.start();
		
		//wait for a reply form the reconfiguration manager
		Pair<Boolean, String> response = reconfResponseQueue.take();
		//logger.info("reconfiguration response:"+response.getLeft()+" "+response.getRight());

		if (response.getLeft()) {
			//reconfiguration succeeded
			//logger.info("reconfiguration was succesful, component reached quiescent state");
			
			//get internal state of the component (if it supports it)
			Object internalState = compProxy.getInternalState();
			
			//now all calls to the former interfaces are blocked
			//and the component is quiescent. We throw away the component proxy and the pojo (set to null)
			//components.remove(targetComponent);

			//replace the component --> this replaces the component with the new POJO and instantiates the new component
			replaceComponent(targetComponent, newImplementation, internalState);
			
			//inform connectors
			for (IConnectorManager connector: connectorList) {
				//logger.info("calling setReconfiguring to false on connectors");

				connector.setReconfiguring(false,0);
			}			
		} else {
			//reconfiguration failed at the specified timeframe
			
			//inform component to proceed. 
			//get lock on new component
			//logger.info("obtaining lock");
			compProxy.lock();
			
			//logger.info("calling proceed on component proxy");
			compProxy.proceed();
			
			//release lock on component
			//logger.info("releasing lock");
			compProxy.unlock();	
		}

		return response;
	}

	/**
	 * This method is used during dynamic reconfiguration.
	 * It instantiates the new component implementation, replaces the receptacles,
	 * and component proxy of the old component to the new ones from the new component,
	 * inserts the old state to the new component (if any) and initializes the component.
	 * We keep the connector chains of the old component as these are not replaced
	 * during a reconfiguration.
	 * @param targetComponent the name of the target component
	 * @param newImplementation the new implementation class
	 * @param internalState 
	 * @throws ApplicationReconfigurationException in case there are exceptions with instantiating and connecting the new component
	 */
	private void replaceComponent(String targetComponent,
			String newImplementation, Object internalState) throws ApplicationReconfigurationException {
	    try {
	    	Pair<Object,JLCFComponent> newComponent = null;
	    	JLCFComponent oldComponent = components.get(targetComponent);
	    	
		    //locate component description 
		    for (org.jlcf.core.types.xml.Component compDescription : componentApplication.getComponent()) {
		    	if (compDescription.getName().equals(targetComponent)) {
		    		//found the old component description, get the associated objects

			    	//create new instance
			    	newComponent = JLCFFrameworkUtilities.instantiatePOJO(compDescription, newImplementation, componentApplication, container);
		    		
		    		//replace component proxy and pojo to the connectors
		    		for (String connector : oldComponent.getConnectors().keySet()) {
		    			oldComponent.getConnectors().get(connector).getLeft().setTarget(newComponent.getLeft(), newComponent.getRight().getComponentProxy());
		    		}
		    		//replace the receptacles list with the ones from the new component
		    		oldComponent.setReceptacles(newComponent.getRight().getReceptacles());
		    		oldComponent.setComponentProxy(newComponent.getRight().getComponentProxy());
		    	}
		    }
		    
		    //logger.debug("central component map:"+components);
		    //connect components together
		    //we need to connect all receptacles of components that target interfaces the new component
		    //and all the receptacles of the new component.
		    for (org.jlcf.core.types.xml.Component compDescription : componentApplication.getComponent()) {
		    	//for each receptacle
		    	for (org.jlcf.core.types.xml.Receptacle receptacle : compDescription.getReceptacle()) {
		    		//if it is form the component itself
		    		if (compDescription.getName().equals(targetComponent)) {
		    			//connect all receptacles , business as usual
			    		String path = receptacle.getReference().getPath();
			    		//logger.info("connecting "+compDescription.getName()+" / "+receptacle.getName()+" -> " +path);
			    		connect(compDescription.getName(), receptacle.getName(), path);		
		    		} 
		    	}
		    }
	
		    //set the internal state of the old component (if any) to the new component
		    //logger.debug("setting internal state");
		    newComponent.getRight().getComponentProxy().setInternalState(internalState);
		    
		    //INITIALIZATION PHASE
		    //call init methods on all components
		    for (String component : components.keySet()) {
	    		if (component.equals(targetComponent) ) {
	    			//found new component that we just inserted into the application
			    	//logger.debug("calling init method on component:"+component);
			    	components.get(component).getComponentProxy().callInitMethod();    			
	    		}

		    }
	    
	    } catch (Exception e) {
	    	//logger.log(Level.ERROR, "cannot instantiate application", e);
	    	throw new ApplicationReconfigurationException(e.getMessage());
	    }		
	}



	/**
	 * handles an internal request to get a component target reference.
	 * This is called by receptacle context interceptors in order to find their target
	 * @param path  interface path
	 * @return a pair with Boolean=true , Object=Ref if succesfull, Boolean=false, Object=null otherwise.
	 * @throws Exception
	 */
	private IContextManagerInterface handleComponentTargetRequest(String targetPath) throws Exception {
		
		//check if this receptacle is used for remoting (targetPath = "")
		if (targetPath.equals("")) {
			//there is no target to the receptacle as the interceptor will intercept and redirect the call
			return null;
		}
		
		String[] path = targetPath.split("/");
		if (path.length == 2) {
			//local
			JLCFComponent compHolder = components.get(path[0]);
			if (compHolder == null) {
				throw new Exception("cannot find component:"+path[0]);
			} else {
				//component found
				for (String intfName : compHolder.getConnectors().keySet()) {
					if (intfName.equals(path[1])) {
						//found interface
						Pair <IConnectorManager, IContextManagerInterface> intfDesc = compHolder.getConnectors().get(intfName);
						return intfDesc.getRight();
					}
				}
				throw new Exception("cannot find interface on path:"+targetPath);
			}			
		}else {
			throw new Exception ("Path:"+targetPath+" cannot be decoded");
		}
	}
	/**
	 * creates a complete application based on the application input file.
	 * @param applicationFile The input file describing the component composition
	 */
	public void handleLoadAppRequest(String applicationFile) throws ApplicationInstantiationException{
	    try {
			//create the JAXB unmarshaller
			JAXBContext context = JAXBContext.newInstance(Application.class);
			Unmarshaller um = context.createUnmarshaller();
		    
			//read the file
		    componentApplication = (Application) um.unmarshal(new FileReader(applicationFile));

		    //initialize all components and store their references
		    for (org.jlcf.core.types.xml.Component compDescription : componentApplication.getComponent()) {
		    	JLCFComponent componentProxy = JLCFFrameworkUtilities.instantiateComponent(compDescription, componentApplication, container);
		    	//logger.debug("inserting component to central map:"+compDescription.getName());
		    	components.put(compDescription.getName(), componentProxy);
		    }
		    
		    //logger.debug("central component map:"+components);
		    //connect components together
		    for (org.jlcf.core.types.xml.Component compDescription : componentApplication.getComponent()) {
		    	//for each receptacle
		    	for (org.jlcf.core.types.xml.Receptacle receptacle : compDescription.getReceptacle()) {
		    		String path = receptacle.getReference().getPath();
		    		//logger.info("connecting "+compDescription.getName()+" / "+receptacle.getName()+" -> " +path);
		    		connect(compDescription.getName(), receptacle.getName(), path);		    		
		    	}
		    }

		    //INITIALIZATION PHASE
		    //call init methods on all components, by the order they are in the composite file
		    for (org.jlcf.core.types.xml.Component compDescription : componentApplication.getComponent()) {
		    	//logger.debug("calling init method on component:"+compDescription.getName());
		    	components.get(compDescription.getName()).getComponentProxy().callInitMethod();
		    }
		    
	    } catch (Exception e) {
	    	//logger.log(Level.ERROR, "cannot instantiate application", e);
	    	throw new ApplicationInstantiationException(e.getMessage());
	    }
	}
	
	/**
	 * Connects a receptacle of one component to an interface of another component.
	 * In practice this connects the 2 context interceptors at the end of each chain.
	 * @param componentSource the source component
	 * @param receptacle the receptacle name
	 * @param targetPath the target path
	 * @throws Exception in case the components cannot be connected
	 */
	private void connect(String componentSource, String receptacle,
			String targetPath) throws Exception {

		//get the receptacle of the source component
		JLCFComponent srcComponentData = components.get(componentSource);
		Pair <IReceptacle, IContextManagerReceptacle> recData 
								= srcComponentData.getReceptacles().get(receptacle);
		if (recData == null) {
			throw new Exception("cannot find :"+componentSource+"/"+receptacle);
		} else {
			//found target receptacle, set the target interface (IContextManagerInterface) 
			//and the target component to the receptacle context manager.
			IContextManagerInterface targetInterface = handleComponentTargetRequest(targetPath);
			recData.getRight().setTarget(targetInterface, targetPath);
			//get the callback, if any, from the application description 
			org.jlcf.core.types.xml.Receptacle recDesc = 
							JLCFFrameworkUtilities.getReceptacleDescription(componentSource, receptacle, componentApplication);
			String cbReference = recDesc.getReference().getCallbackReference();
			recData.getRight().setCallback(cbReference);
		}	
	}
	
	/**
	 * returns the application description
	 * @return description of the application that is currently loaded
	 */
	protected Application getComponentApplication() {
		return componentApplication;
	}
}
