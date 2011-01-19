package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import org.geopublishing.atlasViewer.swing.AtlasFeatureLayerFilterDialog;
import org.geopublishing.atlasViewer.swing.Icons;
import org.opengis.filter.Filter;

import schmitzm.geotools.gui.FilterChangeListener;
import schmitzm.geotools.gui.GeotoolsGUIUtil;
import schmitzm.swing.SwingUtil;
import skrueger.geotools.StyledFeaturesInterface;
import skrueger.swing.swingworker.AtlasSwingWorker;

public class FilterTableCellEditor extends AbstractCellEditor implements
		TableCellEditor {
	protected static final int WARN_CELLS = 30000;
	JButton button;
	AtlasFeatureLayerFilterDialog dialog;
	private final StyledFeaturesInterface<?> sf;
	private final Component owner;
	private Filter backupValueBeforeLastEdit;

	public FilterTableCellEditor(final Component owner,
			final StyledFeaturesInterface<?> sf) {
		this.owner = owner;
		this.sf = sf;
		button = new JButton(new AbstractAction("", Icons.ICON_FILTER) {

			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtil.setRelativeFramePosition(dialog, owner,
						SwingUtil.BOUNDS_INNER, SwingUtil.NORTH);

				dialog.setModal(true);
				dialog.setVisible(true);
				if (!dialog.isCancelled()) {
					stopCellEditing();
				} else {
					dialog.setFilterRule(backupValueBeforeLastEdit != null ? backupValueBeforeLastEdit
							.toString() : null);
				}
				cancelCellEditing();
			}
		});
		// button.addActionListener(this);
		button.setBorderPainted(false);
	}

	// Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		return dialog.getFilter();
	}

	// Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {

		Filter filter = (Filter) value;
		backupValueBeforeLastEdit = filter;

		dialog = getOrCreateFilterTableDialog(sf);

		if (dialog == null)
			return null;

		if (filter != null)
			dialog.setFilterRule(filter.toString());
		else
			dialog.setFilterRule(null);

		dialog.setTitle(GeotoolsGUIUtil.R(
				"FilterTableCellEditor.FilterDialogTitle", table.getModel()
						.getValueAt(row, RulesListTable.COLIDX_TITLE),
				table.getModel().getValueAt(row, RulesListTable.COLIDX_TYPE),
				sf.getTitle()));

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				button.doClick();
			}
		});

		return button;
	}

	private AtlasFeatureLayerFilterDialog getOrCreateFilterTableDialog(
			final StyledFeaturesInterface<?> sf) {
		// if (dialog == null) {

		new AtlasSwingWorker<AtlasFeatureLayerFilterDialog>(owner) {

			@Override
			protected AtlasFeatureLayerFilterDialog doInBackground()
					throws Exception {

				// make a check on howmany feateurs we have an print a
				// warning if too
				// many
				int numCells = sf.getFeatureCollectionFiltered().size()
						* sf.getAttributeMetaDataMap()
								.sortedValuesVisibleOnly().size();
				if (numCells > WARN_CELLS) {
					if (SwingUtil
							.askYesNo(owner, SwingUtil.R(
									"AttributeTable.dialog.warnTooManyCells",
									numCells)) == false) {
						return null;
					}
				}

				dialog = new AtlasFeatureLayerFilterDialog(null, sf);

				dialog.addListener(listener);
				dialog.setFilterRule("");
				return dialog;
			}
		}.executeModalNoEx();

		// }
		return dialog;
	}

	FilterChangeListener listener = new FilterChangeListener() {

		@Override
		public void changed(Filter newFilter) {
			fireEditingStopped();
		}
	};
}
