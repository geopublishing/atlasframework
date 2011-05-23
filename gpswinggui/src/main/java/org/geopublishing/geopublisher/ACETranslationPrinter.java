/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.geopublisher;

import javax.swing.tree.TreeNode;

import org.apache.commons.lang.StringEscapeUtils;
import org.geopublishing.atlasViewer.dp.DpEntry;
import org.geopublishing.atlasViewer.dp.Group;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVector;
import org.geopublishing.atlasViewer.dp.layer.DpLayerVectorFeatureSource;
import org.geopublishing.atlasViewer.dp.layer.LayerStyle;
import org.geopublishing.geopublisher.swing.GeopublisherGUI;

import de.schmitzm.geotools.data.amd.AttributeMetadataImpl;
import de.schmitzm.geotools.data.amd.AttributeMetadataMap;
import de.schmitzm.i18n.Translation;
import de.schmitzm.jfree.chart.style.ChartStyle;
import de.schmitzm.lang.LangUtil;

public class ACETranslationPrinter {
	String ERROR = "ERROR ";
	String WAY = " ->";
	private final AtlasConfigEditable ace;
	private StringBuffer txt;

	public ACETranslationPrinter(AtlasConfigEditable ace) {
		this.ace = ace;
	}

	/**
	 * This method can take a while and produce disk traffic.
	 * 
	 * @return a {@link String} containing a human-readable summary of all {@link Translation}s used.
	 */
	public String printAllTranslations() {
		txt = new StringBuffer(4000);

		/**
		 * AtlasParams
		 */
		printWayH1(R("ACETranslationPrinter.Heading.GeneralInformation"));
		printWayH2(R("MenuBar.AtlasMenu"), R("MenuBar.AtlasMenu.ChangeAtlasParams"));
		printTranslation(ace.getTitle(), "AtlasParamsTranslationDialog.Title");
		printTranslation(ace.getDesc(), "AtlasParamsTranslationDialog.Description");
		printTranslation(ace.getCreator(), "AtlasParamsTranslationDialog.Creator");
		printTranslation(ace.getCopyright(), "AtlasParamsTranslationDialog.Copyright");

		/**
		 * DataPool
		 */
		printWayH1(R("DataPoolJTable.Border.Title"));
		for (DpEntry<? extends ChartStyle> dpe : ace.getDataPool().values()) {

			printWayH2(R("DataPoolJTable.Border.Title"), R("DataPoolJTable.ColumnName.Filename"), dpe.getFilename(),
					R("DataPoolWindow_Action_EditDPE_label"));
			printTranslation(dpe.getTitle(), "EditDPEDialog.TranslateTitle");
			printTranslation(dpe.getDesc(), "EditDPEDialog.TranslateDescription");
			printTranslation(dpe.getKeywords(), "EditDPEDialog.TranslateKeywords");

			if (dpe instanceof DpLayerVector<?, ?>) {

				/**
				 * List additional layer styles
				 */

				DpLayerVector<?, ?> dplv = (DpLayerVector<?, ?>) dpe;
				for (LayerStyle ls : dplv.getLayerStyles()) {
					printWayH3(R("DataPoolJTable.Border.Title"), "\"<b>" + dpe.getFilename() + "</b>\"",
							R("DataPoolWindow_Action_ManageLayerStyles_label"), "\"<b>" + ls.getFilename() + "</b>\"");

					txt.append("<ul>");
					printTranslation(ls.getTitle(), "Attributes.Edit.Title");
					printTranslation(ls.getDesc(), "Attributes.Edit.Desc");
					txt.append("</ul>");
				}
			}

			if (dpe instanceof DpLayerVectorFeatureSource) {

				/**
				 * List visible attributes
				 */
				DpLayerVectorFeatureSource dplv = (DpLayerVectorFeatureSource) dpe;
				AttributeMetadataMap<AttributeMetadataImpl> attributeMetaDataMap = dplv.getAttributeMetaDataMap();
				for (AttributeMetadataImpl attributeMetaData : attributeMetaDataMap.sortedValues()) {
					if (attributeMetaData.isVisible()) {
						try {
							printWayH3(R("DataPoolJTable.Border.Title"), "\"<b>" + dpe.getFilename() + "</b>\"",
									R("DataPoolWindow_Action_EditColumns_label"), "\"<b>" + attributeMetaData.getName()
											+ "</b>\"");

							txt.append("<ul>");
							printTranslation(attributeMetaData.getTitle(), "Attributes.Edit.Title");
							printTranslation(attributeMetaData.getDesc(), "Attributes.Edit.Desc");
							txt.append("<li><h3>" + R("Unit") + " = " + attributeMetaData.getUnit() + "</h3></li>");
							txt.append("</ul>");

						} catch (Exception e) {
							txt.append(ERROR + e.getLocalizedMessage());
						}

					}

				}
			}

			/**
			 * list Charts
			 */
			printWayH1(R("Charts"));
			try {
				org.geopublishing.atlasViewer.map.Map map = ace.getMapPool().get(0);
				for (ChartStyle cs : dpe.getCharts()) {
					txt.append("<ul>");
					printWayH2(R("MapPoolJTable.Border.Title"),
							R("MapPoolJTable.ColumnName.NameLang", Translation.getActiveLang()), "<b>\""
									+ StringEscapeUtils.escapeHtml(map.getTitle().toString()) + "\"</b>",
							R("MapPoolWindow.Action.OpenInMapComposer"),
							"<b>\"" + StringEscapeUtils.escapeHtml(dpe.getTitle().toString()) + "\"</b>",
							R("DataPoolWindow_Action_ManageCharts_label"));
					printTranslation(cs.getTitleStyle().getLabelTranslation(), "Attributes.Edit.Title");
					printTranslation(cs.getDescStyle().getLabelTranslation(), "Attributes.Edit.Desc");
					txt.append("</ul>");
				}
			} catch (Exception e) {
				txt.append(ERROR + e.getLocalizedMessage());
			}
		}

		/**
		 * MapPool
		 */
		printWayH1(R("MapPoolJTable.Border.Title"));
		for (org.geopublishing.atlasViewer.map.Map map : ace.getMapPool().values()) {

			printWayH2(R("MapPoolJTable.Border.Title"),
					R("MapPoolJTable.ColumnName.NameLang", Translation.getActiveLang()), "<b>\""
 + StringEscapeUtils.escapeHtml(map.getTitle().toString()) + "\"</b>",
					R("MapPoolWindow.Button_EditMap_label"));
			printTranslation(map.getTitle(), "MapPreferences_translateTheMapsName");
			printTranslation(map.getDesc(), "MapPreferences_translateTheMapsDescription");
			printTranslation(map.getKeywords(), "MapPreferences_translateTheMapsKeywords");
		}

		/**
		 * Groups/ Menus
		 */
		printWayH1(R("EditGroupsDnDJTreePanel.Border.Title"));
		printGroup(ace.getRootGroup(), R("EditGroupsDnDJTreePanel.Border.Title"));

		return txt.toString();
	}

