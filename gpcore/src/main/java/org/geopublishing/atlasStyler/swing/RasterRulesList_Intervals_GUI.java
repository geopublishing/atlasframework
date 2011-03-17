package org.geopublishing.atlasStyler.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RasterRulesList_Intervals;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.classification.CLASSIFICATION_METHOD;
import org.geopublishing.atlasStyler.classification.ClassificationChangeEvent;
import org.geopublishing.atlasStyler.classification.ClassificationChangedAdapter;
import org.geopublishing.atlasStyler.classification.RasterClassification;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geotools.brewer.color.BrewerPalette;

import de.schmitzm.i18n.Translation;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.ExceptionDialog;
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

	final static int COLIDX_COLOR = 0;
	final static int COLIDX_OPACITY = 1;
	final static int COLIDX_VALUE = 2;
	final static int COLIDX_LABEL = 3;

	private JTable jTable;
	private DefaultTableModel tableModel;
	private JComboBox jComboBoxOpacity;
	private ThinButton jButtonApplyOpacity;

	private final AtlasStylerRaster atlasStyler;

	public RasterRulesList_Intervals_GUI(RasterRulesList_Intervals rulesList,
			AtlasStylerRaster atlasStyler) {
		super(rulesList);
		this.atlasStyler = atlasStyler;

		rulesList.pushQuite();

		try {
			classifier = createAndConfigureClassifier(rulesList);

			initialize();
			rulesList.fireEvents(new RuleChangedEvent("GUI created for "
					+ this.getClass().getSimpleName()
					+ ", possibly setting some default values", rulesList));
		} finally {
			rulesList.popQuite();
		}
	}

	/**
	 * Creates a new FeatureClassificationGUIfied classifier and configures it
	 * with the number of classes etc. from the ruleslist. Also adds a listener
	 * that communicates classifier changes to the ruleslist.
	 */
	private RasterClassificationGUIfied createAndConfigureClassifier(
			final RasterRulesList_Intervals ruleList) {
		final RasterClassificationGUIfied newClassifier = new RasterClassificationGUIfied(
				RasterRulesList_Intervals_GUI.this,
				atlasStyler.getStyledRaster());

		newClassifier.pushQuite();
		try {


			/**
			 * If the ruleList doesn't contain calculated class limits, we have
			 * to start calculation directly.
			 */
			if (ruleList.getValues().size() == 0) {
				// Initialize some defaults
				newClassifier.setMethod(CLASSIFICATION_METHOD.QUANTILES);
				newClassifier.setNumClasses(5);
			} else {
				// Normal case
				newClassifier.setMethod(ruleList.getMethod());
				newClassifier.setNumClasses(ruleList.getNumClasses());
				newClassifier.setClassLimits(new TreeSet(ruleList.getValues()));
			}

			/**
			 * Any changes in the classifier must be reported to the RuleList
			 */
			newClassifier.addListener(new ClassificationChangedAdapter() {

				@Override
				public void classifierAvailableNewClasses(
						final ClassificationChangeEvent e) {

					ruleList.pushQuite();

					// // Checking if anything has really changed
					// boolean equalsValue = newClassifier.getValue_field_name()
					// .equals(ruleList.getValue_field_name());
					//
					// boolean equalsNormalizer = newClassifier
					// .getNormalizer_field_name() == ruleList
					// .getNormalizer_field_name()
					// || (newClassifier.getNormalizer_field_name() != null &&
					// newClassifier
					// .getNormalizer_field_name()
					// .equals(ruleList.getNormalizer_field_name()));
					boolean equalsNumClasses = newClassifier.getNumClasses() == ruleList
							.getValues().size()-1;
					boolean noChange = equalsNumClasses;

					try {

						// ruleList.setValue_field_name(newClassifier
						// .getValue_field_name());
						// ruleList.setNormalizer_field_name(newClassifier
						// .getNormalizer_field_name());
						// ruleList.setMethod(newClassifier.getMethod());
						ruleList.setValues(
								new ArrayList<Double>(newClassifier
										.getClassLimits()), !noChange); // here

						// On MANUAL mode deactivate the numClassesJComboBox,
						// otherwise enable it
						// if (newClassifier.getMethod() ==
						// CLASSIFICATION_METHOD.MANUAL) {
						// getNumClassesJComboBox().setEnabled(false);
						// getNumClassesJComboBox().setSelectedItem(
						// new Integer(ruleList.getNumClasses()));
						// } else
						// getNumClassesJComboBox().setEnabled(true);

					} finally {
						ruleList.popQuite();
					}

				}
			});

		} finally {
			newClassifier.popQuite();
		}

		return newClassifier;
	}

	private final RasterClassification classifier;

	protected SwingWorker<TreeSet<Double>, String> calculateStatisticsWorker;

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JToggleButton getClassifyJToggleButton() {
		final JToggleButton jToggleButton_Classify = new JToggleButton();
		jToggleButton_Classify.setAction(new AbstractAction(ASUtil
				.R("GraduatedColorQuantities.Classify.Button")) {

			private ClassificationGUI openQuantitiesClassificationGUI;

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (jToggleButton_Classify.isSelected()) {

					// Test here, if the data is problematic and show the
					// exception to the user without opening the dialog.
					try {
						classifier.getStatistics();
					} catch (final Exception eee) {
						jToggleButton_Classify.setSelected(false);
						ExceptionDialog.show(
								RasterRulesList_Intervals_GUI.this, eee);
						return;
					}

					openQuantitiesClassificationGUI = getQuantitiesClassificationGUI();
					openQuantitiesClassificationGUI.setVisible(true);
				} else {
					if (openQuantitiesClassificationGUI != null)
						openQuantitiesClassificationGUI.setVisible(false);
					openQuantitiesClassificationGUI = null;
				}

			}

			private ClassificationGUI getQuantitiesClassificationGUI() {
				// Title like :
				ClassificationGUI quantGUI = new RasterClassificationGUI(
						jToggleButton_Classify, classifier, atlasStyler, ASUtil
								.R("QuantitiesClassificationGUI.Title", ""));
				quantGUI.addWindowListener(new WindowAdapter() {

					@Override
					public void windowClosed(final WindowEvent e) {
						jToggleButton_Classify.setSelected(false);
					}

					@Override
					public void windowClosing(final WindowEvent e) {
						jToggleButton_Classify.setSelected(false);
					}

				});
				// }
				return quantGUI;
			}

		});
		jToggleButton_Classify.setToolTipText(ASUtil
				.R("GraduatedColorQuantities.Classify.Button.TT"));
		return jToggleButton_Classify;
	}

	private void initialize() {
		new JLabel(ASUtil.R("RasterRulesList_Intervals_GUI.Heading"))
				.setFont(new JLabel(ASUtil
						.R("RasterRulesList_Intervals_GUI.Heading")).getFont()
						.deriveFont(AVSwingUtil.HEADING_FONT_SIZE));
		this.setLayout(new MigLayout("inset 1, gap 1, wrap 1, fillx"));

		this.add(new JLabel(ASUtil.R("RasterRulesList_Intervals_GUI.Heading")),
				"center");

		this.add(getJPanelColorAndOpacity(), "align l, split 2");
		this.add(getClassifyJToggleButton(), "align l, split 2");

		this.add(new JScrollPane(getJTable()), "grow x, grow y 20000");
	}

	private Component getJPanelColorAndOpacity() {
		final JPanel jPanelColorAndTemplate = new JPanel(new MigLayout(
				"wrap 2, inset 1, gap 1", "[grow][]"));
		jPanelColorAndTemplate
				.setBorder(BorderFactory.createTitledBorder(ASUtil
						.R("RasterRulesList_PanelBorderTitle.Colors_and_Opacity")));

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

			if (rulesList.getOpacity() != null) {
				ASUtil.selectOrInsert(jComboBoxOpacity, rulesList.getOpacity()
						.floatValue());
			}

			jComboBoxOpacity.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					rulesList.setOpacity(((Float) jComboBoxOpacity
							.getSelectedItem()).doubleValue());
				}
			});

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
		}
		return jButtonApplyOpacity;
	}

	/**
	 * Listen for changes in the RulesList. Must be kept as a reference in
	 * {@link RasterRulesList_Intervals_GUI} because the listeners are kept in a
	 * {@link WeakHashMap}
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
						return String.class;

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
						return ASUtil.R("ColorLabel");
					if (columnIndex == COLIDX_OPACITY)
						return ASUtil.R("OpacityLabel");
					if (columnIndex == COLIDX_VALUE)
						return ASUtil
								.R("GraduatedColorQuantities.Column.Limits");
					if (columnIndex == COLIDX_LABEL)
						return ASUtil.R("LabelLabel");
					return super.getColumnName(columnIndex);
				}

				@Override
				public int getRowCount() {
					return getRulesList().getNumClasses();
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {

					if (columnIndex == COLIDX_COLOR) {
						if (rulesList.getOpacities().get(rowIndex) <= 0.) {
							// Do not sh
							return null;
						}
						return rulesList.getColors().get(rowIndex);
					} else if (columnIndex == COLIDX_OPACITY) {
						return rulesList.getOpacities().get(rowIndex);
					} else if (columnIndex == COLIDX_VALUE) {

						final Number lower = rulesList.getValues()
								.get(rowIndex);

						DecimalFormat formatter = rulesList.getFormatter();

						if (rowIndex + 1 < rulesList.getValues().size()) {
							final Number upper = rulesList.getValues().get(
									rowIndex + 1);

							final String limitsLabel = formatter.format(lower)
									+ " -> " + formatter.format(upper);
							return limitsLabel;
						} else {
							final String limitsLabel = formatter.format(lower);
							return limitsLabel;
						}
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
			// if (1 == 1.)
			// return new JTable();
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
													"RasterRulesList_Intervals_GUI.LabelForClass",
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
								String newTitle = ASUtil.askForString(
										RasterRulesList_Intervals_GUI.this,
										getRulesList().getLabels().get(row)
												.toString(), null);
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

			classifier.addListener(new ClassificationChangedAdapter() {

				@Override
				public void classifierAvailableNewClasses(
						final ClassificationChangeEvent e) {
					jTable.setEnabled(true);
					getTableModel().fireTableStructureChanged();
				}

				@Override
				public void classifierCalculatingStatistics(
						final ClassificationChangeEvent e) {

					jTable.setEnabled(false);

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

					rulesList.applyPalette(RasterRulesList_Intervals_GUI.this);
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
