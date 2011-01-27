/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher.gui.datapool;

import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.dp.DataPool;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.dp.ImportToDataPoolDropTargetListener;
import org.geopublishing.geopublisher.gui.importwizard.ImportWizard;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geopublishing.geopublisher.swing.GpSwingUtil;

import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SmallButton;
import de.schmitzm.swing.event.FilterTableKeyListener;

/**
 * This panel allows to manage the {@link DataPool}.
 * 
 * <li>New Items can be imported by D'n'D from the host system. Actions on the
 * data-pool entries can be performed via the right-mouse menu.
 * 
 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
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

	/** As the suer types into this filter, the list is filtered **/
	private JTextField filterTextField;

	/**
	 * This panel allows to edit the {@link DataPool}.
	 * <ul>
	 * <li>New Items can be imported by D'n'D from the host system (or using the
	 * import wizard),
	 * <li>Delete,
	 * <li>Edit preferences,
	 * <li>edit colormaps for rasters
	 * <li>edit legend for raster
	 * </ul>
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 * 
	 */
	public EditDataPoolPanel(final AtlasConfigEditable ace) {
		super(new MigLayout("wrap 1", "[grow]", "[shrink][grow][shrink]"));
		this.ace = ace;

		// A row to enter a filter:
		JLabel filterLabel = new JLabel("Filter:");
		add(filterLabel, "split 2, top, gap 0");
		filterLabel.setToolTipText(GpSwingUtil
				.R("DataPoolWindow.FilterTable.TT"));
		add(getFilterTextField(), "growx, top, gap 0");

		// The table
		final JScrollPane scrollDatapoolTable = new JScrollPane(
				getDatapoolJTable());
		add(scrollDatapoolTable, "grow 2000");

		add(new JLabel(GeopublisherGUI.R("EditDataPoolPanel.Explanation")),
				"shrinky, split 2");

		// A button to start the import-wizard
		add(new SmallButton(new AbstractAction(
				GeopublisherGUI.R("EditDataPoolPanel.ImportButton"),
				Icons.ICON_ADD_SMALL) {

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

			// KeyListener reacts on every key
			/*
			 * DataPoolJTable.ColumnName.Quality=Quality
			 * DataPoolJTable.ColumnName.Type=Type
			 * DataPoolJTable.ColumnName.TitleLang=Title (${0})
			 * DataPoolJTable.ColumnName.ViewsLang=Views (${0})
			 * DataPoolJTable.ColumnName.Filename=Filename
			 * sizeOnFilesystemWithoutSVN=Size (MB)
			 */

			// The constructor adds itself to the textfield
			new FilterTableKeyListener(datapoolJTable, getFilterTextField(), 1,
					2, 3, 4, 5);
		}
		return datapoolJTable;
	}

	public void setDatapoolJTable(DraggableDatapoolJTable datapoolJTable) {
		this.datapoolJTable = datapoolJTable;
	}

	public JTextField getFilterTextField() {
		if (filterTextField == null) {
			filterTextField = new JTextField();
			filterTextField.setToolTipText(GpSwingUtil
					.R("DataPoolWindow.FilterTable.TT"));
		}
		return filterTextField;
	}

}
