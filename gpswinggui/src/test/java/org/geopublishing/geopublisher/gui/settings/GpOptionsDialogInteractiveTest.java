package org.geopublishing.geopublisher.gui.settings;

import org.geopublishing.geopublisher.GpTestingUtil.TestAtlas;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schmitzm.swing.SwingUtil;
import de.schmitzm.testing.TestingClass;

public class GpOptionsDialogInteractiveTest extends TestingClass {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testInitGUI() {
		SwingUtil.invokeAndWait(new Runnable() {

			@Override
			public void run() {
				GeopublisherGUI gpg = new GeopublisherGUI(TestAtlas.small
						.getAce());
				GpOptionsDialog gpOptionsDialog = new GpOptionsDialog(gpg
						.getJFrame(), gpg);
			}
		});
	}

}
