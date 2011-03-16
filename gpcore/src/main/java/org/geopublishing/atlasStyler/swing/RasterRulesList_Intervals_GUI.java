package org.geopublishing.atlasStyler.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RasterRulesList_Intervals;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geotools.brewer.color.BrewerPalette;

import de.schmitzm.i18n.Translation;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.swing.ThinButton;
import de.schmitzm.swing.TranslationAskJDialog;
import de.schmitzm.swing.TranslationEditJPanel;

/**
 * A GUI to edit a {@link RasterRulesList_Intervals}
 */
public class RasterRulesList_Intervals_GUI extends
		AbstractRulesListGui<RasterRulesList_Intervals> {

	protected final static Logger LOGGER = LangUtil
			.createLogger(RasterRulesList_Intervals_GUI.class);
	
	private JTable jTable;
	private DefaultTableModel tableModel;
	private JComboBox jComboBoxOpacity;
	private ThinButton jButtonApplyOpacity;

	public RasterRulesList_Intervals_GUI(
			RasterRulesList_Intervals rulesList,
			AtlasStylerRaster atlasStyler) {
		super(rulesList);
		initialize();
		rulesList.fireEvents(new RuleChangedEvent("GUI created for "
				+ this.getClass().getSimpleName()
				+ ", possibly setting some default values", rulesList));
	}

	private void initialize() {
		JLabel jLabelHeading = new JLabel(ASUtil.R("RasterRulesList_Distinctvalues_GUI.Heading"));
		jLabelHeading.setFont(jLabelHeading.getFont().deriveFont(
				AVSwingUtil.HEADING_FONT_SIZE));
		this.setLayout(new MigLayout("inset 1, gap 1, wrap 1, fillx"));

		this.add(jLabelHeading, "center");
		this.add(getJPanelColorAndOpacity(), "align l");

		this.add(new JScrollPane(getJTable()), "grow x, grow y 20000");
	}

	private Component getJPanelColorAndOpacity() {
		final JPanel jPanelColorAndTemplate = new JPanel(new MigLayout(
				"wrap 2, inset 1, gap 1", "[grow][]"));
		jPanelColorAndTemplate
				.setBorder(BorderFactory.createTitledBorder(ASUtil
						.R("RasterRulesList_Distinctvalues_GUI.PanelBorderTitle.Colors_and_Opacity")));

		jPanelColorAndTemplate.add(getJComboBoxPalette(), "align r");
		jPanelColorAndTemplate.add(getJButtonApplyPalette(), "sgx");
		JLabel jLabelTemplate = new JLabel(ASUtil.R("OpacityLabel"));
		jPanelColorAndTemplate.add(jLabelTemplate, "split 2, align r");
		jPanelColorAndTemplate.add(getJComboboxOpacity(), "align r");
		jPanelColorAndTemplate.add(getJButtonApplyOpacity(), "sgx");
		return jPanelColorAndTemplate;
	}

	private JComboBox getJComboboxOpacity() {
		if (jComboBoxOpacity == null) {
			jComboBoxOpacity = new JComboBox();
			jComboBoxOpacity.setModel(new DefaultComboBoxModel(
					AbstractStyleEditGUI.OPACITY_VALUES));

			jComboBoxOpacity.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					rulesList.setOpacity(((Float) jComboBoxOpacity
							.getSelectedItem()).doubleValue());
				}
			});

			if (rulesList.getOpacity() != null) {
				ASUtil.selectOrInsert(jComboBoxOpacity, rulesList.getOpacity()
						.floatValue());
			}

			SwingUtil.addMouseWheelForCombobox(jComboBoxOpacity);
		}
		return jComboBoxOpacity;

	}

	private Component getJButtonApplyOpacity() {
		if (jButtonApplyOpacity == null) {
			jButtonApplyOpacity = new ThinButton(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					rulesList.applyOpacity();
				}
			});
			jButtonApplyOpacity.setText(ASUtil
					.R("UniqueValues.applyTemplateButton.title"));
			jButtonApplyOpacity.setToolTipText(ASUtil
					.R("RasterRulesList_Distinctvalues_GUI.PanelBorderTitle.applyTemplateButton.tooltip",rulesList.getNumClassesVisible()));
		}
		return jButtonApplyOpacity;
	}

	final static int COLIDX_COLOR = 0;
	final static int COLIDX_OPACITY = 1;
	final static int COLIDX_VALUE = 2;
	final static int COLIDX_LABEL = 3;

	/**
	 * Listen for changes in the RulesList. Must be kept as a reference in
	 * {@link RasterRulesList_Intervals_GUI} because the listeners are kept
	 * in a {@link WeakHashMap}
	 */
	final RuleChangeListener updateTableWhenRuleListChanges = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {

			// Try to remember the selected item
			int selViewIdx = getJTable().getSelectedRow();

			getTableModel().fireTableStructureChanged();

			getJTable().getSelectionModel().clearSelection();
			getJTable().getSelectionModel().addSelectionInterval(selViewIdx,
					selViewIdx);

			/** scroll */
			getJTable().scrollRectToVisible(
					getJTable().getCellRect(selViewIdx, 0, true));
		}

	};
	private ThinButton jButtonApplyPalette;
	private JComboBoxBrewerPalettes jComboBoxPalette;

	private DefaultTableModel getTableModel() {

		if (tableModel == null) {

			tableModel = new DefaultTableModel() {

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					if (columnIndex == COLIDX_COLOR)
						return Color.class;

					if (columnIndex == COLIDX_OPACITY)
						return Double.class;

					if (columnIndex == COLIDX_VALUE)
						return Double.class;

					if (columnIndex == COLIDX_LABEL)
						return Translation.class;

					return null;
				}

				@Override
				public int getColumnCount() {
					return 4;
				}

				@Override
				public String getColumnName(int columnIndex) {
					if (columnIndex == COLIDX_COLOR)
						return ASUtil
								.R("ColorLabel");
					if (columnIndex == COLIDX_OPACITY)
						return ASUtil
								.R("OpacityLabel");
					if (columnIndex == COLIDX_VALUE)
						return ASUtil
								.R("RasterRulesList_Distinctvalues_GUI.classesTable.columnHeadersTitle.value");
					if (columnIndex == COLIDX_LABEL)
						return ASUtil
								.R("RasterRulesList_Distinctvalues_GUI.classesTable.columnHeadersTitle.label");
					return super.getColumnName(columnIndex);
				}

				@Override
				public int getRowCount() {
					return getRulesList().getNumClasses();
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {

					if (columnIndex == COLIDX_COLOR) {
						if (rulesList.getOpacities().get(rowIndex) == 0)
							return null;
						return rulesList.getColors().get(rowIndex);
					} else if (columnIndex == COLIDX_OPACITY) {
						return rulesList.getOpacities().get(rowIndex);
					} else if (columnIndex == COLIDX_VALUE) {
						return rulesList.getValues().get(rowIndex);
					} else if (columnIndex == COLIDX_LABEL) {
						return rulesList.getLabels().get(rowIndex);
					}
					return super.getValueAt(rowIndex, columnIndex);
				}

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					if (columnIndex == COLIDX_OPACITY)
						return true;
					return false;
				}

				@Override
				public void setValueAt(Object aValue, int rowIndex,
						int columnIndex) {
					if (columnIndex == COLIDX_OPACITY) {
						rulesList.setOpacity(rowIndex, (Double) aValue);
					}
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

			getRulesList().addListener(updateTableWhenRuleListChanges);

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

						if (col == COLIDX_LABEL) {

							if (AtlasStylerVector.getLanguageMode() == AtlasStylerVector.LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {
								LOGGER.debug(AtlasStylerVector.getLanguages());

								final Translation translation = getRulesList()
										.getLabels().get(row);

								if (ask == null) {
									TranslationEditJPanel transLabel;

									// The index depends on the whether the
									// "all others rule" is eneabled and
									// where it is positioned in the list!
									int index = row;

									transLabel = new TranslationEditJPanel(
											ASUtil.R(
													"RasterRulesList_Distinctvalues_GUI.LabelForClass",
													getRulesList().getValues()
															.get(index)),
											translation, AtlasStylerVector
													.getLanguages());

									ask = new TranslationAskJDialog(
											RasterRulesList_Intervals_GUI.this,
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

												getRulesList().getLabels().set(
														row, translation);

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
												RasterRulesList_Intervals_GUI.this,
												getRulesList().getLabels()
														.get(row).toString(),
												null);
								if (newTitle != null) {
									getRulesList().getLabels().set(row,
											new Translation(newTitle));
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
							// Click on the color field => Manually change the
							// color.
							final Color oldColor = rulesList.getColors().get(
									row);
							final Color newColor = AVSwingUtil
									.showColorChooser(
											RasterRulesList_Intervals_GUI.this,
											"", oldColor);

							if (newColor != oldColor) {
								rulesList.getColors().set(row, newColor);
								rulesList.fireEvents(new RuleChangedEvent(
										"Manually changed a color", rulesList));
							}
						}
					}

				}

			});

			// jTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

			jTable.setRowHeight(jTable.getRowHeight() + 2);

			/** Render nicely COLOR */
			jTable.setDefaultRenderer(Color.class, new ColorTableCellRenderer());
			SwingUtil.setColumnLook(jTable, COLIDX_COLOR,
					new ColorTableCellRenderer(), 30, 60, 100);
			SwingUtil.setColumnLook(jTable, COLIDX_OPACITY, null, 30, 40, 40);
			SwingUtil.setColumnLook(jTable, COLIDX_VALUE, null, 30, 100, 200);
		}
		return jTable;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonApplyPalette() {
		if (jButtonApplyPalette == null) {
			jButtonApplyPalette = new ThinButton(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {

					rulesList
							.applyPalette(RasterRulesList_Intervals_GUI.this);
				}
			});

			jButtonApplyPalette.setText(ASUtil
					.R("UniqueValues.applyPaletteButton.title"));
			jButtonApplyPalette.setToolTipText(ASUtil
					.R("UniqueValues.applyPaletteButton.toolTip"));

		}
		return jButtonApplyPalette;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxPalette() {
		if (jComboBoxPalette == null) {

			jComboBoxPalette = new JComboBoxBrewerPalettes(true);

			if (rulesList.getPalette() != null)
				jComboBoxPalette.getModel().setSelectedItem(
						rulesList.getPalette());
			else if (jComboBoxPalette.getSelectedItem() != null
					&& jComboBoxPalette.getSelectedItem() instanceof BrewerPalette) {
				rulesList.setPalette((BrewerPalette) jComboBoxPalette
						.getSelectedItem());
			}
			jComboBoxPalette.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						rulesList.setPalette((BrewerPalette) (e.getItem()));
						jComboBoxPalette.setToolTipText(rulesList.getPalette()
								.getDescription());
					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxPalette, false);
		}
		return jComboBoxPalette;
	}


}
