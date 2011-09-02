package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.miginfocom.swing.MigLayout;

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
	private Object[] contrastEnhancementMethods;

	public RasterRulesList_RGB_GUI(RasterRulesListRGB rulesList,
			AtlasStylerRaster atlasStyler) {
		super(rulesList);
		this.atlasStyler = atlasStyler;
		initialize();
	}

	private void initialize() {
		setLayout(new MigLayout("wrap 3, fillx", "[left][left]"));
		add(getDescriptionJLabel(), "span, center");
		add(getContrastDescriptionJLabel(), "span, right");
		add(getRedJLabel());
		add(getRedChannelComboBox(), "growx");
		add(getRedChannelContrastComboBox());
		add(getGreenJLabel());
		add(getGreenChannelComboBox(), "growx");
		add(getGreenChannelContrastComboBox());
		add(getBlueJLabel());
		add(getBlueChannelComboBox(), "growx");
		add(getBlueChannelContrastComboBox());
	}

	private JLabel getContrastDescriptionJLabel() {
		if (contrastDescriptionJLabel == null) {
			contrastDescriptionJLabel = new JLabel(
					"Choose your enhancement method");
		}
		return contrastDescriptionJLabel;
	}

	private JComboBox getBlueChannelContrastComboBox() {
		if (blueChannelContrastComboBox == null) {
			blueChannelContrastComboBox = new JComboBox(ContrastMethod.values());
			blueChannelContrastComboBox.setRenderer(contrastListCellRenderer);

			SwingUtil.addMouseWheelForCombobox(blueChannelContrastComboBox);
			blueChannelContrastComboBox.setSelectedItem(rulesList.getBlueMethod());
			blueChannelContrastComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setBlueMethod((ContrastMethod) blueChannelContrastComboBox.getSelectedItem());
				}
			});
		}
		return blueChannelContrastComboBox;
	}


	private JComboBox getGreenChannelContrastComboBox() {
		if (greenChannelContrastComboBox == null) {
			greenChannelContrastComboBox = new JComboBox(ContrastMethod.values());
			greenChannelContrastComboBox.setRenderer(contrastListCellRenderer);

			greenChannelContrastComboBox.setSelectedItem(rulesList.getGreenMethod());
			SwingUtil.addMouseWheelForCombobox(greenChannelContrastComboBox);
			greenChannelContrastComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setGreenMethod((ContrastMethod) greenChannelContrastComboBox.getSelectedItem());
				}
			});
		}
		return greenChannelContrastComboBox;
	}
	
	DefaultListCellRenderer contrastListCellRenderer = new DefaultListCellRenderer() {
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			l.setText(((ContrastMethod)value).name());
			return l;
		}
	};


	private JComboBox getRedChannelContrastComboBox() {
		if (redChannelContrastComboBox == null) {
			redChannelContrastComboBox = new JComboBox(ContrastMethod.values());
			redChannelContrastComboBox.setSelectedItem(rulesList.getRedMethod());
			
			redChannelContrastComboBox.setRenderer(contrastListCellRenderer);
			
			
//			redChannelContrastComboBox = new JComboBox(
//					getContrastEnhancementMethods());
			SwingUtil.addMouseWheelForCombobox(redChannelContrastComboBox);
			redChannelContrastComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setRedMethod((ContrastMethod) redChannelContrastComboBox.getSelectedItem());
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
			blueChannelComboBox.setSelectedIndex(rulesList.getBlue() - 1); // 1-based
																			// to
																			// 0-based
			SwingUtil.addMouseWheelForCombobox(blueChannelComboBox);
			blueChannelComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setBlue(blueChannelComboBox.getSelectedIndex() + 1); // 0-based
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
			greenChannelComboBox.setSelectedIndex(rulesList.getGreen() - 1); // 1-based
																				// to
																				// 0-based
			SwingUtil.addMouseWheelForCombobox(greenChannelComboBox);
			greenChannelComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setGreen(greenChannelComboBox.getSelectedIndex() + 1); // 0-based
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
			redChannelComboBox.setSelectedIndex(rulesList.getRed() - 1); // 1-based
																			// to
																			// 0-based
			SwingUtil.addMouseWheelForCombobox(redChannelComboBox);
			redChannelComboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {
					rulesList.setRed(redChannelComboBox.getSelectedIndex() + 1); // 0-based
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
