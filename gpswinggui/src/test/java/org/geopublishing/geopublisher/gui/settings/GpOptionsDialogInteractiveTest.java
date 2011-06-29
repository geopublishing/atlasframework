package org.geopublishing.geopublisher.gui.settings;

import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schmitzm.swing.SwingUtil;
import de.schmitzm.testing.TestingClass;

public class GpOptionsDialogInteractiveTest extends TestingClass {
private AtlasConfigEditable ace;
	@Before
	public void setUp() throws Exception {
		ace = TestAtlas.small.getAce();
	}

	@After
	public void tearDown() throws Exception {
		ace.deleteAtlas();
	}

	@Test
	public void testInitGUI() {
		SwingUtil.invokeAndWait(new Runnable() {

			@Override
			public void run() {
				GeopublisherGUI gpg = new GeopublisherGUI(ace);
				GpOptionsDialog gpOptionsDialog = new GpOptionsDialog(gpg
						.getJFrame(), gpg);
			}
		});
	}

}
