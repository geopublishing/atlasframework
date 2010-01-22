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

import java.awt.BorderLayout;
import java.awt.dnd.DropTarget;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import skrueger.atlas.dp.DataPool;
import skrueger.atlas.dp.DpEntry;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.dp.ImportToDataPoolDropTargetListener;

/**
 * This panel allows to manage the {@link DataPool}.
 * 
 * <li>New Items can be imported by D'n'D from the host system. Actions on the
 * data-pool entries can be performed via the right-mouse menu.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Kr&uuml;ger</a>
 * 
 */
public class EditDataPoolPanel extends JPanel {
	static private final Logger LOGGER = Logger
			.getLogger(EditDataPoolPanel.class);

	/**
	 * The Table that shows all {@link DpEntry}s
	 */
	private DraggableDatapoolJTable datapoolJTable;

	private final AtlasConfigEditable ace;

	/**
	 * This panel allows to edit the {@link DataPool}. <li>New Items can be
	 * imported by D'n'D from the host system. <li>Delete, <li>Edit preferences,
	 * <li>edit colormaps for rasters <li>edit legend for raster
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons
	 *         Kr&uuml;ger</a>
	 * 
	 */
	public EditDataPoolPanel(AtlasConfigEditable ace) {
		super(new BorderLayout());
		this.ace = ace;
		JScrollPane dataPoolTableJScrollPane = new JScrollPane(
				getDatapoolJTable());
		add(dataPoolTableJScrollPane, BorderLayout.CENTER);
		add(new JLabel(AtlasCreator.R("EditDataPoolPanel.Explanation")),
				BorderLayout.SOUTH); 
		// add(getEditDatapoolButtonsJPanel(), BorderLayout.SOUTH);

		// ****************************************************************************
		// D'n'D stuff
		// ****************************************************************************
		// Make the DatapoolJList accept Drops form the filesystem to import...
		final ImportToDataPoolDropTargetListener importByDropTargetListener = new ImportToDataPoolDropTargetListener(
				ace);

		@SuppressWarnings("unused")
		// is needed for Drag ('n'Drop)
		DropTarget dt = new DropTarget(dataPoolTableJScrollPane,
				importByDropTargetListener);
	}

	/**
	 * @return and caches the {@link DraggableDatapoolJTable} that represents
	 *         the {@link DataPool}
	 */
	public DataPoolJTable getDatapoolJTable() {
		if (datapoolJTable == null) {
			datapoolJTable = new DraggableDatapoolJTable(ace);
		}
		return datapoolJTable;
	}

	public void setDatapoolJTable(DraggableDatapoolJTable datapoolJTable) {
		this.datapoolJTable = datapoolJTable;
	}

}
