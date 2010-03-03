/*******************************************************************************
 * Copyright (c) 2010 Stefan A. Krüger (soon changing to Stefan A. Tzeggai).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan A. Krüger (soon changing to Stefan A. Tzeggai) - initial API and implementation
 ******************************************************************************/
package skrueger.sld.gui;

/*******************************************************************************
 * The allowed SVG/CSS styling parameters for a stroke are: “stroke” (color),
 * “stroke- opacity”, “stroke-width”, “stroke-linejoin”, “stroke-linecap”,
 * “stroke- dasharray”, and “stroke-dashoffset”. The chosen parameter is given
 * by the name attribute of the CssParameter element. The “stroke” CssParameter
 * element gives the solid color that will be used for a stroke. The color value
 * is RGB-encoded using two hexadecimal digits per primary-color component, in
 * the order Red, Green, Blue, prefixed with a hash (#) sign. The hexadecimal
 * digits between A and F may be in either uppercase or lowercase. For example,
 * full red is encoded as “#ff0000” (with no quotation marks). If the “stroke”
 * CssParameter element is absent, the default color is defined to be black
 * (“#000000”) in the context of the LineSymbolizer. The “stroke-opacity”
 * CssParameter element specifies the level of translucency to use when
 * rendering the stroke. The value is encoded as a floating-point value
 * (“float”) between 0.0 and 1.0 with 0.0 representing completely transparent
 * and 1.0 representing completely opaque, with a linear scale of translucency
 * for intermediate values. For example, “0.65” would represent 65% opacity. The
 * default value is 1.0 (opaque). The “stroke-width” CssParameter element gives
 * the absolute width (thickness) of a stroke in pixels encoded as a float.
 * (Arguably, more units could be provided for encoding sizes, such as
 * millimeters or typesetter's points.) The default is 1.0. Fractional numbers
 * are allowed (with a system-dependent interpretation) but negative numbers are
 * not. The “stroke-linejoin” and “stroke-linecap” CssParameter elements encode
 * enumerated values telling how line strings should be joined (between line
 * segments) and capped (at the two ends of the line string). The values are
 * represented as content strings. The allowed values for line join are “mitre”,
 * “round”, and “bevel”, and the allowed values for line cap are “butt”,
 * “round”, and “square”. The default values are system- dependent. The
 * “stroke-dasharray” CssParameter element encodes a dash pattern as a series of
 * space separated floats. The first number gives the length in pixels of dash
 * to draw, the second gives the amount of space to leave, and this pattern
 * repeats. If an odd number of values is given, then the pattern is expanded by
 * repeating it twice to give an even number of values. Decimal values have a
 * system-dependent interpretation (usually depending on whether antialiasing is
 * being used). The default is to draw an unbroken line. The “stroke-dashoffset”
 * CssParameter element specifies the distance as a float into the
 * “stroke-dasharray” pattern at which to start drawing.
 * 
 * 
 */

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.geotools.styling.LineSymbolizer;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.swing.SwingUtil;
import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;
import skrueger.swing.ColorButton;

public class LineSymbolEditGUI extends AbstractEditGUI {
	private static final String[] LINEJOIN_VALUES = new String[] { "mitre",
			"round", "bevel" };

	private static final String[] LINECAP_VALUES = new String[] { "butt",
			"round", "square" };

	protected Logger LOGGER = ASUtil.createLogger(this);

	private final LineSymbolizer symbolizer;

	private JPanel jPanelStroke;

	private JLabel jLabelStrokeColor;

	private JLabel jLabelStrokeWidth;

	private JLabel jLabelStrokeOpacity;

	private ColorButton jButtonStrokeColor;

	private JComboBox jComboBoxStrokeWidth;

	private JComboBox jComboBoxStrokeOpacity;

	private JPanel jPanelLineStyle = null;

	private JLabel jLabelLineJoin = null;

	private JComboBox jComboBoxLinejoin = null;

	private JLabel jLabelLinecap = null;

	private JComboBox jComboBoxLineCap = null;

	private JPanel jPanelDashArray = null;

	private JTextField jTextFieldDashPattern = null;

	private JLabel jLabelDashPattern = null;

	private JLabel jLabelDashOffset = null;

	private JComboBox jComboBoxDashOffset = null;

