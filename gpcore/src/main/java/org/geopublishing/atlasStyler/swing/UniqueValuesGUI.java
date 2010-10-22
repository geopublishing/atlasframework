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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CancellationException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.SingleRuleList;
import org.geopublishing.atlasStyler.UniqueValuesRuleList;
import org.geopublishing.atlasViewer.swing.AtlasSwingWorker;
import org.geopublishing.atlasViewer.swing.Icons;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;
import org.geotools.brewer.color.PaletteType;
import org.geotools.styling.Symbolizer;

import schmitzm.lang.LangUtil;
import schmitzm.swing.ExceptionDialog;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.i8n.Translation;
import skrueger.swing.ThinButton;
import skrueger.swing.TranslationAskJDialog;
import skrueger.swing.TranslationEditJPanel;

public class UniqueValuesGUI extends JPanel implements ClosableSubwindows {
	protected Logger LOGGER = ASUtil.createLogger(this);

	/**
	 * Listen for changes in the RuleList. Must be kept as a reference in
	 * {@link UniqueValuesGUI} because the listeners are kept in a
	 * {@link WeakHashMap}
	 */
	final RuleChangeListener updateClassificationTableWhenRuleListChanges = new RuleChangeListener() {

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

	private JLabel jLabelHeading = null;

	private JLabel jLabelValue = null;

	private JComboBox jComboBoxPalette = null;

	private JButton jButtonApplyPalette = null;

	private JLabel jLabelTemplate = null;

	private JButton jButtonTemplate = null;

	private JButton jButtonApplyTemplate = null;

	private JButton jButtonAddAllValues = null;

	private JButton jButtonAddValues = null;

	private JButton jButtonRemove = null;

	private JButton jButtonRemoveAll = null;

	private JButton jButtonUp = null;

	private JButton jButtonDown = null;

	private JTable jTable = null;

	private DefaultTableModel tableModel;

	/**
	 * Used for the "move up" and "move down" buttons.
	 */
	private final static Dimension SMALLBUTTONSIZE = new Dimension(12, 16);

	protected final AtlasStyler atlasStyler;

	/**
	 * This is the RuleList this GUI is working on.
	 */
	private final UniqueValuesRuleList rulesList;

	public static final Dimension ICON_SIZE = AtlasStyler.DEFAULT_SYMBOL_PREVIEW_SIZE;

	public UniqueValuesGUI(UniqueValuesRuleList catRuleList,
			AtlasStyler atlasStyler) {
		this.rulesList = catRuleList;
		this.atlasStyler = atlasStyler;
		initialize();
		rulesList.fireEvents(new RuleChangedEvent("GUI opened", rulesList));
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getAttributeJPanel() {
		// JPanel jPanelValueField = new JPanel(new MigLayout("inset 1, gap 1"),
		// AtlasStyler.R("UniqueValuesGUI.borderTitle.value_Field"));
		//
		// jLabelValue = new JLabel(
		// AtlasStyler
		// .R("UniqueValuesGUI.labelFor.valueFieldSelectionCombobox"));

		JPanel jPanelValueField = new JPanel(new MigLayout("inset 1, gap 4"));

		jLabelValue = new JLabel(
				AtlasStyler.R("UniqueValuesGUI.borderTitle.value_Field"));

		jPanelValueField.add(jLabelValue);
		jPanelValueField.add(getJComboBoxValueField(), "growx, right, wrap");

		jPanelValueField.add(getWithDefaultCheckbox(), "span 2, align right");

		return jPanelValueField;
	}

	/**
	 * This method initializes a AttributesJComboBox with attributes to use for
	 * the classification
	 */
	private AttributesJComboBox getJComboBoxValueField() {
		AttributesJComboBox jComboBoxValueAttribute = new AttributesJComboBox(
				atlasStyler, ASUtil.getValueFieldNames(rulesList
						.getStyledFeatures().getSchema()));

		jComboBoxValueAttribute.setSelectedItem(rulesList
				.getPropertyFieldName());
		// This
		// may
		// well
		// return
		// null

		// Listen to changes in the AttributesJComboBox and propagate them
		// to the underlying rulesList
		jComboBoxValueAttribute.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {

				if (e.getStateChange() != ItemEvent.SELECTED)
					return;

				/**
				 * Here we not only change the Value FIeld Name, but also remove
				 * all entries..
				 */
				rulesList.pushQuite();
				rulesList.removeValues(rulesList.getValues());
				rulesList.setWithDefaultSymbol(true);
				rulesList.setPropertyFieldName((String) e.getItem(), true);
				rulesList.popQuite();
			}

		});

		SwingUtil.addMouseWheelForCombobox(jComboBoxValueAttribute);

		return jComboBoxValueAttribute;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelColorAndTemplate() {
		final JPanel jPanelColorAndTemplate = new JPanel(new MigLayout(
				"wrap 2, inset 1, gap 1", "[grow][]"));
		jPanelColorAndTemplate
				.setBorder(BorderFactory.createTitledBorder(AtlasStyler
						.R("UniqueValues.PanelBorderTitle.Colors_and_Template")));

		jLabelTemplate = new JLabel(
				AtlasStyler.R("UniqueValues.ChooseTemplate.Label"));

		jPanelColorAndTemplate.add(getJComboBoxPalette());
		jPanelColorAndTemplate.add(getJButtonApplyPalette(), "sgx");
		jPanelColorAndTemplate.add(jLabelTemplate, "split 2");
		jPanelColorAndTemplate.add(getJButtonTemplate());
		jPanelColorAndTemplate.add(getJButtonApplyTemplate(), "sgx");
		return jPanelColorAndTemplate;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxPalette() {
		if (jComboBoxPalette == null) {

			BrewerPalette[] palettes = new BrewerPalette[] {};
			// This code only the paletes usefull for unique values.
			final PaletteType paletteTypeUnique = new PaletteType(false, true);
			final PaletteType paletteTypeRanged = new PaletteType(true, false);
			try {
				ColorBrewer brewer1, brewer2;
				brewer1 = ColorBrewer.instance(paletteTypeUnique);
				brewer2 = ColorBrewer.instance(paletteTypeRanged);
				BrewerPalette[] palettes1 = brewer1
						.getPalettes(paletteTypeUnique);
				BrewerPalette[] palettes2 = brewer2
						.getPalettes(paletteTypeRanged);
				palettes = LangUtil.extendArray(palettes1, palettes2);
			} catch (IOException e) {
				LOGGER.error("Error loading palettes", e);
				palettes = ColorBrewer.instance().getPalettes();
			}
			DefaultComboBoxModel aModel = new DefaultComboBoxModel(palettes);
			//
			// DefaultComboBoxModel aModel = new
			// DefaultComboBoxModel(StylingUtil.addReversePalettes( brewer
			// .getPalettes(paletteTypeUnique)));

			aModel.setSelectedItem(rulesList.getBrewerPalette());

			jComboBoxPalette = new JComboBox(aModel);

			PaletteCellRenderer aRenderer = new PaletteCellRenderer();
			jComboBoxPalette.setRenderer(aRenderer);

			jComboBoxPalette.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						rulesList.setBrewerPalette((BrewerPalette) (e.getItem()));
						jComboBoxPalette.setToolTipText(rulesList
								.getBrewerPalette().getDescription());
						// getTableModel().fireTableDataChanged();
					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxPalette, false);
		}
		return jComboBoxPalette;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonApplyPalette() {
		if (jButtonApplyPalette == null) {
			jButtonApplyPalette = new ThinButton(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {

					rulesList.applyPalette(UniqueValuesGUI.this);
				}
			});

			jButtonApplyPalette.setText(AtlasStyler
					.R("UniqueValues.applyPaletteButton.title"));
			jButtonApplyPalette.setToolTipText(AtlasStyler
					.R("UniqueValues.applyPaletteButton.toolTip"));

		}
		return jButtonApplyPalette;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonTemplate() {
		if (jButtonTemplate == null) {
			jButtonTemplate = new JButton();
			jButtonTemplate.setToolTipText(AtlasStyler
					.R("UniqueValuesGUI.selectTemplateButton.toolTip"));
			jButtonTemplate.setBorder(BorderFactory.createEtchedBorder());

			ImageIcon imageIcon = new ImageIcon(rulesList.getTemplate()
					.getImage(ICON_SIZE));
			jButtonTemplate.setAction(new AbstractAction("", imageIcon) {

				public void actionPerformed(ActionEvent e) {

					rulesList.getTemplate().getListeners().clear();
					final SingleRuleList<? extends Symbolizer> template = rulesList
							.getTemplate();
					final SingleRuleList<? extends Symbolizer> backup = template
							.copy();

					SymbolSelectorGUI gui = new SymbolSelectorGUI(
							UniqueValuesGUI.this,
							AtlasStyler
									.R("UniqueValuesGUI.selectTemplateDialog.dialogTitle"),
							template);

					/***********************************************************
					 * Listen to a CANCEL to use the backup
					 */
					gui.addPropertyChangeListener(new PropertyChangeListener() {

						public void propertyChange(PropertyChangeEvent evt) {

							if (evt.getPropertyName().equals(
									SymbolSelectorGUI.PROPERTY_CANCEL_CHANGES)) {

								backup.copyTo(template);
							}

							if (evt.getPropertyName().equals(
									SymbolSelectorGUI.PROPERTY_CLOSED)) {
							}

						}

					});

					template.addListener(listenToRuleListeChangesAndUpdateTemplate);

					gui.setModal(true);
					gui.setVisible(true);
				}

			});

		}
		return jButtonTemplate;
	}

	RuleChangeListener listenToRuleListeChangesAndUpdateTemplate = new RuleChangeListener() {

		public void changed(RuleChangedEvent e) {

			// LOGGER.debug("reason = " + e.toString());

			rulesList.setTemplate((SingleRuleList) e.getSourceRL());

			jButtonTemplate.setIcon(new ImageIcon(rulesList.getTemplate()
					.getImage(ICON_SIZE)));
		}

	};

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonApplyTemplate() {
		if (jButtonApplyTemplate == null) {
			jButtonApplyTemplate = new ThinButton(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					rulesList.applyTemplate();
				}
			});
			jButtonApplyTemplate.setText(AtlasStyler
					.R("UniqueValues.applyTemplateButton.title"));
			jButtonApplyTemplate.setToolTipText(AtlasStyler
					.R("UniqueValues.applyTemplateButton.tooltip"));
		}
		return jButtonApplyTemplate;
	}

