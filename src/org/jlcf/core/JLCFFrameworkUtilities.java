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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jlcf.core.exception.ComponentReferenceException;
import org.jlcf.core.types.xml.Application;
import org.jlcf.core.types.xml.Component;
import org.jlcf.core.types.xml.Interface;
import org.jlcf.core.types.xml.Property;
import org.jlcf.core.types.xml.Receptacle;
import org.jlcf.core.util.Pair;

/**
 * Several useful methods that are called internally by the framework
 * 
 * @author Petros Pissias
 *
 */
public class JLCFFrameworkUtilities {

	private static final Logger logger = Logger.getLogger(JLCFFrameworkUtilities.class);
	

	/**
	 * instantiates the POJO component provided by the user, and returns a structure containing
	 * relevant data that the framework needs to access at runtime.
	 * The component is instantiated as described in the component description. 
	 * 
	 * @param compDescription the component description
	 * @param componentApplication the application descrpition, needed for getting the type of receptacles
	 * @return the relevant data structures that are needed at runtime.
	 * @throws Exception in case the component cannot be found or there is a problem creating the receptacles proxies.
	 */
	protected static JLCFComponent instantiateComponent(Component compDescription, Application componentApplication, JLCFContainer container) throws Exception{
		
		/**
		 * the steps are the following
		 * - instantiate the component pojo
		 * - create the interface chains. that is POJO <- Component Proxy <- Connector (per interface) <- context manager
		 */
		
		//logger.debug("instantiating component:"+compDescription.getName());
		
		//get the POJO instance with its proxy object, and the proxy handler and receptacles. use the default implementation class
		Pair<Object,JLCFComponent> pojoComponentInstance = instantiatePOJO(compDescription, null, componentApplication, container);

		//now create the interfaces chain 
		
		//create the first level proxy that implements all interfaces
		//the proxy implements all the formal interfaces of the component
		Class<?>[] interfaces = new Class<?>[compDescription.getInterface().size()];
		for (int i=0;i<compDescription.getInterface().size();i++) {
			interfaces[i] = Class.forName(compDescription.getInterface().get(i).getType());
		}
		
		//create one connector and one context manager on top of the connector for each interface and store them for the return
		Map<String, Pair<IConnectorManager, IContextManagerInterface>> interfaceConnectors 
							= new HashMap<String, Pair<IConnectorManager, IContextManagerInterface>>();
		for (Interface receptacleDescription : compDescription.getInterface()) {
			//connector
			Pair<Object, IConnectorManager> connectorObjs = JLCFFrameworkUtilities.getConnector(receptacleDescription, pojoComponentInstance.getLeft(), pojoComponentInstance.getRight().getComponentProxy());
			//context handler
			IContextManagerInterface contextHandlerIntf = JLCFFrameworkUtilities.getInterfaceContextInterceptor(connectorObjs.getLeft(), container);
			interfaceConnectors.put(receptacleDescription.getName(), 
										new Pair<IConnectorManager, IContextManagerInterface>(connectorObjs.getRight(), contextHandlerIntf) );
		}
				
		//return the overall structure
		JLCFComponent componentHolder 
					= new JLCFComponent(pojoComponentInstance.getRight().getComponentProxy(), pojoComponentInstance.getRight().getReceptacles(), interfaceConnectors);
		//logger.info("component:"+compDescription.getName()+" instantiated");

		return componentHolder;
	}


