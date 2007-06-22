

These tools require ant to be in the execution path.
 - for HTTP Proxy, edit proxy entries in:
   trafficlight_server/project.properties
 - for deploy target, set $HOME/.ant.properties
   catalina.home: <J2EE server home directory>
 - execute 'ant' in samples directory
 - output of the build is placed in the root of the distribution
 - copy the traffic.war and users.war files to your application server webapps directory
 - execute the clients with the following cmds: 
 	> java -jar traffic-client.jar
 	> java -jar users-client.jar
 	> java -jar gui-users-client.jar
 
 This project may be used with Eclipse.
 - In your Eclipse project
   - Under: "Window->Preferences->Java-Build Path->Classpath Variables"
     Add the variable: WISEMAN_HOME=<directory of Wiseman distribution>
   - Select "File->Import->General->Existing Projects into Workspace" and then click "Next"
   - Browse to "samples" directory and then "OK"
   - Select the "samples" project and "Finish"
   
 - To build within Eclipse using ant:
   - right click on build.xml & select "Run As->Ant Build"
   
 - Each of the samples(contacts/contacts_client/traffic_client/traffic_server) are separate
 	ant projects and can be loaded individually with your IDE. The project.properties
 	and .classpath files should populate automatically if using Eclipse.