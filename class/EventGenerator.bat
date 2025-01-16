@ECHO OFF
REM ######################################################################
REM #
REM # IBM Tivoli Event Generator
REM #
REM # IBM Confidential
REM # OCO Source Materials
REM #
REM # 5724-S45
REM #
REM # (C) Copyright IBM Corp. 1995, 2008
REM #
REM # The source code for this program is not published or otherwise
REM # divested of its trade secrets, irrespective of what has
REM # been deposited with the U.S. Copyright Office.
REM #
REM ######################################################################
set EGEN_HOME=.
set EGEN_LIBS=%EGEN_HOME%\libs

java -splash:"%EGEN_HOME%\images\splash.jpg" -DEGEN_HOME="%EGEN_HOME%" -Djava.library.path="%EGEN_LIBS%" -cp "%EGEN_LIBS%\sybjdbc.jar;%EGEN_LIBS%\Configurator.jar" gui.Configurator