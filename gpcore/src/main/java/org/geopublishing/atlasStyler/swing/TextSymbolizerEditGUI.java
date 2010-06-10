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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;

import net.miginfocom.swing.MigLayout;

import org.apache.log4j.Logger;
import org.apache.tools.ant.taskdefs.Tstamp;
import org.geopublishing.atlasStyler.ASProps;
import org.geopublishing.atlasStyler.ASUtil;
import org.geopublishing.atlasStyler.AbstractRuleList;
import org.geopublishing.atlasStyler.AtlasStyler;
import org.geopublishing.atlasStyler.RuleChangeListener;
import org.geopublishing.atlasStyler.RuleChangedEvent;
import org.geopublishing.atlasStyler.TextRuleList;
import org.geopublishing.atlasViewer.swing.AVSwingUtil;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.Style;
import org.geotools.styling.TextSymbolizer;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

import schmitzm.geotools.FilterUtil;
import schmitzm.geotools.feature.FeatureUtil;
import schmitzm.geotools.gui.SelectableXMapPane;
import schmitzm.geotools.gui.XMapPane;
import schmitzm.geotools.styling.StylingUtil;
import schmitzm.lang.LangUtil;
import schmitzm.swing.JPanel;
import schmitzm.swing.SwingUtil;
import skrueger.swing.ColorButton;

/**
 * TODO The preView features are not beeing closed/disposed securely
 * 
 * @author stefan
 * 
 */
public class TextSymbolizerEditGUI extends AbstractEditGUI {
	protected Logger LOGGER = ASUtil.createLogger(this);

	private JComboBox jComboBoxSize = null;

	private JComboBox jComboBoxStyle = null;

	private JComboBox jComboBoxWeight = null;

	final AtlasStyler atlasStyler;

	// Create a new JMapPane and fill it with 40 sample features
	final protected XMapPane mapPane = new SelectableXMapPane();

	/**
	 * Shall the preview JMapPane be anti-aliased? Can be toggled with the mouse
	 * menu
	 **/
	boolean mapPaneAntiAliased = ASProps.getInt(ASProps.Keys.antialiasingMaps,
			1) == 1;

	/** The font sizes that can be selected. */
	public static final Double[] SIZES = { 6., 7., 8., 9., 10., 11., 12., 14.,
			16., 18., 20., 22., 24., 28., 36., 48., 72. };

	/**
	 * The “font-style” SvgParameter element gives the style to use for a font.
	 * The allowed values are “normal”, “italic”, and “oblique”.
	 */
	public static final String[] FONT_STYLES = { "normal", "italic", "oblique" };

	public static final String[] FONT_WEIGHTS = { "normal", "bold" };

	private final TextRuleList rulesList;

	private ColorButton jButtonColorHalo;
	/**
	 * When setting halo radius to 0, the halo elemnt is nulled. When we
	 * reactivate it, let's
	 **/

	private JComboBox jComboBoxFont;

	private ColorButton jButtonColor;

	private JComboBox jComboBoxHaloFillOpacity;

	private JComboBox jComboBoxHaloRadius;

	/**
	 * This is the default constructor. It takes the {@link TextRuleList} that
	 * it will allow to edit, and an {@link AtlasStyler} so the preview can use
	 * the other Style.
	 * 
	 * @param preViewFeatures
	 */
	public TextSymbolizerEditGUI(TextRuleList rulesList,
			AtlasStyler atlasStyler,
			FeatureCollection<SimpleFeatureType, SimpleFeature> preViewFeatures) {
		this.rulesList = rulesList;
		this.atlasStyler = atlasStyler;
		initialize(preViewFeatures);
	}

