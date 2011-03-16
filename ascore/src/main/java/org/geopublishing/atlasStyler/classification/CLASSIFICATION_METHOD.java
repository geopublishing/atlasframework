package org.geopublishing.atlasStyler.classification;

import org.geopublishing.atlasStyler.ASUtil;

/**
 * Different Methods to classify
 */
public enum CLASSIFICATION_METHOD {
	EI, MANUAL, QUANTILES;

	// TODO NATURAL BREAKS

	public String getDesc() {
		return ASUtil.R("QuantitiesClassifiction.Method.ComboboxEntry."
				+ toString());
	}

	public String getToolTip() {
		return ASUtil.R("QuantitiesClassifiction.Method.ComboboxEntry."
				+ toString() + ".TT");
	}

	public static final CLASSIFICATION_METHOD DEFAULT_METHOD = CLASSIFICATION_METHOD.QUANTILES;
}
