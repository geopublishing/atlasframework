/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator.dp;

import java.awt.Component;
import java.io.File;
import java.net.URL;

import skrueger.atlas.AtlasConfig;
import skrueger.atlas.gui.internal.AtlasStatusDialog;
import skrueger.creator.AtlasConfigEditable;

/**
 * This interface guarantees that you can edit and save the {@link AtlasConfig}
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 */
public interface DpEditableInterface {

	/**
	 * Cast you {@link AtlasConfig} to {@link AtlasConfigEditable} if you are
	 * sure that you have got one ;-)
	 * 
	 * @return {@link AtlasConfigEditable}
	 */
	public AtlasConfigEditable getAce();
	
	
	public void copyFiles(URL sourceUrl, Component owner, File targetDir,
			AtlasStatusDialog atlasStatusDialog) throws Exception;

}