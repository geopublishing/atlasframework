package org.geopublishing.atlasStyler.swing;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.WeakHashMap;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RasterRulesList_DistinctValues;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.UniqueValuesRuleList;
import org.geotools.styling.ColorMapEntry;

import de.schmitzm.i18n.Translation;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.TranslationAskJDialog;
import de.schmitzm.swing.TranslationEditJPanel;

public class RasterRulesList_Distinctvalues_GUI extends
		AbstractRulesListGui<RasterRulesList_DistinctValues> {

	protected final static Logger LOGGER = LangUtil
			.createLogger(RasterRulesList_Distinctvalues_GUI.class);
	private JTable jTable;
	private DefaultTableModel tableModel;

	public RasterRulesList_Distinctvalues_GUI(
			RasterRulesList_DistinctValues rulesList,
			AtlasStylerRaster atlasStyler) {
		super(rulesList);
	}

	final static int COLIDX_COLOR = 0;
	final static int COLIDX_OPACITY = 1;
	final static int COLIDX_VALUE = 2;
	final static int COLIDX_LABEL = 3;

	/**
	 * Listen for changes in the RulesList. Must be kept as a reference in
	 * {@link UniqueValuesGUI} because the listeners are kept in a
	 * {@link WeakHashMap}
	 */
	final RuleChangeListener updateClassificationTableWhenRuleListChanges = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {

			// Try to remember the selected item
			int selViewIdx = getJTable().getSelectedRow();

			getTableModel().fireTableDataChanged();

			getJTable().getSelectionModel().clearSelection();
			getJTable().getSelectionModel().addSelectionInterval(selViewIdx,
					selViewIdx);

			/** scroll */
			getJTable().scrollRectToVisible(
					getJTable().getCellRect(selViewIdx, 0, true));
		}

	};

	private DefaultTableModel getTableModel() {

		if (tableModel == null) {

			tableModel = new DefaultTableModel() {

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					if (columnIndex == COLIDX_COLOR) // Color
						return Color.class;

					if (columnIndex == COLIDX_OPACITY) // Value
						return Double.class;

					if (columnIndex == COLIDX_VALUE) // Value
						return Double.class;

					if (columnIndex == COLIDX_LABEL) // Label
						return String.class;

					return null;
				}

				@Override
				public int getColumnCount() {
					return 4;
				}

				@Override
				public String getColumnName(int columnIndex) {
					if (columnIndex == COLIDX_COLOR)
						return AtlasStylerVector
								.R("RasterRulesList_Distinctvalues_GUI.classesTable.columnHeadersTitle.color");
					if (columnIndex == COLIDX_OPACITY)
						return AtlasStylerVector
								.R("RasterRulesList_Distinctvalues_GUI.classesTable.columnHeadersTitle.opacity");
					if (columnIndex == COLIDX_VALUE)
						return AtlasStylerVector
								.R("RasterRulesList_Distinctvalues_GUI.classesTable.columnHeadersTitle.value");
					if (columnIndex == COLIDX_LABEL)
						return AtlasStylerVector
								.R("RasterRulesList_Distinctvalues_GUI.classesTable.columnHeadersTitle.label");
					return super.getColumnName(columnIndex);
				}

				@Override
				public int getRowCount() {
					return getRulesList().getNumClasses();
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {

					ColorMapEntry colorMapEntry = getRulesList().getColorMap()
							.getColorMapEntries()[rowIndex];
					if (columnIndex == COLIDX_COLOR) {
						return colorMapEntry.getColor();
					} else if (columnIndex == COLIDX_OPACITY) {
						return colorMapEntry.getOpacity();
					} else if (columnIndex == COLIDX_VALUE) {
						return colorMapEntry.getQuantity();
					} else if (columnIndex == COLIDX_LABEL) {
						return colorMapEntry.getLabel();
					}
					return super.getValueAt(rowIndex, columnIndex);
				}

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return false;
				}

				@Override
				public void setValueAt(Object aValue, int rowIndex,
						int columnIndex) {
				}

			};
		}

		return tableModel;
	}

	/**
	 * This method initializes jTable
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable(getTableModel());

			/** Render nicely COLOR */
			// jTable.setDefaultRenderer(SingleRuleList.class,
			// new UniqueValuesSingleRuleListCellRenderer());

			getRulesList().addListener(
					updateClassificationTableWhenRuleListChanges);

			/*******************************************************************
			 * Listening to clicks on the JTable.. e.g. for translation and
			 * symbol changes
			 */
			jTable.addMouseListener(new MouseAdapter() {

				private TranslationAskJDialog ask;

				@Override
				public void mouseClicked(MouseEvent e) {

					if (e.getClickCount() == 2) {
						int col = jTable.columnAtPoint(e.getPoint());
						final int row = jTable.rowAtPoint(e.getPoint());

						if (col == 2) {

							Object val = getRulesList().getValues().get(row);

							if (AtlasStylerVector.getLanguageMode() == AtlasStylerVector.LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {
								LOGGER.debug(AtlasStylerVector.getLanguages());

								final Translation translation = new Translation();

								translation.fromOneLine(getRulesList()
										.getLabels().get(row));

								if (ask == null) {
									TranslationEditJPanel transLabel;
									if (val.equals(UniqueValuesRuleList.ALLOTHERS_IDENTIFICATION_VALUE)) {
										/** We are in the default rule* */
										transLabel = new TranslationEditJPanel(
												AtlasStylerVector
														.R("RasterRulesList_Distinctvalues_GUI.LabelForClass",
																AtlasStylerVector
																		.R("RasterRulesList_Distinctvalues_GUI.AllOthersSymbol.label")),
												translation, AtlasStylerVector
														.getLanguages());
									} else {

										// The index depends on the whether the
										// "all others rule" is eneabled and
										// where it is positioned in the list!
										int index = row;

										transLabel = new TranslationEditJPanel(
												AtlasStylerVector
														.R("RasterRulesList_Distinctvalues_GUI.LabelForClass",
																getRulesList()
																		.getValues()
																		.get(index)),
												translation, AtlasStylerVector
														.getLanguages());
									}

									ask = new TranslationAskJDialog(
											RasterRulesList_Distinctvalues_GUI.this,
											transLabel);
									ask.addPropertyChangeListener(new PropertyChangeListener() {

										@Override
										public void propertyChange(
												PropertyChangeEvent evt) {
											if (evt.getPropertyName()
													.equals(TranslationAskJDialog.PROPERTY_CANCEL_AND_CLOSE)) {
												ask = null;
											}
											if (evt.getPropertyName()
													.equals(TranslationAskJDialog.PROPERTY_APPLY_AND_CLOSE)) {
												LOGGER.debug("Saving new ranslation in rulelist and fire event ");

												getRulesList()
														.getLabels()
														.set(row,
																translation
																		.toOneLine());

											}
											ask = null;
											//
											getRulesList()
													.fireEvents(
															new RuleChangedEvent(
																	"Legend Label changed",
																	getRulesList()));
										}

									});

								}
								ask.setVisible(true);

							} else {
								/***********************************************
								 * AtlasStyler.LANGUAGE_MODE.OGC
								 */
								String newTitle = ASUtil
										.askForString(
												RasterRulesList_Distinctvalues_GUI.this,
												getRulesList().getLabels().get(
														row), null);
								if (newTitle != null) {
									getRulesList().getLabels().set(row,
											newTitle);
									getRulesList().fireEvents(
											new RuleChangedEvent(
													"Legend Label changed",
													getRulesList()));
								}
							}

						}

						else
						/*******************************************************
						 * Changing the Symbol with a MouseClick
						 */
						if (col == COLIDX_COLOR) {
							// getRulesList().getSymbols().get(row).getListeners()
							// .clear();
							// final SingleRuleList<? extends Symbolizer>
							// editSymbol = getRulesList()
							// .getSymbols().get(row);
							// final SingleRuleList<? extends Symbolizer> backup
							// = editSymbol
							// .copy();
							//
							// SymbolSelectorGUI gui = new SymbolSelectorGUI(
							// SwingUtil
							// .getParentWindow(RasterRulesList_Distinctvalues_GUI.this),
							// "Change symbol for "
							// + getRulesList().getLabels().get(
							// row), editSymbol);
							//
							// /***************************************************
							// * Listen to a CANCEL to use the backup
							// */
							// gui.addPropertyChangeListener(new
							// PropertyChangeListener() {
							//
							// @Override
							// public void propertyChange(
							// PropertyChangeEvent evt) {
							//
							// if (evt.getPropertyName()
							// .equals(SymbolSelectorGUI.PROPERTY_CANCEL_CHANGES))
							// {
							//
							// backup.copyTo(editSymbol);
							// }
							//
							// if (evt.getPropertyName().equals(
							// SymbolSelectorGUI.PROPERTY_CLOSED)) {
							// }
							//
							// }
							//
							// });
							//
							// // we have a referenct to it!
							// listenToEditedSymbolAndPassOnTheEvent = new
							// RuleChangeListener() {
							//
							// @Override
							// public void changed(RuleChangedEvent e) {
							//
							// /** Exchanging the Symbol * */
							// getRulesList().getSymbols().set(row,
							// editSymbol);
							//
							// // Fire an event?! TODO
							// getRulesList().fireEvents(
							// new RuleChangedEvent(
							// "Editing a Symbol",
							// getRulesList()));
							//
							// }
							//
							// };
							// editSymbol
							// .addListener(listenToEditedSymbolAndPassOnTheEvent);
							//
							// gui.setModal(true);
							// gui.setVisible(true);
						}
					}

				}

			});

			jTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

			jTable.setRowHeight(AtlasStylerVector.DEFAULT_SYMBOL_PREVIEW_SIZE.height + 2);
			jTable.getColumnModel().getColumn(0).setMaxWidth(53);
		}
		return jTable;
	}

}
