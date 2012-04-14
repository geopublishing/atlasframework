package org.geopublishing.atlasStyler.chartgraphic;

import java.awt.Color;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

		for (GraphicalSymbol gs : importThis.graphicalSymbols()) {
			if (gs instanceof ExternalGraphic) {
				ExternalGraphic eg = (ExternalGraphic) gs;
				try {
					if (eg.getLocation() == null)
						continue;
					String url = eg.getLocation().toString();

					// Read the ATTRIBUTE NAMES from the Style
					Pattern nextAtt = RegexCache.getInstance().getPattern(
							"\\$\\{(.*?)\\}");
					Matcher m = nextAtt.matcher(url);
					while (m.find()) {
						attributes.add(m.group(1));
					}

					// Read the COLORS from the Style
					Pattern colorPart = RegexCache.getInstance().getPattern(
							"chco=(.*[,&])");
					m = colorPart.matcher(url);
					if (m.find() && m.groupCount() > 0) {
						String colorstr = m.group(1);
						Pattern nextColor = RegexCache.getInstance()
								.getPattern("(......)[,&]");
						m = nextColor.matcher(colorstr);
						int count = 0;
						while (m.find()) {
							colors.put(attributes.get(count),
									Color.decode("#" + m.group(1)));
							count++;
						}

					}

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

	public enum ChartTyp {
		p, bhs, bvg;

		/**
		 * @return a human-readable and translated Title
		 */
		public String getTitle() {
			return ASUtil.R("ChartTyp." + this.toString());
		}
	}

	private ChartTyp chartType = ChartTyp.bvg;

	List<String> attributes = new ArrayList<String>();

	public ExternalGraphic getChartGraphic() {

		StringBuffer url = new StringBuffer("http://chart?cht="
				+ getChartType());

		// StringBuffer url = new StringBuffer(
		// "http://chart?cht=bvg&chs=50x100&chd=t:${MEAT}|${DEMSTAT}&chco=4D89F9,C6D9FD");
		// Data
		if (attributes.size() > 0) {

			url.append("&chd=t:");
			for (String att : attributes) {
				url.append("${" + att + "}");

				if (chartType == ChartTyp.p)
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

		url.append("&chs=50x50");

		ExternalGraphic crt = StylingUtil.STYLE_BUILDER.createExternalGraphic(
				url.toString(), "application/chart");

		return crt;
	}

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

	public void fireEvents(ChartGraphicChangedEvent cgce) {

		for (ChartGraphicChangeListener l : listeners) {
			try {
				l.changed(cgce);
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
}
