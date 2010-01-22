///*******************************************************************************
// * Copyright (c) 2009 Stefan A. Krüger.
// * 
// * This file is part of the AtlasStyler SLD/SE Editor application - A Java Swing-based GUI to create OGC Styled Layer Descriptor (SLD 1.0) / OGC Symbology Encoding 1.1 (SE) XML files.
// * http://www.geopublishing.org
// * 
// * AtlasStyler SLD/SE Editor is part of the Geopublishing Framework hosted at:
// * http://wald.intevation.org/projects/atlas-framework/
// * 
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public License
// * as published by the Free Software Foundation; either version 3
// * of the License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// * 
// * You should have received a copy of the GNU Lesser General Public License (license.txt)
// * along with this program; if not, write to the Free Software
// * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
// * or try this link: http://www.gnu.org/licenses/lgpl.html
// * 
// * Contributors:
// *     Stefan A. Krüger - initial API and implementation
// ******************************************************************************/
//package skrueger.sld.gui;
//
//import java.awt.Color;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Insets;
//import java.awt.Window;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.ItemEvent;
//import java.awt.event.ItemListener;
//import java.awt.image.BufferedImage;
//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;
//
//import javax.swing.AbstractAction;
//import javax.swing.BorderFactory;
//import javax.swing.DefaultComboBoxModel;
//import javax.swing.ImageIcon;
//import javax.swing.JButton;
//import javax.swing.JCheckBox;
//import javax.swing.JComboBox;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//
//import org.apache.log4j.Logger;
//import org.geotools.renderer.lite.SVGGlyphRenderer;
//import org.geotools.styling.Displacement;
//import org.geotools.styling.ExternalGraphic;
//import org.geotools.styling.Fill;
//import org.geotools.styling.Graphic;
//import org.geotools.styling.Mark;
//import org.geotools.styling.Stroke;
//import org.geotools.swing.ExceptionDialog;
//import org.opengis.feature.simple.SimpleFeatureType;
//import org.opengis.filter.expression.Expression;
//
//import schmitzm.geotools.styling.StylingUtil;
//import schmitzm.swing.SwingUtil;
//import skrueger.sld.ASUtil;
//import skrueger.sld.AtlasStyler;
//
//public class GraphicEditGUI extends AbstractEditGUI {
//	protected Logger LOGGER = ASUtil.createLogger(this);
//
//	enum MARK_TYPE {
//		circle, hatch, square, triangle, star, cross, external;
//
//		@Override
//		public String toString() {
//			return AtlasStyler.R("Marktyp." + super.toString() + ".Label");
//		}
//
//		/**
//		 * @param wellKnownName
//		 *            When we only have a textual description of the MARK_TYPE,
//		 *            we identify it here.
//		 */
//		public static MARK_TYPE fromString(String wellKnownName) {
//			return MARK_TYPE.valueOf(wellKnownName);
//		}
//
//		// /**
//		// * @param wellKnownName
//		// * When we only have a textual description of the MARK_TYPE,
//		// * we identify it here.
//		// */
//		// public static MARK_TYPE fromString(String wellKnownName) {
//		// for (MARK_TYPE mt : values()) {
//		// if (mt.getWellKnownName().equals(wellKnownName)) {
//		// return mt;
//		// }
//		// }
//		// return null;
//		// }
//
//		public String getWellKnownName() {
//			return super.toString();
//		}
//	};
//
//	private static final long serialVersionUID = 1L;
//
//	public static final String OPENMAPSYMBOLS_SVG_SERVERBASENAME = "http://www.geopublishing.org/openmapsymbols/svg"; 
//
//	public static final String SVG_MIMETYPE = "image/svg+xml";
//
//	private static final int EXT_GRAPHIC_BUTTON_HEIGHT = 34;
//
//	private static final int EXT_GRAPHIC_BUTTON_WIDTH = 34;
//
//	private final Graphic graphic;
//
//	private JLabel jLabel = null;
//
//	private JComboBox jComboBoxMarkType = null;
//
//	private JPanel jPanelStroke = null;
//
//	private JLabel jLabelStrokeColor = new JLabel();
//
//	private JButton jButtonStrokeColor = null;
//
//	private JLabel jLabelStrokeWidth = new JLabel();
//
//	private JComboBox jComboBoxStrokeWidth = null;
//
//	private JLabel jLabelStrokeOpacity = new JLabel();
//
//	private JComboBox jComboBoxStrokeOpacity = null;
//
//	private JPanel jPanel = null;
//
//	private JLabel jLabel4 = null;
//
//	private JComboBox jComboBoxGraphicSize = null;
//
//	private JLabel jLabel5 = null;
//
//	private JComboBox jComboBoxGraphicOpacity = null;
//
//	protected boolean mark_mode = true;
//
//	private JPanel jPanelFill = null;
//
//	private JLabel jLabelFillColor = new JLabel();
//
//	private JButton jButtonFillColor = null;
//
//	private JLabel jLabelFillOpacity = new JLabel();
//
//	private JComboBox jComboBoxFillOpacity = null;
//
//	private JLabel jLabelRotation = new JLabel();;
//
//	private JComboBox jComboBoxGraphicRotation = null;
//
//	private JCheckBox jCheckBoxFill = null;
//
//	private JCheckBox jCheckBoxStroke = null;
//
//	Expression rememberStrokeColor;
//
//	Fill rememberFill;
//
//	private JCheckBox jCheckBoxDisplacement = null;
//
//	private JPanel jPanelDisplacement = null;
//
//	private JLabel jLabelDisplacementX = null;
//
//	private JComboBox jComboBoxDisplacementX = null;
//
//	private JLabel jLabelDisplacementY = null;
//
//	private JComboBox jComboBoxDisplacementY = null;
//
//	private JPanel jPanelExternalGraphic = null;
//
//	private JLabel jLabelEG = new JLabel();
//
//	private JButton jButtonExtGraphic;
//
//	private SVGSelector selectExternalGraphicDialog;
//
//	protected ExternalGraphic backupExternalGraphic = null;
//
//	private SimpleFeatureType graphicDefaultGeometry;
//
//	
//
//	/**
//	 * This is the default constructor
//	 * 
//	 * @param graphic
//	 * @param graphicDefaultGeometry
//	 *            Optionally defines how the graphic will be used, so that it
//	 *            can make a good preview.
//	 */
//	public GraphicEditGUI(final Graphic graphic,
//			SimpleFeatureType graphicDefaultGeometry) {
//		this.graphic = graphic;
//		this.graphicDefaultGeometry = graphicDefaultGeometry;
//		initialize();
//	}
//
//	/**
//	 * This method initializes this
//	 * 
//	 * @return void
//	 */
//	private void initialize() {
//		GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
//		gridBagConstraints41.gridx = 0;
//		gridBagConstraints41.gridwidth = 5;
//		gridBagConstraints41.fill = GridBagConstraints.HORIZONTAL;
//		gridBagConstraints41.gridy = 1;
//		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
//		gridBagConstraints2.fill = GridBagConstraints.VERTICAL;
//		gridBagConstraints2.gridy = 0;
//		gridBagConstraints2.weightx = 1.0;
//		gridBagConstraints2.anchor = GridBagConstraints.WEST;
//		gridBagConstraints2.insets = new Insets(5, 5, 5, 0);
//		gridBagConstraints2.gridx = 1;
//		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
//		gridBagConstraints1.gridx = 0;
//		gridBagConstraints1.insets = new Insets(5, 5, 5, 0);
//		gridBagConstraints1.gridy = 0;
//		jLabel = new JLabel();
//		jLabel.setText(AtlasStyler.R("GraphicEdit.Type"));
//		GridBagConstraints gridBagConstraints = new GridBagConstraints();
//		gridBagConstraints.gridx = 0;
//		gridBagConstraints.gridy = 0;
//		this.setSize(310, 302);
//		this.setLayout(new GridBagLayout());
//		this.add(jLabel, gridBagConstraints1);
//		this.add(getJComboBoxMarkType(), gridBagConstraints2);
//		this.add(getJPanelGraphic(), gridBagConstraints41);
//	}
//
//	/**
//	 * This method initializes jComboBox The WellKnownName element gives the
//	 * well-known name of the shape of the mark. Allowed values include at least
//	 * “square”, “circle”, “triangle”, “star”, “cross”, and “x”, though map
//	 * servers may draw a different symbol instead if they don't have a shape
//	 * for all of these. The default WellKnownName is “square”. Renderings of
//	 * these marks may be made solid or hollow depending on Fill and Stroke
//	 * elements. These elements are discussed in Sections 11.2.2 and 11.1.3,
//	 * respectively.
//	 * 
//	 * 
//	 * @return javax.swing.JComboBox
//	 */
//	private JComboBox getJComboBoxMarkType() {
//		if (jComboBoxMarkType == null) {
//			jComboBoxMarkType = new JComboBox();
//			jComboBoxMarkType.setModel(new DefaultComboBoxModel(MARK_TYPE
//					.values()));
//
//			/** Initialize with Graphic object * */
//			if ((graphic.getMarks() != null) && (graphic.getMarks().length > 0)) {
//				jComboBoxMarkType.getModel().setSelectedItem(
//						MARK_TYPE.fromString(graphic.getMarks()[0]
//								.getWellKnownName().toString()));
//				mark_mode = true;
//			} else if ((graphic.getExternalGraphics() != null)
//					&& (graphic.getExternalGraphics().length > 0)) {
//
//				mark_mode = false;
//				// We have an External Graphic
//				// System.out.println("We have an external graphic");
//				jComboBoxMarkType.getModel()
//						.setSelectedItem(MARK_TYPE.external);
//			} else {
//				// We have nothing, so we create a default Mark
//				mark_mode = true;
//				graphic.setMarks(new Mark[] { ASUtil.createDefaultMark() });
//				jComboBoxMarkType.getModel().setSelectedItem(
//						MARK_TYPE.fromString(graphic.getMarks()[0]
//								.getWellKnownName().toString()));
//
//				firePropertyChange(PROPERTY_UPDATED, null, null);
//			}
//			updateToMarkMode();
//
//			jComboBoxMarkType.addItemListener(new ItemListener() {
//
//				public void itemStateChanged(ItemEvent e) {
//					if (e.getStateChange() == ItemEvent.SELECTED) {
//						Object item = e.getItem();
//
//						if (item.equals(MARK_TYPE.external)) {
//							/** We are dealing with external graphics * */
//							graphic.setMarks(Mark.MARKS_EMPTY);
//							mark_mode = false;
//
//							if (backupExternalGraphic != null) {
//								graphic.graphicalSymbols().clear();
//								graphic.graphicalSymbols().add( backupExternalGraphic );
//							} else {
//								graphic.graphicalSymbols().clear();
//							}
//
//							updateToMarkMode();
//
//						} else {
//							mark_mode = true;
//
//							if (graphic.getExternalGraphics() != null) {
//								backupExternalGraphic = graphic
//										.getExternalGraphics()[0];
//							} else {
//								backupExternalGraphic = null;
//							}
//							/**
//							 * We are dealing with a Mark, throw away the
//							 * external graphic *
//							 */
//							graphic.graphicalSymbols().clear();
//
//							/** Create a new Mark if needed * */
//							if (graphic.getMarks().length == 0) {
//								graphic.setMarks(new Mark[] { ASUtil
//										.createDefaultMark() });
//								// System.out
//								// .println("created a new default mark");
//							}
//
//							Mark mark = graphic.getMarks()[0];
//							mark.setWellKnownName(ASUtil.ff2
//									.literal(((MARK_TYPE) item)
//											.getWellKnownName()));
//
//							updateToMarkMode();
//
//						}
//
//						firePropertyChange(PROPERTY_UPDATED, null, null);
//					}
//				}
//
//			});
//
//			ASUtil.addMouseWheelForCombobox(jComboBoxMarkType);
//		}
//		return jComboBoxMarkType;
//	}
//
//	protected void updateToMarkMode() {
//		// External Graphic
//		getJPanelExternalgraphic().setEnabled(!mark_mode);
//		jLabelEG.setEnabled(!mark_mode);
//		getJButtonExtGraphic().setEnabled(!mark_mode);
//
//		// Fill Panel
//		getJCheckBoxFill().setEnabled(mark_mode);
//		if ((getJCheckBoxFill().isSelected())) {
//			getJPanelFill().setEnabled(mark_mode);
//			getJComboBoxFillOpacity().setEnabled(mark_mode);
//			getJButtonFillColor().setEnabled(mark_mode);
//			jLabelFillColor.setEnabled(mark_mode);
//			jLabelFillOpacity.setEnabled(mark_mode);
//		}
//
//		// Stroke Panel
//		getJCheckBoxStroke().setEnabled(mark_mode);
//		if (getJCheckBoxStroke().isSelected()) {
//			getJPanelStroke().setEnabled(mark_mode);
//			jLabelStrokeColor.setEnabled(mark_mode);
//			getJButtonStrokeColor().setEnabled(mark_mode);
//			jLabelStrokeOpacity.setEnabled(mark_mode);
//			jLabelStrokeWidth.setEnabled(mark_mode);
//			getJComboBoxStrokeWidth().setEnabled(mark_mode);
//			getJComboBoxStrokeOpacity().setEnabled(mark_mode);
//		}
//
//	}
//
//	/**
//	 * This method initializes jPanel
//	 * 
//	 * @return javax.swing.JPanel
//	 */
//	private JPanel getJPanelStroke() {
//		if (jPanelStroke == null) {
//			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
//			gridBagConstraints9.fill = GridBagConstraints.NONE;
//			gridBagConstraints9.gridy = 0;
//			gridBagConstraints9.weightx = 1.0;
//			gridBagConstraints9.insets = new Insets(5, 5, 5, 5);
//			gridBagConstraints9.gridx = 5;
//			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
//			gridBagConstraints8.gridx = 4;
//			gridBagConstraints8.insets = new Insets(0, 15, 0, 0);
//			gridBagConstraints8.gridy = 0;
//			jLabelStrokeOpacity.setText(AtlasStyler.R("OpacityLabel"));
//			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
//			gridBagConstraints7.fill = GridBagConstraints.NONE;
//			gridBagConstraints7.gridy = 0;
//			gridBagConstraints7.weightx = 1.0;
//			gridBagConstraints7.insets = new Insets(5, 5, 5, 5);
//			gridBagConstraints7.gridx = 3;
//			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
//			gridBagConstraints6.gridx = 2;
//			gridBagConstraints6.insets = new Insets(0, 15, 0, 0);
//			gridBagConstraints6.gridy = 0;
//			jLabelStrokeWidth.setText(AtlasStyler.R("WidthLabel"));
//			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
//			gridBagConstraints5.gridx = 1;
//			gridBagConstraints5.insets = new Insets(5, 5, 5, 5);
//			gridBagConstraints5.gridy = 0;
//			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
//			gridBagConstraints4.gridx = 0;
//			gridBagConstraints4.insets = new Insets(0, 5, 0, 0);
//			gridBagConstraints4.gridy = 0;
//			jLabelStrokeColor.setText(AtlasStyler.R("ColorLabel"));
//			jPanelStroke = new JPanel();
//			jPanelStroke.setLayout(new GridBagLayout());
//			jPanelStroke.setBorder(BorderFactory.createTitledBorder(AtlasStyler
//					.R("GraphicEdit.Stroke.Title")));
//			jPanelStroke.add(jLabelStrokeColor, gridBagConstraints4);
//			jPanelStroke.add(getJButtonStrokeColor(), gridBagConstraints5);
//			jPanelStroke.add(jLabelStrokeWidth, gridBagConstraints6);
//			jPanelStroke.add(getJComboBoxStrokeWidth(), gridBagConstraints7);
//			jPanelStroke.add(jLabelStrokeOpacity, gridBagConstraints8);
//			jPanelStroke.add(getJComboBoxStrokeOpacity(), gridBagConstraints9);
//		}
//		return jPanelStroke;
//	}
//
//	/**
//	 * This method initializes jButton
//	 * 
//	 * @return javax.swing.JButton
//	 */
//	private JButton getJButtonStrokeColor() {
//		if (jButtonStrokeColor == null) {
//			jButtonStrokeColor = new JButton();
//
//			jButtonStrokeColor.setAction(new AbstractAction() {
//
//				public void actionPerformed(ActionEvent e) {
//					Color color = null;
//
//					String substring = graphic.getMarks()[0].getStroke()
//							.getColor().toString();
//					color = Color.decode(substring);
//
//					Color newColor = ASUtil.showColorChooser(
//							GraphicEditGUI.this, AtlasStyler
//									.R("Stroke.ColorChooserDialog.Title"),
//							color);
//
//					if (newColor != null) {
//						graphic.getMarks()[0].getStroke().setColor(
//								StylingUtil.STYLE_BUILDER
//										.colorExpression(newColor));
//
//						GraphicEditGUI.this.firePropertyChange(
//								PROPERTY_UPDATED, null, null);
//
//						ASUtil.updateColorButton(jButtonStrokeColor, newColor);
//
//					}
//
//				}
//
//			});
//
//			/**
//			 * Stroke color is always disabled when we deal with an external
//			 * Graphic
//			 */
//			if (mark_mode) {
//				Stroke s = graphic.getMarks()[0].getStroke();
//				if (s != null) {
//					ASUtil.updateColorButton(jButtonStrokeColor, s.getColor());
//				} else {
//					jButtonStrokeColor.setEnabled(false);
//					jLabelStrokeColor.setEnabled(false);
//				}
//			} else {
//				jButtonStrokeColor.setEnabled(false);
//				jLabelStrokeColor.setEnabled(false);
//			}
//
//		}
//		return jButtonStrokeColor;
//	}
//
//	/**
//	 * This method initializes jComboBox1
//	 * 
//	 * @return javax.swing.JComboBox
//	 */
//	private JComboBox getJComboBoxStrokeWidth() {
//		if (jComboBoxStrokeWidth == null) {
//
//			jComboBoxStrokeWidth = new JComboBox();
//
//			jComboBoxStrokeWidth
//					.setModel(new DefaultComboBoxModel(WIDTH_VALUES));
//
//			jComboBoxStrokeWidth.addItemListener(new ItemListener() {
//
//				public void itemStateChanged(ItemEvent e) {
//					if (e.getStateChange() == ItemEvent.SELECTED) {
//
//						graphic.getMarks()[0].getStroke().setWidth(
//								ASUtil.ff2.literal(e.getItem()));
//
//						firePropertyChange(PROPERTY_UPDATED, null, null);
//
//					}
//				}
//
//			});
//
//			if (mark_mode) {
//				// TODO .. might not be in list...
//				Stroke s = graphic.getMarks()[0].getStroke();
//				if (s != null)
//					jComboBoxStrokeWidth.setSelectedItem(Float.valueOf(s
//							.getWidth().toString()));
//				else {
//					jComboBoxStrokeWidth.setEnabled(false);
//					jLabelStrokeWidth.setEnabled(false);
//				}
//			} else {
//				jComboBoxStrokeWidth.setEnabled(false);
//				jLabelStrokeWidth.setEnabled(false);
//			}
//
//			ASUtil.addMouseWheelForCombobox(jComboBoxStrokeWidth);
//		}
//		return jComboBoxStrokeWidth;
//	}
//
//	/**
//	 * This method initializes jComboBox1
//	 * 
//	 * @return javax.swing.JComboBox
//	 */
//	private JComboBox getJComboBoxStrokeOpacity() {
//		if (jComboBoxStrokeOpacity == null) {
//			jComboBoxStrokeOpacity = new JComboBox();
//			jComboBoxStrokeOpacity.setModel(new DefaultComboBoxModel(
//					OPACITY_VALUES));
//
//			if ((mark_mode) && (graphic.getMarks()[0].getStroke() != null)) {
//				jComboBoxStrokeOpacity.setSelectedItem(Float.valueOf(graphic
//						.getMarks()[0].getStroke().getOpacity().toString()));
//				// TODO ASUtil... hat da was?
//			} else {
//				jComboBoxStrokeOpacity.setEnabled(false);
//				jLabelStrokeOpacity.setEnabled(false);
//			}
//
//			jComboBoxStrokeOpacity.addItemListener(new ItemListener() {
//
//				public void itemStateChanged(ItemEvent e) {
//					if (e.getStateChange() == ItemEvent.SELECTED) {
//
//						graphic.getMarks()[0].getStroke().setOpacity(
//								ASUtil.ff2.literal(e.getItem()));
//
//						firePropertyChange(PROPERTY_UPDATED, null, null);
//					}
//				}
//
//			});
//
//			ASUtil.addMouseWheelForCombobox(jComboBoxStrokeOpacity);
//		}
//		return jComboBoxStrokeOpacity;
//	}
//
//	/**
//	 * This method initializes jPanel
//	 * 
//	 * @return javax.swing.JPanel
//	 */
//	private JPanel getJPanelGraphic() {
//		if (jPanel == null) {
//			GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
//			gridBagConstraints30.gridx = 2;
//			gridBagConstraints30.gridwidth = 9;
//			gridBagConstraints30.fill = GridBagConstraints.BOTH;
//			gridBagConstraints30.gridy = 4;
//			GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
//			gridBagConstraints25.gridx = 1;
//			gridBagConstraints25.gridwidth = 10;
//			gridBagConstraints25.fill = GridBagConstraints.HORIZONTAL;
//			gridBagConstraints25.insets = new Insets(0, 0, 0, 0);
//			gridBagConstraints25.anchor = GridBagConstraints.WEST;
//			gridBagConstraints25.gridy = 3;
//			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
//			gridBagConstraints23.gridx = 0;
//			gridBagConstraints23.anchor = GridBagConstraints.NORTHEAST;
//			gridBagConstraints23.gridy = 3;
//			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
//			gridBagConstraints22.gridx = 0;
//			gridBagConstraints22.anchor = GridBagConstraints.NORTHEAST;
//			gridBagConstraints22.insets = new Insets(1, 0, 0, 0);
//			gridBagConstraints22.gridy = 1;
//			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
//			gridBagConstraints21.gridx = 0;
//			gridBagConstraints21.anchor = GridBagConstraints.NORTHEAST;
//			gridBagConstraints21.insets = new Insets(1, 0, 0, 0);
//			gridBagConstraints21.gridy = 2;
//			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
//			gridBagConstraints20.fill = GridBagConstraints.NONE;
//			gridBagConstraints20.gridy = 0;
//			gridBagConstraints20.weightx = 1.0;
//			gridBagConstraints20.gridx = 10;
//			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
//			gridBagConstraints19.gridx = 9;
//			gridBagConstraints19.gridy = 0;
//			jLabelRotation.setText(AtlasStyler.R("RotationLabel"));
//			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
//			gridBagConstraints14.gridx = 1;
//			gridBagConstraints14.gridwidth = 10;
//			gridBagConstraints14.fill = GridBagConstraints.HORIZONTAL;
//			gridBagConstraints14.gridy = 2;
//			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
//			gridBagConstraints13.gridx = 1;
//			gridBagConstraints13.fill = GridBagConstraints.HORIZONTAL;
//			gridBagConstraints13.gridwidth = 10;
//			gridBagConstraints13.gridy = 1;
//			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
//			gridBagConstraints12.fill = GridBagConstraints.NONE;
//			gridBagConstraints12.gridy = 0;
//			gridBagConstraints12.weightx = 1.0;
//			gridBagConstraints12.insets = new Insets(5, 5, 5, 5);
//			gridBagConstraints12.anchor = GridBagConstraints.WEST;
//			gridBagConstraints12.gridx = 4;
//			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
//			gridBagConstraints11.gridx = 3;
//			gridBagConstraints11.insets = new Insets(5, 15, 6, 0);
//			gridBagConstraints11.anchor = GridBagConstraints.EAST;
//			gridBagConstraints11.gridy = 0;
//			jLabel5 = new JLabel();
//			jLabel5.setText(AtlasStyler.R("OpacityLabel"));
//			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
//			gridBagConstraints10.fill = GridBagConstraints.NONE;
//			gridBagConstraints10.gridy = 0;
//			gridBagConstraints10.weightx = 1.0;
//			gridBagConstraints10.insets = new Insets(5, 5, 5, 0);
//			gridBagConstraints10.anchor = GridBagConstraints.WEST;
//			gridBagConstraints10.gridx = 2;
//			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
//			gridBagConstraints3.gridx = 0;
//			gridBagConstraints3.insets = new Insets(5, 6, 5, 0);
//			gridBagConstraints3.anchor = GridBagConstraints.EAST;
//			gridBagConstraints3.gridy = 0;
//			jLabel4 = new JLabel();
//			jLabel4.setText(AtlasStyler.R("SizeLabel"));
//			jPanel = new JPanel();
//			jPanel.setLayout(new GridBagLayout());
//			jPanel.setBorder(BorderFactory.createTitledBorder(AtlasStyler
//					.R("GraphicEdit.Graphic.Title")));
//			jPanel.add(jLabel4, gridBagConstraints3);
//			jPanel.add(getJComboBoxGraphicSize(), gridBagConstraints10);
//			jPanel.add(jLabel5, gridBagConstraints11);
//			jPanel.add(getJComboBoxGraphicOpacity(), gridBagConstraints12);
//			jPanel.add(getJPanelStroke(), gridBagConstraints13);
//			jPanel.add(getJPanelFill(), gridBagConstraints14);
//			jPanel.add(jLabelRotation, gridBagConstraints19);
//			jPanel.add(getJComboBoxGraphicRotation(), gridBagConstraints20);
//			jPanel.add(getJCheckBoxFill(), gridBagConstraints21);
//			jPanel.add(getJCheckBoxStroke(), gridBagConstraints22);
//			jPanel.add(getJCheckBoxDisplacement(), gridBagConstraints23);
//			jPanel.add(getJPanelDisplacement(), gridBagConstraints25);
//			jPanel.add(getJPanelExternalgraphic(), gridBagConstraints30);
//		}
//		return jPanel;
//	}
//
//	/**
//	 * This method initializes jComboBox1
//	 * 
//	 * @return javax.swing.JComboBox
//	 */
//	private JComboBox getJComboBoxGraphicSize() {
//		if (jComboBoxGraphicSize == null) {
//			jComboBoxGraphicSize = new JComboBox();
//			jComboBoxGraphicSize
//					.setModel(new DefaultComboBoxModel(SIZE_VALUES));
//
//			Expression size = graphic.getSize();
//
//			/** Setting a default if null or ugly Expression.NIL */
//			if (size == null || size == Expression.NIL) {
//				graphic.setSize(size = ASUtil.ff2.literal("13"));
//				firePropertyChange(PROPERTY_UPDATED, null, null);
//			}
//
//			jComboBoxGraphicSize.getModel().setSelectedItem(
//					Float.valueOf(size.toString()));
//
//			jComboBoxGraphicSize.addItemListener(new ItemListener() {
//
//				public void itemStateChanged(ItemEvent e) {
//					if (e.getStateChange() == ItemEvent.SELECTED) {
//
//						graphic
//								.setSize(ASUtil.ff2
//										.literal(e.getItem()));
//
//						firePropertyChange(PROPERTY_UPDATED, null, null);
//					}
//				}
//
//			});
//
//			ASUtil.addMouseWheelForCombobox(jComboBoxGraphicSize);
//		}
//		return jComboBoxGraphicSize;
//	}
//
//	/**
//	 * This method initializes jComboBox1
//	 * 
//	 * @return javax.swing.JComboBox
//	 */
//	private JComboBox getJComboBoxGraphicOpacity() {
//		if (jComboBoxGraphicOpacity == null) {
//			jComboBoxGraphicOpacity = new JComboBox();
//			jComboBoxGraphicOpacity.setModel(new DefaultComboBoxModel(
//					OPACITY_VALUES));
//
//			Expression opacity = graphic.getOpacity();
//			if (opacity == null) {
//				graphic.setOpacity(opacity = ASUtil.ff2.literal(1.));
//				firePropertyChange(PROPERTY_UPDATED, null, null);
//			}
//
//			// TODO Check.. the value might acutally not be included
//			jComboBoxGraphicOpacity.setSelectedItem(Float.valueOf(opacity
//					.toString()));
//
//			jComboBoxGraphicOpacity.addItemListener(new ItemListener() {
//
//				public void itemStateChanged(ItemEvent e) {
//					if (e.getStateChange() == ItemEvent.SELECTED) {
//
//						graphic.setOpacity(ASUtil.ff2.literal(e
//								.getItem()));
//
//						firePropertyChange(PROPERTY_UPDATED, null, null);
//					}
//
//				}
//			});
//			ASUtil.addMouseWheelForCombobox(jComboBoxGraphicOpacity);
//		}
//		return jComboBoxGraphicOpacity;
//	}
//
//	/**
//	 * This method initializes jPanel1
//	 * 
//	 * @return javax.swing.JPanel
//	 */
//	private JPanel getJPanelFill() {
//		if (jPanelFill == null) {
//			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
//			gridBagConstraints18.fill = GridBagConstraints.NONE;
//			gridBagConstraints18.gridy = 0;
//			gridBagConstraints18.weightx = 1.0;
//			gridBagConstraints18.anchor = GridBagConstraints.WEST;
//			gridBagConstraints18.insets = new Insets(0, 5, 0, 5);
//			gridBagConstraints18.gridx = 4;
//			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
//			gridBagConstraints17.gridx = 3;
//			gridBagConstraints17.insets = new Insets(0, 15, 0, 0);
//			gridBagConstraints17.anchor = GridBagConstraints.EAST;
//			gridBagConstraints17.gridy = 0;
//			jLabelFillOpacity = new JLabel();
//			jLabelFillOpacity.setText(AtlasStyler.R("OpacityLabel"));
//			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
//			gridBagConstraints16.fill = GridBagConstraints.NONE;
//			gridBagConstraints16.gridy = 0;
//			gridBagConstraints16.weightx = 1.0;
//			gridBagConstraints16.anchor = GridBagConstraints.WEST;
//			gridBagConstraints16.insets = new Insets(5, 5, 5, 0);
//			gridBagConstraints16.gridx = 1;
//			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
//			gridBagConstraints15.gridx = 0;
//			gridBagConstraints15.insets = new Insets(5, 5, 5, 0);
//			gridBagConstraints15.gridy = 0;
//			jLabelFillColor = new JLabel();
//			jLabelFillColor.setText(AtlasStyler.R("ColorLabel"));
//			jPanelFill = new JPanel();
//			jPanelFill.setLayout(new GridBagLayout());
//			jPanelFill.setBorder(BorderFactory.createTitledBorder(AtlasStyler
//					.R("GraphicEdit.Fill.Title")));
//			jPanelFill.add(jLabelFillColor, gridBagConstraints15);
//			jPanelFill.add(getJButtonFillColor(), gridBagConstraints16);
//			jPanelFill.add(jLabelFillOpacity, gridBagConstraints17);
//			jPanelFill.add(getJComboBoxFillOpacity(), gridBagConstraints18);
//		}
//		return jPanelFill;
//	}
//
//	/**
//	 * This method initializes jComboBox
//	 * 
//	 * @return javax.swing.JComboBox
//	 */
//	private JButton getJButtonFillColor() {
//		if (jButtonFillColor == null) {
//			jButtonFillColor = new JButton();
//
//			jButtonFillColor.setAction(new AbstractAction() {
//
//				public void actionPerformed(ActionEvent e) {
//					Color color = null;
//
//					String substring = graphic.getMarks()[0].getFill()
//							.getColor().toString();
//					color = Color.decode(substring);
//
//					Color newColor = ASUtil.showColorChooser(
//							GraphicEditGUI.this, AtlasStyler
//									.R("Fill.ColorChooserDialog.Title"), color);
//
//					if (newColor != null) {
//						graphic.getMarks()[0].getFill().setColor(
//								StylingUtil.STYLE_BUILDER
//										.colorExpression(newColor));
//
//						GraphicEditGUI.this.firePropertyChange(
//								PROPERTY_UPDATED, null, null);
//
//						ASUtil.updateColorButton(jButtonFillColor, newColor);
//
//					}
//
//				}
//
//			});
//
//			if (mark_mode) {
//				Fill f = graphic.getMarks()[0].getFill();
//				if (f != null) {
//					ASUtil.updateColorButton(jButtonFillColor, f.getColor());
//				} else {
//					jButtonFillColor.setEnabled(false);
//					jLabelFillColor.setEnabled(false);
//				}
//			} else {
//				jButtonFillColor.setEnabled(false);
//				jLabelFillColor.setEnabled(false);
//			}
//
//		}
//		return jButtonFillColor;
//	}
//
//	/**
//	 * This method initializes jComboBox1
//	 * 
//	 * @return javax.swing.JComboBox
//	 */
//	private JComboBox getJComboBoxFillOpacity() {
//		if (jComboBoxFillOpacity == null) {
//			jComboBoxFillOpacity = new JComboBox();
//			jComboBoxFillOpacity.setModel(new DefaultComboBoxModel(
//					OPACITY_VALUES));
//
//			if (mark_mode) {
//				Fill ff = graphic.getMarks()[0].getFill();
//				if (ff != null) {
//					jComboBoxFillOpacity.setSelectedItem(Float.valueOf(ff
//							.getOpacity().toString()));
//				}
//			} else {
//				jComboBoxFillOpacity.setEnabled(false);
//			}
//
//			jComboBoxFillOpacity.addItemListener(new ItemListener() {
//
//				public void itemStateChanged(ItemEvent e) {
//					if (e.getStateChange() == ItemEvent.SELECTED) {
//
//						graphic.getMarks()[0].getFill().setOpacity(
//								ASUtil.ff2.literal(e.getItem()));
//
//						firePropertyChange(PROPERTY_UPDATED, null, null);
//
//					}
//				}
//
//			});
//			ASUtil.addMouseWheelForCombobox(jComboBoxFillOpacity);
//
//		}
//		return jComboBoxFillOpacity;
//	}
//
//	/**
//	 * This method initializes jComboBox1
//	 * 
//	 * @return javax.swing.JComboBox
//	 */
//	private JComboBox getJComboBoxGraphicRotation() {
//		if (jComboBoxGraphicRotation == null) {
//			jComboBoxGraphicRotation = new JComboBox();
//			jComboBoxGraphicRotation.setModel(new DefaultComboBoxModel(
//					ROTATION_VALUES));
//			jComboBoxGraphicRotation.setSelectedItem(Double.valueOf(graphic
//					.getRotation().toString()));
//			jComboBoxGraphicRotation.addItemListener(new ItemListener() {
//
//				public void itemStateChanged(ItemEvent e) {
//					if (e.getStateChange() == ItemEvent.SELECTED) {
//
//						graphic.setRotation(ASUtil.ff2.literal(e
//								.getItem()));
//
//						firePropertyChange(PROPERTY_UPDATED, null, null);
//					}
//
//				}
//			});
//			ASUtil.addMouseWheelForCombobox(jComboBoxGraphicRotation);
//
//		}
//		return jComboBoxGraphicRotation;
//	}
//
//	/**
//	 * This method initializes jCheckBox
//	 * 
//	 * @return javax.swing.JCheckBox
//	 */
//	private JCheckBox getJCheckBoxFill() {
//		if (jCheckBoxFill == null) {
//			jCheckBoxFill = new JCheckBox();
//
//			jCheckBoxFill.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//
//					boolean enabled = jCheckBoxFill.isSelected();
//
//					if (enabled) {
//
//						final Fill fill;
//						if (rememberFill != null) {
//							fill = rememberFill;
//							fill.setColor(rememberFill.getColor());
//						} else {
//							fill = ASUtil.createDefaultFill();
//						}
//						graphic.getMarks()[0].setFill(fill);
//					} else {
//						Fill fill = graphic.getMarks()[0].getFill();
//						if (fill != null) {
//							rememberFill = fill;
//						}
//						graphic.getMarks()[0].setFill(null);
//					}
//
//					getJPanelFill().setEnabled(enabled);
//					jComboBoxFillOpacity.setEnabled(enabled);
//					jButtonFillColor.setEnabled(enabled);
//					jLabelFillColor.setEnabled(enabled);
//					jLabelFillOpacity.setEnabled(enabled);
//
//					// System.out.println("setting fillpanel to "
//					// + getJPanelFill().isEnabled());
//
//					firePropertyChange(PROPERTY_UPDATED, null, null);
//				}
//
//			});
//			jCheckBoxFill.setSelected(mark_mode ? graphic.getMarks()[0]
//					.getFill() != null : false);
//		}
//		return jCheckBoxFill;
//	}
//
//	/**
//	 * This method initializes jCheckBoxStroke
//	 * 
//	 * @return javax.swing.JCheckBoxStroke
//	 */
//	private JCheckBox getJCheckBoxStroke() {
//		if (jCheckBoxStroke == null) {
//			jCheckBoxStroke = new JCheckBox();
//
//			jCheckBoxStroke.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					boolean onOff = jCheckBoxStroke.isSelected();
//					// TODO remember the Fill obejct!?
//					if (onOff) {
//						// If available, read the stored values from the GUI
//						// elements
//						Float opacity = (Float) jComboBoxStrokeOpacity
//								.getSelectedItem();
//						Float width = (Float) jComboBoxStrokeWidth
//								.getSelectedItem();
//						if (rememberStrokeColor == null) {
//							rememberStrokeColor = ASUtil.ff2.literal("#FFFFFF");
//						}
//						if (opacity == null)
//							opacity = 1f;
//						if (width == null)
//							width = 1f;
//						org.geotools.styling.Stroke stroke = ASUtil.sf
//								.createStroke(rememberStrokeColor, ASUtil.ff2
//										.literal(width), ASUtil.ff2
//										.literal(opacity));
//
//						graphic.getMarks()[0].setStroke(stroke);
//					} else {
//						if (graphic.getMarks()[0].getStroke() != null) {
//							rememberStrokeColor = graphic.getMarks()[0]
//									.getStroke().getColor();
//						}
//
//						graphic.getMarks()[0].setStroke(null);
//					}
//
//					getJPanelStroke().setEnabled(onOff);
//					jComboBoxStrokeOpacity.setEnabled(onOff);
//					jButtonStrokeColor.setEnabled(onOff);
//					jComboBoxStrokeWidth.setEnabled(onOff);
//					jLabelStrokeColor.setEnabled(onOff);
//					jLabelStrokeOpacity.setEnabled(onOff);
//					jLabelStrokeWidth.setEnabled(onOff);
//
//					firePropertyChange(PROPERTY_UPDATED, null, null);
//				}
//
//			});
//			jCheckBoxStroke.setSelected(mark_mode ? graphic.getMarks()[0]
//					.getStroke() != null : false);
//		}
//		return jCheckBoxStroke;
//	}
//
//	/**
//	 * This method initializes jCheckBox
//	 * 
//	 * @return javax.swing.JCheckBox
//	 */
//	private JCheckBox getJCheckBoxDisplacement() {
//		if (jCheckBoxDisplacement == null) {
//			jCheckBoxDisplacement = new JCheckBox();
//
//			jCheckBoxDisplacement.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					boolean onOff = jCheckBoxDisplacement.isSelected();
//
//					/***********************************************************
//					 * Intervention because GT is not rendering it
//					 */
//					JOptionPane.showMessageDialog(GraphicEditGUI.this,
//							AtlasStyler.R("NotWorkingInGT24.Displacement"),
//							"GeoTools 2.4 doesn't honour displacement tags",
//							JOptionPane.WARNING_MESSAGE);
//
//					if (onOff) {
//						// If available, read the stored values from the GUI
//						// elements
//						Float dx = (Float) jComboBoxDisplacementX
//								.getSelectedItem();
//						Float dy = (Float) jComboBoxDisplacementY
//								.getSelectedItem();
//						if (dx == null)
//							dx = 0f;
//						if (dy == null)
//							dy = 0f;
//
//						Displacement dis = ASUtil.sf.createDisplacement(
//								ASUtil.ff2.literal(dx), ASUtil.ff2.literal(dy));
//
//						graphic.setDisplacement(dis);
//					} else {
//						graphic.setDisplacement(null);
//					}
//
//					getJPanelStroke().setEnabled(onOff);
//					jComboBoxDisplacementX.setEnabled(onOff);
//					jComboBoxDisplacementY.setEnabled(onOff);
//					jLabelDisplacementX.setEnabled(onOff);
//					jLabelDisplacementY.setEnabled(onOff);
//
//					firePropertyChange(PROPERTY_UPDATED, null, null);
//				}
//
//			});
//			jCheckBoxDisplacement
//					.setSelected(graphic.getDisplacement() != null);
//		}
//
//		return jCheckBoxDisplacement;
//	}
//
//	/**
//	 * This method initializes jPanel2
//	 * 
//	 * @return javax.swing.JPanel
//	 */
//	private JPanel getJPanelDisplacement() {
//		if (jPanelDisplacement == null) {
//			GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
//			gridBagConstraints29.fill = GridBagConstraints.NONE;
//			gridBagConstraints29.gridy = 0;
//			gridBagConstraints29.weightx = 1.0;
//			gridBagConstraints29.anchor = GridBagConstraints.WEST;
//			gridBagConstraints29.insets = new Insets(5, 0, 5, 5);
//			gridBagConstraints29.gridx = 3;
//			GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
//			gridBagConstraints28.gridx = 2;
//			gridBagConstraints28.anchor = GridBagConstraints.EAST;
//			gridBagConstraints28.insets = new Insets(5, 5, 5, 5);
//			gridBagConstraints28.gridy = 0;
//			jLabelDisplacementY = new JLabel();
//			jLabelDisplacementY.setText(AtlasStyler.R("VerticalLabel"));
//			GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
//			gridBagConstraints27.fill = GridBagConstraints.VERTICAL;
//			gridBagConstraints27.gridy = 0;
//			gridBagConstraints27.weightx = 1.0;
//			gridBagConstraints27.insets = new Insets(5, 5, 5, 5);
//			gridBagConstraints27.anchor = GridBagConstraints.WEST;
//			gridBagConstraints27.gridx = 1;
//			GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
//			gridBagConstraints26.gridx = 0;
//			gridBagConstraints26.anchor = GridBagConstraints.EAST;
//			gridBagConstraints26.gridy = 0;
//			jLabelDisplacementX = new JLabel();
//			jLabelDisplacementX.setText(AtlasStyler.R("HorizontalLabel"));
//			jPanelDisplacement = new JPanel();
//			jPanelDisplacement.setLayout(new GridBagLayout());
//			jPanelDisplacement.setBorder(BorderFactory
//					.createTitledBorder(AtlasStyler
//							.R("GraphicEdit.Displacement.Title")));
//			jPanelDisplacement.add(jLabelDisplacementX, gridBagConstraints26);
//			jPanelDisplacement.add(getJComboBoxDisplacementX(),
//					gridBagConstraints27);
//			jPanelDisplacement.add(jLabelDisplacementY, gridBagConstraints28);
//			jPanelDisplacement.add(getJComboBoxDisplacementY(),
//					gridBagConstraints29);
//		}
//		return jPanelDisplacement;
//	}
//
//	/**
//	 * This method initializes jComboBox
//	 * 
//	 * @return javax.swing.JComboBox
//	 */
//	private JComboBox getJComboBoxDisplacementX() {
//		if (jComboBoxDisplacementX == null) {
//			jComboBoxDisplacementX = new JComboBox();
//			jComboBoxDisplacementX.setModel(new DefaultComboBoxModel(
//					DISPLACEMENT_VALUES));
//
//			if (graphic.getDisplacement() == null) {
//				jComboBoxDisplacementX.setEnabled(false);
//				jLabelDisplacementX.setEnabled(false);
//			} else {
//				jComboBoxDisplacementX.setSelectedItem(Float.valueOf(graphic
//						.getDisplacement().getDisplacementX().toString()));
//			}
//
//			jComboBoxDisplacementX.addItemListener(new ItemListener() {
//
//				public void itemStateChanged(ItemEvent e) {
//					if (e.getStateChange() == ItemEvent.SELECTED) {
//
//						Displacement d = graphic.getDisplacement();
//						d.setDisplacementX(ASUtil.ff2.literal(e.getItem()));
//
//						graphic.setDisplacement(d);
//
//						firePropertyChange(PROPERTY_UPDATED, null, null);
//					}
//
//				}
//			});
//			ASUtil.addMouseWheelForCombobox(jComboBoxDisplacementX);
//		}
//		return jComboBoxDisplacementX;
//	}
//
//	/**
//	 * This method initializes jComboBox
//	 * 
//	 * @return javax.swing.JComboBox
//	 */
//	private JComboBox getJComboBoxDisplacementY() {
//		if (jComboBoxDisplacementY == null) {
//			jComboBoxDisplacementY = new JComboBox();
//			jComboBoxDisplacementY.setModel(new DefaultComboBoxModel(
//					DISPLACEMENT_VALUES));
//
//			if (graphic.getDisplacement() == null) {
//				jComboBoxDisplacementY.setEnabled(false);
//				jLabelDisplacementY.setEnabled(false);
//			} else {
//				jComboBoxDisplacementY.setSelectedItem(Float.valueOf(graphic
//						.getDisplacement().getDisplacementY().toString()));
//			}
//
//			jComboBoxDisplacementY.addItemListener(new ItemListener() {
//
//				public void itemStateChanged(ItemEvent e) {
//					if (e.getStateChange() == ItemEvent.SELECTED) {
//
//						Displacement d = graphic.getDisplacement();
//						d.setDisplacementY(ASUtil.ff2.literal(e.getItem()));
//
//						graphic.setDisplacement(d);
//
//						firePropertyChange(PROPERTY_UPDATED, null, null);
//					}
//
//				}
//			});
//			ASUtil.addMouseWheelForCombobox(jComboBoxDisplacementY);
//		}
//		return jComboBoxDisplacementY;
//	}
//
//	/**
//	 * This method initializes jPanel2
//	 * 
//	 * @return javax.swing.JPanel
//	 */
//	private JPanel getJPanelExternalgraphic() {
//		if (jPanelExternalGraphic == null) {
//			GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
//			gridBagConstraints32.gridx = 1;
//			gridBagConstraints32.gridy = 0;
//			GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
//			gridBagConstraints31.gridx = 0;
//			gridBagConstraints31.gridy = 0;
//			gridBagConstraints31.insets = new Insets(5, 5, 5, 5);
//			jLabelEG.setText(AtlasStyler.R("ExternalGraphicLabel"));
//			jPanelExternalGraphic = new JPanel();
//			jPanelExternalGraphic.setLayout(new GridBagLayout());
//			jPanelExternalGraphic.setBorder(BorderFactory
//					.createTitledBorder(AtlasStyler
//							.R("GraphicEdit.ExternalGraphic.Title")));
//			jPanelExternalGraphic.add(jLabelEG, gridBagConstraints31);
//			jPanelExternalGraphic.add(getJButtonExtGraphic(),
//					gridBagConstraints32);
//		}
//		return jPanelExternalGraphic;
//	}
//
//	/**
//	 * This method initializes jButton
//	 * 
//	 * @return javax.swing.JButton
//	 */
//	private JButton getJButtonExtGraphic() {
//		if (jButtonExtGraphic == null) {
//			jButtonExtGraphic = new JButton();
//
//			jButtonExtGraphic.setAction(new AbstractAction() {
//
//				public void actionPerformed(ActionEvent e) {
//					openExternalGraphicSelector();
//				}
//
//			});
//
//			jButtonExtGraphic.setSize(EXT_GRAPHIC_BUTTON_WIDTH,
//					EXT_GRAPHIC_BUTTON_HEIGHT);
//
//			updateExternalGraphicButton();
//
//		}
//		return jButtonExtGraphic;
//	}
//
//	private void updateExternalGraphicButton() {
//		// Update the Button Icon
//		SVGGlyphRenderer renderer = new SVGGlyphRenderer();
//		ImageIcon icon = null;
//		if ((graphic.getExternalGraphics() != null)
//				&& (graphic.getExternalGraphics().length > 0)) {
//			ExternalGraphic externalGraphic = graphic.getExternalGraphics()[0];
//			if (externalGraphic == null) {
//				throw new IllegalArgumentException(
//						"ExternalGraphicsArray contains null");
//			}
//
//			BufferedImage renderedImage = renderer.render(null,
//					externalGraphic, null, EXT_GRAPHIC_BUTTON_HEIGHT);
//			if (renderedImage != null)
//				icon = new ImageIcon(renderedImage);
//		}
//		;
//
//		if (icon == null) {
//			// Generating the icon failed, use an empty default
//			icon = new ImageIcon(new BufferedImage(EXT_GRAPHIC_BUTTON_WIDTH,
//					EXT_GRAPHIC_BUTTON_HEIGHT, BufferedImage.TYPE_INT_ARGB));
//		}
//
//		jButtonExtGraphic.setIcon(icon);
//		jButtonExtGraphic.setSize(EXT_GRAPHIC_BUTTON_WIDTH,
//				EXT_GRAPHIC_BUTTON_HEIGHT);
//
//		// Repack the window if it makes sense
//		final Window parentWindow = SwingUtil.getParentWindow(this);
//		if (parentWindow != null)
//			parentWindow.pack();
//
//	}
//
//	protected void openExternalGraphicSelector() {
//		try {
//			if (selectExternalGraphicDialog == null) {
//
//				selectExternalGraphicDialog = new SVGSelector(SwingUtil
//						.getParentWindow(GraphicEditGUI.this),
//						graphicDefaultGeometry.getGeometryDescriptor(), graphic
//								.getExternalGraphics());
//
//				// selectExternalGraphicDialog = new SVGSelector(SwingUtil
//				// .getParentWindow(GraphicEditGUI4.this), Utilities
//				// .createFeatureType(Point.class).getDefaultGeometry(),
//				// graphic.getExternalGraphics() );
//
//				selectExternalGraphicDialog.setModal(true);
//				selectExternalGraphicDialog
//						.addPropertyChangeListener(new PropertyChangeListener() {
//
//							public void propertyChange(PropertyChangeEvent evt) {
//
//								if (evt.getPropertyName().equals(
//										SVGSelector.PROPERTY_UPDATED)) {
//
//									LOGGER.info(evt.getSource().getClass()
//											.getSimpleName());
//									ExternalGraphic[] egs = (ExternalGraphic[]) evt
//											.getNewValue();
//									if (egs != null) {
//										try {
//											LOGGER.info("EG Location = "
//													+ egs[0].getLocation());
//										} catch (Exception e) {
//											LOGGER
//													.error(
//															"The ExternalGraphic is not valid. Removing it",
//															e);
//											graphic.setExternalGraphics(null);
//											ExceptionDialog
//													.show(
//															SwingUtil
//																	.getParentWindowComponent(GraphicEditGUI.this),
//															e);
//										}
//										graphic.setExternalGraphics(egs);
//									} else {
//										graphic.setExternalGraphics(null);
//									}
//
//									GraphicEditGUI.this.firePropertyChange(
//											AbstractEditGUI.PROPERTY_UPDATED,
//											null, null);
//
//									updateExternalGraphicButton();
//								}
//							}
//
//						});
//
//			}
//			selectExternalGraphicDialog.setVisible(true);
//
//		} catch (Exception e1) {
//			ExceptionDialog.show(SwingUtil
//					.getParentWindowComponent(GraphicEditGUI.this), e1);
//		}
//
//	}
//} visual-constraint="10,10"
