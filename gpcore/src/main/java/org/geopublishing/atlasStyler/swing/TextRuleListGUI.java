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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AbstractRuleList;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.SingleRuleList;
import org.geopublishing.atlasStyler.StyleChangeListener;
import org.geopublishing.atlasStyler.StyleChangedEvent;
import org.geopublishing.atlasStyler.TextRuleList;
import org.geotools.data.DefaultQuery;
import org.geotools.feature.FeatureCollection;
import org.geotools.styling.TextSymbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.swing.ThinButton;

/**
 * This GUI defines all the buttons to define the {@link TextSymbolizer}
 * 
 * @author SK
 */
public class TextRuleListGUI extends JPanel {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private JCheckBox jCheckBoxEnabled = null;

	private JPanel jPanelClass = null;

	private JComboBox jComboBoxClass = null;

	private JCheckBox jCheckBoxClassEnabled = null;

	private JButton jButtonClassAdd = null;

	private JButton jButtonClassDelete = null;

	private JButton jButtonClassRename = null;

	private JButton jButtonClassFromSymbols = null;

	private AttributesJComboBox jComboBoxLabelField = null;
	
	private AttributesJComboBox jComboBoxLabelField2 = null;
	
	private AttributesJComboBox jComboBoxPriorityField;

	private TextSymbolizerEditGUI jPanelEditTextSymbolizer = null;

	private final TextRuleList rulesList;

	private final AtlasStyler atlasStyler;

	private JPanel jPanelLabelDefinition;

	/**
	 * This is the default constructor
	 */
	public TextRuleListGUI(final TextRuleList rulesList,
			final AtlasStyler atlasStyler) {
		this.rulesList = rulesList;
		this.atlasStyler = atlasStyler;
		rulesList.addDefaultClass();

		initialize();

		updateGUI();

		atlasStyler.addListener(new StyleChangeListener() {

			public void changed(StyleChangedEvent e) {
				jButtonClassFromSymbols.setEnabled(!(atlasStyler
						.getLastChangedRuleList() instanceof SingleRuleList));
			}

		});
		jButtonClassFromSymbols.setEnabled(!(atlasStyler
				.getLastChangedRuleList() instanceof SingleRuleList));

	}

