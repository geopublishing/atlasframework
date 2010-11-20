package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.geopublishing.atlasStyler.AbstractRulesList;
import org.geopublishing.atlasStyler.AbstractRulesList.RulesListType;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.RulesListsList;
import org.opengis.filter.Filter;

import schmitzm.swing.SwingUtil;

public class RulesListTable extends JTable {

	private static final int COLIDX_TITLE = 0;
	private static final int COLIDX_TYPE = 1;
	public static final int COLIDX_ENABLED = 2;
	public static final int COLIDX_MINSCALE = 3;
	public static final int COLIDX_MAXSCALE = 4;
	public static final int COLIDX_FILTER = 5;

	private final RulesListsList rulesList;
	private final AtlasStyler atlasStyler;

	protected RuleChangeListener listenForRulesListChangesWhichShowInTheTable = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {
			if (e.getReason() != null
					&& e.getReason().equals(
							RuleChangedEvent.RULE_CHANGE_EVENT_FILTER_STRING)) {
				((DefaultTableModel) getModel()).fireTableDataChanged();
			}
		}
	};

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
			// Default selcet the last one.. nice when it has just been added
			if (getSelectionModel().isSelectionEmpty())
				getSelectionModel().setSelectionInterval(rc - 1, rc - 1);
		}

		SwingUtil.setColumnLook(this, COLIDX_ENABLED, null, 17, 18, 20);
		SwingUtil.setColumnLook(this, COLIDX_FILTER,
				new FilterTableCellRenderer(), 17, 18, 20);

		setDefaultEditor(Filter.class, new FilterTableCellEditor(
				RulesListTable.this, atlasStyler.getStyledFeatures()));

		// Re-add the weak listener that listens for filter changes created
		// external, e.g. by popup menu insert
		for (AbstractRulesList rl : rulesList) {
			rl.addListener(listenForRulesListChangesWhichShowInTheTable);
		}

	}

	public RulesListTable(AtlasStyler atlasStyler) {
		this.atlasStyler = atlasStyler;
		rulesList = atlasStyler.getRuleLists();
		setModel(new RulesListTableModel());

		rulesList.addListener(updateOnRulesListsListChanges);

		changeTableModel();

		addMouseListener(new PopupListener());
	}

	class PopupListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			showPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showPopup(e);
		}

		private void showPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				final Component component = e.getComponent();

				int columnAtPoint = RulesListTable.this.columnAtPoint(e
						.getPoint());
				int rowAtPoint = RulesListTable.this.rowAtPoint(e.getPoint());

				int colInModel = RulesListTable.this
						.convertColumnIndexToModel(columnAtPoint);
				int rowInModel = RulesListTable.this
						.convertRowIndexToModel(rowAtPoint);

				AbstractRulesList ruleList = rulesList.get(rowInModel);
				RulesListPopup popup = new RulesListPopup(ruleList);
				popup.show(component, e.getX(), e.getY());
			}
		}
	}

	class RulesListTableModel extends DefaultTableModel {

		@Override
		public int getRowCount() {
			return rulesList.size();
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public String getColumnName(int column) {

			switch (column) {
			case COLIDX_TITLE:
				return "name"; // i8n
			case COLIDX_TYPE:
				return "type"; // i8n
			case COLIDX_MINSCALE:
				return "min-scale"; // i8n
			case COLIDX_MAXSCALE:
				return "max-scale"; // i8n
			case COLIDX_FILTER:
				return "filter"; // i8n
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
			case COLIDX_MINSCALE:
				return rulesList.get(row).getMinScaleDenominator();
			case COLIDX_MAXSCALE:
				return rulesList.get(row).getMaxScaleDenominator();
			case COLIDX_FILTER:
				return rulesList.get(row).getRlFilter();
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
			case COLIDX_FILTER:
			case COLIDX_MINSCALE:
			case COLIDX_MAXSCALE:
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
			case COLIDX_FILTER:
				return Filter.class;
			case COLIDX_MINSCALE:
			case COLIDX_MAXSCALE:
				return Double.class;
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
			case COLIDX_MINSCALE:
				rulesList.get(row).setMinScaleDenominator((Double) aValue);
				return;
			case COLIDX_MAXSCALE:
				rulesList.get(row).setMaxScaleDenominator((Double) aValue);
				return;
			case COLIDX_FILTER:
				rulesList.get(row).setRlFilter((Filter) aValue);
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
