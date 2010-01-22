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

import skrueger.atlas.AVUtil;
import skrueger.atlas.dp.DataPool;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.Group;
import skrueger.atlas.gui.plaf.BasicMapLayerLegendPaneUI;
import skrueger.atlas.map.MapPool;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GPDialogManager;
/**
 * Delete a {@link DpEntry} from the {@link DataPool} and also remove all
 * references to it from the {@link MapPool} or the {@link Group}s
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
 * Kr&uuml;ger</a>
 **/
public class DataPoolDeleteAction extends AbstractAction {

	static final Logger LOGGER = Logger.getLogger(DataPoolDeleteAction.class);

	private final Component owner;

	private final DataPoolJTable dpTable;

	public DataPoolDeleteAction(DataPoolJTable dpTable, Component owner) {
		super(AtlasCreator.R("DataPoolWindow_Action_DeleteDPE_label"),
				BasicMapLayerLegendPaneUI.ICON_REMOVE);

		this.dpTable = dpTable;
		this.owner = owner;
	}

	@Override
	/**
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
		DataPool dataPool = dpTable.getDataPool();
		int modelIndex = dpTable.convertRowIndexToModel(dpTable
				.getSelectedRow());
		DpEntry dpe = dataPool.get(modelIndex);

		if (AVUtil.askYesNo(owner, AtlasCreator.R(
				"Action_DeleteDPE_reallyDeleteQuestion", dpe.getTitle()
						.toString(), dpe.getFilename())) == false)
			return;

		// TODO Only close the windows that reference this dplayer!
		if (!GPDialogManager.dm_MapComposer.closeAllInstances())
			return;

		AtlasConfigEditable.deleteDpEntry(owner, (AtlasConfigEditable) dpe
				.getAc(), dpe, true);

	}
}
