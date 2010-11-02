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

import javax.swing.JLabel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.geopublisher.AtlasConfigEditable;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.JPanel;
import skrueger.swing.Cancellable;

public class DpEntryUsageJPanel extends JPanel implements Cancellable {

	private final AtlasConfigEditable ace;
	private final DpEntry<? extends ChartStyle> dpe;

	public DpEntryUsageJPanel(final DpEntry<? extends ChartStyle> dpe) {

		super(new MigLayout("width 100%, wrap 1", "[grow]"));
		this.dpe = dpe;

		ace = (AtlasConfigEditable) dpe.getAtlasConfig();

		backup();

		// ****************************************************************************
		// A bordered panel with a table listing the maps it is used in
		// ****************************************************************************
		// {
		// final JPanel mapUsage = new JPanel(new
		// MigLayout("width 100%, wrap 1",
		// "[grow]","[150]"));
		// mapUsage.setBorder(BorderFactory
		// .createTitledBorder(R("EditDpEntryGUI.usage.Maps.border")));

		add(new JLabel(R("EditDpEntryGUI.usage.Maps.border")));
		MapusageTable mapUt = new MapusageTable(dpe, ace.getMapPool());
		// mapUsage.
		add(new JScrollPane(mapUt), "height 150");
		// add(mapUsage);

		// ****************************************************************************
		// A bordered panel with a table listing the maps it is used in
		// ****************************************************************************
		// {
		// final JPanel menuUsage = new JPanel(new
		// MigLayout("width 100%, wrap 1",
		// "[grow]", "[150]"));
		// mapUsage.setBorder(BorderFactory
		// .createTitledBorder(R("EditDpEntryGUI.usage.Menu.border")));

		add(new JLabel(R("EditDpEntryGUI.usage.Menu.border")), "gapy unrel");

		MenuusageTable menuUt = new MenuusageTable(dpe, ace.getFirstGroup());
		add(new JScrollPane(menuUt), "height 150");

		if (menuUt.getModel().getRowCount() == 0
				&& mapUt.getModel().getRowCount() == 0) {
			add(new JLabel(R("EditDpEntryGUI.usage.notexported")), "gapy unrel");
		}
		add(new JLabel(R("EditDpEntryGUI.usage.exported")), "gapy unrel");

		// menuUsage.add(new JScrollPane(menuUt));
		// add(menuUsage);

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

		// }

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
