/*******************************************************************************
 * Copyright (c) 2009 Stefan A. Krüger.
 * 
 * This file is part of the AtlasStyler SLD/SE Editor application - A Java Swing-based GUI to create OGC Styled Layer Descriptor (SLD 1.0) / OGC Symbology Encoding 1.1 (SE) XML files.
 * http://www.geopublishing.org
 * 
 * AtlasStyler SLD/SE Editor is part of the Geopublishing Framework hosted at:
 * http://wald.intevation.org/projects/atlas-framework/
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License (license.txt)
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * or try this link: http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     Stefan A. Krüger - initial API and implementation
 ******************************************************************************/
package skrueger.sld.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geotools.data.DefaultQuery;
import org.geotools.feature.FeatureCollection;
import org.geotools.styling.TextSymbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.swing.JPanel;
import skrueger.sld.ASUtil;
import skrueger.sld.AbstractRuleList;
import skrueger.sld.AtlasStyler;
import skrueger.sld.RuleChangedEvent;
import skrueger.sld.SingleRuleList;
import skrueger.sld.StyleChangeListener;
import skrueger.sld.StyleChangedEvent;
import skrueger.sld.TextRuleList;
import skrueger.swing.ThinButton;

/**
 * This GUI defines all the buttons to define the {@link TextSymbolizer}
 * 
 * @author SK
 */
public class TextRuleListGUI extends JPanel {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private static final long serialVersionUID = 1L;

	private JCheckBox jCheckBoxEnabled = null;

	private JPanel jPanelClass = null;

	private JLabel jLabelClass = null;

	private JComboBox jComboBoxClass = null;

	private JCheckBox jCheckBoxClassEnabled = null;

	private JLabel jLabelClassEnabled = null;

	private JPanel jPanelClassButtons = null;

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

		jComboBoxClass.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					rulesList.setSelIdx(jComboBoxClass.getSelectedIndex());
					updateGUI();
				}
			}
		});
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
		 * Class managemant buttons
		 */
		jButtonClassDelete.setEnabled(jComboBoxClass.getSelectedIndex() != 0);
		jButtonClassRename.setEnabled(jComboBoxClass.getSelectedIndex() != 0);

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
		this.setLayout(new MigLayout("wrap 1, top"));
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
				}

			});
			jCheckBoxEnabled.setSelected(rulesList.isEnabled());
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
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 3;
			gridBagConstraints17.weightx = 0.1;
			gridBagConstraints17.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints17.gridy = 4;
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 0;
			gridBagConstraints16.gridwidth = 3;
			gridBagConstraints16.weightx = 1.0;
			gridBagConstraints16.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints16.gridy = 4;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.weightx = 1.0;
			gridBagConstraints15.gridwidth = 4;
			gridBagConstraints15.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints15.insets = new Insets(5, 0, 0, 0);
			gridBagConstraints15.gridy = 3;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 0;
			gridBagConstraints12.gridwidth = 4;
			gridBagConstraints12.weightx = 1.0;
			gridBagConstraints12.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints12.insets = new Insets(5, 0, 0, 0);
			gridBagConstraints12.gridy = 2;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.gridwidth = 4;
			gridBagConstraints7.gridy = 1;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 3;
			gridBagConstraints6.anchor = GridBagConstraints.WEST;
			gridBagConstraints6.insets = new Insets(5, 5, 0, 5);
			gridBagConstraints6.gridy = 0;
			jLabelClassEnabled = new JLabel(AtlasStyler
					.R("TextRulesList.Labelclass.Checkbox"));
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 2;
			gridBagConstraints5.anchor = GridBagConstraints.EAST;
			gridBagConstraints5.insets = new Insets(5, 10, 5, 0);
			gridBagConstraints5.gridy = 0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.anchor = GridBagConstraints.WEST;
			gridBagConstraints4.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints4.gridx = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints3.gridy = 0;
			jLabelClass = new JLabel(AtlasStyler.R("TextRulesList.Labelclass"));
			jPanelClass = new JPanel();
			jPanelClass.setLayout(new GridBagLayout());
