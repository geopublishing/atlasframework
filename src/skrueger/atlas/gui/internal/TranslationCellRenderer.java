package skrueger.atlas.gui.internal;

import java.awt.Color;
import java.awt.Component;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import schmitzm.swing.SwingUtil;
import skrueger.atlas.AtlasConfig;
import skrueger.i8n.Translation;

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