	protected void updateGUI() {

		rulesList.pushQuite();
		final TextSymbolizer symbolizer = rulesList.getSymbolizer();

		/***********************************************************************
		 * LabelFieldName
		 */
		PropertyName pn1 = StylingUtil.getFirstPropertyName(rulesList
				.getStyledFeatures().getSchema(), rulesList.getSymbolizer());
		jComboBoxLabelField.setSelectedItem(pn1.toString());

		PropertyName pn2 = StylingUtil.getSecondPropertyName(rulesList
				.getStyledFeatures().getSchema(), rulesList.getSymbolizer());
		if (pn2 != null) {
			jComboBoxLabelField2.setSelectedItem(pn2.toString());
		} else
			jComboBoxLabelField2.setSelectedItem("-");

		/***********************************************************************
		 * PriorityFieldName
		 */
		if (symbolizer.getPriority() != null) {
			jComboBoxPriorityField.setSelectedItem(symbolizer.getPriority()
					.toString());
		} else {
			jComboBoxPriorityField.setSelectedItem("");
		}

		/***********************************************************************
		 * FontFamiliy
		 */
		String fontFamiliyString = symbolizer.getFont().getFamily().toString();
		jPanelEditTextSymbolizer.getJComboBoxFont().setSelectedItem(
				fontFamiliyString);

		/***********************************************************************
		 * Font Size
		 */
		String fontFamiliySize = symbolizer.getFont().getSize().toString();
		ASUtil.selectOrInsert(jPanelEditTextSymbolizer.getJComboBoxSize(),
				Double.parseDouble(fontFamiliySize));

		/***********************************************************************
		 * Font Style
		 */
		String fontStyle = symbolizer.getFont().getStyle().toString();
		ASUtil.selectOrInsert(jPanelEditTextSymbolizer.getJComboBoxStyle(),
				fontStyle);

		/***********************************************************************
		 * Font Weight
		 */
		String fontWeight = symbolizer.getFont().getWeight().toString();
		ASUtil.selectOrInsert(jPanelEditTextSymbolizer.getJComboBoxWeight(),
				fontWeight.toString());

		jPanelEditTextSymbolizer.getJButtonColor().setColor(
				symbolizer.getFill().getColor());

		/***********************************************************************
		 * Halo FILL
		 */
		if (symbolizer.getHalo() == null) {
			jPanelEditTextSymbolizer.getJComboBoxHaloRadius().setSelectedItem(
					0f);
			jPanelEditTextSymbolizer.getJComboBoxHaloOpacity()
					.setEnabled(false);
			jPanelEditTextSymbolizer.getJButtonColorHalo().setEnabled(false);
		} else {
			ASUtil.selectOrInsert(jPanelEditTextSymbolizer
					.getJComboBoxHaloRadius(), Float.valueOf(symbolizer
					.getHalo().getRadius().toString()));

			if (symbolizer.getHalo().getFill() == null) {
				symbolizer.getHalo().setFill(ASUtil.SB.createFill(Color.white));
			}

			jPanelEditTextSymbolizer.getJButtonColorHalo().setColor(
					symbolizer.getHalo().getFill().getColor());

			float opacity = Float.valueOf(symbolizer.getHalo().getFill()
					.getOpacity().toString());
			ASUtil.selectOrInsert(jPanelEditTextSymbolizer
					.getJComboBoxHaloOpacity(), opacity);
		}

		/***********************************************************************
		 * Class management buttons
		 */
		jButtonClassDelete.setEnabled(getJComboBoxClass().getSelectedIndex() != 0);
		jButtonClassRename.setEnabled(getJComboBoxClass().getSelectedIndex() != 0);
		

		// Change the state of the class enabled CB
		jCheckBoxClassEnabled.setSelected(rulesList
				.getClassEnabled(rulesList.getSelIdx()));
		reactToClassEnabledChange();
		
		rulesList.popQuite();
	}

