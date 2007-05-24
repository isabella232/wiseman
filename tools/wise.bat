
rem set CLASSPATH
set CP=wiseman-core.jar;wiseman-tools.jar;jaxws\saaj-api.jar;jaxws\jaxb-api.jar;jaxws\saaj-impl.jar;jaxws\jaxb-impl.jar;jaxws\jsr173_api.jar

java -cp %CP% com.sun.ws.management.tools.WisemanCmdLine %*