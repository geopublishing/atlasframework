package org.geopublishing.atlasStyler;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Symbolizer;
import org.opengis.style.Fill;
import org.opengis.style.GraphicFill;
import org.opengis.style.GraphicalSymbol;

import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.regex.RegexCache;

/**
 * https://developers.google.com/chart/image/docs/chart_params
 * https://developers.google.com/chart/image/docs/chart_playground
 */
public class ChartGraphic {

	/**
	 * Returns <code>true</code> if the given Graphic is an ExternalGraphic with
	 * a URL that starts with "http://chart?"
	 */
	public static boolean isChart(Symbolizer symbolizer) {
		if (symbolizer == null)
			return false;
		if (symbolizer instanceof PointSymbolizer) {
			Graphic graphic = ((PointSymbolizer) symbolizer).getGraphic();
			return isChart(graphic);
		} else if (symbolizer instanceof PolygonSymbolizer) {
			Fill fill = ((PolygonSymbolizer) symbolizer).getFill();
			if (fill == null)
				return false;
			GraphicFill gf = fill.getGraphicFill();
			if (gf == null)
				return false;
			// TODO
			// TODO
			return false;
			// return isChart(gf.graphicalSymbols());
		} else if (symbolizer instanceof LineSymbolizer) {
			org.geotools.styling.Stroke fill = ((LineSymbolizer) symbolizer)
					.getStroke();
			if (fill == null)
				return false;
			Graphic graphic = fill.getGraphicStroke();
			return isChart(graphic);
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the given Graphic is an ExternalGraphic with
	 * a URL that starts with "http://chart?"
	 */
	public static boolean isChart(Graphic graphic) {
		try {
			if (graphic == null)
				return false;
			List<GraphicalSymbol> graphicalSymbols = graphic.graphicalSymbols();
			if (graphicalSymbols.size() == 0)
				return false;
			GraphicalSymbol graphicalSymbol = graphicalSymbols.get(0);
			if (!(graphicalSymbol instanceof ExternalGraphic))
				return false;
			ExternalGraphic eg = (ExternalGraphic) graphicalSymbol;
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
	}

	public ChartGraphic() {
		this(null);
	}

	public enum ChartTyp {
		p, bhs, bvg,

		ChartTyp() {
		};

		/**
		 * @return a human-readable and translated Title
		 */
		public String getTitle() {
			return ASUtil.R("ChartTyp." + this.toString());
		}
	}

	ChartTyp chartType = ChartTyp.bvg;

	List<String> attributes = new ArrayList<String>();

	public ExternalGraphic getChartGraphic() {

		StringBuffer url = new StringBuffer("http://chart?cht=" + chartType);

		// StringBuffer url = new StringBuffer(
		// "http://chart?cht=bvg&chs=50x100&chd=t:${MEAT}|${DEMSTAT}&chco=4D89F9,C6D9FD");
		// Data
		if (attributes.size() > 0) {

			url.append("&chd=t:");
			for (String att : attributes) {
				url.append("${" + att + "}");
				url.append("|");
			}
			url.setLength(url.length() - 1);
		}
		//
		url.append("&chco=4D89F9");
		url.append("&chs=50x50");

		ExternalGraphic crt = StylingUtil.STYLE_BUILDER.createExternalGraphic(
				url.toString(), "application/chart");

		return crt;
	}

	public void addAttribute(String att) {
		attributes.add(att);
	}

	public void removeAttribute(int index) {
		attributes.remove(index);
	}

	static final Logger LOGGER = Logger.getLogger(ChartGraphic.class);

	public static Symbolizer getFixDataSymbolizer(Symbolizer sym) {
		if (!isChart(sym))
			return null;
		if (sym instanceof PointSymbolizer) {
			return getFixDataSymbolizer(((PointSymbolizer) sym).getGraphic());
		} else {
			// TODO
			// TODO
			LOGGER.warn("TODO");
		}

		return null;

	}

	public static Symbolizer getFixDataSymbolizer(Graphic graphic) {

		if (!isChart(graphic))
			return null;

		ExternalGraphic externalGraphic = graphic.getExternalGraphics()[0];

		String url2;
		try {
			url2 = externalGraphic.getLocation().toString();
			String regex = "\\$\\{.*?\\}";
			while (RegexCache.getInstance().getPattern(regex).matcher(url2)
					.find()) {
				url2 = url2.replaceFirst(regex, ((int) (Math.random() * 100))
						+ "");
			}

			ExternalGraphic externalGraphic2 = StylingUtil.STYLE_BUILDER
					.createExternalGraphic(url2, externalGraphic.getFormat());
			Graphic graphic2 = StylingUtil.STYLE_BUILDER.createGraphic(
					externalGraphic2, null, null);

			PointSymbolizer symbolizer = StylingUtil.STYLE_BUILDER
					.createPointSymbolizer(graphic2);

			return symbolizer;
		} catch (MalformedURLException e) {
			LOGGER.error("Could not create a fixed data chart symbolizer", e);
			return null;
		}

	}

}
