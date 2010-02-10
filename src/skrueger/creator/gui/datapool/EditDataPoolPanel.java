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