	/**
	 * A button to add all unique values of the selected column
	 */
	private JButton getJButtonAddAllValues() {
		if (jButtonAddAllValues == null) {
			jButtonAddAllValues = new ThinButton(new AbstractAction(
					AtlasStyler.R("UniqueValues.Button.AddAllValues")) {

				public void actionPerformed(ActionEvent e) {
					if (rulesList.getPropertyFieldName() == null) {
						JOptionPane
								.showMessageDialog(
										UniqueValuesGUI.this,
										AtlasStyler
												.R("UniqueValuesRuleList.AddAllValues.Error.NoAttribSelected"));
						return;
					}

					String title = AtlasStyler
							.R("UniqueValuesRuleList.AddAllValues.SearchingMsg");

					final AtlasSwingWorker<Integer> findUniques = new AtlasSwingWorker<Integer>(
							UniqueValuesGUI.this, title) {

						@Override
						protected Integer doInBackground() throws Exception {
							return rulesList.addAllValues(this);
						}
					};
					try {
						Integer added = findUniques.executeModal();
						JOptionPane
								.showMessageDialog(
										UniqueValuesGUI.this,
										AtlasStyler
												.R("UniqueValuesRuleList.AddAllValues.DoneMsg",
														added));
					} catch (CancellationException ce) {
//						findUniques.cancel(true);
						return;
					} catch (Exception ee) {
						ExceptionDialog.show(ee);
					}
				}

			});
		}
		return jButtonAddAllValues;
	}

