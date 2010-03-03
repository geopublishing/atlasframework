/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.sld.gui;

import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.atlas.AVUtil;
import skrueger.sld.ASUtil;
import skrueger.sld.AbstractRuleList;
import skrueger.sld.AtlasStyler;
import skrueger.sld.GraduatedColorRuleList;
import skrueger.sld.SingleRuleList;
import skrueger.sld.UniqueValuesRuleList;
import skrueger.swing.Disposable;

/**
 * This {@link JTabbedPane} offers to edit symbology or labeling for this Style.
 * 
 */
public class AtlasStylerTabbedPane extends JTabbedPane {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private final JPanel jPanelRuleListEditor = new JPanel(new MigLayout(
			"wrap 1"));

	public final static float HEADING_FONT_SIZE = 14;

	public final Icon ICON_SYMBOLOGY = new ImageIcon(
			AtlasStylerTabbedPane.class.getResource("images/symbology.png"));

	public final Icon ICON_LABELS = new ImageIcon(AtlasStylerTabbedPane.class
			.getResource("images/labels.png"));

	public static final int BUTTON_FONT_STYLE = Font.PLAIN;// TODO replace with
	// thin button
	public static final float BUTTON_FONT_SIZE = 11; // TODO replace with thin
														// button

	private final AtlasStyler atlasStyler;

	final private JLabel jLabelRuleListeTypeImage = new JLabel();

	private JComboBox jComboBoxRuleListType = null;

	// /**
	// * Default SIZE for the Sytler TABS. Very important for fucking GUI
	// */
	// public static int TAB_WIDTH = 610; // erhoeht von 425 // von 430
	//
	// public static int TAB_HEIGHT = 460; // von 450

	/**
	 * height and size of the little Images explaining the classification
	 * options.
	 */
	private int IMAGE_WIDTH_SYMBOLIZATIONICON = 95;
	private int IMAGE_HEIGHT_SYMBOLIZATIONICON = 70;

	private ClosableSubwindows lastOpenGUI;

	/**
	 * Indicated the position of this symbolization type in the
	 * {@link JComboBox}, or -1 if it doesn't exist
	 */
	private int IDX_SINGLE_SYMBOL = -1;

	/**
	 * Indicated the position of this symbolization type in the
	 * {@link JComboBox}, or -1 if it doesn't exist
	 */
	private int IDX_SINGLE_SYMBOL_CENT = -1;

	/**
	 * Indicated the position of this symbolization type in the
	 * {@link JComboBox}, or -1 if it doesn't exist
	 */
	private int IDX_UNIQUE_VALUES = -1;

	/**
	 * Indicated the position of this symbolization type in the
	 * {@link JComboBox}, or -1 if it doesn't exist
	 */
	private int IDX_UNIQUE_VALUES_CENT = -1;

	/**
	 * Indicated the position of this symbolization type in the
	 * {@link JComboBox}, or -1 if it doesn't exist
	 */
	private int IDX_COLORED_QUANTITIES = -1;

	/**
	 * Indicated the position of this symbolization type in the
	 * {@link JComboBox}, or -1 if it doesn't exist
	 */
	private int IDX_COLORED_QUANTITIES_CENT = -1;

	/**
	 * Indicated the position of this symbolization type in the
	 * {@link JComboBox}, or -1 if it doesn't exist
	 */
	private int IDX_SYMBOLIZED_QUANTITIES = -1;

	/**
	 * This is the default constructor
	 */
	public AtlasStylerTabbedPane(AtlasStyler atlasStyler) {
		this.atlasStyler = atlasStyler;

		jLabelRuleListeTypeImage.setSize(IMAGE_WIDTH_SYMBOLIZATIONICON,
				IMAGE_HEIGHT_SYMBOLIZATIONICON);

		atlasStyler.setQuite(true);
		initialize();
		atlasStyler.setQuite(false);
	}

	/**
	 * Adds the tabs to the {@link JTabbedPane}
	 */
	private void initialize() {

		addTab(AtlasStyler.R("AtlasStyler.TabbedPane.Symbology"),
				ICON_SYMBOLOGY, getSymbologyTab(), null);

		// Only allow labeling, if there is at least one attribute field
		if (ASUtil.getValueFieldNames(
				atlasStyler.getStyledFeatures().getSchema(), false).size() > 0) {
			addTab(AtlasStyler.R("AtlasStyler.TabbedPane.Labels"), ICON_LABELS,
					new TextRuleListGUI(atlasStyler.getTextRulesList(),
							atlasStyler), null);
		} else {
			AVUtil.showMessageDialog(this, AtlasStyler
					.R("TextRuleListGUI.notAvailableBecauseNoAttribsExist"));
		}
	}