	/**
	 * It instantiates the POJO component provided by the user, and returns a structure containing
	 * relevant data.
	 * 
	 * This is also called during dynamic reconfiguration as
	 * in these cases, we do not instantiate the connector chains as they are reused
	 * form the previous component that is being replaced.
	 * 
	 * @param compDescription the component description
	 * @param implementationClass the component implementation class. Null if the default class in the component description is to be used.
	 * @param componentApplication The aplpciation description
	 * @return the relevant data structures that are needed at runtime. A component proxy implementation, implementing all the formal component interfaces and a JLCFComponent
	 * instance without the connector information.
	 * @throws Exception in case the component cannot be found or there is a problem creating the receptacles proxies.
	 */
	protected static Pair<Object,JLCFComponent> instantiatePOJO(Component compDescription, String implementationClass, 
																		Application componentApplication, JLCFContainer container) throws Exception{
		
		/**
		 * the steps are the following
		 * - create all receptacle chains. This is needed prematurely as the component pojo expects specific
		 * arguments at its constructor. Later on, the framework will set the actual targets on the receptacles.
		 * - create the constructor arguments for the component pojo. This contains receptacles and properties.
		 * - instantiate the component
		 */
		
		//logger.debug("instantiating component:"+compDescription.getName());
		//get instances (proxies) of all receptacles and their chains
		HashMap<String, Pair<Object, Pair<IReceptacle, IContextManagerReceptacle>> > listOfReceptacles
								= new HashMap<String, Pair<Object, Pair<IReceptacle, IContextManagerReceptacle>>>();
		for (Receptacle receptacleDescription : compDescription.getReceptacle()) {
			listOfReceptacles.put(receptacleDescription.getName(), getReceptacle(receptacleDescription, componentApplication, container));
		}
		
		//create property values
		HashMap<String, String> properties = new HashMap<String, String>();
		for (Property prop : compDescription.getProperty()) {
			properties.put(prop.getName(), prop.getValue());
		}
		
		//get the class of the pojo
		Class<?> pojoClass = null;
		if (implementationClass != null) {
			//logger.debug("loading alternative implementation class :"+implementationClass);
			pojoClass = Class.forName(implementationClass);
		} else  {
			//logger.debug("loading class as defined in the application description:"+compDescription.getImplementationClass());
			pojoClass = Class.forName(compDescription.getImplementationClass());
		}

		//get the pojo instance
		Object pojoInstance = getComponentPojoInstance(listOfReceptacles, properties , pojoClass, container);
		
		//now create the interfaces chain (proxies)
		
		//create the first level proxy that implements all interfaces
		//the proxy implements all the formal interfaces of the component, it is primarily used to
		//call init methods on pojos and for dynamic reconfiguration.
		Class<?>[] interfaces = new Class<?>[compDescription.getInterface().size()];
		for (int i=0;i<compDescription.getInterface().size();i++) {
			interfaces[i] = Class.forName(compDescription.getInterface().get(i).getType());
		}
		//create proxy
		ComponentProxyHandler compProxyHandler = new ComponentProxyHandler(pojoInstance, compDescription.getName());
		Object componentProxy = Proxy.newProxyInstance(JLCFFrameworkUtilities.class.getClassLoader(), 
				interfaces, compProxyHandler) ;
				
		
		//create the receptacle references for the return
		Map<String , Pair<IReceptacle, IContextManagerReceptacle>> receptaclesReferences 
								= new HashMap<String , Pair<IReceptacle, IContextManagerReceptacle>>();
		for (String receptacleName : listOfReceptacles.keySet()) {
			receptaclesReferences.put(receptacleName, listOfReceptacles.get(receptacleName).getRight());
		}
		
		//return the overall structure
		JLCFComponent componentHolder 
					= new JLCFComponent(compProxyHandler, receptaclesReferences, null);
		
		if (implementationClass != null) {
			//logger.info("component:"+compDescription.getName()+" instantiated with new implementation class:"+implementationClass);
		}
		return new Pair<Object,JLCFComponent> (componentProxy, componentHolder);
	}

