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
package org.geopublishing.atlasStyler.swing;

import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RasterRulesList_Intervals;
import org.geopublishing.atlasStyler.RulesListsList;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList;
import org.geopublishing.atlasStyler.rulesLists.GraduatedColorRuleList;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesList_DistinctValues;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesList_Ramps;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;
import org.geopublishing.atlasStyler.rulesLists.TextRuleList;
import org.geopublishing.atlasStyler.rulesLists.UniqueValuesRuleList;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.JPanel;

/**
 * {@link JTabbedPane} offers a RulesListTable and a GUI to edit it on the right
 * side.
 * 
 */
public class AtlasStylerPane extends JSplitPane implements ClosableSubwindows {
	protected Logger LOGGER = LangUtil.createLogger(this);

	/**
	 * Caches the GUIs for the {@link RulesListsList}s
	 */
	private final HashMap<AbstractRulesList, JComponent> cachedRulesListGuis = new HashMap<AbstractRulesList, JComponent>();

	private final JPanel jPanelRuleListEditor = new JPanel(new MigLayout(
			"wrap 1, fill"));

	// public final Icon ICON_SYMBOLOGY = new ImageIcon(
	// AtlasStylerPane.class.getResource("/images/symbology.png"));
	//
	// public final Icon ICON_LABELS = new ImageIcon(
	// AtlasStylerPane.class.getResource("/images/labels.png"));

	private final AtlasStyler atlasStyler;

	/**
	 * height and size of the little Images explaining the classification
	 * options.
	 */
	// ClosableSubwindows ?
	private JComponent lastOpenEditorGUI;

	private RulesListsListTablePanel rulesListsListTablePanel;