	/**
	 * This method initializes this
	 * 
	 * @param features
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new MigLayout("wrap 1, gap 1, inset 1, top"));
		this.add(getJCheckBoxEnabled(), "top");
		this.add(getJPanelClass());
	}

	/**
	 * This method initializes jCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxEnabled() {
		if (jCheckBoxEnabled == null) {
			jCheckBoxEnabled = new JCheckBox();
			jCheckBoxEnabled.setAction(new AbstractAction(AtlasStyler
					.R("TextRulesList.Labels.Checkbox")) {

				public void actionPerformed(ActionEvent e) {
					rulesList.setEnabled(jCheckBoxEnabled.isSelected());
					TextRuleListGUI.this.setEnabled(jCheckBoxEnabled.isSelected());
					jCheckBoxEnabled.setEnabled(true); // THIS CB has to stay enabled
					reactToClassEnabledChange();
				}

			});
			jCheckBoxEnabled.setSelected(rulesList.isEnabled());
			TextRuleListGUI.this.setEnabled(jCheckBoxEnabled.isSelected());
			jCheckBoxEnabled.setEnabled(true); // THIS CB has to stay enabled
			reactToClassEnabledChange();
		}
		return jCheckBoxEnabled;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @param features
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelClass() {
		if (jPanelClass == null) {
		
			jPanelClass = new JPanel(new MigLayout("nogrid, gap 1, inset 1"));
			
			jPanelClass.add(new JLabel(AtlasStyler.R("TextRulesList.Labelclass")+":"));
			jPanelClass.add(getJComboBoxClass());
			jPanelClass.add(getJCheckBoxClassEnabled(),"wrap");
			
//			jPanelClass.add(getJButtonClassAdd());
			jPanelClass
			.add(getJButtonClassDelete());
			jPanelClass.add(getJButtonClassRename());
			jPanelClass.add(getJButtonClassFromSymbols(),"wrap");
			
			jPanelClass.add(getJPanelLabelDefinition(), "wrap");
			jPanelClass
					.add(getJPanelEditTextSymbolizer());
		}
		return jPanelClass;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxClass() {
		if (jComboBoxClass == null) {
			jComboBoxClass = new JComboBox();
			jComboBoxClass.setModel(new DefaultComboBoxModel(rulesList
					.getRuleNames().toArray()));

			jComboBoxClass.setSelectedIndex(0);

			SwingUtil.addMouseWheelForCombobox(jComboBoxClass, false);
			
			jComboBoxClass.setRenderer( new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus) {
					
					JComponent superT = (JComponent)super.getListCellRendererComponent(list, value, index, isSelected,
							cellHasFocus);

					if (index >= 0 && index < rulesList.getClassesFilters().size())
						superT.setToolTipText("<html>"+rulesList.getClassesFilters().get(index).toString()+"</html>");
					else 
						superT.setToolTipText(null);
					
					return superT;
				}
			});
			
			jComboBoxClass.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						rulesList.setSelIdx(getJComboBoxClass().getSelectedIndex());

						// Update the tool-tip
						getJComboBoxClass().setToolTipText("<html>"+rulesList.getClassesFilters().get(getJComboBoxClass().getSelectedIndex()).toString()+"</html>");
						
						updateGUI();
					}
				}
			});
			
			// Update the tool-tip
			jComboBoxClass.setToolTipText("<html>"+rulesList.getClassesFilters().get(getJComboBoxClass().getSelectedIndex()).toString()+"</html>");
		}
		return jComboBoxClass;
	}

	/**
	 * This method initializes jCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxClassEnabled() {
		if (jCheckBoxClassEnabled == null) {
			jCheckBoxClassEnabled = new JCheckBox(AtlasStyler
					.R("TextRulesList.Labelclass.Checkbox"));
			
			jCheckBoxClassEnabled.setSelected(rulesList
					.getClassEnabled(rulesList.getSelIdx()));
			reactToClassEnabledChange();
			
			
			jCheckBoxClassEnabled.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					rulesList.setClassEnabled(rulesList.getSelIdx(),
							jCheckBoxClassEnabled.isSelected());
					reactToClassEnabledChange();
				}
			});

		}
		return jCheckBoxClassEnabled;
	}

	private void reactToClassEnabledChange() {
			getJPanelEditTextSymbolizer().setEnabled(
					getJCheckBoxClassEnabled().isSelected() && getJCheckBoxEnabled().isSelected());
			getJPanelLabelDefinition().setEnabled(
					getJCheckBoxClassEnabled().isSelected() && getJCheckBoxEnabled().isSelected());
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonClassAdd() {
		if (jButtonClassAdd == null) {
			jButtonClassAdd = new ThinButton(AtlasStyler
					.R("TextRulesList.Labelclass.Action.Add"));
			// jButtonClassAdd.setFont(jButtonClassAdd.getFont().deriveFont(
			// AtlasStylerTabbedPane.BUTTON_FONT_STYLE,
			// AtlasStylerTabbedPane.BUTTON_FONT_SIZE));
			jButtonClassAdd.setToolTipText(AtlasStyler
					.R("TextRulesList.Labelclass.Action.Add.TT"));
			jButtonClassAdd.setEnabled(false);
		}
		return jButtonClassAdd;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonClassDelete() {
		if (jButtonClassDelete == null) {
			jButtonClassDelete = new ThinButton(AtlasStyler
					.R("TextRulesList.Labelclass.Action.Delete"));

			jButtonClassDelete.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					if (rulesList.getSelIdx() == 0) {
						LOGGER
								.warn("Will not delete the default rule.. Why is the button enabled anyway?");
						jButtonClassDelete.setEnabled(false);
						return;
					}
					int idx = rulesList.getSelIdx();
					rulesList.getSymbolizers().remove(idx);
					rulesList.getClassesFilters().remove(idx);
					rulesList.getRuleNames().remove(idx);
					getJComboBoxClass().setModel(new DefaultComboBoxModel(rulesList
							.getRuleNames().toArray()));
					rulesList.removeClassMinScale(idx);
					rulesList.removeClassMaxScale(idx);
					rulesList.removeClassEnabledScale(idx);
					rulesList.setSelIdx(idx-1);
					updateGUI();
				}

			});

			jButtonClassDelete.setToolTipText(AtlasStyler
					.R("TextRulesList.Labelclass.Action.Delete.TT"));
		}
		return jButtonClassDelete;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonClassRename() {
		if (jButtonClassRename == null) {
			jButtonClassRename = new ThinButton(AtlasStyler
					.R("TextRulesList.Labelclass.Action.Rename"));
			jButtonClassRename.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					String ruleName = rulesList.getRuleName();
					String result = ASUtil.askForString(TextRuleListGUI.this,
							ruleName, null);
					if (result != null) {
						// We do not check if the name exists twice, because it
						// wouldn't really matter
						rulesList.getRuleNames().set(
								rulesList.getSelIdx(), result);
						getJComboBoxClass().setModel(new DefaultComboBoxModel(
								rulesList.getRuleNames().toArray()));
						getJComboBoxClass().setSelectedIndex(rulesList
								.getSelIdx());
					}
				}
			});
		}
		return jButtonClassRename;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonClassFromSymbols() {
		if (jButtonClassFromSymbols == null) {
			jButtonClassFromSymbols = new ThinButton(
					AtlasStyler
							.R("TextRulesList.Labelclass.Action.LoadClassesFromSymbols"));

			jButtonClassFromSymbols.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					AbstractRuleList symbRL = atlasStyler
							.getLastChangedRuleList();

					rulesList.importClassesFromStyle(symbRL,
							TextRuleListGUI.this);

					getJComboBoxClass().setModel(new DefaultComboBoxModel(rulesList
							.getRuleNames().toArray()));
					getJComboBoxClass().setSelectedIndex(0);
				}
			});

			jButtonClassFromSymbols
					.setToolTipText(AtlasStyler
							.R("TextRulesList.Labelclass.Action.LoadClassesFromSymbols.TT"));
		}
		return jButtonClassFromSymbols;
	}

	/**
	 * A {@link JPanel} allowing to define the label settings for a text symbolizer class
	 */
	private JPanel getJPanelLabelDefinition() {

		if (jPanelLabelDefinition == null) {

			jPanelLabelDefinition = new JPanel(new MigLayout("wrap 2, gap 1, inset 1"),
					AtlasStyler.R("TextRulesList.Labeltext.Title"));

			{
				/**
				 * Label for the selection of the value attribute
				 */

				DefaultComboBoxModel model = new DefaultComboBoxModel(ASUtil
						.getValueFieldNamesPrefereStrings(
								rulesList.getStyledFeatures().getSchema(),
								false).toArray());
				jComboBoxLabelField = new AttributesJComboBox(atlasStyler,
						model);
				jComboBoxLabelField.addItemListener(new ItemListener() {

					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									PropertyName literalLabelField1 = ASUtil.ff2
											.property((String) jComboBoxLabelField
													.getSelectedItem());

									PropertyName literalLabelField2 = ASUtil.ff2
											.property((String) jComboBoxLabelField2
													.getSelectedItem());

									StylingUtil.setDoublePropertyName(rulesList
											.getSymbolizer(),
											literalLabelField1,
											literalLabelField2);

									rulesList.fireEvents(new RuleChangedEvent(
											"The LabelProperyName changed to "
													+ jComboBoxLabelField
															.getSelectedItem(),
											rulesList));
								}
							});
						}
					}

				});

				jPanelLabelDefinition.add(new JLabel(AtlasStyler
						.R("TextRulesList.LabellingAttribute")));
				jPanelLabelDefinition.add(jComboBoxLabelField);
				
			}
			
			{
				// Second label
				
				JLabel comp = new JLabel(AtlasStyler
						.R("TextRulesList.2ndLabellingAttribute"));
				comp.setToolTipText( AtlasStyler.R("TextRulesList.2ndLabellingAttribute") );
				
				jPanelLabelDefinition.add(comp);
				
				jPanelLabelDefinition.add(getJComboBoxLabelField2());
				
				getJComboBoxLabelField2().setToolTipText( AtlasStyler.R("TextRulesList.2ndLabellingAttribute") );
			}

			{
				DefaultComboBoxModel model = new DefaultComboBoxModel(
						FeatureUtil
								.getNumericalFieldNames(
										rulesList.getStyledFeatures()
												.getSchema(), true).toArray());
				jComboBoxPriorityField = new AttributesJComboBox(atlasStyler,
						model);
				jComboBoxPriorityField.addItemListener(new ItemListener() {

					public void itemStateChanged(ItemEvent e) {
						if (e.getStateChange() == ItemEvent.SELECTED) {

							final String selectedItem = (String) jComboBoxPriorityField
									.getSelectedItem();

							if (selectedItem == null || selectedItem.equals("")) {
								rulesList.getSymbolizer().setPriority(
										Expression.NIL);

							} else {
								rulesList.getSymbolizer().setPriority(
										ASUtil.ff2.property(selectedItem));
							}

							rulesList.fireEvents(new RuleChangedEvent(
									"LabelPriorityField changed to "
											+ jComboBoxPriorityField
													.getSelectedItem(),
									rulesList));
						}
					}

				});

				final JLabel priorityLabel = new JLabel(AtlasStyler
						.R("TextRuleListGUI.labelingPriorityField"));
				priorityLabel.setToolTipText(AtlasStyler
						.R("TextRuleListGUI.labelingPriorityField.TT"));
				jComboBoxPriorityField.setToolTipText(AtlasStyler
						.R("TextRuleListGUI.labelingPriorityField.TT"));
				jPanelLabelDefinition.add(priorityLabel);
				jPanelLabelDefinition.add(jComboBoxPriorityField);
			}
		}

		return jPanelLabelDefinition;
	}

	/**
	 * Creates a {@link JComboBox} that offers to select attributes - text
	 * attributes preferred
	 */
	private JComboBox getJComboBoxLabelField2() {
		if (jComboBoxLabelField2 == null) {

			final List<String> valueFieldNamesPrefereStrings = ASUtil
					.getValueFieldNamesPrefereStrings(rulesList
							.getStyledFeatures().getSchema(), false);
			valueFieldNamesPrefereStrings.add(0, "-");
			jComboBoxLabelField2 = new AttributesJComboBox(atlasStyler,
					valueFieldNamesPrefereStrings);

			/**
			 * Read the selected attribute name from the symbolizer and set it
			 * in the JComboBox
			 */
			try {
				final TextSymbolizer textSymbolizer = rulesList.getSymbolizer();
				PropertyName pn2 = StylingUtil.getSecondPropertyName(rulesList
						.getStyledFeatures().getSchema(), textSymbolizer);

				if (pn2 == null) {
					jComboBoxLabelField2.setSelectedItem("-");
				} else {
					jComboBoxLabelField2.setSelectedItem(pn2.toString());
				}
			} catch (Exception e) {
				LOGGER
						.error(
								"Unable to read the value attribute from the TextSymbolizer",
								e);
			}

			/**
			 * Update the Label in the TextRuleList
			 */
			jComboBoxLabelField2.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								PropertyName literalLabelField1 = ASUtil.ff2
										.property((String) jComboBoxLabelField
												.getSelectedItem());
								PropertyName literalLabelField2 = ASUtil.ff2
										.property((String) jComboBoxLabelField2
												.getSelectedItem());
								StylingUtil.setDoublePropertyName(rulesList
										.getSymbolizer(), literalLabelField1,
										literalLabelField2);

								rulesList.fireEvents(new RuleChangedEvent(
										"The LabelProperyName changed to "
												+ jComboBoxLabelField2
														.getSelectedItem(),
										rulesList));
							}
						});
					}
				}

			});
		}
		return jComboBoxLabelField2;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @param features
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelEditTextSymbolizer() {
		if (jPanelEditTextSymbolizer == null) {

			// TODO ! We need as many samples, as we have label style classes.
			// they
			// have to be created with thour filters..
			// TODO or we make the jmappane not a symbolzer preview, but a
			// ruleliste
			// preview.. so we have all styles in one preview... and maybe all
			// features in
			// the preview..
			FeatureCollection<SimpleFeatureType, SimpleFeature> features;
			try {

				LOGGER.debug("Putting 100 random features into the preview");
				Filter filter = rulesList.getClassesFilters().get(0) != null ? rulesList
						.getClassesFilters().get(0)
						: Filter.INCLUDE;

				features = rulesList.getStyledFeatures().getFeatureSource()
						.getFeatures(
								new DefaultQuery(rulesList.getStyledFeatures()
										.getSchema().getTypeName(), filter,
										100, null, "max 100 sample features"));
				
				if (features.size() == 0 && filter != Filter.INCLUDE) {
					LOGGER.info("Getting preview features for the filter "+filter+" failed, getting preview features without a filter!");
					
					features = rulesList.getStyledFeatures().getFeatureSource()
					.getFeatures(
							new DefaultQuery(rulesList.getStyledFeatures()
									.getSchema().getTypeName(), Filter.INCLUDE,
									100, null, "max 100 sample features"));
				}				

			} catch (IOException e) {
				LOGGER
						.error(
								"Sample features for TextSymbolizer Preview could not be requested:",
								e);
				throw new RuntimeException(
						"Sample features for TextSymbolizer Preview could not be requested",
						e);
			}

			jPanelEditTextSymbolizer = new TextSymbolizerEditGUI(rulesList,
					atlasStyler, features);
			// jPanelEditTextSymbolizer.setBorder(BorderFactory
			// .createTitledBorder(AtlasStyler
			// .R("TextRuleListGUI.TextStyling.BorderTitle")));
		}
		return jPanelEditTextSymbolizer;
	}

	//
	// /**
	// * This method initializes jButton
	// *
	// * @return javax.swing.JButton
	// */
	// private JButton getJButton() {
	// if (jButtonPlacement == null) {
	// jButtonPlacement = new JButton();
	// jButtonPlacement.setFont(jButtonPlacement.getFont().deriveFont(
	// AtlasStylerTabbedPane.BUTTON_FONT_STYLE,
	// AtlasStylerTabbedPane.BUTTON_FONT_SIZE));
	//			
	// LabelPlacement labelPlacement =
	// rulesList.getSymbolizers().get(0).getLabelPlacement();
	// PointPlacement pPlacement;
	// LinePlacement linePlacement;
	//			
	// linePlacement.set
	//			
	// AnchorPoint ap = StylingUtil.STYLE_BUILDER.createAnchorPoint(10., 10.);
	// pPlacement.setAnchorPoint(ap);
	//			
	// jButtonPlacement.setEnabled(false);
	// }
	// return jButtonPlacement;
	// }
	//
	// /**
	// * This method initializes jButton
	// *
	// * @return javax.swing.JButton
	// */
	// private JButton getJButtonScaleRange() {
	// if (jButtonScaleRange == null) {
	// jButtonScaleRange = new JButton(AtlasStyler
	// .R("TextRulesList.Button.ScaleRange"));
	// jButtonScaleRange.setFont(jButtonScaleRange.getFont().deriveFont(
	// AtlasStylerTabbedPane.BUTTON_FONT_STYLE,
	// AtlasStylerTabbedPane.BUTTON_FONT_SIZE));
	// jButtonScaleRange.setEnabled(false);
	// }
	// return jButtonScaleRange;
	// }

}