	/**
	 * This method initializes this
	 * 
	 * @param features
	 * 
	 * @return void
	 */
	private void initialize(
			FeatureCollection<SimpleFeatureType, SimpleFeature> features) {
		this.setLayout(new MigLayout("wrap 1", "[grow]", "[grow][][]"));
		this.add(getPreviewMapPane(features), "top, growy, gap 0! 0! 0! 0!");

		JPanel jPanelFont = new JPanel(new MigLayout("", "[grow]"));
		{

			jPanelFont.add(new JLabel(AtlasStyler
					.R("TextRulesList.Textstyle.FontFamily")));
			jPanelFont.add(getJComboBoxFont(), "span 2");
			jPanelFont.add(new JLabel(AtlasStyler.R("SizeLabel")));
			jPanelFont.add(getJComboBoxSize(), "wrap");

			jPanelFont.add(new JLabel(AtlasStyler
					.R("TextRulesList.Textstyle.FontStyle")));
			jPanelFont.add(getJComboBoxStyle());

			jPanelFont.add(new JLabel(AtlasStyler.R("ColorLabel")));
			jPanelFont.add(getJButtonColor());

			jPanelFont.add(new JLabel(AtlasStyler
					.R("TextRulesList.Textstyle.FontWeight")));
			jPanelFont.add(getJComboBoxWeight(), "wrap");

			jPanelFont.add(new JLabel(ASUtil
					.R("TextRulesList.Textstyle.Halo.Radius.label")));
			jPanelFont.add(getJComboBoxHaloRadius());
			jPanelFont.add(new JLabel(ASUtil
					.R("TextRulesList.Textstyle.Halo.Color.label")));
			jPanelFont.add(getJButtonColorHalo());
			jPanelFont.add(new JLabel(ASUtil
					.R("TextRulesList.Textstyle.Halo.Opacity.label")));
			jPanelFont.add(getJComboBoxHaloOpacity());

		}
		this.add(jPanelFont, "growx");

		switch (FeatureUtil.getGeometryForm(rulesList.getStyledFeatures()
				.getSchema())) {
		case LINE:
			this.add(getJPanelLinePlacement(), "growx");
			break;
		case POINT:
		case POLYGON:
			this.add(getJPanelPointPlacement(), "growx");
			break;
		}

	}

