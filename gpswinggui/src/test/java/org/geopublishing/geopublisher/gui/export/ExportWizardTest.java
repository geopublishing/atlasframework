package org.geopublishing.geopublisher.gui.export;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.geopublishing.atlasViewer.exceptions.AtlasException;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import de.schmitzm.swing.SwingUtil;
import de.schmitzm.testing.TestingClass;

public class ExportWizardTest extends TestingClass {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * DO NOt COMMIT UNIGNORED => WILL HANG HUDSON
	 */
	@Test
	@Ignore
	public void testExportWizard() throws AtlasException, FactoryException,
			TransformException, SAXException, IOException,
			ParserConfigurationException {
		if (!isInteractive()) {
			// Stupidly "isInteractive" means "hasGui". It would hang hudson
			// now, because hudson now has x11.
			return;
		}

		SwingUtil.invokeAndWait(new Runnable() {

			@Override
			public void run() {
				GeopublisherGUI geopublisherGUI = new GeopublisherGUI(
						TestAtlas.small.getAce());
				new ExportWizard().showWizard(geopublisherGUI.getJFrame(),
						geopublisherGUI.getAce());
			}
		});

	}

}
