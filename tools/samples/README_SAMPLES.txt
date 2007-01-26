

These tools require ant to be in the execution path.
 - for HTTP Proxy, edit proxy entries in:
   trafficlight_server/project.properties
 - for deploy target, set $HOME/.ant.properties
   catalina.home: <J2EE server home directory>
 - execute 'ant' in samples directory
 - output of the build is placed in the root of the distribution
 - copy the traffic.war file to your application server webapps directory
 - execute the client: java -jar traffic-client.jar
 
 This project may be used with Eclipse.
 - In your Eclipse project
   - Under: Window->Preferences->Java-Build Path->Classpath Variables
     Add the variable: WISEMAN_HOME=<directory of Wiseman distribution>
   - Select File->Import->Existing Projects into Workspace and then "Next"
   - Browse to "samples" directory and then "OK"
   - Select the "samples" prject and "Finish"
   
 - To build within Eclipse:
   - right click on build.xml & select Run As->Ant Build
   