	/**
	 * This method returns an instance of the component pojo. It does the following:
	 * - checks the number of constructors. Only 1 constructor is allowed.
	 * - determines the arguments for the constructor to be used
	 * - creates the arguments for the constructor (if necessary)
	 * - creates and returns an instance of the component pojo
	 * @param receptacles the receptacle instances that the component pojo expects
	 * @param properties the properties that the component expects
	 * @param targetClass the type of the component
	 * @return an instance of the component pojo
	 */
	private static Object getComponentPojoInstance(Map<String, Pair<Object, Pair<IReceptacle, IContextManagerReceptacle>>> receptacles, Map<String, String> properties, Class<?> targetClass, JLCFContainer container) throws Exception {
		//get the constructors of the target class
		Constructor<?>[] pojoConstructors = targetClass.getConstructors();
		//logger.debug("Inspecting "+targetClass.getName()+ " class, found "+pojoConstructors.length+" constructors");
		
		if (pojoConstructors.length > 1) {
			throw new Exception(targetClass.getName()+ " More than 2 constructors are not supported. Only use one.");
		} else { //only 1 constructor, OK
			Constructor<?> pojoConstructor = pojoConstructors[0];
			
			//get constructor param types
			Class<?>[] constructorParams = pojoConstructor.getParameterTypes();
			//get annotations
			Annotation[][] annotations = pojoConstructor.getParameterAnnotations();
			//create arguments array
			Object[] args = new Object[pojoConstructor.getParameterTypes().length];
			
			//logger.debug("performing basic POJO constructor checks");

			//check if some of the parameters are not annotated. This is an error.
			for (int i=0; i< annotations.length ; i++) {
				//logger.debug("checking argument "+i+" type:"+constructorParams[i].getName());
				if (annotations[i].length == 0) {
					throw new Exception(targetClass.getName()+" all constructor args must be annotated");
				}
			}
			
			//go through all arguments and create the value instances
			//logger.debug("performing basic POJO constructor high level checks");
			for (int i=0; i< constructorParams.length ; i++) {
				//logger.debug("checking argument "+i+" type:"+constructorParams[i].getName());

				Annotation[] argAnnotations = annotations[i];
				boolean foundValidAnnotation = false;
				for (Annotation annotation: argAnnotations ) {
					//logger.debug("found annotation: "+annotation.annotationType());
					if (annotation.annotationType().equals(org.jlcf.core.annotation.Property.class)) {
						foundValidAnnotation = true;
						String propName = ((org.jlcf.core.annotation.Property)annotation).name();
						String value = properties.get(propName);
						if (value == null) {
							throw new Exception("cannot find Property with name "+propName);
						}
						//now cast the object to the appropriate type and put it in the arguments array of the pojo
						//this is arg[i] of the pojo
						//logger.debug("casting "+value+" to "+constructorParams[i].getName());
						if (constructorParams[i].equals(String.class)) {
							args[i] = value;
						} else if (constructorParams[i].equals(Integer.class)) {
							args[i] = Integer.parseInt(value);
						} else if (constructorParams[i].equals(Float.class) ) {
							args[i] = Float.parseFloat(value);
						} else if (constructorParams[i].equals(Double.class)) {
							args[i] = Double.parseDouble(value);
						} else if (constructorParams[i].equals(Boolean.class)) {
							args[i] = Boolean.parseBoolean(value);
						} else {
							throw new Exception("Cannot cast value to type:"+constructorParams[i].getName());
						}
					} else if (annotation.annotationType().equals(org.jlcf.core.annotation.Receptacle.class)) {
						//this is a receptacle
						foundValidAnnotation = true;
						//get rec name and proxy object
						String receptacleName = ((org.jlcf.core.annotation.Receptacle)annotation).name();
						Object receptacleProxy = receptacles.get(receptacleName).getLeft();
						if (receptacleProxy == null) {
							throw new Exception("cannot find Receptacle with name"+receptacleName);
						}
						//now cast the object to the appropriate type and put it in the arguments array of the pojo
						//this is arg[i] of the pojo
						//logger.debug("casting type "+receptacleProxy.getClass()+" to "+constructorParams[i].getName());
						args[i] = constructorParams[i].cast(receptacleProxy);
					}  else if (annotation.annotationType().equals(org.jlcf.core.annotation.ContainerRef.class)) {
						//this is a conteiner referecne annotation
						foundValidAnnotation = true;
						
						//this is a reference to the container
						//inject the container reference
						//logger.debug("Injecting container reference to argument number "+(i+1));
						args[i] = (IJLCFContainer)container;
					} 
				}
				if (!foundValidAnnotation) {
					throw new Exception(targetClass.getName()+ "parameter "+i+" is not Annotated properly. All constructor args must be annotated with Receptcle or Property");
				}
			}
			
			//logger.debug("Processing component "+targetClass.getName()+ " class, invoking constructor");
			for ( int i=0; i<args.length;i++) {
				//logger.debug("argument "+i+" type:"+args[i].getClass().getName());
			}
			Object pojo = pojoConstructor.newInstance(args);
			return pojo;
		} 
	}

	
	/**
	 * returns an interceptor instance of the provided class.
	 * @param interceptorClass the full name of the class
	 * @return an interceptor instance.
	 * @throws Exception in case the class or instance cannot be loaded / created.
	 */
	private static Interceptor getInterceptor(String interceptorClass) throws Exception {
		Class<?> interceptorPojoClass = Class.forName(interceptorClass);
		Constructor<?> interceptorPojoConstructor = interceptorPojoClass.getConstructor(new Class[]{});
		Object interceptorInstance = interceptorPojoConstructor.newInstance();	
		//logger.info("created interceptor instance of class "+interceptorClass);

		return (Interceptor)	interceptorInstance;
	}
	