	/**
	 * This tab contains the {@link JComboBox} and the settings {@link JPanel}.
	 */
	private JPanel getSymbologyTab() {
		JPanel jPanelSymbology = new JPanel(new MigLayout("wrap 1", "grow"));

		jPanelSymbology.add(getJComboBoxRuleListType(),
				"split 2, growx, growy, top");
		jPanelSymbology.add(jLabelRuleListeTypeImage);

		jPanelSymbology.add(jPanelRuleListEditor, "grow, top");
		return jPanelSymbology;
	}

	/**
	 * This method initializes jComboBoxRuleListType
	 * 
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("fallthrough")
	private JComboBox getJComboBoxRuleListType() {
		if (jComboBoxRuleListType == null) {
			jComboBoxRuleListType = new JComboBox();

			DefaultComboBoxModel cbmodel = new DefaultComboBoxModel();

			/*******************************************************************
			 * 
			 * public static int IDX_SINGLE_SYMBOL;
			 * 
			 * public static int IDX_SINGLE_SYMBOL_CENT
			 * 
			 * public static int IDX_UNIQUE_VALUES;
			 * 
			 * public static int IDX_UNIQUE_VALUES_CENT;
			 * 
			 * public static int IDX_COLORED_QUANTITIES;
			 * 
			 * public static int IDX_COLORED_QUANTITIES_CENT
			 * 
			 */

			int countEntries = 0;

			// 
			cbmodel.addElement(AtlasStyler.R("StylerSelection.single_symbol"));
			IDX_SINGLE_SYMBOL = countEntries++;

			// For polygons we also offer to style a PointSymbolizer
			if (atlasStyler.isPolygon()) {
				cbmodel.addElement(AtlasStyler
						.R("StylerSelection.single_symbol_centroids"));
				IDX_SINGLE_SYMBOL_CENT = countEntries++;
			}

			// Unique values are only available if we have any fields other than
			// geometry
			if (ASUtil.getValueFieldNames(
					atlasStyler.getStyledFeatures().getSchema()).size() > 0) {
				cbmodel.addElement(AtlasStyler
						.R("StylerSelection.categories_unique_values"));
				IDX_UNIQUE_VALUES = countEntries++;

				// For polygons we also offer to style a PointSymbolizer
				if (atlasStyler.isPolygon()) {
					cbmodel
							.addElement(AtlasStyler
									.R("StylerSelection.categories_unique_values_centroids"));
					IDX_UNIQUE_VALUES_CENT = countEntries++;
				}

			}

			if (FeatureUtil.getNumericalFieldNames(
					atlasStyler.getStyledFeatures().getSchema(), false).size() > 0) {
				{
					// Quantities classification are only available if we have
					// any numeric fields
					cbmodel.addElement(AtlasStyler
							.R("StylerSelection.quantities_colored"));
					IDX_COLORED_QUANTITIES = countEntries++;

					if (atlasStyler.isPolygon()) {
						cbmodel
								.addElement(AtlasStyler
										.R("StylerSelection.quantities_colored_centroids"));
						IDX_COLORED_QUANTITIES_CENT = countEntries++;
					}
				}
			}

			jComboBoxRuleListType.setModel(cbmodel);

			SwingUtil.addMouseWheelForCombobox(jComboBoxRuleListType, false);

