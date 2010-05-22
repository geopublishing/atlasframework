/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.swing;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 * A util class with typical methods for converning the Atlas layout
 * 
 * @author Stefan Alfons Tzeggai
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class LayoutUtil {
	final static Border border1 = BorderFactory
			.createBevelBorder(javax.swing.border.BevelBorder.RAISED);

	public static void border1(JComponent component) {
		component.setBorder(border1);
	}

	public static void borderTitle(JComponent component, String title) {
		component.setBorder(BorderFactory.createTitledBorder(title));
	}
}
