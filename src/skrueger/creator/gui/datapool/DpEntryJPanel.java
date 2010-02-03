package skrueger.creator.gui.datapool;

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

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import schmitzm.jfree.chart.style.ChartStyle;
import schmitzm.swing.JPanel;
import skrueger.atlas.AVUtil;
import skrueger.atlas.dp.DataPool;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.layer.DpLayer;
import skrueger.creator.AtlasConfigEditable;
import skrueger.creator.AtlasCreator;
import skrueger.creator.GPProps;
import skrueger.creator.GpUtil;
import skrueger.swing.Cancellable;
import skrueger.swing.SmallButton;

import com.vividsolutions.jts.geom.Envelope;

public class DpEntryJPanel extends JPanel implements Cancellable {

	private final AtlasConfigEditable ace;
	private final DpEntry<? extends ChartStyle> dpe;

	public DpEntryJPanel(final DpEntry<? extends ChartStyle> dpe) {

		super(new MigLayout("width 100%, wrap 1", "[grow]"));
		this.dpe = dpe;

		ace = (AtlasConfigEditable) dpe.getAc();

		backup();

		// ****************************************************************************
		// A bordered panel for system related to the local storage/ filesystem
		// ****************************************************************************
		{
			final JPanel fileSystem = new JPanel(new MigLayout(
					"width 100%, wrap 2", "[grow]"));
			fileSystem.setBorder(BorderFactory
					.createTitledBorder(R("EditDpEntryGUI.filesystem.border")));

			fileSystem.add(new JLabel(R(
					"EditDpEntryGUI.filesystem.explanation", dpe.getType()
							.getLine1())), "span 2, right, width 100%, growx");
			fileSystem.add(new JLabel(R("path")), "right");
			final File dataDir = new File(ace.getDataDir(), dpe
					.getDataDirname());

			final JTextField folderTextField = new JTextField(dataDir
					.getAbsolutePath());
			folderTextField.setEditable(false);
			fileSystem.add(folderTextField, "right, growx");

			// A KVP for the folder size
			final JLabel sizeLabel = new JLabel(R("sizeOnFilesystemWithoutSVN")
					+ ":");
			sizeLabel.setToolTipText(R("sizeOnFilesystemWithoutSVN.TT"));
			fileSystem.add(sizeLabel, "split 2, right");
			final JLabel sizeValueLabel = new JLabel(GpUtil.MbDecimalFormatter
					.format(ace.getFolderSize(dpe)));
			sizeValueLabel.setToolTipText(R("sizeOnFilesystemWithoutSVN.TT"));
			fileSystem.add(sizeValueLabel, "left");

			// A button to open the containing directory
			final JButton openDirJButton = new SmallButton(new AbstractAction(
					R("EditDPEDialog.OpenFolderButton") ) {

				public void actionPerformed(final ActionEvent e) {
					AVUtil.openOSFolder(new File(((AtlasConfigEditable) dpe
							.getAc()).getDataDir(), dpe.getDataDirname()));
				}

			}, R("EditDPEDialog.OpenFolderButton.TT"));
			fileSystem.add(openDirJButton, "split 2, right");
			
			// A button to open the containing directory
			final JButton uncacheJButton = new SmallButton(new AbstractAction(
					R("EditDPEDialog.uncacheDpeButton"), new ImageIcon(GPProps.class
							.getResource("resource/uncache.png")) ) {
				
				public void actionPerformed(final ActionEvent e) {
					ace.uncacheAndReread(dpe);
					ace.getDataPool().fireChangeEvents(DataPool.EventTypes.changeDpe);
					sizeValueLabel.setText(GpUtil.MbDecimalFormatter
							.format(ace.getFolderSize(dpe)));
				}
				
			}, R("EditDPEDialog.uncacheDpeButton.TT"));
			fileSystem.add(uncacheJButton, "right");

			add(fileSystem, "growx");
		}

		// ****************************************************************************
		// Information about coordinates (CRS, BBOX) it if is a layer
		// ****************************************************************************
		if (dpe instanceof DpLayer) {
			final DpLayer<?, ? extends ChartStyle> dpLayer = (DpLayer<?, ? extends ChartStyle>) dpe;

			final JPanel coorsPanel = new JPanel(new MigLayout("width 100%",
					"[grow]"));
			coorsPanel.setBorder(BorderFactory
					.createTitledBorder(R("EditDpEntryGUI.coords.border")));
			add(coorsPanel, "growx");

			// ****************************************************************************
			// CRS
			// ****************************************************************************
			{
				final JLabel crsLabel = new JLabel(R("CRS") + ": "
						+ dpLayer.getCRSString());
				crsLabel.setToolTipText(R("CRS.TT"));
				coorsPanel.add(crsLabel, "growx, wrap");
			}

			// ****************************************************************************
			// BBOX
			// ****************************************************************************
			{

				final JPanel bbodyPanel = new JPanel(new MigLayout("right",
						"[grow]:20:[grow]", "::[grow]"));
				bbodyPanel.setBorder(null);
				final Envelope env = dpLayer.getEnvelope();
				final CoordinateReferenceSystem crs = dpLayer.getCrs();

				if (env != null ){
					bbodyPanel.add(new JLabel("x1:"));
					bbodyPanel.add(new JLabel(AVUtil
							.formatCoord(crs, env.getMinX())));
					bbodyPanel.add(new JLabel("y1:"));
					bbodyPanel.add(new JLabel(AVUtil
							.formatCoord(crs, env.getMinY())), "wrap");
					
					bbodyPanel.add(new JLabel("x2:"));
					bbodyPanel.add(new JLabel(AVUtil
							.formatCoord(crs, env.getMaxX())));
					bbodyPanel.add(new JLabel("y2:"));
					bbodyPanel.add(new JLabel(AVUtil
							.formatCoord(crs, env.getMaxY())));
					
					final JLabel bblabel = new JLabel(R("BBOX") + ":");
					bblabel.setToolTipText(R("BBOX.TT"));
					coorsPanel.add(bblabel, "top, split 2");
					coorsPanel.add(bbodyPanel, "top, left, growx");
				}
			}
		}

		// ****************************************************************************
		// A checkbox to control the exporting-flag of this DpEntry
		// ****************************************************************************
		final JCheckBox exportableCbox = new JCheckBox(new AbstractAction(
				R("EditDPEDialog.IsDpeExportable")) {

			@Override
			public void actionPerformed(final ActionEvent e) {
				dpe.setExportable(!dpe.isExportable());
				((JCheckBox) e.getSource()).setSelected(dpe.isExportable());
			}

		});
		exportableCbox.setSelected(dpe.isExportable());
		exportableCbox.setToolTipText(R("EditDPEDialog.IsDpeExportable.TT"));

		add(exportableCbox);

		// ****************************************************************************
		// Internal folder name
		// ****************************************************************************
		final JLabel id = new JLabel("<html><font color='gray'>" + R("AtlasID")
				+ ": " + dpe.getId() + "</font></html>");
		add(id, "gap, bottom, growy");

	}

	protected String R(String string, Object... obj) {
		return AtlasCreator.R(string, obj);
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