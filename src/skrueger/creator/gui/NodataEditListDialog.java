package skrueger.creator.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.SwingUtil;
import skrueger.AttributeMetadata;
import skrueger.atlas.AVUtil;
import skrueger.atlas.dp.layer.DpLayerVectorFeatureSource;
import skrueger.creator.AtlasCreator;
import skrueger.swing.CancellableDialogAdapter;

/**
 */
public class NodataEditListDialog extends CancellableDialogAdapter {

	private final DpLayerVectorFeatureSource dplv;
	private final AttributeMetadata attMetaData;
	private HashSet<Object> backupNodata;

	// Contains a list of Unique values. The runtime classes equal the Attribute
	// binding class
	private TreeSet<Object> uniqueValues = null;
	private JButton addButton;
	private JTextField valueField;
	private JButton removeButton;
	private JTable nodataValuesJTable;

	/**
	 * This dialog is automaticall visible and modal when constructed
	 */
	public NodataEditListDialog(Component owner,
			DpLayerVectorFeatureSource dplv, AttributeMetadata attMetaData) {

		super(owner, AtlasCreator.R("NoDataValues.EditDialog.Title",
				attMetaData.getTitle()));
		this.dplv = dplv;
		this.attMetaData = attMetaData;

		// Backup so we can cancel
		backupNodata = (HashSet<Object>) attMetaData.getNodataValues().clone();

		initGui();

		setModal(true);
		
		SwingUtil.setRelativeFramePosition(this, owner, SwingUtil.BOUNDS_OUTER, SwingUtil.WEST);
	}

	@Override
	public void setVisible(boolean b) {
		if (b == true && uniqueValues != null) {
			// 
		}
		super.setVisible(b);
	};

	/**
	 * Initialize the GUI
	 */
	private void initGui() {
		JPanel contentPane = new JPanel(new MigLayout("wrap 1, w 400"));

		JPanel descPane = new JPanel(new MigLayout());
		descPane.add(new JLabel(AtlasCreator.R(
				"NoDataValues.EditDialog.Explain", attMetaData.getTitle())),
				BorderLayout.WEST);
		contentPane.add(descPane);

		contentPane.add(new JScrollPane(getNODATAValuesJTable()));

		contentPane.add(getValueTextField(), "split 3");
		contentPane.add(getAddButton());
		contentPane.add(getRemoveButton());

		contentPane.add(getOkButton(), "tag ok, split 2");
		contentPane.add(getCancelButton(), "tag cancel");

		setContentPane(contentPane);
		pack();
	}

	private JButton getRemoveButton() {
		if (removeButton == null) {
			removeButton = new JButton(new AbstractAction("-") {

				@Override
				public void actionPerformed(ActionEvent e) {
					int selRow = getNODATAValuesJTable().getSelectedRow();
					if (selRow > -1) {

						attMetaData
								.getNodataValues()
								.remove(
										attMetaData.getNodataValues().toArray()[selRow]);
						((DefaultTableModel) getNODATAValuesJTable().getModel())
								.fireTableDataChanged();
					}
				}
			});
			getNODATAValuesJTable().getSelectionModel()
					.addListSelectionListener(new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent e) {
							removeButton.setEnabled(e.getFirstIndex() >= 0);
						}
					});
		}
		return removeButton;
	}

	private JTextField getValueTextField() {
		if (valueField == null) {
			valueField = new JTextField(8);
		}
		return valueField;
	}

	private JButton getAddButton() {
		if (addButton == null) {
			addButton = new JButton(new AbstractAction("+") {

				@Override
				public void actionPerformed(ActionEvent e) {
					String valueString = getValueTextField().getText();
					if (Number.class.isAssignableFrom(dplv.getSchema()
							.getDescriptor(attMetaData.getName()).getType()
							.getBinding())) {
						try {
							attMetaData.getNodataValues().add(
									Double.parseDouble(valueString));
						} catch (NumberFormatException nex) {
							AVUtil
									.showMessageDialog(
											NodataEditListDialog.this,
											AtlasCreator
													.R(
															"NoDataValues.EditDialog.NumberParseError",
															valueString,
															attMetaData
																	.getTitle()));
						} catch (Exception ex) {
							ExceptionDialog.show(ex);
						}
					} else
						attMetaData.getNodataValues().add(valueString);

					((DefaultTableModel) getNODATAValuesJTable().getModel())
							.fireTableDataChanged();
				}

			});
		}
		return addButton;
	}

	private JTable getNODATAValuesJTable() {
		if (nodataValuesJTable == null) {
			nodataValuesJTable = new JTable(new NoDataValuesTableModel());

			nodataValuesJTable.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						@Override
						public void valueChanged(ListSelectionEvent e) {
							if (e.getValueIsAdjusting() == false
									&& e.getFirstIndex() != -1) {
								getValueTextField()
										.setText(
												attMetaData.getNodataValues()
														.toArray()[e
														.getFirstIndex()]
														.toString());
								getRemoveButton().setEnabled(true);
							} else {
								getRemoveButton().setEnabled(false);
							}
						}
					});
		}

		return nodataValuesJTable;
	}

	@Override
	public void cancel() {
		attMetaData.getNodataValues().clear();
		for (Object noData : backupNodata)
			attMetaData.getNodataValues().add(noData);
	}

	class NoDataValuesTableModel extends DefaultTableModel {

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public String getColumnName(int column) {
			return AtlasCreator.R("NodataValues");
		};

		@Override
		public int getRowCount() {
			return attMetaData.getNodataValues().size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			return attMetaData.getNodataValues().toArray()[row];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return dplv.getSchema().getDescriptor(attMetaData.getName())
					.getType().getBinding();
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			// Remove old value from Set
			attMetaData.getNodataValues().remove(getValueAt(row, column));
			// Add new value to set
			attMetaData.getNodataValues().add(aValue);
			// This affects all rows because the list will resort
			((DefaultTableModel) getNODATAValuesJTable().getModel())
					.fireTableDataChanged();
		}
	}

}
