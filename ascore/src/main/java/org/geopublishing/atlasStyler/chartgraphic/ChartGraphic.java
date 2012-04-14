package org.geopublishing.atlasStyler.chartgraphic;

import java.awt.Color;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Graphic;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;
import org.geotools.util.WeakHashSet;
import org.opengis.style.GraphicalSymbol;

import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.regex.RegexCache;
import de.schmitzm.swing.SwingUtil;

/**
 * https://developers.google.com/chart/image/docs/chart_params
 * https://developers.google.com/chart/image/docs/chart_playground
 */
public class ChartGraphic {

	public enum ChartTyp {
		p, p3, bhg, bvg, bhs, bvs, bvo, lc;

		/**
		 * @return a human-readable and translated Title
		 */
		public String getTitle() {
			return ASUtil.R("ChartTyp." + this.toString());
		}
	}

	public ExternalGraphic getChartGraphic() {

		StringBuffer url = new StringBuffer("http://chart?cht="
				+ getChartType());

		// StringBuffer url = new StringBuffer(
		// "http://chart?cht=bvg&chs=50x100&chd=t:${MEAT}|${DEMSTAT}&chco=4D89F9,C6D9FD");
		// Data
		if (attributes.size() > 0) {

			url.append("&chd=t:");
			for (String att : attributes) {

				if (chartType == ChartTyp.p || chartType == ChartTyp.p3
						|| chartType == ChartTyp.lc) {

					// Sum up all others in this "row" to 100
					StringBuffer allOthers = new StringBuffer();

					for (String a : attributes) {
						allOthers.append(a);
						allOthers.append("+");
					}
					allOthers.setLength(allOthers.length() - 1);

					url.append("${100 * " + att + " / (" + allOthers + ")}");

				} else {

					// For Barcharts scale all numbers so that maxValue equals
					// 100.

					url.append("${" + att + " * 100. / " + getMaxValue() + "}");

				}

				if (chartType == ChartTyp.p || chartType == ChartTyp.p3
						|| chartType == ChartTyp.lc)
					url.append(",");
				else
					url.append("|");
			}
			url.setLength(url.length() - 1);

			// Write colors:
			url.append("&chco=");
			for (String att : attributes) {
				url.append(SwingUtil.convertColorToHex(getColor(att), false,
						false));
				url.append(",");
			}
			url.setLength(url.length() - 1);

		}

		// Chart Background Color works: set it fully transparent here.
		// backgrouds can be defined with other symbolizers
		url.append("&chf=bg,s,FFFFFF00");

		// url.append("&chdl=NASDAQ|FTSE100|DOW");

		// System.err.println("&chf=bg,s," + getBackgroundColorString());

		// Bar Chart Gaps don't seem to work :-/
		// https://developers.google.com/chart/image/docs/gallery/bar_charts#chbh
		// url.append("&chbh=a,5,15");
		// // Chart Margins don't seem to work :-/
		// //
		// https://developers.google.com/chart/image/docs/chart_params#gcharts_chart_margins
		// url.append("&chma=1,1,1,1");
		//
		// // Hiding chart axes don't seem to work :-/
		// url.append("&chxt=");

		int w = imageWidth == null ? DEFAULT_WIDTH : imageWidth;
		int h = imageHeight == null ? DEFAULT_HEIGHT : imageHeight;
		url.append("&chs=" + w + "x" + h);

		System.err.println(url);

		ExternalGraphic crt = StylingUtil.STYLE_BUILDER.createExternalGraphic(
				url.toString(), "application/chart");

		return crt;
	}

	/**
	 * Mapping of the colors for the attributes
	 */
	HashMap<String, Color> colors = new HashMap<String, Color>();

	/**
	 * Returns <code>true</code> if the given Graphic is an ExternalGraphic with
	 * a URL that starts with "http://chart?"
	 */
	public static boolean isChart(Symbolizer symbolizer) {
		if (symbolizer == null)
			return false;

		final AtomicBoolean isChart = new AtomicBoolean(false);
		DuplicatingStyleVisitor v = new DuplicatingStyleVisitor() {

			@Override
			public void visit(ExternalGraphic eg) {
				super.visit(eg);
				isChart.set(isChart(eg));
			}
		};
		symbolizer.accept(v);

		return isChart.get();
	}

	/**
	 * Scale the data, so that his {@link #maxValue} is transformed to 100. -
	 * which is by Eastwood-default the top of the value axis.
	 */
	private Double maxValue = 100.;

