set JAVA_HOME=C:\Sun\SDK\jdk
set EGEN_HOME=C:\Pimmy\Sandbox\EventGenerator\BUILD

set EGEN_LIBS=%EGEN_HOME%\libs

%JAVA_HOME%\bin\java -DEGEN_HOME="%EGEN_HOME%" -Djava.library.path="%EGEN_LIBS%" -cp "%EGEN_LIBS%\sybjdbc.jar;%EGEN_LIBS%\Configurator.jar" gui.Configurator