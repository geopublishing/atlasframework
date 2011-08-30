package org.geopublishing.atlasStyler.swing;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesListRGB;

public class RasterRulesList_RGB_GUI extends
		AbstractRulesListGui<RasterRulesListRGB> {

	private AtlasStylerRaster atlasStyler;
	private JLabel redJLabel;
	private JLabel greenJLabel;
	private JLabel blueJLabel;
	private JComboBox redChannelComboBox ;

	public RasterRulesList_RGB_GUI(RasterRulesListRGB rulesList,
			AtlasStylerRaster atlasStyler) {
		super(rulesList);
		this.atlasStyler = atlasStyler;
		initialize();
	}

	private void initialize() {
		setLayout(new MigLayout("wrap 2, fillx"));
		add(getRedJLabel());
		add(getRedChannelComboBox());
		add(getGreenJLabel());
		add(getBlueJLabel());
	}

	private JComboBox getRedChannelComboBox() {
		if(redChannelComboBox == null){
			redChannelComboBox = new JComboBox();
		}
		return redChannelComboBox;
	}

	private JLabel getBlueJLabel() {
		if (blueJLabel == null) {
			blueJLabel = new JLabel("Blue channel"); //i8n
		}
		return blueJLabel;
	}

	private JLabel getGreenJLabel() {
		if (greenJLabel == null) {
			greenJLabel = new JLabel("Green channel"); //i8n
		}
		return greenJLabel;
	}

	private JLabel getRedJLabel() {
		if (redJLabel == null) {
			redJLabel = new JLabel("Red Channel"); // i8n
		}
		return redJLabel;
	}

}