	JComboBox getJComboBoxHaloOpacity() {
		if (jComboBoxHaloFillOpacity == null) {
			jComboBoxHaloFillOpacity = new JComboBox();
			jComboBoxHaloFillOpacity.setModel(new DefaultComboBoxModel(
					OPACITY_VALUES));

			jComboBoxHaloFillOpacity.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						rulesList.getSymbolizer().getHalo().getFill()
								.setOpacity(ASUtil.ff2.literal(e.getItem()));

						rulesList.fireEvents(new RuleChangedEvent(
								"The halo opacity changed to " + e.getItem(),
								rulesList));

					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxHaloFillOpacity);
		}
		return jComboBoxHaloFillOpacity;

	}

	ColorButton getJButtonColorHalo() {
		if (jButtonColorHalo == null) {
			jButtonColorHalo = new ColorButton();
			jButtonColorHalo.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					Color color = AVSwingUtil.showColorChooser(

					TextSymbolizerEditGUI.this, AtlasStyler
							.R("Text.Halo.ColorChooserDialog.Title"),
							StylingUtil.getColorFromExpression(rulesList
									.getSymbolizer().getHalo().getFill()
									.getColor()));

					if (color != null) {
						rulesList.getSymbolizer().getHalo().getFill().setColor(
								StylingUtil.STYLE_BUILDER
										.colorExpression(color));
						jButtonColorHalo.setColor(color);

						rulesList.fireEvents(new RuleChangedEvent(
								"Halo color changed for "
										+ rulesList.getRuleName(), rulesList));
					}
				}

			});
		}
		return jButtonColorHalo;
	}

	JComboBox getJComboBoxHaloRadius() {
		if (jComboBoxHaloRadius == null) {
			jComboBoxHaloRadius = new JComboBox(HALO_RADIUS_VALUES);

			jComboBoxHaloRadius.setRenderer(HALO_RADIUS_VALUES_RENDERER);

			jComboBoxHaloRadius.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						final float radius = (Float) e.getItem();

						rulesList.pushQuite();

						if (radius == 0f) {
							getJComboBoxHaloOpacity().setEnabled(false);
							getJButtonColorHalo().setEnabled(false);
							rulesList.getSymbolizer().setHalo(null);
						} else {

							Color restoreColor = getJButtonColorHalo()
									.getColor() != null ? getJButtonColorHalo()
									.getColor() : Color.green;
							Float restoreOpacity = (Float) (getJComboBoxHaloOpacity()
									.getSelectedItem() != null ? getJComboBoxHaloOpacity()
									.getSelectedItem()
									: 1f);

							if (rulesList.getSymbolizer().getHalo() == null) {
								rulesList.getSymbolizer().setHalo(
										ASUtil.SB.createHalo(restoreColor,
												restoreOpacity, radius));
							}
							if (rulesList.getSymbolizer().getHalo().getFill() == null) {
								rulesList.getSymbolizer().getHalo().setFill(
										ASUtil.SB.createFill(restoreColor,
												restoreOpacity));
							}

							getJButtonColorHalo().setColor(
									rulesList.getSymbolizer().getHalo()
											.getFill().getColor());

							float opacity = Float.valueOf(rulesList
									.getSymbolizer().getHalo().getFill()
									.getOpacity().toString());
							getJComboBoxHaloOpacity().setSelectedItem(opacity);

							getJComboBoxHaloOpacity().setEnabled(true);
							getJButtonColorHalo().setEnabled(true);

							rulesList.getSymbolizer().getHalo().setRadius(
									ASUtil.ff2.literal(radius));
						}

						rulesList.popQuite(new RuleChangedEvent(
								"The halo radius changed to " + radius,
								rulesList));
					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxHaloRadius);
		}
		return jComboBoxHaloRadius;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	@SuppressWarnings("unchecked")
	public JComboBox getJComboBoxFont() {
		if (jComboBoxFont == null) {

			jComboBoxFont = new JComboBox();

			List<Literal>[] fontFamilies = AtlasStyler.getDefaultFontFamilies();

			/**
			 * Add user defined Fonts. One Family for every extra font.
			 */
			List<Font> extraFonts = atlasStyler.getFonts();

			int i = 6;
			for (Font f : extraFonts) {
				fontFamilies = LangUtil.extendArray(fontFamilies,
						new ArrayList<Literal>());
				fontFamilies[i].add(FilterUtil.FILTER_FAC.literal(f.getName()));
				i++;
			}

			/**
			 * This renderer present the items of type Collection<Literal>
			 * nicely to the user.
			 */
			jComboBoxFont.setRenderer(new DefaultListCellRenderer() {

				@Override
				public Component getListCellRendererComponent(JList list,
						Object value, int index, boolean isSelected,
						boolean cellHasFocus) {

					JLabel proto = (JLabel) super.getListCellRendererComponent(
							list, value, index, isSelected, cellHasFocus);

					ArrayList<Literal> itemValue = (ArrayList<Literal>) value;
					proto.setText(itemValue.get(0).toString());

					return proto;
				}

			});

			jComboBoxFont.setModel(new DefaultComboBoxModel(fontFamilies));

			// Expression fontFamily =
			// rulesList.getSymbolizer().getFonts()[0].getFontFamily();
			// String fontFamiliyString = fontFamily.toString();
			// jComboBoxFont.setSelectedItem( fontFamiliyString );

			jComboBoxFont.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						ArrayList<Literal> fontExpressions = (ArrayList<Literal>) jComboBoxFont
								.getSelectedItem();

						rulesList.getSymbolizer().getFont().getFamily().clear();
						rulesList.getSymbolizer().getFont().getFamily().addAll(
								fontExpressions);

						rulesList
								.fireEvents(new RuleChangedEvent(
										"The FontFamiliy changed to "
												+ fontExpressions.toString(),
										rulesList));

					}
				}

			});

			// Determine where the selected font is and select the combobox
			{
				Expression selectedFontFamily = rulesList.getSymbolizer()
						.getFont().getFamily().get(0);

				int idx = 0;
				for (List<Literal> ffs : fontFamilies) {
					for (Literal l : ffs) {
						if (l.equals(selectedFontFamily)) {
							jComboBoxFont.setSelectedIndex(idx);
						}

					}
					idx++;
				}
			}

			SwingUtil.addMouseWheelForCombobox(jComboBoxFont);
		}
		return jComboBoxFont;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	protected JComboBox getJComboBoxSize() {
		if (jComboBoxSize == null) {
			jComboBoxSize = new JComboBox();
			jComboBoxSize.setModel(new DefaultComboBoxModel(SIZES));

			/**
			 * Selecting the imported size in the JComboBox
			 */
			Double sizeDouble = Double.valueOf((rulesList.getSymbolizer()
					.getFont().getSize()).toString());
			ASUtil.selectOrInsert(jComboBoxSize, sizeDouble);

			jComboBoxSize.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						Double size = (Double) jComboBoxSize.getSelectedItem();
						Literal sizeLiteral = ASUtil.ff2.literal(size);
						rulesList.getSymbolizer().getFont()
								.setSize(sizeLiteral);
						rulesList.fireEvents(new RuleChangedEvent("FonSize = "
								+ size, rulesList));
					}
				}

			});

			SwingUtil.addMouseWheelForCombobox(jComboBoxSize);
		}
		return jComboBoxSize;
	}

	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	protected ColorButton getJButtonColor() {
		if (jButtonColor == null) {

			jButtonColor = new ColorButton();
			jButtonColor.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					Color color = AVSwingUtil.showColorChooser(
							TextSymbolizerEditGUI.this, AtlasStyler
									.R("Text.ColorChooserDialog.Title"),
							StylingUtil.getColorFromExpression(rulesList
									.getSymbolizer().getFill().getColor()));
					if (color != null) {
						rulesList.getSymbolizer().getFill().setColor(
								StylingUtil.STYLE_BUILDER
										.colorExpression(color));
						jButtonColor.setColor(color);
					}
					rulesList.fireEvents(new RuleChangedEvent(
							"FontColor changed for " + rulesList.getRuleName(),
							rulesList));
				}

			});
		}
		return jButtonColor;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	protected JComboBox getJComboBoxStyle() {
		if (jComboBoxStyle == null) {
			jComboBoxStyle = new JComboBox();
			jComboBoxStyle.setModel(new DefaultComboBoxModel(FONT_STYLES));
			jComboBoxStyle.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						rulesList.getSymbolizer().getFont().setStyle(
								ASUtil.ff2.literal(jComboBoxStyle
										.getSelectedItem()));
						rulesList.fireEvents(new RuleChangedEvent("FontStyle",
								rulesList));
					}
				}

			});
			SwingUtil.addMouseWheelForCombobox(jComboBoxStyle);
		}
		return jComboBoxStyle;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	protected JComboBox getJComboBoxWeight() {
		if (jComboBoxWeight == null) {
			jComboBoxWeight = new JComboBox();
			jComboBoxWeight.setModel(new DefaultComboBoxModel(FONT_WEIGHTS));
			jComboBoxWeight.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						rulesList.getSymbolizer().getFont().setWeight(
								ASUtil.ff2.literal(jComboBoxWeight
										.getSelectedItem()));
						rulesList.fireEvents(new RuleChangedEvent("FontWeight",
								rulesList));
					}
				}

			});
			SwingUtil.addMouseWheelForCombobox(jComboBoxWeight);
		}
		return jComboBoxWeight;
	}

	/**
	 * Creates a {@link SelectableXMapPane} that will preview the TextSymbolizer
	 * with real data.
	 * 
	 * @param features
	 */
	private XMapPane getPreviewMapPane(
			FeatureCollection<SimpleFeatureType, SimpleFeature> features) {

		MapContext context = new DefaultMapContext();
		context.addLayer(features, createPreviewStyle());
		mapPane.setLocalContext(context);
		final MapLayer layer = context.getLayer(0);
		ReferencedEnvelope bounds;
		try {
			bounds = layer.getBounds();
		} catch (Exception e) {
			LOGGER
					.error("Calculating BOUNDs for the PreviewMapPane failed:",
							e);

			bounds = features.getBounds();

		}
		bounds.expandBy(bounds.getSpan(0) * 1.5, bounds.getSpan(1));
		mapPane.setMaxExtend(bounds);
		mapPane.zoomToLayer(0);

		rulesList.addListener(ruleChangedUpadteThePreview);

		// mapPane.setMinimumSize(new Dimension(300, 40));
		mapPane.setMinimumSize(new Dimension(500, 170));

		// A mouse listener to toggel anti-aliasing
		mapPane.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON2) {
					// not left button
					JPopupMenu popup = new JPopupMenu();
					final JCheckBoxMenuItem menuItemAntiAliased = new JCheckBoxMenuItem(
							new AbstractAction(
									ASUtil
											.R("TextSymbolizerEditGUI.Preview.ToggleAntiAliasing.label")) {

								@Override
								public void actionPerformed(ActionEvent e) {
									mapPaneAntiAliased = !mapPaneAntiAliased;
									mapPane.setAntiAliasing(mapPaneAntiAliased);
									mapPane.refresh();

								}

							});
					menuItemAntiAliased.setSelected(mapPaneAntiAliased);
					popup.add(menuItemAntiAliased);
					popup.show(mapPane, e.getX(), e.getY());
				}
			}
		});

		return mapPane;
	}

	private final RuleChangeListener ruleChangedUpadteThePreview = new RuleChangeListener() {

		@Override
		public void changed(RuleChangedEvent e) {

			final Style style = createPreviewStyle();

			// There is always only one layer in the preview
			final MapLayer mapLayer = mapPane.getMapContext().getLayer(0);

			// This is un-nice but needed to reflect all changes automatically
			mapPane.getLocalRenderer().setContext(mapPane.getMapContext());

			// We **have to** make a copy of the Style, otherwise the changes to
			// the font are only reflected after zooming
			DuplicatingStyleVisitor dsv = new DuplicatingStyleVisitor();
			dsv.visit(style);
			mapLayer.setStyle((Style) dsv.getCopy());

			// mapPane.refresh();
		}

	};

	private JComboBox jComboBoxPointPlacementDisplacementY;

	private JComboBox jComboBoxPointPlacementDisplacementX;

	private JComboBox jComboBoxPointPlacementAnchorY;

	private JComboBox jComboBoxPointPlacementAnchorX;

	private JComboBox jComboBoxLabelRotation;

	/**
	 * Creating a Style that for the preview. Depends on the selected class
	 */
	private Style createPreviewStyle() {
		final TextSymbolizer tSymbolizer = rulesList.getSymbolizer();

		final Style style = StylingUtil.STYLE_BUILDER.createStyle();

		// We use the actual styling defined as the default
		final AbstractRuleList lastChangedRuleList = atlasStyler
				.getLastChangedRuleList();
		if (lastChangedRuleList != null) {
			style.featureTypeStyles().add(lastChangedRuleList.getFTS());
		}

		style.featureTypeStyles().add(
				StylingUtil.STYLE_BUILDER.createFeatureTypeStyle(tSymbolizer));
		return style;
	}

	/**
	 * This method initializes jComboBox
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxDisplacementX() {
		if (jComboBoxPointPlacementDisplacementX == null) {
			jComboBoxPointPlacementDisplacementX = new JComboBox(
					new DefaultComboBoxModel(POINTDISPLACEMENT_VALUES));
			jComboBoxPointPlacementDisplacementX
					.setRenderer(POINTDISPLACEMENT_VALUES_RENDERER);

			PointPlacement pPlacement = (PointPlacement) rulesList
					.getSymbolizer().getLabelPlacement();

			ASUtil.selectOrInsert(jComboBoxPointPlacementDisplacementX,
					pPlacement.getDisplacement().getDisplacementX());

			jComboBoxPointPlacementDisplacementX
					.addItemListener(new ItemListener() {

						public void itemStateChanged(ItemEvent e) {
							if (e.getStateChange() == ItemEvent.SELECTED) {

								PointPlacement pPlacement = (PointPlacement) rulesList
										.getSymbolizer().getLabelPlacement();

								pPlacement.getDisplacement().setDisplacementX(
										ASUtil.ff2.literal(e.getItem()));

								// firePropertyChange(PROPERTY_UPDATED, null,
								// null);
								rulesList.fireEvents(new RuleChangedEvent(
										"placement changed", rulesList));
							}

						}
					});
			SwingUtil
					.addMouseWheelForCombobox(jComboBoxPointPlacementDisplacementX);
		}
		return jComboBoxPointPlacementDisplacementX;
	}

	/**
	 * This method initializes jComboBoY
	 * 
	 * @return javaY.swing.JComboBoY
	 */
	private JComboBox getJComboBoxDisplacementY() {
		if (jComboBoxPointPlacementDisplacementY == null) {
			jComboBoxPointPlacementDisplacementY = new JComboBox(
					POINTDISPLACEMENT_VALUES);
			jComboBoxPointPlacementDisplacementY
					.setRenderer(POINTDISPLACEMENT_VALUES_RENDERER);

			PointPlacement pPlacement = (PointPlacement) rulesList
					.getSymbolizer().getLabelPlacement();

			ASUtil.selectOrInsert(jComboBoxPointPlacementDisplacementY,
					pPlacement.getDisplacement().getDisplacementY());

			jComboBoxPointPlacementDisplacementY
					.addItemListener(new ItemListener() {

						public void itemStateChanged(ItemEvent e) {
							if (e.getStateChange() == ItemEvent.SELECTED) {

								PointPlacement pPlacement = (PointPlacement) rulesList
										.getSymbolizer().getLabelPlacement();

								pPlacement.getDisplacement().setDisplacementY(
										ASUtil.ff2.literal(e.getItem()));

								// firePropertyChange(PROPERTY_UPDATED, null,
								// null);
								rulesList.fireEvents(new RuleChangedEvent(
										"placement changed", rulesList));
							}

						}
					});
			SwingUtil
					.addMouseWheelForCombobox(jComboBoxPointPlacementDisplacementY);
		}
		return jComboBoxPointPlacementDisplacementY;
	}

	/**
	 * This method initializes jComboBoY
	 * 
	 * @return javaY.swing.JComboBoY
	 */
	private JComboBox getJComboBoxAnchorY() {
		if (jComboBoxPointPlacementAnchorY == null) {
			jComboBoxPointPlacementAnchorY = new JComboBox(
					new DefaultComboBoxModel(ANCHORVALUES));
			jComboBoxPointPlacementAnchorY.setRenderer(ANCHORVALUES_RENDERER);

			PointPlacement pPlacement = (PointPlacement) rulesList
					.getSymbolizer().getLabelPlacement();

			ASUtil.selectOrInsert(jComboBoxPointPlacementAnchorY, pPlacement
					.getAnchorPoint().getAnchorPointY());

			jComboBoxPointPlacementAnchorY.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						PointPlacement pPlacement = (PointPlacement) rulesList
								.getSymbolizer().getLabelPlacement();

						pPlacement.getAnchorPoint().setAnchorPointY(
								ASUtil.ff2.literal(e.getItem()));

						// firePropertyChange(PROPERTY_UPDATED, null, null);
						rulesList.fireEvents(new RuleChangedEvent(
								"placement changed", rulesList));
					}

				}
			});
			SwingUtil.addMouseWheelForCombobox(jComboBoxPointPlacementAnchorY);
		}
		return jComboBoxPointPlacementAnchorY;
	}

	private JComboBox getJComboBoxAnchorX() {
		if (jComboBoxPointPlacementAnchorX == null) {
			jComboBoxPointPlacementAnchorX = new JComboBox();
			jComboBoxPointPlacementAnchorX.setModel(new DefaultComboBoxModel(
					ANCHORVALUES));
			jComboBoxPointPlacementAnchorX.setRenderer(ANCHORVALUES_RENDERER);

			PointPlacement pPlacement = (PointPlacement) rulesList
					.getSymbolizer().getLabelPlacement();

			ASUtil.selectOrInsert(jComboBoxPointPlacementAnchorX, pPlacement
					.getAnchorPoint().getAnchorPointX());

			jComboBoxPointPlacementAnchorX.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						PointPlacement pPlacement = (PointPlacement) rulesList
								.getSymbolizer().getLabelPlacement();

						pPlacement.getAnchorPoint().setAnchorPointX(
								ASUtil.ff2.literal(e.getItem()));

						rulesList.fireEvents(new RuleChangedEvent(
								"placement changed", rulesList));
					}

				}
			});
			SwingUtil.addMouseWheelForCombobox(jComboBoxPointPlacementAnchorX);
		}
		return jComboBoxPointPlacementAnchorX;
	}

	private JComboBox getJComboBoxLabelRotation() {
		if (jComboBoxLabelRotation == null) {
			jComboBoxLabelRotation = new JComboBox();
			jComboBoxLabelRotation.setModel(new DefaultComboBoxModel(
					ROTATION_VALUES));
			jComboBoxLabelRotation.setRenderer(ROTATION_VALUES_RENDERER);

			PointPlacement pPlacement = (PointPlacement) rulesList
					.getSymbolizer().getLabelPlacement();

			ASUtil.selectOrInsert(jComboBoxLabelRotation, pPlacement
					.getRotation());

			jComboBoxLabelRotation.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {

						PointPlacement pPlacement = (PointPlacement) rulesList
								.getSymbolizer().getLabelPlacement();

						pPlacement.setRotation(ASUtil.ff2.literal(e.getItem()));

						// firePropertyChange(PROPERTX_UPDATED, null, null);
						rulesList.fireEvents(new RuleChangedEvent(
								"label rotation changed", rulesList));
					}

				}
			});
			SwingUtil.addMouseWheelForCombobox(jComboBoxLabelRotation);
		}
		return jComboBoxLabelRotation;
	}

	/**
	 * A {@link JPanel} to define point placement settings
	 */
	private JPanel getJPanelPointPlacement() {
		JPanel jPanelPlacement = new JPanel(new MigLayout("wrap 3, fillx"),
				AtlasStyler.R("TextRulesList.Button.PlacementProperties"));

		// Avoid nulls and fill with default if needed
		{

			if (rulesList.getSymbolizer().getLabelPlacement() == null
					|| !(rulesList.getSymbolizer().getLabelPlacement() instanceof PointPlacement)) {
				rulesList.getSymbolizer().setLabelPlacement(
						StylingUtil.STYLE_BUILDER.createPointPlacement());
			}

			PointPlacement pPlacement = (PointPlacement) rulesList
					.getSymbolizer().getLabelPlacement();
			if (pPlacement.getAnchorPoint() == null) {
				pPlacement.setAnchorPoint(StylingUtil.STYLE_BUILDER
						.createAnchorPoint(0.5, 0.5));
			}

			if (pPlacement.getDisplacement() == null) {
				pPlacement.setDisplacement(StylingUtil.STYLE_BUILDER
						.createDisplacement(0., 0.));
			}

			if (pPlacement.getRotation() == null) {
				pPlacement.setRotation(StylingUtil.STYLE_BUILDER
						.literalExpression(0.0));
			}
		}

		jPanelPlacement.add(new JLabel(AtlasStyler
				.R("TextRuleListGUI.PointPlacement.Displacement")), "right");
		// jPanelPlacement
		// .add(new JLabel(AtlasStyler
		// .R("TextRuleListGUI.PointPlacement.Displacement.X")),
		// "split 2");
		jPanelPlacement.add(getJComboBoxDisplacementX(), "split 2");
		jPanelPlacement.add(getJComboBoxDisplacementY(), "left");
		// jPanelPlacement
		// .add(new JLabel(AtlasStyler
		// .R("TextRuleListGUI.PointPlacement.Displacement.Y")),
		// "split 2");

		jPanelPlacement.add(new JLabel(AtlasStyler
				.R("TextRuleListGUI.PointPlacement.Anchor.Rotation")),
				"split 2, right");
		jPanelPlacement.add(getJComboBoxLabelRotation(), "right");

		jPanelPlacement.add(new JLabel(AtlasStyler
				.R("TextRuleListGUI.PointPlacement.Anchor")), "right");
		jPanelPlacement.add(getJComboBoxAnchorX(), "split 2");
		jPanelPlacement.add(getJComboBoxAnchorY(), "left");

		return jPanelPlacement;
	}

	/**
	 * A {@link JPanel} to define Line placement settings
	 */
	private JPanel getJPanelLinePlacement() {
		JPanel jPanelPlacement = new JPanel(new MigLayout("nogrid, fillx"),
				AtlasStyler.R("TextRulesList.Button.PlacementProperties"));
		{

			if (rulesList.getSymbolizer().getLabelPlacement() == null
					|| !(rulesList.getSymbolizer().getLabelPlacement() instanceof LinePlacement)) {
				rulesList.getSymbolizer().setLabelPlacement(
						StylingUtil.STYLE_BUILDER.createLinePlacement(0.));
			}

			// Avoid nulls and fill with default if needed
			final LinePlacement lPlacement = (LinePlacement) rulesList
					.getSymbolizer().getLabelPlacement();

			if (lPlacement.getGap() == null) {
				lPlacement.setGap(StylingUtil.STYLE_BUILDER
						.literalExpression("0"));
			}

			if (lPlacement.getPerpendicularOffset() == null) {
				lPlacement.setPerpendicularOffset(StylingUtil.STYLE_BUILDER
						.literalExpression("0"));
			}

			if (lPlacement.getInitialGap() == null) {
				lPlacement.setInitialGap(StylingUtil.STYLE_BUILDER
						.literalExpression("0"));
			}
		}

		// Avoid nulls and fill with default if needed
		final LinePlacement lPlacement = (LinePlacement) rulesList
				.getSymbolizer().getLabelPlacement();

		// PerpendicularOffset JCombobox
		{

			final JComboBox jComboBoxLinePlacementPerpendicularGap = new JComboBox(
					new DefaultComboBoxModel(POINTDISPLACEMENT_VALUES));
			jComboBoxLinePlacementPerpendicularGap
					.setRenderer(POINTDISPLACEMENT_VALUES_RENDERER);
			ASUtil.selectOrInsert(jComboBoxLinePlacementPerpendicularGap,
					lPlacement.getPerpendicularOffset());
			jComboBoxLinePlacementPerpendicularGap
					.addItemListener(new ItemListener() {

						@Override
						public void itemStateChanged(ItemEvent e) {
							if (e.getStateChange() == ItemEvent.SELECTED) {

								// Avoid nulls and fill with default if needed
								final LinePlacement lPlacement = (LinePlacement) rulesList
										.getSymbolizer().getLabelPlacement();

								lPlacement.setPerpendicularOffset(ASUtil.ff2
										.literal(e.getItem()));

								// firePropertyChange(PROPERTX_UPDATED, null,
								// null);
								rulesList
										.fireEvents(new RuleChangedEvent(
												"line placement PerpendicularOffset changed",
												rulesList));
							}
						}
					});
			jPanelPlacement.add(new JLabel(AtlasStyler
					.R("TextRuleListGUI.LinePlacement.PerpendicularGap")),
					"right, split 2, gap right rel");
			jPanelPlacement.add(jComboBoxLinePlacementPerpendicularGap,
					"left, gap right unrel");
			SwingUtil
					.addMouseWheelForCombobox(jComboBoxLinePlacementPerpendicularGap);
		}
		return jPanelPlacement;
	}

}
