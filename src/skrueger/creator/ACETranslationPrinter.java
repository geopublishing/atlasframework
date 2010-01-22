/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the Geopublisher application - An authoring tool to facilitate the publication and distribution of geoproducts in form of online and/or offline end-user GIS.
 * http://www.geopublishing.org
 * 
 * Geopublisher is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.creator;

import javax.swing.tree.TreeNode;

// TODO Dokumentation
// TODO Add Charts

import schmitzm.lang.LangUtil;
import skrueger.AttributeMetadata;
import skrueger.atlas.dp.DpEntry;
import skrueger.atlas.dp.Group;
import skrueger.atlas.dp.layer.DpLayerVector;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.atlas.dp.layer.LayerStyle;
import skrueger.geotools.AttributeMetadataMap;
import skrueger.i8n.Translation;

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
	 * @return a {@link String} containing a human-readable summary of all
	 *         {@link Translation}s used.
	 */
	public String printAllTranslations() {
		txt = new StringBuffer(4000);

		/**
		 * AtlasParams
		 */
		printWayH1(R("ACETranslationPrinter.Heading.GeneralInformation"));
		printWayH2(R("MenuBar.AtlasMenu"),
				R("MenuBar.AtlasMenu.ChangeAtlasParams"));
		printTranslation(ace.getTitle(), "AtlasParamsTranslationDialog.Title");
		printTranslation(ace.getDesc(),
				"AtlasParamsTranslationDialog.Description");
		printTranslation(ace.getCreator(),
				"AtlasParamsTranslationDialog.Creator");
		printTranslation(ace.getCopyright(),
				"AtlasParamsTranslationDialog.Copyright");

		/**
		 * DataPool
		 */
		printWayH1(R("DataPoolJTable.Border.Title"));
		for (DpEntry dpe : ace.getDataPool().values()) {

			printWayH2(R("DataPoolJTable.Border.Title"),
					R("DataPoolJTable.ColumnName.Filename"), dpe.getFilename(),
					R("DataPoolWindow_Action_EditDPE_label"));
			printTranslation(dpe.getTitle(), "EditDPEDialog.TranslateTitle");
			printTranslation(dpe.getDesc(),
					"EditDPEDialog.TranslateDescription");
			printTranslation(dpe.getKeywords(),
					"EditDPEDialog.TranslateKeywords");

			if (dpe instanceof DpLayerVector<?, ?>) {

				/**
				 * List additional layer styles
				 */

				DpLayerVector<?, ?> dplv = (DpLayerVector<?, ?>) dpe;
				for (LayerStyle ls : dplv.getLayerStyles()) {
					printWayH3(R("DataPoolJTable.Border.Title"), "\"<b>"
							+ dpe.getFilename() + "</b>\"",
							R("DataPoolWindow_Action_ManageLayerStyles_label"),
							"\"<b>" + ls.getFilename() + "</b>\"");

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
				AttributeMetadataMap attributeMetaDataMap = dplv
						.getAttributeMetaDataMap();
				for (AttributeMetadata attributeMetaData : attributeMetaDataMap.sortedValues()) {
					if (attributeMetaData.isVisible()) {
						try {
							printWayH3(
									R("DataPoolJTable.Border.Title"),
									"\"<b>" + dpe.getFilename() + "</b>\"",
									R("DataPoolWindow_Action_EditColumns_label"),
									"\"<b>" + attributeMetaData.getName() + "</b>\"");

							txt.append("<ul>");
							printTranslation(attributeMetaData.getTitle(),
									"Attributes.Edit.Title");
							printTranslation(attributeMetaData.getDesc(),
									"Attributes.Edit.Desc");
							txt.append("<li><h3>" + R("Unit") + " = "
									+ attributeMetaData.getUnit()
									+ "</h3></li>");
							txt.append("</ul>");

						} catch (Exception e) {
							txt.append(ERROR + e.getLocalizedMessage());
						}

					}

				}
			}

		}

		/**
		 * MapPool
		 */
		printWayH1(R("MapPoolJTable.Border.Title"));
		for (skrueger.atlas.map.Map map : ace.getMapPool().values()) {

			printWayH2(R("MapPoolJTable.Border.Title"), R(
					"MapPoolJTable.ColumnName.NameLang", Translation
							.getActiveLang()), "<b>\""
					+ map.getTitle().toString() + "\"</b>",
					R("MapPoolWindow.Button_EditMap_label"));
			printTranslation(map.getTitle(),
					"MapPreferences_translateTheMapsName");
			printTranslation(map.getDesc(),
					"MapPreferences_translateTheMapsDescription");
			printTranslation(map.getKeywords(),
					"EditMapPreferences_translateTheMapsKeywords");
		}

		/**
		 * Groups/ Menus
		 */
		printWayH1(R("EditGroupsDnDJTreePanel.Border.Title"));
		printGroup(ace.getFirstGroup(),
				R("EditGroupsDnDJTreePanel.Border.Title"));

		return txt.toString();
	}

	private void printGroup(Group g, String... wayToHere) {

		LangUtil.extendArray(wayToHere, R("GroupTree.Action.Edit"));

		for (int gi = 0; gi < g.getChildCount(); gi++) {
			TreeNode child = g.getChildAt(gi);
			if (!child.isLeaf()) {
				Group subg = (Group) child;

				String[] longWay = LangUtil.extendArray(wayToHere, subg
						.getTitle().toString());
				printWayH2(longWay);

				printTranslation(subg.getTitle(),
						"GroupTree.Edit.TranslateTitle");
				printTranslation(subg.getDesc(),
						"GroupTree.Edit.TranslateDescription");
				printTranslation(subg.getKeywords(),
						"GroupTree.Edit.TranslateKeywords");

				printGroup(subg, longWay);
			}

		}

		/*
		 * GroupTree.Edit.TranslateTitle=Translate name:
		 * GroupTree.Edit.TranslateDescription=Translate description:
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
		return AtlasCreator.R(key, values);
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
			txt.append(l + " = " + val);
			txt.append("</p>");
			txt.append("</li>");
		}
		txt.append("</ul>");
		txt.append("</li>");
	}

}
