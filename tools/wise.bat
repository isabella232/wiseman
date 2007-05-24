
rem set CLASSPATH
set CP=..\lib\wiseman-core.jar;..\lib\wiseman-tools.jar;..\lib\jaxws\saaj-api.jar;..\lib\jaxws\jaxb-api.jar;..\lib\jaxws\saaj-impl.jar;..\lib\jaxws\jaxb-impl.jar;..\lib\jaxws\jsr173_api.jar

java -cp %CP% com.sun.ws.management.tools.WisemanCmdLine %*