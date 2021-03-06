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
package org.geopublishing.atlasStyler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.AtlasStyler.LANGUAGE_MODE;
import org.geopublishing.atlasStyler.rulesLists.SingleLineSymbolRuleList;
import org.geopublishing.atlasStyler.rulesLists.SinglePointSymbolRuleList;
import org.geopublishing.atlasStyler.rulesLists.SinglePolygonSymbolRuleList;
import org.geopublishing.atlasStyler.rulesLists.SingleRuleList;
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;
import org.geotools.brewer.color.PaletteType;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Literal;

import de.schmitzm.geotools.LegendIconFeatureRenderer;
import de.schmitzm.geotools.feature.FeatureUtil;
import de.schmitzm.geotools.feature.FeatureUtil.GeometryForm;
import de.schmitzm.geotools.styling.StyledFeaturesInterface;
import de.schmitzm.geotools.styling.StyledLayerInterface;
import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.geotools.styling.chartsymbols.ChartGraphic;
import de.schmitzm.i18n.Translation;
import de.schmitzm.lang.LangUtil;
import de.schmitzm.lang.ResourceProvider;
import de.schmitzm.swing.CancelButton;
import de.schmitzm.swing.OkButton;
import de.schmitzm.swing.SwingUtil;

public class ASUtil {
	static Logger LOGGER = Logger.getLogger(ASUtil.class);

	/**
	 * {@link ResourceProvider}, der die Lokalisation fuer GUI-Komponenten des
	 * Package {@code skrueger.sld} zur Verfuegung stellt. Diese sind in
	 * properties-Datein unter {@code skrueger.sld} hinterlegt.
	 */
	private final static ResourceProvider RESOURCE = ResourceProvider
			.newInstance("locales.AtlasStylerTranslation", Locale.ENGLISH);

	private static BrewerPalette arthursPalette;
	/**
	 * A flag that allows only one of these dialogs to pop up.
	 */
	static boolean askForStringOpen = false;

	static String askForStringResult;

	public static DecimalFormat df = new DecimalFormat("##0.000");

	private static TableCellRenderer doubleTableCellRenderer;

	public static final FilterFactory ff = FeatureUtil.FILTER_FACTORY;

	public static final FilterFactory2 ff2 = FeatureUtil.FILTER_FACTORY2;

	public static final FileNameExtensionFilter FILTER_SLD = new FileNameExtensionFilter(
			"SLD", "sld", "xml", "SLD", "XML");

	public static final FileNameExtensionFilter FILTER_SVG = new FileNameExtensionFilter(
			"SVG", "svg", "SVG");
	public static final FileNameExtensionFilter FILTER_PNG = new FileNameExtensionFilter(
			"PNG", "png", "PNG");

	public static final FileNameExtensionFilter FILTER_EXTERNALGRAPHIC_FORMATS = new FileNameExtensionFilter(
			"SVG/PNG", "svg", "SVG", "png", "PNG");

	public static final FileNameExtensionFilter FILTER_GML = new FileNameExtensionFilter(
			"GML", "gml", "GML");
	public static final FileNameExtensionFilter FILTER_ALLSUPPORTED = new FileNameExtensionFilter(
			ASUtil.R("Filetype_AllSupported"), "shp", "SHP", "zip", "ZIP",
			"gml", "GML", "tif", "TIF", "tiff", "TIFF", "a00", "asc", "ASC",
			"txt", "ascii", "ASCII");
	public static final FileNameExtensionFilter FILTER_RASTERSUPPORTED = new FileNameExtensionFilter(
			ASUtil.R("Filetype_ImportableRaster"), "tif", "TIF", "tiff",
			"TIFF", "a00", "asc", "ASC", "txt");
	public static final FileNameExtensionFilter FILTER_SHAPE = new FileNameExtensionFilter(
			ASUtil.R("Filetype_ShapeOrShapeAsZip"), "shp", "SHP", "zip", "ZIP");

	public static final StyleBuilder SB = StylingUtil.STYLE_BUILDER;

	public static final String PRODUCTION_FILENAME_PART = "production";

	/**
	 * Asks to edit a {@link String} in a modal dialog. As we are working with
	 * that stupid static {@link String}, we only allow one open {@link JDialog}
	 * 
	 * @param preset
	 *            Optional the {@link String} to start editing with
	 * 
	 * @return <code>null</code> is canceled. Otherwise the new {@link String}
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static String askForString(final Component owner,
			final String preset, final String question) {

		// As we are working with that stupid static String, we only allow one
		// open Dialog
		if (askForStringOpen)
			return preset;

		askForStringOpen = true;
		final JTextField tf = new JTextField(20);
		final JDialog dialog = new JDialog(SwingUtil.getParentWindow(owner)) {

			private static final long serialVersionUID = 6629175844460277695L;

			/**
			 * Since the registerKeyboardAction() method is part of the
			 * JComponent class definition, you must define the Escape keystroke
			 * and register the keyboard action with a JComponent, not with a
			 * JDialog. The JRootPane for the JDialog serves as an excellent
			 * choice to associate the registration, as this will always be
			 * visible. If you override the protected createRootPane() method of
			 * JDialog, you can return your custom JRootPane with the keystroke
			 * enabled:
			 */
			@Override
			protected JRootPane createRootPane() {
				final KeyStroke stroke = KeyStroke.getKeyStroke(
						KeyEvent.VK_ESCAPE, 0);
				final JRootPane rootPane = new JRootPane();
				rootPane.registerKeyboardAction(new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent e) {
						askForStringResult = null;
						dispose();
					}

				}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

