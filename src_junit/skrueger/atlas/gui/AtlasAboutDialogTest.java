/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
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
