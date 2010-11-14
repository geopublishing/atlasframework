package org.geopublishing.atlasStyler.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.geopublishing.atlasStyler.AbstractRulesList.RulesListType;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.RulesListsList;

import schmitzm.swing.SwingUtil;

public class RulesListTable extends JTable {

	private static final int COLIDX_TITLE = 0;
	private static final int COLIDX_TYPE = 1;
	public static final int COLIDX_ENABLED = 2;
	private final RulesListsList rulesList;
	private final AtlasStyler atlasStyler;

	private final PropertyChangeListener updateOnRulesListsListChanges = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			changeTableModel();
		}

	};

	private void changeTableModel() {
		((DefaultTableModel) getModel()).fireTableStructureChanged();

		int rc = rulesList.size();
		if (rc > 0) {
			// Default selcet the last one.. nice then it has just been added
			if (getSelectionModel().isSelectionEmpty())
				getSelectionModel().setSelectionInterval(rc - 1, rc - 1);
		}

		SwingUtil.setColumnLook(this, COLIDX_ENABLED, null, 17, 25, 40);
	}

	public RulesListTable(AtlasStyler atlasStyler) {
		this.atlasStyler = atlasStyler;
		rulesList = atlasStyler.getRuleLists();
		setModel(new RulesListTableModel());

		rulesList.addListener(updateOnRulesListsListChanges);

		changeTableModel();
	}

	class RulesListTableModel extends DefaultTableModel {

		@Override
		public int getRowCount() {
			return rulesList.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public String getColumnName(int column) {

			switch (column) {
			case COLIDX_TITLE:
				return "name"; // i8n
			case COLIDX_TYPE:
				return "type"; // i8n
			case COLIDX_ENABLED:
				return "on"; // i8n
			default:
				return super.getColumnName(column);
			}
		}

		@Override
		public Object getValueAt(int row, int column) {

			switch (column) {
			case COLIDX_TYPE:
				return rulesList.get(row).getType();
			case COLIDX_TITLE:
				return rulesList.get(row).getTitle();
			case COLIDX_ENABLED:
				return rulesList.get(row).isEnabled();
			default:
				return super.getValueAt(row, column);
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			switch (column) {
			case COLIDX_ENABLED:
				return true;
			case COLIDX_TITLE:
				return true;
			default:
				return false;
			}
		}

		@Override
		public Class<?> getColumnClass(int column) {

			switch (column) {
			case COLIDX_TITLE:
				return String.class;
			case COLIDX_TYPE:
				return RulesListType.class;
			case COLIDX_ENABLED:
				return Boolean.class;
			default:
				return super.getColumnClass(column);
			}
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			switch (column) {
			case COLIDX_TITLE:
				rulesList.get(row).setTitle(aValue.toString());
				return;
			case COLIDX_ENABLED:
				rulesList.get(row).setEnabled(!rulesList.get(row).isEnabled());
				return;
			default:
				return;
			}
		}

	}

}
