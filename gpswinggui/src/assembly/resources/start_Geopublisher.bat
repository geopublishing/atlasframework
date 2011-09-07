@echo off
setlocal

echo Copyright 2011 Stefan Alfons Tzeggai
rem atlas-framework - This file is part of the Atlas Framework
rem This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
rem This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
rem  You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA

echo "Starting Geopublisher..." 

set HOMEPATH_COMPL=%HOMEDRIVE%%HOMEPATH%
set SWTJAR=swt-windows-${swt.version}-x86.jar
set JAI=jai_codec-windows-1.1.3.jar;jai_core-windows-1.1.3.jar;mlibwrapper_jai-windows-1.1.3.jar

md "%HOMEPATH_COMPL%\.Geopublishing"
java -version 2>"%HOMEPATH_COMPL%\.Geopublishing\javaversion.txt"

FINDSTR "64" "%HOMEPATH_COMPL%\.Geopublishing\javaversion.txt"
if %ERRORLEVEL% EQU 0 set SWTJAR=swt-windows-${swt.version}-x86_64.jar
echo.Using native SWT library %SWTJAR%

javaw -Xmx500m -Dfile.encoding=UTF8  -Djava.library.path=. -cp "%JAI%;%SWTJAR%;%HOMEPATH%\.Geopublishing;gpswinggui-${project.version}.jar" org.geopublishing.geopublisher.swing.GeopublisherGUI %CD% %~f1 %~f2 %~f3 %~f4 %~f5 %~f6 %~f7 %~f8 %~f9 %~f10 %~f11 %~f12 %~f13

endlocal

