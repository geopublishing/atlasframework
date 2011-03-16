/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Tzeggai.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Tzeggai - initial API and implementation
 ******************************************************************************/
package org.geopublishing.atlasStyler.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.classification.ClassificationChangeEvent;
import org.geopublishing.atlasStyler.classification.ClassificationChangedAdapter;
import org.geopublishing.atlasStyler.classification.QuantitiesClassification.METHOD;
import org.geopublishing.atlasStyler.rulesLists.GraduatedColorRuleList;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;
import org.geopublishing.atlasStyler.swing.classification.QuantitiesClassification;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.PaletteType;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.map.event.MapLayerListener;
import org.geotools.styling.Symbolizer;

import de.schmitzm.geotools.data.amd.AttributeMetadataImpl;
import de.schmitzm.geotools.data.amd.AttributeMetadataMap;
import de.schmitzm.geotools.map.event.MapLayerAdapter;
import de.schmitzm.i18n.Translation;
import de.schmitzm.swing.Disposable;
import de.schmitzm.swing.ExceptionDialog;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SmallButton;
import de.schmitzm.swing.SwingUtil;
import de.schmitzm.swing.TranslationAskJDialog;
import de.schmitzm.swing.TranslationEditJPanel;

public class GraduatedColorQuantitiesGUI extends
		AbstractRulesListGui<GraduatedColorRuleList> implements
		ClosableSubwindows, Disposable {

	private static final Dimension ICON_SIZE = new Dimension(25, 25);

	final protected static Logger LOGGER = Logger
			.getLogger(GraduatedColorQuantitiesGUI.class);

	protected static final int COLIDX_COLOR = 0;

	protected static final int COLIDX_LIMIT = 1;

	protected static final int COLIDX_LABEL = 2;

	private JPanel jPanel = null;

	private JLabel jLabelClassificationTypeDescription = null;

	private JComboBox jComboBoxValueField = null;

	private JComboBox jComboBoxNormlization = null;

	private JTable jTable = null;

	private JLabel jLabelHeading = null;

	private final QuantitiesClassification classifier;

	protected SwingWorker<TreeSet<Double>, String> calculateStatisticsWorker;

	private DefaultTableModel tableModel;

	private JComboBoxBrewerPalettes jComboBoxPalettes = null;

	private JLabel jLabelTemplate = null;

	private JButton jButtonTemplate = null;

	private final AtlasStylerVector atlasStyler;

	private NumClassesJComboBox jComboBoxNumClasses;

	private final MapLayerListener listenToFilterChangesAndRecalcStatistics = new MapLayerAdapter() {

		@Override
		public void layerChanged(MapLayerEvent event) {
			if (event.getReason() == MapLayerEvent.FILTER_CHANGED)
				classifier.onFilterChanged();
		}
	};

	private JPanel noDataPanel;

	/**
	 * This is the default constructor
	 */
	public GraduatedColorQuantitiesGUI(final GraduatedColorRuleList ruleList,
			final AtlasStylerVector atlasStyler) {

		super(ruleList);

		ruleList.pushQuite();

		try {

			this.atlasStyler = atlasStyler;

			classifier = new QuantitiesClassification(
					GraduatedColorQuantitiesGUI.this,
					ruleList.getStyledFeatures(),
					ruleList.getValue_field_name(),
					ruleList.getNormalizer_field_name());

			classifier.pushQuite();
			try {

				classifier.setMethod(ruleList.getMethod());
				classifier.setNumClasses(ruleList.getNumClasses());
				classifier.setClassLimits(ruleList.getClassLimits());

				/**
				 * If the ruleList doesn't contain calculated class limits, we
				 * have to start calculation directly.
				 */
				if (ruleList.getClassLimits().size() == 0) {
					classifier.setMethod(METHOD.QUANTILES);
					classifier.setNumClasses(5);
				}

				/**
				 * Any changes to the classifier must be reported to the
				 * RuleList
				 */
				classifier.addListener(new ClassificationChangedAdapter() {

					@Override
					public void classifierAvailableNewClasses(
							final ClassificationChangeEvent e) {

						ruleList.pushQuite();

						// Checking if anything has really changed
						boolean equalsValue = classifier.getValue_field_name()
								.equals(ruleList.getValue_field_name());

						boolean equalsNormalizer = classifier
								.getNormalizer_field_name() == ruleList
								.getNormalizer_field_name()
								|| (classifier.getNormalizer_field_name() != null && classifier
										.getNormalizer_field_name()
										.equals(ruleList
												.getNormalizer_field_name()));
						boolean equalsNumClasses = classifier.getNumClasses() == ruleList
								.getNumClasses();
						boolean noChange = equalsValue && equalsNormalizer
								&& equalsNumClasses;

						try {

							ruleList.setValue_field_name(classifier
									.getValue_field_name());
							ruleList.setNormalizer_field_name(classifier
									.getNormalizer_field_name());
							ruleList.setMethod(classifier.getMethod());
							ruleList.setClassLimits(
									classifier.getClassLimits(), !noChange); // here

							if (classifier.getMethod() == METHOD.MANUAL) {
								getNumClassesJComboBox().setEnabled(false);
								getNumClassesJComboBox().setSelectedItem(
										new Integer(ruleList.getNumClasses()));
							} else
								getNumClassesJComboBox().setEnabled(true);

						} finally {
							ruleList.popQuite();
						}

					}
				});

			} finally {
				classifier.popQuite();
			}

			if (atlasStyler.getMapLayer() != null)
				atlasStyler.getMapLayer().addMapLayerListener(
						listenToFilterChangesAndRecalcStatistics);

			initialize();

		} finally {
			ruleList.popQuite();
		}
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		setLayout(new MigLayout("wrap 2, fillx", "grow", "grow"));
		jLabelHeading = new JLabel(
				AtlasStylerVector.R("GraduatedColorQuantities.Heading"));
		jLabelHeading.setFont(jLabelHeading.getFont().deriveFont(
				AVSwingUtil.HEADING_FONT_SIZE));

		this.add(jLabelHeading, "span 2");
		this.add(getJPanelFields(), "grow x");
		this.add(getJPanelClassification(), "grow x");
		this.add(getJPanelColorsAndTemplate(), "span 2, growx");
		this.add(new JScrollPane(getJTableClasses()),
				"span 2, height 50:150:600, grow x, bottom");
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelFields() {
		if (jPanel == null) {
			jPanel = new JPanel(new MigLayout("inset 1, gap 1, wrap 2"));
			// jPanel.setBorder(BorderFactory.createTitledBorder(AtlasStyler
			// .R("GraduatedColorQuantities.Attributes.BorderTitle")));

			JLabel jLabelNormalization = new JLabel(
					AtlasStylerVector
							.R("GraduatedColorQuantities.NormalizationAttribute"));
			jLabelNormalization.setToolTipText(AtlasStylerVector
					.R("GraduatedColorQuantities.NormalizationAttribute.TT"));
			JLabel jLabelValue = new JLabel(
					AtlasStylerVector
							.R("GraduatedColorQuantities.ValueAttribute"));
			jLabelValue.setToolTipText(AtlasStylerVector
					.R("GraduatedColorQuantities.ValueAttribute.TT"));
			jPanel.add(jLabelValue, "");
			jPanel.add(getJComboBoxValueField(), "sgx1, growx");
			jPanel.add(jLabelNormalization, "");
			jPanel.add(getJComboBoxNormalizationField(), "sgx1, growx");
		}
		return jPanel;
	}

	/**
	 * This method initializes jPanel2
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelClassification() {

		JLabel jLabelParam = new JLabel(
				AtlasStylerVector.R("ComboBox.NumberOfClasses"));
		jLabelParam.setToolTipText(AtlasStylerVector
				.R("ComboBox.NumberOfClasses.TT"));

		jLabelClassificationTypeDescription = new JLabel(AtlasStylerVector.R(
				"GraduatedColorQuantities.classification.Method", classifier
						.getMethod().getDesc()));

		jLabelClassificationTypeDescription.setToolTipText(classifier
				.getMethod().getToolTip());

		classifier.addListener(new ClassificationChangedAdapter() {

			@Override
			public void classifierMethodChanged(
					final ClassificationChangeEvent e) {
				jLabelClassificationTypeDescription.setText(AtlasStylerVector
						.R("GraduatedColorQuantities.classification.Method",
								classifier.getMethod().getDesc()));
				jLabelClassificationTypeDescription.setToolTipText(classifier
						.getMethod().getToolTip());
			}

		});

		JPanel jPanelClassification = new JPanel(
				new MigLayout("inset 1, gap 1"),
				AtlasStylerVector
						.R("GraduatedColorQuantities.classification.BorderTitle"));
		jPanelClassification.add(jLabelClassificationTypeDescription, "wrap");
		jPanelClassification.add(jLabelParam, "split 3");
		jPanelClassification.add(getNumClassesJComboBox());
		jPanelClassification.add(getClassifyJToggleButton());

		return jPanelClassification;
	}

	private NumClassesJComboBox getNumClassesJComboBox() {
		if (jComboBoxNumClasses == null) {
			jComboBoxNumClasses = new NumClassesJComboBox(classifier);
		}
		return jComboBoxNumClasses;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JToggleButton getClassifyJToggleButton() {
		final JToggleButton jToggleButton_Classify = new JToggleButton();
		jToggleButton_Classify.setAction(new AbstractAction(AtlasStylerVector
				.R("GraduatedColorQuantities.Classify.Button")) {

			@Override
			public void actionPerformed(final ActionEvent e) {
				if (jToggleButton_Classify.isSelected()) {

					// Test here, if the data is problematic and show the
					// exception to the user without opening the dialog.
					try {
						classifier.getStatistics();
					} catch (final Exception eee) {
						jToggleButton_Classify.setSelected(false);
						ExceptionDialog.show(GraduatedColorQuantitiesGUI.this,
								eee);
						return;
					}

					getQuantitiesClassificationGUI().setVisible(true);
				} else {
					getQuantitiesClassificationGUI().setVisible(false);
				}

			}

			private QuantitiesClassificationGUI getQuantitiesClassificationGUI() {
				// if (quantGUI == null) {
				AttributeMetadataMap<AttributeMetadataImpl> attributeMetaDataMap = rulesList
						.getStyledFeatures().getAttributeMetaDataMap();

				// Title like :
				String titleVariables = attributeMetaDataMap
						.get(classifier.getValue_field_name()).getTitle()
						.toString();

				if (classifier.getNormalizer_field_name() != null
						&& !classifier.getNormalizer_field_name().isEmpty()) {
					titleVariables += ":"
							+ attributeMetaDataMap
									.get(classifier.getNormalizer_field_name())
									.getTitle().toString();
				}

				QuantitiesClassificationGUI quantGUI = new QuantitiesClassificationGUI(
						jToggleButton_Classify, classifier, atlasStyler,
						AtlasStylerVector.R(
								"QuantitiesClassificationGUI.Title",
								titleVariables));
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
		jToggleButton_Classify.setToolTipText(AtlasStylerVector
				.R("GraduatedColorQuantities.Classify.Button.TT"));
		return jToggleButton_Classify;
	}

	/**
	 * This method initializes jComboBox1
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxValueField() {
		if (jComboBoxValueField == null) {
			jComboBoxValueField = new AttributesJComboBox(atlasStyler,
					classifier.getValueFieldsComboBoxModel());

			jComboBoxValueField
					.setSelectedItem(rulesList.getValue_field_name());

			jComboBoxValueField.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final java.awt.event.ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						final String valueField = (String) e.getItem();

						getJComboBoxNormalizationField()
								.setModel(
										classifier
												.createNormalizationFieldsComboBoxModel());

						LOGGER.debug("Set valuefield to " + valueField);
						classifier.setValue_field_name(valueField);
						//
						// // When the valueField has been changed by
						// the
						// // user, throw away the ruleTitles
						// rulesList.getRuleTitles().clear();
					}
				}
			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxValueField, false);
		}
		return jComboBoxValueField;
	}

	/**
	 * This method initializes jComboBox2
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxNormalizationField() {
		if (jComboBoxNormlization == null) {

			jComboBoxNormlization = new AttributesJComboBox(atlasStyler,
					classifier.createNormalizationFieldsComboBoxModel());

			jComboBoxNormlization.setSelectedItem(rulesList
					.getNormalizer_field_name());

			// jComboBoxNormlization.addItem(null); // means "no normalization"
			// REMOVED, is now already in the model creation

			jComboBoxNormlization
					.addItemListener(new java.awt.event.ItemListener() {
						@Override
						public void itemStateChanged(
								final java.awt.event.ItemEvent e) {
							if (e.getStateChange() == ItemEvent.SELECTED) {

								if (org.geopublishing.atlasStyler.classification.QuantitiesClassification.NORMALIZE_NULL_VALUE_IN_COMBOBOX == e
										.getItem())
									classifier.setNormalizer_field_name(null);
								else
									classifier
											.setNormalizer_field_name((String) e
													.getItem());
								//
								// // When the normalizationField has been
								// changed
								// // by the user, throw away the ruleTitles
								// rulesList.getRuleTitles().clear();

							}
						}
					});
			SwingUtil.addMouseWheelForCombobox(jComboBoxNormlization, false);
		}
		return jComboBoxNormlization;
	}

	/**
	 * This method initializes jTable
	 */
	private JTable getJTableClasses() {
		if (jTable == null) {
			jTable = new JTable(getTableModel());

			jTable.setDefaultRenderer(Color.class, new ColorTableCellRenderer());

			((JLabel) jTable.getDefaultRenderer(String.class))
					.setHorizontalAlignment(SwingConstants.RIGHT);

			/*******************************************************************
			 * Listening to clicks on the JTable.
			 */
			jTable.addMouseListener(new MouseAdapter() {

				private TranslationAskJDialog ask;

				@Override
				public void mouseClicked(final MouseEvent e) {

					if (e.getClickCount() == 1) {
						final int col = jTable.columnAtPoint(e.getPoint());
						final int row = jTable.rowAtPoint(e.getPoint());

						if (col == COLIDX_COLOR) {
							// Click on the color field => Manually change the
							// color.
							final Color oldColor = rulesList.getColors()[row];
							final Color newColor = AVSwingUtil
									.showColorChooser(
											GraduatedColorQuantitiesGUI.this,
											"", oldColor);

							if (newColor != oldColor) {
								rulesList.getColors()[row] = newColor;
								rulesList.fireEvents(new RuleChangedEvent(
										"Manually changed a color", rulesList));
							}

						} else if (col == COLIDX_LIMIT) {
							JOptionPane.showMessageDialog(
									SwingUtil
											.getParentWindowComponent(GraduatedColorQuantitiesGUI.this),
									AtlasStylerVector
											.R("GraduatedColorQuantities.ClassesTable.ClickLimits.Message",
													AtlasStylerVector
															.R("GraduatedColorQuantities.Classify.Button")));
							return;
						}

						if (col != COLIDX_LABEL)
							return;

						/**
						 * If its a right mouse click, we open a context menu
						 * which allows to reset all labels to default.
						 */
						if (e.isPopupTrigger()) {
							final JPopupMenu toolPopup = new JPopupMenu();
							toolPopup.add(new JMenuItem(
									new AbstractAction(
											AtlasStylerVector
													.R("GraduatedColorQuantities.ClassesTable.PopupMenuCommand.ResetLabels")) {

										@Override
										public void actionPerformed(
												final ActionEvent e) {
											rulesList.setClassLimits(
													rulesList.getClassLimits(),
													true);
											jTable.repaint();
										}

									}));
							toolPopup.show(jTable, e.getX(), e.getY());
							return;
						}

						final String ruleTitle = rulesList.getRuleTitles().get(
								row);

						if (AtlasStylerVector.getLanguageMode() == AtlasStylerVector.LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {
							final Translation translation = new Translation(
									ruleTitle);

							if (ask == null) {

								final TranslationEditJPanel transLabel = new TranslationEditJPanel(
										AtlasStylerVector
												.R("GraduatedColorsQuant.translate_label_for_classN",
														(row + 1)),
										translation, AtlasStylerVector
												.getLanguages());

								ask = new TranslationAskJDialog(
										GraduatedColorQuantitiesGUI.this,
										transLabel);
								ask.addPropertyChangeListener(new PropertyChangeListener() {

									@Override
									public void propertyChange(
											final PropertyChangeEvent evt) {
										if (evt.getPropertyName()
												.equals(TranslationAskJDialog.PROPERTY_CANCEL_AND_CLOSE)) {
											ask = null;
										}
										if (evt.getPropertyName()
												.equals(TranslationAskJDialog.PROPERTY_APPLY_AND_CLOSE)) {
											rulesList.getRuleTitles().put(row,
													translation.toOneLine());
										}
										ask = null;
										rulesList
												.fireEvents(new RuleChangedEvent(
														"Legend Label changed",
														rulesList));
									}

								});

							}
							ask.setVisible(true);
						} else {
							/***************************************************
							 * Simple OGC conform labels.. not in multi-language
							 * mode
							 */
							final String newTitle = ASUtil.askForString(
									GraduatedColorQuantitiesGUI.this,
									ruleTitle,
									AtlasStylerVector
											.R("GraduatedColorsQuant.translate_label_for_classN",
													(row + 1)));

							if (newTitle != null) {
								rulesList.getRuleTitles().put(row, newTitle);

								rulesList.fireEvents(new RuleChangedEvent(
										"Legend Label changed", rulesList));
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

			jTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			jTable.getTableHeader().setResizingAllowed(true);
			final TableColumn col = jTable.getColumnModel().getColumn(0);
			col.setMinWidth(40);
			col.setMaxWidth(40);
			col.setPreferredWidth(40);

		}
		return jTable;
	}

	public DefaultTableModel getTableModel() {
		if (tableModel == null) {

			tableModel = new DefaultTableModel() {

				@Override
				public Class<?> getColumnClass(final int columnIndex) {
					if (columnIndex == COLIDX_COLOR) // Colors
						return Color.class;

					if (columnIndex == COLIDX_LIMIT) // Limits
						return String.class;

					if (columnIndex == COLIDX_LABEL) // Label
						return String.class;

					return null;
				}

				@Override
				public int getColumnCount() {
					return 3;
				}

				@Override
				public String getColumnName(final int columnIndex) {
					if (columnIndex == COLIDX_COLOR)
						return AtlasStylerVector
								.R("GraduatedColorQuantities.Column.Color");
					if (columnIndex == 1)
						return AtlasStylerVector
								.R("GraduatedColorQuantities.Column.Limits");
					if (columnIndex == 2)
						return AtlasStylerVector
								.R("GraduatedColorQuantities.Column.Label");
					return super.getColumnName(columnIndex);
				}

				@Override
				public int getRowCount() {
					final int numClasses = rulesList.getNumClasses();
					int i = numClasses >= 0 ? numClasses : 0;
					return i;
				}

				@Override
				public Object getValueAt(final int rowIndex,
						final int columnIndex) {

					/***********************************************************
					 * getValue 0 Color
					 */
					if (columnIndex == 0) { // Color
						return rulesList.getColors()[rowIndex];
					}

					/***********************************************************
					 * getValue 1 Limit
					 */
					if (columnIndex == 1) { // Limits

						final ArrayList<Double> classLimitsAsArrayList = rulesList
								.getClassLimitsAsArrayList();

						final Number lower = classLimitsAsArrayList
								.get(rowIndex);

						DecimalFormat formatter = rulesList.getFormatter();

						if (rowIndex + 1 < classLimitsAsArrayList.size()) {
							final Number upper = classLimitsAsArrayList
									.get(rowIndex + 1);

							final String limitsLabel = formatter.format(lower)
									+ " -> " + formatter.format(upper);
							return limitsLabel;
						} else {
							final String limitsLabel = formatter.format(lower);
							return limitsLabel;
						}

					}

					/***********************************************************
					 * getValue 2 Label
					 */
					if (columnIndex == 2) { // Label

						String string = rulesList.getRuleTitles().get(rowIndex);

						if (AtlasStylerVector.getLanguageMode() == AtlasStylerVector.LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {
							string = new Translation(string).toString();
						}

						return string;
					}

					return super.getValueAt(rowIndex, columnIndex);
				}

			};

		}
		return tableModel;
	}

	/**
	 * This method initializes jPanel3
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelColorsAndTemplate() {
		final JPanel panel = new JPanel(new MigLayout("width 100%", "grow"));

		jLabelTemplate = new JLabel(
				AtlasStylerVector.R("GraduatedColorQuantities.Template"));
		jLabelTemplate.setToolTipText(AtlasStylerVector
				.R("GraduatedColorQuantities.Template.TT"));

		final JLabel jLabelColorPalette = new JLabel(
				AtlasStylerVector.R("GraduatedColorQuantities.ColorRamp"));

		panel.add(jLabelColorPalette, "left");
		panel.add(getJComboBoxColors(), "left");
		panel.add(getInvertColorsButton(), "gap rel, left");

		panel.add(getNoDataPanel());

		panel.add(new JPanel(), "growx");
		panel.add(jLabelTemplate, "gapx unrelated, right");
		panel.add(getJButtonTemplate(), "right");

		return panel;
	}

	private JPanel getNoDataPanel() {
		if (noDataPanel == null) {
			noDataPanel = new JPanel(new MigLayout());

			final SingleRuleList<? extends Symbolizer> noDataSymbol = rulesList
					.getNoDataSymbol();

			final SmallButton noDataLabelButton = new SmallButton(
					new Translation(noDataSymbol.getLabel()) + ":",
					AtlasStylerVector.R("translate_label_for_NODATA_values.tt"));
			noDataLabelButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (AtlasStyler.getLanguageMode() == AtlasStyler.LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {

						// Open a dialog that allows to edit the NODATA legend
						// entries label

						final Translation translation = new Translation(
								noDataSymbol.getLabel());

						final TranslationEditJPanel transLabel = new TranslationEditJPanel(
								ASUtil
										.R("translate_label_for_NODATA_values"),
								translation, AtlasStylerVector.getLanguages());
						TranslationAskJDialog ask = new TranslationAskJDialog(
								GraduatedColorQuantitiesGUI.this, transLabel);

						// We have to convert the Translation object to a String
						// when the dialog is closed
						ask.addPropertyChangeListener(new PropertyChangeListener() {

							@Override
							public void propertyChange(
									final PropertyChangeEvent evt) {
								if (evt.getPropertyName()
										.equals(TranslationAskJDialog.PROPERTY_CANCEL_AND_CLOSE)) {
									return;
								} else if (evt
										.getPropertyName()
										.equals(TranslationAskJDialog.PROPERTY_APPLY_AND_CLOSE)) {

									noDataLabelButton.setText(translation
											.toString() + ":");
									noDataSymbol.setTitle("NODATARULE");
									noDataSymbol.setRuleTitle(translation);

									rulesList
											.getNoDataSymbol()
											.fireEvents(
													new RuleChangedEvent(
															"nodata legend label changed",
															noDataSymbol));

								}

							}

						});
						ask.setVisible(true);

					} else {
						final String newNodataTitle = ASUtil.askForString(
								GraduatedColorQuantitiesGUI.this, noDataSymbol
										.getLabel(), ASUtil
										.R("translate_label_for_NODATA_values"));

						if (newNodataTitle != null) {
							noDataLabelButton.setText(newNodataTitle + ":");
							noDataSymbol.setLabel(newNodataTitle);

							rulesList.getNoDataSymbol().fireEvents(
									new RuleChangedEvent(
											"nodata legend label changed",
											rulesList.getNoDataSymbol()));
						}
					}
				}
			});

			noDataPanel.add(noDataLabelButton, "gap 0");

			// A button to change the NODATA symbol
			SymbolButton noDataSymbolButton = new EditSymbolButton(
					rulesList.getNoDataSymbol(), new Dimension(14, 14));
			noDataPanel.add(noDataSymbolButton, "gap 0, wrap");

			// If running in GP/Atlas context, the user may disable the NODATA
			// values from appearing in the legend
			if (AtlasStylerVector.getLanguageMode() == AtlasStylerVector.LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {

				final JCheckBox noDataShowInLegendCB = new JCheckBox(
						ASUtil
								.R("NoDataValues.ShallAppearInLegend.Label"));
				noDataShowInLegendCB.setToolTipText(ASUtil
						.R("NoDataValues.ShallAppearInLegend.TT"));

				// Initially set the value depending on the rules's name
				noDataShowInLegendCB.setSelected(rulesList.getNoDataSymbol()
						.isVisibleInLegend());

				noDataShowInLegendCB.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						boolean selected = noDataShowInLegendCB.isSelected();
						rulesList.getNoDataSymbol()
								.setVisibleInLegend(selected);
					}
				});

				noDataPanel.add(noDataShowInLegendCB, "gap 0, span 2");

			} else {
			}

		}
		return noDataPanel;
	}

	/**
	 * A button to invert the order of the applied colors
	 * 
	 * @return
	 */
	private JButton getInvertColorsButton() {
		final JButton button = new JButton(new AbstractAction("",
				Icons.AS_REVERSE_COLORORDER) {

			@Override
			public void actionPerformed(final ActionEvent e) {
				final Color[] colors = rulesList.getColors();
				final List<Color> asList = Arrays.asList(colors);
				Collections.reverse(asList);
				getTableModel().fireTableDataChanged();
				rulesList.fireEvents(new RuleChangedEvent("Colors changed",
						rulesList));
			}

		});

		button.setMargin(new Insets(1, 1, 1, 1));

		// button.setBorder( BorderFactory.createEmptyBorder(1,1,1,1));

		button.setToolTipText(AtlasStylerVector
				.R("GraduatedColorQuantities.ColorRamp.RevertButton.TT"));

		return button;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxColors() {

		if (jComboBoxPalettes == null) {

			// final DefaultComboBoxModel aModel = new DefaultComboBoxModel(
			// ASUtil.getPalettes(new PaletteType(true, false),
			// classifier.getNumClasses()));
			//
			// jComboBoxPalettes = new JComboBox(aModel);
			//
			// jComboBoxPalettes.setRenderer(new PaletteCellRenderer());

			jComboBoxPalettes = new JComboBoxBrewerPalettes(false);

			jComboBoxPalettes.getModel().setSelectedItem(
					rulesList.getBrewerPalette());

			jComboBoxPalettes.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						rulesList.setBrewerPalette((BrewerPalette) (e.getItem()));
						getTableModel().fireTableDataChanged();
					}
				}

			});

			classifier.addListener(new ClassificationChangedAdapter() {

				/**
				 * Create a new list of BrewerPalettes depending on the number
				 * of classes. Reselect the old selection if it is still
				 * contained in the new list *
				 */
				@Override
				public void classifierAvailableNewClasses(
						final ClassificationChangeEvent e) {

					final BrewerPalette oldSelection = (BrewerPalette) jComboBoxPalettes
							.getSelectedItem();

					final BrewerPalette[] palettes = ASUtil.getPalettes(
							new PaletteType(true, false),
							classifier.getNumClasses());
					jComboBoxPalettes.setModel(new DefaultComboBoxModel(
							palettes));

					if (oldSelection != null)
						for (final BrewerPalette bp : palettes) {
							if (bp.getDescription().equals(
									oldSelection.getDescription())) {
								jComboBoxPalettes.setSelectedItem(bp);
								break;
							}
						}

					rulesList
							.setBrewerPalette((BrewerPalette) jComboBoxPalettes
									.getSelectedItem());
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxPalettes, false);
		}
		return jComboBoxPalettes;
	}

	/**
	 * A backup of the template symbol. Used when the GUI opens.
	 */
	protected SingleRuleList<? extends Symbolizer> backup;

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonTemplate() {
		if (jButtonTemplate == null) {
			jButtonTemplate = new EditSymbolButton(rulesList.getTemplate(),
					ICON_SIZE);

			// final ImageIcon imageIcon = new ImageIcon(rulesList.getTemplate()
			// .getImage(ICON_SIZE));

		}
		return jButtonTemplate;
	}

	@Override
	public void dispose() {
		if (classifier != null)
			classifier.dispose();

		if (atlasStyler != null) {
			if (atlasStyler.getMapLayer() != null)
				atlasStyler.getMapLayer().removeMapLayerListener(
						listenToFilterChangesAndRecalcStatistics);
		}
	}

}
