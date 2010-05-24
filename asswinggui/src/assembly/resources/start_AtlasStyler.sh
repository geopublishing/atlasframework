#!/bin/bash
# Copyright 2009 Stefan Alfons Krueger
# atlas-framework - This file is part of the Atlas Framewo
# This library is free software; you can redistribute it and/or modify it under the terms of the GNU  Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the  License, or (at your option) any later version.
# This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser  General Public License for more details.
#  You should have received a copy of the GNU Lesser General Public License along with this library;  if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
#  Diese Bibliothek ist freie Software; Sie dürfen sie unter den Bedingungen der GNU Lesser General  Public License, wie von der Free Software Foundation ver�ffentlicht, weiterverteilen und/oder  modifizieren; entweder gemäß Version 2.1 der Lizenz oder (nach Ihrer Option) jeder späteren Version.
#  Diese Bibliothek wird in der Hoffnung weiterverbreitet, das sie nützlich sein wird, jedoch OHNE  IRGENDEINE GARANTIE, auch ohne die implizierte Garantie der MARKTREIFE oder der VERWENDBARKEIT FÜR EINEN  BESTIMMTEN ZWECK. Mehr Details finden Sie in der GNU Lesser General Public License.
#  Sie sollten eine Kopie der GNU Lesser General Public License zusammen mit dieser Bibliothek  erhalten haben; falls nicht, schreiben Sie an die Free Software Foundation, Inc., 51 Franklin St, Fifth  Floor, Boston, MA 02110, USA.

echo "Starting AtlasStyler. You need to have at least Java Runtime Environment (JRE) Version 6 installed."

java -Xmx160m -Dfile.encoding=UTF8 -Djava.library.path=. -jar asswinggui-${project.version}.jar
