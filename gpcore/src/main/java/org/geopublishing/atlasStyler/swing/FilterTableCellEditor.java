package org.geopublishing.atlasStyler.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.geopublishing.atlasViewer.swing.AtlasFeatureLayerFilterDialog;
import org.geopublishing.atlasViewer.swing.Icons;
import org.opengis.filter.Filter;

import schmitzm.geotools.gui.GeotoolsGUIUtil;
import schmitzm.swing.SwingUtil;
import skrueger.geotools.StyledFeaturesInterface;

public class FilterTableCellEditor extends AbstractCellEditor implements
		TableCellEditor {
	JButton button;
	AtlasFeatureLayerFilterDialog dialog;

	public FilterTableCellEditor(final Component owner,
			StyledFeaturesInterface<?> sf) {
		button = new JButton(new AbstractAction("", Icons.ICON_FILTER) {

			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtil.setRelativeFramePosition(dialog, owner,
						SwingUtil.BOUNDS_INNER, SwingUtil.NORTH);
				dialog.setModal(true);
				dialog.setVisible(true);

				fireEditingStopped(); // Make the renderer reappear.
			}
		});
		// button.addActionListener(this);
		button.setBorderPainted(false);

		// Set up the dialog that the button brings up.
		// colorChooser = new JColorChooser();
		// dialog = JColorChooser.createDialog(button, "Pick a Color", true, //
		// modal
		// colorChooser, this, // OK button handler
		// null); // no CANCEL button handler
		dialog = new AtlasFeatureLayerFilterDialog(null, sf);
		dialog.setFilterRule("");
		// dialog.addListener(this);
	}

	// Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		return dialog.getFilter();
	}

	// Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {

		Filter filter = (Filter) value;

		if (filter != null)
			dialog.setFilterRule(filter.toString());
		else
			dialog.setFilterRule(null);

		dialog.setTitle(GeotoolsGUIUtil.R(
				"FilterTableCellEditor.FilterDialogTitle", ""));
		return button;
	}

	// @Override
	// public void changed(Filter newFilter) {
	// ruleList.setRlFilter(newFilter);
	// }
}