//			jPanelClass.setBorder(BorderFactory.createTitledBorder(""));
			jPanelClass.add(jLabelClass, gridBagConstraints3);
			jPanelClass.add(getJComboBoxClass(), gridBagConstraints4);
			jPanelClass.add(getJCheckBoxClassEnabled(), gridBagConstraints5);
			jPanelClass.add(jLabelClassEnabled, gridBagConstraints6);
			jPanelClass.add(getJPanelClassButtons(), gridBagConstraints7);
			jPanelClass.add(getJPanelTextString(), gridBagConstraints12);
			jPanelClass
					.add(getJPanelEditTextSymbolizer(), gridBagConstraints15);

			// jPanelClass.add(getJPanel(), gridBagConstraints17);
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

			ASUtil.addMouseWheelForCombobox(jComboBoxClass);
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
			jCheckBoxClassEnabled = new JCheckBox();
			jCheckBoxClassEnabled.setSelected(true);
			jCheckBoxClassEnabled.setEnabled(false);
		}
		return jCheckBoxClassEnabled;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelClassButtons() {
		if (jPanelClassButtons == null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 3;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.insets = new Insets(5, 0, 5, 5);
			gridBagConstraints11.gridy = 0;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 2;
			gridBagConstraints10.weightx = 1.0;
			gridBagConstraints10.insets = new Insets(5, 0, 5, 0);
			gridBagConstraints10.gridy = 0;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.insets = new Insets(5, 0, 5, 0);
			gridBagConstraints9.gridy = 0;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.insets = new Insets(5, 5, 5, 0);
			gridBagConstraints8.gridy = 0;
			jPanelClassButtons = new JPanel();
			jPanelClassButtons.setLayout(new GridBagLayout());
			jPanelClassButtons.add(getJButtonClassAdd(), gridBagConstraints8);
			jPanelClassButtons
					.add(getJButtonClassDelete(), gridBagConstraints9);
			jPanelClassButtons.add(getJButtonClassRename(),
					gridBagConstraints10);
			jPanelClassButtons.add(getJButtonClassFromSymbols(),
					gridBagConstraints11);
		}
		return jPanelClassButtons;
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
					if (rulesList.getSelectedIdx() == 0) {
						LOGGER
								.warn("Will not delete the default rule.. Why is the button enabled anyway?");
						jButtonClassDelete.setEnabled(false);
						return;
					}
					int idx = rulesList.getSelectedIdx();
					rulesList.getSymbolizers().remove(idx);
					rulesList.getFilterRules().remove(idx);
					rulesList.getRuleNames().remove(idx);
					jComboBoxClass.setModel(new DefaultComboBoxModel(rulesList
							.getRuleNames().toArray()));
					rulesList.setSelIdx(0);
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
								rulesList.getSelectedIdx(), result);
						jComboBoxClass.setModel(new DefaultComboBoxModel(
								rulesList.getRuleNames().toArray()));
						jComboBoxClass.setSelectedIndex(rulesList
								.getSelectedIdx());
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
			jButtonClassFromSymbols = new JButton(
					AtlasStyler
							.R("TextRulesList.Labelclass.Action.LoadClassesFromSymbols"));
			jButtonClassFromSymbols.setFont(jButtonClassFromSymbols.getFont()
					.deriveFont(AtlasStylerTabbedPane.BUTTON_FONT_STYLE,
							AtlasStylerTabbedPane.BUTTON_FONT_SIZE));

			jButtonClassFromSymbols.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					AbstractRuleList symbRL = atlasStyler
							.getLastChangedRuleList();

					rulesList.importClassesFromStyle(symbRL,
							TextRuleListGUI.this);

					jComboBoxClass.setModel(new DefaultComboBoxModel(rulesList
							.getRuleNames().toArray()));
					jComboBoxClass.setSelectedIndex(0);
				}
			});

			jButtonClassFromSymbols
					.setToolTipText(AtlasStyler
							.R("TextRulesList.Labelclass.Action.LoadClassesFromSymbols.TT"));
		}
		return jButtonClassFromSymbols;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelTextString() {
		JPanel jPanelTextString = new JPanel(new MigLayout("w 100%, wrap 3"));

		jPanelTextString.setBorder(BorderFactory.createTitledBorder(AtlasStyler
				.R("TextRulesList.Labeltext.Title")));

		{
			/**
			 * Label for the selection of the value attribute
			 */

			DefaultComboBoxModel model = new DefaultComboBoxModel(ASUtil
					.getValueFieldNamesPrefereStrings(
							rulesList.getStyledFeatures().getSchema(), false)
					.toArray());
			jComboBoxLabelField = new AttributesJComboBox(atlasStyler, model);
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
										.getSymbolizer(), literalLabelField1,
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

			jPanelTextString.add(new JLabel(AtlasStyler
					.R("TextRulesList.LabellingAttribute")));
			jPanelTextString.add(jComboBoxLabelField);
			jPanelTextString.add(getJComboBoxLabelField2());
		}

		{
			DefaultComboBoxModel model = new DefaultComboBoxModel(ASUtil
					.getNumericalFieldNames(
							rulesList.getStyledFeatures().getSchema(), true)
					.toArray());
			jComboBoxPriorityField = new AttributesJComboBox(atlasStyler, model);
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
												.getSelectedItem(), rulesList));
					}
				}

			});

			final JLabel priorityLabel = new JLabel(AtlasStyler
					.R("TextRuleListGUI.labelingPriorityField"));
			priorityLabel.setToolTipText(AtlasStyler
					.R("TextRuleListGUI.labelingPriorityField.TT"));
			jComboBoxPriorityField.setToolTipText(AtlasStyler
					.R("TextRuleListGUI.labelingPriorityField.TT"));
			jPanelTextString.add(priorityLabel);
			jPanelTextString.add(jComboBoxPriorityField);
		}

		return jPanelTextString;
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
				Filter filter = rulesList.getFilterRules().get(0) != null ? rulesList
						.getFilterRules().get(0)
						: Filter.INCLUDE;

				features = rulesList.getStyledFeatures().getFeatureSource()
						.getFeatures(
								new DefaultQuery(rulesList.getStyledFeatures()
										.getSchema().getTypeName(), filter,
										100, null, "max 100 sample features"));

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
//			jPanelEditTextSymbolizer.setBorder(BorderFactory
//					.createTitledBorder(AtlasStyler
//							.R("TextRuleListGUI.TextStyling.BorderTitle")));
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
