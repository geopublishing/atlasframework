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

import org.apache.log4j.Logger;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Stroke;

import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.sld.ASUtil;
import skrueger.sld.AtlasStyler;
import skrueger.swing.ColorButton;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class PolygonSymbolEditGUI extends AbstractEditGUI {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private org.geotools.styling.PolygonSymbolizer symbolizer;

	private JPanel jPanelFill = null;

	private JCheckBox jCheckBoxFill = null;

	private JCheckBox jCheckBoxStroke = null;

	private JPanel jPanelStroke = null;

	private JComboBox jComboBoxStrokeOpacity;

	private JComboBox jComboBoxStrokeWidth;

	private ColorButton jButtonStrokeColor;

	protected Color rememberStrokeColor;

	private JPanel jPanelFillGraphics = null;

	private JCheckBox jCheckBoxFillGraphic = null;

	private JButton jButtonFillGraphic = null;

	private JLabel jLabelFillGraphic = null;

	private JLabel jLabelFillGraphicOpacity = null;

	private JComboBox jComboBoxGraphicFillOpacity = null;

	/**
	 * Remembers GUI settings so that clicking is more fun..
	 */
	protected Fill rememberFill;

	private JComboBox jComboBoxFillOpacity;
	{
		rememberFill = ASUtil.createDefaultFill();
		rememberFill.setGraphicFill(ASUtil.createDefaultGraphicFill());
	}

	/**
	 * This is the default constructor
	 * 
	 * @param graphic
	 */
	public PolygonSymbolEditGUI(
			final org.geotools.styling.PolygonSymbolizer symbolizer) {
		this.symbolizer = symbolizer;
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
//		GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
//		gridBagConstraints51.gridx = 0;
//		gridBagConstraints51.anchor = GridBagConstraints.NORTHEAST;
//		gridBagConstraints51.insets = new Insets(1, 0, 0, 0);
//		gridBagConstraints51.gridy = 2;
//		GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
//		gridBagConstraints41.gridx = 1;
//		gridBagConstraints41.fill = GridBagConstraints.HORIZONTAL;
//		gridBagConstraints41.gridy = 2;
//		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
//		gridBagConstraints3.gridx = 1;
//		gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
//		gridBagConstraints3.gridy = 1;
//		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
//		gridBagConstraints2.gridx = 0;
//		gridBagConstraints2.anchor = GridBagConstraints.NORTHEAST;
//		gridBagConstraints2.gridy = 1;
//		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
//		gridBagConstraints1.gridx = 0;
//		gridBagConstraints1.anchor = GridBagConstraints.NORTHEAST;
//		gridBagConstraints1.insets = new Insets(1, 0, 0, 0);
//		gridBagConstraints1.gridy = 0;
//		GridBagConstraints gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 1;
//		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
//		gridBagConstraints.gridy = 0;
//		this.setSize(334, 280);
		this.setLayout( new MigLayout("wrap 2"));
		this.add(getJCheckBoxFill(),"top, gap r rel");
		this.add(getJPanelFill());

		this.add(getJCheckBoxStroke(),"top, gap r rel");
		this.add(getJPanelStroke());
		
		this.add(getJCheckBoxFillGraphic(),"top, gap r rel");
		this.add(getJPanelFillGraphics());
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelFill() {
		if (jPanelFill == null) {
			jPanelFill = new JPanel(new MigLayout(), AtlasStyler
					.R("GraphicEdit.Fill.Title"));
			jPanelFill.add(new JLabel(AtlasStyler.R("ColorLabel")));
			jPanelFill.add(getJButtonFillColor());
			jPanelFill.add(new JLabel(AtlasStyler.R("OpacityLabel")));
			jPanelFill.add(getJComboBoxFillOpacity());
		}
		return jPanelFill;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private ColorButton getJButtonFillColor() {
		final ColorButton jButtonFillColor = new ColorButton();

		jButtonFillColor.setAction(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				Color color = null;

				Fill fill = symbolizer.getFill();
				String substring = fill.getColor().toString();
				color = Color.decode(substring);

				// Color newColor = JColorChooser.showDialog(
				// PolygonSymbolEditGUI.this, "Choose fill color",
				// color); // i8nAC
				Color newColor = ASUtil.showColorChooser(
						PolygonSymbolEditGUI.this, AtlasStyler
								.R("Fill.ColorChooserDialog.Title"), color);

				if (newColor != null) {
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
			jButtonFillColor.setColor(f.getColor());
		} else {
			jPanelFill.setEnabled(false);
		}

		return jButtonFillColor;
	}

	/**
	 * This method initializes jComboBox1
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxFillOpacity() {
		if (jComboBoxFillOpacity == null) {
			jComboBoxFillOpacity = new JComboBox();

			jComboBoxFillOpacity.setModel(new DefaultComboBoxModel(
					OPACITY_VALUES));
			Fill ff = symbolizer.getFill();
			if (ff != null) {
				if (ff.getOpacity() == null)
					ff.setOpacity(ASUtil.ff2.literal(1f));
				
				ASUtil.selectOrInsert(jComboBoxFillOpacity, ff
						.getOpacity());
			}

			jComboBoxFillOpacity.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getFill().setOpacity(
								ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);

					}
				}

			});
			ASUtil.addMouseWheelForCombobox(jComboBoxFillOpacity);

		}
		return jComboBoxFillOpacity;
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

				public void actionPerformed(ActionEvent e) {

					boolean enabled = jCheckBoxFill.isSelected();
					if (enabled) {
						if (jCheckBoxFillGraphic.isSelected())
							jCheckBoxFillGraphic.doClick();

						Fill fill = ASUtil.createDefaultFill();
						fill.setColor(rememberFill.getColor());
						fill.setOpacity(rememberFill.getOpacity());
						fill.setGraphicFill(null);

						symbolizer.setFill(fill);

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
			jCheckBoxFill.setSelected(symbolizer.getFill() != null);
		}
		return jCheckBoxFill;
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
				public void actionPerformed(ActionEvent e) {
					boolean onOff = jCheckBoxStroke.isSelected();

					// TODO remember the Fill obejct!?
					if (onOff) {
						// If available, read the stored values from the GUI
						// elements
						Number opacity = (Number) jComboBoxStrokeOpacity
								.getSelectedItem();
						Number width = (Number) jComboBoxStrokeWidth
								.getSelectedItem();
						if (rememberStrokeColor == null) {
							rememberStrokeColor = Color.black;
						}
						if (opacity == null)
							opacity = 1f;
						if (width == null)
							width = 1f;
						org.geotools.styling.Stroke stroke = ASUtil.SB
								.createStroke(rememberStrokeColor, width
										.doubleValue(), opacity.doubleValue());

						symbolizer.setStroke(stroke);
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
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelStroke() {
		if (jPanelStroke == null) {
			jPanelStroke = new JPanel(new MigLayout("nogrid"),
					AtlasStyler.R("PolygonSymbolEdit.Stroke.Title"));

			JLabel jLabelStrokeOpacity = new JLabel(AtlasStyler
					.R("OpacityLabel"));

			JLabel jLabelStrokeWidth = new JLabel(AtlasStyler.R("WidthLabel"));

			jPanelStroke.add(new JLabel(AtlasStyler.R("ColorLabel")), "right");
			jPanelStroke.add(getJButtonStrokeColor(), "left");
			jPanelStroke.add(jLabelStrokeWidth, "right");
			jPanelStroke.add(getJComboBoxStrokeWidth(), "left");
			jPanelStroke.add(jLabelStrokeOpacity, "right");
			jPanelStroke.add(getJComboBoxStrokeOpacity(), "left");
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
			jButtonStrokeColor = new ColorButton();

			jButtonStrokeColor.setAction(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					Color color = null;

					String substring = symbolizer.getStroke().getColor()
							.toString();
					color = Color.decode(substring);

					Color newColor = ASUtil.showColorChooser(
							PolygonSymbolEditGUI.this, AtlasStyler
									.R("Stroke.ColorChooserDialog.Title"),
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
				jButtonStrokeColor.setColor(s.getColor());
			} else {
				jButtonStrokeColor.setEnabled(false);
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

			// TODO .. might not be in list...
			Stroke s = symbolizer.getStroke();
			if (s != null)
				if (s.getWidth() == null) {
					// Having a Stroke but no StrokeWidth doesn't make sense.
					s.setWidth(ASUtil.ff2.literal("1."));
				} else {

					ASUtil.selectOrInsert(jComboBoxStrokeWidth, s
							.getWidth());
				}
			else {
				jComboBoxStrokeWidth.setEnabled(false);
			}

			jComboBoxStrokeWidth.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getStroke().setWidth(
								ASUtil.ff2.literal(e.getItem()));

						PolygonSymbolEditGUI.this.firePropertyChange(
								PROPERTY_UPDATED, null, null);

					}
				}

			});

			ASUtil.addMouseWheelForCombobox(jComboBoxStrokeWidth);
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

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getStroke().setOpacity(
								ASUtil.ff2.literal(e.getItem()));

						PolygonSymbolEditGUI.this.firePropertyChange(
								PROPERTY_UPDATED, null, null);
					}
				}

			});

			ASUtil.addMouseWheelForCombobox(jComboBoxStrokeOpacity);
		}
		return jComboBoxStrokeOpacity;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelFillGraphics() {
		if (jPanelFillGraphics == null) {
			
			jLabelFillGraphicOpacity = new JLabel(AtlasStyler.R("OpacityLabel"));

			jLabelFillGraphic = new JLabel(AtlasStyler
					.R("ExternalGraphicLabel"));
			jPanelFillGraphics = new JPanel(new MigLayout(),AtlasStyler
					.R("PolygonSymbolEdit.Patternfill.Title"));
			jPanelFillGraphics.add(jLabelFillGraphic,"gap r rel");
			jPanelFillGraphics.add(getJButtonFillGraphic(),"gap r unrel");
			jPanelFillGraphics.add(jLabelFillGraphicOpacity,"gap r rel");
			jPanelFillGraphics.add(getJComboBoxFillGraphicOpacity());
		}
		return jPanelFillGraphics;
	}

	/**
	 * This method initializes jCheckBox
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxFillGraphic() {
		if (jCheckBoxFillGraphic == null) {
			jCheckBoxFillGraphic = new JCheckBox();

			jCheckBoxFillGraphic.addActionListener(new ActionListener() {

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
							.getFillImage(symbolizer.getFill(), FeatureUtil
									.createFeatureType(Point.class))));
					jLabelFillGraphic.setEnabled(enabled);
					jComboBoxGraphicFillOpacity.setEnabled(enabled);
					jLabelFillGraphicOpacity.setEnabled(enabled);

					PolygonSymbolEditGUI.this.firePropertyChange(
							PROPERTY_UPDATED, null, null);
				}

			});

			boolean initWith = false;
			if ((symbolizer.getFill() != null)
					&& (symbolizer.getFill().getGraphicFill() != null))
				initWith = true;
			jCheckBoxFillGraphic.setSelected(initWith);

		}
		return jCheckBoxFillGraphic;
	}

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

				public void actionPerformed(ActionEvent e) {

					if (symbolizer.getFill() == null) {
						symbolizer.setFill(StylingUtil.STYLE_BUILDER
								.createFill((Color) null, (Color) null, 1.,
										StylingUtil.STYLE_BUILDER
												.createGraphic()));
					}

					JDialog editFillGraphicJDialog = new GraphicEditGUIinDialog(
							SwingUtil
									.getParentWindow(PolygonSymbolEditGUI.this),
							symbolizer.getFill());

					editFillGraphicJDialog
							.addPropertyChangeListener(new PropertyChangeListener() {

								public void propertyChange(
										PropertyChangeEvent evt) {
									if (evt.getPropertyName().equals(
											AbstractEditGUI.PROPERTY_UPDATED)) {

										PolygonSymbolEditGUI.this
												.firePropertyChange(
														AbstractEditGUI.PROPERTY_UPDATED,
														null, null);

										// Update the Button Icon
										jButtonFillGraphic
												.setIcon(new ImageIcon(
														ASUtil
																.getSymbolizerImage(
																		symbolizer,
																		FeatureUtil
																				.createFeatureType(Polygon.class))));
									}
								}

							});
					
					SwingUtil.setRelativeFramePosition(editFillGraphicJDialog, PolygonSymbolEditGUI.this, SwingUtil.BOUNDS_OUTER, SwingUtil.NORTHEAST );

					editFillGraphicJDialog.setVisible(true);
				}

			});

			// Initialize correctly
			jLabelFillGraphic.setEnabled(enabled);
			jButtonFillGraphic.setEnabled(enabled);
			if (enabled) {
				jButtonFillGraphic.setIcon(new ImageIcon(ASUtil
						.getSymbolizerImage(symbolizer, FeatureUtil
								.createFeatureType(Polygon.class))));
			}

		}
		return jButtonFillGraphic;
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
			jComboBoxGraphicFillOpacity = new JComboBox();

			// Initialize correctly
			boolean enabled = false;
			if (symbolizer.getFill() != null) {
				if (symbolizer.getFill().getGraphicFill() != null)
					enabled = true;
			}
			jComboBoxGraphicFillOpacity.setEnabled(enabled);
			jLabelFillGraphicOpacity.setEnabled(enabled);

			// This {@link JComboBox} has the same Model as the
			// FillOpacityCOmboBox
			jComboBoxGraphicFillOpacity.setModel(getJComboBoxFillOpacity()
					.getModel());
			final Fill ff = symbolizer.getFill();
			if (ff != null) {
				ASUtil.selectOrInsert(jComboBoxGraphicFillOpacity, ff
						.getOpacity());
			}

			jComboBoxGraphicFillOpacity.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						symbolizer.getFill().setOpacity(
								ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);

					}
				}

			});
			ASUtil.addMouseWheelForCombobox(jComboBoxGraphicFillOpacity);

		}
		return jComboBoxGraphicFillOpacity;
	}

}
