/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasViewer.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.geopublishing.atlasViewer.GpCoreUtil;
import org.geopublishing.atlasViewer.dp.layer.DpLayer;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;
import org.geopublishing.atlasViewer.map.Map;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Style;

import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.geotools.styling.StyledLayerUtil;
import de.schmitzm.geotools.styling.StyledRasterInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.i18n.I18NUtil;
import de.schmitzm.i18n.Translation;
import de.schmitzm.jfree.chart.style.ChartStyle;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.JPanel;

/**
 * This helper class is helping to create nice legend images, {@link JComponent}
 * s etc.
 */
public class SldLegendUtil {
	static final private Logger LOGGER = LangUtil
			.createLogger(SldLegendUtil.class);

	/** Determine height of the icons depending on the Font used **/
	protected static final Font FONT = new JLabel().getFont();

	/** The hight of the single legend icons **/
	public final static int ICONHEIGHT = new JLabel().getFontMetrics(FONT)
			.getHeight();

	/** The width of the single legend icons **/
	public final static int ICONWIDTH = 23;

	/** The size of the gaps between the color-symbols in raster legends * */
	public final static int gapHeight = 3;

	/**
	 * Creates a legend using the given description and {@link Style}.
	 * 
	 * @param style All selection related {@link FeatureTypeStyle}s are automatically removed.
	 * @param desc
	 *            may be <code>null</code>. In that case no description is used.
	 */
	static public JPanel createLegend(
			final StyledLayerInterface<?> styledLayer, Style style, String desc) {

		if (styledLayer == null)
			throw new IllegalArgumentException("styledLayer may not be null!");

		if (style == null) {
			style = styledLayer.getStyle();
		}
		
		// Remove any selection & text 
		if (style != null)
			style = StylingUtil.removeSelectionFeatureTypeStyle(style);

		JPanel oneStyleLegendBox;
		if (styledLayer instanceof StyledFeaturesInterface) {
			oneStyleLegendBox = StyledLayerUtil.createLegendSwingPanel(style,
					((StyledFeaturesInterface) styledLayer).getSchema(),
					ICONWIDTH, ICONHEIGHT);
		} else if (styledLayer instanceof StyledRasterInterface<?>) {
			oneStyleLegendBox = StyledLayerUtil.createLegendSwingPanel(
					(StyledRasterInterface<?>) styledLayer, style, ICONWIDTH,
					ICONHEIGHT);
		} else {
			throw new IllegalArgumentException("Can't create a legend for "
					+ styledLayer.getClass().getSimpleName());
		}

		if (desc != null && !desc.isEmpty()) {
			JPanel descAndLegend = new JPanel(new BorderLayout());
			descAndLegend.setBorder(BorderFactory.createEmptyBorder());

			// We have description. Lets use it as a label above the legend
			JTextArea descLabel = new JTextArea(desc);
			descLabel.setWrapStyleWord(true);
			descLabel.setEditable(false);
			descLabel.setLineWrap(true);
			descLabel.setOpaque(false);
			descAndLegend.add(descLabel, BorderLayout.NORTH);
			descAndLegend.add(oneStyleLegendBox, BorderLayout.CENTER);

			return descAndLegend;
		} else
			return oneStyleLegendBox;
	}

	/**
	 * Creates a legend using the given translated description and and
	 * {@link Style}.
	 * 
	 * @param layerStyle
	 *            If <code>null</code>, description and {@link Style} from the
	 *            {@link StyledLayerInterface} are used.
	 */
	static public JPanel createLegend(
			final StyledLayerInterface<?> styledLayer, Style style,
			Translation desc) {
		return createLegend(styledLayer, style, desc != null ? desc.toString()
				: null);
	}

	/**
	 * Creates a legend using any description (if available) and {@link Style} from the
	 * {@link StyledLayerInterface}.
	 */
	static public JPanel createLegend(
			final StyledLayerInterface<?> styledLayer) {
		return createLegend(styledLayer, styledLayer.getStyle(), styledLayer
				.getDesc());
	}

