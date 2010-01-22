/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasViewer application - A GIS viewer application targeting at end-users with no GIS-experience. Its main purpose is to present the atlases created with the Geopublisher application.
 * http://www.geopublishing.org
 * 
 * AtlasViewer is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.atlas.gui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.junit.Test;
import org.xml.sax.SAXException;

import skrueger.atlas.AtlasConfigTest;
import skrueger.atlas.AtlasViewer;
import skrueger.atlas.dp.AMLImport;
import skrueger.atlas.exceptions.AtlasException;
import skrueger.atlas.http.Webserver;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.GPProps;

public class AtlasAboutDialogTest extends TestCase {
	@Test
	public void testAtlasAboutDialog() throws IOException, SAXException,
			ParserConfigurationException, AtlasException, URISyntaxException {
		String atlasDir = new File(AtlasConfigTest.class.getClassLoader()
				.getResource("data/testAtlas1/ad/atlas.xml").toURI())
				.getParentFile().getParent();
		AtlasViewer.setupResLoMan(new String[] { atlasDir });

		/***********************************************************************
		 * Remove the old geopublisher.properties file, so we always start with
		 * the default
		 */
		GPProps.resetProperties(null);

		AtlasConfigEditable atlasConfig = new AtlasConfigEditable();
		atlasConfig.setAtlasDir(new File(atlasDir));

		Webserver webserver = new Webserver(false);

		AMLImport.parseAtlasConfig(null, atlasConfig, false);

		if (atlasConfig.getAboutHTMLURL() != null) {

			AtlasAboutDialog atlasAboutDialog2 = new AtlasAboutDialog(null,
					true, atlasConfig);
			atlasAboutDialog2.setVisible(true);
		}

	}
}
