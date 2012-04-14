package org.geopublishing.atlasStyler.chartgraphic;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.geopublishing.atlasStyler.ASUtil;
import org.geotools.renderer.lite.gridcoverage2d.StyleVisitorAdapter;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Graphic;
import org.geotools.styling.Symbolizer;

import de.schmitzm.geotools.styling.StylingUtil;

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
		
		final AtomicBoolean isChart = new AtomicBoolean(false);
		StyleVisitorAdapter v = new StyleVisitorAdapter() {
			
			@Override
			public void visit(ExternalGraphic eg) {
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
		StyleVisitorAdapter v = new StyleVisitorAdapter() {
			
			@Override
			public void visit(ExternalGraphic eg) {
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
		ChartGraphicPreivewFixStyleVisitor visitor = new ChartGraphicPreivewFixStyleVisitor();
		sym.accept(visitor);
		return (Symbolizer) visitor.getCopy();
	}

	public static Graphic getFixDataSymbolizer(Graphic graphic) {

		ChartGraphicPreivewFixStyleVisitor visitor = new ChartGraphicPreivewFixStyleVisitor();
		graphic.accept(visitor);
		return (Graphic) visitor.getCopy();
	}

}
