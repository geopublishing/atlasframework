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

public class GdalInfo2RulesListDialog extends CancellableDialogAdapter {

	private JTextArea gdaltextarea;
	private final AtlasStylerRaster asr;
	private ColorMap colorMap;

	public GdalInfo2RulesListDialog(Component owner, AtlasStylerRaster asr) {
		super(owner, "Import gdal info output"); //i8n
		this.asr = asr;
		
		JPanel contentPane = new JPanel(new MigLayout("wrap 1, w 100:300:500"));
		contentPane.add(new JLabel(ASUtil.R("GdalInfo2RulesList.Explanation")));
		contentPane.add( new JScrollPane( getGdalTextArea() ),"grow y, grow x");
		contentPane.add(getOkButton());
		setContentPane(contentPane);
		
		pack();
	}

	private JTextArea getGdalTextArea() {
		if (gdaltextarea == null){
			gdaltextarea = new JTextArea(30,30);
			
			gdaltextarea.getDocument().addDocumentListener(new DocumentListener() {
				
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
	}
	
	@Override
	public boolean okClose() {
		RasterRulesList_DistinctValues newRl = new RasterRulesList_DistinctValues(asr.getStyledRaster());
		newRl.importColorMap(colorMap);
		asr.addRulesList(newRl);
		return super.okClose();
	}

	public void parse() {
		colorMap = StylingUtil.parseColormapToSld(getGdalTextArea().getText());
	}
}