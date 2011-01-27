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
package org.geopublishing.atlasViewer.swing.internal;

import java.awt.Color;
import java.awt.Component;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.geopublishing.atlasViewer.AtlasConfig;

import de.schmitzm.i18n.Translation;
import de.schmitzm.swing.SwingUtil;

/**
 * Renders a tablecell with a small {@link JTable} presenting all translations
 * for all languages
 * 
 */
public class TranslationCellRenderer extends DefaultTableCellRenderer {

	private AtlasConfig atlasConfig;
	
	Vector<String> columnNames = new Vector<String>();

	public TranslationCellRenderer(AtlasConfig ac) {
		atlasConfig = ac;
		
		columnNames.add("lang"); // not shown but need for col count
		columnNames.add("trans");// not shown but need for col count
		
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		Translation trans = (Translation) value;
	
		Vector<Vector<String>> data = new Vector<Vector<String>>();
//		Vector<String> translations = new Vector<String>();;
//		Vector<String> langs = new Vector<String>();;
		for (int idx = 0; idx < atlasConfig.getLanguages().size(); idx++) {
			String key = atlasConfig.getLanguages().get(idx);
			Vector<String> rowData = new Vector<String>();
			rowData.add(key);
			rowData.add(trans.get(key));
			data.add(rowData);
		}
		
//		langs.add("dex");
//		langs.add("dux");
//		translations.add("sds");
//		translations.add("ssss");

//		data.add(langs);
//		data.add(translations);

		JTable list = new JTable(data, columnNames);
		list.setTableHeader(null);

		if (isSelected)
			list.getSelectionModel().addSelectionInterval(0, 1000);
		else
			list.getSelectionModel().clearSelection();

		SwingUtil.setColumnLook(list, 0, null, 20, 25, 40);
		SwingUtil.setColumnLook(list, 1, null, 100, 300, null);

		list.setGridColor(new Color(210, 210, 210));
		list.setShowVerticalLines(false);
//		
//		SwingUtil.setPreferredHeight(list, data.size()*20);

		return list;
	}

}