	/**
	 * Creates a legend using the description and {@link Style} from the
	 * {@link LayerStyle}.
	 * 
	 * @param layerStyle
	 *            If <code>null</code>, description and {@link Style} from the
	 *            {@link StyledLayerInterface} are used.
	 */
	static public JComponent createLegend(
			final StyledLayerInterface<?> styledLayer, LayerStyle layerStyle) {

		if (layerStyle == null)
			return createLegend(styledLayer);

		Translation desc = null;
		if (!I18NUtil.isEmpty(layerStyle.getDesc())) {
			desc = layerStyle.getDesc();
		} else if (!I18NUtil.isEmpty(styledLayer.getDesc())) {
			desc = styledLayer.getDesc();
		}

		return createLegend(styledLayer, layerStyle.getStyle(), desc);
	}
	
	

	/**
	 * @param mapLayer
	 *            The GeoTools {@link MapLayer} that will be affected by style
	 *            changes.
	 * @param availStyleIDs
	 *            A {@link List} of IDs of AdditionalStyles
	 * @param dpLayer
	 *            The {@link DpLayer} that holds all the additional styles
	 * 
	 * @return a new {@link Component} that will represent all available Styles
	 *         for a {@link DpLayer}, if there are less than 5 additional
	 *         styles. If there are 5 or more additional styles, a
	 *         {@link JComboBox} is returned. If only one additonal style is
	 *         available, it will replace the {@link DpLayer} as the source for
	 *         title and description.
	 */
	public static Component createAdditionalStylesPane(
			final MapLayer mapLayer,
			final ArrayList<String> availStyleIDs,
			final DpLayer<?, ? extends ChartStyle> dpLayer, 
			final Map map, 
			final AtlasMapLegend atlasMapLegend
			) {

		/**
		 * Determine whether the the number of rules differs so much, that a
		 * JTabbedPne would look ugly
		 */
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (String lsID : availStyleIDs) {
			LayerStyle ls = dpLayer.getLayerStyleByID(lsID);
			int length = ls.getStyle().featureTypeStyles().get(0).rules()
					.size();
			min = Math.min(min, length);
			max = Math.max(max, length);
		}

		if (availStyleIDs.size() > 3 || (max - min > 4)) {
			String selectedStyleID = map.getSelectedStyleID(
					dpLayer.getId());
			if (selectedStyleID == null) {
				// We might just have created new additional styles, and none
				// has been selected yet. But thats not a reason for a NPE -
				// right? Just use the first one.
				selectedStyleID = availStyleIDs.get(0);
			}

			// Let's create a JComboBox
			final JComboBoxForAddStylesPanel comboBoxForAddStylesPanel = new JComboBoxForAddStylesPanel(
					availStyleIDs, dpLayer, mapLayer, selectedStyleID, atlasMapLegend);
			
			comboBoxForAddStylesPanel.getComboBox().addItemListener(
					new ItemListener() {

						@Override
						public void itemStateChanged(ItemEvent e) {
							if (e.getStateChange() == ItemEvent.DESELECTED)
								return;

							/**
							 * Remember the selection as the default style to
							 * use when opening this map.
							 */
							if (comboBoxForAddStylesPanel.getComboBox()
									.getSelectedIndex() < 0) {
								map.setSelectedStyleID(dpLayer.getId(),
										null);
							} else {

								final String styleID = availStyleIDs
										.get(comboBoxForAddStylesPanel
												.getComboBox()
												.getSelectedIndex());
								map.setSelectedStyleID(dpLayer.getId(),
										styleID);
							}
							
							atlasMapLegend.recreateLayerList(dpLayer.getId());

						}

					});

			return comboBoxForAddStylesPanel;

		} else {
			// Let's create a JTabbedPane

			final JTabbedPane legendPanel = new JTabbedPane();

			/**
			 * Now fill the tabs...
			 */

			int count = 0;
			for (String lsID : availStyleIDs) {

				LayerStyle ls = dpLayer.getLayerStyleByID(lsID);

				JComponent oneStyleLegend = SldLegendUtil.createLegend(dpLayer,
						ls);

				legendPanel.addTab(ls.getTitle() != null ? ls.getTitle()
						.toString() : "UNNAMED!", oneStyleLegend);

				/**
				 * One of these Tabs should be selected:
				 */
				if (lsID.equals(map.getSelectedStyleID(dpLayer.getId()))) {
					legendPanel.setSelectedComponent(oneStyleLegend);
				}

				legendPanel.setToolTipTextAt(count, GpCoreUtil.R(
						"Legend.Views.TabbedPane.ToolTip", ls.getTitle()
								.toString()));

				count++;
			}

			/**
			 * This listener reacts to the tab selections. It may only be added
			 * AFTER the tabs have been created.
			 */
			if (availStyleIDs.size() > 1)
				legendPanel.addChangeListener(new ChangeListener() {

					@Override
					public void stateChanged(ChangeEvent e) {

						/**
						 * The selected Style is applied to the maplayer
						 */
						int selectedIndex = legendPanel.getSelectedIndex();
						if (selectedIndex >= 0) {
							String styleID = availStyleIDs.get(selectedIndex);
							LayerStyle layerStyle = dpLayer
									.getLayerStyleByID(styleID);
							mapLayer.setStyle(layerStyle.getStyle());
							map.setSelectedStyleID(dpLayer.getId(),
									styleID);
						}
					}

				});

			if (availStyleIDs.size() == 1) {
				return legendPanel.getComponent(0);
			} else {
				return legendPanel;
			}
		}

	}


