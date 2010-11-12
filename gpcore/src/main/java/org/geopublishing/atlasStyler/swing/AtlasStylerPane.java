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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AbstractRuleList;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.GraduatedColorRuleList;
import org.geopublishing.atlasStyler.SingleRuleList;
import org.geopublishing.atlasStyler.TextRuleList;
import org.geopublishing.atlasStyler.UniqueValuesRuleList;

import schmitzm.swing.JPanel;

/**
 * {@link JTabbedPane} offers a RulesListTable and a GUI to edit it on the right
 * side.
 * 
 */
public class AtlasStylerPane extends JPanel implements ClosableSubwindows {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private final JPanel jPanelRuleListEditor = new JPanel(new MigLayout(
			"wrap 1"));

	public final Icon ICON_SYMBOLOGY = new ImageIcon(
			AtlasStylerPane.class.getResource("/images/symbology.png"));

	public final Icon ICON_LABELS = new ImageIcon(
			AtlasStylerPane.class.getResource("/images/labels.png"));

	private final AtlasStyler atlasStyler;

	// final private JLabel jLabelRuleListeTypeImage = new JLabel();

	// private JComboBox jComboBoxRuleListType = null;

	/**
	 * height and size of the little Images explaining the classification
	 * options.
	 */
	private int IMAGE_WIDTH_SYMBOLIZATIONICON = 95;
	private int IMAGE_HEIGHT_SYMBOLIZATIONICON = 70;

	// ClosableSubwindows ?
	private JComponent lastOpenEditorGUI;

	private RulesListTablePanel rulesListsListTablePanel;

	private ListSelectionListener listenToSelectionInTable = new ListSelectionListener() {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int selectedRow = getRulesListsListTablePanel().getRulesListTable()
					.getSelectedRow();
			if (selectedRow >= 0) {
				AbstractRuleList abstractRuleList = atlasStyler.getRuleLists()
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

	public AtlasStylerPane(AtlasStyler atlasStyler) {

		this.atlasStyler = atlasStyler;
		//
		// jLabelRuleListeTypeImage.setSize(IMAGE_WIDTH_SYMBOLIZATIONICON,
		// IMAGE_HEIGHT_SYMBOLIZATIONICON);

		atlasStyler.setQuite(true);
		initialize();
		getRulesListsListTablePanel().getRulesListTable().getSelectionModel()
				.addListSelectionListener(listenToSelectionInTable);
		atlasStyler.setQuite(false);
	}

	public void changeEditorComponent(JComponent newComponent) {
		jPanelRuleListEditor.removeAll();
		jPanelRuleListEditor.add(newComponent, "top, grow");
		lastOpenEditorGUI = newComponent;
		invalidate();
		// repaint();
		validate();
	};

	public void changeEditorComponent(AbstractRuleList ruleList) {
		JComponent newEditorGui = new JLabel();
		if (ruleList instanceof SingleRuleList)
			newEditorGui = new SingleSymbolGUI((SingleRuleList<?>) ruleList);
		else if (ruleList instanceof UniqueValuesRuleList)
			newEditorGui = new UniqueValuesGUI((UniqueValuesRuleList) ruleList,
					atlasStyler);
		else if (ruleList instanceof GraduatedColorRuleList)
			newEditorGui = new GraduatedColorQuantitiesGUI(
					(GraduatedColorRuleList) ruleList, atlasStyler);
		else if (ruleList instanceof TextRuleList)
			newEditorGui = new TextRuleListGUI((TextRuleList) ruleList,
					atlasStyler);

		changeEditorComponent(newEditorGui);
	};

	/**
	 * Adds the tabs to the {@link JTabbedPane}
	 */
	private void initialize() {

		setLayout(new MigLayout());

		add(getRulesListsListTablePanel());
		// add(new JScrollPane(getRulesListTable()));

		// add(getButtonsPanel(), "wrap");

		add(jPanelRuleListEditor, "width :500:700");

		// addTab(AtlasStyler.R("AtlasStylerGUI.TabbedPane.Symbology"),
		// ICON_SYMBOLOGY, getSymbologyTab(), null);
		//
		// // Only allow labeling, if there is at least one attribute field
		// if (ASUtil.getValueFieldNames(
		// atlasStyler.getStyledFeatures().getSchema()).size() > 0) {
		//
		// addTab(AtlasStyler.R("AtlasStylerGUI.TabbedPane.Labels"),
		// ICON_LABELS,
		// new TextRuleListGUI(atlasStyler.getTextRulesList(),
		// atlasStyler), null);
		// } else {
		// AVSwingUtil.showMessageDialog(this, AtlasStyler
		// .R("TextRuleListGUI.notAvailableBecauseNoAttribsExist"));
		// }
	}

	//
	// private JPanel getButtonsPanel() {
	// JPanel buttonsPanel = new JPanel();
	//
	// buttonsPanel.add(getAddButton())
	//
	// return buttonsPanel;
	// }

	private RulesListTablePanel getRulesListsListTablePanel() {
		if (rulesListsListTablePanel == null) {
			rulesListsListTablePanel = new RulesListTablePanel(atlasStyler);
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
	// if (ASUtil.getValueFieldNames(
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

		if (lastOpenEditorGUI != null
				&& lastOpenEditorGUI instanceof ClosableSubwindows)
			((ClosableSubwindows) lastOpenEditorGUI).dispose();
	}

}
