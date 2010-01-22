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
package skrueger.creator.gui.datapool;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.media.DpMedia;
import skrueger.creator.AtlasCreator;

public class DataPoolPreviewAction extends AbstractAction {
	static final Logger LOGGER = Logger.getLogger(DataPoolPreviewAction.class);

	private final Component owner;

	private final DataPoolJTable dpTable;

	public DataPoolPreviewAction(DataPoolJTable dpTable, Component owner) {
		super(AtlasCreator.R("DataPoolWindow_Action_ShowDPM_label"));

		this.dpTable = dpTable;
		this.owner = owner;
	}

	@Override
	/*
	 * Delete a {@link DpEntry} from the {@link DataPool} and also remove all
	 * references to it from the {@link MapPool} or the {@link Group}s
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 * Kr&uuml;ger</a>
	 */
	public void actionPerformed(ActionEvent e) {

		// Determine which DPEntry is selected
		if (dpTable.getSelectedRow() == -1)
			return;

		DpEntry dpe = dpTable.getDataPool().get(
				dpTable.convertRowIndexToModel(dpTable.getSelectedRow()));

		if (dpe instanceof DpMedia) {
			DpMedia dpm = (DpMedia) dpe;
			
			dpm.show(owner);
		}
	}

}
