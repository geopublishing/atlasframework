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
package org.geopublishing.geopublisher.gui.group;

import java.awt.Component;

import javax.swing.JDialog;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.Group;
import org.geopublishing.geopublisher.AtlasCreator;

import skrueger.swing.TranslationAskJDialog;
import skrueger.swing.TranslationEditJPanel;

/**
 * Modal {@link JDialog} that allows to edit the internationalized Title and
 * Description and Keywords of a {@link Group}.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 */
public class EditGroupJDialog extends TranslationAskJDialog {
	Logger log = Logger.getLogger(EditGroupJDialog.class);

	/**
	 * Modal {@link JDialog} that allows to edit the internationalized Title and
	 * Description and Keywords of a {@link Group}.
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 */
	public EditGroupJDialog(Component owner, Group group) {
		super(owner);

		TranslationEditJPanel a = new TranslationEditJPanel(AtlasCreator
				.R("GroupTree.Edit.TranslateTitle"), group.getTitle(), group
				.getAc().getLanguages());
		TranslationEditJPanel b = new TranslationEditJPanel(AtlasCreator
				.R("GroupTree.Edit.TranslateDescription"), group.getDesc(),
				group.getAc().getLanguages());
		TranslationEditJPanel c = new TranslationEditJPanel(AtlasCreator
				.R("GroupTree.Edit.TranslateKeywords"), group.getKeywords(),
				group.getAc().getLanguages());

		setComponents(a, b, c);

	}

}