	/**
	 * A button to add one unique value of the selected column
	 */
	private JButton getJButtonAddValues() {
		if (jButtonAddValues == null) {
			jButtonAddValues = new ThinButton(new AbstractAction(
					AtlasStyler.R("UniqueValues.Button.AddValues")) {

				public void actionPerformed(ActionEvent e) {
					UniqueValuesAddGUI valuesGUI = new UniqueValuesAddGUI(
							SwingUtil.getParentWindow(UniqueValuesGUI.this),
							rulesList);
				}

			});

		}

		return jButtonAddValues;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonRemove() {
		if (jButtonRemove == null) {
			jButtonRemove = new ThinButton(new AbstractAction(
					AtlasStyler.R("UniqueValues.Button.RemoveValue")) {

				public void actionPerformed(ActionEvent e) {
					int[] selectedRows = getJTable().getSelectedRows();
					List<Object> removeValues = new ArrayList<Object>();
					for (int rowIdx : selectedRows) {

						removeValues.add(rulesList.getValues().get(rowIdx));

					}
					rulesList.removeValues(removeValues);

					// De-select anything afterwards
					getJTable().getSelectionModel().clearSelection();
				}

			});

			getJTable().getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						public void valueChanged(ListSelectionEvent e) {
							if (getJTable().getSelectedRows().length == 0)
								jButtonRemove.setEnabled(false);
							else {
								jButtonRemove.setEnabled(true);
							}
						}

					});

			/** Initializing with disabled button * */
			jButtonRemove.setEnabled(false);

		}
		return jButtonRemove;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonRemoveAll() {
		if (jButtonRemoveAll == null) {
			jButtonRemoveAll = new ThinButton(new AbstractAction(
					AtlasStyler.R("UniqueValues.Button.RemoveAllValues")) {

				public void actionPerformed(ActionEvent e) {
					// Ahhh.. this changes the original List! :-/
					// List<String> vals = rulesList.getValues();
					// vals.remove(UniqueValuesRuleList.ALLOTHERS_IDENTIFICATION_VALUE);
					// rulesList.removeValues(vals);

					List<Object> vals = rulesList.getValues();
					List<Object> vals2 = new ArrayList<Object>();
					for (Object s : vals) {
						if (!s.equals(UniqueValuesRuleList.ALLOTHERS_IDENTIFICATION_VALUE))
							vals2.add(s);
					}
					rulesList.removeValues(vals2);

					LOGGER.debug("After remove all we have "
							+ rulesList.getNumClasses() + " classes left");
				}

			});

			if (rulesList.getValues().size() < 1) {
				jButtonRemoveAll.setEnabled(false);
			} else {
				jButtonRemoveAll.setEnabled(true);
			}

			rulesList
					.addListener(listenWhenDefaultSymbolisInTheRLandEnableButton);
		}
		return jButtonRemoveAll;
	}

