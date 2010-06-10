package org.geopublishing.geopublisher.gui;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GPTestingUtil;
import org.geopublishing.geopublisher.GPTestingUtil.Atlas;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;


public class ManageFontsDialogTest {
	
	@Test
	public void testJustOpenGUI() throws AtlasException, FactoryException, TransformException, SAXException, IOException, ParserConfigurationException {
		if (!GPTestingUtil.INTERACTIVE) return;
		
		AtlasConfigEditable ace = GPTestingUtil.getAtlasConfigE(Atlas.small);
		ManageFontsDialog manageFontsDialog = new ManageFontsDialog(null, ace);
	}

}