	/**
	 * Returns <code>true</code> if the given Graphic is an ExternalGraphic with
	 * a URL that starts with "http://chart?"
	 */
	public static boolean isChart(Graphic graphic) {
		if (graphic == null)
			return false;
		final AtomicBoolean isChart = new AtomicBoolean(false);
		DuplicatingStyleVisitor v = new DuplicatingStyleVisitor() {

			@Override
			public void visit(ExternalGraphic eg) {
				super.visit(eg);
				isChart.set(isChart(eg));
			}
		};
		graphic.accept(v);

		return isChart.get();
	}

	public static boolean isChart(ExternalGraphic eg) {
		try {
			if (eg.getLocation() == null)
				return false;
			if (eg.getLocation().toString().toLowerCase()
					.startsWith("http://chart?"))
				return true;

		} catch (MalformedURLException e) {
		}
		return false;
	}

	public ChartGraphic(Graphic importThis) {
		importChartFromGraphic(importThis);
	}

	/**
	 * Tries to find a gt-chart defined in this Graphic object. If a chart
	 * definition can be found, its settings are stored in this
	 * {@link ChartGraphic} object.
	 */
	private void importChartFromGraphic(Graphic importThis) {

		if (importThis == null)
			return;

		for (GraphicalSymbol gs : importThis.graphicalSymbols()) {
			if (gs instanceof ExternalGraphic) {

				ExternalGraphic eg = (ExternalGraphic) gs;
				try {
					if (eg.getLocation() == null)
						continue;
					String url = eg.getLocation().toString();

					/*
					 * Parse the URL-Parameters, but do NOT CALL THE SETTERS!
					 */

					// Read the ATTRIBUTE NAMES from the Style
					Pattern nextAtt = RegexCache.getInstance().getPattern(
							"\\$\\{([^ ]*?)( .*?|)\\}");
					Matcher m = nextAtt.matcher(url);
					while (m.find() && m.groupCount() > 0) {
						attributes.add(m.group(1));
					}

					// Read the COLORS from the Style
					Pattern colorPart = RegexCache.getInstance().getPattern(
							"chco=([^&]*?)(&.*|$)");
					m = colorPart.matcher(url);
					if (m.find() && m.groupCount() == 2) {
						String colorstr = m.group(1);
						Pattern nextColor = RegexCache.getInstance()
								.getPattern("([a-f,A-F,0-9]{6,6})(?:[&,]|$)?");
						m = nextColor.matcher(colorstr);
						int count = 0;
						while (m.find() && m.groupCount() == 1) {
							colors.put(attributes.get(count),
									Color.decode("#" + m.group(1)));
							count++;
						}
					}

					// Read any size from the Style
					Pattern sizePart = RegexCache.getInstance().getPattern(
							"chs=(\\d+)x(\\d+)");
					m = sizePart.matcher(url);
					if (m.find() && m.groupCount() == 2) {
						imageWidth = Integer.valueOf(m.group(1));
						imageHeight = Integer.valueOf(m.group(2));
					}

					// Try to find a maxValue
					Pattern maxValuePattern = RegexCache
							.getInstance()
							.getPattern(
									"\\$\\{([^ \\*]*?) \\* 100\\. / ([^ \\*]*)\\}");
					m = maxValuePattern.matcher(url);
					if (m.find() && m.groupCount() == 2) {
						maxValue = Double.valueOf(m.group(2));
					}

					break;

				} catch (MalformedURLException e) {
					LOGGER.warn(e, e);
					continue;
				}
			}
		}

	}

	public ChartGraphic() {
		this(null);
	}

	private ChartTyp chartType = ChartTyp.bvg;

	List<String> attributes = new ArrayList<String>();

	/**
	 * Value between 0 and 1 for the transparency of the background
	 */
	// private float opacity = 0f;
	//
	// private Color bgColor = Color.white;

	// /**
	// * Creates a Color string like FFFFFFTT, where the 4th Hex-Byte represents
	// * transparenzy.
	// */
	// private String getBackgroundColorString() {
	//
	// return SwingUtil.convertColorToHex(
	// new Color(getBgColor().getRed() / 255f,
	// getBgColor().getGreen() / 255f,
	// getBgColor().getBlue() / 255f, getOpacity()), true,
	// false).toUpperCase();
	// }

	public void addAttribute(String att) {

		if (attributes.contains(att))
			return;

		attributes.add(att);

		fireEvents(new ChartGraphicChangedEvent());
	}

