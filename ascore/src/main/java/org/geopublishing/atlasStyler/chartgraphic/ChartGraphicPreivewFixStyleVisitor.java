package org.geopublishing.atlasStyler.chartgraphic;

import org.apache.log4j.Logger;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.visitor.DuplicatingStyleVisitor;

import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.regex.RegexCache;

public class ChartGraphicPreivewFixStyleVisitor extends DuplicatingStyleVisitor {

	@Override
	public ExternalGraphic copy(ExternalGraphic eg) {
		if (!ChartGraphic.isChart(eg)) {
			return super.copy(eg);
		}

		String url2;
		try {
			url2 = eg.getLocation().toString();
			String regex = "\\$\\{.*?\\}";
			while (RegexCache.getInstance().getPattern(regex).matcher(url2)
					.find()) {
				url2 = url2.replaceFirst(regex, ((int) (Math.random() * 100))
						+ "");
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
