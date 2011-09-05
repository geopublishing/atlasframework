package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.ArrayUtils;
import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesListRGB;
import org.geopublishing.geopublisher.GpUtil;
import org.opengis.style.ContrastMethod;

import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.SwingUtil;

/**
 * A GUI to edit a {@link RasterRulesListRGB}
 */
public class RasterRulesList_RGB_GUI extends
		AbstractRulesListGui<RasterRulesListRGB> {

	private AtlasStylerRaster atlasStyler;
	private JLabel[] channelJLabel = new JLabel[3];
	private JLabel descriptionJLabel;
	private JComboBox[] channelComboBox = new JComboBox[3];
	private Object[] bands;
	private JComboBox[] channelContrastComboBox = new JComboBox[3];
	private JLabel contrastDescriptionJLabel;
	private JComboBox[] channelGammaComboBox = new JComboBox[3];
	private JLabel contrastJLabel;
	private JLabel gammaValueJLabel;
	private JComboBox globalGammaComboBox;
	private JComboBox globalMethodComboBox;
	private JLabel globalJLabel;
	private JLabel perChannelJLabel;

	DefaultListCellRenderer contrastListCellRenderer = new DefaultListCellRenderer() {

		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel l = (JLabel) super.getListCellRendererComponent(list, value,
					index, isSelected, cellHasFocus);
			l.setText(((ContrastMethod) value).name());
			return l;
		}
	};

	public RasterRulesList_RGB_GUI(RasterRulesListRGB rulesList,
			AtlasStylerRaster atlasStyler) {
		super(rulesList);
		this.atlasStyler = atlasStyler;
		initialize();
	}

	/**
	 * Gets the number of bands
	 * 
	 * @return Object holding strings of "Channel + <Channelnumber>" for every
	 *         band
	 */
	private Object[] getBands() {
		if (bands == null) {
			int n = atlasStyler.getBands();
			bands = new Object[0];
			for (int i = 0; i < n; i++)
				bands = LangUtil
						.extendArray(bands,
								(GpUtil.R("RasterRulesListRGB.Gui.Channel")
										+ " " + (i + 1)));
		}
		return bands;
	}

	/**
	 * returns the corresponding JComboBox for a given channel
	 * 
	 * @param channel
	 *            RGB-Channel where 1=red, 2=green, 3=blue
	 * @return
	 */
	private JComboBox getChannelComboBox(int channel) {
		if (channelComboBox[channel - 1] == null) {
			channelComboBox[channel - 1] = new JComboBox(getBands());
			channelComboBox[channel - 1].setSelectedIndex(rulesList
					.getChannel(channel) - 1);
			SwingUtil.addMouseWheelForCombobox(channelComboBox[channel - 1]);
			channelComboBox[channel - 1].addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					int index = ArrayUtils.indexOf(channelComboBox,
							arg0.getSource()) + 1; // 0-based to 1-based
					rulesList.setChannel(index,
							channelComboBox[index - 1].getSelectedIndex() + 1); // 1-based
																				// to
																				// 0-based
				}
			});
		}
		return channelComboBox[channel - 1];
	}

	/**
	 * returns the corresponding Contrast JComboBox for a given channel
	 * 
	 * @param channel
	 *            RGB-Channel where 1=red, 2=green, 3=blue
	 * @return
	 */
	private JComboBox getChannelContrastComboBox(int channel) {
		if (channelContrastComboBox[channel - 1] == null) {
			channelContrastComboBox[channel - 1] = new JComboBox(
					ContrastMethod.values());
			channelContrastComboBox[channel - 1]
					.setRenderer(contrastListCellRenderer);
			SwingUtil
					.addMouseWheelForCombobox(channelContrastComboBox[channel - 1]);
			channelContrastComboBox[channel - 1].setSelectedItem(rulesList
					.getChannelMethod(channel));

			channelContrastComboBox[channel - 1]
					.addItemListener(new ItemListener() {

						@Override
						public void itemStateChanged(ItemEvent arg0) {
							int index = ArrayUtils.indexOf(
									channelContrastComboBox, arg0.getSource()) + 1; // 0-based
																					// to
																					// 1-based
							rulesList
									.setChannelMethod(
											index,
											(ContrastMethod) channelContrastComboBox[index - 1] // 1-based
																								// to
																								// 0-based
													.getSelectedItem());
						}
					});
		}
		return channelContrastComboBox[channel - 1];
	}

	/**
	 * returns the corresponding GammaValue JComboBox for a given channel
	 * 
	 * @param channel
	 *            RGB-Channel where 1=red, 2=green, 3=blue
	 * @return
	 */
	private JComboBox getChannelGammaComboBox(int channel) {
		if (channelGammaComboBox[channel - 1] == null) {
			channelGammaComboBox[channel - 1] = new JComboBox(
					AbstractStyleEditGUI.GAMMA_VALUES);
			SwingUtil
					.addMouseWheelForCombobox(channelGammaComboBox[channel - 1]);
			channelGammaComboBox[channel - 1].setSelectedItem(Double
					.valueOf(rulesList.getGammaValue(channel).toString()));
			channelGammaComboBox[channel - 1]
					.addItemListener(new ItemListener() {

						@Override
						public void itemStateChanged(ItemEvent arg0) {
							int index = ArrayUtils.indexOf(
									channelGammaComboBox, arg0.getSource()) + 1; // 0-based
																					// to
																					// 1-based
							rulesList.setGammaValue(index, Double
									.valueOf(channelGammaComboBox[index - 1] // 1-based
																				// to
																				// 0-based
											.getSelectedItem().toString()));
						}
					});
		}
		return channelGammaComboBox[channel - 1];
	}

	/**
	 * returns the corresponding JLabel for a given channel
	 * 
	 * @param channel
	 *            RGB-Channel where 1=red, 2=green, 3=blue
	 * @return
	 */
	private JLabel getChannelJLabel(int channel) {
		if (channelJLabel[channel - 1] == null) {
			channelJLabel[channel - 1] = new JLabel(GpUtil.R(
					"RasterRulesListRGB.GUI.RGBChannel",
					getChannelName(channel)));
		}
		return channelJLabel[channel - 1];
	}

	/**
	 * convenience method to transform channel indexes to channel names
	 * 
	 * @param channel
	 *            RGBChannel where 1=red, 2=green, 3=blue
	 * @return String holding the channel name
	 */
	private String getChannelName(int channel) {
		if (channel == 1)
			return GpUtil.R("RasterRulesListRGB.Gui.Red");
		if (channel == 2)
			return GpUtil.R("RasterRulesListRGB.Gui.Green");
		if (channel == 3)
			return GpUtil.R("RasterRulesListRGB.Gui.Blue");
		else
			return "";
	}

	/**
	 * this method returns an explanation on what the ContrastMethods and
	 * GammaValues do
	 * 
	 * @return
	 */
	private JLabel getContrastDescriptionJLabel() {
		if (contrastDescriptionJLabel == null) {
			contrastDescriptionJLabel = new JLabel(
					GpUtil.R("RasterRulesListRGB.Gui.ContrastEnhancementDescription"));
		}
		return contrastDescriptionJLabel;
	}

	private JLabel getContrastJLabel() {
		if (contrastJLabel == null) {
			contrastJLabel = new JLabel(
					GpUtil.R("RasterRulesListRGB.Gui.Contrast"));
		}
		return contrastJLabel;
	}

	private JLabel getDescriptionJLabel() {
		if (descriptionJLabel == null) {
			descriptionJLabel = new JLabel(
					GpUtil.R("RasterRulesListRGB.Gui.Description"));
		}
		return descriptionJLabel;
	}

	private JLabel getGammaValueJLabel() {
		if (gammaValueJLabel == null) {
			gammaValueJLabel = new JLabel(
					GpUtil.R("RasterRulesListRGB.Gui.GammaValue"));
		}
		return gammaValueJLabel;
	}

	private JComboBox getGlobalGammaComboBox() {
		if (globalGammaComboBox == null) {
			globalGammaComboBox = new JComboBox(
					AbstractStyleEditGUI.GAMMA_VALUES);
			SwingUtil.addMouseWheelForCombobox(globalGammaComboBox);
			globalGammaComboBox.setSelectedItem(Double.valueOf(rulesList
					.getRSGamma().toString()));
			globalGammaComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setRSGamma(Double.valueOf(globalGammaComboBox
							.getSelectedItem().toString()));
				}
			});
		}
		return globalGammaComboBox;
	}

	private JLabel getGlobalJLabel() {
		if (globalJLabel == null) {
			globalJLabel = new JLabel(GpUtil.R("RasterRulesListRGB.Gui.Global"));
		}
		return globalJLabel;
	}

	private JComboBox getGlobalMethodComboBox() {
		if (globalMethodComboBox == null) {
			globalMethodComboBox = new JComboBox(ContrastMethod.values());
			globalMethodComboBox.setRenderer(contrastListCellRenderer);

			SwingUtil.addMouseWheelForCombobox(globalMethodComboBox);
			globalMethodComboBox.setSelectedItem(rulesList.getRSMethod());
			globalMethodComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setRSMethod((ContrastMethod) globalMethodComboBox
							.getSelectedItem());
				}
			});
		}
		return globalMethodComboBox;
	}

	private JLabel getPerChannelJLabel() {
		if (perChannelJLabel == null) {
			perChannelJLabel = new JLabel(
					GpUtil.R("RasterRulesListRGB.Gui.PerChannel"));
		}
		return perChannelJLabel;
	}

	private void initialize() {
		setLayout(new MigLayout("wrap 4, fillx",
				"[100!][min!][min!][min!, right]", ""));
		add(getDescriptionJLabel(), "span, center, wrap 10");
		add(getGammaValueJLabel(), "cell 3 1");
		add(getContrastJLabel(), "cell 2 1");
		add(getGlobalJLabel(), "split, span");
		add(new JSeparator(), "growx, wrap");
		add(getGlobalMethodComboBox(), "cell 2 3,growx");
		add(getGlobalGammaComboBox(), "cell 3 3, wrap 15");
		add(getPerChannelJLabel(), "split, span");
		add(new JSeparator(), "growx, wrap");
		// red
		add(getChannelJLabel(1));
		add(getChannelComboBox(1), "growx");
		add(getChannelContrastComboBox(1), "growx");
		add(getChannelGammaComboBox(1));
		// green
		add(getChannelJLabel(2));
		add(getChannelComboBox(2), "growx");
		add(getChannelContrastComboBox(2), "growx");
		add(getChannelGammaComboBox(2));
		// blue
		add(getChannelJLabel(3));
		add(getChannelComboBox(3), "growx");
		add(getChannelContrastComboBox(3), "growx");
		add(getChannelGammaComboBox(3), "wrap 10");
		add(getContrastDescriptionJLabel(), "span, south");
	}

}
