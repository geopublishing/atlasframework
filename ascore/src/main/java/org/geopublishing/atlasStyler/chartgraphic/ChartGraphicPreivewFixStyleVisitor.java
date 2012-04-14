package org.geopublishing.atlasStyler.chartgraphic;

import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;

import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.regex.RegexCache;

/**
 * When a dynamic Chart Symvol is previewed, the freemarker(?) expressions (like
 * "${COUNTHOUSES}") are replaced with fixed values;
 */
public class ChartGraphicPreivewFixStyleVisitor extends DuplicatingStyleVisitor {

	final static String regex = "\\$\\{.*?\\}";

	@Override
	public ExternalGraphic copy(ExternalGraphic eg) {
		if (!ChartGraphic.isChart(eg)) {
			return super.copy(eg);
		}

		String url2;
		try {
			url2 = eg.getLocation().toString();

			// Assume a Bar-Chart:
			// Howmany placeholders to we have?

			int count = 0;
			Matcher m = RegexCache.getInstance().getPattern(regex).matcher(url2);
			while (m
					.find()) {
				count++;
			}
			
			int count2 = 0;
			m = RegexCache.getInstance().getPattern(regex).matcher(url2);
			while (m
					.find()) {
				count2++;
				url2 = url2.replaceFirst(regex, (int) (100. / (double) count * count2)
						+ "");
				// url2 = url2.replaceFirst(regex, ((int) (Math.random() * 100))
				// + "");
			}

			ExternalGraphic externalGraphic2 = StylingUtil.STYLE_BUILDER
					.createExternalGraphic(url2, eg.getFormat());

			return externalGraphic2;
		} catch (Exception e) {
			Logger.getLogger(ChartGraphicPreivewFixStyleVisitor.class).error(e,
					e);
			return eg;
		}

	}
}
