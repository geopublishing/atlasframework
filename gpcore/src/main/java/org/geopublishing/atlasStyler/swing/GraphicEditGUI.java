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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geopublishing.atlasStyler.MARKTYPE;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Stroke;
import org.opengis.filter.expression.Expression;
import org.opengis.style.GraphicalSymbol;

import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.swing.ColorButton;
import de.schmitzm.swing.JPanel;
import de.schmitzm.swing.SwingUtil;

/**
 * A GUI to select/edait a SLD graphic symbol. It can often refers to a SVG.
 */
public class GraphicEditGUI extends AbstractStyleEditGUI {
	protected Logger LOGGER = LangUtil.createLogger(this);

	private final Graphic graphic;

	private JComboBox jComboBoxMarkType = null;

	private JPanel jPanelStroke = null;
	

	protected static final String PROPERTY_UPDATE_PREVIEWS = "UPDATE_PREVIEWS";

	private final JLabel jLabelStrokeColor = new JLabel();

	private ColorButton jButtonStrokeColor = null;

	private final JLabel jLabelStrokeWidth = new JLabel();

	private JComboBox jComboBoxStrokeWidth = null;

	private final JLabel jLabelStrokeOpacity = new JLabel();

	private JComboBox jComboBoxStrokeOpacity = null;

	private JPanel jPanel = null;

	private JLabel jLabelSize = null;

	private JComboBox jComboBoxGraphicSize = null;

	private JLabel jLabelOpacity = null;

	private JComboBox jComboBoxGraphicOpacity = null;

	protected boolean mark_mode = true;

	private JPanel jPanelFill = null;

	private JLabel jLabelFillColor = new JLabel();

	private ColorButton jButtonFillColor = null;

	private JLabel jLabelFillOpacity = new JLabel();

	private JComboBox jComboBoxFillOpacity = null;

	private final JLabel jLabelRotation = new JLabel(ASUtil.R("RotationLabel"));

	private JComboBox jComboBoxGraphicRotation = null;

	private JCheckBox jCheckBoxFill = null;

	private JCheckBox jCheckBoxStroke = null;

	Color rememberStrokeColor;

	Fill rememberFill;

	private JPanel jPanelExternalGraphic = null;

	/**
	 * When switching the {@link Graphic} to use a {@link Mark} we backup the
	 * old {@link ExternalGraphic}
	 **/
	protected ExternalGraphic backupExternalGraphic = null;

	/**
	 * When switching the {@link Graphic} to use an {@link ExternalGraphic} we
	 * backup the old {@link Mark}
	 **/
	protected Mark backupMark = null;

	private final GeometryForm geometryForm;

	/**
	 * This is the default constructor
	 * 
	 * @param graphic
	 * @param geomForm
	 *            Optionally defines how the graphic will be used, so that it
	 *            can make a good preview.
	 */
	public GraphicEditGUI(final AtlasStylerVector asv, final Graphic graphic_,
			GeometryForm geomForm) {
		super(asv);

		this.geometryForm = geomForm;

		/**
		 * Ensure that graphic is not <code>null</code>
		 */
		if (graphic_ != null) {
			this.graphic = graphic_;
		} else {
			this.graphic = StylingUtil.STYLE_BUILDER.createGraphic();
			firePropertyChange(PROPERTY_UPDATED, null, null);
		}

		/**
		 * Ensure that we have any graphic symbol
		 */
		/**
		 * Checking and ensuring that we have at least one graphical symbol
		 */
		final int countGraphicalSymbols = graphic.graphicalSymbols().size();
		if (countGraphicalSymbols == 0) {
			graphic.graphicalSymbols().add(
					StylingUtil.STYLE_BUILDER.createMark(MARKTYPE.square
							.toWellKnownName()));
		} else if (countGraphicalSymbols > 1) {
			// We are loosing graphical symbols :-(
			LOGGER.warn("The Graphic "
					+ graphic
					+ " contains more than one graphical symbols. Only the first one will be kept.");
			GraphicalSymbol firstGraphicalSymbol = graphic.graphicalSymbols()
					.get(0);
			graphic.graphicalSymbols().clear();
			graphic.graphicalSymbols().add(firstGraphicalSymbol);
		}

		initialize();
	}

