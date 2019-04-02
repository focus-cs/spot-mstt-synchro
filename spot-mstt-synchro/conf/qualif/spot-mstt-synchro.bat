@echo off
setlocal

rem
rem Copyright (c) 2008 SciForma. All right reserved.
rem 
rem spot_mstt_synchro.bat
rem

set JAVA_ARGS=-showversion -Dlog4j.overwrite=true -Xmx1024m -Duse_description=true

set CLASSPATH=..\mstt-lib\PSClient.jar;..\mstt-lib\utilities.jar;..\mstt-lib\PSClient_en.jar;..\lib\spot_mstt_synchro.jar;..\lib\psconnect-api-0.7.jar;..\lib\psconnect-core-0.15.0.jar;..\lib\psconnect-runner-0.14.0.jar;..\lib\spring-beans-2.5.6.jar;..\lib\spring-context-2.5.6.jar;..\lib\spring-core-2.5.6.jar;..\lib\spring-jdbc-2.5.6.jar;..\lib\spring-tx-2.5.6.jar;..\lib\jtds-1.2.2.jar;..\lib\postgresql-8.2-507.jdbc3.jar;..\lib\commons-logging-1.1.1.jar;
java %JAVA_ARGS% pco.schneider.main.SpotMsttSynchro match mstt ..\conf\
java %JAVA_ARGS% pco.schneider.main.SpotMsttSynchro extract mstt ..\conf\

set CLASSPATH=..\spot-lib\PSClient.jar;..\spot-lib\utilities.jar;..\spot-lib\PSClient_en.jar;..\lib\spot_mstt_synchro.jar;..\lib\psconnect-api-0.7.jar;..\lib\psconnect-core-0.15.0.jar;..\lib\psconnect-runner-0.14.0.jar;..\lib\spring-beans-2.5.6.jar;..\lib\spring-context-2.5.6.jar;..\lib\spring-core-2.5.6.jar;..\lib\spring-jdbc-2.5.6.jar;..\lib\spring-tx-2.5.6.jar;..\lib\jtds-1.2.2.jar;..\lib\postgresql-8.2-507.jdbc3.jar;..\lib\commons-logging-1.1.1.jar;
java %JAVA_ARGS% pco.schneider.main.SpotMsttSynchro extract spot ..\conf\

pause