	/**
	 * creates a new receptacle object chain for component.
	 * @param receptacleDescription the description of the receptacle
	 * @param componentApplication 
	 * @return returns the proxy object implementing the receptacle interface that is passed to the user component and the receptacle manager and context interceptor invoction handlers.
	 * @throws Exception in case the receptacle cannot be created.
	 */
	private static Pair<Object, Pair<IReceptacle, IContextManagerReceptacle>> getReceptacle (Receptacle receptacleDescription, Application componentApplication, JLCFContainer container) throws Exception{
		/**
		 * The steps for creating a receptacle are the following:
		 * - create instances of all interceptors
		 * - instantiate the receptacle manager proxy invocation handler
		 * - create the proxy object
		 * - create the context manager for the receptacle.
		 */
		//logger.debug("Processing receptacle : "+receptacleDescription.getName());
		//determine number of interceptors and create array
		int numberOfInterceptors = 0;
		if (receptacleDescription.getInterceptor() != null) {
			numberOfInterceptors = receptacleDescription.getInterceptor().size();
		}
		Interceptor[] interceptors = new Interceptor[numberOfInterceptors];
		//logger.debug(receptacleDescription.getName()+" "+interceptors.length+" interceptors");
		
		//put interceptor instances into the array
		for (int i=0;i<numberOfInterceptors;i++) {
			interceptors[i] = JLCFFrameworkUtilities.getInterceptor(receptacleDescription.getInterceptor().get(i).getType());
		}
		
		//create the context-aware proxy of the receptacle
		String path = receptacleDescription.getReference().getPath();
		String callbackPath = receptacleDescription.getReference().getCallbackReference();
		//get the typs of the receptacle,

		String receptacleType = receptacleDescription.getReference().getType();
		if (receptacleType == null || "".equals(receptacleType)) {
			//logger.debug("Receptacle type not provided for receptacle"+receptacleDescription.getName()+". Resolving.");
			//if not provided at all or empty then it needs to be determined
			receptacleType = getReceptacleType(path, componentApplication);
			//logger.debug("resolved to :"+receptacleType);
		}
		
		Pair<Object,IContextManagerReceptacle> receptacleContextManager = JLCFFrameworkUtilities.getReceptacleContextInterceptor(path, callbackPath, receptacleType, container);
		Object contextAwareRecProxy = receptacleContextManager.getLeft();
		
		//create invocation handler and proxy object for the target interface of the receptacle
		String receptacleWrapperName = receptacleDescription.getName()+":"+receptacleDescription.getReference().getPath();
		JLCFReceptacle handler = new JLCFReceptacle(interceptors, receptacleWrapperName, contextAwareRecProxy);
		
		//create the receptacle proxy that will be passed to the POJO constructor
		Object proxy = Proxy.newProxyInstance(JLCFFrameworkUtilities.class.getClassLoader(), new Class[]{ 
									Class.forName(receptacleType)}, handler);
		//logger.debug("Finished processing receptacle : "+receptacleDescription.getName());

		//create return type
		Pair<IReceptacle, IContextManagerReceptacle> recManagers = 
						new Pair<IReceptacle, IContextManagerReceptacle>(handler, receptacleContextManager.getRight());
		return new Pair<Object, Pair<IReceptacle, IContextManagerReceptacle>>(proxy, recManagers);
	}
	
	/**
	 * returns a context aware proxy for a receptacle. 
	 * This is also returned to the framework users when they request access to one of the component interfaces.
	 * @param path path of the target
	 * @param callback the callback path, if any
	 * @return returns a proxy object implementing the user interface and an ReceptacleContextInterceptorHandler that adds context aware information
	 * @throws ClassNotFoundException 
	 */
	protected static Pair<Object, IContextManagerReceptacle> getReceptacleContextInterceptor(String path, String callbackpath, String userInterface, JLCFContainer container) throws ClassNotFoundException {
		//create the context-aware proxy of the receptacle, also implements IReceptacleContextInterceptor
		//logger.debug("loading class:"+userInterface);
		Class<?>[] interfaces = new Class[]{ Class.forName(userInterface)};
		ReceptacleContextManager contextRecInterceptor 
								= new ReceptacleContextManager(path, callbackpath, container);
		Object contextAwareRecProxy = Proxy.newProxyInstance(JLCFFrameworkUtilities.class.getClassLoader(), 
				interfaces, contextRecInterceptor);		
		//logger.debug("returning proxy and context handler");

		return new Pair<Object, IContextManagerReceptacle>(contextAwareRecProxy, contextRecInterceptor);
	}
	

