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
import java.awt.Component;
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
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.ColorButton;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SwingUtil;

public class LineSymbolEditGUI extends AbstractStyleEditGUI {
	private static final String[] LINEJOIN_VALUES = new String[] { "mitre", "round", "bevel" };

	private static final String[] LINECAP_VALUES = new String[] { "butt", "round", "square" };

	protected Logger LOGGER = LangUtil.createLogger(this);

	private final LineSymbolizer symbolizer;

	private JPanel jPanelStroke;

	private final JLabel jLabelStrokeColor = new JLabel(AtlasStylerVector.R("ColorLabel"));

	private final JLabel jLabelStrokeWidth = new JLabel(AtlasStylerVector.R("WidthLabel"));

	private final JLabel jLabelStrokeOpacity = new JLabel(AtlasStylerVector.R("OpacityLabel"));

	private ColorButton jButtonStrokeColor;

	private JComboBox jComboBoxStrokeWidth;

	private OpacityJComboBox jComboBoxStrokeOpacity;

	// private JPanel jPanelLineStyle = null;

	private final JLabel jLabelLineJoin = new JLabel(AtlasStylerVector.R("LinejoinLabel"));

	private final JLabel jLabelPerpendicularOffset = new JLabel("Perpend. Offset");// i8n

	private JComboBox jComboBoxLinejoin = null;

	private final JLabel jLabelLinecap = new JLabel(AtlasStylerVector.R("LinecapLabel"));

	private JComboBox jComboBoxLineCap = null;

	private JPanel jPanelDashArray = null;

	private JTextField jTextFieldDashPattern = null;

	private JLabel jLabelDashPattern = null;

	private JLabel jLabelDashOffset = null;

	private JComboBox jComboBoxDashOffset = null;

	private JComboBox jComboBoxPerpendicularOffset = null;

	private JPanel jPanelGraphicStroke;

	/**
	 * When switching the {@link Graphic} to use an {@link ExternalGraphic} we
	 * backup the old {@link Mark}
	 **/
	protected Mark backupMark = null;
	/**
	 * When switching the {@link Graphic} to use a {@link Mark} we backup the
	 * old {@link ExternalGraphic}
	 **/
	protected ExternalGraphic backupExternalGraphic = null;

	private JComboBox jComboBoxStyleType;

	private JComboBox jComboBoxWidthExtGraphic;

	protected Graphic backupStroke;

	public LineSymbolEditGUI(final org.geotools.styling.LineSymbolizer symbolizer) {
		this.symbolizer = symbolizer;
		initialize();
	}

	private void initialize() {
		// this.setSize(310, 280);
		this.setLayout(new MigLayout("wrap 1"));
		this.add(getJComboBoxStyleType(), "sgx");
		this.add(getJPanelStroke(), "sgx");
		// this.add(getJPanelLineStyle(), "sgx");
		this.add(getJPanelDashArray(), "sgx");

		this.add(getJPanelGraphicStroke(), "sgx");
	}

