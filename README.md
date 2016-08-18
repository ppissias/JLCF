# JLCF
**Java Lightweight Component Framework**

![alt tag](manual/component_cloud.jpg)

The **Java Light Component Framework (JLCF)** is a framework for developing modular applications in Java. It allows designing an application using building blocks with well-defined inputs and outputs - aka software components. Rising the abstrction level from objects to software components provides a concrete design pattern which facilitates the development and maintenance of software. 

**What is a software component ?** While there is no formal commonly agreed definition in academia, the following definition is adopted: *a software component is a piece of software offering and requiring functionality explicitly*. The Framework maps a software component to a Java Class implementing and requiring a set of Java Interfaces.      

JLCFs core is a lightweight dependency injection framework which serves as a structural pattern for the design of an application. It goes one step further from standard dependency injection frameworks in explicitly defining a components inputs (implemented interfces), outputs (required interfaces) and how an application is formed by connecting various instances of components. 

JLCF focuses on usability with a simple programming model and also provides advanced features such as interceptors between component connections, explicit callbacks on the definition of interfaces and also supports dynamic component replacement at runtime.

The user manual of JLCF, containing all of its concepts and examples can be downloaded here:

[Read the manual!](manual/jlcf-1.0.0.pdf)

Go to the JLCF examples repository

[JLCF Examples repository](https://github.com/ppissias/JLCFExamples)

##Quick Tutorial of the concepts

###How do you specify the software components and how they are connected

The central point to define how an application is formed as a composition of components is the Application Description file. This is an XML file which enables defining components and their interconnections. Remember: Explicit software design and architecture are good! 

In this XML file, you create and connect components. Each component offers functionality through interfaces and requires functionality through interface dependencies (also called receptacles). In both cases the software either offers or requires functionality via plan Java Interfaces. 

This is a definition of a simple component
```xml
	<component implementationClass="example.ComponentA" name="compA">
		<interface name="processData" type="example.IDataProcessor" />
	</component>
```
It is implemented by class *example.ComponentA* and offers a servive called *processData* which is defined by interface *example.IDataProcessor* . 


###How do you implement the components

Each component in the Application Description XML file points to a java class. This is the main component class that the framework will instantiate. 

Below is the implementation of *ComponentA*:
```java
package example;
public class ComponentA implements IDataProcessor {

	public ComponentA() {
		System.out.println("ComponentA: instantiating");		
	}

	@Override
	public boolean processData(String data) {
		System.out.println("Processing Data:"+data);
		//process data
		
		return true;
	}

}
```

and the definition of the *IDataProcessor* service:
```java
package example;
public interface IDataProcessor {

	public boolean processData(String data);

}
```

**OK now we need another component that uses the provided service of component A.**
This is defined as follows:
```java
	<component implementationClass="example.ComponentB" name="compB">	
		<receptacle name="dataProcessor">
			<Reference path="compA/processData"/>
		</receptacle>
	</component>
```

The component references a service provided by another component (compA/processData) and the framework will inject a reference able to invoke this service when it instantiates it. 

Below is the implementation of *Component B*: 
```java
package example;
public class ComponentB {

	private final IDataProcessor dataProcessor;

	public ComponentB(@Receptacle(name = "dataProcessor") IDataProcessor dataProc) {

		this.dataProcessor = dataProc;
		System.out.println("ComponentB: instantiating");
	}

	@InitMethod
	public void init() {
		System.out.println("ComponentB: initializing");
		//call every 5 seconds the service provided by the other component
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				String randomData = new Double(new Random().nextDouble()).toString();	
				System.out.println("ComponentB: Sending Data to be processed");
				boolean ret = dataProcessor.processData(randomData);
				System.out.println("ComponentB: Data processing reply:"+ret);
			}
		}, 0, 5000);
	}
}
```


Notice in the constructor arguments, we use an annotation *@Receptacle* for the argument, which will instruct the framework to inject an object that can be used to call the required service. 

Finally, in order to send some data to the other component we create a *Timer* that will periodically send random data to the service offered by the other component.  Notice the annotation *@InitMethod*. Using this annotation we instruct the framework to call this method after instantiating and connecting the component. This is  an appropriate place to create this periodic timer. 

###OK How to start the application now ? 

Assuming that the XML configuration file is placed in a file named *ExampleTest.xml* in folder *resources* :

```java
public class TestExample {

	public static void main(String[] args) throws Exception {
		IJLCFContainer runtime = JLCFContainer.getInstance();
		runtime.loadApplication("resources/ExampleTest.xml");
	}
}
```

and the output is

`
ComponentA: instantiating
ComponentB: instantiating
ComponentB: initializing
ComponentB: Sending Data to be processed
Processing Data:0.42589666410609905
ComponentB: Data processing reply:true
ComponentB: Sending Data to be processed
Processing Data:0.7387504225752601
ComponentB: Data processing reply:true
ComponentB: Sending Data to be processed
Processing Data:0.17061612034322726
ComponentB: Data processing reply:true
`

The full XML file of the application, defining and connecting the 2 components is shown below 
```xml
<?xml version="1.0" encoding="UTF-8"?>
<Application applicationName="Test Example"
	xmlns="http://jlcf.sourceforge.net/JLCFApplication" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

	<component implementationClass="example.ComponentA" name="compA">
		<interface name="processData" type="example.IDataProcessor" />
	</component>


	<component implementationClass="example.ComponentB" name="compB">	
		<receptacle name="dataProcessor">
			<Reference path="compA/processData"/>
		</receptacle>
	</component>

</Application>
```
This is included in the JLCF Examples

###Download the latest version from the releases

##Using the binary distribution

The binary distribution of JLCF is a .zip file with the following structure:

**dist** ==> contains the JLCF framework .jar file

**ext_lib** ==> contains libraries used by JLCF (currentyly only log4j) . These need to be in the classpath as well

**docs** ==> contains the javadoc documentation and the .pdf manual

**LICENSE** ==> contains some licensing related information

In order create a JLCF application, you need to have jlcf-1.0.0.jar and the log4j jar in your classpath.

##Running the JLCF Examples

The JLCF Examples distribution is a separate .zip file. After unzipping the file you can run the examples. All you need is ant and java.

at the root location of the unzipped folder run:

**ant -f runProject** ==> this will tun the calculator example

**ant -f runProject \<name\>** ==> this will run the example indicated by <name>. 

The example names can be found in the ant runProject.xml file (advancedcalculator, simplecallback, advancedcallback, interceptors, remotinginterceptor, reconfiguration, remoting)

##Downloading and compiling the source distribution

The source distribution of JLCF is contained in the 2 eclipse projects, JLCF and JLcFExamples. The projects can be imported in eclipse in order to further develop the system. In order to just build JLCF you do not need to import the project in eclipse, it can be done from the command line using ant.

##Building JLCF

go the the JLCF folder and type:

**and -f buildSchema** ==> this will build the necessary java types from the .xsd schemas of JLCF

**ant -f buildProject** ==> this will compile JLCF, generate the java documentation and create the .jar and .zip distribution. The .zip distribution will be located in the dist folder.

##Building the JLCF Examples

copy the jlcf jar from JLCF/dist to JLCFExamples/ext_lib

go to the JLCFExamples folder type:

**ant -f buildProject** ==> This will compile the JLCF examples and generate the .zip distribution in the dist folder.

##Importing in eclipse
JLCF is developed using eclipse Juno SR2. It can be imported in eclipse using the "import existing projects" function.



