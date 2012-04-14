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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Stroke;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.ColorButton;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SwingUtil;

public class PolygonSymbolEditGUI extends AbstractStyleEditGUI {
	private JButton jButtonFillGraphic = null;

	private ColorButton jButtonStrokeColor;

	private JCheckBox jCheckBoxFill = null;

	private JCheckBox jCheckBoxFillGraphic = null;

	private JCheckBox jCheckBoxStroke = null;

	private OpacityJComboBox jComboBoxFillOpacity;

	private OpacityJComboBox jComboBoxGraphicFillOpacity = null;

	private OpacityJComboBox jComboBoxStrokeOpacity;

	private JComboBox jComboBoxStrokeWidth;

	private final JLabel jLabelFillGraphic = new JLabel(
			AtlasStylerVector.R("ExternalGraphicLabel"));

	private final JLabel jLabelGraphicFillOpacity = new JLabel(
			AtlasStylerVector.R("OpacityLabel"));

	private final JLabel jLabelFillOpacity = new JLabel(
			AtlasStylerVector.R("OpacityLabel"));

	private JPanel jPanelFill = null;

	private JPanel jPanelFillGraphics = null;

	private JPanel jPanelStroke = null;

	protected Logger LOGGER = LangUtil.createLogger(this);

	/**
	 * Remembers GUI settings so that clicking is more fun..
	 */
	protected Fill rememberFill;

	protected Color rememberStrokeColor;

	private final org.geotools.styling.PolygonSymbolizer symbolizer;

	private ColorButton jButtonFillColor;
	{
		rememberFill = ASUtil.createDefaultFill();
		rememberFill.setGraphicFill(ASUtil.createDefaultGraphicFill());
	}