	private JComboBox getJComboBoxStyleType() {
		if (jComboBoxStyleType == null) {
			jComboBoxStyleType = new JComboBox();
			jComboBoxStyleType.setModel(new DefaultComboBoxModel(new String[] { "Normal", "Ext Graphics" })); // i8n

			if (symbolizer.getStroke().getGraphicStroke() == null) {
				getJPanelGraphicStroke().setEnabled(false);
				getJPanelDashArray().setEnabled(true);
				getJPanelStroke().setEnabled(true);
			}
			jComboBoxStyleType.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						if (e.getItem().toString().equals("Normal")) {

							backupStroke = symbolizer.getStroke().getGraphicStroke();
							symbolizer.getStroke().setGraphicStroke(null);

							getJPanelGraphicStroke().setEnabled(false);
							getJPanelDashArray().setEnabled(true);
							getJPanelStroke().setEnabled(true);
							getJComboBoxPerpendicularOffset().setEnabled(false); // until
																					// proper
																					// implementation
																					// in
																					// geotools
							jLabelPerpendicularOffset.setEnabled(false);
							
						} else {
							if (backupStroke != null)
								symbolizer.getStroke().setGraphicStroke(backupStroke);
							else {
							}

							getJPanelGraphicStroke().setEnabled(true);

							getJPanelDashArray().setEnabled(false);
							getJPanelStroke().setEnabled(false);
						}

						firePropertyChange(PROPERTY_UPDATED, null, null);

					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxStyleType);
		}
		return jComboBoxStyleType;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelStroke() {
		if (jPanelStroke == null) {
			jPanelStroke = new JPanel(new MigLayout("wrap 1", "[grow]"));
			jPanelStroke.setBorder(BorderFactory.createTitledBorder(AtlasStylerVector
					.R("LineSymbolEdit.LineStyle.Title")));

			jPanelStroke.add(jLabelStrokeColor, "split 6");
			jPanelStroke.add(getJButtonStrokeColor(), "");
			jPanelStroke.add(jLabelStrokeWidth, "gap unrel");
			jPanelStroke.add(getJComboBoxStrokeWidth(), "");
			jPanelStroke.add(jLabelStrokeOpacity, "gap unrel");
			jPanelStroke.add(getJComboBoxStrokeOpacity(), "");

			// wraps here
			jPanelStroke.add(jLabelLineJoin, "split 6");
			jPanelStroke.add(getJComboBoxLineJoin(), "");
			jPanelStroke.add(jLabelPerpendicularOffset, "");
			jPanelStroke.add(getJComboBoxPerpendicularOffset(), "");
			jPanelStroke.add(new JLabel(), "growx 100");
			jPanelStroke.add(jLabelLinecap, "");
			jPanelStroke.add(getJComboBoxLineCap(), "");

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

				@Override
				public void actionPerformed(final ActionEvent e) {
					Color color = null;

					final String substring = symbolizer.getStroke().getColor().toString();
					color = Color.decode(substring);

					final Color newColor = AVSwingUtil.showColorChooser(LineSymbolEditGUI.this,
							AtlasStylerVector.R("Stroke.ColorChooserDialog.Title"), color);

					if (newColor != null) {
						symbolizer.getStroke().setColor(StylingUtil.STYLE_BUILDER.colorExpression(newColor));

						LineSymbolEditGUI.this.firePropertyChange(PROPERTY_UPDATED, null, null);

						jButtonStrokeColor.setColor(newColor);

					}

				}

			});

