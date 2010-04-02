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
package org.geopublishing.geopublisher.gui.datapool;

import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DataPool;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.AtlasCreator;
import org.geopublishing.geopublisher.dp.ImportToDataPoolDropTargetListener;
import org.geopublishing.geopublisher.gui.importwizard.ImportWizard;

import schmitzm.swing.JPanel;
import skrueger.swing.SmallButton;

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
	public EditDataPoolPanel(final AtlasConfigEditable ace) {
		super(new MigLayout("wrap 1", "[grow]", "[grow][shrink]"));
		this.ace = ace;
		final JScrollPane scrollDatapoolTable = new JScrollPane(
				getDatapoolJTable());

		add(scrollDatapoolTable, "grow 2000");

		add(new JLabel(AtlasCreator.R("EditDataPoolPanel.Explanation")),
				"shrinky, split 2");

		add(new SmallButton(new AbstractAction(AtlasCreator
				.R("EditDataPoolPanel.ImportButton")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				ImportWizard.showWizard(EditDataPoolPanel.this, ace);
			}
		}));

		// ****************************************************************************
		// D'n'D stuff
		// ****************************************************************************
		// Make the DatapoolJList accept Drops form the file system to import...
		final ImportToDataPoolDropTargetListener importByDropTargetListener = new ImportToDataPoolDropTargetListener(
				ace);

		@SuppressWarnings("unused")
		// is needed for Drag ('n'Drop)
		DropTarget dt = new DropTarget(scrollDatapoolTable,
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