			/**
			 * Set the ComboBox to the RUle that was last changed. If the
			 * atlasStyler has been fed with an SLD file, this should still
			 * work. If no lastRule is defined, create a SingleSymbol rule.
			 */
			AbstractRuleList lastChangedRuleList = atlasStyler
					.getLastChangedRuleList();
			if (lastChangedRuleList != null)
				switch (lastChangedRuleList.getTypeID()) {
				case SINGLE_SYMBOL_POINT:
					if (atlasStyler.isPolygon()) {
						jComboBoxRuleListType
								.setSelectedIndex(IDX_SINGLE_SYMBOL_CENT);
						break;
					}
				case SINGLE_SYMBOL_LINE:
				case SINGLE_SYMBOL_POLYGON:
					jComboBoxRuleListType.setSelectedIndex(IDX_SINGLE_SYMBOL);
					break;

				case UNIQUE_VALUE_POINT:
					if (atlasStyler.isPolygon()) {
						jComboBoxRuleListType
								.setSelectedIndex(IDX_UNIQUE_VALUES_CENT);
						break;
					}
				case UNIQUE_VALUE_LINE:
				case UNIQUE_VALUE_POLYGON:
					jComboBoxRuleListType.setSelectedIndex(IDX_UNIQUE_VALUES);
					break;
				//
				// case UNIQUE_VALUE_COMBINATIONS_POINT:
				// case UNIQUE_VALUE_COMBINATIONS_LINE:
				// case UNIQUE_VALUE_COMBINATIONS_POLYGONE:
				// jComboBoxRuleListType
				// .setSelectedIndex(IDX_UNIQUE_VALUE_COMBINATIONS);
				// break;

				case QUANTITIES_COLORIZED_POINT:
					if (atlasStyler.isPolygon()) {
						jComboBoxRuleListType
								.setSelectedIndex(IDX_COLORED_QUANTITIES_CENT);
						break;
					}
				case QUANTITIES_COLORIZED_LINE:
				case QUANTITIES_COLORIZED_POLYGON:
					jComboBoxRuleListType
							.setSelectedIndex(IDX_COLORED_QUANTITIES);
					break;

				// case QUANTITIES_SIZED_POINT:
				// case QUANTITIES_SIZED_LINE:
				// jComboBoxRuleListType
				// .setSelectedIndex(IDX_SYMBOLIZED_QUANTITIES);
				// break;

				}
			else {
				LOGGER
						.info("lastChangedRuleList == null => Selecting a default = IDX_SINGLE_SYMBOL:");
				jComboBoxRuleListType.setSelectedIndex(IDX_SINGLE_SYMBOL);
			}

			// Update once for the initial GUI
			updateGuiForSelectedIndex();