				final KeyStroke strokeEnter = KeyStroke.getKeyStroke(
						KeyEvent.VK_ENTER, 0);
				rootPane.registerKeyboardAction(new ActionListener() {

					@Override
					public void actionPerformed(final ActionEvent e) {
						askForStringResult = tf.getText();
						dispose();
					}

				}, strokeEnter, JComponent.WHEN_IN_FOCUSED_WINDOW);

				return rootPane;
			}

		};

		dialog.setLayout(new BorderLayout());
		if (question != null) {
			dialog.setTitle(question);
			dialog.add(new JLabel(question + ":"), BorderLayout.WEST);

		}
		tf.setText(preset);
		dialog.add(tf, BorderLayout.CENTER);

		final JPanel buttons = new JPanel();
		final OkButton okButton = new OkButton();
		okButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				askForStringResult = tf.getText();
				dialog.dispose();
			}

		});
		final CancelButton cancelButton = new CancelButton();
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				askForStringResult = null;
				dialog.dispose();
			}
		});
		buttons.add(okButton);
		buttons.add(cancelButton);
		dialog.add(buttons, BorderLayout.SOUTH);
		dialog.setModal(true);
		dialog.pack();
		SwingUtil.setRelativeFramePosition(dialog, owner, 0.5, .5);
		dialog.setVisible(true);

		askForStringOpen = false;
		return askForStringResult;
	}

	public static Fill createDefaultFill() {
		return SB.createFill();
	}

	public static Graphic createDefaultGraphic() {
		return SB.createGraphic();
	}

	public static Graphic createDefaultGraphicFill() {
		return SB.createGraphic();
	}

	/**
	 * @return a default {@link Mark} with a <code>circle</code>
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static final Mark createDefaultMark() {
		return SB.createMark("circle");
	};

	public static Stroke createDefaultStroke() {
		return SB.createStroke();
	}

	/**
	 * Creates a default Style that is compatible with {@link AtlasStylerVector}
	 * .
	 */
	public static Style createDefaultStyle(
			final StyledLayerInterface<?> styledLayer) {
		final Style loadStyle = StylingUtil.createDefaultStyle(styledLayer);

		if (!(styledLayer instanceof StyledFeaturesInterface<?>)) {
			// Not compatible with AtlasStyler, but if it is no
			// StyledFeaturesInterface, we are not in AtlasStyler. TODO: Is this
			// a nice solution?
			return loadStyle;
		}

		// This method may not instantiate AtlasStyler with the styledLayer! It
		// would be recursive!

		StyledFeaturesInterface styledFs = (StyledFeaturesInterface) styledLayer;

		// final Style compatibleStyle = new
		// AtlasStyler((StyledFeaturesInterface<?>) styledLayer,
		// loadStyle, null, null).getStyle();
		SingleRuleList<? extends Symbolizer> singleRulesList = new RuleListFactory(
				styledFs).createSingleRulesList(
				AtlasStylerVector.getRuleTitleFor(styledFs), true);

		Style defaultStyle = StylingUtil.STYLE_BUILDER.createStyle();
		defaultStyle.featureTypeStyles().add(singleRulesList.getFTS());

		return defaultStyle;
	}

	public static Symbolizer createDefaultSymbolizer(
			final GeometryDescriptor geomType) {

		switch (FeatureUtil.getGeometryForm(geomType)) {
		case POINT:
			return SB.createPointSymbolizer();
		case LINE:
			return SB.createLineSymbolizer();
		case POLYGON:
			return SB.createPolygonSymbolizer();
		default:
			throw new RuntimeException("TYpe not recognized!");
		}

	}

	/**
	 * Creates a default {@link SimpleFeatureType} for a given class type
	 * 
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static SimpleFeatureType createFeatureType(
			final GeometryDescriptor defaultGeometry) {
		return FeatureUtil.createFeatureType(defaultGeometry.getType()
				.getBinding());
	}

	private static BrewerPalette getArthursPalette() throws IOException {

		if (arthursPalette == null) {

			/**
			 * Special hack for Arthur dunkelblau R:0 G:38 B:115
			 * 
			 * blau R: 0 G:0 B:255
			 * 
			 * hellblau R: 51 G: 194 B: 255
			 * 
			 * grün R: 182 G: 255 B:143
			 * 
			 * orange R: 255 G:200 B:0
			 * 
			 * rot R: 255 G:0 B:0
			 * 
			 * dunkel rot R: 168 G:0 B:0
			 */
			arthursPalette = StylingUtil.createBrewerPalette("BlGrRe",
					new Color[] { new Color(0, 38, 115), new Color(0, 0, 255),
							new Color(51, 194, 225), new Color(182, 255, 143),
							new Color(255, 200, 0), new Color(255, 0, 0),
							new Color(168, 0, 0) });

			for (int i = 2; i < 6; i++) {
				arthursPalette.getPaletteSuitability().setSuitability(i,
						new String[] { "?", "?", "?", "?", "?", "?" });
			}

			arthursPalette.getColorScheme().setSampleScheme(2,
					new int[] { 1, 5 });
			arthursPalette.getColorScheme().setSampleScheme(3,
					new int[] { 1, 3, 5 });
			arthursPalette.getColorScheme().setSampleScheme(4,
					new int[] { 1, 2, 4, 5 });
			arthursPalette.getColorScheme().setSampleScheme(5,
					new int[] { 1, 2, 3, 4, 5 });
			arthursPalette.getColorScheme().setSampleScheme(6,
					new int[] { 0, 1, 2, 3, 4, 5 });
			arthursPalette.getColorScheme().setSampleScheme(7,
					new int[] { 0, 1, 2, 3, 4, 5, 6 });
		}
		return arthursPalette;
	}

	/**
	 * Returns the index of the first (and hopefully only) attribute with that
	 * LocalName.
	 * 
	 * @param schema
	 *            The {@link SimpleFeatureType} to search
	 * @param attName
	 *            Attribute name
	 * @return -1 if not attribute with that name is found
	 */
	public static int getAttribIndex(final SimpleFeatureType schema,
			final String attName) {
		for (int position = 0; position < schema.getAttributeCount(); position++) {
			if (schema.getAttributeDescriptors().get(position).getLocalName()
					.equals(attName))
				return position;
		}
		return -1;
	}

	/**
	 * Returns the AttributeDescriptor of the first (and hopefully only)
	 * attribute with that LocalName.
	 * 
	 * @param schema
	 *            The {@link SimpleFeatureType} to search
	 * @param attName
	 *            Attribute name
	 * @return <code>null</code> if no attribute with that name is found
	 */
	public static AttributeDescriptor getAttribType(
			final SimpleFeatureType schema, final String attName) {
		for (int position = 0; position < schema.getAttributeCount(); position++) {
			if (schema.getAttributeDescriptors().get(position).getLocalName()
					.equals(attName))
				return schema.getAttributeDescriptors().get(position);
		}
		return null;
	}

	public static Float getBiggestSize(final Fill fill, Float maxSize) {
		if (fill == null)
			return maxSize;

		maxSize = getBiggestSize(fill.getGraphicFill(), maxSize);

		return maxSize;
	}

	public static Float getBiggestSize(final Graphic graphic, Float maxSize) {
		if (graphic == null)
			return maxSize;

		final Expression size2 = graphic.getSize();
		if (size2 != null && size2 != Expression.NIL) {
			try {
				final Float size = Float.valueOf(size2.toString());
				if (size > maxSize) {
					maxSize = size;
				}
			} catch (final Exception e) {
				LOGGER.error("", e);
			}
		}

		// if (graphic.getMarks() != null) {
		// TODO Stefan Tzeggai 12.9.11 GT8 Migration
		// for (final Mark m : graphic.getMarks()) {
		// final Expression mSize = m.getSize();
		// if (mSize != null && mSize != Expression.NIL) {
		// try {
		// final Float mSizeFloat = Float
		// .valueOf(mSize.toString());
		// if (mSizeFloat > maxSize) {
		// maxSize = mSizeFloat;
		// }
		// } catch (final Exception e) {
		// LOGGER.error(e);
		// }
		// }
		// }
		// }

		// TODO external graphics sizes
		return maxSize;
	}

	public static Float getBiggestSize(final PointSymbolizer ps, Float maxSize) {

		maxSize = getBiggestSize(ps.getGraphic(), maxSize);

		return maxSize;
	}

	public static Float getBiggestSize(final PolygonSymbolizer ps, Float maxSize) {

		maxSize = getBiggestSize(ps.getFill(), maxSize);
		maxSize = getBiggestStrokeWidthOrGraphicSize(ps.getStroke(), maxSize);

		return maxSize;
	}

	public static Float getBiggestWidth(final LineSymbolizer ps, Float maxSize) {
		maxSize = getBiggestStrokeWidthOrGraphicSize(ps.getStroke(), maxSize);
		return maxSize;
	}

	public static Float getBiggestStrokeWidthOrGraphicSize(final Stroke stroke,
			Float maxSize) {
		if (stroke == null)
			return maxSize;

		if (stroke.getGraphicStroke() != null) {

			// If the Stroke uses a GrphicStroke, use it's size

			if (stroke.getGraphicStroke().getSize() != null
					&& stroke.getGraphicStroke().getSize() != Expression.NIL) {
				final Float size = Float.valueOf(stroke.getGraphicStroke()
						.getSize().toString());
				if (size > maxSize) {
					maxSize = size;
				}
			}

		} else {
			if (stroke.getWidth() != null
					&& stroke.getWidth() != Expression.NIL) {
				final Float size = Float.valueOf(stroke.getWidth().toString());
				if (size > maxSize) {
					maxSize = size;
				}
			}
		}

		return maxSize;
	}

	/**
	 * http://www.velocityreviews.com/forums/t146956-popupmenu-for-a-cell-in-a-
	 * jtable.html
	 * 
	 * @param requestor
	 * @return
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static String getClipboardContents(final Object requestor) {
		final Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard()
				.getContents(requestor);
		if (t != null) {
			final DataFlavor df = DataFlavor.stringFlavor;
			if (df != null) {
				try {
					final Reader r = df.getReaderForText(t);
					final char[] charBuf = new char[512];
					final StringBuffer buf = new StringBuffer();
					int n;
					while ((n = r.read(charBuf, 0, charBuf.length)) > 0) {
						buf.append(charBuf, 0, n);
					}
					r.close();
					return (buf.toString());
				} catch (final IOException ex) {
					LOGGER.error(ex);
				} catch (final UnsupportedFlavorException ex) {
					LOGGER.error(ex);
				}
			}
		}
		return null;
	}

	/**
	 * @return A default symbol to use for lines
	 */
	public static SingleLineSymbolRuleList getDefaultLineTemplate() {
		final SingleLineSymbolRuleList rl = new SingleLineSymbolRuleList("");

		final LineSymbolizer symb = (LineSymbolizer) ASUtil
				.createDefaultSymbolizer(rl.getGeometryDescriptor());
		rl.addSymbolizer(symb);

		return rl;
	}

	/**
	 * Returns a default {@link SingleRuleList} symbol for NODATA values. If
	 * {@link AtlasStylerVector} is running in multilanguage mode, it tries to
	 * find a default legend label automatically for all languages.
	 * 
	 * @param colors
	 *            none or one or two color paramters that will be used.
	 */
	public static SingleRuleList getDefaultNoDataSymbol(
			final GeometryForm form, final Color... colors) {
		return getDefaultNoDataSymbol(form, 1., colors);
	}

	/**
	 * Returns a default {@link SingleRuleList} symbol for NODATA values. If
	 * {@link AtlasStylerVector} is running in multilanguage mode, it tries to
	 * find a default legend label automatically for all languages.
	 * 
	 * @param colors
	 *            none or one or two color paramters that will be used.
	 */
	public static SingleRuleList<? extends Symbolizer> getDefaultNoDataSymbol(
			final GeometryForm form, final double opacity_fill,
			final Color... colors) {
		final SingleRuleList<? extends Symbolizer> rl;

		final Color defaultWhite = colors.length > 0 ? colors[0] : Color.WHITE;
		final Color defaultGray = colors.length > 1 ? colors[1]
				: Color.LIGHT_GRAY;

		switch (form) {
		case POINT:
			rl = new SinglePointSymbolRuleList("");
			// A white circle is the default NODATA symbol for points
			rl.addSymbolizer(SB.createPointSymbolizer(SB.createGraphic(null,
					SB.createMark("circle", defaultWhite), null, opacity_fill,
					8., 0.)));
			break;
		case POLYGON:
			rl = new SinglePolygonSymbolRuleList("");
			// A 50% white fill is the default NODATA symbol for polygons
			rl.addSymbolizer(SB.createPolygonSymbolizer(
					SB.createStroke(defaultGray, 1, opacity_fill),
					SB.createFill(defaultWhite, opacity_fill)));
			break;
		default:
		case LINE:
			rl = new SingleLineSymbolRuleList("");
			// A white line is the default NODATA symbol for lines
			rl.addSymbolizer(SB.createLineSymbolizer(defaultWhite));
			break;
		}

		rl.setTitle("NODATA");

		// Find suitable default labels
		if (AtlasStyler.languageMode == LANGUAGE_MODE.ATLAS_MULTILANGUAGE) {

			final Translation nodT = new Translation();
			for (final String lang : AtlasStylerVector.getLanguages()) {

				// Try to find a default for every language
				final String localized = R(new Locale(lang),
						"NoDataLegendEntry.Default");
				nodT.put(lang, localized);
			}

			rl.setRuleTitle(nodT);
		} else
			rl.setLabel(R("NoDataLegendEntry.Default"));
		return rl;
	}

	/**
	 * @return A default symbol to use for points
	 */
	public static SinglePointSymbolRuleList getDefaultPointTemplate() {

		final SinglePointSymbolRuleList rl = new SinglePointSymbolRuleList("");
		rl.addSymbolizer(createDefaultSymbolizer(rl.getGeometryDescriptor()));

		return rl;
	}

	public static SinglePolygonSymbolRuleList getDefaultPolygonTemplate() {
		final SinglePolygonSymbolRuleList rl = new SinglePolygonSymbolRuleList(
				"");

		rl.addSymbolizer(createDefaultSymbolizer(rl.getGeometryDescriptor()));

		return rl;
	}

	public static SingleRuleList<? extends Symbolizer> getDefaultTemplate(
			final GeometryForm geomForm) {
		if (geomForm == GeometryForm.POINT) {
			return getDefaultPointTemplate();
		} else if (geomForm == GeometryForm.LINE) {
			return getDefaultLineTemplate();
		} else if (geomForm == GeometryForm.POLYGON) {
			return getDefaultPolygonTemplate();
		} else
			throw new IllegalArgumentException();
	}

	public static TableCellRenderer getDoubleCellRenderer() {
		if (doubleTableCellRenderer == null)
			doubleTableCellRenderer = new DefaultTableCellRenderer() {

				private static final long serialVersionUID = -7498350601156304636L;

				@Override
				public Component getTableCellRendererComponent(
						final JTable table, final Object value,
						final boolean isSelected, final boolean hasFocus,
						final int row, final int column) {
					final JLabel label = (JLabel) super
							.getTableCellRendererComponent(table, value,
									isSelected, hasFocus, row, column);

					final NumberFormat doubleFormat = NumberFormat
							.getNumberInstance();
					doubleFormat.setMinimumFractionDigits(3);
					doubleFormat.setMaximumFractionDigits(3);
					doubleFormat.setMinimumIntegerDigits(1);

					if (value instanceof Double) {
						final Double val = ((Double) value).doubleValue();
						final String str = doubleFormat.format(val);

						label.setText(str);
					}

					// TODO monospaced font

					label.setHorizontalAlignment(SwingConstants.RIGHT);
					return label;
				}

			};
		return doubleTableCellRenderer;
	}

	/**
	 * @return an image using the AtlasStyler's default size
	 */
	public static BufferedImage getFillGraphicImage(final Graphic fillGraphic,
			final Color color, final Color backgroundColor,
			final SimpleFeatureType featuretype) {
		return getGraphicImage(fillGraphic, color, backgroundColor, 1.,
				AtlasStylerVector.DEFAULT_SYMBOL_PREVIEW_SIZE, featuretype);
	}

	public static BufferedImage getFillImage(final Fill fill,
			final Dimension size, final SimpleFeatureType featureType) {

		final PolygonSymbolizer symb = SB.createPolygonSymbolizer(null, fill);

		return getSymbolizerImage(symb, size, featureType);
	}

	/**
	 * @return an image using the AtlasStyler's default size
	 */
	public static BufferedImage getFillImage(final Fill fill,
			final SimpleFeatureType featureType) {
		return getFillImage(fill,
				AtlasStylerVector.DEFAULT_SYMBOL_PREVIEW_SIZE, featureType);
	}

	public static BufferedImage getGraphicImage(final Graphic graphic,
			final Color color, final Color backgroundColor,
			final double opacity, final Dimension size,
			final SimpleFeatureType featureType) {

		final Fill fill = SB.createFill(color, backgroundColor, opacity,
				graphic);

		return getFillImage(fill, size, featureType);
	}

	/**
	 * When converting a {@link Number} (which is usually a {@link Double}
	 * coming from the classification) to a literal, the SLD StreamingRenderer
	 * need correct {@link Literal}s. This method returns a {@link Literal} that
	 * fits the {@link AttributeDescriptor}s binding type.
	 * 
	 * @param number
	 *            The number to express as a {@link Literal}
	 * @return
	 * @author <a href="mailto:skpublic@wikisquare.de">Stefan Alfons Tzeggai</a>
	 */
	public static Literal getLiteralForField(final AttributeDescriptor at,
			final Number number) {

		final Class<?> c = at.getType().getBinding();

		if (c == Long.class) {
			final long l1 = number.longValue();
			return ff2.literal(l1);
		} else if (c == Byte.class) {
			final byte l1 = number.byteValue();
			return ff2.literal(l1);
		} else if (c == Integer.class) {
			final int l1 = number.intValue();
			return ff2.literal(l1);
		} else if (c == Short.class) {
			final int l1 = number.shortValue();
			return ff2.literal(l1);
		} else {
			return ff2.literal(number);
		}
	}

	/**
	 * Returns a list of {@link BrewerPalette} suitable for the given number of
	 * classes.
	 * 
	 * @param paletteTypeGraduation
	 * @param numClasses
	 *            -1 may be passed.
	 */
	public static BrewerPalette[] getPalettes(
			final PaletteType paletteTypeGraduation, final int numClasses) {

		ColorBrewer brewer;
		try {
			brewer = ColorBrewer.instance(paletteTypeGraduation);
		} catch (final IOException e) {
			LOGGER.error("Error loading new  PaletteType(true, false)", e);
			brewer = ColorBrewer.instance();
		}

		BrewerPalette[] palettes = brewer.getPalettes(paletteTypeGraduation,
				numClasses);

		try {
			final BrewerPalette arthursPalette = getArthursPalette();

			final int maxColors = arthursPalette.getMaxColors();
			if (maxColors >= numClasses)
				palettes = LangUtil.extendArray(palettes, arthursPalette);
		} catch (final IOException e1) {
			LOGGER.error("Creating Arthurs special palette failed:", e1);
		}

		return palettes;
	}

	/**
	 * Liefert eine Liste aller Sprachen, in die die {@link AtlasStylerGUI}
	 * übersetzt ist.
	 */
	public static String[] getSupportedLanguages() {
		final Set<Locale> availableLocales = ResourceProvider
				.getAvailableLocales(RESOURCE, true);
		final ArrayList<String> list = new ArrayList<String>(
				availableLocales.size());
		for (final Locale l : availableLocales) {
			list.add(l.getLanguage());
		}
		return list.toArray(new String[0]);
	}

	public static BufferedImage getSymbolizerImage(Symbolizer symb,
			final Dimension size, final SimpleFeatureType featureType) {

		// TODO Caching?

		// Since this rule might well contain any ChartSymbols (which can not be
		// previewed without modification) we have to check all Symbolizers and
		// change any Chart-Symbolizers for proper preview.

		if (ChartGraphic.isChart(symb))
			symb = ChartGraphic.getFixDataSymbolizer(symb);

		Rule rule = StylingUtil.STYLE_FACTORY.createRule();
		rule.symbolizers().add(symb);

		final BufferedImage image = new LegendIconFeatureRenderer()
				.createImageForRule(rule, featureType, size);

		return image;
	}

	public static BufferedImage getSymbolizerImage(final Symbolizer symbolizer,
			final SimpleFeatureType featureType) {
		return getSymbolizerImage(symbolizer,
				AtlasStylerVector.DEFAULT_SYMBOL_PREVIEW_SIZE, featureType);
	}

	public static SingleLineSymbolRuleList importLineTemplateFromFirstRule(
			final FeatureTypeStyle fts) {

		SingleLineSymbolRuleList tempRL = new SingleLineSymbolRuleList(
				fts.getName());

		try {

			final Rule rule0 = fts.rules().get(0);

			for (final Symbolizer ps : rule0.getSymbolizers()) {
				tempRL.addSymbolizer(ps);
			}

			tempRL.reverseSymbolizers();

		} catch (final Exception e) {
			LOGGER.warn("Error " + e.getLocalizedMessage()
					+ " while importing the template RL. Using default");
			LOGGER.error(e);
			tempRL = getDefaultLineTemplate();

		}

		return tempRL;
	}

	public static SinglePointSymbolRuleList importPointTemplateFromFirstRule(
			final FeatureTypeStyle importFTS) {
		SinglePointSymbolRuleList tempRL = new SinglePointSymbolRuleList(
				importFTS.getName());

		try {

			final Rule rule0 = importFTS.rules().get(0);

			for (final Symbolizer ps : rule0.getSymbolizers()) {
				tempRL.addSymbolizer(ps);
			}
			tempRL.reverseSymbolizers();

		} catch (final Exception e) {
			LOGGER.warn("Error " + e.getLocalizedMessage()
					+ " while importing the template RL. Using default");
			LOGGER.error(e);
			tempRL = getDefaultPointTemplate();
		}

		return tempRL;
	}

	public static SinglePolygonSymbolRuleList importPolygonTemplateFromFirstRule(
			final FeatureTypeStyle importFTS) {
		SinglePolygonSymbolRuleList tempRL = new SinglePolygonSymbolRuleList(
				importFTS.getName());

		try {

			final Rule rule0 = importFTS.rules().get(0);

			for (final Symbolizer ps : rule0.getSymbolizers()) {
				tempRL.addSymbolizer(ps);
			}

			tempRL.reverseSymbolizers();

		} catch (final Exception e) {
			LOGGER.warn("Error " + e.getLocalizedMessage()
					+ " while importing the template RL. Using default");
			LOGGER.error(e);
			tempRL = getDefaultPolygonTemplate();
		}
		return tempRL;
	}

	/**
	 * http://www.velocityreviews.com/forums/t146956-popupmenu-for-a-cell-in-a-
	 * jtable.html
	 */
	public static boolean isClipboardContainingText(final Object requestor) {
		final Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard()
				.getContents(requestor);
		return t != null
				&& (t.isDataFlavorSupported(DataFlavor.stringFlavor) || t
						.isDataFlavorSupported(DataFlavor.plainTextFlavor));
	}

	/**
	 * Convenience method to access the {@link AtlasStylerVector}s translation
	 * resources for a specific {@link Locale}.
	 * 
	 * @param key
	 *            the key for the AtlasStylerTranslation.properties file
	 * @param values
	 *            optional values
	 */
	public static String R(final Locale locale, final String key,
			final Object... values) {
		String string = RESOURCE.getString(key, locale, values);
		if (string.equals("???")) {
			string = "???" + key;
			LOGGER.error("missing key in AS: '" + key + "'");
		}
		return string;

	}

	/**
	 * Convenience method to access the {@link AtlasStylerVector}s translation
	 * resources.
	 * 
	 * @param key
	 *            the key for the AtlasStylerTranslation.properties file
	 * @param values
	 *            optional values
	 */
	public static String R(final String key, final Object... values) {
		String string = RESOURCE.getString(key, values);
		if (string.equals("???")) {
			string = "???" + key;
			LOGGER.error("missing key in AS: '" + key + "'");
		}
		return string;
	}

	public static void replaceFillSize(final Fill fill, final Float factor) {
		if (fill == null)
			return;

		replaceGraphicSize(fill.getGraphicFill(), factor);

	}

	public static void replaceGraphicSize(final Graphic graphic,
			final Float factor) {
		if (graphic == null)
			return;

		final Expression gSize = graphic.getSize();
		if (gSize != null && gSize != Expression.NIL) {
			final Double newSize = Double.valueOf(gSize.toString()) * factor;
			// System.out.println("Changed a Graphics's size from to "
			// + Float.valueOf(graphic.getSize().toString()) + " to "
			// + newSize);
			graphic.setSize(ASUtil.ff2.literal(newSize));
		}

		// else if (graphic.getMarks() != null) {
		// // TODO Stefan Tzeggai 12.9.11 GT8 Migration
		// for (final Mark m : graphic.getMarks()) {
		// final Expression mSize = m.getSize();
		// if (mSize != null && mSize != Expression.NIL) {
		// final Float newSize = Float.valueOf(mSize.toString())
		// * factor;
		// // System.out.println("Changed a Mark's size from to "
		// // + Double.valueOf(m.getSize().toString()) + " to "
		// // + newSize);
		// m.setSize(ASUtil.ff2.literal(newSize));
		// }
		// }
		// }
	}

	/**
	 * Replaces the "main" size in a given {@link PolygonSymbolizer} element
	 */
	public static void replacePolygonSymbolizerSize(final PolygonSymbolizer ps,
			final Float factor) {
		if (ps == null)
			return;

		replaceFillSize(ps.getFill(), factor);

	}

	/**
	 * Rounds all elements of the {@link TreeSet} to the number of digits
	 * specified by {@link #limitsDigits}. The first break (start of the first
	 * interval) is rounded down and the last break (end of last interval) is
	 * rounded up, so that every possible value is still included in one
	 * interval.
	 * 
	 * @param breaksList
	 *            interval breaks
	 * @return a new {@link TreeSet}
	 */
	public static TreeSet<Double> roundLimits(final TreeSet<Double> breaksList,
			final Integer limitsDigits) {
		// No round -> use the original values
		if (limitsDigits == null)
			return breaksList;

		final TreeSet<Double> roundedBreaks = new TreeSet<Double>();
		for (final double value : breaksList) {
			int roundMode = 0; // normal round
			// begin of first interval must be rounded DOWN, so that all
			// values are included
			if (value == breaksList.first())
				roundMode = -1;
			// end of last interval must be rounded UP, so that all
			// values are included
			if (value == breaksList.last())
				roundMode = 1;

			// round value and put it into the new TreeSet
			roundedBreaks.add(LangUtil.round(value, limitsDigits, roundMode));
		}
		return roundedBreaks;
	}

	public static void selectOrInsert(final JComboBox comboBox, Double doubleVal) {

		// Round the value to the second decimal digit

		doubleVal = Math.floor(doubleVal * 100f) / 100f;

		final DefaultComboBoxModel model = ((DefaultComboBoxModel) comboBox
				.getModel());
		final int indexOf = model.getIndexOf(doubleVal);

		if (indexOf < 0) {
			LOGGER.info("The Double expression " + doubleVal
					+ " had to be inserted into the ComboBox");

			final int size = model.getSize();
			final Set<Double> doubles = new TreeSet<Double>();
			doubles.add(doubleVal);

			for (int i = 0; i < size; i++) {
				final Double elementAt = (Double) model.getElementAt(i);
				if (elementAt != null) {
					doubles.add(elementAt);
				} else {
					LOGGER.warn("A null in the JComboBoxmodel has been ignored");
				}
			}
			model.removeAllElements();

			for (final Double f : doubles) {
				model.addElement(f);
			}
		}
		model.setSelectedItem(doubleVal);
	}

	public static void selectOrInsert(final JComboBox comboBox,
			final Expression expression) {
		if (expression instanceof Literal) {
			final Literal lit = (Literal) expression;

			if (comboBox.getItemAt(0) instanceof String) {
				final String stringVal = lit.toString();
				selectOrInsert(comboBox, stringVal);

			} else

			if (comboBox.getItemAt(0) instanceof Double) {
				final Double doubleVal = Double.valueOf(lit.toString());
				selectOrInsert(comboBox, doubleVal);

			}

			else if (comboBox.getItemAt(0) instanceof Float) {
				final Float floatVal = Float.valueOf(lit.toString());
				selectOrInsert(comboBox, floatVal);
			} else {
				throw new RuntimeException("selectOrInsert neede for type "
						+ comboBox.getItemAt(0));
			}
		}

	}

	//
	// public static <ItemClass> void selectOrInsert(final JComboBox comboBox,
	// final ItemClass val) {
	// final DefaultComboBoxModel model = ((DefaultComboBoxModel) comboBox
	// .getModel());
	// final int indexOf = model.getIndexOf(val);
	//
	// if (indexOf < 0) {
	// LOGGER.info("The float expression " + val
	// + " had to be inserted into the ComboBox");
	//
	// final int size = model.getSize();
	// final Set<ItemClass> ints = new TreeSet<ItemClass>();
	// ints.add(val);
	//
	// for (int i = 0; i < size; i++) {
	// ints.add((ItemClass) model.getElementAt(i));
	// }
	// model.removeAllElements();
	//
	// for (final ItemClass f : ints) {
	// model.addElement(f);
	// }
	// }
	// model.setSelectedItem(val);
	// }
	//
	public static void selectOrInsert(final JComboBox comboBox, float floatVal) {

		// Round the value to the second decimal digit
		floatVal = (float) Math.floor(floatVal * 100f) / 100f;

		final DefaultComboBoxModel model = ((DefaultComboBoxModel) comboBox
				.getModel());
		final int indexOf = model.getIndexOf(floatVal);

		if (indexOf < 0) {
			LOGGER.info("The float expression " + floatVal
					+ " had to be inserted into the ComboBox");

			final int size = model.getSize();
			final Set<Float> floats = new TreeSet<Float>();
			floats.add(floatVal);

			for (int i = 0; i < size; i++) {
				final Float elementAt = (Float) model.getElementAt(i);
				if (elementAt != null) {
					floats.add(elementAt);
				} else {
					LOGGER.warn("A null in the JComboBoxmodel has been ignored");
				}
			}
			model.removeAllElements();

			for (final Float f : floats) {
				model.addElement(f);
			}
		}
		model.setSelectedItem(floatVal);
	}

	public static void selectOrInsert(final JComboBox comboBox,
			Integer integerVal) {

		final DefaultComboBoxModel model = ((DefaultComboBoxModel) comboBox
				.getModel());
		final int indexOf = model.getIndexOf(integerVal);

		if (indexOf < 0) {
			LOGGER.info("The integer expression " + integerVal
					+ " had to be inserted into the ComboBox");

			final int size = model.getSize();
			final Set<Integer> integers = new TreeSet<Integer>();
			integers.add(integerVal);

			for (int i = 0; i < size; i++) {
				final Integer elementAt = (Integer) model.getElementAt(i);
				if (elementAt != null) {
					integers.add(elementAt);
				} else {
					LOGGER.warn("A null in the JComboBoxmodel has been ignored");
				}
			}
			model.removeAllElements();

			for (final Integer f : integers) {
				model.addElement(f);
			}
		}
		model.setSelectedItem(integerVal);
	}

	public static void selectOrInsert(final JComboBox comboBox,
			final String stringVal) {

		final DefaultComboBoxModel model = ((DefaultComboBoxModel) comboBox
				.getModel());
		final int indexOf = model.getIndexOf(stringVal);

		if (indexOf < 0) {
			LOGGER.info("The string expression " + stringVal
					+ " had to be inserted into the ComboBox");

			final int size = model.getSize();
			final Set<String> strings = new TreeSet<String>();
			strings.add(stringVal);

			for (int i = 0; i < size; i++) {
				final String elementAt = (String) model.getElementAt(i);
				if (elementAt != null) {
					strings.add(elementAt);
				} else {
					LOGGER.warn("A null in the JComboBoxmodel has been ignored");
				}
			}
			model.removeAllElements();

			for (final String f : strings) {
				model.addElement(f);
			}
		}
		model.setSelectedItem(stringVal);
	}

	/**
	 * Changes the name of the original .sld file to the optimized version
	 */
	public static File changeToOptimizedFilename(File sldFile) {
		String pathname = sldFile.getAbsolutePath()
				.replaceAll("\\.sld", "." + PRODUCTION_FILENAME_PART + ".sld")
				.replaceAll("\\.SLD", "." + PRODUCTION_FILENAME_PART + ".SLD")
				.replaceAll("\\.xml", "." + PRODUCTION_FILENAME_PART + ".xml")
				.replaceAll("\\.XML", "." + PRODUCTION_FILENAME_PART + ".XML");
		return new File(pathname);
	}

}