	public void setColor(int index, Color color) {

		setColor(attributes.get(index), color);

	}

	public void setColor(String attName, Color color) {

		if (color != colors.get(attName)) {
			colors.put(attName, color);
			fireEvents(new ChartGraphicChangedEvent());
		}

	}

	public Color getColor(int index) {
		return getColor(attributes.get(index));
	}

	public Color getColor(String attname) {
		if (attname == null)
			return null;
		Color c = colors.get(attname);
		if (c == null) {
			c = Color.red;
			colors.put(attname, c);
		}
		return c;
	}

	public void removeAttribute(int index) {
		colors.remove(attributes.remove(index));
		fireEvents(new ChartGraphicChangedEvent());
	}

	static final Logger LOGGER = Logger.getLogger(ChartGraphic.class);

	public static final int DEFAULT_WIDTH = 60;
	public static final int DEFAULT_HEIGHT = 60;

	public static Symbolizer getFixDataSymbolizer(Symbolizer sym) {
		ChartGraphicPreivewFixStyleVisitor visitor = new ChartGraphicPreivewFixStyleVisitor();
		sym.accept(visitor);
		return (Symbolizer) visitor.getCopy();
	}

	public static Graphic getFixDataSymbolizer(Graphic graphic) {

		ChartGraphicPreivewFixStyleVisitor visitor = new ChartGraphicPreivewFixStyleVisitor();
		graphic.accept(visitor);
		return (Graphic) visitor.getCopy();
	}

	public List<String> getAttributes() {
		return attributes;
	}

	/**
	 * This is a WeakHashSet, so references to the listeners have to exist in
	 * the classes adding the listeners. They shall not be anonymous instances.
	 */
	final WeakHashSet<ChartGraphicChangeListener> listeners = new WeakHashSet<ChartGraphicChangeListener>(
			ChartGraphicChangeListener.class);

	public void addListener(ChartGraphicChangeListener listener) {
		listeners.add(listener);
	}

	public void clearListeners() {
		listeners.clear();
	}

	public void fireEvents(final ChartGraphicChangedEvent cgce) {

		for (final ChartGraphicChangeListener l : listeners) {
			try {

				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						l.changed(cgce);
					}

				});

			} catch (Exception e) {
				LOGGER.error("While fireEvents: " + cgce, e);
			}
		}
	}

	public ChartTyp getChartType() {
		return chartType;
	}

	public void setChartType(ChartTyp chartType) {
		if (this.chartType != chartType) {
			this.chartType = chartType;
			fireEvents(new ChartGraphicChangedEvent());
		}
	}

	public Integer getImageWidth() {
		return imageWidth;
	}

	public void setImageWidth(Integer imageWidth) {
		if (this.imageWidth != imageWidth) {
			this.imageWidth = imageWidth;
			fireEvents(new ChartGraphicChangedEvent());
		}

	}

	public Integer getImageHeight() {
		return imageHeight;
	}

	public void setImageHeight(Integer imageHeight) {
		if (this.imageHeight != imageHeight) {
			this.imageHeight = imageHeight;
			fireEvents(new ChartGraphicChangedEvent());
		}
	}

	//
	// public float getOpacity() {
	// return opacity;
	// }
	//
	// public void setOpacity(float opacity) {
	//
	// if (this.opacity != opacity) {
	//
	// if (opacity > 1f)
	// opacity = 1f;
	// if (opacity < 0f)
	// opacity = 0f;
	// this.opacity = opacity;
	//
	// fireEvents(new ChartGraphicChangedEvent());
	// }
	// }
	//
	// public Color getBgColor() {
	// if (bgColor == null)
	// return Color.white;
	// return bgColor;
	// }
	//
	// public void setBgColor(Color bgColor) {
	// // Mit Absicht kein Check:
	// if (this.bgColor != bgColor) {
	// this.bgColor = bgColor;
	// fireEvents(new ChartGraphicChangedEvent());
	// }
	// }

	public Double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Double maxValue) {
		if (this.maxValue != maxValue) {

			if (maxValue == null || maxValue == 0)
				maxValue = 100.;
			this.maxValue = maxValue;

			if (chartType != ChartTyp.p && chartType != ChartTyp.p3)
				fireEvents(new ChartGraphicChangedEvent());
		}
	}

	private Integer imageWidth;
	private Integer imageHeight;
}