	public LineSymbolEditGUI(
			final org.geotools.styling.LineSymbolizer symbolizer) {
		this.symbolizer = symbolizer;
		initialize();
	}

	private void initialize() {
		GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
		gridBagConstraints12.gridx = 1;
		gridBagConstraints12.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints12.gridy = 1;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.insets = new Insets(5, 0, 5, 0);
		gridBagConstraints1.gridwidth = 2;
		gridBagConstraints1.gridy = 2;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.gridy = 0;
		this.setSize(310, 280);
		this.setLayout(new GridBagLayout());
		this.add(getJPanelStroke(), gridBagConstraints);
		this.add(getJPanelLineStyle(), gridBagConstraints1);
		this.add(getJPanelDashArray(), gridBagConstraints12);
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelStroke() {
		if (jPanelStroke == null) {
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.fill = GridBagConstraints.NONE;
			gridBagConstraints9.gridy = 0;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints9.gridx = 5;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 4;
			gridBagConstraints8.insets = new Insets(0, 15, 0, 0);
			gridBagConstraints8.gridy = 0;
			jLabelStrokeOpacity = new JLabel();
			jLabelStrokeOpacity.setText(AtlasStyler.R("OpacityLabel"));
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = GridBagConstraints.NONE;
			gridBagConstraints7.gridy = 0;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints7.gridx = 3;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 2;
			gridBagConstraints6.insets = new Insets(0, 15, 0, 0);
			gridBagConstraints6.gridy = 0;
			jLabelStrokeWidth = new JLabel();
			jLabelStrokeWidth.setText(AtlasStyler.R("WidthLabel"));
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints5.gridy = 0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints4.gridy = 0;
			jLabelStrokeColor = new JLabel();
			jLabelStrokeColor.setText(AtlasStyler.R("ColorLabel"));
			jPanelStroke = new JPanel();
			jPanelStroke.setLayout(new GridBagLayout());
			// jPanelStroke.setBorder(BorderFactory.createTitledBorder(""
			// + "Stroke"));
			jPanelStroke.add(jLabelStrokeColor, gridBagConstraints4);
			jPanelStroke.add(getJButtonStrokeColor(), gridBagConstraints5);
			jPanelStroke.add(jLabelStrokeWidth, gridBagConstraints6);
			jPanelStroke.add(getJComboBoxStrokeWidth(), gridBagConstraints7);
			jPanelStroke.add(jLabelStrokeOpacity, gridBagConstraints8);
			jPanelStroke.add(getJComboBoxStrokeOpacity(), gridBagConstraints9);
		}
		return jPanelStroke;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private ColorButton getJButtonStrokeColor() {
		if (jButtonStrokeColor == null) {
			jButtonStrokeColor = new ColorButton(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					Color color = null;

					String substring = symbolizer.getStroke().getColor()
							.toString();
					color = Color.decode(substring);

					Color newColor = ASUtil.showColorChooser(
							LineSymbolEditGUI.this, AtlasStyler
									.R("Stroke.ColorChooserDialog.Title"),
							color);

					if (newColor != null) {
						symbolizer.getStroke().setColor(
								StylingUtil.STYLE_BUILDER
										.colorExpression(newColor));

						LineSymbolEditGUI.this.firePropertyChange(
								PROPERTY_UPDATED, null, null);

						jButtonStrokeColor.setColor(newColor);

					}

				}

			});

			if (symbolizer.getStroke().getColor() != null) {
				jButtonStrokeColor.setColor( symbolizer
						.getStroke().getColor());
			} else {
				jButtonStrokeColor.setEnabled(false);
				jLabelStrokeColor.setEnabled(false);
			}

		}
		return jButtonStrokeColor;
	}