	public static class JComboBoxForAddStylesPanel extends JPanel {
		protected Logger LOGGER = LangUtil
				.createLogger(JComboBoxForAddStylesPanel.class);

		/** The actual {@link JComboBox} **/
		private final JComboBox comboBox;

		/**
		 * @param availStyleIDs a list of additional style IDs to offer
		 * @param dpLayer {@link DpLayer} where the add. styles come from 
		 * @param mapLayer the {@link MapLayer} where the styles will be set when changed 
		 * @param selectedStyleId
		 *            The ID if the LayerStyle that should be selected by
		 *            default
		 */
		public JComboBoxForAddStylesPanel(
				final ArrayList<String> availStyleIDs, final DpLayer dpLayer,
				final MapLayer mapLayer, String selectedStyleId, final AtlasMapLegend atlasMapLegend) {
			super(new BorderLayout());

			int selectedIndex = -1;
			final Vector<String> styleTitles = new Vector<String>();
			for (int i = 0; i < availStyleIDs.size(); i++) {
				LayerStyle ls = dpLayer.getLayerStyleByID(availStyleIDs.get(i));

				if (ls == null) {
					LOGGER
							.warn("JComboBoxForAddStylesPanel has been told to offer a view with ID="
									+ availStyleIDs.get(i)
									+ ", but it doesn't exist. We omit the style.");
					continue;
				}

				styleTitles.add(ls.getTitle().toString());

				if (availStyleIDs.get(i).equals(selectedStyleId)) {
					// This style is selected. We set it as the selected layer
					// in the ComboBox later.
					selectedIndex = i;
				}
			}

			comboBox = new JComboBox(styleTitles);
			comboBox.setSelectedIndex(selectedIndex);

			// Utilities.addMouseWheelForCombobox(getComboBox());
//
//			/**
//			 * This Listener reacts to selections on the ComboBox with A)
//			 * updating the legnd, and B) updting the map's style.
//			 */
//			getComboBox().addItemListener(new ItemListener() {
//
//				@Override
//				public void itemStateChanged(ItemEvent e) {
//					if (e.getStateChange() == ItemEvent.DESELECTED) {
//						return;
//					}
//
//					String newSelectedLayerStyle = availStyleIDs.get(getComboBox()
//							.getSelectedIndex());
//					
//					map.set
//					
////					final LayerStyle ls = dpLayer
////							.getLayerStyleByID(newSelectedLayerStyle);
////					/**
////					 * Because JComboBox doesn't have all the legends as
////					 * components on the tabs, we create them
////					 */
////					{
////						JComboBoxForAddStylesPanel.this.removeAll();
////						JComboBoxForAddStylesPanel.this.add(comboBox,
////								BorderLayout.NORTH);
////						final JComponent legend = LegendHelper.createLegend(
////								dpLayer, ls);
////						JComboBoxForAddStylesPanel.this.add(legend,
////								BorderLayout.CENTER);
////					}
//					atlasMapLegend.recreateLayerList(dpLayer.getId());
//
////					mapLayer.setStyle(ls.getStyle());
//				}
//
//			});

			add(comboBox, BorderLayout.NORTH);
			add(SldLegendUtil.createLegend(dpLayer, dpLayer
					.getLayerStyleByID(selectedStyleId)), BorderLayout.CENTER);

		}

		public JComboBox getComboBox() {
			return comboBox;
		}
	}

}