			// Add a listener for further udpates
			/** * Listens to Selections to open the according GUIs ** */
			jComboBoxRuleListType.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() != ItemEvent.SELECTED) {
						return;
					}

					LOGGER
							.debug("A selection was made on the jComboBoxRuleListType");
					updateGuiForSelectedIndex();

					// TODO Maybe we can remove this... Check if all
					// updateGuiForSelectedIndex end up with their own
					// fireStyleChangedEvents
					atlasStyler.fireStyleChangedEvents();
				}

			});

		}
		return jComboBoxRuleListType;
	}

	/**
	 * Updates the GUI with the new Options. This routine uses {@link #selIdx}
	 */
	protected void updateGuiForSelectedIndex() {

		int selIdx = jComboBoxRuleListType.getSelectedIndex();
		if (selIdx < 0) {
			LOGGER
					.debug("not changing the GUI, because the newly selected index is -1");
			return;
		}

		JComponent gui = null;

		/** Update the GUI * */
		if (jPanelRuleListEditor.getComponentCount() > 0) {
			((Disposable) jPanelRuleListEditor.getComponent(0)).dispose();
		}
		
		String imageName = null;

		if (selIdx == IDX_SINGLE_SYMBOL) {
			/**
			 * Single Symbol has been selected. This entry is dependent on the
			 * Geometry type
			 */

			SingleRuleList<?> ruleList = null;

			if (atlasStyler.isPoint()) {
				/**
				 * We have Points!
				 */
				ruleList = atlasStyler.getSinglePointSymbolRulesList();
				imageName = "images/single_point_symbol.png";

			} else if (atlasStyler.isLineString()) {
				/**
				 * We have Lines!
				 */
				ruleList = atlasStyler.getSingleLineSymbolRulesList();
				imageName = "images/single_line_symbol.png";

			} else if (atlasStyler.isPolygon()) {
				/**
				 * We have Polygons! There should actually be two buttons.. one
				 * for the polygonsymbilzers, one for the pointsymbolizers
				 */
				ruleList = atlasStyler.getSinglePolygonSymbolRulesList();
				imageName = "images/single_polygon_symbol.png";
			}

			atlasStyler.setLastChangedRuleList(ruleList);
			gui = new SingleSymbolGUI(ruleList);

		} else

		if (selIdx == IDX_SINGLE_SYMBOL_CENT) {
			// Only happens with polygons
			SingleRuleList<?> ruleList = atlasStyler
					.getSinglePointSymbolRulesList();
			imageName = "images/single_point_symbol.png";
			gui = new SingleSymbolGUI(ruleList);
		} else

		if (selIdx == IDX_UNIQUE_VALUES) {

			UniqueValuesRuleList catRuleList = null;

			if (atlasStyler.isPoint()) {
				catRuleList = atlasStyler.getUniqueValuesPointRulesList();
				imageName = "images/point_unique_values.png";
			} else if (atlasStyler.isLineString()) {
				catRuleList = atlasStyler.getUniqueValuesLineRulesList();
				imageName = "images/line_unique_values.png";
			} else if (atlasStyler.isPolygon()) {
				catRuleList = atlasStyler.getUniqueValuesPolygonRuleList();
				imageName = "images/polygon_unique_values.png";
			}
			atlasStyler.setLastChangedRuleList(catRuleList);
			gui = new UniqueValuesGUI(catRuleList, atlasStyler);

		} else

		if (selIdx == IDX_UNIQUE_VALUES_CENT) {

			UniqueValuesRuleList catRuleList = null;
			catRuleList = atlasStyler.getUniqueValuesPointRulesList();
			imageName = "images/point_unique_values.png";
			atlasStyler.setLastChangedRuleList(catRuleList);
			gui = new UniqueValuesGUI(catRuleList, atlasStyler);

		} else

		if (selIdx == IDX_COLORED_QUANTITIES_CENT) {
			// Only happens with polygons

			GraduatedColorRuleList graduatedColorRuleList = atlasStyler
					.getGraduatedColorRuleList(ASUtil.getDefaultPointTemplate()
							.getGeometryDescriptor());

			atlasStyler.setLastChangedRuleList(graduatedColorRuleList);
			gui = new GraduatedColorQuantitiesGUI(graduatedColorRuleList,
					atlasStyler);

			imageName = "images/point_graduated_colors.png";
		} else

		if (selIdx == IDX_COLORED_QUANTITIES) {

			GraduatedColorRuleList graduatedColorRuleList = atlasStyler
					.getGraduatedColorRuleList(atlasStyler.getStyledFeatures()
							.getSchema().getGeometryDescriptor());

			atlasStyler.setLastChangedRuleList(graduatedColorRuleList);
			gui = new GraduatedColorQuantitiesGUI(graduatedColorRuleList,
					atlasStyler);

			if (atlasStyler.isPoint()) {
				imageName = "images/point_graduated_colors.png";
			} else if (atlasStyler.isLineString()) {
				imageName = "images/line_graduated_colors.png";
			} else if (atlasStyler.isPolygon()) {
				imageName = "images/polygon_graduated_colors.png";
			}
		}

		else

		if (selIdx == IDX_SYMBOLIZED_QUANTITIES) {
			// /**
			// * Symbolized Quantities can't exist for LINES!
			// */

		} else {
			jLabelRuleListeTypeImage.setIcon(null);
		}

		/***********************************************************************
		 * Close open windows of the old gui
		 */
		if (gui instanceof ClosableSubwindows) {
			lastOpenGUI = (ClosableSubwindows) gui;
		} else {
			lastOpenGUI = null;
		}

		/***********************************************************************
		 * Update using the GUI we just created...
		 */

			jPanelRuleListEditor.removeAll();
			jPanelRuleListEditor.add(gui, "top, grow");

			/** Replace the preview image * */
			if (imageName != null) {
				URL resource = getClass().getResource(imageName);
				if (resource != null) {
					jLabelRuleListeTypeImage.setIcon(new ImageIcon(resource));
				} else {

				}
			}

			// Noe.. erstmal nicht mehr packen
			if (AtlasStylerTabbedPane.this != null) {
				if (SwingUtil.getParentWindow(AtlasStylerTabbedPane.this) != null)
					SwingUtil.getParentWindow(AtlasStylerTabbedPane.this)
							.pack();

			}
	}

	public void dispose() {
		if (jComboBoxRuleListType != null) {
			jComboBoxRuleListType.removeAllItems();
			jComboBoxRuleListType = null;
		}
		lastOpenGUI.dispose();
	}

}