	private void printGroup(Group g, String... wayToHere) {

		LangUtil.extendArray(wayToHere, R("GroupTree.Action.Edit"));

		for (int gi = 0; gi < g.getChildCount(); gi++) {
			TreeNode child = g.getChildAt(gi);
			if (!child.isLeaf()) {
				Group subg = (Group) child;

				String[] longWay = LangUtil.extendArray(wayToHere, subg.getTitle().toString());

				// Object[] lw2 = ArrayUtils.clone(longWay);
				// longWay = new String[0];
				// for (Object s : lw2) {
				// longWay = LangUtil.extendArray(wayToHere, StringEscapeUtils.escapeHtml(((String) s)));
				// }

				printWayH2(longWay);

				printTranslation(subg.getTitle(), "GroupTree.Edit.TranslateTitle");
				printTranslation(subg.getDesc(), "GroupTree.Edit.TranslateDescription");
				printTranslation(subg.getKeywords(), "GroupTree.Edit.TranslateKeywords");

				printGroup(subg, longWay);
			}

		}

		/*
		 * GroupTree.Edit.TranslateTitle=Translate name: GroupTree.Edit.TranslateDescription=Translate description:
		 * GroupTree.Edit.TranslateKeywords=Translate keywords:
		 */
	}

	private void printWayH1(String... steps) {
		txt.append("<h1>");
		printWay(steps);
		txt.append("</h1>");
	}

	private void printWayH2(String... steps) {
		txt.append("<h2>");
		printWay(steps);
		txt.append("</h2>");
	}

	private void printWayH3(String... steps) {
		txt.append("<h3>");
		printWay(steps);
		txt.append("</h3>");
	}

	public String R(String key, Object... values) {
		return StringEscapeUtils.escapeHtml(GeopublisherGUI.R(key, values));
	}

	private void printWay(String... steps) {

		for (String stepKey : steps) {

			txt.append(stepKey + WAY);
		}

		txt.delete(txt.length() - WAY.length(), txt.length());

	}

	private void printTranslation(Translation translation, String key) {
		txt.append("<li>");

		txt.append("<h3>");
		txt.append(R(key, "", ""));
		txt.append("</h3>");

		txt.append("<ul>");
		for (String l : ace.getLanguages()) {
			txt.append("<li>");
			String val = translation.get(l);
			if (val == null)
				val = "";
			txt.append("<p>");
			txt.append(l + " = " + StringEscapeUtils.escapeHtml(val));
			txt.append("</p>");
			txt.append("</li>");
		}
		txt.append("</ul>");
		txt.append("</li>");
	}

}