	/**
	 * This is the default constructor
	 * 
	 * @param graphic
	 */
	public PolygonSymbolEditGUI(final AtlasStylerVector asv, 
			final org.geotools.styling.PolygonSymbolizer symbolizer) {
		super(asv);
		this.symbolizer = symbolizer;
		initialize();
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private ColorButton getJButtonFillColor() {
		if (jButtonFillColor == null) {
			jButtonFillColor = new ColorButton();

			jButtonFillColor.setAction(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {

					Fill fill = symbolizer.getFill();

					Color color = null;
					if (fill != null && fill.getColor() != null) {
						String substring = fill.getColor().toString();
						color = Color.decode(substring);
					}
					// Color newColor = JColorChooser.showDialog(
					// PolygonSymbolEditGUI.this, "Choose fill color",
					// color); // i8nAC
					Color newColor = AVSwingUtil.showColorChooser(
							PolygonSymbolEditGUI.this,
							AtlasStylerVector.R("Fill.ColorChooserDialog.Title"),
							color);

					if (newColor != null && newColor != color) {
						String rgb = Integer.toHexString(newColor.getRGB());
						rgb = "#" + rgb.substring(2, rgb.length());
						fill.setColor(ASUtil.ff2.literal(rgb));

						PolygonSymbolEditGUI.this.firePropertyChange(
								PROPERTY_UPDATED, null, null);

						jButtonFillColor.setColor(newColor);

					}

				}

			});

			Fill f = symbolizer.getFill();
			if (f != null) {
				jButtonFillColor.setColor(StylingUtil.getColorFromExpression(f.getColor()));
			} else {
				jButtonFillColor.setColor((Color) null);
				jPanelFill.setEnabled(false);
			}
		}
		return jButtonFillColor;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonFillGraphic() {
		if (jButtonFillGraphic == null) {
			jButtonFillGraphic = new JButton();

			boolean enabled = false;
			if (symbolizer.getFill() != null) {
				Graphic graphicFill = symbolizer.getFill().getGraphicFill();
				enabled = (graphicFill != null);
			}

			jButtonFillGraphic.setAction(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (symbolizer.getFill() == null) {
						symbolizer.setFill(StylingUtil.STYLE_BUILDER
								.createFill((Color) null, (Color) null, 1.,
										StylingUtil.STYLE_BUILDER
												.createGraphic()));
					}

					JDialog editFillGraphicJDialog = new GraphicEditGUIinDialog(asv,
							SwingUtil
									.getParentWindow(PolygonSymbolEditGUI.this),
							symbolizer.getFill());

					editFillGraphicJDialog
							.addPropertyChangeListener(new PropertyChangeListener() {

								@Override
								public void propertyChange(
										PropertyChangeEvent evt) {
									if (evt.getPropertyName().equals(
											AbstractStyleEditGUI.PROPERTY_UPDATED)) {

										PolygonSymbolEditGUI.this
												.firePropertyChange(
														AbstractStyleEditGUI.PROPERTY_UPDATED,
														null, null);

										// Update the Button Icon
										jButtonFillGraphic.setIcon(new ImageIcon(
												ASUtil.getSymbolizerImage(
														symbolizer,
														FeatureUtil
																.createFeatureType(Polygon.class))));
									}
								}

							});

					SwingUtil.setRelativeFramePosition(editFillGraphicJDialog,
							PolygonSymbolEditGUI.this, SwingUtil.BOUNDS_OUTER,
							SwingUtil.NORTHEAST);

					editFillGraphicJDialog.setVisible(true);
				}

			});

			// Initialize correctly
			jLabelFillGraphic.setEnabled(enabled);
			jButtonFillGraphic.setEnabled(enabled);
			if (enabled) {
				jButtonFillGraphic.setIcon(new ImageIcon(ASUtil
						.getSymbolizerImage(symbolizer,
								FeatureUtil.createFeatureType(Polygon.class))));
			}

		}
		return jButtonFillGraphic;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private ColorButton getJButtonStrokeColor() {
		if (jButtonStrokeColor == null) {
			jButtonStrokeColor = new ColorButton();

			jButtonStrokeColor.setAction(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {

					Fill fill = symbolizer.getFill();

					Color color = null;
					if (fill != null && fill.getColor() != null) {
						String substring = fill.getColor().toString();
						color = Color.decode(substring);
					}

					Color newColor = AVSwingUtil.showColorChooser(
							PolygonSymbolEditGUI.this,
							AtlasStylerVector.R("Stroke.ColorChooserDialog.Title"),
							color);

					if (newColor != null) {
						String rgb = Integer.toHexString(newColor.getRGB());
						rgb = "#" + rgb.substring(2, rgb.length());
						symbolizer.getStroke()
								.setColor(ASUtil.ff2.literal(rgb));

						PolygonSymbolEditGUI.this.firePropertyChange(
								PROPERTY_UPDATED, null, null);

						jButtonStrokeColor.setColor(newColor);

					}

				}

			});

			Stroke s = symbolizer.getStroke();
			if (s != null) {
				jButtonStrokeColor.setColor(StylingUtil.getColorFromExpression(s.getColor()));
			} else {
				jButtonStrokeColor.setEnabled(false);
			}

		}
		return jButtonStrokeColor;
	}

	/**
	 * This method initializes jCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxFill() {
		if (jCheckBoxFill == null) {
			jCheckBoxFill = new JCheckBox();

			jCheckBoxFill.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					boolean enabled = jCheckBoxFill.isSelected();
					if (enabled) {
						if (jCheckBoxFillGraphic.isSelected())
							jCheckBoxFillGraphic.doClick();

						Fill fill = ASUtil.createDefaultFill();
						if (rememberFill.getColor() == null) {
							rememberFill.setColor(StylingUtil.STYLE_BUILDER
									.colorExpression(Color.GREEN));
						}
						fill.setColor(rememberFill.getColor());
						fill.setOpacity(rememberFill.getOpacity());
						fill.setGraphicFill(null);

						symbolizer.setFill(fill);
						getJButtonFillColor().setColor(StylingUtil.getColorFromExpression(fill.getColor()));
						getJComboBoxFillOpacity()
								.getModel()
								.setSelectedItem(
										NumberUtils.createFloat(fill
												.getOpacity() == null ? null
												: fill.getOpacity().toString()));

					} else {
						if (symbolizer.getFill() != null) {
							rememberFill.setColor(symbolizer.getFill()
									.getColor());
							rememberFill.setOpacity(symbolizer.getFill()
									.getOpacity());

							symbolizer.setFill(null);
						}
					}

					getJPanelFill().setEnabled(enabled);

					firePropertyChange(PROPERTY_UPDATED, null, null);
				}

			});
			jCheckBoxFill.setSelected(symbolizer.getFill() != null
					&& symbolizer.getFill().getGraphicFill() == null);
		}
		return jCheckBoxFill;
	}

	/**
	 * This method initializes jCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	JCheckBox getJCheckBoxFillGraphic() {
		if (jCheckBoxFillGraphic == null) {
			jCheckBoxFillGraphic = new JCheckBox();

			jCheckBoxFillGraphic.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					boolean enabled = jCheckBoxFillGraphic.isSelected();

					if (enabled) {
						if (jCheckBoxFill.isSelected())
							jCheckBoxFill.doClick();

						Fill fill = ASUtil.createDefaultFill();
						fill.setColor(null);
						fill.setOpacity(rememberFill.getOpacity());
						fill.setGraphicFill(rememberFill.getGraphicFill());

						symbolizer.setFill(fill);

						getJComboBoxFillGraphicOpacity()
								.getModel()
								.setSelectedItem(
										NumberUtils.createFloat(fill
												.getOpacity() == null ? null
												: fill.getOpacity().toString()));
					} else {

						if (symbolizer.getFill() != null) {
							rememberFill.setGraphicFill(symbolizer.getFill()
									.getGraphicFill());
							rememberFill.setOpacity(symbolizer.getFill()
									.getOpacity());
							symbolizer.setFill(null);
						}

					}

					getJPanelFillGraphics().setEnabled(enabled);
					jButtonFillGraphic.setEnabled(enabled);
					jButtonFillGraphic.setIcon(new ImageIcon(ASUtil
							.getFillImage(symbolizer.getFill(),
									FeatureUtil.createFeatureType(Point.class))));
					jLabelFillGraphic.setEnabled(enabled);
					jComboBoxGraphicFillOpacity.setEnabled(enabled);
					jLabelGraphicFillOpacity.setEnabled(enabled);

					PolygonSymbolEditGUI.this.firePropertyChange(
							PROPERTY_UPDATED, null, null);
				}

			});

			jCheckBoxFillGraphic
					.setSelected((symbolizer.getFill() != null && symbolizer
							.getFill().getGraphicFill() != null));

		}
		return jCheckBoxFillGraphic;
	}

	/**
	 * This method initializes jCheckBoxStroke
	 * 
	 * @return javax.swing.JCheckBoxStroke
	 */
	private JCheckBox getJCheckBoxStroke() {
		if (jCheckBoxStroke == null) {
			jCheckBoxStroke = new JCheckBox();

			jCheckBoxStroke.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean onOff = jCheckBoxStroke.isSelected();

					// TODO remember the Fill obejct!? -- ???? ?? probably not..
					if (onOff) {
						// If available, read the stored values from the GUI
						// elements
						Number opacity = (Number) jComboBoxStrokeOpacity
								.getSelectedItem();
						Number width = (Number) jComboBoxStrokeWidth
								.getSelectedItem();
						if (rememberStrokeColor == null) {
							rememberStrokeColor = Color.black;
							// Update the color of the button in case
							getJButtonStrokeColor().setColor(
									rememberStrokeColor);
						}
						if (opacity == null)
							opacity = 1f;
						if (width == null)
							width = 1f;
						Stroke stroke = ASUtil.SB.createStroke(
								rememberStrokeColor, width.doubleValue(),
								opacity.doubleValue());

						symbolizer.setStroke(stroke);
						getJComboBoxStrokeOpacity().getModel()
								.setSelectedItem(
										NumberUtils.createFloat(stroke
												.getOpacity() == null ? null
												: stroke.getOpacity()
														.toString()));
					} else {
						if (symbolizer.getStroke() != null) {
							rememberStrokeColor = StylingUtil
									.getColorFromExpression(symbolizer
											.getStroke().getColor());
						}

						symbolizer.setStroke(null);
					}

					getJPanelStroke().setEnabled(onOff);

					firePropertyChange(PROPERTY_UPDATED, null, null);
				}

			});

			jCheckBoxStroke.setSelected(symbolizer.getStroke() != null);
		}
		return jCheckBoxStroke;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * This {@link JComboBox} has the same Model as the FillOpacity
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxFillGraphicOpacity() {
		if (jComboBoxGraphicFillOpacity == null) {
			jComboBoxGraphicFillOpacity = new OpacityJComboBox();

			// Initialize correctly
			boolean enabled = (symbolizer.getFill() != null && symbolizer
					.getFill().getGraphicFill() != null);
			jComboBoxGraphicFillOpacity.setEnabled(enabled);
			jLabelGraphicFillOpacity.setEnabled(enabled);

			// This {@link JComboBox} has the same Model as the
			// FillOpacityCOmboBox
			jComboBoxGraphicFillOpacity.setModel(getJComboBoxFillOpacity()
					.getModel());
			final Fill ff = symbolizer.getFill();
			if (ff != null) {
				ASUtil.selectOrInsert(jComboBoxGraphicFillOpacity,
						ff.getOpacity());
			}

			jComboBoxGraphicFillOpacity.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getFill().setOpacity(
								ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);

					}
				}

			});
			SwingUtil.addMouseWheelForCombobox(jComboBoxGraphicFillOpacity);

		}
		return jComboBoxGraphicFillOpacity;
	}

	/**
	 * This method initializes jComboBox1
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxFillOpacity() {
		if (jComboBoxFillOpacity == null) {
			jComboBoxFillOpacity = new OpacityJComboBox();

			jComboBoxFillOpacity.setModel(new DefaultComboBoxModel(
					OPACITY_VALUES));

			Fill ff = symbolizer.getFill();
			if (ff != null) {
				if (ff.getOpacity() == null)
					ff.setOpacity(ASUtil.ff2.literal(1f));

				ASUtil.selectOrInsert(jComboBoxFillOpacity, ff.getOpacity());
			}

			// Initialize the enabled state
			boolean enabled = (symbolizer.getFill() != null && symbolizer
					.getFill().getGraphicFill() != null);
			jComboBoxFillOpacity.setEnabled(enabled);
			jLabelFillOpacity.setEnabled(enabled);

			jComboBoxFillOpacity.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getFill().setOpacity(
								ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);

					}
				}

			});
			SwingUtil.addMouseWheelForCombobox(jComboBoxFillOpacity);

		}
		return jComboBoxFillOpacity;
	}

	/**
	 * This method initializes jComboBox1
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxStrokeOpacity() {
		if (jComboBoxStrokeOpacity == null) {
			jComboBoxStrokeOpacity = new OpacityJComboBox();
			jComboBoxStrokeOpacity.setModel(new DefaultComboBoxModel(
					OPACITY_VALUES));

			if (symbolizer.getStroke() != null) {
				// We have a stroke, so now we need opacity

				if (symbolizer.getStroke().getOpacity() == null) {
					symbolizer.getStroke().setOpacity(ASUtil.ff2.literal("1."));
				}

				ASUtil.selectOrInsert(jComboBoxStrokeOpacity, symbolizer
						.getStroke().getOpacity());
			}

			else {
				jComboBoxStrokeOpacity.setEnabled(false);
			}

			jComboBoxStrokeOpacity.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getStroke().setOpacity(
								ASUtil.ff2.literal(e.getItem()));

						PolygonSymbolEditGUI.this.firePropertyChange(
								PROPERTY_UPDATED, null, null);
					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxStrokeOpacity);
		}
		return jComboBoxStrokeOpacity;
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

			// TODO .. might not be in list...
			Stroke s = symbolizer.getStroke();
			if (s != null)
				if (s.getWidth() == null) {
					// Having a Stroke but no StrokeWidth doesn't make sense.
					s.setWidth(ASUtil.ff2.literal("1."));
				} else {

					ASUtil.selectOrInsert(jComboBoxStrokeWidth, s.getWidth());
				}
			else {
				jComboBoxStrokeWidth.setEnabled(false);
			}

			jComboBoxStrokeWidth.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getStroke().setWidth(
								ASUtil.ff2.literal(e.getItem()));

						PolygonSymbolEditGUI.this.firePropertyChange(
								PROPERTY_UPDATED, null, null);

					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxStrokeWidth);
		}
		return jComboBoxStrokeWidth;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelFill() {
		if (jPanelFill == null) {
			jPanelFill = new JPanel(new MigLayout(),
					AtlasStylerVector.R("GraphicEdit.Fill.Title"));
			jPanelFill.add(new JLabel(AtlasStylerVector.R("ColorLabel")));
			jPanelFill.add(getJButtonFillColor());
			jPanelFill.add(jLabelFillOpacity);
			jPanelFill.add(getJComboBoxFillOpacity());
		}
		return jPanelFill;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelFillGraphics() {
		if (jPanelFillGraphics == null) {
			jPanelFillGraphics = new JPanel(new MigLayout(),
					AtlasStylerVector.R("PolygonSymbolEdit.Patternfill.Title"));
			jPanelFillGraphics.add(jLabelFillGraphic, "gap r rel");
			jPanelFillGraphics.add(getJButtonFillGraphic(), "gap r unrel");
			jPanelFillGraphics.add(jLabelGraphicFillOpacity, "gap r rel");
			jPanelFillGraphics.add(getJComboBoxFillGraphicOpacity());
		}
		return jPanelFillGraphics;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelStroke() {
		if (jPanelStroke == null) {
			jPanelStroke = new JPanel(new MigLayout("nogrid"),
					AtlasStylerVector.R("PolygonSymbolEdit.Stroke.Title"));

			JLabel jLabelStrokeOpacity = new JLabel(
					AtlasStylerVector.R("OpacityLabel"));

			JLabel jLabelStrokeWidth = new JLabel(AtlasStylerVector.R("WidthLabel"));

			jPanelStroke.add(new JLabel(AtlasStylerVector.R("ColorLabel")), "right");
			jPanelStroke.add(getJButtonStrokeColor(), "left");
			jPanelStroke.add(jLabelStrokeWidth, "right");
			jPanelStroke.add(getJComboBoxStrokeWidth(), "left");
			jPanelStroke.add(jLabelStrokeOpacity, "right");
			jPanelStroke.add(getJComboBoxStrokeOpacity(), "left");
		}
		return jPanelStroke;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		// GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
		// gridBagConstraints51.gridx = 0;
		// gridBagConstraints51.anchor = GridBagConstraints.NORTHEAST;
		// gridBagConstraints51.insets = new Insets(1, 0, 0, 0);
		// gridBagConstraints51.gridy = 2;
		// GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
		// gridBagConstraints41.gridx = 1;
		// gridBagConstraints41.fill = GridBagConstraints.HORIZONTAL;
		// gridBagConstraints41.gridy = 2;
		// GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		// gridBagConstraints3.gridx = 1;
		// gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
		// gridBagConstraints3.gridy = 1;
		// GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		// gridBagConstraints2.gridx = 0;
		// gridBagConstraints2.anchor = GridBagConstraints.NORTHEAST;
		// gridBagConstraints2.gridy = 1;
		// GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		// gridBagConstraints1.gridx = 0;
		// gridBagConstraints1.anchor = GridBagConstraints.NORTHEAST;
		// gridBagConstraints1.insets = new Insets(1, 0, 0, 0);
		// gridBagConstraints1.gridy = 0;
		// GridBagConstraints gridBagConstraints = new GridBagConstraints();
		// gridBagConstraints.gridx = 1;
		// gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		// gridBagConstraints.gridy = 0;
		// this.setSize(334, 280);
		this.setLayout(new MigLayout("wrap 2"));
		this.add(getJCheckBoxFill(), "top, gap r rel");
		this.add(getJPanelFill());

		this.add(getJCheckBoxStroke(), "top, gap r rel");
		this.add(getJPanelStroke());

		this.add(getJCheckBoxFillGraphic(), "top, gap r rel");
		this.add(getJPanelFillGraphics());
	}

}
