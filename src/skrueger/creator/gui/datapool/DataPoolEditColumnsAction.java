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

import skrueger.atlas.AVUtil;
import skrueger.atlas.dp.DataPool;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.layer.DpLayerVector;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.creator.AtlasCreator;
import skrueger.creator.EditAttributesJDialog;
import skrueger.creator.GPDialogManager;

public class DataPoolEditColumnsAction extends AbstractAction {

	private final Component owner;
	private final DataPoolJTable dpTable;

	// static protected HashMap<String, JDialog> editAttribsJDialogs = new
	// HashMap<String, JDialog>();

	public DataPoolEditColumnsAction(DataPoolJTable dpTable, Component owner) {

		super(AtlasCreator.R("DataPoolWindow_Action_EditColumns_label"));

		this.dpTable = dpTable;
		this.owner = owner;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// Determine which DPEntry is selected
		if (dpTable.getSelectedRow() == -1)
			return;
		DataPool dataPool = dpTable.getDataPool();
		DpEntry dpe = dataPool.get(dpTable.convertRowIndexToModel(dpTable
				.getSelectedRow()));
		if (!(dpe instanceof DpLayerVector)) {
			AVUtil.showMessageDialog(owner,
					"You can only edit the columns of vector data"); // i8n
			return;
		}

		final DpLayerVectorFeatureSource dplv = (DpLayerVectorFeatureSource) dpe;

		EditAttributesJDialog d = GPDialogManager.dm_EditAttribute
				.getInstanceFor(dplv, owner, dplv);

	}

}
