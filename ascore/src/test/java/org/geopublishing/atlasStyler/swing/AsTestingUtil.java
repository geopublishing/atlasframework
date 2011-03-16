package org.geopublishing.atlasStyler.swing;

import java.io.IOException;
import java.net.URL;

import org.geopublishing.atlasStyler.AtlasStylerRaster;
import org.geopublishing.atlasStyler.AtlasStylerVector;
import org.geotools.styling.Style;

import de.schmitzm.geotools.styling.StylingUtil;
import de.schmitzm.geotools.testing.GTTestingUtil.TestDatasetsRaster;
import de.schmitzm.geotools.testing.GTTestingUtil.TestDatasetsVector;
import de.schmitzm.testing.TestingUtil;

public class AsTestingUtil extends TestingUtil {

	/**
	 * Provides access to SLDs
	 */
	public enum TestDatasetsSld {
		textRulesDefaultLocalizedPre16("/oldLocalizedDefaultRule_pre16.sld"), textRulesPre15(
				"/oldTextRuleClasses_Pre15.sld");

		private final String resLoc;

		TestDatasetsSld(String resLoc) {
			this.resLoc = resLoc;
		}

		public Style getStyle() {
			return StylingUtil.loadSLD(getUrl())[0];
		}

		public String getResLoc() {
			return resLoc;
		}

		public URL getUrl() {
			return AsTestingUtil.class.getResource(resLoc);
		}

	}

	/**
	 * Provides an AtlasStyler linked with a StyledFS from the
	 * {@link TestDatasetsVector}.
	 */
	public static AtlasStylerVector getAtlasStyler(TestDatasetsVector testdata)
			throws IOException {
		return new AtlasStylerVector(testdata.getStyledFS());
	}

	/**
	 * Provides an AtlasStyler linked with a StyledFS from the
	 * {@link TestDatasetsVector}.
	 */
	public static AtlasStylerRaster getAtlasStyler(TestDatasetsRaster testdata)
			throws IOException {
		return new AtlasStylerRaster(testdata.getReader());
	}

}
