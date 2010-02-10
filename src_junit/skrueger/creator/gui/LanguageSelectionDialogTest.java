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

import java.util.ArrayList;

import junit.framework.TestCase;

import org.junit.Test;

import skrueger.creator.TestingUtil;

public class LanguageSelectionDialogTest extends TestCase {

	@Test
	public void testLangSelection() {
		if (!TestingUtil.INTERACTIVE)
			return;

		ArrayList<String> langs = new ArrayList<String>();
		langs.add("de");
		langs.add("ti");
		LanguageSelectionDialog dialog = new LanguageSelectionDialog(null,
				langs);
		dialog.setVisible(true);
	}

}