			if (symbolizer.getStroke() != null && symbolizer.getStroke().getColor() != null) {
				jButtonStrokeColor.setColor(StylingUtil.getColorFromExpression(symbolizer.getStroke().getColor()));
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

			jComboBoxStrokeWidth.setModel(new DefaultComboBoxModel(WIDTH_VALUES));
			jComboBoxStrokeWidth.setRenderer(WIDTH_VALUES_RENDERER);
			ASUtil.selectOrInsert(jComboBoxStrokeWidth, symbolizer.getStroke().getWidth());

			jComboBoxStrokeWidth.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getStroke().setWidth(ASUtil.ff2.literal(e.getItem()));

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
			jComboBoxStrokeOpacity = new OpacityJComboBox();
			jComboBoxStrokeOpacity.setModel(new DefaultComboBoxModel(OPACITY_VALUES));

			if (symbolizer.getStroke() != null && symbolizer.getStroke().getOpacity() != null) {
				Expression opacity = symbolizer.getStroke().getOpacity();
				ASUtil.selectOrInsert(jComboBoxStrokeOpacity, opacity);
			} else {
				// set default?
			}

			jComboBoxStrokeOpacity.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getStroke().setOpacity(ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);
					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxStrokeOpacity);
		}
		return jComboBoxStrokeOpacity;
	}

	// /**
	// * This method initializes jPanel
	// *
	// * @return javax.swing.JPanel
	// */
	// private JPanel getJPanelLineStyle() {
	// if (jPanelLineStyle == null) {
	// jLabelLinecap = new JLabel(AtlasStyler.R("LinecapLabel"));
	// jLabelLineJoin = new JLabel(AtlasStyler.R("LinejoinLabel"));
	// jPanelLineStyle = new JPanel(new MigLayout("", "grow"));
	// jPanelLineStyle.setBorder(BorderFactory
	// .createTitledBorder(AtlasStyler
	// .R("LineSymbolEdit.LineStyle.Title")));
	//
	// jPanelLineStyle.add(jLabelLineJoin, "split 5");
	// jPanelLineStyle.add(getJComboBoxLineJoin(), "");
	// jPanelLineStyle.add(new JLabel(), "growx 100");
	// jPanelLineStyle.add(jLabelLinecap, "");
	// jPanelLineStyle.add(getJComboBoxLineCap(), "");
	// }
	// return jPanelLineStyle;
	// }

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxLineJoin() {
		if (jComboBoxLinejoin == null) {
			jComboBoxLinejoin = new JComboBox(LINEJOIN_VALUES);

			/** Preset when started * */
			String preset;
			try {
				final Expression lineJoin = symbolizer.getStroke().getLineJoin();
				preset = ((Literal) lineJoin).toString();
			} catch (final Exception e) {
				preset = LINEJOIN_VALUES[0];
				symbolizer.getStroke().setLineJoin(ASUtil.ff2.literal(preset));
			}

			// The combobox conatins the original Strings as used inside SLD,
			// but the renderer puts nicer expressions there
			jComboBoxLinejoin.setRenderer(new DefaultListCellRenderer() {

				@Override
				public Component getListCellRendererComponent(final JList list, final Object value, final int index,
						final boolean isSelected, final boolean cellHasFocus) {
					final Component p = super
							.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					if (p instanceof JLabel)
						((JLabel) p).setText(ASUtil.R("AtlasStyler.LineJoin.Values." + value));
					return p;
				}
			});

			jComboBoxLinejoin.setSelectedItem(preset);
			jComboBoxLinejoin.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						final Object itemStringValue = e.getItem();
						symbolizer.getStroke().setLineJoin(ASUtil.ff2.literal(itemStringValue));

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
			jComboBoxLineCap = new JComboBox(LINECAP_VALUES);

			/** Preset when started * */
			String preset;
			try {
				final Expression lineCap = symbolizer.getStroke().getLineCap();
				preset = ((Literal) lineCap).toString();
			} catch (final Exception e) {
				preset = LINECAP_VALUES[0];
				symbolizer.getStroke().setLineCap(ASUtil.ff2.literal(preset));
			}
			jComboBoxLineCap.setSelectedItem(preset);

			// The combobox conatins the original Strings as used inside SLD,
			// but the renderer puts nicer expressions there
			jComboBoxLineCap.setRenderer(new DefaultListCellRenderer() {

				@Override
				public Component getListCellRendererComponent(final JList list, final Object value, final int index,
						final boolean isSelected, final boolean cellHasFocus) {
					final Component p = super
							.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					if (p instanceof JLabel)
						((JLabel) p).setText(ASUtil.R("AtlasStyler.LineCap.Values." + value));
					return p;
				}
			});

			jComboBoxLineCap.setSelectedItem(preset);
			jComboBoxLineCap.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						final Object itemStringValue = e.getItem();
						symbolizer.getStroke().setLineCap(ASUtil.ff2.literal(itemStringValue));

						firePropertyChange(PROPERTY_UPDATED, null, null);
					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxLineCap);
		}
		return jComboBoxLineCap;
	}

	private JPanel getJPanelDashArray() {
		if (jPanelDashArray == null) {
			jPanelDashArray = new JPanel(new MigLayout("wrap 1", "[grow]"));
			jLabelDashOffset = new JLabel(ASUtil.R("LineSymbolEdit.DashedLine.DashOffset"));
			jLabelDashPattern = new JLabel(ASUtil.R("LineSymbolEdit.DashedLine.DashPattern"));
			jPanelDashArray.setBorder(BorderFactory.createTitledBorder(ASUtil.R("LineSymbolEdit.DashedLine.Title")));
			final JLabel lineExplanation = new JLabel(ASUtil.R("LineSymbolEditGUI.dashPattern_tooltip"));

			jPanelDashArray.add(lineExplanation, "grow x, width ::500");

			jPanelDashArray.add(jLabelDashPattern, "split 4");
			jPanelDashArray.add(getJTextFieldDashPattern(), "grow x, width ::200");
			jPanelDashArray.add(jLabelDashOffset, "gap unrel");
			jPanelDashArray.add(getJComboBoxDashOffset(), "");
		}
		return jPanelDashArray;
	}

	private JPanel getJPanelGraphicStroke() {
		if (jPanelGraphicStroke == null) {
			jPanelGraphicStroke = new JPanel(new MigLayout("wrap 2", "[r][l]"));
			jPanelGraphicStroke.setBorder(BorderFactory.createTitledBorder("External Graphic")); // i8n

			Graphic graphicStroke = symbolizer.getStroke().getGraphicStroke();

			if (graphicStroke == null) {
				backupStroke = graphicStroke = StylingUtil.STYLE_BUILDER.createGraphic();
			}

			jPanelGraphicStroke.add(getJButtonExtGraphic(GeometryForm.ANY, backupStroke));

			jPanelGraphicStroke.add(getJComboxBoxWidthExtGraphic());
			if (symbolizer.getStroke().getGraphicStroke() == null) {
				jPanelGraphicStroke.setEnabled(false);
			}

		}
		return jPanelGraphicStroke;
	}

	private JComboBox getJComboxBoxWidthExtGraphic() {
		if (jComboBoxWidthExtGraphic == null) {
			jComboBoxWidthExtGraphic = new JComboBox(new DefaultComboBoxModel(SPACE_AROUND_VALUES));

			if (jButtonExtGraphic != null) {
				Integer height = jButtonExtGraphic.getHeight();
				ASUtil.selectOrInsert(jComboBoxWidthExtGraphic, height);
			} else {
				// set default?
			}

			jComboBoxWidthExtGraphic.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						final Graphic graphicStroke = symbolizer.getStroke().getGraphicStroke();
						graphicStroke.setSize(ASUtil.ff2.literal(e.getItem()));
						firePropertyChange(PROPERTY_UPDATED, null, null);
					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxWidthExtGraphic);
		}
		return jComboBoxWidthExtGraphic;
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

				@Override
				public void keyPressed(final KeyEvent e) {
				}

				@Override
				public void keyReleased(final KeyEvent e) {
					if ((e.getKeyCode() == KeyEvent.VK_ENTER) || (e.getKeyCode() == KeyEvent.VK_TAB)) {
						updateDashFromtextfield();
					}
				}

				@Override
				public void keyTyped(final KeyEvent e) {
				}

			});

			jTextFieldDashPattern.addFocusListener(new FocusListener() {

				@Override
				public void focusGained(final FocusEvent e) {
				}

				@Override
				public void focusLost(final FocusEvent e) {
					updateDashFromtextfield();
				}

			});

			jTextFieldDashPattern.setToolTipText(AtlasStylerVector.R("LineSymbolEditGUI.dashPattern_tooltip"));
		}
		return jTextFieldDashPattern;
	}

	private void updateDashFromtextfield() {
		final String text = jTextFieldDashPattern.getText();
		if ((text == null) || (text.trim().equals(""))) {
			symbolizer.getStroke().setDashArray(null);
			firePropertyChange(PROPERTY_UPDATED, null, null);
			return;
		}

		final String[] strings = text.split(" ");
		final float[] dashArrays = new float[strings.length];
		int count = 0;
		for (final String s : strings) {
			try {
				final float f = Float.valueOf(s);
				dashArrays[count] = f;
				count++;
			} catch (final NumberFormatException e) {
				updateTextFieldDashPattern();
				JOptionPane.showMessageDialog(LineSymbolEditGUI.this,
						AtlasStylerVector.R("LineSymbolEditGUI.dashPattern_illegalDashPatternFormatMessage"));
				return;
			}
		}
		symbolizer.getStroke().setDashArray(dashArrays);
		firePropertyChange(PROPERTY_UPDATED, null, null);
	}

	private void updateTextFieldDashPattern() {
		final float[] dashArrays = symbolizer.getStroke().getDashArray();
		String text = "";
		if (dashArrays != null)
			for (final float f : dashArrays) {
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
			jComboBoxDashOffset = new JComboBox(new DefaultComboBoxModel(DISPLACEMENT_VALUES));

			jComboBoxDashOffset.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getStroke().setDashOffset(ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);

					}
				}

			});

			ASUtil.selectOrInsert(jComboBoxDashOffset, symbolizer.getStroke().getDashOffset());

			SwingUtil.addMouseWheelForCombobox(jComboBoxDashOffset);
		}
		return jComboBoxDashOffset;
	}

	private JComboBox getJComboBoxPerpendicularOffset() {
		if (jComboBoxPerpendicularOffset == null) {
			jComboBoxPerpendicularOffset = new JComboBox(new DefaultComboBoxModel(DISPLACEMENT_VALUES));

			if (symbolizer.getPerpendicularOffset() != null) {
				Expression offset = symbolizer.getPerpendicularOffset();
				ASUtil.selectOrInsert(jComboBoxPerpendicularOffset, offset);
			} else {
				// set default?
			}

			jComboBoxPerpendicularOffset.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(final ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.setPerpendicularOffset(ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);
					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxPerpendicularOffset);

			// disabled until supported by geotools
			jComboBoxPerpendicularOffset.setEnabled(false);
		}
		return jComboBoxPerpendicularOffset;
	}
}
