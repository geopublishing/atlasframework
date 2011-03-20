package org.geopublishing.atlasStyler.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.RulesListsList;
import org.geopublishing.atlasStyler.rulesLists.AbstractRulesList.RulesListType;
import org.geopublishing.atlasStyler.rulesLists.RulesListInterface;
import org.geopublishing.atlasViewer.swing.AtlasStylerDialog;
import org.opengis.filter.Filter;

import de.schmitzm.geotools.gui.XMapPane;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.SwingUtil;

public class RulesListTable extends JTable {

	public static final int COLIDX_ENABLED = 0;
	static final int COLIDX_TITLE = 1;
	static final int COLIDX_TYPE = 2;
	public static final int COLIDX_MINSCALE = 3;
	public static final int COLIDX_MAXSCALE = 4;
	public static final int COLIDX_FILTER = 5;

	private final RulesListsList rulesList;
	private final AtlasStyler atlasStyler;

	/**
	 * Listen to changes in the rulelist that affect this table view
	 */
	protected RuleChangeListener listenForRulesListChangesWhichShowInTheTable = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {
			if (RuleChangedEvent.RULE_CHANGE_EVENT_FILTER_STRING.equals(e
					.getReason())) {
				// ((DefaultTableModel) getModel()).fireTableDataChanged();
				repaint();
			} else if (RuleChangedEvent.RULE_CHANGE_EVENT_MINMAXSCALE_STRING
					.equals(e.getReason())) {
				// ((DefaultTableModel) getModel()).fireTableDataChanged();
				repaint();
			} else if (RuleChangedEvent.RULE_CHANGE_EVENT_ENABLED_STRING
					.equals(e.getReason()))
				repaint();
		}
	};

	/**
	 * The ScaleDenominator shown in any preview {@link XMapPane} outside of
	 * this {@link AtlasStylerDialog}
	 */
	private Double scaleDenominator;

	protected PropertyChangeListener listenForScaleChangesInthePreviewPaneAndRepaintTable = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			setScaleInPreview((Double) evt.getNewValue());

		}

	};

	private void setScaleInPreview(Double newScale) {
		if (newScale == scaleDenominator)
			return;

		scaleDenominator = newScale;

		// ((DefaultTableModel) getModel()).fireTableDataChanged();
		repaint();
	}

	private final PropertyChangeListener updateOnRulesListsListChanges = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			changeTableModel();
		}

	};
	private final StylerDialog asd;
	private final TableCellRenderer ruleListLabelRenderer = new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {

			JLabel proto = (JLabel) super.getTableCellRendererComponent(table,
					value, isSelected, hasFocus, row, column);

			if (!(Boolean) table.getModel().getValueAt(row,
					RulesListTable.COLIDX_ENABLED)) {
				proto.setForeground(Color.gray);
			} else
				proto.setForeground(Color.BLACK);

			return proto;
		}

	};

	private void changeTableModel() {
		((DefaultTableModel) getModel()).fireTableStructureChanged();

		int rc = rulesList.size();
		if (rc > 0) {
			// Default selcet the last one.. nice when it has just been added
			if (getSelectionModel().isSelectionEmpty())
				// getSelectionModel().setSelectionInterval(rc - 1, rc - 1);
				getSelectionModel().setSelectionInterval(0, 0);
		}

		updateColumnsLook();

		if (atlasStyler instanceof AtlasStylerVector) {
			setDefaultEditor(
					Filter.class,
					new FilterTableCellEditor(RulesListTable.this,
							((AtlasStylerVector) atlasStyler)
									.getStyledFeatures()));
		}

		// Re-add the weak listener that listens for filter changes created
		// external, e.g. by popup menu insert
		for (RulesListInterface rl : rulesList) {
			rl.addListener(listenForRulesListChangesWhichShowInTheTable);
		}

	}

	public RulesListTable(StylerDialog asd) {

		this.asd = asd;

		asd.addScaleChangeListener(listenForScaleChangesInthePreviewPaneAndRepaintTable);

		if (asd.getPreviewMapPane() != null)
			scaleDenominator = asd.getPreviewMapPane().getScaleDenominator();

		this.atlasStyler = asd.getAtlasStyler();
		rulesList = atlasStyler.getRuleLists();
		setModel(new RulesListTableModel());

		rulesList.addListener(updateOnRulesListsListChanges);

		changeTableModel();

		addMouseListener(new PopupListener());

		addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int columnAtPoint = RulesListTable.this.columnAtPoint(e
						.getPoint());
				int rowAtPoint = RulesListTable.this.rowAtPoint(e.getPoint());
				int colInModel = RulesListTable.this
						.convertColumnIndexToModel(columnAtPoint);
				int rowInModel = RulesListTable.this
						.convertRowIndexToModel(rowAtPoint);

				RulesListInterface ruleList = rulesList.get(rowInModel);

				final int i = e.getWheelRotation() * -1;

				if (colInModel == COLIDX_MINSCALE) {
					double tenPercent = ruleList.getMinScaleDenominator() * 0.1;
					if (tenPercent < 5000)
						tenPercent = 5000;
					double newValue = ruleList.getMinScaleDenominator() + i
							* tenPercent;

					// Nicht größer Unendlich erlauben
					if (i > 0
							&& ruleList.getMinScaleDenominator() >= RulesListInterface.MAX_SCALEDENOMINATOR) {
						return;
					}

					newValue = LangUtil.round(newValue, -3);
					ruleList.setMinScaleDenominator(newValue);
				}

				if (colInModel == COLIDX_MAXSCALE) {
					double tenPercent = ruleList.getMaxScaleDenominator() * 0.1;
					if (tenPercent < 5000)
						tenPercent = 5000;
					double newValue = ruleList.getMaxScaleDenominator() + i
							* tenPercent;

					// Nicht größer Unendlich erlauben
					if (i > 0
							&& ruleList.getMaxScaleDenominator() >= RulesListInterface.MAX_SCALEDENOMINATOR) {
						return;
					}

					// newValue = LangUtil.round(newValue, -3);
					ruleList.setMaxScaleDenominator(newValue);
				}

			}
		});
	}

	/**
	 * Renderer for a table cell that presents a Min- or MaxScaleDenominator.
	 */
	class ScaleCellRenderer extends DefaultTableCellRenderer {

		private final boolean isMin;

		/**
		 * @param min
		 *            if <code>true</code> this renders a minScaleDenominator,
		 *            otherwise a maxScaleDenominator
		 */
		public ScaleCellRenderer(boolean min) {
			this.isMin = min;
		}

		@Override
		public JLabel getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel proto = (JLabel) new DefaultTableCellRenderer()
					.getTableCellRendererComponent(table, value, isSelected,
							hasFocus, row, column);

			proto.setHorizontalAlignment(SwingConstants.RIGHT);
			proto.setVerticalAlignment(SwingConstants.TOP);

			if (value != null
					&& ((Number) value).doubleValue() >= RulesListInterface.MAX_SCALEDENOMINATOR)
				proto.setText("<html>&#8734;</html>");
			else
				proto.setText(NumberFormat.getIntegerInstance().format(value));

			if (isMin && scaleDenominator != null
					&& scaleDenominator < (Double) value) {
				proto.setBackground(Color.red);
			} else if (!isMin && scaleDenominator != null
					&& scaleDenominator > (Double) value) {
				proto.setBackground(Color.red);
			}

			// Use gray font if the rulelist is disabled
			if (!(Boolean) table.getModel().getValueAt(row, COLIDX_ENABLED)) {
				proto.setForeground(Color.GRAY);
			}

			return proto;
		}
	}

	class PopupListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if (!showPopup(e)) {
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (!showPopup(e)) {
			}
		}

		private boolean showPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				final Component component = e.getComponent();

				// int columnAtPoint = RulesListTable.this.columnAtPoint(e
				// .getPoint());
				int rowAtPoint = RulesListTable.this.rowAtPoint(e.getPoint());

				// int colInModel = RulesListTable.this
				// .convertColumnIndexToModel(columnAtPoint);
				int rowInModel = RulesListTable.this
						.convertRowIndexToModel(rowAtPoint);

				getSelectionModel()
						.setSelectionInterval(rowInModel, rowInModel);

				RulesListInterface ruleList = rulesList.get(rowInModel);
				RulesListPopup popup = new RulesListPopup(ruleList, asd);
				popup.show(component, e.getX(), e.getY());
				return true;
			}
			return false;
		}
	}

	class RulesListTableModel extends DefaultTableModel {

		@Override
		public int getRowCount() {
			return rulesList.size();
		}

		@Override
		public int getColumnCount() {
			if (atlasStyler instanceof AtlasStylerVector) {
				return 6;
			} else
				return 5;
		}

		@Override
		public String getColumnName(int column) {

			switch (column) {
			case COLIDX_TITLE:
				return ASUtil.R("RulesListTable.Column.Name");
			case COLIDX_TYPE:
				return ASUtil.R("RulesListTable.Column.Type");
			case COLIDX_MINSCALE:
				return ASUtil.R("RulesListTable.Column.MinScale");
			case COLIDX_MAXSCALE:
				return ASUtil.R("RulesListTable.Column.MaxScale");
			case COLIDX_FILTER:
				return ASUtil.R("RulesListTable.Column.Filter");
			case COLIDX_ENABLED:
				return ASUtil.R("RulesListTable.Column.Enabled");
			default:
				return super.getColumnName(column);
			}
		}

		@Override
		public Object getValueAt(int row, int column) {

			switch (column) {
			case COLIDX_TITLE:
				return rulesList.get(row).getTitle();
			case COLIDX_TYPE:
				return "<html>" + rulesList.get(row).getType().getTitle()
						+ "</html>";
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

	/**
	 * Configures how the columns should be rendered. This method checks for the
	 * "isEasy" flag and hides columns.
	 */
	public void updateColumnsLook() {

		boolean easy = asd.isEasy();
		SwingUtil.setColumnLook(this, COLIDX_ENABLED, null, 17, 19, 19);

		SwingUtil.setColumnLook(this, COLIDX_TITLE, ruleListLabelRenderer,
				easy ? 0 : 10, easy ? 0 : 40, easy ? 0 : 120);

		SwingUtil.setColumnLook(this, COLIDX_TYPE, ruleListLabelRenderer, 40,
				50, null);

		SwingUtil.setColumnLook(this, COLIDX_MINSCALE, new ScaleCellRenderer(
				true), easy ? 0 : 10, easy ? 0 : 40, easy ? 0 : 120);

		SwingUtil.setColumnLook(this, COLIDX_MAXSCALE, new ScaleCellRenderer(
				false), easy ? 0 : 10, easy ? 0 : 40, easy ? 0 : 120);

		if (atlasStyler instanceof AtlasStylerVector) {
			SwingUtil.setColumnLook(this, COLIDX_FILTER,
					new FilterTableCellRenderer(), easy ? 0 : 17,
					easy ? 0 : 19, easy ? 0 : 19);
		}

	}
}
