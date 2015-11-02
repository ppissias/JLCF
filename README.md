# JLCF
Java Lightweight Component Framework

![alt tag](manual/component_cloud.jpg)

The Java Light Component Framework (JLCF) is a framework for developing modular applications for the java language. It allows designing an application using building blocks with well-defined inputs and outputs (aka software components). Using this well-defined modular approach, the application has a clear and explicit design which facilitates the development and maintenance of a software system.

JLCF focuses on usability on a simple programming model and also provides advanced features such as interceptors, callbacks and dynamic component replacement.

The user manual of JLCF, containing all of its concepts and eamples can be downlosded here:

[Read the manual!](manual/jlcf-1.0.0.pdf)

[JLCF Examples repository](https://github.com/ppissias/JLCFExamples)


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
Importing the projects in eclipse

##Importing in eclipse
JLCF is developed using eclipse Juno SR2. It can be imported in eclipse using the "import existing projects" function.


