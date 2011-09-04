package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;

import net.miginfocom.swing.MigLayout;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesListRGB;
import org.geopublishing.geopublisher.GpUtil;
import org.jfree.chart.ChartMouseEvent;
import org.opengis.style.ContrastMethod;

import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.SwingUtil;

/**
 * A GUI to edit a {@link RasterRulesListRGB}
 */
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
	private JComboBox redChannelContrastComboBox;
	private JComboBox blueChannelContrastComboBox;
	private JComboBox greenChannelContrastComboBox;
	private JLabel contrastDescriptionJLabel;
	private JComboBox redChannelGammaComboBox;
	private JComboBox greenChannelGammaComboBox;
	private JComboBox blueChannelGammaComboBox;
	private JLabel contrastJLabel;
	private JLabel gammaValueJLabel;
	private JComboBox globalGammaComboBox;
	private JComboBox globalMethodComboBox;
	private JLabel globalJLabel;
	private JLabel perChannelJLabel;

	public RasterRulesList_RGB_GUI(RasterRulesListRGB rulesList,
			AtlasStylerRaster atlasStyler) {
		super(rulesList);
		this.atlasStyler = atlasStyler;
		initialize();
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
		add(getRedJLabel());
		add(getRedChannelComboBox(), "growx");
		add(getRedChannelContrastComboBox(), "growx");
		add(getRedChannelGammaComboBox());
		add(getGreenJLabel());
		add(getGreenChannelComboBox(), "growx");
		add(getGreenChannelContrastComboBox(), "growx");
		add(getGreenChannelGammaComboBox());
		add(getBlueJLabel());
		add(getBlueChannelComboBox(), "growx");
		add(getBlueChannelContrastComboBox(), "growx");
		add(getBlueChannelGammaComboBox(), "wrap 10");
		add(getContrastDescriptionJLabel(), "span, south");
	}

	private JLabel getPerChannelJLabel() {
		if (perChannelJLabel == null) {
			perChannelJLabel = new JLabel(
					GpUtil.R("RasterRulesListRGB.Gui.PerChannel"));
		}
		return perChannelJLabel;
	}

	private JLabel getGlobalJLabel() {
		if (globalJLabel == null) {
			globalJLabel = new JLabel(GpUtil.R("RasterRulesListRGB.Gui.Global"));
		}
		return globalJLabel;
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

	private JLabel getContrastJLabel() {
		if (contrastJLabel == null) {
			contrastJLabel = new JLabel(
					GpUtil.R("RasterRulesListRGB.Gui.Contrast"));
		}
		return contrastJLabel;
	}

	private JLabel getGammaValueJLabel() {
		if (gammaValueJLabel == null) {
			gammaValueJLabel = new JLabel(
					GpUtil.R("RasterRulesListRGB.Gui.GammaValue"));
		}
		return gammaValueJLabel;
	}

	private JComboBox getRedChannelGammaComboBox() {
		if (redChannelGammaComboBox == null) {
			redChannelGammaComboBox = new JComboBox(
					AbstractStyleEditGUI.GAMMA_VALUES);
			SwingUtil.addMouseWheelForCombobox(redChannelGammaComboBox);
			redChannelGammaComboBox.setSelectedItem(Double.valueOf(rulesList
					.getGammaValue(1).toString()));
			redChannelGammaComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setGammaValue(1, Double
							.valueOf(redChannelGammaComboBox.getSelectedItem()
									.toString()));
				}
			});
		}
		return redChannelGammaComboBox;
	}

	private JComboBox getGreenChannelGammaComboBox() {
		if (greenChannelGammaComboBox == null) {
			greenChannelGammaComboBox = new JComboBox(
					AbstractStyleEditGUI.GAMMA_VALUES);
			SwingUtil.addMouseWheelForCombobox(greenChannelGammaComboBox);
			greenChannelGammaComboBox.setSelectedItem(Double.valueOf(rulesList
					.getGammaValue(2).toString()));
			greenChannelGammaComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setGammaValue(2, Double
							.valueOf(greenChannelGammaComboBox
									.getSelectedItem().toString()));
				}
			});
		}
		return greenChannelGammaComboBox;
	}

	private JComboBox getBlueChannelGammaComboBox() {
		if (blueChannelGammaComboBox == null) {
			blueChannelGammaComboBox = new JComboBox(
					AbstractStyleEditGUI.GAMMA_VALUES);
			SwingUtil.addMouseWheelForCombobox(blueChannelGammaComboBox);
			blueChannelGammaComboBox.setSelectedItem(Double.valueOf(rulesList
					.getGammaValue(3).toString()));
			blueChannelGammaComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setGammaValue(3, Double
							.valueOf(blueChannelGammaComboBox.getSelectedItem()
									.toString()));
				}
			});
		}
		return blueChannelGammaComboBox;
	}

	private JLabel getContrastDescriptionJLabel() {
		if (contrastDescriptionJLabel == null) {
			contrastDescriptionJLabel = new JLabel(
					GpUtil.R("RasterRulesListRGB.Gui.ContrastEnhancementDescription"));
		}
		return contrastDescriptionJLabel;
	}

	private JComboBox getBlueChannelContrastComboBox() {
		if (blueChannelContrastComboBox == null) {
			blueChannelContrastComboBox = new JComboBox(ContrastMethod.values());
			blueChannelContrastComboBox.setRenderer(contrastListCellRenderer);

			SwingUtil.addMouseWheelForCombobox(blueChannelContrastComboBox);
			blueChannelContrastComboBox.setSelectedItem(rulesList
					.getChannelMethod(3));
			blueChannelContrastComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setChannelMethod(3,
							(ContrastMethod) blueChannelContrastComboBox
									.getSelectedItem());
				}
			});
		}
		return blueChannelContrastComboBox;
	}

	private JComboBox getGreenChannelContrastComboBox() {
		if (greenChannelContrastComboBox == null) {
			greenChannelContrastComboBox = new JComboBox(
					ContrastMethod.values());
			greenChannelContrastComboBox.setRenderer(contrastListCellRenderer);

			greenChannelContrastComboBox.setSelectedItem(rulesList
					.getChannelMethod(2));
			SwingUtil.addMouseWheelForCombobox(greenChannelContrastComboBox);
			greenChannelContrastComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setChannelMethod(2,
							(ContrastMethod) greenChannelContrastComboBox
									.getSelectedItem());
				}
			});
		}
		return greenChannelContrastComboBox;
	}

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

	private JComboBox getRedChannelContrastComboBox() {
		if (redChannelContrastComboBox == null) {
			redChannelContrastComboBox = new JComboBox(ContrastMethod.values());
			redChannelContrastComboBox.setSelectedItem(rulesList
					.getChannelMethod(1));

			redChannelContrastComboBox.setRenderer(contrastListCellRenderer);

			// redChannelContrastComboBox = new JComboBox(
			// getContrastEnhancementMethods());
			SwingUtil.addMouseWheelForCombobox(redChannelContrastComboBox);
			redChannelContrastComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setChannelMethod(1,
							(ContrastMethod) redChannelContrastComboBox
									.getSelectedItem());
				}
			});
		}
		return redChannelContrastComboBox;
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
			blueChannelComboBox.setSelectedIndex(rulesList.getChannel(3) - 1); // 1-based
																				// to
																				// 0-based
			SwingUtil.addMouseWheelForCombobox(blueChannelComboBox);
			blueChannelComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setChannel(3,
							blueChannelComboBox.getSelectedIndex() + 1); // 0-based
					// to
					// 1-based
				}
			});
		}
		return blueChannelComboBox;
	}

	private JComboBox getGreenChannelComboBox() {
		if (greenChannelComboBox == null) {
			greenChannelComboBox = new JComboBox(getBands());
			greenChannelComboBox.setSelectedIndex(rulesList.getChannel(2) - 1); // 1-based
																				// to
																				// 0-based
			SwingUtil.addMouseWheelForCombobox(greenChannelComboBox);
			greenChannelComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setChannel(2,
							greenChannelComboBox.getSelectedIndex() + 1); // 0-based
																			// to
																			// 1-based
				}
			});
		}
		return greenChannelComboBox;
	}

	private JComboBox getRedChannelComboBox() {
		if (redChannelComboBox == null) {
			redChannelComboBox = new JComboBox(getBands());
			redChannelComboBox.setSelectedIndex(rulesList.getChannel(1) - 1); // 1-based
																				// to
																				// 0-based
			SwingUtil.addMouseWheelForCombobox(redChannelComboBox);
			redChannelComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setChannel(1,
							redChannelComboBox.getSelectedIndex() + 1); // 0-based
																		// to
																		// 1-based
				}
			});
		}
		return redChannelComboBox;
	}

	private JLabel getBlueJLabel() {
		if (blueJLabel == null) {
			blueJLabel = new JLabel(
					GpUtil.R("RasterRulesListRGB.Gui.BlueChannel"));
		}
		return blueJLabel;
	}

	private JLabel getGreenJLabel() {
		if (greenJLabel == null) {
			greenJLabel = new JLabel(
					GpUtil.R("RasterRulesListRGB.Gui.GreenChannel"));
		}
		return greenJLabel;
	}

	private JLabel getRedJLabel() {
		if (redJLabel == null) {
			redJLabel = new JLabel(
					GpUtil.R("RasterRulesListRGB.Gui.RedChannel"));
		}
		return redJLabel;
	}

}
