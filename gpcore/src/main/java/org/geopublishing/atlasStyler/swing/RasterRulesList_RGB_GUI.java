package org.geopublishing.atlasStyler.swing;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesListRGB;
import org.geopublishing.geopublisher.GpUtil;

import de.schmitzm.lang.LangUtil;

public class RasterRulesList_RGB_GUI extends
		AbstractRulesListGui<RasterRulesListRGB> {

	private AtlasStylerRaster atlasStyler;
	private JLabel redJLabel;
	private JLabel greenJLabel;
	private JLabel blueJLabel;
	private JLabel descriptionJLabel;
	private JComboBox redChannelComboBox;
	private JComboBox greenChannelComboBox;
	private JComboBox blueChannelComboBox;
	private Object[] bands;

	public RasterRulesList_RGB_GUI(RasterRulesListRGB rulesList,
			AtlasStylerRaster atlasStyler) {
		super(rulesList);
		this.atlasStyler = atlasStyler;
		initialize();
	}

	private void initialize() {
		setLayout(new MigLayout("wrap 2, fillx", "[left][left]"));
		add(getDescriptionJLabel(), "span, center");
		add(getRedJLabel(), "gapright 5");
		add(getRedChannelComboBox(), "growx");
		add(getGreenJLabel(), "gapright 5");
		add(getGreenChannelComboBox(), "growx");
		add(getBlueJLabel(), "gapright 5");
		add(getBlueChannelComboBox(), "growx");
		super.rulesList.setBlue(1);
	}

	private Object[] getBands() {
		if (bands == null) {
			int n = atlasStyler.getBands();
			bands = new Object[0];
			for (int i = 0; i < n; i++)
				bands = LangUtil.extendArray(bands, (GpUtil.R("RasterRulesListRGB.Gui.Channel") + " "+(i+1)));
		}
		return bands;
	}

	private JLabel getDescriptionJLabel() {
		if (descriptionJLabel == null) {
			descriptionJLabel = new JLabel(
					GpUtil.R("RasterRulesListRGB.Gui.Description"));
		}
		return descriptionJLabel;
	}

	private JComboBox getBlueChannelComboBox() {
		if (blueChannelComboBox == null) {
			blueChannelComboBox = new JComboBox(getBands());
			blueChannelComboBox.setSelectedIndex(rulesList.getBlue()-1);
			blueChannelComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setBlue(blueChannelComboBox.getSelectedIndex()+1);
				}
			});
		}
		return blueChannelComboBox;
	}

	private JComboBox getGreenChannelComboBox() {
		if (greenChannelComboBox == null) {
			greenChannelComboBox = new JComboBox(getBands());
			greenChannelComboBox.setSelectedIndex(rulesList.getGreen()-1);
			greenChannelComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setGreen(greenChannelComboBox.getSelectedIndex()+1);
				}
			});
		}
		return greenChannelComboBox;
	}

	private JComboBox getRedChannelComboBox() {
		if (redChannelComboBox == null) {
			redChannelComboBox = new JComboBox(getBands());
			redChannelComboBox.setSelectedIndex(rulesList.getRed()-1);
			redChannelComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setRed(redChannelComboBox.getSelectedIndex()+1);
				}
			});
		}
		return redChannelComboBox;
	}

	private JLabel getBlueJLabel() {
		if (blueJLabel == null) {
			blueJLabel = new JLabel(GpUtil.R("RasterRulesListRGB.Gui.BlueChannel")); 
		}
		return blueJLabel;
	}

	private JLabel getGreenJLabel() {
		if (greenJLabel == null) {
			greenJLabel = new JLabel(GpUtil.R("RasterRulesListRGB.Gui.GreenChannel")); 
		}
		return greenJLabel;
	}

	private JLabel getRedJLabel() {
		if (redJLabel == null) {
			redJLabel = new JLabel(GpUtil.R("RasterRulesListRGB.Gui.RedChannel")); 
		}
		return redJLabel;
	}

}
