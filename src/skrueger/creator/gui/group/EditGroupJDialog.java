/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator.gui.group;

import java.awt.Component;

import javax.swing.JDialog;

import org.apache.log4j.Logger;

import skrueger.atlas.dp.Group;
import skrueger.creator.AtlasCreator;
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
