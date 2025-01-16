set EGEN_PATH=C:\Pimmy\Sandbox\EventGenerator
set LIBPATH=%EGEN_PATH%\src\jre\lib\comm.jar
set JAVA_HOME="C:\Sun\SDK\jdk"

cd %EGEN_PATH%

IF EXIST BUILD (
	rm -rf BUILD
)
mkdir BUILD
cd BUILD
mkdir log
mkdir props
mkdir libs

cd ..
cp -r images BUILD\

cd Stuff
cp License.rtf ..\BUILD\
cp License.pdf ..\BUILD\
cp UsersGuide.pdf ..\BUILD\
cd ..

rm -rf classes
mkdir classes
cd src

cp "EventGenerator.bat" %EGEN_PATH%\BUILD\
cp "testEventGenerator.bat" %EGEN_PATH%\BUILD\

%JAVA_HOME%\bin\javac -O -g:none -Xlint:unchecked -d ..\classes -classpath %EGEN_PATH% %EGEN_PATH%\src\generator\*.java %EGEN_PATH%\src\gui\*.java %EGEN_PATH%\src\log\*.java %EGEN_PATH%\src\log\exception\*.java %EGEN_PATH%\src\utils\*.java

cd gui
cp generator.prp ..\..\BUILD\props\
cp usersets.prp ..\..\BUILD\props\


cd ..\utils
cp template.xml ..\..\BUILD\props\
cp WUQ.xml ..\..\BUILD\props\

cd ..\..\classes
%JAVA_HOME%\bin\jar cvf ..\BUILD\libs\Configurator.jar *

cd ..\libs
cp * ..\BUILD\libs\

cd ..\JavaLauncher\Debug
cp "EventGenerator.exe" ..\..\BUILD\

cd %EGEN_PATH%
chmod -R 777 BUILD
cd %EGEN_PATH%\src

PAUSE