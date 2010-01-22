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
import javax.swing.Action;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.gui.plaf.BasicMapLayerLegendPaneUI;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GPDialogManager;

/**
 * An {@link Action} that will open a dialog the configure DpEntry specific settings. 
 */
public class DataPoolEditAction extends AbstractAction {

	private Component owner;
	DpEntry<? extends ChartStyle> dpe;
	DataPoolJTable dpTable;

	public DataPoolEditAction(DataPoolJTable dpTable, Component owner) {
		super(AtlasCreator.R("DataPoolWindow_Action_EditDPE_label"),
				BasicMapLayerLegendPaneUI.ICON_TOOL);
		this.dpTable = dpTable;
		this.owner = owner;

	}

	public DataPoolEditAction(Component owner, DpEntry<? extends ChartStyle> dpe) {
		super(AtlasCreator.R("DataPoolWindow_Action_EditDPE_label"),
				BasicMapLayerLegendPaneUI.ICON_TOOL);
		this.owner = owner;
		this.dpe = dpe;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (dpe == null) {
			dpe = dpTable.getDataPool().get(
					dpTable.convertRowIndexToModel(dpTable.getSelectedRow()));
		}

		GPDialogManager.dm_EditDpEntry.getInstanceFor(dpe, owner, dpe);
	}

}