	private void initialize() {

		setLayout(new MigLayout("wrap 2"));
		this.add(new JLabel(ASUtil.R("GraphicEdit.Type")), "split 2");
		this.add(getJComboBoxMarkType(), "wrap");
		this.add(getJPanelGraphic(), "span 2");

		this.setSize(310, 302); // TODO do we like that ?
	}

	/**
	 * This method initializes jComboBox The WellKnownName element gives the
	 * well-known name of the shape of the mark. Allowed values include at least
	 * “square”, “circle”, “triangle”, “star”, “cross”, and “x”, though map
	 * servers may draw a different symbol instead if they don't have a shape
	 * for all of these. The default WellKnownName is “square”. Renderings of
	 * these marks may be made solid or hollow depending on Fill and Stroke
	 * elements. These elements are discussed in Sections 11.2.2 and 11.1.3,
	 * respectively.
	 */
	private JComboBox getJComboBoxMarkType() {
		if (jComboBoxMarkType == null) {
			jComboBoxMarkType = new JComboBox(new DefaultComboBoxModel(
					MARKTYPE.values()));

			// This renderer will use the labels from the localization
			jComboBoxMarkType.setRenderer(new DefaultListCellRenderer() {
				@Override
				public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus) {

					JLabel fromSuper = (JLabel) super
							.getListCellRendererComponent(list, value, index,
									isSelected, cellHasFocus);

					fromSuper.setText(ASUtil.R("Marktyp." + value.toString()
							+ ".Label"));

					return fromSuper;
				}
			});

			GraphicalSymbol graphicalSymbol = graphic.graphicalSymbols().get(0);

			/**
			 * What kind of graphical Symbol is this? Set the markType
			 */
			if (graphicalSymbol instanceof Mark) {
				Mark mark = (Mark) graphicalSymbol;
				jComboBoxMarkType.getModel().setSelectedItem(
						MARKTYPE.readWellKnownName(mark.getWellKnownName()
								.toString()));
				mark_mode = true;
			} else if (graphicalSymbol instanceof ExternalGraphic) {
				mark_mode = false;

				// TODO select between internal and external graphic
				jComboBoxMarkType.getModel().setSelectedItem(
						MARKTYPE.external_graphic);
			} else {
				throw new IllegalArgumentException("graphic = " + graphic
						+ " is not recognized.");
			}

			updateToMarkMode();

			jComboBoxMarkType.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						Object item = e.getItem();

						if (item.equals(MARKTYPE.external_graphic)) {
							/** We are dealing with external graphics * */
							mark_mode = false;

							// Backup any Mark
							if (graphic.graphicalSymbols().get(0) instanceof Mark) {
								backupMark = (Mark) graphic.graphicalSymbols()
										.get(0);
							}
							graphic.graphicalSymbols().clear();

							// Import any backupped ExternalGraphic
							if (backupExternalGraphic != null) {
								graphic.graphicalSymbols().add(
										backupExternalGraphic);
							}

							updateToMarkMode();

						} else {

							// Some graphical symbol of type Mark has been
							// selected
							mark_mode = true;

							// Backup any ExternalGraphic
							if (graphic.graphicalSymbols().size() > 0
									&& graphic.graphicalSymbols().get(0) instanceof ExternalGraphic) {
								backupExternalGraphic = (ExternalGraphic) graphic
										.graphicalSymbols().get(0);
							}

							/**
							 * We are dealing with a Mark, throw away the
							 * external graphic *
							 */
							graphic.graphicalSymbols().clear();

							// Import any backupped Mark
							if (backupMark != null) {
								graphic.graphicalSymbols().add(backupMark);
							} else {
								/** Create a new Mark if needed * */
								graphic.graphicalSymbols().add(
										ASUtil.createDefaultMark());
							}

							// Adapt the new type of Mark
							Mark mark = (Mark) graphic.graphicalSymbols()
									.get(0);

							String markTypeName = ((MARKTYPE) item)
									.toWellKnownName();

							mark.setWellKnownName(ASUtil.ff2
									.literal(markTypeName));

							updateToMarkMode();
						}

						firePropertyChange(PROPERTY_UPDATED, null, item);
					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxMarkType);
		}
		return jComboBoxMarkType;
	}

	protected void updateToMarkMode() {
		// External Graphic
		getJPanelExternalGraphic().setEnabled(!mark_mode);

		// Fill Panel
		getJCheckBoxFill().setEnabled(mark_mode);
		if ((getJCheckBoxFill().isSelected())) {
			getJPanelFill().setEnabled(mark_mode);
		}

		// Stroke Panel
		getJCheckBoxStroke().setEnabled(mark_mode);
		if (getJCheckBoxStroke().isSelected()) {
			getJPanelStroke().setEnabled(mark_mode);
		}

		/**
		 * 
		 */
		if (mark_mode) {

			if (graphic.graphicalSymbols().size() > 0) {
				Mark someMark = (Mark) graphic.graphicalSymbols().get(0);

				Stroke s = someMark.getStroke();
				if (s != null) {
					jButtonStrokeColor.setColor(StylingUtil
							.getColorFromExpression(s.getColor()));
				} else {
					getJButtonStrokeColor().setEnabled(false);
					jLabelStrokeColor.setEnabled(false);
				}
			} else {
				getJButtonStrokeColor().setEnabled(false);
				jLabelStrokeColor.setEnabled(false);
			}
		}

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
			jLabelStrokeOpacity.setText(ASUtil.R("OpacityLabel"));
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
			jLabelStrokeWidth.setText(ASUtil.R("WidthLabel"));
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints5.gridy = 0;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.insets = new Insets(0, 5, 0, 0);
			gridBagConstraints4.gridy = 0;
			jLabelStrokeColor.setText(ASUtil.R("ColorLabel"));
			jPanelStroke = new JPanel();
			jPanelStroke.setLayout(new GridBagLayout());
			jPanelStroke.setBorder(BorderFactory.createTitledBorder(ASUtil
					.R("GraphicEdit.Stroke.Title")));
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

				@Override
				public void actionPerformed(ActionEvent e) {
					Color oldColor = null;

					if (graphic.graphicalSymbols().size() > 0) {
						Mark someMark = (Mark) graphic.graphicalSymbols()
								.get(0);

						if (someMark.getStroke() != null
								&& someMark.getStroke().getColor() != null) {
							oldColor = Color.decode(someMark.getStroke()
									.getColor().toString());
						}

						Color newColor = AVSwingUtil.showColorChooser(
								GraphicEditGUI.this,
								ASUtil.R("Stroke.ColorChooserDialog.Title"),
								oldColor);

						if (newColor != null && newColor != oldColor) {
							if (someMark.getStroke() == null) {
								someMark.setStroke(StylingUtil.STYLE_BUILDER
										.createStroke(newColor));
							} else {
								someMark.getStroke().setColor(
										StylingUtil.STYLE_BUILDER
												.colorExpression(newColor));
							}

							jButtonStrokeColor.setColor(newColor);

							GraphicEditGUI.this.firePropertyChange(
									PROPERTY_UPDATED, null, null);

						}
					}

				}

			});

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

			jComboBoxStrokeWidth.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						graphic.getMarks()[0].getStroke().setWidth(
								ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);

					}
				}

			});

			if (mark_mode) {
				// TODO .. might not be in list...
				Stroke s = graphic.getMarks()[0].getStroke();
				if (s != null)
					jComboBoxStrokeWidth.setSelectedItem(Float.valueOf(s
							.getWidth().toString()));
				else {
					jComboBoxStrokeWidth.setEnabled(false);
					jLabelStrokeWidth.setEnabled(false);
				}
			} else {
				jComboBoxStrokeWidth.setEnabled(false);
				jLabelStrokeWidth.setEnabled(false);
			}

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

			if ((mark_mode) && (graphic.getMarks()[0].getStroke() != null)) {
				jComboBoxStrokeOpacity.setSelectedItem(Float.valueOf(graphic
						.getMarks()[0].getStroke().getOpacity().toString()));
				// TODO ASUtil... hat da was?
			} else {
				jComboBoxStrokeOpacity.setEnabled(false);
				jLabelStrokeOpacity.setEnabled(false);
			}

			jComboBoxStrokeOpacity.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						graphic.getMarks()[0].getStroke().setOpacity(
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
	private JPanel getJPanelGraphic() {
		if (jPanel == null) {
			jPanel = new JPanel(new MigLayout("wrap 2"));

			jLabelOpacity = new JLabel(ASUtil.R("OpacityLabel"));
			jLabelSize = new JLabel(ASUtil.R("SizeLabel"));

			jPanel.setBorder(BorderFactory.createTitledBorder(ASUtil
					.R("GraphicEdit.Graphic.Title")));

			JPanel generalSettingsPanel = new JPanel();
			generalSettingsPanel.add(jLabelSize, "split 6");
			generalSettingsPanel.add(getJComboBoxGraphicSize());
			generalSettingsPanel.add(jLabelOpacity);
			generalSettingsPanel.add(getJComboBoxGraphicOpacity());
			generalSettingsPanel.add(jLabelRotation);
			generalSettingsPanel.add(getJComboBoxGraphicRotation());

			jPanel.add(new JLabel(), "right, sgx"); // Gap
			jPanel.add(generalSettingsPanel, "left, sgx2");

			jPanel.add(getJCheckBoxStroke(), "right, top,sgx");
			jPanel.add(getJPanelStroke(), "left, sgx2");

			jPanel.add(getJCheckBoxFill(), "right, top,sgx");
			jPanel.add(getJPanelFill(), "left, sgx2");
			//
			// jPanel.add(getJCheckBoxDisplacement(), "right, top, sgx");
			// jPanel.add(getJPanelDisplacement(), "left, sgx2");

			jPanel.add(new JLabel(), "right, sgx"); // Gap
			jPanel.add(getJPanelExternalGraphic(), "left, sgx2");
		}
		return jPanel;
	}

	/**
	 * This method initializes jComboBox1
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxGraphicSize() {
		if (jComboBoxGraphicSize == null) {
			jComboBoxGraphicSize = new JComboBox();
			jComboBoxGraphicSize
					.setModel(new DefaultComboBoxModel(SIZE_VALUES));

			Expression size = graphic.getSize();

			/** Setting a default if null or ugly Expression.NIL */
			if (size == null || size == Expression.NIL) {
				graphic.setSize(size = ASUtil.ff2.literal("13"));
				firePropertyChange(PROPERTY_UPDATED, null, null);
			}

			jComboBoxGraphicSize.getModel().setSelectedItem(
					Float.valueOf(size.toString()));

			jComboBoxGraphicSize.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						graphic.setSize(ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);
					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxGraphicSize);
		}
		return jComboBoxGraphicSize;
	}

	/**
	 * This method initializes jComboBox1
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxGraphicOpacity() {
		if (jComboBoxGraphicOpacity == null) {
			jComboBoxGraphicOpacity = new JComboBox();
			jComboBoxGraphicOpacity.setModel(new DefaultComboBoxModel(
					OPACITY_VALUES));

			Expression opacity = graphic.getOpacity();
			if (opacity == null) {
				graphic.setOpacity(opacity = ASUtil.ff2.literal(1.));
				firePropertyChange(PROPERTY_UPDATED, null, null);
			}

			// TODO Check.. the value might acutally not be included
			jComboBoxGraphicOpacity.setSelectedItem(Float.valueOf(opacity
					.toString()));

			jComboBoxGraphicOpacity.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						graphic.setOpacity(ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);
					}

				}
			});
			SwingUtil.addMouseWheelForCombobox(jComboBoxGraphicOpacity);
		}
		return jComboBoxGraphicOpacity;
	}

	/**
	 * This method initializes jPanel1
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelFill() {
		if (jPanelFill == null) {
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.fill = GridBagConstraints.NONE;
			gridBagConstraints18.gridy = 0;
			gridBagConstraints18.weightx = 1.0;
			gridBagConstraints18.anchor = GridBagConstraints.WEST;
			gridBagConstraints18.insets = new Insets(0, 5, 0, 5);
			gridBagConstraints18.gridx = 4;
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 3;
			gridBagConstraints17.insets = new Insets(0, 15, 0, 0);
			gridBagConstraints17.anchor = GridBagConstraints.EAST;
			gridBagConstraints17.gridy = 0;
			jLabelFillOpacity = new JLabel();
			jLabelFillOpacity.setText(AtlasStylerVector.R("OpacityLabel"));
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.fill = GridBagConstraints.NONE;
			gridBagConstraints16.gridy = 0;
			gridBagConstraints16.weightx = 1.0;
			gridBagConstraints16.anchor = GridBagConstraints.WEST;
			gridBagConstraints16.insets = new Insets(5, 5, 5, 0);
			gridBagConstraints16.gridx = 1;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.gridx = 0;
			gridBagConstraints15.insets = new Insets(5, 5, 5, 0);
			gridBagConstraints15.gridy = 0;
			jLabelFillColor = new JLabel();
			jLabelFillColor.setText(AtlasStylerVector.R("ColorLabel"));
			jPanelFill = new JPanel();
			jPanelFill.setLayout(new GridBagLayout());
			jPanelFill.setBorder(BorderFactory
					.createTitledBorder(AtlasStylerVector
							.R("GraphicEdit.Fill.Title")));
			jPanelFill.add(jLabelFillColor, gridBagConstraints15);
			jPanelFill.add(getJButtonFillColor(), gridBagConstraints16);
			jPanelFill.add(jLabelFillOpacity, gridBagConstraints17);
			jPanelFill.add(getJComboBoxFillOpacity(), gridBagConstraints18);
		}
		return jPanelFill;
	}

	/**
	 */
	private JButton getJButtonFillColor() {
		if (jButtonFillColor == null) {
			jButtonFillColor = new ColorButton(new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					Color color = null;

					String substring = graphic.getMarks()[0].getFill()
							.getColor().toString();
					color = Color.decode(substring);

					Color newColor = AVSwingUtil.showColorChooser(
							GraphicEditGUI.this,
							ASUtil.R("Fill.ColorChooserDialog.Title"), color);

					if (newColor != null) {
						Expression colorExpression = StylingUtil.STYLE_BUILDER
								.colorExpression(newColor);
						graphic.getMarks()[0].getFill().setColor(
								colorExpression);

						GraphicEditGUI.this.firePropertyChange(
								PROPERTY_UPDATED, null, null);

						jButtonFillColor.setColor(newColor);

					}

				}

			});

			if (mark_mode) {
				Fill f = graphic.getMarks()[0].getFill();
				if (f != null) {
					jButtonFillColor.setColor(StylingUtil
							.getColorFromExpression(f.getColor()));
				} else {
					jButtonFillColor.setEnabled(false);
					jLabelFillColor.setEnabled(false);
				}
			} else {
				jButtonFillColor.setEnabled(false);
				jLabelFillColor.setEnabled(false);
			}

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

			if (mark_mode) {
				Fill ff = graphic.getMarks()[0].getFill();
				if (ff != null) {
					jComboBoxFillOpacity.setSelectedItem(Float.valueOf(ff
							.getOpacity().toString()));
				}
			} else {
				jComboBoxFillOpacity.setEnabled(false);
			}

			jComboBoxFillOpacity.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						graphic.getMarks()[0].getFill().setOpacity(
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
	private JComboBox getJComboBoxGraphicRotation() {
		if (jComboBoxGraphicRotation == null) {
			jComboBoxGraphicRotation = new JComboBox(new DefaultComboBoxModel(
					ROTATION_VALUES));
			jComboBoxGraphicRotation.setRenderer(ROTATION_VALUES_RENDERER);

			jComboBoxGraphicRotation.setSelectedItem(Double.valueOf(graphic
					.getRotation().toString()));
			jComboBoxGraphicRotation.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						graphic.setRotation(ASUtil.ff2.literal(e.getItem()));

						firePropertyChange(PROPERTY_UPDATED, null, null);
					}

				}
			});
			SwingUtil.addMouseWheelForCombobox(jComboBoxGraphicRotation);

		}
		return jComboBoxGraphicRotation;
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

						final Fill fill;
						if (rememberFill != null) {
							fill = rememberFill;
							fill.setColor(rememberFill.getColor());
						} else {
							fill = ASUtil.createDefaultFill();
						}
						graphic.getMarks()[0].setFill(fill);
					} else {
						Fill fill = graphic.getMarks()[0].getFill();
						if (fill != null) {
							rememberFill = fill;
						}
						graphic.getMarks()[0].setFill(null);
					}

					getJPanelFill().setEnabled(enabled);
					jComboBoxFillOpacity.setEnabled(enabled);
					jButtonFillColor.setEnabled(enabled);
					jLabelFillColor.setEnabled(enabled);
					jLabelFillOpacity.setEnabled(enabled);

					// System.out.println("setting fillpanel to "
					// + getJPanelFill().isEnabled());

					firePropertyChange(PROPERTY_UPDATED, null, null);
				}

			});
			jCheckBoxFill.setSelected(mark_mode ? graphic.getMarks()[0]
					.getFill() != null : false);
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
				@Override
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
						Stroke stroke = ASUtil.SB.createStroke(
								rememberStrokeColor, width.doubleValue(),
								opacity.doubleValue());

						graphic.getMarks()[0].setStroke(stroke);
					} else {
						if (graphic.getMarks()[0].getStroke() != null) {
							rememberStrokeColor = StylingUtil
									.getColorFromExpression(graphic.getMarks()[0]
											.getStroke().getColor());
						}

						graphic.getMarks()[0].setStroke(null);
					}

					getJPanelStroke().setEnabled(onOff);
					jComboBoxStrokeOpacity.setEnabled(onOff);
					jButtonStrokeColor.setEnabled(onOff);
					jComboBoxStrokeWidth.setEnabled(onOff);
					jLabelStrokeColor.setEnabled(onOff);
					jLabelStrokeOpacity.setEnabled(onOff);
					jLabelStrokeWidth.setEnabled(onOff);

					firePropertyChange(PROPERTY_UPDATED, null, null);
				}

			});
			jCheckBoxStroke.setSelected(mark_mode ? graphic.getMarks()[0]
					.getStroke() != null : false);
		}
		return jCheckBoxStroke;
	}

	/**
	 * This method initializes the Panel which allows to define an
	 * ExternalGraphic.
	 */
	private JPanel getJPanelExternalGraphic() {
		if (jPanelExternalGraphic == null) {
			jPanelExternalGraphic = new JPanel();
			jPanelExternalGraphic.setLayout(new MigLayout());
			jPanelExternalGraphic.setBorder(BorderFactory
					.createTitledBorder(ASUtil
							.R("GraphicEdit.ExternalGraphic.Title")));

			// The first button is for images like SVG etc.
			// jPanelExternalGraphic.add(
			// new JLabel(ASUtil.R("ExternalGraphicLabel")), "");
			jButtonExtGraphicPreview = new JLabel();
			jButtonExtGraphicPreview.setSize(EXT_GRAPHIC_BUTTON_WIDTH,
					EXT_GRAPHIC_BUTTON_HEIGHT);

			GraphicEditGUI.this.addPropertyChangeListener(listenerUpdatePreviewExtChartGraphic);
			updateExternalGraphicButton(jButtonExtGraphicPreview, graphic);
			jPanelExternalGraphic.add(jButtonExtGraphicPreview, "");

			jPanelExternalGraphic.add(
					getJButtonExtGraphic(geometryForm, graphic), "");

			// An external graphic may also be a chart provided by eastwood /
			// gt-charts
			// jPanelExternalGraphic.add(
			// new JLabel(ASUtil.R("ChartGraphicLabel")), "gap 70");
			jPanelExternalGraphic.add(
					getJButtonChartGraphic(geometryForm, graphic), "");
		}
		return jPanelExternalGraphic;
	}
	
	final PropertyChangeListener listenerUpdatePreviewExtChartGraphic = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {

			if (evt.getPropertyName().equals(PROPERTY_UPDATE_PREVIEWS)) {
				updateExternalGraphicButton(jButtonExtGraphicPreview, graphic);
			}
		}

	};

}
