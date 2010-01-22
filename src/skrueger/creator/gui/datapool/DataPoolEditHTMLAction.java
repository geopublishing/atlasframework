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

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import schmitzm.jfree.chart.style.ChartStyle;
import skrueger.atlas.dp.DataPool;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.layer.DpLayer;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.gui.SimplyHTMLUtil;
import skrueger.i8n.I8NUtil;

public class DataPoolEditHTMLAction extends AbstractAction {

	static final Logger LOGGER = Logger.getLogger(DataPoolDeleteAction.class);

	private final DataPoolJTable dpTable;

	public DataPoolEditHTMLAction(DataPoolJTable dpTable) {
		super(AtlasCreator.R("DataPoolWindow_Action_EditDPEHTML_label"));

		this.dpTable = dpTable;
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
		DataPool dataPool = dpTable.getDataPool();
		DpEntry dpe = dataPool.get(dpTable.convertRowIndexToModel(dpTable
				.getSelectedRow()));

		if (!(dpe instanceof DpLayer))
			return;

		DpLayer<?, ChartStyle> dpl = (DpLayer<?, ChartStyle>) dpe;
		java.util.List<File> infoFiles = dpTable.getAce().getHTMLFilesFor(dpl);

		java.util.List<String> tabTitles = new ArrayList<String>();
		AtlasConfigEditable ace = dpTable.getAce();
		for (String l : ace.getLanguages()) {
			tabTitles.add(AtlasCreator.R("DPLayer.HTMLInfo.LanguageTabTitle",
					I8NUtil.getLocaleFor(l).getDisplayLanguage()));
		}

		SimplyHTMLUtil.openHTMLEditors(dpTable, ace, infoFiles, tabTitles,
				AtlasCreator.R("EditLayerHTML.Dialog.Title", dpl.getTitle()
						.toString()));

		/**
		 * Try to reset a few cached values for the TODO nicer! // Expect that
		 * the number of HTML files, and with it the QM have changed. // TODO
		 * replace this events?! Should uncache fire the events?
		 */
		dpl.uncache();
		((AbstractTableModel) dpTable.getModel()).fireTableDataChanged();
		dpTable.setTableCellRenderers();

	}

}
