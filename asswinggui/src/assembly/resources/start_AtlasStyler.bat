@echo off
echo Copyright 2008 Stefan Alfons Krueger
rem atlas-framework - This file is part of the Atlas Framewo
rem This library is free software; you can redistribute it and/or modify it under the terms of the GNU  Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the  License, or (at your option) any later version.
rem This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser  General Public License for more details.
rem  You should have received a copy of the GNU Lesser General Public License along with this library;  if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
rem  Diese Bibliothek ist freie Software; Sie d�rfen sie unter den Bedingungen der GNU Lesser General  Public License, wie von der Free Software Foundation ver�ffentlicht, weiterverteilen und/oder  modifizieren; entweder gem�� Version 2.1 der Lizenz oder (nach Ihrer Option) jeder sp�teren Version.
rem  Diese Bibliothek wird in der Hoffnung weiterverbreitet, da� sie n�tzlich sein wird, jedoch OHNE  IRGENDEINE GARANTIE, auch ohne die implizierte Garantie der MARKTREIFE oder der VERWENDBARKEIT F�R EINEN  BESTIMMTEN ZWECK. Mehr Details finden Sie in der GNU Lesser General Public License.
rem  Sie sollten eine Kopie der GNU Lesser General Public License zusammen mit dieser Bibliothek  erhalten haben; falls nicht, schreiben Sie an die Free Software Foundation, Inc., 51 Franklin St, Fifth  Floor, Boston, MA 02110, USA.

echo Starting AtlasStyler. You need to have at least Java Runtime Environment (JRE) Version 6 installed.

javaw -Xmx160m -Dfile.encoding=UTF-8 -Djava.library.path=. -cp %HOMEPATH%\.Geopublishing:asswinggui-${project.version}.jar org.geopublishing.atlasStyler.swing.AtlasStylerGUI:asswinggui-${project.version}.jar org.geopublishing.atlasStyler.swing.AtlasStylerGUI -jar asswinggui-${project.version}.jar

