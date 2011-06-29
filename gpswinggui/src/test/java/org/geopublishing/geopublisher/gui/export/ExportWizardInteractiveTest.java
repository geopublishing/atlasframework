package org.geopublishing.geopublisher.gui.export;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import de.schmitzm.swing.SwingUtil;
import de.schmitzm.testing.TestingClass;

public class ExportWizardInteractiveTest extends TestingClass {

	AtlasConfigEditable ace;
	@Before
	public void setUp() throws Exception {
		ace = TestAtlas.small.getAce();
	}

	@After
	public void tearDown() throws Exception {
		ace.deleteAtlas();
	}

	@Test
	public void testExportWizard() throws AtlasException, FactoryException,
			TransformException, SAXException, IOException,
			ParserConfigurationException {
		if (!isInteractive()) {
			// Stupidly "isInteractive" means "hasGui".
			return;
		}

		SwingUtil.invokeAndWait(new Runnable() {

			@Override
			public void run() {
				GeopublisherGUI geopublisherGUI = new GeopublisherGUI(
						ace);
				new ExportWizard().showWizard(geopublisherGUI.getJFrame(),
						geopublisherGUI.getAce());
			}
		});

	}

}