	/** Switch the button on/off when no rules exist no more * */
	RuleChangeListener listenWhenDefaultSymbolisInTheRLandEnableButton = new RuleChangeListener() {

		public void changed(RuleChangedEvent e) {
			if (rulesList.getValues().size() < (rulesList.isWithDefaultSymbol() ? 2
					: 1)) {
				jButtonRemoveAll.setEnabled(false);
			} else {
				jButtonRemoveAll.setEnabled(true);
			}

		}

	};

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonUp() {
		if (jButtonUp == null) {

			jButtonUp = new JButton();

			jButtonUp.setAction(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					int row = getJTable().getSelectedRow();

					if (row == 0)
						return;

					rulesList.pushQuite();

					try {

						// values has one less, that all the minus
						Object val = rulesList.getValues().remove(row - 1);
						rulesList.getValues().add(row, val);

						String label = rulesList.getLabels().remove(row - 1);
						rulesList.getLabels().add(row, label);

						SingleRuleList<? extends Symbolizer> valS = rulesList
								.getSymbols().remove(row - 1);
						rulesList.getSymbols().add(row, valS);

					} finally {
						rulesList.popQuite(new RuleChangedEvent(
								"Order changed", rulesList));
					}

					// Update the selection
					getJTable().getSelectionModel().clearSelection();
					getJTable().getSelectionModel().addSelectionInterval(
							row - 1, row - 1);
				}

			});

			jButtonUp.setMaximumSize(SMALLBUTTONSIZE);
			jButtonUp.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			jButtonUp.setEnabled(false);
			jButtonUp.setIcon(Icons.getUpArrowIcon());