	private final ListSelectionListener listenToSelectionInTable = new ListSelectionListener() {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int selectedRow = getRulesListsListTablePanel().getRulesListTable()
					.getSelectedRow();
			if (selectedRow >= 0) {
				AbstractRulesList abstractRuleList = atlasStyler.getRuleLists()
						.get(selectedRow);
				changeEditorComponent(abstractRuleList);
			} else {
				if (lastOpenEditorGUI instanceof ClosableSubwindows) {
					((ClosableSubwindows) lastOpenEditorGUI).dispose();
				}
				changeEditorComponent(new JLabel("select something on the left"));
			}
		}
	};

	private final StylerDialog asd;

	public AtlasStylerPane(StylerDialog stylerDialog) {
		this.asd = stylerDialog;
		this.atlasStyler = stylerDialog.getAtlasStyler();
		initialize();

		atlasStyler.setQuite(true);

		// Create all GUIs and cache them.
		{
			for (AbstractRulesList ruleList : atlasStyler.getRuleLists()) {
				createEditorComponent(ruleList);
			}
		}

		getRulesListsListTablePanel().getRulesListTable().getSelectionModel()
				.addListSelectionListener(listenToSelectionInTable);

		if (getRulesListsListTablePanel().getRulesListTable().getSelectedRow() != -1) {
			int s1 = getRulesListsListTablePanel().getRulesListTable()
					.getSelectedRow();
			// Wenn eine RL selektiert ist, dann nochmal selektieren, damit die
			// GUI sich aktualisiert
			getRulesListsListTablePanel().getRulesListTable()
					.getSelectionModel().clearSelection();
			getRulesListsListTablePanel().getRulesListTable()
					.getSelectionModel().addSelectionInterval(s1, s1);
		}

		atlasStyler.setQuite(false);

	}

	public void changeEditorComponent(JComponent newComponent) {
		jPanelRuleListEditor.removeAll();
		jPanelRuleListEditor.add(newComponent, "top, grow");
		lastOpenEditorGUI = newComponent;
		invalidate();
		validate();
		repaint();
		// Window owner = schmitzm.swing.SwingUtil.getParentWindow(this);
		// if (owner != null)
		// schmitzm.swing.SwingUtil.getParentWindow(this).pack();
	};

	/**
	 * SHows the cached GUI for a {@link AbstractRulesList}. If not cached, a
	 * new instance will be cached.
	 */
	public void changeEditorComponent(AbstractRulesList ruleList) {

		JComponent newEditorGui = cachedRulesListGuis.get(ruleList);
		if (newEditorGui == null) {
			newEditorGui = createEditorComponent(ruleList);
		}

		changeEditorComponent(newEditorGui);
	};

	/**
	 * Creates a new GUI for a {@link AbstractRulesList} and caches it.
	 */
	public JComponent createEditorComponent(AbstractRulesList ruleList) {

		JComponent newEditorGui = new JLabel();

		// Vector RulesLists:
		if (ruleList instanceof SingleRuleList)
			newEditorGui = new SingleSymbolGUI((SingleRuleList<?>) ruleList);
		else if (ruleList instanceof UniqueValuesRuleList)
			newEditorGui = new UniqueValuesGUI((UniqueValuesRuleList) ruleList,
					(AtlasStylerVector) atlasStyler);
		else if (ruleList instanceof GraduatedColorRuleList)
			newEditorGui = new GraduatedColorQuantitiesGUI(
					(GraduatedColorRuleList) ruleList,
					(AtlasStylerVector) atlasStyler);
		else if (ruleList instanceof TextRuleList)
			newEditorGui = new TextRuleListGUI((TextRuleList) ruleList,
					(AtlasStylerVector) atlasStyler);
		// Raster RulesLists
		else if (ruleList instanceof RasterRulesList_DistinctValues) {
			newEditorGui = new RasterRulesList_Distinctvalues_GUI(
					(RasterRulesList_DistinctValues) ruleList,
					(AtlasStylerRaster) atlasStyler);
		} else if (ruleList instanceof RasterRulesList_Intervals) {
			newEditorGui = new RasterRulesList_Intervals_GUI(
					(RasterRulesList_Intervals) ruleList,
					(AtlasStylerRaster) atlasStyler);
//		} else if (ruleList instanceof RasterRulesListRGB) {
//			newEditorGui = new RasterRulesList_RGB_GUI(
//					(RasterRulesListRGB) ruleList,
//					(AtlasStylerRaster) atlasStyler);
		} else if (ruleList instanceof RasterRulesList_Ramps) {
			newEditorGui = new JLabel(
					"<html>ColorMap.RAMPS not yet supported. Please use the old raster-styler in Geopublisher for this type of raster style.</html>");
		}

		cachedRulesListGuis.put(ruleList, newEditorGui);

		return newEditorGui;
	};

	private JComponent createStatusLabel() {
		String statusText = "<html>";

		if (atlasStyler instanceof AtlasStylerVector) {
			// vector specific error messages:

			if (FeatureUtil.getValueFieldNames(
					((AtlasStylerVector) atlasStyler).getStyledFeatures()
							.getSchema()).size() == 0) {
				statusText += AtlasStylerVector
						.R("TextRuleListGUI.notAvailableBecauseNoAttribsExist")
						+ "<br/>";
			}

		} else {
			// raster specific error messages

		}

		// Go through the AtlasStyler import errors:
		for (Exception e : atlasStyler.getImportErrorLog()) {
			statusText += "<p>";
			statusText += e.getLocalizedMessage();
			statusText += "</p>";
		}

		statusText += "<p><em>Select a RuleList from the left, or add a new RuleList.</em></p>";

		statusText += "</html>";

		return new JLabel(statusText);
	}

	/**
	 * Adds the tabs to the {@link JTabbedPane}
	 */
	private void initialize() {
		setOneTouchExpandable(true);

		// setLayout(new MigLayout("top", "[grow]", "[grow]"));

		// add(getRulesListsListTablePanel(), "width 200:200:400, top, growx");
		setLeftComponent(getRulesListsListTablePanel());

		// add(jPanelRuleListEditor, "width 400:650:900, top, growx 200");
		setRightComponent(jPanelRuleListEditor);
		changeEditorComponent(createStatusLabel());

		setDividerLocation(-1);
	}

	private RulesListsListTablePanel getRulesListsListTablePanel() {
		if (rulesListsListTablePanel == null) {
			rulesListsListTablePanel = new RulesListsListTablePanel(asd);
		}
		return rulesListsListTablePanel;
	}

	// /**
	// * This tab contains the {@link JComboBox} and the settings {@link
	// JPanel}.
	// */
	// private JPanel getSymbologyTab() {
	// JPanel jPanelSymbology = new JPanel(new MigLayout("wrap 1", "grow",
	// "grow"));
	//
	// jPanelSymbology.add(getJComboBoxRuleListType(),
	// "split 2, growx, height 50, top");
	// jPanelSymbology.add(jLabelRuleListeTypeImage, "top");
	//
	// jPanelSymbology.add(jPanelRuleListEditor, "grow, top");
	// return jPanelSymbology;
	// }
	//
	// /**
	// * This method initializes jComboBoxRuleListType
	// *
	// * @return javax.swing.JComboBox
	// */
	// @SuppressWarnings("fallthrough")
	// private JComboBox getJComboBoxRuleListType() {
	// if (jComboBoxRuleListType == null) {
	// jComboBoxRuleListType = new JComboBox();
	//
	// DefaultComboBoxModel cbmodel = new DefaultComboBoxModel();
	//
	// /*******************************************************************
	// *
	// * public static int IDX_SINGLE_SYMBOL;
	// *
	// * public static int IDX_SINGLE_SYMBOL_CENT
	// *
	// * public static int IDX_UNIQUE_VALUES;
	// *
	// * public static int IDX_UNIQUE_VALUES_CENT;
	// *
	// * public static int IDX_COLORED_QUANTITIES;
	// *
	// * public static int IDX_COLORED_QUANTITIES_CENT
	// *
	// */
	//
	// int countEntries = 0;
	//
	// //
	// cbmodel.addElement(AtlasStyler.R("StylerSelection.single_symbol"));
	// IDX_SINGLE_SYMBOL = countEntries++;
	//
	// // For polygons we also offer to style a PointSymbolizer
	// if (atlasStyler.isPolygon()) {
	// cbmodel.addElement(AtlasStyler
	// .R("StylerSelection.single_symbol_centroids"));
	// IDX_SINGLE_SYMBOL_CENT = countEntries++;
	// }
	//
	// // Unique values are only available if we have any fields other than
	// // geometry
	// if (FeatureUtil.getValueFieldNames(
	// atlasStyler.getStyledFeatures().getSchema()).size() > 0) {
	// cbmodel.addElement(AtlasStyler
	// .R("StylerSelection.categories_unique_values"));
	// IDX_UNIQUE_VALUES = countEntries++;
	//
	// // For polygons we also offer to style a PointSymbolizer
	// if (atlasStyler.isPolygon()) {
	// cbmodel.addElement(AtlasStyler
	// .R("StylerSelection.categories_unique_values_centroids"));
	// IDX_UNIQUE_VALUES_CENT = countEntries++;
	// }
	//
	// }
	//
	// if (FeatureUtil.getNumericalFieldNames(
	// atlasStyler.getStyledFeatures().getSchema(), false, true)
	// .size() > 0) {
	// {
	// // Quantities classification are only available if we have
	// // any numeric fields
	// cbmodel.addElement(AtlasStyler
	// .R("StylerSelection.quantities_colored"));
	// IDX_COLORED_QUANTITIES = countEntries++;
	//
	// if (atlasStyler.isPolygon()) {
	// cbmodel.addElement(AtlasStyler
	// .R("StylerSelection.quantities_colored_centroids"));
	// IDX_COLORED_QUANTITIES_CENT = countEntries++;
	// }
	// }
	// }
	//
	// jComboBoxRuleListType.setModel(cbmodel);
	//
	// SwingUtil.addMouseWheelForCombobox(jComboBoxRuleListType, false);
	//
	// /**
	// * Set the ComboBox to the RUle that was last changed. If the
	// * atlasStyler has been fed with an SLD file, this should still
	// * work. If no lastRule is defined, create a SingleSymbol rule.
	// */
	// AbstractRuleList lastChangedRuleList = atlasStyler
	// .getLastChangedRuleList();
	// if (lastChangedRuleList != null)
	// switch (lastChangedRuleList.getTypeID()) {
	// case SINGLE_SYMBOL_POINT:
	// if (atlasStyler.isPolygon()) {
	// jComboBoxRuleListType
	// .setSelectedIndex(IDX_SINGLE_SYMBOL_CENT);
	// break;
	// }
	// case SINGLE_SYMBOL_LINE:
	// case SINGLE_SYMBOL_POLYGON:
	// jComboBoxRuleListType.setSelectedIndex(IDX_SINGLE_SYMBOL);
	// break;
	//
	// case UNIQUE_VALUE_POINT:
	// if (atlasStyler.isPolygon()) {
	// jComboBoxRuleListType
	// .setSelectedIndex(IDX_UNIQUE_VALUES_CENT);
	// break;
	// }
	// case UNIQUE_VALUE_LINE:
	// case UNIQUE_VALUE_POLYGON:
	// jComboBoxRuleListType.setSelectedIndex(IDX_UNIQUE_VALUES);
	// break;
	// //
	// // case UNIQUE_VALUE_COMBINATIONS_POINT:
	// // case UNIQUE_VALUE_COMBINATIONS_LINE:
	// // case UNIQUE_VALUE_COMBINATIONS_POLYGONE:
	// // jComboBoxRuleListType
	// // .setSelectedIndex(IDX_UNIQUE_VALUE_COMBINATIONS);
	// // break;
	//
	// case QUANTITIES_COLORIZED_POINT:
	// if (atlasStyler.isPolygon()) {
	// jComboBoxRuleListType
	// .setSelectedIndex(IDX_COLORED_QUANTITIES_CENT);
	// break;
	// }
	// case QUANTITIES_COLORIZED_LINE:
	// case QUANTITIES_COLORIZED_POLYGON:
	// jComboBoxRuleListType
	// .setSelectedIndex(IDX_COLORED_QUANTITIES);
	// break;
	//
	// // case QUANTITIES_SIZED_POINT:
	// // case QUANTITIES_SIZED_LINE:
	// // jComboBoxRuleListType
	// // .setSelectedIndex(IDX_SYMBOLIZED_QUANTITIES);
	// // break;
	//
	// }
	// else {
	// LOGGER.info("lastChangedRuleList == null => Selecting a default = IDX_SINGLE_SYMBOL:");
	// jComboBoxRuleListType.setSelectedIndex(IDX_SINGLE_SYMBOL);
	// }
	//
	// // Update once for the initial GUI
	// updateGuiForSelectedIndex();
	//
	// // Add a listener for further udpates
	// /** * Listens to Selections to open the according GUIs ** */
	// jComboBoxRuleListType.addItemListener(new ItemListener() {
	//
	// @Override
	// public void itemStateChanged(ItemEvent e) {
	// if (e.getStateChange() != ItemEvent.SELECTED) {
	// return;
	// }
	//
	// LOGGER.debug("A selection was made on the jComboBoxRuleListType");
	// updateGuiForSelectedIndex();
	//
	// // TODO Maybe we can remove this... Check if all
	// // updateGuiForSelectedIndex end up with their own
	// // fireStyleChangedEvents
	// atlasStyler.fireStyleChangedEvents();
	// }
	//
	// });
	//
	// }
	// return jComboBoxRuleListType;
	// }
	//
	// /**
	// * Updates the GUI with the new Options. This routine uses {@link #selIdx}
	// */
	// @Deprecated
	// protected void updateGuiForSelectedIndex() {
	//
	// int selIdx = jComboBoxRuleListType.getSelectedIndex();
	// if (selIdx < 0) {
	// LOGGER.debug("not changing the GUI, because the newly selected index is -1");
	// return;
	// }
	//
	// JComponent gui = null;
	//
	// /** Update the GUI * */
	// if (jPanelRuleListEditor.getComponentCount() > 0) {
	// ((Disposable) jPanelRuleListEditor.getComponent(0)).dispose();
	// }
	//
	// String imageName = null;
	//
	// if (selIdx == IDX_SINGLE_SYMBOL) {
	// /**
	// * Single Symbol has been selected. This entry is dependent on the
	// * Geometry type
	// */
	//
	// SingleRuleList<?> ruleList = null;
	//
	// if (atlasStyler.isPoint()) {
	// /**
	// * We have Points!
	// */
	// ruleList = atlasStyler.getSinglePointSymbolRulesList();
	// imageName = "/images/single_point_symbol.png";
	//
	// } else if (atlasStyler.isLineString()) {
	// /**
	// * We have Lines!
	// */
	// ruleList = atlasStyler.getSingleLineSymbolRulesList();
	// imageName = "/images/single_line_symbol.png";
	//
	// } else if (atlasStyler.isPolygon()) {
	// /**
	// * We have Polygons! There should actually be two buttons.. one
	// * for the polygonsymbilzers, one for the pointsymbolizers
	// */
	// ruleList = atlasStyler.getSinglePolygonSymbolRulesList();
	// imageName = "/images/single_polygon_symbol.png";
	// }
	//
	// atlasStyler.setLastChangedRuleList(ruleList);
	// gui = new SingleSymbolGUI(ruleList);
	//
	// } else
	//
	// if (selIdx == IDX_SINGLE_SYMBOL_CENT) {
	// // Only happens with polygons
	// SingleRuleList<?> ruleList = atlasStyler
	// .getSinglePointSymbolRulesList();
	// imageName = "/images/single_point_symbol.png";
	// gui = new SingleSymbolGUI(ruleList);
	// } else
	//
	// if (selIdx == IDX_UNIQUE_VALUES) {
	//
	// UniqueValuesRuleList catRuleList = null;
	//
	// if (atlasStyler.isPoint()) {
	// catRuleList = atlasStyler.getUniqueValuesPointRulesList();
	// imageName = "/images/point_unique_values.png";
	// } else if (atlasStyler.isLineString()) {
	// catRuleList = atlasStyler.getUniqueValuesLineRulesList();
	// imageName = "/images/line_unique_values.png";
	// } else if (atlasStyler.isPolygon()) {
	// catRuleList = atlasStyler.getUniqueValuesPolygonRuleList();
	// imageName = "/images/polygon_unique_values.png";
	// }
	// atlasStyler.setLastChangedRuleList(catRuleList);
	// gui = new UniqueValuesGUI(catRuleList, atlasStyler);
	//
	// } else
	//
	// if (selIdx == IDX_UNIQUE_VALUES_CENT) {
	//
	// UniqueValuesRuleList catRuleList = null;
	// catRuleList = atlasStyler.getUniqueValuesPointRulesList();
	// imageName = "/images/point_unique_values.png";
	// atlasStyler.setLastChangedRuleList(catRuleList);
	// gui = new UniqueValuesGUI(catRuleList, atlasStyler);
	//
	// } else
	//
	// if (selIdx == IDX_COLORED_QUANTITIES_CENT) {
	// // Only happens with polygons
	//
	// GraduatedColorRuleList graduatedColorRuleList = atlasStyler
	// .getGraduatedColorRuleList(ASUtil.getDefaultPointTemplate()
	// .getGeometryDescriptor());
	//
	// atlasStyler.setLastChangedRuleList(graduatedColorRuleList);
	// gui = new GraduatedColorQuantitiesGUI(graduatedColorRuleList,
	// atlasStyler);
	//
	// imageName = "/images/point_graduated_colors.png";
	// } else
	//
	// if (selIdx == IDX_COLORED_QUANTITIES) {
	//
	// GraduatedColorRuleList graduatedColorRuleList = atlasStyler
	// .getGraduatedColorRuleList(atlasStyler.getStyledFeatures()
	// .getSchema().getGeometryDescriptor());
	//
	// atlasStyler.setLastChangedRuleList(graduatedColorRuleList);
	// gui = new GraduatedColorQuantitiesGUI(graduatedColorRuleList,
	// atlasStyler);
	//
	// if (atlasStyler.isPoint()) {
	// imageName = "/images/point_graduated_colors.png";
	// } else if (atlasStyler.isLineString()) {
	// imageName = "/images/line_graduated_colors.png";
	// } else if (atlasStyler.isPolygon()) {
	// imageName = "/images/polygon_graduated_colors.png";
	// }
	// }
	//
	// else
	//
	// if (selIdx == IDX_SYMBOLIZED_QUANTITIES) {
	// // /**
	// // * Symbolized Quantities can't exist for LINES!
	// // */
	//
	// } else {
	// jLabelRuleListeTypeImage.setIcon(null);
	// }
	//
	// /***********************************************************************
	// * Close open windows of the old gui
	// */
	// if (gui instanceof ClosableSubwindows) {
	// lastOpenGUI = (ClosableSubwindows) gui;
	// lastOpenGUI.dispose();
	// } else {
	// lastOpenGUI = null;
	// }
	//
	// /***********************************************************************
	// * Update using the GUI we just created...
	// */
	//
	// jPanelRuleListEditor.removeAll();
	// jPanelRuleListEditor.add(gui, "top, grow");
	//
	// /** Replace the preview image * */
	// if (imageName != null) {
	// URL resource = getClass().getResource(imageName);
	// if (resource != null) {
	// jLabelRuleListeTypeImage.setIcon(new ImageIcon(resource));
	// } else {
	//
	// }
	// }
	//
	// // Noe.. erstmal nicht mehr packen
	// if (AtlasStylerPane.this != null) {
	// if (SwingUtil.getParentWindow(AtlasStylerPane.this) != null)
	// SwingUtil.getParentWindow(AtlasStylerPane.this).pack();
	//
	// }
	// }

	public void dispose() {

		for (JComponent comp : cachedRulesListGuis.values()) {
			if (comp != null && comp instanceof ClosableSubwindows)
				((ClosableSubwindows) comp).dispose();
		}
		cachedRulesListGuis.clear();
	}

}
