package org.geopublishing.atlasStyler.swing;

import java.net.URL;

import org.geotools.styling.Style;

import schmitzm.geotools.styling.StylingUtil;
import schmitzm.swing.TestingUtil;

public class AsTestingUtil extends TestingUtil {

	public enum TestSld {
		textRulesDefaultLocalizedPre16("/oldLocalizedDefaultRule_pre16.sld"), textRulesPre15(
				"/oldTextRuleClasses_Pre15.sld");

		private final String resLoc;

		TestSld(String resLoc) {
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

}