	/**
	 * Returns a connector for the specified interface
	 * @param intf the interface description
	 * @param componentProxy the component proxy that the conector will forward all calls 
	 * @return the connector proxy implementing the target interface and the connector manager (invocation handler)
	 */
	private static Pair<Object,IConnectorManager> getConnector(Interface intf, Object componentProxy, IComponentProxy compProxyHandler) throws Exception {
		//logger.debug("Processing interface : "+intf.getName());
		
		//create invocation handler and proxy object for the target interface of the component
		ConnectorHandler handler = new ConnectorHandler(componentProxy, compProxyHandler, intf.getName()+":"+intf.getType());
		
		Class<?> interfaceClass = Class.forName(intf.getType());
		Object proxy = Proxy.newProxyInstance(JLCFFrameworkUtilities.class.getClassLoader(), new Class[]{interfaceClass}, handler);	
		
		return new Pair<Object, IConnectorManager>(proxy, handler);
	}	
	

	/**
	 * returns a new interface context manager
	 * @param connector the connector that this context manager will forward calls
	 * @return the new instance of the interface context manager
	 */
	private static IContextManagerInterface getInterfaceContextInterceptor(Object connector, JLCFContainer container){
		//logger.debug("Creating interface context interceptor for connector");
		
		//create InterfaceContextInterceptorHandler
		InterfaceContextManager handler = new InterfaceContextManager(connector, container);

		return handler;
	}		
	
	/**
	 * helper method to return a receptacle description
	 * @param component the component name
	 * @param receptacleName the receptacle name
	 * @param applicationDesc the application description
	 * @return null if the receptacle does not exits or the Receptacle description
	 */
	protected static Receptacle getReceptacleDescription(String component, String receptacleName, Application applicationDesc) {
		List<Component> components = applicationDesc.getComponent();
		for (int i=0; i< components.size(); i++ ) {
			if (components.get(i).getName().equals(component)) {
				//found component
				List<Receptacle> receptacles = components.get(i).getReceptacle();
				for (int j=0;j<receptacles.size();j++) {
					if (receptacles.get(j).getName().equals(receptacleName)) {
						//found receptacle
						return receptacles.get(j);
					}
				}
			}
		}
		
		return null;
	}
	
	/**
	 * helper method that returns an interface
	 * @param component the component name
	 * @param intf the interface name
	 * @param applicationDesc the application description
	 * @return null if the interface does not exist, or the Interface description
	 */
	protected static Interface getInterfaceDescription(String component, String intf, Application applicationDesc) {
		List<Component> components = applicationDesc.getComponent();
		for (int i=0; i< components.size(); i++ ) {
			if (components.get(i).getName().equals(component)) {
				//found component
				List<Interface> interfc = components.get(i).getInterface();
				for (int j=0;j<interfc.size();j++) {
					if (interfc.get(j).getName().equals(intf)) {
						//found receptacle
						return interfc.get(j);
					}
				}
			}
		}
		
		return null;		
	}
	
	/**
	 * Returns the type of a receptacle by investigating its path
	 * @param path the path
	 * @param componentApplication the applciation description
	 * @return the interface typs of the receptacle
	 * @throws ComponentReferenceException in case the path cannot be mapped to an interface
	 */
	protected static String getReceptacleType(String path, Application componentApplication) throws ComponentReferenceException {
		
		String[] pathsplit = path.split("/");
		if (pathsplit.length != 2) {
			throw new ComponentReferenceException("path :"+path+" cannot be deoded");
		}
		String componentName = pathsplit[0];
		String targetInterfaceName = pathsplit[1];
		
		for (Component comp : componentApplication.getComponent() ) {
			if (comp.getName().equals(componentName)) {
				//found
				for (Interface intf : comp.getInterface()) {
					if (intf.getName().equals(targetInterfaceName)) {
						//found target interface
						return intf.getType();
					}
				}
			}
		}
		
		throw new ComponentReferenceException("path :"+path+" cannot be mapped to an interface");
	}

}