			/** Activate and disable on selection-changes * */
			getJTable().getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						public void valueChanged(ListSelectionEvent e) {
							if (getJTable().getSelectedRowCount() != 1)
								jButtonUp.setEnabled(false);
							else {
								if (getJTable().getSelectedRow() > 0) // not the
									// first pos
									jButtonUp.setEnabled(true);
								else
									jButtonUp.setEnabled(false);
							}
						}

					});

		}
		return jButtonUp;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonDown() {
		if (jButtonDown == null) {

			jButtonDown = new JButton(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {

					rulesList.pushQuite();

					int row = getJTable().getSelectedRow();

					if (row == getJTable().getModel().getRowCount() - 1)
						return;

					// values has one less, that all the minus
					Object val = rulesList.getValues().remove(row + 1);
					rulesList.getValues().add(row, val);

					String label = rulesList.getLabels().remove(row + 1);
					rulesList.getLabels().add(row, label);

					SingleRuleList<? extends Symbolizer> valS = rulesList
							.getSymbols().remove(row + 1);
					rulesList.getSymbols().add(row, valS);

					rulesList.popQuite(new RuleChangedEvent("Order changed",
							rulesList));

					getJTable().getSelectionModel().clearSelection();
					getJTable().getSelectionModel().addSelectionInterval(
							row + 1, row + 1);
				}

			});

			jButtonDown.setMaximumSize(SMALLBUTTONSIZE);
			jButtonDown.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
			jButtonDown.setEnabled(false);
			jButtonDown.setIcon(Icons.getDownArrowIcon());

			/** Activate and disable on selection-changes * */
			getJTable().getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {

						public void valueChanged(ListSelectionEvent e) {
							if (getJTable().getSelectedRowCount() != 1)// onyl
								// single
								// selections
								jButtonDown.setEnabled(false);
							else {
								if ((getJTable().getSelectedRow() != getJTable()
										.getModel().getRowCount() - 1) // not
								// on
								// the
								// last
								// pos
								)

									jButtonDown.setEnabled(true);
								else
									jButtonDown.setEnabled(false);
							}
						}

					});

		}
		return jButtonDown;
	}

	/**
	 * This method initializes jTable
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable(getTableModel());

			/** Render nicely* */
			jTable.setDefaultRenderer(SingleRuleList.class,
					new UniqueValuesSingleRuleListCellRenderer());

			rulesList.addListener(updateClassificationTableWhenRuleListChanges);

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

							Object val = rulesList.getValues().get(row);

							if (AtlasStyler.getLanguageMode() == AtlasStyler.LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {
								LOGGER.debug(AtlasStyler.getLanguages());

								final Translation translation = new Translation();

								translation.fromOneLine(rulesList.getLabels()
										.get(row));

								if (ask == null) {
									TranslationEditJPanel transLabel;
									if (val.equals(UniqueValuesRuleList.ALLOTHERS_IDENTIFICATION_VALUE)) {
										/** We are in the default rule* */
										transLabel = new TranslationEditJPanel(
												AtlasStyler
														.R("UniqueValuesGUI.LabelForClass",
																AtlasStyler
																		.R("UniqueValuesGUI.AllOthersSymbol.label")),
												translation, AtlasStyler
														.getLanguages());
									} else {

										// The index depends on the whether the
										// "all others rule" is eneabled and
										// where it is positioned in the list!
										int index = row;
										if (UniqueValuesGUI.this.rulesList
												.isWithDefaultSymbol()) {
											int allOthersRuleIdx = UniqueValuesGUI.this.rulesList
													.getAllOthersRuleIdx();

											if (allOthersRuleIdx < row)
												index = row - 1;
										}

										transLabel = new TranslationEditJPanel(
												AtlasStyler
														.R("UniqueValuesGUI.LabelForClass",
																rulesList
																		.getValues()
																		.get(index)),
												translation, AtlasStyler
														.getLanguages());
									}

									ask = new TranslationAskJDialog(
											UniqueValuesGUI.this, transLabel);
									ask.addPropertyChangeListener(new PropertyChangeListener() {

										public void propertyChange(
												PropertyChangeEvent evt) {
											if (evt.getPropertyName()
													.equals(TranslationAskJDialog.PROPERTY_CANCEL_AND_CLOSE)) {
												ask = null;
											}
											if (evt.getPropertyName()
													.equals(TranslationAskJDialog.PROPERTY_APPLY_AND_CLOSE)) {
												LOGGER.debug("Saving new ranslation in rulelist and fire event ");

												rulesList
														.getLabels()
														.set(row,
																translation
																		.toOneLine());

											}
											ask = null;
											//
											rulesList
													.fireEvents(new RuleChangedEvent(
															"Legend Label changed",
															rulesList));
										}

									});

								}
								ask.setVisible(true);

							} else {
								/***********************************************
								 * AtlasStyler.LANGUAGE_MODE.OGC
								 */
								String newTitle = ASUtil.askForString(
										UniqueValuesGUI.this, rulesList
												.getLabels().get(row), null);
								if (newTitle != null) {
									rulesList.getLabels().set(row, newTitle);
									rulesList.fireEvents(new RuleChangedEvent(
											"Legend Label changed", rulesList));
								}
							}

						}

						else
						/*******************************************************
						 * Changing the Symbol with a MouseClick
						 */
						if (col == 0) {
							rulesList.getSymbols().get(row).getListeners()
									.clear();
							final SingleRuleList<? extends Symbolizer> editSymbol = rulesList
									.getSymbols().get(row);
							final SingleRuleList<? extends Symbolizer> backup = editSymbol
									.copy();

							SymbolSelectorGUI gui = new SymbolSelectorGUI(
									SwingUtil
											.getParentWindow(UniqueValuesGUI.this),
									"Change symbol for "
											+ rulesList.getLabels().get(row),
									editSymbol);

							/***************************************************
							 * Listen to a CANCEL to use the backup
							 */
							gui.addPropertyChangeListener(new PropertyChangeListener() {

								public void propertyChange(
										PropertyChangeEvent evt) {

									if (evt.getPropertyName()
											.equals(SymbolSelectorGUI.PROPERTY_CANCEL_CHANGES)) {

										backup.copyTo(editSymbol);
									}

									if (evt.getPropertyName().equals(
											SymbolSelectorGUI.PROPERTY_CLOSED)) {
									}

								}

							});

							// we have a referenct to it!
							listenToEditedSymbolAndPassOnTheEvent = new RuleChangeListener() {

								public void changed(RuleChangedEvent e) {

									/** Exchanging the Symbol * */
									rulesList.getSymbols().set(row, editSymbol);

									// Fire an event?! TODO
									rulesList.fireEvents(new RuleChangedEvent(
											"Editing a Symbol", rulesList));

								}

							};
							editSymbol
									.addListener(listenToEditedSymbolAndPassOnTheEvent);

							gui.setModal(true);
							gui.setVisible(true);
						}
					}

				}

			});

			jTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

			jTable.setRowHeight(ICON_SIZE.height + 2);
			jTable.getColumnModel().getColumn(0).setMaxWidth(53);
			// jTable.getColumnModel().getColumn(3).setMaxWidth(63); //TODO die
			// Count column...
		}
		return jTable;
	}

	RuleChangeListener listenToEditedSymbolAndPassOnTheEvent;

	// We need a place to store the color the JLabel should be returned
	// to after its foreground and background colors have been set
	// to the selection background color.
	// // These ivars will be made protected when their names are finalized.
	// private Color unselectedForeground;
	//
	// private Color unselectedBackground;

	class UniqueValuesSingleRuleListCellRenderer extends
			DefaultTableCellRenderer {

		protected Logger LOGGER = ASUtil.createLogger(this);

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (value == null)
				return new JLabel("null");

			SingleRuleList<? extends Symbolizer> singleRL = (SingleRuleList<?>) value;

			BufferedImage image = singleRL.getImage(ICON_SIZE);

			// JLabel tableCellRendererComponent = (JLabel) super
			// .getTableCellRendererComponent(table, value, isSelected,
			// hasFocus, row, column);

			JLabel tableCellRendererComponent = new JLabel();
			//
			// Color fg = null;
			// Color bg = null;
			//
			// JTable.DropLocation dropLocation = table.getDropLocation();
			// if (dropLocation != null && !dropLocation.isInsertRow()
			// && !dropLocation.isInsertColumn()
			// && dropLocation.getRow() == row
			// && dropLocation.getColumn() == column) {
			//
			// fg = UIManager.getColor("Table.dropCellForeground");
			// bg = UIManager.getColor("Table.dropCellBackground");
			//
			// isSelected = true;
			// }
			//
			// if (isSelected) {
			// tableCellRendererComponent.setForeground(fg == null ? table
			// .getSelectionForeground() : fg);
			// tableCellRendererComponent.setBackground(bg == null ? table
			// .getSelectionBackground() : bg);
			// } else {
			// tableCellRendererComponent
			// .setForeground(unselectedForeground != null ?
			// unselectedForeground
			// : table.getForeground());
			// tableCellRendererComponent
			// .setBackground(unselectedBackground != null ?
			// unselectedBackground
			// : table.getBackground());
			// }

			tableCellRendererComponent.setText("");
			tableCellRendererComponent
					.setHorizontalAlignment(SwingConstants.CENTER);
			tableCellRendererComponent.setIcon(new ImageIcon(image));

			return tableCellRendererComponent;
		}

	}

	private DefaultTableModel getTableModel() {

		if (tableModel == null) {

			tableModel = new DefaultTableModel() {

				@Override
				public Class<?> getColumnClass(int columnIndex) {
					if (columnIndex == 0) // Symbol
						return SingleRuleList.class;

					if (columnIndex == 1) // Value
						return String.class;

					if (columnIndex == 2) // Label
						return String.class;

					if (columnIndex == 3) // Count
						return Integer.class;

					return null;
				}

				@Override
				public int getColumnCount() {
					return 3;
					// war 4 //TODO count irgendwann mal implementieren
				}

				@Override
				public String getColumnName(int columnIndex) {
					if (columnIndex == 0)
						return AtlasStyler
								.R("UniqueValuesGUI.classesTable.columnHeadersTitle.symbol");
					if (columnIndex == 1)
						return AtlasStyler
								.R("UniqueValuesGUI.classesTable.columnHeadersTitle.value");
					if (columnIndex == 2)
						return AtlasStyler
								.R("UniqueValuesGUI.classesTable.columnHeadersTitle.label");
					if (columnIndex == 3)
						return AtlasStyler
								.R("UniqueValuesGUI.classesTable.columnHeadersTitle.count");
					return super.getColumnName(columnIndex);
				}

				@Override
				public int getRowCount() {
					return rulesList.getNumClasses();
				}

				@Override
				public Object getValueAt(int rowIndex, int columnIndex) {

					if (columnIndex == 0) {
						return rulesList.getSymbols().get(rowIndex);
					} else if (columnIndex == 1) {
						return rulesList.getValues().get(rowIndex);
					} else if (columnIndex == 2) {
						return rulesList.getLabels().get(rowIndex);
					}
					// else if (columnIndex == 3) {
					// return rulesList.getCount(rowIndex); // TODO Count
					// implementieren
					// }

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
	 * Creates a {@link JCheckBox} to add/remove the default/all-others rule.
	 */
	public JCheckBox getWithDefaultCheckbox() {
		final JCheckBox jCheckBoxWithDefault = new JCheckBox(
				AtlasStyler.R("UniqueValuesGUI.AllOthersSymbol.label"));

		jCheckBoxWithDefault.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				rulesList.pushQuite();
				rulesList.setWithDefaultSymbol(jCheckBoxWithDefault
						.isSelected());

				/**
				 * Select the row automatically
				 */
				if (jCheckBoxWithDefault.isSelected()) {
					getJTable().getSelectionModel().clearSelection();
					final int indexOf = rulesList
							.getValues()
							.indexOf(
									UniqueValuesRuleList.ALLOTHERS_IDENTIFICATION_VALUE);
					getJTable().getSelectionModel().addSelectionInterval(
							indexOf, indexOf);
				}

				rulesList.popQuite();
			}

		});

		listenToRLToDefineDefaultSymbol = new RuleChangeListener() {

			public void changed(RuleChangedEvent e) {
				jCheckBoxWithDefault.setSelected(rulesList
						.isWithDefaultSymbol());
			}

		};
		rulesList.addListener(listenToRLToDefineDefaultSymbol);

		jCheckBoxWithDefault.setSelected(rulesList.isWithDefaultSymbol());
		return jCheckBoxWithDefault;
	}

	RuleChangeListener listenToRLToDefineDefaultSymbol;

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		jLabelHeading = new JLabel(AtlasStyler.R("UniqueValues.Heading"));
		jLabelHeading.setFont(jLabelHeading.getFont().deriveFont(
				AtlasStylerTabbedPane.HEADING_FONT_SIZE));
		this.setLayout(new MigLayout("inset 1, gap 1, wrap 1, fillx"));

		this.add(jLabelHeading, "center");
		this.add(getAttributeJPanel(), "split 2, grow x");
		this.add(getJPanelColorAndTemplate(), "grow x");

		this.add(new JScrollPane(getJTable()), "grow x, height 50:150:600");

		JPanel jPanelButtons = new JPanel(new MigLayout(
				"ins n 0 n 0, gap 1, fillx"));
		{
			jPanelButtons.add(getJButtonAddAllValues());
			jPanelButtons.add(getJButtonAddValues(), "gapx rel unrel");
			jPanelButtons.add(getJButtonRemove());
			jPanelButtons.add(getJButtonRemoveAll(), "gapx rel unrel");
			jPanelButtons.add(getJButtonUp());
			jPanelButtons.add(getJButtonDown(), "gapx rel");
		}
		this.add(jPanelButtons, "");

	}

	public void dispose() {
		for (Window w : openWindows) {
			if (w instanceof ClosableSubwindows) {
				((ClosableSubwindows) w).dispose();
			}
			w.dispose();
		}
	}
}
