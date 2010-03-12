/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import skrueger.atlas.exceptions.AtlasException;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.TestingUtil;

public class GpJSplitPaneTest extends TestCase {

	@Test
	public void testTable() throws FactoryException, TransformException,
			SAXException, IOException, ParserConfigurationException,
			AtlasException, URISyntaxException {

		AtlasConfigEditable ace = TestingUtil.getAtlasConfigE();

		GpJSplitPane acePanel = new GpJSplitPane(ace);

		JDialog dialog = new JDialog((JFrame) null, true);
		dialog.setContentPane(acePanel);

		dialog.pack();

		acePanel.invalidate();

		dialog.setVisible(true);

	}

}