	/**
	 * This method initializes jComboBox1
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxStrokeWidth() {
		if (jComboBoxStrokeWidth == null) {

			jComboBoxStrokeWidth = new JComboBox();

			jComboBoxStrokeWidth
					.setModel(new DefaultComboBoxModel(WIDTH_VALUES));
			jComboBoxStrokeWidth.setRenderer(WIDTH_VALUES_RENDERER);
			ASUtil.selectOrInsert(jComboBoxStrokeWidth, symbolizer.getStroke()
					.getWidth());

			jComboBoxStrokeWidth.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getStroke().setWidth(
								ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);

					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxStrokeWidth);
		}
		return jComboBoxStrokeWidth;
	}

	/**
	 * This method initializes jComboBox1
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxStrokeOpacity() {
		if (jComboBoxStrokeOpacity == null) {
			jComboBoxStrokeOpacity = new JComboBox();
			jComboBoxStrokeOpacity.setModel(new DefaultComboBoxModel(
					OPACITY_VALUES));

			ASUtil.selectOrInsert(jComboBoxStrokeOpacity, symbolizer
					.getStroke().getOpacity());

			// else {
			// jComboBoxStrokeOpacity.setEnabled(false);
			// jLabelStrokeOpacity.setEnabled(false);
			// }

			jComboBoxStrokeOpacity.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getStroke().setOpacity(
								ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);
					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxStrokeOpacity);
		}
		return jComboBoxStrokeOpacity;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelLineStyle() {
		if (jPanelLineStyle == null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints11.gridy = 0;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.anchor = GridBagConstraints.WEST;
			gridBagConstraints11.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints11.gridx = 3;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 2;
			gridBagConstraints10.anchor = GridBagConstraints.EAST;
			gridBagConstraints10.gridy = 0;
			gridBagConstraints10.insets = new Insets(0, 5, 5, 5);
			jLabelLinecap = new JLabel();
			jLabelLinecap.setText(AtlasStyler.R("LinecapLabel"));
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.anchor = GridBagConstraints.WEST;
			gridBagConstraints3.insets = new Insets(0, 5, 5, 5);
			gridBagConstraints3.gridx = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.gridy = 0;
			gridBagConstraints2.insets = new Insets(0, 5, 5, 5);
			jLabelLineJoin = new JLabel();
			jLabelLineJoin.setText(AtlasStyler.R("LinejoinLabel"));
			jPanelLineStyle = new JPanel();
			jPanelLineStyle.setLayout(new GridBagLayout());
			jPanelLineStyle.setBorder(BorderFactory
					.createTitledBorder(AtlasStyler
							.R("LineSymbolEdit.LineStyle.Title")));
			jPanelLineStyle.add(jLabelLineJoin, gridBagConstraints2);
			jPanelLineStyle.add(getJComboBoxLineJoin(), gridBagConstraints3);
			jPanelLineStyle.add(jLabelLinecap, gridBagConstraints10);
			jPanelLineStyle.add(getJComboBoxLineCap(), gridBagConstraints11);
		}
		return jPanelLineStyle;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxLineJoin() {
		if (jComboBoxLinejoin == null) {
			jComboBoxLinejoin = new JComboBox();
			jComboBoxLinejoin
					.setModel(new DefaultComboBoxModel(LINEJOIN_VALUES));

			/** Preset when started * */
			String preset;
			try {
				Expression lineJoin = symbolizer.getStroke().getLineJoin();
				preset = ((Literal) lineJoin).toString();
			} catch (Exception e) {
				preset = LINEJOIN_VALUES[0];
				symbolizer.getStroke().setLineJoin(ASUtil.ff2.literal(preset));
			}
			jComboBoxLinejoin.setSelectedItem(preset);
			jComboBoxLinejoin.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getStroke().setLineCap(
								ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);
					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxLinejoin);

		}
		return jComboBoxLinejoin;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxLineCap() {
		if (jComboBoxLineCap == null) {
			jComboBoxLineCap = new JComboBox();
			jComboBoxLineCap.setModel(new DefaultComboBoxModel(LINECAP_VALUES));

			/** Preset when started * */
			String preset;
			try {
				Expression lineCap = symbolizer.getStroke().getLineCap();
				preset = ((Literal) lineCap).toString();
			} catch (Exception e) {
				preset = LINECAP_VALUES[0];
				symbolizer.getStroke().setLineCap(ASUtil.ff2.literal(preset));
			}
			jComboBoxLinejoin.setSelectedItem(preset);

			SwingUtil.addMouseWheelForCombobox(jComboBoxLineCap);
		}
		return jComboBoxLineCap;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelDashArray() {
		if (jPanelDashArray == null) {
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.fill = GridBagConstraints.VERTICAL;
			gridBagConstraints16.gridy = 0;
			gridBagConstraints16.weightx = 1.0;
			gridBagConstraints16.anchor = GridBagConstraints.WEST;
			gridBagConstraints16.gridx = 3;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 2;
			gridBagConstraints15.insets = new Insets(0, 15, 0, 5);
			gridBagConstraints15.anchor = GridBagConstraints.EAST;
			gridBagConstraints15.gridy = 0;
			jLabelDashOffset = new JLabel();
			jLabelDashOffset.setText(AtlasStyler
					.R("LineSymbolEdit.DashedLine.DashOffset"));
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 0;
			gridBagConstraints14.gridy = 0;
			jLabelDashPattern = new JLabel();
			jLabelDashPattern.setText(AtlasStyler
					.R("LineSymbolEdit.DashedLine.DashPattern"));
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints13.gridy = 0;
			gridBagConstraints13.weightx = 1.0;
			gridBagConstraints13.anchor = GridBagConstraints.WEST;
			gridBagConstraints13.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints13.gridx = 1;
			jPanelDashArray = new JPanel();
			jPanelDashArray.setLayout(new GridBagLayout());
			jPanelDashArray.setBorder(BorderFactory
					.createTitledBorder(AtlasStyler
							.R("LineSymbolEdit.DashedLine.Title")));
			jPanelDashArray.add(getJTextFieldDashPattern(),
					gridBagConstraints13);
			jPanelDashArray.add(jLabelDashPattern, gridBagConstraints14);
			jPanelDashArray.add(jLabelDashOffset, gridBagConstraints15);
			jPanelDashArray.add(getJComboBoxDashOffset(), gridBagConstraints16);
		}
		return jPanelDashArray;
	}

	/**
	 * This method initializes jTextField
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldDashPattern() {
		if (jTextFieldDashPattern == null) {
			jTextFieldDashPattern = new JTextField(12);

			updateTextFieldDashPattern();

			jTextFieldDashPattern.addKeyListener(new KeyListener() {

				public void keyPressed(KeyEvent e) {
				}

				public void keyReleased(KeyEvent e) {
					if ((e.getKeyCode() == KeyEvent.VK_ENTER)
							|| (e.getKeyCode() == KeyEvent.VK_TAB)) {
						updateDashFromtextfield();
					}
				}

				public void keyTyped(KeyEvent e) {
				}

			});

			jTextFieldDashPattern.addFocusListener(new FocusListener() {

				public void focusGained(FocusEvent e) {
				}

				public void focusLost(FocusEvent e) {
					updateDashFromtextfield();
				}

			});

			jTextFieldDashPattern.setToolTipText(AtlasStyler
					.R("LineSymbolEditGUI.dashPattern_tooltip"));
		}
		return jTextFieldDashPattern;
	}

	private void updateDashFromtextfield() {
		String text = jTextFieldDashPattern.getText();
		if ((text == null) || (text.trim().equals(""))) {
			symbolizer.getStroke().setDashArray(null);
			firePropertyChange(PROPERTY_UPDATED, null, null);
			return;
		}

		String[] strings = text.split(" ");
		float[] dashArrays = new float[strings.length];
		int count = 0;
		for (String s : strings) {
			try {
				float f = Float.valueOf(s);
				dashArrays[count] = f;
				count++;
			} catch (NumberFormatException e) {
				updateTextFieldDashPattern();
				JOptionPane
						.showMessageDialog(
								LineSymbolEditGUI.this,
								AtlasStyler
										.R("LineSymbolEditGUI.dashPattern_illegalDashPatternFormatMessage"));
				return;
			}
		}
		symbolizer.getStroke().setDashArray(dashArrays);
		firePropertyChange(PROPERTY_UPDATED, null, null);
	}

	private void updateTextFieldDashPattern() {
		float[] dashArrays = symbolizer.getStroke().getDashArray();
		String text = "";
		if (dashArrays != null)
			for (float f : dashArrays) {
				text += f + " ";
			}
		getJTextFieldDashPattern().setText(text);
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxDashOffset() {
		if (jComboBoxDashOffset == null) {
			jComboBoxDashOffset = new JComboBox(new DefaultComboBoxModel(
					DISPLACEMENT_VALUES));

			jComboBoxDashOffset.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getStroke().setDashOffset(
								ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);

					}
				}

			});

			ASUtil.selectOrInsert(jComboBoxDashOffset, symbolizer.getStroke()
					.getDashOffset());

			SwingUtil.addMouseWheelForCombobox(jComboBoxDashOffset);
		}
		return jComboBoxDashOffset;
	}

}
