package org.geopublishing.atlasStyler.swing;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesList_DistinctValues;
import org.geotools.styling.ColorMap;

import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.swing.CancellableDialogAdapter;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SwingUtil;

/**
 * This dialog allows to paste the output of gdalinfo, like: <code>
  Band 1 Block=256x256 Type=Byte, ColorInterp=Palette
  Overviews: 2397x2103, 1199x1052, 600x526
  Color Table (RGB with 256 entries)
    0: 216,240,224,255
    1: 32,104,48,255
    2: 208,240,224,255
    3: 200,224,208,255
    4: 192,224,200,255
</code>
 * 
 * @author stefan
 * 
 */
public class GdalInfo2RulesListDialog extends CancellableDialogAdapter {

	private JTextArea gdaltextarea;
	private final AtlasStylerRaster asr;
	private ColorMap colorMap;
	private final JLabel infolabel = new JLabel();

	public GdalInfo2RulesListDialog(Component owner, AtlasStylerRaster asr) {
		super(owner, ASUtil.R("GdalInfo2RulesList.Header"));
		this.asr = asr;

		JPanel contentPane = new JPanel(new MigLayout("wrap 1, w 100:300:500"));
		contentPane.add(new JLabel(ASUtil.R("GdalInfo2RulesList.Explanation")));
		contentPane.add(new JScrollPane(getGdalTextArea()), "grow y, grow x");
		contentPane.add(getOkButton(), "tag ok, split 3");
		contentPane.add(getCancelButton(), "tag cancel");
		contentPane.add(infolabel);
		setContentPane(contentPane);
		getOkButton().setEnabled(false);

		SwingUtil.setPreferredWidth(this, 400);

		pack();
	}

	private JTextArea getGdalTextArea() {
		if (gdaltextarea == null) {
			gdaltextarea = new JTextArea(30, 30);

			gdaltextarea.getDocument().addDocumentListener(
					new DocumentListener() {

						@Override
						public void removeUpdate(DocumentEvent e) {
							parse();
						}

						@Override
						public void insertUpdate(DocumentEvent e) {
							parse();
						}

						@Override
						public void changedUpdate(DocumentEvent e) {
							parse();
						}
					});

		}
		return gdaltextarea;
	}

	@Override
	public void cancel() {
		dispose();
	}

	@Override
	public boolean okClose() {
		RasterRulesList_DistinctValues newRl = new RasterRulesList_DistinctValues(
				asr.getStyledRaster());
		newRl.importColorMap(colorMap);
		newRl.setTitle("gdalimport");
		asr.addRulesList(newRl);
		return super.okClose();
	}

	public void parse() {
		try {
			colorMap = StylingUtil.parseColormapToSld(getGdalTextArea()
					.getText());
		} catch (Exception e) {
			colorMap = null;
		}
		getOkButton().setEnabled(
				colorMap != null && colorMap.getColorMapEntries().length > 0);
		if (colorMap == null) {
			infolabel.setText(ASUtil.R("GdalInfo2RulesList.NothingImported"));
		} else {
			infolabel.setText(ASUtil.R(
					"GdalInfo2RulesList.UnderstandingColormapWithNValues",
					colorMap.getColorMapEntries().length));
		}
	}
}