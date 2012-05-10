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
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RulesListsList;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList;
import org.geopublishing.atlasStyler.rulesLists.GraduatedColorRuleList;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesListRGB;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesList_DistinctValues;
import org.geopublishing.atlasStyler.rulesLists.RasterRulesList_Intervals;
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
			newEditorGui = new SingleSymbolGUI((AtlasStylerVector) atlasStyler, (SingleRuleList<?>) ruleList);
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
		} else if (ruleList instanceof RasterRulesListRGB) {
			newEditorGui = new RasterRulesList_RGB_GUI(
					(RasterRulesListRGB) ruleList,
					(AtlasStylerRaster) atlasStyler);
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
				statusText += ASUtil
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

	public void dispose() {

		for (JComponent comp : cachedRulesListGuis.values()) {
			if (comp != null && comp instanceof ClosableSubwindows)
				((ClosableSubwindows) comp).dispose();
		}
		cachedRulesListGuis.clear();
	}

}
