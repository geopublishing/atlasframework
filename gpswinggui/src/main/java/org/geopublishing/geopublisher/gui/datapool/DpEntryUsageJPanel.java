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

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.AVUtil;
import org.geopublishing.atlasViewer.dp.DataPool;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.GpUtil;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;
import org.geopublishing.geopublisher.swing.GpSwingUtil;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.swing.Cancellable;
import skrueger.swing.SmallButton;

import com.vividsolutions.jts.geom.Envelope;

public class DpEntryUsageJPanel extends JPanel implements Cancellable {

	private final AtlasConfigEditable ace;
	private final DpEntry<? extends ChartStyle> dpe;

	public DpEntryUsageJPanel(final DpEntry<? extends ChartStyle> dpe) {

		super(new MigLayout("width 100%, wrap 1", "[grow]"));
		this.dpe = dpe;

		ace = (AtlasConfigEditable) dpe.getAtlasConfig();

		backup();

		// ****************************************************************************
		// A bordered panel for system related to the local storage/ filesystem
		// ****************************************************************************
//		{
			// final JPanel fileSystem = new JPanel(new MigLayout(
			// "width 100%, wrap 2", "[grow]"));
			// fileSystem.setBorder(BorderFactory
			// .createTitledBorder(R("EditDpEntryGUI.filesystem.border")));
			//
			// fileSystem.add(new JLabel(R(
			// "EditDpEntryGUI.filesystem.explanation", dpe.getType()
			// .getLine1())), "span 2, right, width 100%, growx");
			// fileSystem.add(new JLabel(R("path")), "right");
			// final File dataDir = new File(ace.getDataDir(), dpe
			// .getDataDirname());
			//
			// final JTextField folderTextField = new JTextField(dataDir
			// .getAbsolutePath());
			// folderTextField.setEditable(false);
			// fileSystem.add(folderTextField, "right, growx");
			//
			// // A KVP for the folder size
			// final JLabel sizeLabel = new
			// JLabel(R("sizeOnFilesystemWithoutSVN")
			// + ":");
			// sizeLabel.setToolTipText(R("sizeOnFilesystemWithoutSVN.TT"));
			// fileSystem.add(sizeLabel, "split 2, right");
			// final JLabel sizeValueLabel = new
			// JLabel(GpUtil.MbDecimalFormatter
			// .format(ace.getFolderSize(dpe)));
			// sizeValueLabel.setToolTipText(R("sizeOnFilesystemWithoutSVN.TT"));
			// fileSystem.add(sizeValueLabel, "left");
			//
			// // A button to open the containing directory
			// final JButton openDirJButton = new SmallButton(new
			// AbstractAction(
			// R("EditDPEDialog.OpenFolderButton") ) {
			//
			// public void actionPerformed(final ActionEvent e) {
			// SwingUtil.openOSFolder(new File(((AtlasConfigEditable) dpe
			// .getAtlasConfig()).getDataDir(), dpe.getDataDirname()));
			// }
			//
			// }, R("EditDPEDialog.OpenFolderButton.TT"));
			// fileSystem.add(openDirJButton, "split 2, right");
			//
			// // A button to open the containing directory
			// final JButton uncacheJButton = new SmallButton(new
			// AbstractAction(
			// R("EditDPEDialog.uncacheDpeButton"), new
			// ImageIcon(GpSwingUtil.class
			// .getResource("/icons/uncache.png")) ) {
			//
			// public void actionPerformed(final ActionEvent e) {
			// ace.uncacheAndReread(dpe);
			// ace.getDataPool().fireChangeEvents(DataPool.EventTypes.changeDpe);
			// sizeValueLabel.setText(GpUtil.MbDecimalFormatter
			// .format(ace.getFolderSize(dpe)));
			// }
			//
			// }, R("EditDPEDialog.uncacheDpeButton.TT"));
			// fileSystem.add(uncacheJButton, "right");
			//
			// add(fileSystem, "growx");
			// }

//		}

	}

	protected String R(String string, Object... obj) {
		return GeopublisherGUI.R(string, obj);
	}

	boolean backupExportable = false;

	private void backup() {
		backupExportable = dpe.isExportable();

	}

	@Override
	public void cancel() {
		dpe.setExportable(backupExportable);

	}

}
